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

package org.openecomp.appc.dg.common.impl;

import org.apache.commons.lang3.StringUtils;
import org.openecomp.appc.adapter.message.EventSender;
import org.openecomp.appc.adapter.message.MessageDestination;
import org.openecomp.appc.adapter.message.event.EventHeader;
import org.openecomp.appc.adapter.message.event.EventMessage;
import org.openecomp.appc.adapter.message.event.EventStatus;
import org.openecomp.appc.dg.common.DCAEReporterPlugin;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.sdnc.sli.SvcLogicContext;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import java.util.Map;

public class DCAEReporterPluginImpl implements DCAEReporterPlugin {

    private EventSender eventSender;

    public DCAEReporterPluginImpl() {
        BundleContext bctx = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        ServiceReference sref = bctx.getServiceReference(EventSender.class);
        eventSender = (EventSender) bctx.getService(sref);
    }

    @Override
    public void report(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        String errorDescription,apiVersion,eventId ;

        Integer errorCode = readErrorCode(params,ctx);
        errorDescription = params.get(Constants.EVENT_MESSAGE);

        if(StringUtils.isEmpty(errorDescription)) {
            reportLegacy(params , ctx);
        }else{
        apiVersion = ctx.getAttribute("input.common-header.api-ver");
        eventId = ctx.getAttribute("input.common-header.request-id");

            EventMessage eventMessage = new EventMessage(new EventHeader((new java.util.Date()).toString(), apiVersion, eventId), new EventStatus(errorCode, errorDescription));
            String eventWriteTopic = params.get("event-topic-name");
            if(!StringUtils.isEmpty(eventWriteTopic) && eventWriteTopic!=null){
                eventSender.sendEvent(MessageDestination.DCAE, eventMessage,eventWriteTopic);
            }else {
                eventSender.sendEvent(MessageDestination.DCAE, eventMessage);
            }
        }
    }

    private Integer readErrorCode(Map<String, String> params, SvcLogicContext ctx) {
        Integer errorReportCode = 501;
        String errorCodeStr = params.get(Constants.DG_ERROR_CODE);
        errorCodeStr = StringUtils.isEmpty(errorCodeStr)?
                ctx.getAttribute(Constants.DG_ERROR_CODE):errorCodeStr;
        try{
            errorReportCode =  Integer.parseInt(errorCodeStr);
        }
        catch (NumberFormatException e){
            // Ignore Exception
        }
        return errorReportCode;
    }

    @Override
    public void reportSuccess(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        Integer successReportCode = 500;
        String successDescription, apiVersion, eventId;
        successDescription = params.get(Constants.EVENT_MESSAGE);

        if(StringUtils.isEmpty(successDescription)) {
            successDescription = params.get(Constants.DG_OUTPUT_STATUS_MESSAGE);
        }

        apiVersion = ctx.getAttribute("input.common-header.api-ver");
        eventId = ctx.getAttribute("input.common-header.request-id");

        if (null == successDescription) {
            successDescription = "Success";
        }
        EventMessage eventMessage = new EventMessage(new EventHeader((new java.util.Date()).toString(), apiVersion, eventId), new EventStatus(successReportCode, successDescription));
        String eventWriteTopic = params.get("event-topic-name");
        if(!StringUtils.isEmpty(eventWriteTopic) && eventWriteTopic!=null){
            eventSender.sendEvent(MessageDestination.DCAE, eventMessage,eventWriteTopic);
        }else {
            eventSender.sendEvent(MessageDestination.DCAE, eventMessage);
        }
    }

    private void reportLegacy(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        String errorDescription,apiVersion,eventId ;

        Integer errorCode = readErrorCode(params,ctx);
        errorDescription = getErrorDescriptionAndAddToCtx(params,ctx);

        apiVersion = ctx.getAttribute("input.common-header.api-ver");
        eventId = ctx.getAttribute("input.common-header.request-id");

        EventMessage eventMessage = new EventMessage(new EventHeader((new java.util.Date()).toString(), apiVersion, eventId), new EventStatus(errorCode, errorDescription));
        String eventWriteTopic = params.get("event-topic-name");
        if(!StringUtils.isEmpty(eventWriteTopic) && eventWriteTopic!=null){
            eventSender.sendEvent(MessageDestination.DCAE, eventMessage,eventWriteTopic);
        }else {
            eventSender.sendEvent(MessageDestination.DCAE, eventMessage);
        }
    }

    private String getErrorDescriptionAndAddToCtx(Map<String, String> params, SvcLogicContext ctx) {
        String errorDescription;
        errorDescription = params.get(Constants.DG_OUTPUT_STATUS_MESSAGE);
        if(StringUtils.isEmpty(errorDescription)) {
            errorDescription = ctx.getAttribute(Constants.DG_OUTPUT_STATUS_MESSAGE);
        }
        if(StringUtils.isEmpty(errorDescription)) {
            errorDescription = "Unknown";
        }
        addToContextIfNotContains(errorDescription,ctx);
        return errorDescription;
    }

    private void addToContextIfNotContains(String errorDescription, SvcLogicContext ctx) {
        String errorDescriptionFromCtx;
        errorDescriptionFromCtx = ctx.getAttribute(Constants.DG_OUTPUT_STATUS_MESSAGE);
        if(StringUtils.isEmpty(errorDescriptionFromCtx)){
            errorDescriptionFromCtx = ctx.getAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE);
        }
        if(StringUtils.isEmpty(errorDescriptionFromCtx)){
            ctx.setAttribute(Constants.DG_OUTPUT_STATUS_MESSAGE, errorDescription);
        }else if  (!errorDescriptionFromCtx.contains(errorDescription)){
            ctx.setAttribute(Constants.DG_OUTPUT_STATUS_MESSAGE, errorDescriptionFromCtx+ " | "+ errorDescription);
        }
    }

}
