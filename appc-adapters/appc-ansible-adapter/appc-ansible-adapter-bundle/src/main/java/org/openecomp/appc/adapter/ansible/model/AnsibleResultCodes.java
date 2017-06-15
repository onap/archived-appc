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


import java.util.*;

/**
 * enum of the various codes that APP-C uses to resolve different
 *  status of response from Ansible Server
 **/

public enum AnsibleResultCodes{

    SUCCESS(400),
    KEYSTORE_EXCEPTION(622),
    CERTIFICATE_ERROR(610),
    IO_EXCEPTION (611),
    HOST_UNKNOWN(625),
    USER_UNAUTHORIZED(613),
    UNKNOWN_EXCEPTION(699),
    SSL_EXCEPTION(697),
    INVALID_PAYLOAD(698),
    INVALID_RESPONSE(601),
    PENDING(100),
    REJECTED(101),
    FINAL_SUCCESS(200),
    REQ_FAILURE(401),
    MESSAGE(1),
    CODE(0),
    INITRESPONSE(0),
    FINALRESPONSE(1); 
    
    private final Set<Integer> InitCodes = new HashSet<Integer>(Arrays.asList(100, 101));
    private final Set<Integer> FinalCodes = new HashSet<Integer>(Arrays.asList(200, 500));
    private final ArrayList<Set<Integer>>CodeSets = new ArrayList<Set<Integer>>(Arrays.asList(InitCodes, FinalCodes));
    
    private  final Set<String> MessageSet = new HashSet<String>(Arrays.asList("PENDING", "FINISHED", "TERMINATED"));

    private final int value;
    
    AnsibleResultCodes(int value){
	this.value = value;
    };


    public int getValue(){
	return this.value;
    }


    public boolean checkValidCode(int Type, int Code){
	Set<Integer>CodeSet = CodeSets.get(Type);
	if (CodeSet.contains(Code)){
	    return true;
	}
	else{
	    return false;
	}
    }


    public String getValidCodes(int Type){
	Set<Integer>CodeSet = CodeSets.get(Type);
	
	Iterator iter = CodeSet.iterator();
	String ValidCodes = "[ ";
	while(iter.hasNext()){
	    ValidCodes = ValidCodes +  iter.next().toString() + ",";
	}

	ValidCodes = ValidCodes + "]";
	return ValidCodes;
    }


    public boolean checkValidMessage(String Message){
	if (MessageSet.contains(Message)){
	    return true;
	}
	else{
	    return false;
	}
    }



    public String getValidMessages(){
	Iterator iter = MessageSet.iterator();
	String ValidMessage = "[ ";
	while(iter.hasNext()){
	    ValidMessage = ValidMessage +  iter.next() + ",";
	}

	ValidMessage = ValidMessage + "]";
	return ValidMessage;
    }


};
