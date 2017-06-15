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




/* Class to emulate responses  from the Ansible Server that is compliant with the APP-C Ansible Server
   Interface. Used for jUnit tests to verify code is working. In tests it can be used
   as a replacement for methods from ConnectionBuilder class
*/

package org.openecomp.appc.adapter.ansible.model;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import com.google.common.base.Strings;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.adapter.ansible.model.AnsibleResult;

public class AnsibleServerEmulator {

    
    private String playbookName = "test_playbook.yaml";
    private String TestId;

    /**
     * Method that emulates the response from an Ansible Server
     when presented with a request to execute a playbook 
     Returns an ansible object result. The response code is always the http code 200 (i.e connection successful)
     payload is json string as would be sent back by Ansible Server
    **/
    
    public AnsibleResult Post(String AgentUrl, String payload){
	AnsibleResult result = new AnsibleResult() ;
	
	try{
	    // Request must be a JSON object
	    
	    JSONObject message = new JSONObject(payload);
	    if (message.isNull("Id")){
		RejectRequest(result, "Must provide a valid Id");
	    }
	    else if(message.isNull("PlaybookName")){
		RejectRequest(result, "Must provide a playbook Name");
	    }
	    else if(!message.getString("PlaybookName").equals(playbookName)){
		RejectRequest(result, "Playbook " + message.getString("PlaybookName") + "  not found in catalog");
	    }
	    else{
		AcceptRequest(result);
	    }
	}
	catch (JSONException e){
	    RejectRequest(result, e.getMessage());
	}

	return result;
    }


    /** Method to emulate response from an Ansible
	Server when presented with a GET request
	Returns an ansibl object result. The response code is always the http code 200 (i.e connection successful)
	payload is json string as would be sent back by Ansible Server

    **/
    public AnsibleResult Get(String AgentUrl){

	// Extract id
	Pattern pattern = Pattern.compile(".*?\\?Id=(.*?)&Type.*");
	Matcher matcher = pattern.matcher(AgentUrl);
	String Id = "";
	
	if (matcher.find()){
	    Id = matcher.group(1);
	}

	AnsibleResult get_result = new AnsibleResult();

	JSONObject response = new JSONObject();
	response.put("StatusCode", 200);
	response.put("StatusMessage", "FINISHED");

	JSONObject results = new JSONObject();

	JSONObject vm_results = new JSONObject();
	vm_results.put("StatusCode", 200);
	vm_results.put("StatusMessage", "SUCCESS");
	vm_results.put("Id", Id);
	results.put("192.168.1.10", vm_results);
	

	response.put("Results", results);

	get_result.setStatusCode(200);
	get_result.setStatusMessage(response.toString());

	return get_result;
	
    }

    
    private void RejectRequest(AnsibleResult result, String Message){
	result.setStatusCode(200);
	JSONObject response = new JSONObject();
	response.put("StatusCode", AnsibleResultCodes.REJECTED.getValue());
	response.put("StatusMessage", Message);
	result.setStatusMessage(response.toString());
	
    }

    private void AcceptRequest(AnsibleResult result){
	result.setStatusCode(200);
	JSONObject response = new JSONObject();
	response.put("StatusCode", AnsibleResultCodes.PENDING.getValue());
	response.put("StatusMessage", "PENDING");
	result.setStatusMessage(response.toString());

    }

};
    
