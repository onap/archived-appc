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
 *g  http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.Map;
import java.util.List;
import com.att.cdp.zones.ComputeService;
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

public class DettachVolumeServer extends ProviderServerOperation {
    private final EELFLogger logger = EELFManager.getInstance().getLogger(DettachVolumeServer.class);

    @Override
    protected ModelObject executeProviderOperation(Map<String, String> params, SvcLogicContext context)
            throws APPCException {
        setMDC(Operation.DETACHVOLUME_SERVICE.toString(), "App-C IaaS Adapter:dettachVolume", ADAPTER_NAME);
        logOperation(Msg.DETTACHINGVOLUME_SERVER, params, context);
        return dettachVolume(params, context);
    }

    private Server dettachVolume(Map<String, String> params, SvcLogicContext ctx) {
        Server server = null;
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();
        String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);
        String vm_url = params.get(ProviderAdapter.PROPERTY_INSTANCE_URL);
        String volumeid = params.get(ProviderAdapter.VOLUME_ID);
        String device = params.get(ProviderAdapter.DEVICE);
        VMURL vm = VMURL.parseURL(vm_url);
        Context context = null;
        try {
            if (validateVM(rc, appName, vm_url, vm))
                return null;
            IdentityURL ident = IdentityURL.parseURL(params.get(ProviderAdapter.PROPERTY_IDENTITY_URL));
            String identStr = (ident == null) ? null : ident.toString();
            String vol_id = (volumeid == null) ? null : volumeid.toString();
            context = getContext(rc, vm_url, identStr);
            if (context != null) {
                rc.reset();
                server = lookupServer(rc, context, vm.getServerId());
                logger.debug(Msg.SERVER_FOUND, vm_url, context.getTenantName(), server.getStatus().toString());
                Context contx = server.getContext();
                ComputeService service = contx.getComputeService();
                VolumeService vs = contx.getVolumeService();
                logger.info("collecting volume status for volume -id:" + vol_id);
                List<Volume> volList = vs.getVolumes();
                logger.info("Size of volume list :" + volList.size());
                if (volList != null && !volList.isEmpty()) {
                    for (Volume v : volList) {
                        logger.info("list of volumesif exists" + v.getId());
                        if (v.getId().equals(vol_id)) {
                            v.setId(vol_id);
                            logger.info("Ready to Detach Volume from the server:" + Volume.Status.DETACHING);
                            service.detachVolume(server, v);
                            logger.info("Volume status after performing detach:" + v.getStatus());
                            doSuccess(rc);
                        } else {
                            String msg = "Volume with volume id " + vol_id + " cannot be detached as it doesnot exists";
                            doFailure(rc, HttpStatus.NOT_IMPLEMENTED_501, msg);
                        }
                    }
                }
                context.close();
                doSuccess(rc);
                ctx.setAttribute("VOLUME_STATUS", "SUCCESS");
            } else {
                ctx.setAttribute("VOLUME_STATUS", "CONTEXT_NOT_FOUND");
            }
        } catch (ZoneException e) {
            String msg = EELFResourceManager.format(Msg.SERVER_NOT_FOUND, e, vm_url);
            logger.error(msg);
            doFailure(rc, HttpStatus.NOT_FOUND_404, msg);
        } catch (RequestFailedException e) {
            doFailure(rc, e.getStatus(), e.getMessage());
        } catch (Exception ex) {
            String msg = EELFResourceManager.format(Msg.SERVER_OPERATION_EXCEPTION, ex, ex.getClass().getSimpleName(),
                    ATTACHVOLUME_SERVICE.toString(), vm_url, context == null ? "Unknown" : context.getTenantName());
            logger.error(msg, ex);
            doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, msg);
        }
        return server;
    }

}
