/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications (C) 2019 Ericsson
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

package org.onap.appc.workflow.impl;

import org.apache.commons.lang.ObjectUtils;
import org.onap.appc.common.constant.Constants;
import org.onap.appc.configuration.Configuration;
import org.onap.appc.configuration.ConfigurationFactory;
import org.onap.appc.domainmodel.lcm.RequestContext;
import org.onap.appc.domainmodel.lcm.ResponseContext;
import org.onap.appc.util.ObjectMapper;
import org.onap.appc.workflow.WorkFlowManager;
import org.onap.appc.workflow.objects.WorkflowExistsOutput;
import org.onap.appc.workflow.objects.WorkflowRequest;
import org.onap.appc.workflow.objects.WorkflowResponse;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicService;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;


public class WorkFlowManagerImpl implements WorkFlowManager{
    private SvcLogicService svcLogic = null;
    private final EELFLogger logger = EELFManager.getInstance().getLogger(WorkFlowManagerImpl.class);
    private final Configuration configuration = ConfigurationFactory.getConfiguration();

    private  WorkflowResolver workflowResolver = new WorkflowResolver(
            configuration.getIntegerProperty("org.onap.appc.workflow.resolver.refresh_interval", 300)
    );

    public void setWorkflowResolver(WorkflowResolver workflowResolver){
        this.workflowResolver = workflowResolver;
    }

    public void setSvcLogicServiceRef(SvcLogicService svcLogic) {
        this.svcLogic = svcLogic;
    }

    /**
     * Execute workflow and return response.
     * This method execute workflow with following steps.
     * Retrieve workflow(DG) details - module, version and mode  from database based on command and vnf Type from incoming request.
     * Execute workflow (DG) using SVC Logic Service reference
     * Return response of workflow (DG) to caller.
     *
     * @param workflowRequest workflow execution request which contains vnfType, command, requestId, targetId, payload and (optional) confID;
     * @return Workflow Response which contains execution status and payload from DG if any
     */

