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
import org.openecomp.appc.adapter.iaas.provider.operation.common.enums.Operation;
import org.openecomp.appc.adapter.iaas.provider.operation.impl.base.ProviderStackOperation;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.i18n.Msg;
import com.att.cdp.exceptions.ResourceNotFoundException;
import com.att.cdp.exceptions.ZoneException;
import com.att.cdp.zones.Context;
import com.att.cdp.zones.StackService;
import com.att.cdp.zones.model.ModelObject;
import com.att.cdp.zones.model.Stack;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import org.openecomp.sdnc.sli.SvcLogicContext;
import org.glassfish.grizzly.http.util.HttpStatus;

import java.util.Map;

import static org.openecomp.appc.adapter.utils.Constants.ADAPTER_NAME;

/**
 * @since September 26, 2016
 */
public class TerminateStack extends ProviderStackOperation {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(EvacuateServer.class);

    private void deleteStack(RequestContext rc, Stack stack) throws ZoneException, RequestFailedException {
        SvcLogicContext ctx = rc.getSvcLogicContext();
        Context context = stack.getContext();
        StackService stackService = context.getStackService();
        logger.debug("Deleting Stack: " + "id:{ " + stack.getId() + "}");
        stackService.deleteStack(stack);

        // wait for the stack deletion
        boolean success = waitForStackStatus(rc, stack, Stack.Status.DELETED);
        if (success) {
            ctx.setAttribute("TERMINATE_STATUS", "SUCCESS");
        } else {
            ctx.setAttribute("TERMINATE_STATUS", "ERROR");
            throw new RequestFailedException("Delete Stack failure : " + Msg.STACK_OPERATION_EXCEPTION.toString());
        }
    }

    @SuppressWarnings("nls")
    public Stack terminateStack(Map<String, String> params, SvcLogicContext ctx) throws IllegalArgumentException, APPCException {
        Stack stack = null;
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();

        ctx.setAttribute("TERMINATE_STATUS", "STACK_NOT_FOUND");
        String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);

        try {

            validateParametersExist(params, ProviderAdapter.PROPERTY_INSTANCE_URL,
                    ProviderAdapter.PROPERTY_PROVIDER_NAME, ProviderAdapter.PROPERTY_STACK_ID);

            String stackId = params.get(ProviderAdapter.PROPERTY_STACK_ID);
            String vm_url = params.get(ProviderAdapter.PROPERTY_INSTANCE_URL);

            Context context = resolveContext(rc, params, appName, vm_url);

            try {
                if (context != null) {
                    rc.reset();
                    stack = lookupStack(rc, context, stackId);
                    logger.debug(Msg.STACK_FOUND, vm_url, context.getTenantName(), stack.getStatus().toString());
                    logger.info(EELFResourceManager.format(Msg.TERMINATING_STACK, stack.getName()));
                    deleteStack(rc, stack);
                    logger.info(EELFResourceManager.format(Msg.TERMINATE_STACK, stack.getName()));
                    context.close();
                    doSuccess(rc);
                    String msg = EELFResourceManager.format(Msg.SUCCESS_EVENT_MESSAGE, "TerminateStack", vm_url);
                    ctx.setAttribute(org.openecomp.appc.Constants.ATTRIBUTE_SUCCESS_MESSAGE, msg);
                }
            } catch (ResourceNotFoundException e) {
                String msg = EELFResourceManager.format(Msg.STACK_NOT_FOUND, e, vm_url);
                logger.error(msg);
                doFailure(rc, HttpStatus.NOT_FOUND_404, msg);
            } catch (Throwable t) {
                String msg = EELFResourceManager.format(Msg.STACK_OPERATION_EXCEPTION, t, t.getClass().getSimpleName(),
                        Operation.TERMINATE_STACK.toString(), vm_url, context.getTenantName());
                logger.error(msg, t);
                doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, msg);
            }
        } catch (RequestFailedException e) {
            logger.error(EELFResourceManager.format(Msg.TERMINATE_STACK_FAILED, appName, "n/a", "n/a"));
            doFailure(rc, e.getStatus(), e.getMessage());
        }
        return stack;
    }

    @Override
    protected ModelObject executeProviderOperation(Map<String, String> params, SvcLogicContext context) throws APPCException {

        setMDC(Operation.TERMINATE_STACK.toString(), "App-C IaaS Adapter:Terminate-Stack", ADAPTER_NAME);
        logOperation(Msg.TERMINATING_STACK, params, context);
        return terminateStack(params, context);
    }
}
