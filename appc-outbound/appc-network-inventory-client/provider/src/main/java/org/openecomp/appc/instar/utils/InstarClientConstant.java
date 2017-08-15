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

package org.openecomp.appc.instar.utils;

public class InstarClientConstant
{
	public static String INPUT_PARAM_RESPONSE_PRIFIX = "responsePrefix";
	public static String OUTPUT_PARAM_STATUS = "status";
	public static String OUTPUT_PARAM_ERROR_MESSAGE = "error-message";
	public static String OUTPUT_STATUS_SUCCESS = "success";
	public static String OUTPUT_STATUS_FAILURE = "failure";



	public static final String INSTAR_KEYS = "instarKeys";

	public static final String INTERFACE_IP_ADDRESS = "interface-ip-address";
	public static final String SOURCE_SYSTEM_INSTAR = "INSTAR";
	public static final String VNF_TYPE = "vnf-type";
	public static final String ADDRESSFDQN = "addressfqdn";
	public static final String VNF_NAME = "vnf-name";
	public static final String INSTAR_KEY_VALUES = "INSTAR-KEY-VALUES";
	public static final String INSTAR_RESPONSE_BLOCK_NAME = "vnfConfigurationParameterDetails";
	public static final String FDQN = "fqdn";


	public static final String MOCK_INSTAR="mock_instar";
	public static final String AFT_LATITUDE="aft_latitude";
	public static final String AFT_LONGITUDE="aft_latitude";
	public static final String	AFT_ENVIRONMENT="aft_environment";
	public static final String	SCLD_PLATFORM="scld_platform";
	public static final String	AUTHORIZATION="authorization";
	public static final String	DME2_CLIENT_TIMEOUTMS="dme2_client_timeoutms";
	public static final String	DME2_CLIENT_SENDANDWAIT="dme2_client_sendandwait";
	public static final String	BASE_URL="_base_url";
	public static final String HTTP_HEADERS="_http_headers";
	
	public static final String V6_ADDRESS="ipaddress-v6";
	public static final String INSTAR_V6_ADDRESS="v6IPAddress";
	
	
	public static final String V4_ADDRESS="ipaddress-v4";	
	public static final String INSTAR_V4_ADDRESS="v4IPAddress";

	public static final String	SUB_CONTEXT="_sub_context";
	public static final String	URL_SUFFIX="_suffix";
	public static final String	VERSION="_version";
	public static final String	ENV_CONTEXT="_env_context";
	public static final String ROUTEOFFER="_routeoffer";
	public static final String APPC_PROPERTIES = "appc.properties";
	public static final String METHOD="_method";
	public static final String OPERATION_GET_IPADDRESS_BY_VNF_NAME = "getIpAddressByVnf";
	
	public static final String OUTBOUND_PROPERTIES= "/outbound.properties";

	public static String CONTENT_TYPE = "application/json";
	public static String RETURNED_RESPONSE_TYPE = "application/json";

}