    @Override
    public WorkflowResponse executeWorkflow(WorkflowRequest workflowRequest) {
        logger.trace("Entering to executeWorkflow with WorkflowRequest = " + ObjectUtils.toString(workflowRequest.toString()));
        WorkflowResponse workflowResponse = new WorkflowResponse();
        workflowResponse.setResponseContext(workflowRequest.getResponseContext());

        try {
            WorkflowKey workflowKey = workflowResolver.resolve(workflowRequest.getRequestContext().getAction().name(), workflowRequest.getVnfContext().getType(), null,workflowRequest.getRequestContext().getCommonHeader().getApiVer());

            Properties workflowParams = new Properties();
            String actionProperty;
            String requestIdProperty;
            String vfIdProperty;
            if(!workflowRequest.getRequestContext().getCommonHeader().getApiVer().startsWith("1.")){
                /*
                The following method call (populateDGContext) populates DG context with the
                request parameters to maintain backward compatibility with old DGs,
                 we are not altering the old way of passing (org.onap.appc.vnfId and so on..)
                This is still a temporary solution, the end solution should be agreed with
                all stakeholders and implemented.
             */
                populateDGContext(workflowParams,workflowRequest);
            } else {
                actionProperty = configuration.getProperty("org.onap.appc.workflow.action", String.valueOf(Constants.ACTION));
                requestIdProperty = configuration.getProperty("org.onap.appc.workflow.request.id", String.valueOf(Constants.REQUEST_ID));
                vfIdProperty = configuration.getProperty("org.onap.appc.workflow.vfid", String.valueOf(Constants.VF_ID));
                String vfTypeProperty = configuration.getProperty("org.onap.appc.workflow.vftype", String.valueOf(Constants.VF_TYPE));
                String apiVerProperty = configuration.getProperty("org.onap.appc.workflow.apiVersion", String.valueOf(Constants.API_VERSION));
                String originatorIdProperty = configuration.getProperty("org.onap.appc.workflow.originatorId",  Constants.ORIGINATOR_ID);
                String subRequestId = configuration.getProperty("org.onap.appc.workflow.subRequestId", Constants.SUB_REQUEST_ID);

                workflowParams.put(actionProperty, workflowRequest.getRequestContext().getAction().name());
                workflowParams.put(requestIdProperty, workflowRequest.getRequestContext().getCommonHeader().getRequestId());
                workflowParams.put(vfIdProperty, workflowRequest.getVnfContext().getId());
                workflowParams.put(vfTypeProperty, workflowRequest.getVnfContext().getType());
                workflowParams.put(apiVerProperty, workflowRequest.getRequestContext().getCommonHeader().getApiVer());
                workflowParams.put(originatorIdProperty, workflowRequest.getRequestContext().getCommonHeader().getOriginatorId());
                workflowParams.put(subRequestId, workflowRequest.getRequestContext().getCommonHeader().getSubRequestId());

                Object payloadJson = workflowRequest.getRequestContext().getPayload();
                if(payloadJson != null) {
                    try {
                        Map<String, String> payloadProperties = ObjectMapper.map(payloadJson);
                        workflowParams.putAll(payloadProperties);

                        logger.debug("DG properties: " + workflowParams);
                    } catch (Exception e) {
                        logger.error("Error parsing payload json string", e);
                        Properties workflowPrp = new Properties();
                        workflowPrp.setProperty("error-message", "Error parsing payload json string");
                        fillStatus(501, "Error parsing payload json string: " + e.getMessage(), workflowRequest.getResponseContext());
                        logger.trace("Exiting from executeWorkflow with (workflowResponse = " + ObjectUtils.toString(workflowResponse) + ")");
                        return workflowResponse;
                    }
                }
                logger.debug("DG parameters "+ actionProperty +":"+ workflowRequest.getRequestContext().getAction().name() + ", "+
                        requestIdProperty +":"+ workflowRequest.getRequestContext().getCommonHeader().getRequestId() + ", " +
                        vfIdProperty + ":" + workflowRequest.getVnfContext().getId());

                logger.debug("Starting DG Execution for request "+workflowRequest.getRequestContext().getCommonHeader().getRequestId());
            }
            if (workflowRequest.getRequestContext().getCommonHeader().getApiVer().startsWith("1.")){
                workflowParams.put("isBwcMode", "true");
            } else {
                workflowParams.put("isBwcMode", "false");
            }

            SVCLogicServiceExecute(workflowKey, workflowRequest.getRequestContext(), workflowParams , workflowResponse);
            logger.trace("Completed DG Execution for Request id: " + workflowRequest.getRequestContext().getCommonHeader().getRequestId()
                    + "with response code: " + workflowResponse.getResponseContext().getStatus().getCode());
        }catch (Exception e){
            logger.error("Error Executing DG " + e.getMessage(), e);
            fillStatus(501, "Error Executing DG "+ e.getMessage(), workflowRequest.getResponseContext());
        }
        logger.trace("Exiting from executeWorkflow with (workflowResponse = " +
                ObjectUtils.toString(workflowResponse.getResponseContext().getStatus().getMessage()) + ")");
        return workflowResponse;
    }

