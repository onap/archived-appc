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

package org.onap.appc.provider.lcm.service;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import com.google.common.base.Strings;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.Payload;
import org.onap.appc.Constants;
import org.onap.appc.configuration.Configuration;
import org.onap.appc.configuration.ConfigurationFactory;
import org.onap.appc.domainmodel.lcm.ActionLevel;
import org.onap.appc.domainmodel.lcm.ResponseContext;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.executor.objects.LCMCommandStatus;
import org.onap.appc.executor.objects.Params;
import org.onap.appc.i18n.Msg;
import org.onap.appc.logging.LoggingConstants;
import org.onap.appc.logging.LoggingUtils;
import org.onap.appc.provider.AppcProviderLcm;
import org.onap.appc.provider.lcm.mock.MockRequestExecutor;
import org.onap.appc.requesthandler.RequestHandler;
import org.onap.appc.requesthandler.objects.RequestHandlerInput;
import org.onap.appc.requesthandler.objects.RequestHandlerOutput;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import java.util.Collection;

/**
 * Provider LCM request executor
 */
public class RequestExecutor {
    final String CANNOT_PROCESS = "LCM request cannot be processed at the moment because APPC isn't running";

    private final Configuration configuration = ConfigurationFactory.getConfiguration();
    private final EELFLogger logger = EELFManager.getInstance().getLogger(AppcProviderLcm.class);

    /**
	 * Execute the request.
	 * @param requestHandlerInput of the RequestHandlerInput
	 * @return RequestHandlerOutput
	 */
    public RequestHandlerOutput executeRequest(RequestHandlerInput requestHandlerInput) {
	    // TODO mock backend should be removed when backend implementation is done
	    RequestHandlerOutput requestHandlerOutput = new MockRequestExecutor().executeRequest(requestHandlerInput);
	    if (requestHandlerOutput != null) {
	        // mock support, return mock results
	        return requestHandlerOutput;
	    }
	
	    RequestHandler handler = getRequestHandler(requestHandlerInput.getRequestContext().getActionLevel());
	    if (handler == null) {
	    	logger.debug("execute while requesthandler is null");
	        requestHandlerOutput = createRequestHandlerOutput(requestHandlerInput,
	            LCMCommandStatus.REJECTED, Msg.REQUEST_HANDLER_UNAVAILABLE, new APPCException(CANNOT_PROCESS));
	    } else {
	        try {
	        	logger.debug("execute while requesthandler is not null");
	            requestHandlerOutput = handler.handleRequest(requestHandlerInput);
	        } catch (Exception e) {
	            logger.info(String.format("UNEXPECTED FAILURE while executing %s action",
	                requestHandlerInput.getRequestContext().getAction().name()));
	            requestHandlerOutput = createRequestHandlerOutput(requestHandlerInput,
	                LCMCommandStatus.UNEXPECTED_ERROR, Msg.EXCEPTION_CALLING_DG, e);
	        }
	    }
	    return requestHandlerOutput;
	}

    /**
     * Get Request handler by ActionLevel
     * @param actionLevel the ActionLevel
     * @return RequestHandler if found, otherwise return null or throw RuntimeException
     */
    RequestHandler getRequestHandler(ActionLevel actionLevel) {
        final BundleContext context = FrameworkUtil.getBundle(RequestHandler.class).getBundleContext();
        if (context == null) {
            return null;
        }

        String filter = null;
        try {
            filter = "(level=" + actionLevel.name() + ")";
            Collection<ServiceReference<RequestHandler>> serviceReferences =
                context.getServiceReferences(RequestHandler.class, filter);
            if (serviceReferences.size() == 1) {
                ServiceReference<RequestHandler> serviceReference = serviceReferences.iterator().next();
                return context.getService(serviceReference);
            }

            logger.error(String.format("Cannot find service reference for %s", RequestHandler.class.getName()));
            throw new RuntimeException();

        } catch (InvalidSyntaxException e) {
            logger.error(String.format("Cannot find service reference for %s: Invalid Syntax %s",
                RequestHandler.class.getName(), filter), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Create request handler output
     * @param request of RequestHandlerInput
     * @param cmdStatus of LCMCommandStatus
     * @param msg of Msg for audit log
     * @param e of the Exception
     * @return generated RequestHandlerOutput based on the input
     */
    RequestHandlerOutput createRequestHandlerOutput(RequestHandlerInput request,
                                                    LCMCommandStatus cmdStatus,
                                                    Msg msg,
                                                    Exception e) {
        String errorMsg = e.getMessage() != null ? e.getMessage() : e.toString();
        Params params = new Params().addParam("errorMsg", errorMsg);

        final org.onap.appc.domainmodel.lcm.Status status = new org.onap.appc.domainmodel.lcm.Status();
        status.setMessage(cmdStatus.getFormattedMessage(params));
        status.setCode(cmdStatus.getResponseCode());

        final ResponseContext responseContext = new ResponseContext();
        responseContext.setCommonHeader(request.getRequestContext().getCommonHeader());
        responseContext.setStatus(status);

        RequestHandlerOutput requestHandlerOutput = new RequestHandlerOutput();
        requestHandlerOutput.setResponseContext(responseContext);

        final String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);
        final String reason = EELFResourceManager.format(
            msg, e, appName, e.getClass().getSimpleName(), "", e.getMessage());
        LoggingUtils.logErrorMessage(
            LoggingConstants.TargetNames.APPC_PROVIDER,
            reason,
            this.getClass().getName());

        return requestHandlerOutput;
    }

    /**
     * Get payload from passed in RequestHandlerOutput
     * @param output of the RequestHandlerOutput
     * @return If the passed in RequestHandlerOutput contains payload, return a Payload object of the payload.
     *         Otherwise, return null.
     */
    public Payload getPayload(RequestHandlerOutput output) {
        if (output.getResponseContext() == null
            || Strings.isNullOrEmpty(output.getResponseContext().getPayload())) {
            return null;
        }

        return new Payload(output.getResponseContext().getPayload());
    }
}
