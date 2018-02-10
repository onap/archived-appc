/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */
package org.onap.appc.adapter.iaas.provider.operation.impl;

import static org.onap.appc.adapter.iaas.provider.operation.common.enums.Operation.ATTACHVOLUME_SERVICE;
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
import java.util.List;
import java.util.Map;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.onap.appc.Constants;
import org.onap.appc.adapter.iaas.ProviderAdapter;
import org.onap.appc.adapter.iaas.impl.IdentityURL;
import org.onap.appc.adapter.iaas.impl.RequestContext;
import org.onap.appc.adapter.iaas.impl.RequestFailedException;
import org.onap.appc.adapter.iaas.impl.VMURL;
import org.onap.appc.adapter.iaas.provider.operation.common.enums.Operation;
import org.onap.appc.adapter.iaas.provider.operation.impl.base.ProviderServerOperation;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.i18n.Msg;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

public class AttachVolumeServer extends ProviderServerOperation {

    private final EELFLogger logger = EELFManager.getInstance().getLogger(AttachVolumeServer.class);
    private Server attachVolume(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        Server server = null;
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();
        String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);
        String vmUrl = params.get(ProviderAdapter.PROPERTY_INSTANCE_URL);
        String vol_id = params.get(ProviderAdapter.VOLUME_ID);
        String device = params.get(ProviderAdapter.DEVICE);
        VMURL vm = VMURL.parseURL(vmUrl);
        Context context;
        String tenantName = "Unknown";// to be used also in case of exception
        try {
            if (validateVM(rc, appName, vmUrl, vm)) {
                return null;
            }
            IdentityURL ident = IdentityURL.parseURL(params.get(ProviderAdapter.PROPERTY_IDENTITY_URL));
            String identStr = (ident == null) ? null : ident.toString();
            context = getContext(rc, vmUrl, identStr);
            if (context != null) {
                tenantName = context.getTenantName();// this variable also is used in case of exception
                rc.reset();
                server = lookupServer(rc, context, vm.getServerId());
                logger.debug(Msg.SERVER_FOUND, vmUrl, context.getTenantName(), server.getStatus().toString());
                Context contx = server.getContext();
                ComputeService service = contx.getComputeService();
                VolumeService vs = contx.getVolumeService();
                logger.info("collecting volume status for volume -id:" + vol_id);
                List<Volume> volList = vs.getVolumes();
                Volume v = new Volume();
                logger.info("Size of volume list :" + volList.size());
                if (volList != null && !volList.isEmpty()) {
                       if (!(volList.contains(vol_id))){
                            v.setId(vol_id);
                            logger.info("Ready to Attach Volume to the server:");
                            service.attachVolume(server, v, device);
                            logger.info("Volume status after performing attach:" + v.getStatus());
                            if (validateAttach(vs, vol_id)) {
                                ctx.setAttribute("VOLUME_STATUS", "SUCCESS");
                                doSuccess(rc);
                            }
                        else {
                               String msg = "Failed to attach Volume";
                   logger.info("Volume with " + vol_id + " unable to attach");
                   ctx.setAttribute("VOLUME_STATUS", "FAILURE");
                   doFailure(rc, HttpStatus.NOT_IMPLEMENTED_501, msg);
                        }
                     } else {
                            String msg = "Volume with volume id " + vol_id + " cannot be attached as it already exists";
                            logger.info("Alreday volumes exists:");
                            ctx.setAttribute("VOLUME_STATUS", "FAILURE");
                            doFailure(rc, HttpStatus.NOT_IMPLEMENTED_501, msg);
                        }
                }
                context.close();
            } else {
                ctx.setAttribute("VOLUME_STATUS", "CONTEXT_NOT_FOUND");
            }
        } catch (ZoneException e) {
            String msg = EELFResourceManager.format(Msg.SERVER_NOT_FOUND, e, vmUrl);
            logger.error(msg);
           ctx.setAttribute("VOLUME_STATUS", "FAILURE");
            doFailure(rc, HttpStatus.NOT_FOUND_404, msg);
        } catch (RequestFailedException e) {
            ctx.setAttribute("VOLUME_STATUS", "FAILURE");
            doFailure(rc, e.getStatus(), e.getMessage());
        } catch (Exception ex) {
            String msg = EELFResourceManager.format(Msg.SERVER_OPERATION_EXCEPTION, ex, ex.getClass().getSimpleName(),
                ATTACHVOLUME_SERVICE.toString(), vmUrl, tenantName);
            ctx.setAttribute("VOLUME_STATUS", "FAILURE");
            doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, msg);
        }
        return server;
    }

    @Override
    protected ModelObject executeProviderOperation(Map<String, String> params, SvcLogicContext context)
        throws APPCException {
        setMDC(Operation.ATTACHVOLUME_SERVICE.toString(), "App-C IaaS Adapter:attachVolume", ADAPTER_NAME);
        logOperation(Msg.ATTACHINGVOLUME_SERVER, params, context);
        return attachVolume(params, context);
    }
    protected boolean validateAttach(VolumeService vs, String volId) throws RequestFailedException, ZoneException {
        boolean flag = false;
        List<Volume> volList = vs.getVolumes();
            if (volList.contains(volId)) {
                flag = true;
            } else {
                flag = false;
            }
            logger.info("validateAttach flag-->" + flag);
        return flag;
    }
}