    private void populateDGContext(Properties workflowParams, WorkflowRequest workflowRequest) {
        workflowParams.put("input.common-header.timestamp", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(workflowRequest.getRequestContext().getCommonHeader().getTimeStamp()));
        workflowParams.put("input.common-header.api-ver", workflowRequest.getRequestContext().getCommonHeader().getApiVer());
        workflowParams.put("input.common-header.request-id", workflowRequest.getRequestContext().getCommonHeader().getRequestId());
        workflowParams.put("input.common-header.originator-id", workflowRequest.getRequestContext().getCommonHeader().getOriginatorId());
        workflowParams.put("input.common-header.sub-request-id", workflowRequest.getRequestContext().getCommonHeader().getSubRequestId() != null ?
                workflowRequest.getRequestContext().getCommonHeader().getSubRequestId() : "");
        workflowParams.put("input.action", workflowRequest.getRequestContext().getAction().toString());
        workflowParams.put("input.payload", null != workflowRequest.getRequestContext().getPayload() ?
                workflowRequest.getRequestContext().getPayload() : "");
        workflowParams.put("input.action-identifiers.vnf-id", workflowRequest.getVnfContext().getId());
        workflowParams.put("input.action-identifiers.vnfc-name", workflowRequest.getRequestContext().getActionIdentifiers().getVnfcName() != null ?
                workflowRequest.getRequestContext().getActionIdentifiers().getVnfcName() : "");
        workflowParams.put("input.action-identifiers.service-instance-id", workflowRequest.getRequestContext().getActionIdentifiers().getServiceInstanceId() !=null ?
                workflowRequest.getRequestContext().getActionIdentifiers().getServiceInstanceId() : "");
        workflowParams.put("input.action-identifiers.vserver-id", workflowRequest.getRequestContext().getActionIdentifiers().getVserverId() !=null ?
                workflowRequest.getRequestContext().getActionIdentifiers().getVserverId() : "");
        workflowParams.put("input.action-identifiers.vf-module-id",workflowRequest.getRequestContext().getActionIdentifiers().getVfModuleId() !=null ?
                workflowRequest.getRequestContext().getActionIdentifiers().getVfModuleId() : "");
        final Map<String, String> additionalContext;
        if ((additionalContext = workflowRequest.getRequestContext().getAdditionalContext())!=null) {
            for (Map.Entry<String, String> entry : additionalContext.entrySet()) {
                workflowParams.put("input." + entry.getKey(), null != entry.getValue() ? entry.getValue() : "");
    }
        }
    }

    /**
     * Check if workflow (DG) exists in database
     *
     * @param workflowQueryParams workflow request with command and vnf Type
     * @return True if workflow exists else False.
     */
    @Override
    public WorkflowExistsOutput workflowExists(WorkflowRequest workflowQueryParams) {
        WorkflowExistsOutput workflowExistsOutput = new WorkflowExistsOutput(false,false);
        logger.trace("Entering to workflowExists with WorkflowRequest = " + ObjectUtils.toString(workflowQueryParams.toString()));

        try {
            WorkflowKey workflowKey = workflowResolver.resolve(
                    workflowQueryParams.getRequestContext().getAction().name(),
                    workflowQueryParams.getVnfContext().getType(),
                    workflowQueryParams.getVnfContext().getVersion(),
                    workflowQueryParams.getRequestContext().getCommonHeader().getApiVer());
            if (workflowKey != null) {
                workflowExistsOutput.setMappingExist(true);
                workflowExistsOutput.setWorkflowModule(workflowKey.module());
                workflowExistsOutput.setWorkflowName(workflowKey.name());
                workflowExistsOutput.setWorkflowVersion(workflowKey.version());
                if (isDGExists(workflowKey)) {
                    workflowExistsOutput.setDgExist(true);
                }else{
                    logger.warn(
                            String.format("SLI doesn't have DG for resolved mapping entry:  DG module - '%s', DG name - '%s', DG version - '%s'",
                                    workflowKey.module(), workflowKey.name(), workflowKey.version()));
                }
            }else{
                logger.warn(
                        String.format("Unable to resolve recipe matching action '%s', VNF type '%s' and VNF version '%s'",
                                workflowQueryParams.getRequestContext().getAction().name(), workflowQueryParams.getVnfContext().getType(), null));
            }
        } catch (RuntimeException e) {
            logger.error("Error querying workflow from database"+e.getMessage());
            throw e;
        }catch (SvcLogicException e) {
            logger.error("Error querying workflow from database"+e.getMessage());
            throw new RuntimeException(e);
        }
        logger.trace("Exiting workflowExists");
        return workflowExistsOutput;
    }


    private boolean isDGExists(WorkflowKey workflowKey) throws SvcLogicException {
        return svcLogic.hasGraph(workflowKey.module(), workflowKey.name(), workflowKey.version(), "sync");
    }

