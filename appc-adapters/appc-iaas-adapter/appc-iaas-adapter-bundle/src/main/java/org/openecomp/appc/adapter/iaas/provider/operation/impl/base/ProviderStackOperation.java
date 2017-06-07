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

package org.openecomp.appc.adapter.iaas.provider.operation.impl.base;

import org.openecomp.appc.Constants;
import org.openecomp.appc.adapter.iaas.impl.RequestContext;
import org.openecomp.appc.adapter.iaas.impl.RequestFailedException;
import org.openecomp.appc.adapter.openstack.heat.StackResource;
import org.openecomp.appc.i18n.Msg;
import com.att.cdp.exceptions.ContextConnectionException;
import com.att.cdp.exceptions.ResourceNotFoundException;
import com.att.cdp.exceptions.TimeoutException;
import com.att.cdp.exceptions.ZoneException;
import com.att.cdp.zones.Context;
import com.att.cdp.zones.Provider;
import com.att.cdp.zones.StackService;
import com.att.cdp.zones.model.Stack;
import com.att.cdp.zones.spi.AbstractService;
import com.att.cdp.zones.spi.RequestState;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import org.openecomp.sdnc.sli.SvcLogicContext;
import com.woorea.openstack.base.client.OpenStackBaseException;
import org.glassfish.grizzly.http.util.HttpStatus;

import java.util.List;

/**
 * @since September 29, 2016
 */
public abstract class ProviderStackOperation extends ProviderOperation{

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(ProviderStackOperation.class);


    protected void trackRequest(Context context, AbstractService.State... states) {
        RequestState.clear();

        if (null == states) return;
        for (AbstractService.State state : states) {
            RequestState.put(state.getName(), state.getValue());
        }

        Thread currentThread = Thread.currentThread();
        StackTraceElement[] stack = currentThread.getStackTrace();
        if (stack != null && stack.length > 0) {
            int index = 0;
            StackTraceElement element;
            for (; index < stack.length; index++) {
                element = stack[index];
                if ("trackRequest".equals(element.getMethodName())) {  //$NON-NLS-1$
                    break;
                }
            }
            index++;

            if (index < stack.length) {
                element = stack[index];
                RequestState.put(RequestState.METHOD, element.getMethodName());
                RequestState.put(RequestState.CLASS, element.getClassName());
                RequestState.put(RequestState.LINE_NUMBER, Integer.toString(element.getLineNumber()));
                RequestState.put(RequestState.THREAD, currentThread.getName());
                RequestState.put(RequestState.PROVIDER, context.getProvider().getName());
                RequestState.put(RequestState.TENANT, context.getTenantName());
                RequestState.put(RequestState.PRINCIPAL, context.getPrincipal());
            }
        }
    }

    private boolean checkStatus(String expectedStatus, int pollInterval, String actualStatus) {
        if (actualStatus.toUpperCase().equals(expectedStatus)) {
            return true;
        } else {
            try {
                Thread.sleep(pollInterval * 1000);
            } catch (InterruptedException ignored) {
            }
        }
        return false;
    }

    protected boolean waitForStack(Stack stack, StackResource stackResource, String expectedStatus)
            throws OpenStackBaseException, TimeoutException {
        int pollInterval = configuration.getIntegerProperty(Constants.PROPERTY_OPENSTACK_POLL_INTERVAL);
        int timeout = configuration.getIntegerProperty(Constants.PROPERTY_STACK_STATE_CHANGE_TIMEOUT);
        long maxTimeToWait = System.currentTimeMillis() + (long) timeout * 1000;

        while (System.currentTimeMillis() < maxTimeToWait) {
            String stackStatus = stackResource.show(stack.getName(), stack.getId()).execute().getStackStatus();
            logger.debug("Stack status : " + stackStatus);
            if (stackStatus.toUpperCase().contains("FAILED")) return false;
            if(checkStatus(expectedStatus, pollInterval, stackStatus)) return true;
        }
        throw new TimeoutException("Timeout waiting for stack status change");
    }

    protected Stack lookupStack(RequestContext rc, Context context, String id)
            throws ZoneException, RequestFailedException {
        StackService stackService = context.getStackService();
        Stack stack = null;
        String msg;
        Provider provider = context.getProvider();
        while (rc.attempt()) {
            try {
                List<Stack> stackList = stackService.getStacks();
                for (Stack stackObj : stackList) {
                    if (stackObj.getId().equals(id)) {
                        stack = stackObj;
                        break;
                    }
                }
                break;
            } catch (ContextConnectionException e) {
                msg = EELFResourceManager.format(Msg.CONNECTION_FAILED_RETRY, provider.getName(), stackService.getURL(),
                        context.getTenant().getName(), context.getTenant().getId(), e.getMessage(),
                        Long.toString(rc.getRetryDelay()), Integer.toString(rc.getAttempts()),
                        Integer.toString(rc.getRetryLimit()));
                logger.error(msg, e);
                rc.delay();
            }

        }
        if (rc.isFailed()) {
            msg = EELFResourceManager.format(Msg.CONNECTION_FAILED, provider.getName(), stackService.getURL());
            logger.error(msg);
            doFailure(rc, HttpStatus.BAD_GATEWAY_502, msg);
            throw new RequestFailedException("Lookup Stack", msg, HttpStatus.BAD_GATEWAY_502, stack);
        }

        if (stack == null) {
            throw new ResourceNotFoundException("Stack not found with Id : {" + id + "}");
        }
        return stack;
    }


    protected boolean waitForStackStatus(RequestContext rc, Stack stack, Stack.Status expectedStatus) throws ZoneException, RequestFailedException {
        SvcLogicContext ctx = rc.getSvcLogicContext();
        Context context = stack.getContext();
        StackService stackService = context.getStackService();

        int pollInterval = configuration.getIntegerProperty(Constants.PROPERTY_OPENSTACK_POLL_INTERVAL);
        int timeout = configuration.getIntegerProperty(Constants.PROPERTY_STACK_STATE_CHANGE_TIMEOUT);
        long maxTimeToWait = System.currentTimeMillis() + (long) timeout * 1000;
        Stack.Status stackStatus;
        while (System.currentTimeMillis() < maxTimeToWait) {
            stackStatus = stackService.getStack(stack.getName(), stack.getId()).getStatus();
            logger.debug("Stack status : " + stackStatus.toString());
            if (stackStatus == expectedStatus) {
                return true;
            } else if (stackStatus == Stack.Status.FAILED) {
                return false;
            } else {
                try {
                    Thread.sleep(pollInterval * 1000);
                } catch (InterruptedException e) {
                    logger.trace("Sleep threw interrupted exception, should never occur");
                }
            }
        }

        ctx.setAttribute("TERMINATE_STATUS", "ERROR");
        throw new TimeoutException("Timeout waiting for stack status change");

    }
}
