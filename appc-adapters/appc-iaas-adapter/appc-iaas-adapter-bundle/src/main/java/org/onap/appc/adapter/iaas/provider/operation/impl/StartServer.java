/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.adapter.iaas.provider.operation.impl;

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
import com.att.cdp.exceptions.ResourceNotFoundException;
import com.att.cdp.zones.Context;
import com.att.cdp.zones.model.ModelObject;
import com.att.cdp.zones.model.Server;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.glassfish.grizzly.http.util.HttpStatus;
import java.util.Map;
import static org.onap.appc.adapter.iaas.provider.operation.common.enums.Operation.START_SERVICE;
import static org.onap.appc.adapter.utils.Constants.ADAPTER_NAME;

public class StartServer extends ProviderServerOperation {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(StartServer.class);

    /**
     * @see org.onap.appc.adapter.iaas.ProviderAdapter#startServer(java.util.Map,
     *      org.onap.ccsdk.sli.core.sli.SvcLogicContext)
     */
    @SuppressWarnings("nls")
    public Server startServer(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        Server server = null;
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();

        String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);

        try {
            validateParametersExist(params, ProviderAdapter.PROPERTY_INSTANCE_URL,
                    ProviderAdapter.PROPERTY_PROVIDER_NAME);

            String vm_url = params.get(ProviderAdapter.PROPERTY_INSTANCE_URL);

            VMURL vm = VMURL.parseURL(vm_url);
            if (validateVM(rc, appName, vm_url, vm))
                return null;

            IdentityURL ident = IdentityURL.parseURL(params.get(ProviderAdapter.PROPERTY_IDENTITY_URL));
            String identStr = (ident == null) ? null : ident.toString();

            Context context = null;
            String tenantName = "Unknown";//to be used also in case of exception
            ctx.setAttribute("START_STATUS", "ERROR");
            try {
                context = getContext(rc, vm_url, identStr);
                if (context != null) {
                    tenantName = context.getTenantName();//this varaible also is used in case of exception
                    rc.reset();
                    server = lookupServer(rc, context, vm.getServerId());
                    logger.debug(Msg.SERVER_FOUND, vm_url, tenantName, server.getStatus().toString());
                    String msg;

                    /*
                     * We determine what to do based on the current state of the server
                     */

                    /*
                     * Pending is a bit of a special case. If we find the server is in a pending state, then the
                     * provider is in the process of changing state of the server. So, lets try to wait a little bit and
                     * see if the state settles down to one we can deal with. If not, then we have to fail the request.
                     */

                    if (server.getStatus().equals(Server.Status.PENDING)) {
                        waitForStateChange(rc, server, Server.Status.READY, Server.Status.RUNNING, Server.Status.ERROR,
                                Server.Status.SUSPENDED, Server.Status.PAUSED, Server.Status.DELETED);
                    }

                    switch (server.getStatus()) {
                        case DELETED:
                            // Nothing to do, the server is gone
                            msg = EELFResourceManager.format(Msg.SERVER_DELETED, server.getName(), server.getId(),
                                    server.getTenantId(), "started");
                            logger.error(msg);
                            throw new RequestFailedException("Start Server", msg, HttpStatus.METHOD_NOT_ALLOWED_405,
                                    server);

                        case RUNNING:
                            // Nothing to do, the server is already running
                            logger.info("Server was already running");
                            break;

                        case ERROR:
                            // Server is in error state
                            msg = EELFResourceManager.format(Msg.SERVER_ERROR_STATE, server.getName(), server.getId(),
                                    server.getTenantId(), "start");
                            logger.error(msg);
                            throw new RequestFailedException("Start Server", msg, HttpStatus.METHOD_NOT_ALLOWED_405,
                                    server);

                        case READY:
                            // Server is stopped attempt to start the server
                            rc.reset();
                            startServer(rc, server);
                            break;

                        case PAUSED:
                            // if paused, un-pause it
                            rc.reset();
                            unpauseServer(rc, server);
                            break;

                        case SUSPENDED:
                            // Attempt to resume the suspended server
                            rc.reset();
                            resumeServer(rc, server);
                            break;

                        default:
                            // Hmmm, unknown status, should never occur
                            msg = EELFResourceManager.format(Msg.UNKNOWN_SERVER_STATE, server.getName(), server.getId(),
                                    server.getTenantId(), server.getStatus().name());
                            generateEvent(rc, false, msg);
                            logger.error(msg);
                            throw new RequestFailedException("Start Server", msg, HttpStatus.METHOD_NOT_ALLOWED_405,
                                    server);
                    }
                    context.close();
                    doSuccess(rc);
                    ctx.setAttribute("START_STATUS", "SUCCESS");
                } else {
                    ctx.setAttribute("START_STATUS", "CONTEXT_NOT_FOUND");
                }
            } catch (ResourceNotFoundException e) {
                String msg = EELFResourceManager.format(Msg.SERVER_NOT_FOUND, e, vm_url);
                logger.error(msg);
                doFailure(rc, HttpStatus.NOT_FOUND_404, msg);
            } catch (Exception e1) {
                String msg = EELFResourceManager.format(Msg.SERVER_OPERATION_EXCEPTION, e1,
                        e1.getClass().getSimpleName(), START_SERVICE.toString(), vm_url,
                        tenantName);
                logger.error(msg, e1);
                doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, msg);
            }
        } catch (RequestFailedException e) {
            doFailure(rc, e.getStatus(), e.getMessage());
        }

        return server;
    }

    @Override
    protected ModelObject executeProviderOperation(Map<String, String> params, SvcLogicContext context)
            throws APPCException {
        setMDC(Operation.START_SERVICE.toString(), "App-C IaaS Adapter:Start", ADAPTER_NAME);
        logOperation(Msg.STARTING_SERVER, params, context);
        return startServer(params, context);
    }
}
