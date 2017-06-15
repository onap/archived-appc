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

package org.openecomp.appc.adapter.ansible.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;
import java.lang.*;
    
import org.openecomp.appc.Constants;
import org.openecomp.appc.exceptions.APPCException;

import org.openecomp.appc.configuration.Configuration;
import org.openecomp.appc.configuration.ConfigurationFactory;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.i18n.Msg;
import org.openecomp.appc.pool.Pool;
import org.openecomp.appc.pool.PoolExtensionException;
import org.openecomp.appc.util.StructuredPropertyHelper;
import org.openecomp.appc.util.StructuredPropertyHelper.Node;

import org.openecomp.sdnc.sli.SvcLogicContext;
import org.openecomp.sdnc.sli.SvcLogicException;


import org.slf4j.MDC;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;


import com.google.common.base.Strings;
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;

import org.openecomp.appc.adapter.ansible.AnsibleAdapter;

import org.openecomp.appc.adapter.ansible.model.AnsibleResult;
import org.openecomp.appc.adapter.ansible.model.AnsibleMessageParser;
import org.openecomp.appc.adapter.ansible.model.AnsibleResultCodes;
import org.openecomp.appc.adapter.ansible.model.AnsibleServerEmulator;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import static com.att.eelf.configuration.Configuration.*;


/**
 * This class implements the {@link AnsibleAdapter} interface. This interface
 * defines the behaviors that our service provides.
 *
 */
public class AnsibleAdapterImpl implements AnsibleAdapter {

    /**
     * The constant used to define the adapter name in the mapped diagnostic
     * context
     */
	

    @SuppressWarnings("nls")
    public static final String MDC_ADAPTER = "Ansible Adapter";

    /**
     * The constant used to define the service name in the mapped diagnostic
     * context
     */
    @SuppressWarnings("nls")
    public static final String MDC_SERVICE = "service";

    /**
     * The constant for the status code for a failed outcome
     */
    @SuppressWarnings("nls")
    public static final String OUTCOME_FAILURE = "failure";

    /**
     * The constant for the status code for a successful outcome
     */
    @SuppressWarnings("nls")
    public static final String OUTCOME_SUCCESS = "success";

    /**
      Adapter Name 
    **/
    private static final String ADAPTER_NAME = "Ansible Adapter";

   
    /**
     * The logger to be used
     */
    private static final EELFLogger logger = EELFManager.getInstance().getLogger(AnsibleAdapterImpl.class);
    
    /**
      * A reference to the adapter configuration object.
    */
    private Configuration configuration;;

    /** can Specify a X509 certificate file for use if required ... 
    Must be initialized with setCertFile 
    **/
    private String certFile = "";


    /**
     * Connection object 
     **/
    ConnectionBuilder  http_client ;
    
    /** 
     * Ansible API Message Handlers
     **/
    private AnsibleMessageParser messageProcessor;

    /**
       indicator whether in test mode
    **/
    private boolean  testMode = false;

    /**
       server emulator object to be used if in test mode 
    **/
    private AnsibleServerEmulator testServer;
    
    /**
     * This default constructor is used as a work around because the activator
     * wasnt getting called
     */
    public AnsibleAdapterImpl() {
	initialize();
    }


    /**
     * @param props
     *            not used
     */
    public AnsibleAdapterImpl(Properties props) {
	initialize();
    }



    /** 
	Used for jUnit test and testing interface 
    **/
    public AnsibleAdapterImpl(boolean Mode){
	testMode = Mode;
	testServer = new AnsibleServerEmulator();
	messageProcessor = new AnsibleMessageParser();
    }
    
    /**
     * Returns the symbolic name of the adapter
     * 
     * @return The adapter name
     * @see org.openecomp.appc.adapter.rest.AnsibleAdapter#getAdapterName()
     */
    @Override
    public String getAdapterName() {
	return ADAPTER_NAME;
    }


    
    /**
     * @param rc
     *  Method posts info to Context memory in case of an error
     *  and throws a SvcLogicException causing SLI to register this as a failure
     */
    @SuppressWarnings("static-method")
    private void doFailure(SvcLogicContext svcLogic,  int code, String message)  throws SvcLogicException {

	svcLogic.setStatus(OUTCOME_FAILURE);
	svcLogic.setAttribute("org.openecomp.appc.adapter.ansible.result.code",Integer.toString(code));
	svcLogic.setAttribute("org.openecomp.appc.adapter.ansible.message",message);
	
	throw new SvcLogicException("Ansible Adapter Error = " + message );
    }
	

