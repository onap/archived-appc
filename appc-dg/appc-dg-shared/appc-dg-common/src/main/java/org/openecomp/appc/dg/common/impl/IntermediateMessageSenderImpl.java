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


import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang.StringUtils;
import org.openecomp.appc.adapter.message.MessageAdapterFactory;
import org.openecomp.appc.adapter.message.Producer;
import org.openecomp.appc.configuration.Configuration;
import org.openecomp.appc.configuration.ConfigurationFactory;
import org.openecomp.appc.dg.common.IntermediateMessageSender;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.sdnc.sli.SvcLogicContext;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class IntermediateMessageSenderImpl implements IntermediateMessageSender {


    private Producer producer;

    private Configuration configuration;

    private final EELFLogger logger = EELFManager.getInstance().getLogger(IntermediateMessageSenderImpl.class);

    private static final String STATUS = "STATUS";
    private static final String FAILURE = "FAILURE";
    private static final String SUCCESS = "SUCCESS";
    private static final String ERROR_MESSAGE = "ERROR_MESSAGE";

    private static final String RESPONSE = "response";
    private static final String MSO = "MSO";

    public void init(){
        configuration = ConfigurationFactory.getConfiguration();
        Properties properties=configuration.getProperties();

        String writeTopic = properties.getProperty("appc.LCM.topic.write");
        String apiKey = properties.getProperty("appc.LCM.client.key");
        String apiSecret = properties.getProperty("appc.LCM.client.secret");
        String hostNames = properties.getProperty("appc.LCM.poolMembers");

        logger.debug("Configuration Read : writeTopic = " + writeTopic +", " +
                                            "hostNames = " + hostNames);

        Set<String> pool = new HashSet<>();
        if (!StringUtils.isEmpty(hostNames)) {
            for (String name : hostNames.split(",")) {
                pool.add(name);
            }
        }

        BundleContext ctx = FrameworkUtil.getBundle(IntermediateMessageSenderImpl.class).getBundleContext();
        if (ctx != null) {
            ServiceReference svcRef = ctx.getServiceReference(MessageAdapterFactory.class.getName());
            if (svcRef != null) {
                producer = ((MessageAdapterFactory) ctx.getService(svcRef)).createProducer(pool, writeTopic,apiKey, apiSecret);
            }
        }
    }

    @Override
    public void sendMessage(Map<String, String> params, SvcLogicContext context) {
        String prefix = params.get("prefix");
        prefix = StringUtils.isEmpty(prefix)? "":prefix+".";
        try{
            validateInputs(params,context);
            String jsonMessage = getJsonMessage(params, context);
            logger.debug("Constructed JSON Message : " + jsonMessage);
            producer.post("",jsonMessage);
            context.setAttribute(prefix + STATUS, SUCCESS);
        }
        catch(Exception e){
            String errorMessage = "Error sending intermediate message to initiator " + e.getMessage();
            context.setAttribute(prefix + STATUS, FAILURE);
            context.setAttribute(prefix + ERROR_MESSAGE, errorMessage);
            logger.error(errorMessage,e);
        }
    }

    private void validateInputs(Map<String, String> params, SvcLogicContext context) throws APPCException {
        String code = params.get("code");
        String message = params.get("message");
        if(StringUtils.isEmpty(code) || StringUtils.isEmpty(message)){
            throw new APPCException("code or message is empty");
        }
        String requestId = context.getAttribute("input.common-header.request-id");
        if(StringUtils.isEmpty(requestId)){
            throw new APPCException("requestId is empty");
        }
    }

    private String getJsonMessage(Map<String, String> params, SvcLogicContext context) {
        ObjectMapper objectMapper = new ObjectMapper();

        ObjectNode commonHeader = getCommonHeader(context);
        ObjectNode status = getStatus(params);

        ObjectNode output = objectMapper.createObjectNode();
        output.put("common-header",commonHeader);
        output.put("status",status);

        ObjectNode body =  objectMapper.createObjectNode();
        body.put("output",output);

        ObjectNode root = objectMapper.createObjectNode();
        root.put("type", RESPONSE);
        root.put("rpc-name",context.getAttribute("input.action"));
        root.put("cambria.partition", MSO);
        root.put("correlation-id",getCorrelationId(context));
        root.put("body",body);

        return root.toString();
    }

    private String getCorrelationId(SvcLogicContext context) {
        String requestId = context.getAttribute("input.common-header.request-id");
        String subRequestId = context.getAttribute("input.common-header.sub-request-id");
        return requestId + (StringUtils.isEmpty(subRequestId)?"":("-"+subRequestId));
    }

    private ObjectNode getStatus(Map<String,String> params) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode status =  objectMapper.createObjectNode();
        status.put("code",params.get("code"));
        status.put("message",params.get("message"));
        return status;
    }

    private ObjectNode getCommonHeader(SvcLogicContext context) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode commonHeader =  objectMapper.createObjectNode();
        commonHeader.put("api-ver",context.getAttribute("input.common-header.api-ver"));
        commonHeader.put("timestamp",context.getAttribute("input.common-header.timestamp"));
        commonHeader.put("originator-id",context.getAttribute("input.common-header.originator-id"));
        commonHeader.put("request-id",context.getAttribute("input.common-header.request-id"));
        commonHeader.put("sub-request-id",context.getAttribute("input.common-header.sub-request-id"));
        return commonHeader;
    }
}