    private void SVCLogicServiceExecute(WorkflowKey workflowKey, RequestContext requestContext, Properties workflowParams, WorkflowResponse workflowResponse) {
        logger.trace("Entering SVCLogicServiceExecute");

        Properties respProps = null;

        try {
            respProps = svcLogic.execute(workflowKey.module(), workflowKey.name(), workflowKey.version(), "sync", workflowParams);
        } catch (Exception e) {
            setWorkFlowResponseStatus(workflowResponse.getResponseContext(), "failure", "Unexpected SLI Adapter failure", 200);

            logger.debug("Error while executing DG " + e.getMessage() + e.getStackTrace());
            logger.error("Error in DG", e.getMessage()+ Arrays.toString(e.getStackTrace()),e);
        }

        if (respProps != null) {
            if (!requestContext.getCommonHeader().getApiVer().startsWith("1.")) {
                fillResponseContextByOutputFieldsFromDgContext(workflowResponse.getResponseContext(), respProps);
            }

            final String commonStatus = respProps.getProperty(Constants.DG_ATTRIBUTE_STATUS);
            final String specificStatusMessage = respProps.getProperty(Constants.DG_OUTPUT_STATUS_MESSAGE);
            String dgOutputStatusCode = respProps.getProperty(Constants.DG_OUTPUT_STATUS_CODE);
            int specificStatusCode = 0;
            if (dgOutputStatusCode != null) {
                specificStatusCode = Integer.parseInt(dgOutputStatusCode);
            }

            setWorkFlowResponseStatus(workflowResponse.getResponseContext(), commonStatus, specificStatusMessage, specificStatusCode);

            logger.debug("DG Execution Status: " + commonStatus);
        }

        logger.trace("Exiting from SVCLogicServiceExecute");
    }

    /**
     * Filling response context by output.* fields from DG context. Works only for 2.* API version
     *
     * @param responseContext response context which you need to fill
     * @param respProps DG context in a properties format
     */
    private void fillResponseContextByOutputFieldsFromDgContext(ResponseContext responseContext, Properties respProps) {

        Enumeration<?> e = respProps.propertyNames();
        while (e.hasMoreElements()){
            String key = (String) e.nextElement();
            if (key.startsWith("output.")){
                if (!key.startsWith("output.common-header.") && !key.startsWith("output.status.")){

                    if (key.equalsIgnoreCase("output.payload")){
                        responseContext.setPayload(respProps.getProperty(key));
                    } else {
                        responseContext.addKeyValueToAdditionalContext(key, respProps.getProperty(key));
        }
                }
            }
        }
    }

    /**
     * Filling responceContext status code amd message according to responce messages and codes from DG.
     *
     * @param responseContext response cotext
     * @param commonStatus common status message from DG ("success" or "failure")
     * @param specificStatusMessage specific status message from specific DG node
     * @param specificStatusCode specific status code from specific DG node
     */
    private void setWorkFlowResponseStatus(ResponseContext responseContext, String commonStatus, String specificStatusMessage, int specificStatusCode) {
        if (null == specificStatusMessage) { specificStatusMessage = ""; }
        if (commonStatus.equalsIgnoreCase(Constants.DG_STATUS_SUCCESS)){
            if (specificStatusCode != 0 ){
                fillStatus(specificStatusCode, specificStatusMessage, responseContext);
            } else {
                fillStatus(400, commonStatus, responseContext);
            }
        } else {
            if (specificStatusCode != 0){
                fillStatus(specificStatusCode, specificStatusMessage, responseContext);
            } else {
                fillStatus(401, specificStatusMessage, responseContext);
            }
        }
    }

    /**
     * filling responseContext by status code and status message
     *
     * @param code 3-digit status code
     * @param message explanation of a status code
     * @param responceContext response context which will be store status code and status message
     */
    private void fillStatus(int code, String message, ResponseContext responceContext) {
        responceContext.getStatus().setCode(code);
        responceContext.getStatus().setMessage(message);
    }
}