    /**
     * initialize the  Ansible adapter based on default and over-ride configuration data  
     */
    private void initialize()  {

	configuration = ConfigurationFactory.getConfiguration();
	Properties props = configuration.getProperties();
	
	// Create the message processor instance 
	messageProcessor = new AnsibleMessageParser();

        // Create the http client instance
        // type of client is extracted from the property file parameter
        // org.openecomp.appc.adapter.ansible.clientType
        // It can be :
        //     1. TRUST_ALL  (trust all SSL certs). To be used ONLY in dev
        //     2. TRUST_CERT (trust only those whose certificates have been stored in the trustStore file)
        //     3. DEFAULT    (trust only well known certificates). This is standard behaviour to which it will
        //     revert. To be used in PROD

        try{
            String clientType = props.getProperty("org.openecomp.appc.adapter.ansible.clientType");
	    logger.info("Ansible http client type set to " + clientType);

            if (clientType.equals("TRUST_ALL")){
                logger.info("Creating http client to trust ALL ssl certificates. WARNING. This should be done only in dev environments");
                http_client = new ConnectionBuilder(1);
            }
            else if (clientType.equals("TRUST_CERT")){
                // set path to keystore file
                String trustStoreFile = props.getProperty("org.openecomp.appc.adapter.ansible.trustStore");
                String key  = props.getProperty("org.openecomp.appc.adapter.ansible.trustStore.trustPasswd");
                char [] trustStorePasswd = key.toCharArray();
                String trustStoreType = "JKS";
                logger.info("Creating http client with trustmanager from " + trustStoreFile);
                http_client = new ConnectionBuilder(trustStoreFile, trustStorePasswd);
            }
            else{
                logger.info("Creating http client with default behaviour");
                http_client = new ConnectionBuilder(0);
            }
        }
        catch (Exception e){
            logger.error("Error Initializing Ansible Adapter due to Unknown Exception: reason = " + e.getMessage());
        }

	logger.info("Intitialized Ansible Adapter");
	
    }


    /** set the certificate file if not a trusted/known CA **/
    private void setCertFile(String CertFile){
	this.certFile = CertFile;
    }
    


    // Public Method to post request to execute playbook. Posts the following back
    // to Svc context memory
    //  org.openecomp.appc.adapter.ansible.req.code : 100 if successful
    //  org.openecomp.appc.adapter.ansible.req.messge : any message
    //  org.openecomp.appc.adapter.ansible.req.Id : a unique uuid to reference the request

    public void reqExec(Map <String, String> params, SvcLogicContext ctx) throws SvcLogicException {

	String PlaybookName = "";
	String payload = "";
	String AgentUrl = "";
	String User = "";
	String Password = "";
	String Id = "";
	
	JSONObject JsonPayload;
	
	try{
	    // create json object to send request
	    JsonPayload = messageProcessor.ReqMessage(params);
	    
	    AgentUrl = (String) JsonPayload.remove("AgentUrl");
       	    User =  (String) JsonPayload.remove("User");
	    Password = (String) JsonPayload.remove("Password");
	    Id = (String)JsonPayload.getString("Id");
	    payload = JsonPayload.toString();
	    logger.info("Updated Payload  = "  + payload);
	}
	catch(APPCException e){
	    doFailure(ctx, AnsibleResultCodes.INVALID_PAYLOAD.getValue(), "Error constructing request for execution of playbook due to missing mandatory parameters. Reason = " + e.getMessage());
	}
	catch(JSONException e){
	    doFailure(ctx, AnsibleResultCodes.INVALID_PAYLOAD.getValue(), "Error constructing request for execution of playbook due to invalid JSON block. Reason = " + e.getMessage());
	}
	catch(NumberFormatException e){
	    doFailure(ctx, AnsibleResultCodes.INVALID_PAYLOAD.getValue(), "Error constructing request for execution of playbook due to invalid parameter values. Reason = " + e.getMessage());
	}
	
    
 
	int code = -1;
	String message = "";
	
	try{
	    
	    // post the test request
	    //---------------------------------------
	    logger.info("Posting request = " + payload + " to url = " + AgentUrl );
	    AnsibleResult testresult = postExecRequest(AgentUrl, payload, User, Password);

    
	    // Process if HTTP was successfull
	    if(testresult.getStatusCode() == 200){
		testresult = messageProcessor.parsePostResponse(testresult.getStatusMessage());
	    }
	    else{
		doFailure(ctx, testresult.getStatusCode(), "Error posting request. Reason = " + testresult.getStatusMessage());
	    }

    
	    code = testresult.getStatusCode();
	    message = testresult.getStatusMessage();

		    
	    // Check status of test request returned by Agent
	    //-----------------------------------------------
	    if (code == AnsibleResultCodes.PENDING.getValue()){
		logger.info(String.format("Submission of Test %s successful.", PlaybookName));
		// test request accepted. We are in asynchronous case
	    }
	    else{
		doFailure(ctx, code, "Request for execution of playbook rejected. Reason = " + message);
	    }
	}
	
	catch(APPCException e){
	    doFailure(ctx, AnsibleResultCodes.UNKNOWN_EXCEPTION.getValue(), "Exception encountered when posting request for execution of playbook. Reason = "  + e.getMessage());
	}


	ctx.setAttribute("org.openecomp.appc.adapter.ansible.result.code", Integer.toString(code));
	ctx.setAttribute("org.openecomp.appc.adapter.ansible.message", message );
	ctx.setAttribute("org.openecomp.appc.adapter.ansible.Id", Id);
	
    }


