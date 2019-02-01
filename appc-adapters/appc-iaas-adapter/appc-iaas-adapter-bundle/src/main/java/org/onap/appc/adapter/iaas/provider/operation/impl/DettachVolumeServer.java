/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications Copyright (C) 2019 Ericsson
 * =============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ============LICENSE_END=========================================================
 */
package org.onap.appc.adapter.iaas.provider.operation.impl;

import static org.onap.appc.adapter.iaas.provider.operation.common.enums.Operation.DETACHVOLUME_SERVICE;
import static org.onap.appc.adapter.utils.Constants.ADAPTER_NAME;
import com.att.cdp.exceptions.ZoneException;
import com.att.cdp.zones.ComputeService;
import com.att.cdp.zones.Context;
import com.att.cdp.zones.VolumeService;
import com.att.cdp.zones.model.ModelObject;
import com.att.cdp.zones.model.Server;
import com.att.cdp.zones.model.Volume;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Iterator;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.onap.appc.Constants;
import org.onap.appc.adapter.iaas.ProviderAdapter;
import org.onap.appc.adapter.iaas.impl.IdentityURL;
import org.onap.appc.adapter.iaas.impl.RequestContext;
import org.onap.appc.adapter.iaas.impl.RequestFailedException;
import org.onap.appc.configuration.Configuration;
import org.onap.appc.configuration.ConfigurationFactory;
import org.onap.appc.adapter.iaas.impl.VMURL;
import org.onap.appc.adapter.iaas.provider.operation.common.enums.Operation;
import org.onap.appc.adapter.iaas.provider.operation.impl.base.ProviderServerOperation;
import org.onap.appc.exceptions.APPCException;
import com.att.cdp.exceptions.TimeoutException;
import com.att.cdp.openstack.util.ExceptionMapper;
import org.onap.appc.i18n.Msg;
import com.woorea.openstack.base.client.OpenStackBaseException;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

public class DettachVolumeServer extends ProviderServerOperation {
    private final EELFLogger logger = EELFManager.getInstance().getLogger(DettachVolumeServer.class);
    private static final Configuration config = ConfigurationFactory.getConfiguration();

    @Override
    protected ModelObject executeProviderOperation(Map<String, String> params, SvcLogicContext context)
            throws APPCException {
        setMDC(Operation.DETACHVOLUME_SERVICE.toString(), "App-C IaaS Adapter:dettachVolume", ADAPTER_NAME);
        logOperation(Msg.DETTACHINGVOLUME_SERVER, params, context);
        return dettachVolume(params, context);
    }

