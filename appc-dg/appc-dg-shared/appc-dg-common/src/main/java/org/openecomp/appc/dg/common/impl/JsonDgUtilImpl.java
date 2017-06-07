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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.appc.dg.common.JsonDgUtil;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.i18n.Msg;
import org.openecomp.appc.util.JsonUtil;
import org.openecomp.sdnc.sli.SvcLogicContext;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


public class JsonDgUtilImpl implements JsonDgUtil {
    private static final EELFLogger logger = EELFManager.getInstance().getLogger(JsonDgUtilImpl.class);

    private static final ThreadLocal<SimpleDateFormat> DATE_TIME_PARSER_THREAD_LOCAL = new ThreadLocal<SimpleDateFormat>() {
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
    };

    @Override
    public void flatAndAddToContext(Map<String, String> params, SvcLogicContext ctx) throws APPCException {

        if (logger.isTraceEnabled()) {
            logger.trace("Entering to flatAndAddToContext with params = "+ ObjectUtils.toString(params)+", SvcLogicContext = "+ObjectUtils.toString(ctx));
        }
        try {
            String paramName = Constants.PAYLOAD;
            String payload = params.get(paramName);
            if (payload == "")
                payload = ctx.getAttribute("input.payload");
            if (!StringUtils.isEmpty(payload)) {
                Map<String, String> flatMap = JsonUtil.convertJsonStringToFlatMap(payload);
                if (flatMap != null && flatMap.size() > 0) {
                    for (Map.Entry<String, String> entry : flatMap.entrySet()) {
                        ctx.setAttribute(entry.getKey(), entry.getValue());
                    }
                }
            } else {
                logger.warn("input payload param value is empty (\"\") or null");
            }
        } catch (Exception e) {
            logger.error(e.toString());
            String msg = EELFResourceManager.format(Msg.INPUT_PAYLOAD_PARSING_FAILED,params.get(Constants.PAYLOAD));
            ctx.setAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE, msg);
            throw new APPCException(e);
        }
    }

    @Override
    public void generateOutputPayloadFromContext(Map<String, String> params, SvcLogicContext ctx) throws APPCException{
        if (logger.isTraceEnabled()) {
            logger.trace("Entering to generateOutputPayloadFromContext with SvcLogicContext = "+ObjectUtils.toString(ctx));
        }

        try {
            Set<String> keys = ctx.getAttributeKeySet();
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode JsonNode = objectMapper.createObjectNode();
            for (String key : keys) {
                if(key.startsWith(Constants.OUTPUT_PAYLOAD+".")){
                    String objkey=  key.replaceFirst(Constants.OUTPUT_PAYLOAD + ".", "");
                    if(objkey.contains("[") && objkey.contains("]")){
                        ArrayNode arrayNode;
                        String arrayKey = objkey.substring(0,objkey.indexOf('['));
                        int arrayIndex = Integer.parseInt(objkey.substring(objkey.indexOf('[')+1,objkey.indexOf(']')));
                        if(JsonNode.has(arrayKey)){
                            arrayNode = (ArrayNode)JsonNode.get(arrayKey);
                            arrayNode.insert(arrayIndex,ctx.getAttribute(key));
                        }else {
                            arrayNode = objectMapper.createArrayNode();
                            arrayNode.insert(arrayIndex,ctx.getAttribute(key));
                            JsonNode.put(arrayKey,arrayNode);
                        }
                    }else {
                        JsonNode.put(objkey, ctx.getAttribute(key));
                    }
                }
            }
            if(JsonNode.size()>0) {
                ctx.setAttribute(Constants.OUTPUT_PAYLOAD, objectMapper.writeValueAsString(JsonNode));
            }
        } catch (Exception e) {
            logger.error(e.toString());
            ctx.setAttribute(Constants.DG_OUTPUT_STATUS_MESSAGE, e.toString());
            throw new APPCException(e);
        }

    }

    @Override
    public void cvaasFileNameAndFileContentToContext(Map<String, String> params, SvcLogicContext ctx)
            throws APPCException {

        if (logger.isTraceEnabled()) {
            logger.trace("Entering to caasFileNameAndFileContentToContext with SvcLogicContext = "
                    + ObjectUtils.toString(ctx));
        }

        String vnfId = null;
        try {
            String cvassDirectoryPath = params.get(Constants.CVAAS_DIRECTORY_PATH);
            String appcInstanceId = params.get(Constants.APPC_INSTANCE_ID);

			/*
			 * File name
			 */
            vnfId = params.get("vnf-id");
            long timestampAsLongRepresentingFileCreationTime = System.currentTimeMillis();

            ctx.setAttribute(Constants.CVAAS_FILE_NAME, cvassDirectoryPath + File.separator + vnfId + "_"
                    + timestampAsLongRepresentingFileCreationTime + "_" + appcInstanceId + ".json");

			/*
			 * File content
			 */

            String uploadDate = ctx.getAttribute("running-config.upload-date");
            long epochUploadTimestamp = DATE_TIME_PARSER_THREAD_LOCAL.get().parse(uploadDate).getTime();

            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode jsonNode = objectMapper.createObjectNode();
            jsonNode.put("UPLOAD_CONFIG_ID", ctx.getAttribute("running-config.upload-config-id"));
            jsonNode.put("REQUEST_ID", ctx.getAttribute("running-config.request-id"));
            jsonNode.put("ORIGINATOR_ID", ctx.getAttribute("running-config.originator-id"));
            jsonNode.put("SERVICE_DESCRIPTION", ctx.getAttribute("running-config.service-description"));
            jsonNode.put("ACTION", ctx.getAttribute("running-config.action"));
            jsonNode.put("UPLOAD_TIMESTAMP", epochUploadTimestamp);
            jsonNode.put("UPLOAD_DATE", uploadDate);
            jsonNode.put("VNF_ID", vnfId);
            jsonNode.put("VNF_NAME", ctx.getAttribute("running-config.vnf-name"));
            jsonNode.put("VM_NAME", ctx.getAttribute("running-config.vm-name"));
            jsonNode.put("VNF_TYPE", ctx.getAttribute("running-config.vnf-type"));
            jsonNode.put("VNFC_TYPE", ctx.getAttribute("running-config.vnfc-type"));
            jsonNode.put("HOST_IP_ADDRESS", ctx.getAttribute("running-config.host-ip-address"));
            jsonNode.put("CONFIG_INDICATOR", ctx.getAttribute("running-config.config-indicator"));
            jsonNode.put("PENDING_DELETE", ctx.getAttribute("running-config.pending-delete"));
            jsonNode.put("CONTENT", ctx.getAttribute("running-config.content"));

            ctx.setAttribute(Constants.CVAAS_FILE_CONTENT,
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode));

        } catch (Exception e) {
            String errorMessage = "Failed to parse create cvass file for vnf with id : " + vnfId;
            logger.error(errorMessage, e);
            ctx.setAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE, errorMessage);
            throw new APPCException(e);
        }
    }

    @Override
    public void checkFileCreated(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        String filePath = ctx.getAttribute(Constants.CVAAS_FILE_NAME);
        File file = new File(filePath);

        if (!file.exists()) {
            String vnfId = params.get("vnf-id");
            String errorMessage = "Cvass file could not be created for vnf with id : " + vnfId;
            ctx.setAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE, errorMessage);
            throw new APPCException(errorMessage);
        }
    }
}
