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

import org.openecomp.sdnc.sli.SvcLogicContext;

import java.util.Map;

import org.openecomp.appc.dg.common.OutputMessagePlugin;
import org.openecomp.appc.exceptions.APPCException;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class OutputMessagePluginImpl implements OutputMessagePlugin {

    @Override
    public void outputMessageBuilder(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        String errorDescription, eventDescription;

        //making output.status.message
        errorDescription = params.get(Constants.ATTRIBUTE_ERROR_MESSAGE);
        eventDescription = params.get(Constants.EVENT_MESSAGE);

        addToContextIfNotContains(errorDescription , eventDescription, ctx);

        //making event-message

        if (!isEmpty(eventDescription)) {
            ctx.setAttribute(Constants.EVENT_MESSAGE, eventDescription);
        } else {
            ctx.setAttribute(Constants.EVENT_MESSAGE, ctx.getAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE));
        }
    }

    public static void addToContextIfNotContains(String errorDescription, String eventDescription, SvcLogicContext ctx) {
        if (!isEmpty(errorDescription)){
            if (isEmpty(ctx.getAttribute(Constants.DG_OUTPUT_STATUS_MESSAGE))) {
                ctx.setAttribute(Constants.DG_OUTPUT_STATUS_MESSAGE, errorDescription);
            }else if (!ctx.getAttribute(Constants.DG_OUTPUT_STATUS_MESSAGE).contains(errorDescription)) {
                ctx.setAttribute(Constants.DG_OUTPUT_STATUS_MESSAGE, ctx.getAttribute(Constants.DG_OUTPUT_STATUS_MESSAGE) + " | " + errorDescription);
            }
        }
        if (!isEmpty(eventDescription)){
            if (isEmpty(ctx.getAttribute(Constants.DG_OUTPUT_STATUS_MESSAGE))) {
                ctx.setAttribute(Constants.DG_OUTPUT_STATUS_MESSAGE, eventDescription);
            }else if (!ctx.getAttribute(Constants.DG_OUTPUT_STATUS_MESSAGE).contains(eventDescription)) {
                ctx.setAttribute(Constants.DG_OUTPUT_STATUS_MESSAGE, ctx.getAttribute(Constants.DG_OUTPUT_STATUS_MESSAGE) + " | " + eventDescription);
            }
        }
    }
}
