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

package org.openecomp.sdnc.config.audit.node;

public class CompareConstants {


    public static final String FORMAT_JSON = "RESTCONF";
    public static final String FORMAT_XML = "XML";
    public static final String FORMAT_CLI = "CLI";
    public static final String NETCONF_XML = "NETCONF-XML";
    public static final String RESTCONF_XML = "RESTCONF-XML";
    

    public static final String STATUS_FAILURE = "FAILURE";
    public static final String RESPONSE_STATUS = "STATUS";
    public static final String STATUS_SUCCESS = "SUCCESS";
    
    public static final String ERROR_CODE = "Error-code";
    
    public static final String ERROR_MESSAGE = "Error-Message";
    public static final String ERROR_MESSAGE_DEATIL = "Compare Node Failed-Internal Error.See karaf log file";
    
    public static final String NO_MATCH_MESSAGE = "The configurations do not match";
}