    private Server dettachVolume(Map<String, String> params, SvcLogicContext ctx) {
        Server server = null;
        RequestContext requestContext = new RequestContext(ctx);
        requestContext.isAlive();
        String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);
        String vmUrl = params.get(ProviderAdapter.PROPERTY_INSTANCE_URL);
        String volumeId = params.get(ProviderAdapter.VOLUME_ID);
        VMURL vm = VMURL.parseURL(vmUrl);
        Context context;
        String tenantName = "Unknown";// to be used also in case of exception
        try {
            if (validateVM(requestContext, appName, vmUrl, vm)) {
                return null;
            }
            IdentityURL ident = IdentityURL.parseURL(params.get(ProviderAdapter.PROPERTY_IDENTITY_URL));
            String identStr = (ident == null) ? null : ident.toString();
            context = getContext(requestContext, vmUrl, identStr);
            if (context != null) {
                tenantName = context.getTenantName();// this variable also is
                                                        // used in case of
                                                        // exception
                requestContext.reset();
                server = lookupServer(requestContext, context, vm.getServerId());
                logger.debug(Msg.SERVER_FOUND, vmUrl, context.getTenantName(), server.getStatus().toString());
                if (volumeId == null || volumeId.isEmpty()) {
                    ctx.setAttribute("VOLUME_STATUS", "FAILURE");
                    doFailure(requestContext, HttpStatus.BAD_REQUEST_400, "Volumeid is mandatory");
                }
                Context contx = server.getContext();
                ComputeService service = contx.getComputeService();
                Volume volume = new Volume();
                VolumeService vs = contx.getVolumeService();
                Volume s = vs.getVolume(volumeId);
                boolean flag = false;
                if (validateDetach(service, vm.getServerId(), volumeId)) {
                    volume.setId(volumeId);
                    logger.info("Ready to Detach Volume from the server:");
                    service.detachVolume(server, volume);
                    flag = true;
                } else {
                    String msg = "Volume with volume id " + volumeId + " cannot be detached as it does not exists";
                    logger.info("Volume doesnot exists:");
                    ctx.setAttribute("VOLUME_STATUS", "FAILURE");
                    doFailure(requestContext, HttpStatus.METHOD_NOT_ALLOWED_405, msg);
                    flag = false;
                }
                if (flag) {
                    if (validateDetach(requestContext, service, vm.getServerId(), volumeId)) {
                        String msg = "Volume with volume id " + volumeId + " cannot be detached ";
                        ctx.setAttribute("VOLUME_STATUS", "FAILURE");
                        doFailure(requestContext, HttpStatus.CONFLICT_409, msg);
                    } else {
                        logger.info("status of detaching volume");
                        ctx.setAttribute("VOLUME_STATUS", "SUCCESS");
                        doSuccess(requestContext);
                    }
                }
                context.close();
            } else {
                ctx.setAttribute("VOLUME_STATUS", "CONTEXT_NOT_FOUND");
            }
        } catch (ZoneException e) {
            String msg = EELFResourceManager.format(Msg.SERVER_NOT_FOUND, e, vmUrl);
            logger.error(msg);
            doFailure(requestContext, HttpStatus.NOT_FOUND_404, msg);
        } catch (RequestFailedException e) {
            logger.error("An error occurred when processing the request", e);
            doFailure(requestContext, e.getStatus(), e.getMessage());
        } catch (Exception e) {
            String msg = EELFResourceManager.format(Msg.DETTACHINGVOLUME_SERVER, e, e.getClass().getSimpleName(),
                    DETACHVOLUME_SERVICE.toString(), vmUrl, tenantName);
            logger.error(msg, e);
            try {
                ExceptionMapper.mapException((OpenStackBaseException) e);
            } catch (ZoneException e1) {
                logger.error(e1.getMessage());
            }

            doFailure(requestContext, HttpStatus.INTERNAL_SERVER_ERROR_500, msg);
        }
        return server;
    }

    protected boolean validateDetach(ComputeService ser, String vm, String volumeId)
            throws RequestFailedException, ZoneException {
        boolean flag = false;
        Map<String, String> map = ser.getAttachments(vm);
        if (map != null && !(map.isEmpty())) {
            Iterator<Entry<String, String>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry volumes = (Map.Entry) it.next();
                logger.info("volumes available in before detach");
                logger.info("device" + volumes.getKey() + "volume" + volumes.getValue());
                if (volumes.getValue().equals(volumeId)) {
                    flag = true;
                }
            }
        }
        logger.info("DettachVolume  Flag" + flag);
        return flag;
    }

    protected boolean validateDetach(RequestContext rc, ComputeService ser, String vm, String volumeId)
            throws RequestFailedException, ZoneException {
        boolean flag = false;
        String msg = null;
        config.setProperty(Constants.PROPERTY_RETRY_DELAY, "10");
        config.setProperty(Constants.PROPERTY_RETRY_LIMIT, "30");
        while (rc.attempt()) {
            Map<String, String> map = ser.getAttachments(vm);
            if (map != null && !(map.isEmpty())) {
                Iterator<Entry<String, String>> it = map.entrySet().iterator();
                logger.info("volumes available after  detach ");
                while (it.hasNext()) {
                    Map.Entry volumes = (Map.Entry) it.next();
                    logger.info(" devices " + volumes.getKey() + " volumes" + volumes.getValue());
                    if (volumes.getValue().equals(volumeId)) {
                        logger.info("Device" + volumes.getKey() + "Volume" + volumes.getValue());
                        flag = true;
                        break;
                    } else {
                        flag = false;
                    }
                    logger.info("Dettachvolume flag-->" + flag + "Attempts" + rc.getAttempts());
                }
                if (flag) {
                    rc.delay();
                } else {
                    flag = false;
                    break;
                }
            } else {
                flag = false;
                logger.info(rc.getAttempts() + "No.of attempts");
                break;
            }
        }
        if ((rc.getAttempts() == 30) && (!flag)) {
            msg = EELFResourceManager.format(Msg.CONNECTION_FAILED_RETRY, Long.toString(rc.getRetryDelay()),
                    Integer.toString(rc.getAttempts()), Integer.toString(rc.getRetryLimit()));
            logger.error(msg);
            logger.info(msg);
            throw new TimeoutException(msg);
        }
        logger.info("DettachVolume Flag -->" + flag);
        return flag;
    }

}
