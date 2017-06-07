/*-
 * ============LICENSE_START=======================================================
 * APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Amdocs
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */

package org.openecomp.appc.adapter.iaas.provider.operation.impl;

import org.openecomp.appc.Constants;
import org.openecomp.appc.adapter.iaas.ProviderAdapter;
import org.openecomp.appc.adapter.iaas.impl.IdentityURL;
import org.openecomp.appc.adapter.iaas.impl.RequestContext;
import org.openecomp.appc.adapter.iaas.impl.RequestFailedException;
import org.openecomp.appc.adapter.iaas.impl.VMURL;
import org.openecomp.appc.adapter.iaas.provider.operation.common.enums.Outcome;
import org.openecomp.appc.adapter.iaas.provider.operation.impl.base.ProviderServerOperation;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.i18n.Msg;
import com.att.cdp.exceptions.ResourceNotFoundException;
import com.att.cdp.zones.Context;
import com.att.cdp.zones.model.ModelObject;
import com.att.cdp.zones.model.Server;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import org.openecomp.sdnc.sli.SvcLogicContext;
import org.glassfish.grizzly.http.util.HttpStatus;

import java.util.Map;

import static org.openecomp.appc.adapter.iaas.provider.operation.common.enums.Operation.STOP_SERVICE;
import static org.openecomp.appc.adapter.utils.Constants.ADAPTER_NAME;


public class StopServer extends ProviderServerOperation {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(StopServer.class);
    private static EELFLogger metricsLogger = EELFManager.getInstance().getMetricsLogger();

    /**
     * @see org.openecomp.appc.adapter.iaas.ProviderAdapter#stopServer(java.util.Map, org.openecomp.sdnc.sli.SvcLogicContext)
     */
    @SuppressWarnings("nls")
    public Server stopServer(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        Server server = null;
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();

        String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);

        try {
            validateParametersExist(params, ProviderAdapter.PROPERTY_INSTANCE_URL,
                    ProviderAdapter.PROPERTY_PROVIDER_NAME);

            String vm_url = params.get(ProviderAdapter.PROPERTY_INSTANCE_URL);
            ctx.setAttribute("STOP_STATUS", "SUCCESS");

            VMURL vm = VMURL.parseURL(vm_url);
            if (validateVM(rc, appName, vm_url, vm)) return null;

            IdentityURL ident = IdentityURL.parseURL(params.get(ProviderAdapter.PROPERTY_IDENTITY_URL));
            String identStr = (ident == null) ? null : ident.toString();

            Context context = null;
            ctx.setAttribute("STOP_STATUS", "ERROR");
            try {
                context = getContext(rc, vm_url, identStr);
                if (context != null) {
                    rc.reset();
                    server = lookupServer(rc, context, vm.getServerId());
                    logger.debug(Msg.SERVER_FOUND, vm_url, context.getTenantName(), server.getStatus().toString());
                    
                    String msg;
                    /*
            		 * We determine what to do based on the current state of the server
            		 */

            		/*
            		 * Pending is a bit of a special case. If we find the server is in a
            		 * pending state, then the provider is in the process of changing state
            		 * of the server. So, lets try to wait a little bit and see if the state
            		 * settles down to one we can deal with. If not, then we have to fail
            		 * the request.
            		 */

                    if (server.getStatus().equals(Server.Status.PENDING)) {
                        waitForStateChange(rc, server, Server.Status.READY, Server.Status.RUNNING, Server.Status.ERROR,
                                Server.Status.SUSPENDED, Server.Status.PAUSED, Server.Status.DELETED);
                    }

                    switch (server.getStatus()) {
                        case DELETED:
                            // Nothing to do, the server is gone
                            msg = EELFResourceManager.format(Msg.SERVER_DELETED, server.getName(), server.getId(),
                                    server.getTenantId(), "stopped");
                            generateEvent(rc, false, msg);
                            logger.error(msg);
                            metricsLogger.error(msg);
                            throw new RequestFailedException("Stop Server", msg, HttpStatus.METHOD_NOT_ALLOWED_405, server);

                        case RUNNING:
                            // Attempt to stop the server
                            rc.reset();
                            stopServer(rc, server);
                            generateEvent(rc, true, Outcome.SUCCESS.toString());
                            break;

                        case ERROR:
                            // Server is in error state
                            msg = EELFResourceManager.format(Msg.SERVER_ERROR_STATE, server.getName(), server.getId(),
                                    server.getTenantId(), "stop");
                            generateEvent(rc, false, msg);
                            logger.error(msg);
                            metricsLogger.error(msg);
                            throw new RequestFailedException("Stop Server", msg, HttpStatus.METHOD_NOT_ALLOWED_405, server);

                        case READY:
                            // Nothing to do, the server was already stopped
                            logger.info("Server was already stopped");
                            break;

                        case PAUSED:
                            // if paused, un-pause it and then stop it
                            rc.reset();
                            unpauseServer(rc, server);
                            rc.reset();
                            stopServer(rc, server);
                            generateEvent(rc, true, Outcome.SUCCESS.toString());
                            break;

                        case SUSPENDED:
                            // Attempt to resume the suspended server and after that stop it
                            rc.reset();
                            resumeServer(rc, server);
                            rc.reset();
                            stopServer(rc, server);
                            generateEvent(rc, true, Outcome.SUCCESS.toString());
                            break;

                        default:
                            // Hmmm, unknown status, should never occur
                            msg = EELFResourceManager.format(Msg.UNKNOWN_SERVER_STATE, server.getName(), server.getId(),
                                    server.getTenantId(), server.getStatus().name());
                            generateEvent(rc, false, msg);
                            logger.error(msg);
                            metricsLogger.error(msg);
                            throw new RequestFailedException("Stop Server", msg, HttpStatus.METHOD_NOT_ALLOWED_405, server);
                    }
                    context.close();
                    doSuccess(rc);
                    ctx.setAttribute("STOP_STATUS", "SUCCESS");
                    msg = EELFResourceManager.format(Msg.SUCCESS_EVENT_MESSAGE, "StopServer", vm_url);
                    ctx.setAttribute(org.openecomp.appc.Constants.ATTRIBUTE_SUCCESS_MESSAGE, msg);

                }else{
                    ctx.setAttribute("STOP_STATUS", "CONTEXT_NOT_FOUND");
                }
            } catch (ResourceNotFoundException e) {
                String msg = EELFResourceManager.format(Msg.SERVER_NOT_FOUND, e, vm_url);
                logger.error(msg);
                doFailure(rc, HttpStatus.NOT_FOUND_404, msg);
            } catch (Throwable t) {
                String msg = EELFResourceManager.format(Msg.SERVER_OPERATION_EXCEPTION, t, t.getClass().getSimpleName(),
                        STOP_SERVICE.toString(), vm_url, context == null ? "Unknown" : context.getTenantName());
                logger.error(msg, t);
                doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, msg);
            }
        } catch (RequestFailedException e) {
            logger.error(EELFResourceManager.format(Msg.STOP_SERVER_FAILED, appName, "n/a", "n/a", e.getMessage()));
            doFailure(rc, e.getStatus(), e.getMessage());
        }

        return server;
    }

    @Override
    protected ModelObject executeProviderOperation(Map<String, String> params, SvcLogicContext context) throws APPCException {

        setMDC(STOP_SERVICE.toString(), "App-C IaaS Adapter:Stop", ADAPTER_NAME);
        logOperation(Msg.STOPPING_SERVER, params, context);
        return stopServer(params, context);
    }
}
