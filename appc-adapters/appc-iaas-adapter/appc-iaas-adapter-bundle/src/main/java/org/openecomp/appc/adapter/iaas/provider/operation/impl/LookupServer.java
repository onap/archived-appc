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
import org.openecomp.appc.adapter.iaas.impl.RequestContext;
import org.openecomp.appc.adapter.iaas.impl.RequestFailedException;
import org.openecomp.appc.adapter.iaas.impl.VMURL;
import org.openecomp.appc.adapter.iaas.provider.operation.common.enums.Operation;
import org.openecomp.appc.adapter.iaas.provider.operation.impl.base.ProviderServerOperation;
import org.openecomp.appc.configuration.Configuration;
import org.openecomp.appc.configuration.ConfigurationFactory;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.i18n.Msg;
import com.att.cdp.exceptions.ZoneException;
import com.att.cdp.zones.Context;
import com.att.cdp.zones.model.ModelObject;
import com.att.cdp.zones.model.Server;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import org.openecomp.sdnc.sli.SvcLogicContext;
import org.glassfish.grizzly.http.util.HttpStatus;

import java.io.IOException;
import java.util.Map;

import static org.openecomp.appc.adapter.utils.Constants.ADAPTER_NAME;

/**
 * @since September 26, 2016
 */
public class LookupServer extends ProviderServerOperation {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(EvacuateServer.class);
    private static final Configuration configuration = ConfigurationFactory.getConfiguration();


    public Server lookupServer(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        Server server = null;
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive(); //should we test the return and fail if false?

        String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);

        String vm_url = null;
        VMURL vm = null;
        try {

            //process vm_url
            validateParametersExist(params, ProviderAdapter.PROPERTY_INSTANCE_URL,
                    ProviderAdapter.PROPERTY_PROVIDER_NAME);
            vm_url = params.get(ProviderAdapter.PROPERTY_INSTANCE_URL);
            vm = VMURL.parseURL(vm_url);
            if (validateVM(rc, appName, vm_url, vm)) return null;


            //use try with resource to ensure context is closed (returned to pool)
            try(Context context = resolveContext(rc, params, appName, vm_url)){
                //resloveContext & getContext call doFailure and log errors before returning null
                if (context != null){
                    rc.reset();
                    server = lookupServer(rc, context, vm.getServerId());
                    logger.debug(Msg.SERVER_FOUND, vm_url, context.getTenantName(), server.getStatus().toString());
                    ctx.setAttribute("serverFound", "success");
                    String msg = EELFResourceManager.format(Msg.SUCCESS_EVENT_MESSAGE, "LookupServer", vm_url);
                    ctx.setAttribute(org.openecomp.appc.Constants.ATTRIBUTE_SUCCESS_MESSAGE, msg);
                    doSuccess(rc);
                }
            } catch (ZoneException e) {
                //server not found
                String msg = EELFResourceManager.format(Msg.SERVER_NOT_FOUND, e, vm_url);
                logger.error(msg);
                doFailure(rc, HttpStatus.NOT_FOUND_404, msg);
                ctx.setAttribute("serverFound", "failure");
            }  catch (IOException e) {
                //exception closing context
                String msg = EELFResourceManager.format(Msg.CLOSE_CONTEXT_FAILED, e, vm_url);
                logger.error(msg);
            } catch (Throwable t) {
                String msg = EELFResourceManager.format(Msg.SERVER_OPERATION_EXCEPTION, t, t.getClass().getSimpleName(),
                        Operation.LOOKUP_SERVICE.toString(), vm_url,  "Unknown" );
                logger.error(msg, t);
                doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, msg);
            }

        } catch (RequestFailedException e) {
            // parameters not valid, unable to connect to provider
            String msg = EELFResourceManager.format(Msg.SERVER_NOT_FOUND, e, vm_url);
            logger.error(msg);
            doFailure(rc, HttpStatus.NOT_FOUND_404, msg);
            ctx.setAttribute("serverFound", "failure");
        }
        return server;
    }

    @Override
    protected ModelObject executeProviderOperation(Map<String, String> params, SvcLogicContext context) throws APPCException {

        setMDC(Operation.LOOKUP_SERVICE.toString(), "App-C IaaS Adapter:LookupServer", ADAPTER_NAME);
        logOperation(Msg.LOOKING_SERVER_UP, params, context);
        return lookupServer(params, context);
    }
}
