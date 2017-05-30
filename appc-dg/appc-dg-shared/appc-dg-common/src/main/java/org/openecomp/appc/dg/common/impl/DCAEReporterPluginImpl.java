/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 						reserved.
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
import org.openecomp.appc.adapter.dmaap.EventSender;
import org.openecomp.appc.adapter.dmaap.DmaapDestination;
import org.openecomp.appc.adapter.dmaap.event.EventHeader;
import org.openecomp.appc.adapter.dmaap.event.EventMessage;
import org.openecomp.appc.adapter.dmaap.event.EventStatus;
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
        Integer errorReportCode = 501;
        boolean bwcMode = Boolean.parseBoolean(ctx.getAttribute("isBwcMode"));
        String errorDescription,apiVersion,eventId ;
        errorDescription = getErrorDescriptionAndAddToCtx(bwcMode,params,ctx);
        if(!bwcMode){
            apiVersion = ctx.getAttribute("input.common-header.api-ver");
            eventId = ctx.getAttribute("input.common-header.request-id");
        }else {
            apiVersion = ctx.getAttribute(Constants.API_VERSION_FIELD_NAME);
            eventId = ctx.getAttribute(Constants.REQ_ID_FIELD_NAME);
        }

        EventMessage eventMessage = new EventMessage(new EventHeader((new java.util.Date()).toString(), apiVersion, eventId), new EventStatus(errorReportCode, errorDescription));
        eventSender.sendEvent(DmaapDestination.DCAE, eventMessage);
    }

    private String getErrorDescriptionAndAddToCtx(boolean bwcMode, Map<String, String> params, SvcLogicContext ctx) {
        String errorDescription;
        if(!bwcMode) {
            errorDescription = params.get(Constants.DG_OUTPUT_STATUS_MESSAGE);
            if(StringUtils.isEmpty(errorDescription)) {
                errorDescription = ctx.getAttribute(Constants.DG_OUTPUT_STATUS_MESSAGE);
            }else {
                addToContextIfNotContains(bwcMode,errorDescription,ctx);
            }
        }else{
            errorDescription = params.get(Constants.DG_ERROR_FIELD_NAME);
            if(StringUtils.isEmpty(errorDescription)) {
                errorDescription = ctx.getAttribute("org.openecomp.appc.dg.error");
            }else {
                addToContextIfNotContains(bwcMode, errorDescription,ctx);
            }
        }

        if(StringUtils.isEmpty(errorDescription)) {
            errorDescription = "Unknown";
        }
        return errorDescription;
    }

    private void addToContextIfNotContains(boolean bwcMode, String errorDescription, SvcLogicContext ctx) {
        String errorDescriptionFromCtx;
        if(!StringUtils.isEmpty(errorDescription)) {
            String outputStatusMessageProperty = bwcMode ? "org.openecomp.appc.dg.error" : Constants.DG_OUTPUT_STATUS_MESSAGE;
            errorDescriptionFromCtx = ctx.getAttribute(outputStatusMessageProperty);
            if(StringUtils.isEmpty(errorDescriptionFromCtx)){
                ctx.setAttribute(outputStatusMessageProperty, errorDescription);
            }else if  (!errorDescriptionFromCtx.contains(errorDescription)){
                ctx.setAttribute(outputStatusMessageProperty, errorDescriptionFromCtx+ " | "+ errorDescription);
            }
        }
    }


    @Override
    public void reportSuccess(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        Integer successReportCode = 500;
        String successDescription, apiVersion, eventId;
        successDescription = params.get(Constants.DG_OUTPUT_STATUS_MESSAGE);
        apiVersion = ctx.getAttribute("input.common-header.api-ver");
        eventId = ctx.getAttribute("input.common-header.request-id");
        ctx.setAttribute(Constants.DG_OUTPUT_STATUS_MESSAGE, successDescription);

        if (null == successDescription) {
            successDescription = "Success";
        }
        EventMessage eventMessage = new EventMessage(new EventHeader((new java.util.Date()).toString(), apiVersion, eventId), new EventStatus(successReportCode, successDescription));
        eventSender.sendEvent(DmaapDestination.DCAE, eventMessage);
    }

}