    // Public method to query status of a specific request
    // It blocks till the Ansible Server responds or the session times out
    
    public void reqExecResult(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {

	    
	// Get uri
	String ReqUri = "";
	
	try{
	    ReqUri = messageProcessor.ReqUri_Result(params);
	    System.out.println("Got uri = " + ReqUri);
	}
	catch(APPCException e){
	    doFailure(ctx, AnsibleResultCodes.INVALID_PAYLOAD.getValue(), "Error constructing request to retreive result due to missing parameters. Reason = " + e.getMessage());
	    return;
	}
	catch(NumberFormatException e){
	    doFailure(ctx, AnsibleResultCodes.INVALID_PAYLOAD.getValue(), "Error constructing request to retreive result due to invalid parameters value. Reason = " + e.getMessage());
	    return;
	}

	int code = -1;
	String message = "";
	String results = "";
	
	try{
	    // Try to  retreive the test results (modify the url for that)
	    AnsibleResult testresult = queryServer(ReqUri, params.get("User"), params.get("Password"));
	    code = testresult.getStatusCode();
	    message = testresult.getStatusMessage();

	    if(code == 200){
		logger.info("Parsing response from Server = " + message);
		// Valid HTTP. process the Ansible message
		testresult = messageProcessor.parseGetResponse(message);
		code = testresult.getStatusCode();
		message = testresult.getStatusMessage();
		results = testresult.getResults();
		
	    }
	    
	    logger.info("Request response = " + message);

	}
	catch (APPCException e){
	    doFailure(ctx, AnsibleResultCodes.UNKNOWN_EXCEPTION.getValue(), "Exception encountered retreiving result : " + e.getMessage());
	    return;
	}

	// We were able to get and process the results. Determine if playbook succeeded
	
	if (code == AnsibleResultCodes.FINAL_SUCCESS.getValue()){
	    message = String.format("Ansible Request  %s finished with Result = %s, Message = %s", params.get("Id"), OUTCOME_SUCCESS, message);
	    logger.info(message);
	}
	else {
	    logger.info(String.format("Ansible Request  %s finished with Result %s, Message = %s", params.get("Id"), OUTCOME_FAILURE, message));
	    ctx.setAttribute("org.openecomp.appc.adapter.ansible.results", results);
	    doFailure(ctx, code, message );
	    return;	    
	}
	
      
	ctx.setAttribute("org.openecomp.appc.adapter.ansible.result.code", Integer.toString(400));
	ctx.setAttribute("org.openecomp.appc.adapter.ansible.message",message);
	ctx.setAttribute("org.openecomp.appc.adapter.ansible.results", results);
	ctx.setStatus(OUTCOME_SUCCESS);
    }
    

    // Public method to get logs  from plyabook execution for a  specifcic request
    // It blocks till the Ansible Server responds or the session times out
    // very similar to reqExecResult
    // logs are returned in the DG context variable org.openecomp.appc.adapter.ansible.log
    
    public void reqExecLog(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException{


	// Get uri
	String ReqUri = "";
	try{
	    ReqUri = messageProcessor.ReqUri_Log(params);
	    logger.info("Retreiving results from " + ReqUri); 
	}
	catch(Exception e){
	    doFailure(ctx, AnsibleResultCodes.INVALID_PAYLOAD.getValue(), e.getMessage());
	}

	int code = -1;
	String message = "";
	float Duration = -1;
	
	try{
	    // Try to  retreive the test results (modify the url for that)
	    AnsibleResult testresult = queryServer(ReqUri, params.get("User"), params.get("Password"));
	    code = testresult.getStatusCode();
	    message = testresult.getStatusMessage();

	    logger.info("Request output = " + message);

	}
	catch (Exception e){
	    doFailure(ctx, AnsibleResultCodes.UNKNOWN_EXCEPTION.getValue(), "Exception encountered retreiving output : " + e.getMessage());
	}
	
	ctx.setAttribute("org.openecomp.appc.adapter.ansible.log",message);
	ctx.setStatus(OUTCOME_SUCCESS);
    }
    

    

	
    /**
     * Method that posts the request
     **/
    
    private AnsibleResult  postExecRequest(String AgentUrl, String Payload, String User, String Password)  {
	
	String reqOutput = "UNKNOWN";
	int    reqStatus = -1;

	AnsibleResult testresult;
	
	if (!testMode){
	    http_client.setHttpContext(User, Password);
	    testresult  = http_client.Post(AgentUrl, Payload);
	}
	else{
	    testresult = testServer.Post(AgentUrl, Payload);
	}
	   
	return testresult;
    }
    

    /* 
       Method to query Ansible server

    */
    private AnsibleResult queryServer(String AgentUrl, String User, String Password) {

	String testOutput = "UNKNOWN";
	int    testStatus = -1;
	AnsibleResult testresult;
	
	logger.info("Querying url = " + AgentUrl);

	if (!testMode){
	    testresult = http_client.Get(AgentUrl);
	}
	else{
	    testresult = testServer.Get(AgentUrl);
	}
	
	return testresult;
	
    }



}
