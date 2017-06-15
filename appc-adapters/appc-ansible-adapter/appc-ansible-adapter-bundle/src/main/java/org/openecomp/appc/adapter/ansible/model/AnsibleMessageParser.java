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

package org.openecomp.appc.adapter.ansible.model;

/**
 * This module imples the APP-C/Ansible Server interface
 * based on the REST API specifications
 */

import java.lang.NumberFormatException ;
import java.util.*;
import com.google.common.base.Strings;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.adapter.ansible.model.AnsibleResult;


/**
 * Class that validates and constructs requests sent/received from 
 * Ansible Server
 *
 */
public class AnsibleMessageParser {




    // Accepts a map of strings and
    // a) validates if all parameters are appropriate (else, throws an exception)
    // and b) if correct returns a JSON object with appropriate key-value
    // pairs to send to the server. 
    public JSONObject ReqMessage(Map <String, String> params) throws APPCException, NumberFormatException, JSONException{

	// Mandatory  parameters, that must be in the supplied information to the Ansible Adapter
	// 1. URL to connect to
	// 2. credentials for URL (assume username password for now)
	// 3. Playbook name
	String[] mandatoryTestParams = {"AgentUrl", "PlaybookName", "User", "Password"};

	// Optional testService parameters that may be provided in the request
	String[] optionalTestParams = {"EnvParameters", "NodeList", "LocalParameters", "Timeout", "Version", "FileParameters", "Action"};

	JSONObject JsonPayload = new JSONObject();
	String payload = "";
	JSONObject paramsJson;

  
	// Verify all the mandatory parameters are there 
	for (String key: mandatoryTestParams){
	    if (! params.containsKey(key)){
		throw new APPCException(String.format("Ansible: Mandatory AnsibleAdapter key %s not found in parameters provided by calling agent !", key));
	    }
	    payload = params.get(key);
	    if (Strings.isNullOrEmpty(payload)){
		throw new APPCException(String.format("Ansible: Mandatory AnsibleAdapter key % value is Null or Emtpy", key));
	    }
	    
	    JsonPayload.put(key, payload);
	}

	// Iterate through optional parameters
	// If null or empty omit it 
	for (String key : optionalTestParams){
	    if (params.containsKey(key)){
		payload = params.get(key);
		if(!Strings.isNullOrEmpty(payload)){
		    
		    // different cases require different treatment
		    switch (key){
		    case "Timeout": 
			int Timeout = Integer.parseInt(payload);
			if (Timeout < 0){
			    throw new NumberFormatException(" : specified negative integer for timeout = " +  payload);
			}
			JsonPayload.put(key, payload);
			break;

		    case "Version": 
			JsonPayload.put(key, payload);
			break;

		    case "LocalParameters":  
			paramsJson = new JSONObject(payload);
			JsonPayload.put(key, paramsJson);
			break;
			
		    case "EnvParameters":  
			paramsJson = new JSONObject(payload);
			JsonPayload.put(key, paramsJson);
			break;
			
		    case "NodeList":  
			JSONArray paramsArray = new JSONArray(payload);
			JsonPayload.put(key, paramsArray);
			break;
			
		    case "FileParameters":
			// Files may have strings with newlines. Escape them as appropriate
			String formattedPayload = payload.replace("\n", "\\n").replace("\r", "\\r");
			JSONObject fileParams = new JSONObject(formattedPayload);
			JsonPayload.put(key, fileParams);
			break;
			
		    }
		}
	    }
	}
	

	// Generate a unique uuid for the test
	String ReqId = UUID.randomUUID().toString();
	JsonPayload.put("Id", ReqId);

	return JsonPayload;
	
    }



    // method that validates that the Map has  enough information
    // to query Ansible server for a result . If so, it
    // returns the appropriate url, else an empty string
    public String ReqUri_Result(Map <String, String> params) throws APPCException, NumberFormatException{
	
	// Mandatory  parameters, that must be in the request
	String[] mandatoryTestParams = {"AgentUrl", "Id", "User", "Password" };
	
	// Verify all the mandatory parameters are there
	String payload = "";
	String Uri = "";
	
	for (String key: mandatoryTestParams){
	    if (! params.containsKey(key)){
		throw new APPCException(String.format("Ansible: Mandatory AnsibleAdapter key %s not found in parameters provided by calling agent !", key));		    
	    }

	    payload = params.get(key);
	    if (Strings.isNullOrEmpty(payload)){
		throw new APPCException(String.format("Ansible: Mandatory AnsibleAdapter key %s not found in parameters provided by calling agent !", key));		    
	    }

	}

	Uri = params.get("AgentUrl") + "?Id=" + params.get("Id") + "&Type=GetResult";

	return Uri;
      
    }



    // method that validates that the Map has  enough information
    // to query Ansible server for logs. If so, it populates the appropriate
    // returns the appropriate url, else an empty string
    public String ReqUri_Output(Map <String, String> params) throws APPCException, NumberFormatException{
	
	
	// Mandatory  parameters, that must be in the request
	String[] mandatoryTestParams = {"AgentUrl", "Id", "User", "Password" };
	
	// Verify all the mandatory parameters are there
	String payload = "";
	String Uri = "";
	
	for (String key: mandatoryTestParams){
	    if (! params.containsKey(key)){
		throw new APPCException(String.format("Ansible: Mandatory AnsibleAdapter key %s not found in parameters provided by calling agent !", key));		    
	    }
	    payload = params.get(key);
	    if (Strings.isNullOrEmpty(payload)){
		throw new APPCException(String.format("Ansible: Mandatory AnsibleAdapter key %s not found in parameters provided by calling agent !", key));		    
	    }

	}

	Uri = params.get("AgentUrl") + "?Id=" + params.get("Id") + "&Type=GetOutput";
	return Uri;
      
    }

