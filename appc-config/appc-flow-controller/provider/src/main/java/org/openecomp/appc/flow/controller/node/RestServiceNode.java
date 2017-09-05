/*-
 * ============LICENSE_START=======================================================
 * ONAP : APP-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property.  All rights reserved.
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
 */

package org.openecomp.appc.flow.controller.node;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.openecomp.appc.flow.controller.data.Transaction;
import org.openecomp.appc.flow.controller.executorImpl.RestExecutor;
import org.openecomp.appc.flow.controller.utils.FlowControllerConstants;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RestServiceNode implements SvcLogicJavaPlugin{


    private static final  EELFLogger log = EELFManager.getInstance().getLogger(RestServiceNode.class);
    private static final String SDNC_CONFIG_DIR_VAR = "SDNC_CONFIG_DIR";

    public void sendRequest(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {
        String fn = "RestServiceNode.sendRequest";
        log.info("Received processParamKeys call with params : " + inParams);
        String responsePrefix = inParams.get(FlowControllerConstants.INPUT_PARAM_RESPONSE_PRIFIX);
        try
        {
            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix+".") : "";        
            //Remove below for Block
            for (Object key : ctx.getAttributeKeySet()) {
                String parmName = (String) key;
                String parmValue = ctx.getAttribute(parmName);
                log.info(fn  + "Getting Key = "  + parmName + "and Value = " +  parmValue);
            }
            
            send(ctx, inParams);
            ctx.setAttribute(responsePrefix + FlowControllerConstants.OUTPUT_PARAM_STATUS, FlowControllerConstants.OUTPUT_STATUS_SUCCESS);
            
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + FlowControllerConstants.OUTPUT_PARAM_STATUS, FlowControllerConstants.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + FlowControllerConstants.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
            e.printStackTrace();
            log.error("Error Message : "  + e.getMessage());
            throw new SvcLogicException(e.getMessage());
        }
    }

    public void send(SvcLogicContext ctx, Map<String, String> inParams) throws Exception{        
    try{
            Properties prop = loadProperties();
            log.info("Loaded Properties " + prop.toString());
            String responsePrefix = inParams.get(FlowControllerConstants.INPUT_PARAM_RESPONSE_PRIFIX);    
            RestExecutor restRequestExecutor = new RestExecutor();
            String resourceUri = "";
            if(ctx.getAttribute(FlowControllerConstants.INPUT_URL) != null && !(ctx.getAttribute(FlowControllerConstants.INPUT_URL).isEmpty()))
                resourceUri = ctx.getAttribute(FlowControllerConstants.INPUT_URL);
            else{
                resourceUri = resourceUri.concat(FlowControllerConstants.HTTP);
                log.info("resourceUri=  " + resourceUri );
                resourceUri = resourceUri.concat(ctx.getAttribute(FlowControllerConstants.INPUT_HOST_IP_ADDRESS));
                resourceUri = resourceUri.concat(":");
                resourceUri = resourceUri.concat(ctx.getAttribute(FlowControllerConstants.INPUT_PORT_NUMBER));
                
                if(ctx.getAttribute(FlowControllerConstants.INPUT_CONTEXT) != null && !ctx.getAttribute(FlowControllerConstants.INPUT_CONTEXT).isEmpty()){
                    resourceUri = resourceUri.concat("/").concat(ctx.getAttribute(FlowControllerConstants.INPUT_CONTEXT));
                    log.info("resourceUri= " + resourceUri );
                }
                else if(prop.getProperty(ctx.getAttribute(FlowControllerConstants.INPUT_REQUEST_ACTION).concat(".context")) != null ){
                    log.info("resourceUri = " + resourceUri );        
                    resourceUri = resourceUri.concat("/").concat(prop.getProperty(ctx.getAttribute(FlowControllerConstants.INPUT_REQUEST_ACTION).concat(".context")));
                }
                else 
                    throw new Exception("Could Not found the context for operation " + ctx.getAttribute(FlowControllerConstants.INPUT_REQUEST_ACTION));


                if(ctx.getAttribute(FlowControllerConstants.INPUT_SUB_CONTEXT) != null && !ctx.getAttribute(FlowControllerConstants.INPUT_SUB_CONTEXT).isEmpty()){
                    resourceUri = resourceUri.concat("/").concat(ctx.getAttribute(FlowControllerConstants.INPUT_SUB_CONTEXT)); 
                    log.info("resourceUri" + resourceUri );        
                }
                else if(prop.getProperty(ctx.getAttribute(FlowControllerConstants.INPUT_REQUEST_ACTION).concat(".sub-context")) != null ){
                    resourceUri = resourceUri.concat("/").concat(prop.getProperty(ctx.getAttribute(FlowControllerConstants.INPUT_REQUEST_ACTION).concat(".sub-context"))); 
                    log.info("resourceUri" + resourceUri );        
                }
            }

            log.info("Rest Constructed URL : " + resourceUri);
            Transaction transaction = new Transaction();

            transaction.setExecutionEndPoint(resourceUri);
            transaction.setExecutionRPC(ctx.getAttribute(FlowControllerConstants.INPUT_REQUEST_ACTION_TYPE));
            transaction.setAction(FlowControllerConstants.INPUT_REQUEST_ACTION);
            if(ctx.getAttribute(FlowControllerConstants.INPUT_REQUEST_ACTION_TYPE) == null || ctx.getAttribute(FlowControllerConstants.INPUT_REQUEST_ACTION_TYPE).isEmpty())
                throw new Exception("Dont know REST operation for Action " + transaction.getExecutionRPC());
            if(ctx.getAttribute(FlowControllerConstants.INPUT_REQUEST_ACTION) == null || ctx.getAttribute(FlowControllerConstants.INPUT_REQUEST_ACTION).isEmpty())
                throw new Exception("Dont know request-action " + transaction.getAction());

            //This code need to get changed to get the UserID and pass from a common place.
            if(transaction.getuId() == null )
                transaction.setuId(prop.getProperty(ctx.getAttribute(FlowControllerConstants.INPUT_REQUEST_ACTION).concat(".default-rest-user")));
            if(transaction.getPswd() == null)
                transaction.setPswd(prop.getProperty(ctx.getAttribute(FlowControllerConstants.INPUT_REQUEST_ACTION).concat(".default-rest-pass")));    

            HashMap<String, String> output = restRequestExecutor.execute(transaction, ctx);

            if(output.get("restResponse") !=null && isValidJSON(output.get("restResponse")) != null)
            {
                    ctx.setAttribute(responsePrefix + "." + FlowControllerConstants.OUTPUT_STATUS_MESSAGE , output.get("restResponse"));
//                JsonNode restResponse = isValidJSON(output.get("restResponse"));
//                for (String key : inParams.keySet()) {
//                    if(key !=null &&  key.startsWith("output-")){
//                            log.info("Found Key = " + key);
//                            log.info("Found Key in Params " + inParams.get(key) + ".");
//                            JsonNode setValue =  restResponse.findValue(inParams.get(key));                            
//                             log.info("Found value = " + setValue);
//                             if(setValue !=null && setValue.textValue() !=null && !setValue.textValue().isEmpty())
//                                 ctx.setAttribute(responsePrefix + "." + key, setValue.textValue());
//                             else
//                                 ctx.setAttribute(responsePrefix + "." + key, null);
//                    }
//                }                
            }
            log.info("Response from Rest :" );
        }
        catch(Exception e)
        {
            e.printStackTrace();
            log.error("Error Message " + e.getMessage());
            throw e;
        }
    }

    private Properties loadProperties() throws Exception {
        Properties props = new Properties();
        String propDir = System.getenv(SDNC_CONFIG_DIR_VAR);
        if (propDir == null)
            throw new Exception("Cannot find Property file -" + SDNC_CONFIG_DIR_VAR);
        String propFile = propDir + FlowControllerConstants.APPC_FLOW_CONTROLLER;
        InputStream propStream = new FileInputStream(propFile);        
        try
        {
            props.load(propStream);
        }
        catch (Exception e)
        {
            throw new Exception("Could not load properties file " + propFile, e);
        }
        finally
        {
            try
            {
                propStream.close();
            }
            catch (Exception e)
            {
                log.warn("Could not close FileInputStream", e);
            }
        }
        // TODO Auto-generated method stub
        return props;
    }
    
    public JsonNode isValidJSON(String json) throws IOException {
        JsonNode output = null;     
        log.info("Received response from Interface " + json);
        if(json ==null  || json.isEmpty())
            return null;
        try{ 
            ObjectMapper objectMapper = new ObjectMapper();
            output = objectMapper.readTree(json);
        } catch(JsonProcessingException e){
            log.warn("Response received from interface is not a valid JSON block" + json);
            return null;
        }    
        log.info("state is " + output.findValue("state"));
        
        return output;
    }
}
