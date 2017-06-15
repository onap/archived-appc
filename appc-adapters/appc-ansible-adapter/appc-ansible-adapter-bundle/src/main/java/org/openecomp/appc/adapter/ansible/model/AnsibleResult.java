/*-
 * ============LICENSE_START=======================================================
 * ONAP : APP-C
 * ================================================================================
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
 */

package org.openecomp.appc.adapter.ansible.model;


/* Simple class to store code and message returned by POST/GET to an Ansible Server */
public class AnsibleResult{
    private int StatusCode;
    private String StatusMessage;
    private String Results;
    

    public    AnsibleResult(){
	StatusCode = -1;
	StatusMessage = "UNKNOWN";
	Results = "UNKNOWN";

    }

    // constructor
    public AnsibleResult(int code, String message, String result){
	StatusCode = code;
	StatusMessage = message;
	Results = result;
    }

    //*************************************************
    // Various set methods
    public void setStatusCode(int code){
	this.StatusCode = code;
    }

    public void setStatusMessage(String message){
	this.StatusMessage = message;
    }

    public void setResults(String results){
	this.Results = results;
    }
    

    void set(int code, String message, String results){
	this.StatusCode = code;
	this.StatusMessage = message;
	this.Results = results;

    }

    //*********************************************
    // Various get methods
    public int getStatusCode(){
	return this.StatusCode;
    }

    public String getStatusMessage(){
	return this.StatusMessage;
    }

    public String getResults(){
	return this.Results;
    }


}
    