    // method that validates that the Map has  enough information
    // to query Ansible server for logs. If so, it populates the appropriate
    // returns the appropriate url, else an empty string
    public String ReqUri_Log(Map <String, String> params) throws APPCException, NumberFormatException{
	
	
	// Mandatory  parameters, that must be in the request
	String[] mandatoryTestParams = {"AgentUrl", "Id", "User", "Password" };
	
	// Verify all the mandatory parameters are there
	String payload = "";
	String Uri = "";
	
	for (String key: mandatoryTestParams){
	    if (! params.containsKey(key)){
		throw new APPCException(String.format("Ansible: Mandatory AnsibleAdapter key %s not found in parameters provided by calling agent !", key));		    
	    }
	    payload = params.get(key);
	    if (Strings.isNullOrEmpty(payload)){
		throw new APPCException(String.format("Ansible: Mandatory AnsibleAdapter key %s not found in parameters provided by calling agent !", key));		    
	    }

	}

	Uri = params.get("AgentUrl") + "?Id=" + params.get("Id") + "&Type=GetLog";
	return Uri;
      
    }

   
    /** 
	This method parses response from the 
	Ansible Server when we do a post 
	and returns an AnsibleResult object
    **/
    
    public AnsibleResult  parsePostResponse(String Input) throws APPCException{

	AnsibleResult ansibleResult = new AnsibleResult();
	
	try{
	    //Jsonify it
	    JSONObject  postResponse = new JSONObject(Input);
		
	    // Mandatory keys required are StatusCode and StatusMessage
	    int Code = postResponse.getInt("StatusCode");
	    String Message = postResponse.getString("StatusMessage");

	    
	    // Status code must must be either 100 (accepted) or 101 (rejected)
	    boolean valCode = AnsibleResultCodes.CODE.checkValidCode(AnsibleResultCodes.INITRESPONSE.getValue(), Code);
	    if(!valCode){
		throw new APPCException("Invalid InitResponse code  = " + Code + " received. MUST be one of " + AnsibleResultCodes.CODE.getValidCodes(AnsibleResultCodes.INITRESPONSE.getValue()) );
	    }
	    
	    ansibleResult.setStatusCode(Code);
	    ansibleResult.setStatusMessage(Message);

	}
	catch(JSONException e){
	    ansibleResult = new AnsibleResult(600, "Error parsing response = " + Input + ". Error = " + e.getMessage(), "");
	}

	
	return ansibleResult;
    }


    /** This method  parses response from an Ansible server when we do a GET for a result 
	and returns an AnsibleResult object
     **/
    public AnsibleResult  parseGetResponse(String Input) throws APPCException {

	AnsibleResult ansibleResult = new AnsibleResult();
	int FinalCode = AnsibleResultCodes.FINAL_SUCCESS.getValue();


	try{
	    
	    //Jsonify it
	    JSONObject  postResponse = new JSONObject(Input);
	    
	    // Mandatory keys required are Status and Message
	    int Code = postResponse.getInt("StatusCode");
	    String Message = postResponse.getString("StatusMessage");
	    
	    // Status code must be valid
	    // Status code must must be either 100 (accepted) or 101 (rejected)
	    boolean valCode = AnsibleResultCodes.CODE.checkValidCode(AnsibleResultCodes.FINALRESPONSE.getValue(), Code);
	    
	    if(!valCode){
		throw new APPCException("Invalid FinalResponse code  = " + Code + " received. MUST be one of " + AnsibleResultCodes.CODE.getValidCodes(AnsibleResultCodes.FINALRESPONSE.getValue()));
	    }

	    
	    ansibleResult.setStatusCode(Code);
	    ansibleResult.setStatusMessage(Message);
	    System.out.println("Received response with code = " + Integer.toString(Code) + " Message = " + Message);

	    if(! postResponse.isNull("Results")){

		// Results are available. process them 
		// Results is a dictionary of the form
		// {host :{status:s, group:g, message:m, hostname:h}, ...}
		System.out.println("Processing results in response");
		JSONObject results = postResponse.getJSONObject("Results");
		System.out.println("Get JSON dictionary from Results ..");
		Iterator<String> hosts = results.keys();
		System.out.println("Iterating through hosts");
		
		while(hosts.hasNext()){
		    String host = hosts.next();
		    System.out.println("Processing host = " + host);
		    
		    try{
			JSONObject host_response = results.getJSONObject(host);
			int subCode = host_response.getInt("StatusCode");
			String message = host_response.getString("StatusMessage");

			System.out.println("Code = " + Integer.toString(subCode) + " Message = " + message);
			
			if(subCode != 200 || ! message.equals("SUCCESS")){
			    FinalCode = AnsibleResultCodes.REQ_FAILURE.getValue();
			}
		    }
		    catch(JSONException e){
			ansibleResult.setStatusCode(AnsibleResultCodes.INVALID_RESPONSE.getValue());
			ansibleResult.setStatusMessage(String.format("Error processing response message = %s from host %s", results.getString(host), host));
			break;
		    }
		}

		ansibleResult.setStatusCode(FinalCode);

		// We return entire Results object as message
		ansibleResult.setResults(results.toString());

	    }
	    else{
		ansibleResult.setStatusCode(AnsibleResultCodes.INVALID_RESPONSE.getValue());
		ansibleResult.setStatusMessage("Results not found in GET for response");
	    }
	    
	    
	}
	catch(JSONException e){
	    ansibleResult = new AnsibleResult(AnsibleResultCodes.INVALID_PAYLOAD.getValue(), "Error parsing response = " + Input + ". Error = " + e.getMessage(), "");
	}


	return ansibleResult;
    }



};    
	    
	    

