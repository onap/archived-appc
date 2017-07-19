/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.appc.adapter.chef.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.*;


import org.openecomp.appc.Constants;
import org.openecomp.appc.adapter.chef.ChefAdapter;
import org.openecomp.appc.adapter.chef.chefapi.*;
import org.openecomp.appc.adapter.chef.chefclient.*;
import org.openecomp.appc.configuration.Configuration;
import org.openecomp.appc.configuration.ConfigurationFactory;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.exceptions.UnknownProviderException;
import org.openecomp.appc.i18n.Msg;
import org.openecomp.appc.pool.Pool;
import org.openecomp.appc.pool.PoolExtensionException;
import org.openecomp.appc.util.StructuredPropertyHelper;
import org.openecomp.appc.util.StructuredPropertyHelper.Node;
import com.att.cdp.exceptions.ContextConnectionException;
import com.att.cdp.exceptions.ResourceNotFoundException;
import com.att.cdp.exceptions.TimeoutException;
import com.att.cdp.exceptions.ZoneException;
import com.att.cdp.pal.util.StringHelper;
import com.att.cdp.zones.ComputeService;
import com.att.cdp.zones.Context;
import com.att.cdp.zones.ImageService;
import com.att.cdp.zones.Provider;
import com.att.cdp.zones.model.Image;
import com.att.cdp.zones.model.Server;
import com.att.cdp.zones.model.ServerBootSource;
import com.att.cdp.zones.model.Server.Status;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import org.openecomp.sdnc.sli.SvcLogicContext;
import org.slf4j.MDC;

import java.net.InetAddress;
import java.util.Locale;
import java.util.UUID;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.util.EntityUtils;

import static com.att.eelf.configuration.Configuration.*;

import java.io.IOException;

import java.net.InetAddress;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
//chef
import org.openecomp.appc.adapter.chef.chefapi.*;
import org.openecomp.appc.adapter.chef.chefclient.*;

//json
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
/**
 * This class implements the {@link ChefAdapter} interface. This interface
 * defines the behaviors that our service provides.
 */
public class ChefAdapterImpl implements ChefAdapter {

	// chef server Initialize variable
	public String username = "";
	public String clientPrivatekey = "";
	public String chefserver = "";
	public String serverAddress = "";
	public String organizations = "";
	@SuppressWarnings("nls")
	public static final String MDC_ADAPTER = "adapter";

	@SuppressWarnings("nls")
	public static final String MDC_SERVICE = "service";

	@SuppressWarnings("nls")
	public static final String OUTCOME_FAILURE = "failure";

	@SuppressWarnings("nls")
	public static final String OUTCOME_SUCCESS = "success";

	@SuppressWarnings("nls")
	public static final String PROPERTY_PROVIDER = "provider";

	@SuppressWarnings("nls")
	public static final String PROPERTY_PROVIDER_IDENTITY = "identity";

	@SuppressWarnings("nls")
	public static final String PROPERTY_PROVIDER_NAME = "name";

	@SuppressWarnings("nls")
	public static final String PROPERTY_PROVIDER_TENANT = "tenant";

	@SuppressWarnings("nls")
	public static final String PROPERTY_PROVIDER_TENANT_NAME = "name";

	@SuppressWarnings("nls")
	public static final String PROPERTY_PROVIDER_TENANT_PASSWORD = "password"; // NOSONAR

	@SuppressWarnings("nls")
	public static final String PROPERTY_PROVIDER_TENANT_USERID = "userid";

	@SuppressWarnings("nls")
	public static final String PROPERTY_PROVIDER_TYPE = "type";


	private static final EELFLogger logger = EELFManager.getInstance().getLogger(ChefAdapterImpl.class);

	private static final char LPAREN = '(';

	private static final char NL = '\n';

	private static final char QUOTE = '\'';

	private static final char RPAREN = ')';

	private static final char SPACE = ' ';

	public ChefAdapterImpl() {
		initialize();

	}

	public ChefAdapterImpl(boolean initialize) {

		if (initialize) {
			initialize();

		}
	}

	public ChefAdapterImpl(Properties props) {
		initialize();

	}

	public ChefAdapterImpl(String key) {
		initialize();

	}


	@Override
	public String getAdapterName() {
		return "chef adapter";
	}


	@SuppressWarnings("nls")
	@Override
	public void VnfcEnvironment(Map<String, String> params, SvcLogicContext ctx) {
		logger.info("environment of VNF-C");
		chefInfo(params);
		RequestContext rc = new RequestContext(ctx);
		rc.isAlive();
		String env = params.get("Environment");
		if(env.equals("")){
			chefServerResult(rc, "200", "Skip Environment block ");}
		else{
		JSONObject env_J = new JSONObject(env);
		String envName = env_J.getString("name");
		int code;
		String message = null;
		if (privateKeyCheck()) {
			// update the details of an environment on the Chef server.
			ChefApiClient cac = new ChefApiClient(username, clientPrivatekey, chefserver, organizations);
			ApiMethod am = cac.put("/environments/"+envName).body(env);
			am.execute();
			code = am.getReturnCode();
			message = am.getResponseBodyAsString();
				if(code == 404 ){
					//need create a new environment
					am = cac.post("/environments").body(env);
					am.execute();
					code = am.getReturnCode();
					message = am.getResponseBodyAsString();
				}
		
		} else {
			code = 500;
			message = "Cannot find the private key in the APPC file system, please load the private key to "
					+ clientPrivatekey;
		}
		chefServerResult(rc, Integer.toString(code), message);
		}
	}	
	
	
	@SuppressWarnings("nls")
	@Override
	public void VnfcNodeobjects(Map<String, String> params, SvcLogicContext ctx) {
		logger.info("update the nodeObjects of VNF-C");
		chefInfo(params);
		String nodeList_S = params.get("NodeList");
		String node_S = params.get("Node");
		nodeList_S = nodeList_S.replace("[","");
		nodeList_S = nodeList_S.replace("]","");
		nodeList_S = nodeList_S.replace("\"","");
		nodeList_S = nodeList_S.replace(" ","");
		List<String> nodes = Arrays.asList(nodeList_S.split("\\s*,\\s*"));
		RequestContext rc = new RequestContext(ctx);
		rc.isAlive();
		int code=200;
		String message = null;
		if (privateKeyCheck()) {
			ChefApiClient cac = new ChefApiClient(username, clientPrivatekey, chefserver, organizations);

			for(int i = 0; i < nodes.size(); i++){
				String nodeName=nodes.get(i);
				JSONObject node_J = new JSONObject(node_S);
				node_J.remove("name");
				node_J.put("name",nodeName);
				String nodeObject=node_J.toString();
				logger.info(nodeObject);
				ApiMethod am = cac.put("/nodes/"+nodeName).body(nodeObject);
				am.execute();
				code = am.getReturnCode();
				message = am.getResponseBodyAsString();
				if(code != 200){
					break;
				}
			
			}
		}else{
			code = 500;
			message = "Cannot find the private key in the APPC file system, please load the private key to "
					+ clientPrivatekey;
			}
		chefServerResult(rc, Integer.toString(code), message);
	}
	

	@SuppressWarnings("nls")
	@Override
	public void VnfcPushJob(Map<String, String> params, SvcLogicContext ctx) {
		chefInfo(params);
		String nodeList = params.get("NodeList");
		String isCallback = params.get("CallbackCapable");
		String chefAction = "/pushy/jobs";
		//need work on this
		String pushRequest="";
		if(isCallback.equals("true")){
			String requestId = params.get("RequestId");
			String callbackUrl = params.get("CallbackUrl");
			pushRequest="{"+
					  "\"command\": \"chef-client\","+
					  "\"run_timeout\": 300,"+
					  "\"nodes\":" +nodeList +","+
					  "\"env\": {\"RequestId\": \""+ requestId +"\", \"CallbackUrl\": \""+ callbackUrl +"\"},"+
					  "\"capture_output\": true"+
					"}";
		}else{
			pushRequest="{"+
				  "\"command\": \"chef-client\","+
				  "\"run_timeout\": 300,"+
				  "\"nodes\":" +nodeList +","+
				  "\"env\": {},"+
				  "\"capture_output\": true"+
				"}";
		}
		RequestContext rc = new RequestContext(ctx);

		rc.isAlive();
		SvcLogicContext svcLogic = rc.getSvcLogicContext();
		ChefApiClient cac = new ChefApiClient(username, clientPrivatekey, chefserver, organizations);
		ApiMethod am = cac.post(chefAction).body(pushRequest);
		
		am.execute();
		int code = am.getReturnCode();
		String message = am.getResponseBodyAsString();
		if (code == 201) {
			int startIndex = message.indexOf("jobs") + 5;
			int endIndex = message.length() - 2;
			String jobID = message.substring(startIndex, endIndex);
			svcLogic.setAttribute("jobID", jobID);
			logger.info(jobID);
		}
		chefServerResult(rc, Integer.toString(code), message);
	}
	
	
	@SuppressWarnings("nls")
	@Override
	public void fetchResults (Map<String, String> params, SvcLogicContext ctx) {
		chefInfo(params);
		String nodeList_S = params.get("NodeList");
		nodeList_S = nodeList_S.replace("[","");
		nodeList_S = nodeList_S.replace("]","");
		nodeList_S = nodeList_S.replace("\"","");
		nodeList_S = nodeList_S.replace(" ","");
		List<String> nodes = Arrays.asList(nodeList_S.split("\\s*,\\s*"));
		JSONObject Result = new JSONObject();
		String returnCode= "200";
		String returnMessage="";
		for (int i = 0; i < nodes.size(); i++){
			String node=nodes.get(i);
			String chefAction="/nodes/"+node;
			int code;
			String message = null;
			if (privateKeyCheck()) {
				ChefApiClient cac = new ChefApiClient(username, clientPrivatekey, chefserver, organizations);
				ApiMethod am = cac.get(chefAction);
				am.execute();
				code = am.getReturnCode();
				message = am.getResponseBodyAsString();
			} else {
				code = 500;
				message = "Cannot find the private key in the APPC file system, please load the private key to "
						+ clientPrivatekey;
			}
			if (code==200){
				JSONObject nodeResult = new JSONObject();
				JSONObject allNodeData = new JSONObject(message);
					String attribute= "PushJobOutput";
					String resultData;
					allNodeData=allNodeData.getJSONObject("normal");
					try {
						resultData = allNodeData.getString(attribute);
					} catch (Exception exc1) {
						try {
							resultData = allNodeData.getJSONObject(attribute).toString();
						} catch (Exception exc2) {
							try {
								resultData = allNodeData.getJSONArray(attribute).toString();
							}catch (Exception exc3){
								returnCode = "500";							
								returnMessage="cannot find "+attribute;
								break;
							}
						}
					}
					nodeResult.put(attribute,resultData);
				
				Result.put(node,nodeResult);
			}else{
				returnCode="500";
				returnMessage = message+" Cannot access: "+ node;
				break;
			}
			
		}
		RequestContext rc = new RequestContext(ctx);
		rc.isAlive();
		if (!returnCode.equals("500")){
			returnMessage=Result.toString();
			returnCode="200";
		}
		chefServerResult(rc, returnCode, returnMessage);
	}


	@SuppressWarnings("nls")
	@Override
	public void nodeObejctBuilder(Map<String, String> params, SvcLogicContext ctx) {
		logger.info("nodeObejctBuilder");
		String name = params.get("nodeobject.name");
		String normal = params.get("nodeobject.normal");
		String overrides = params.get("nodeobject.overrides");
		String defaults = params.get("nodeobject.defaults");
		String run_list = params.get("nodeobject.run_list");
		String chef_environment = params.get("nodeobject.chef_environment");
		String nodeObject = "{\"json_class\":\"Chef::Node\",\"default\":{" + defaults
				+ "},\"chef_type\":\"node\",\"run_list\":[" + run_list + "],\"override\":{" + overrides
				+ "},\"normal\": {" + normal + "},\"automatic\":{},\"name\":\"" + name + "\",\"chef_environment\":\""
				+ chef_environment + "\"}";
		logger.info(nodeObject);

		RequestContext rc = new RequestContext(ctx);
		rc.isAlive();
		SvcLogicContext svcLogic = rc.getSvcLogicContext();
		svcLogic.setAttribute("chef.nodeObject", nodeObject);

	}


	public void chefInfo(Map<String, String> params) {
		username = params.get("username");
		serverAddress = params.get("serverAddress");
		organizations = params.get("organizations");
		chefserver = "https://" + serverAddress + "/organizations/" + organizations;
		clientPrivatekey = "/opt/app/bvc/chef/" + serverAddress + "/" + organizations + "/" + username + ".pem";
	}

	public Boolean privateKeyCheck() {
		File f = new File(clientPrivatekey);
		if (f.exists()) {
			return true;
		} else {
			return false;
		}
	}

	@SuppressWarnings("nls")
	@Override
	public void retrieveData(Map<String, String> params, SvcLogicContext ctx) {
		String contextData = "someValue";
		String allConfigData = params.get("allConfig");
		String key = params.get("key");
		String dgContext = params.get("dgContext");
		JSONObject josnConfig = new JSONObject(allConfigData);
		try {
			contextData = josnConfig.getString(key);
		} catch (Exception ex) {
			try {
				contextData = josnConfig.getJSONObject(key).toString();
			} catch (Exception exc) {
				contextData = josnConfig.getJSONArray(key).toString();
			}
		}

		RequestContext rc = new RequestContext(ctx);
		rc.isAlive();
		SvcLogicContext svcLogic = rc.getSvcLogicContext();
		svcLogic.setAttribute(dgContext, contextData);
	}

	@SuppressWarnings("nls")
	@Override
	public void combineStrings(Map<String, String> params, SvcLogicContext ctx) {

		String String1 = params.get("String1");
		String String2 = params.get("String2");
		String dgContext = params.get("dgContext");
		String contextData = String1 + String2;
		RequestContext rc = new RequestContext(ctx);
		rc.isAlive();
		SvcLogicContext svcLogic = rc.getSvcLogicContext();
		svcLogic.setAttribute(dgContext, contextData);
	}

	@SuppressWarnings("nls")
	@Override
	public void chefGet(Map<String, String> params, SvcLogicContext ctx) {
		logger.info("chef get method");
		chefInfo(params);
		String chefAction = params.get("chefAction");
		RequestContext rc = new RequestContext(ctx);
		rc.isAlive();
		int code;
		String message = null;
		if (privateKeyCheck()) {
			ChefApiClient cac = new ChefApiClient(username, clientPrivatekey, chefserver, organizations);
			ApiMethod am = cac.get(chefAction);
			am.execute();
			code = am.getReturnCode();
			message = am.getResponseBodyAsString();
		} else {
			code = 500;
			message = "Cannot find the private key in the APPC file system, please load the private key to "
					+ clientPrivatekey;
		}
		chefServerResult(rc, Integer.toString(code), message);

	}

	/**
	 *   send put request to chef server
	 */

	@SuppressWarnings("nls")
	@Override
	public void chefPut(Map<String, String> params, SvcLogicContext ctx) {
		chefInfo(params);
		String chefAction = params.get("chefAction");
		String CHEF_NODE_STR = params.get("chefRequestBody");
		RequestContext rc = new RequestContext(ctx);
		rc.isAlive();
		int code;
		String message = null;
		if (privateKeyCheck()) {
			ChefApiClient cac = new ChefApiClient(username, clientPrivatekey, chefserver, organizations);

			ApiMethod am = cac.put(chefAction).body(CHEF_NODE_STR);
			am.execute();
			code = am.getReturnCode();
			message = am.getResponseBodyAsString();
		} else {
			code = 500;
			message = "Cannot find the private key in the APPC file system, please load the private key to "
					+ clientPrivatekey;
		}
		logger.info(code + "   " + message);
		chefServerResult(rc, Integer.toString(code), message);
	}

	/**
	 *   send Post request to chef server
	 */

	@SuppressWarnings("nls")
	@Override
	public void chefPost(Map<String, String> params, SvcLogicContext ctx) {
		chefInfo(params);
		logger.info("chef Post method");
		logger.info(username + " " + clientPrivatekey + " " + chefserver + " " + organizations);
		String CHEF_NODE_STR = params.get("chefRequestBody");
		String chefAction = params.get("chefAction");
		RequestContext rc = new RequestContext(ctx);
		rc.isAlive();
		int code;
		String message = null;
		if (privateKeyCheck()) {
			ChefApiClient cac = new ChefApiClient(username, clientPrivatekey, chefserver, organizations);
			ApiMethod am = cac.post(chefAction).body(CHEF_NODE_STR);
			am.execute();
			code = am.getReturnCode();
			message = am.getResponseBodyAsString();
		} else {
			code = 500;
			message = "Cannot find the private key in the APPC file system, please load the private key to "
					+ clientPrivatekey;
		}
		logger.info(code + "   " + message);
		chefServerResult(rc, Integer.toString(code), message);
	}


	@SuppressWarnings("nls")
	@Override
	public void chefDelete(Map<String, String> params, SvcLogicContext ctx) {
		logger.info("chef delete method");
		chefInfo(params);
		String chefAction = params.get("chefAction");
		RequestContext rc = new RequestContext(ctx);
		rc.isAlive();
		int code;
		String message = null;
		if (privateKeyCheck()) {
			ChefApiClient cac = new ChefApiClient(username, clientPrivatekey, chefserver, organizations);
			ApiMethod am = cac.delete(chefAction);
			am.execute();
			code = am.getReturnCode();
			message = am.getResponseBodyAsString();
		} else {
			code = 500;
			message = "Cannot find the private key in the APPC file system, please load the private key to "
					+ clientPrivatekey;
		}
		logger.info(code + "   " + message);
		chefServerResult(rc, Integer.toString(code), message);
	}


	@SuppressWarnings("nls")
	@Override
	public void trigger(Map<String, String> params, SvcLogicContext ctx) {
		logger.info("Run trigger method");
		String tVmIp = params.get("ip");
		// String tUrl = "http://" + tVmIp;
		RequestContext rc = new RequestContext(ctx);
		rc.isAlive();

		try {
			HttpGet httpGet = new HttpGet(tVmIp);
			HttpClient httpClient = HttpClients.createDefault();
			HttpResponse response = null;
			response = httpClient.execute(httpGet);
			int responseCode = response.getStatusLine().getStatusCode();
			HttpEntity entity = response.getEntity();
			String responseOutput = EntityUtils.toString(entity);
			chefClientResult(rc, Integer.toString(responseCode), responseOutput);
			doSuccess(rc);
		} catch (Exception ex) {
			doFailure(rc, 500, ex.toString());
		}
	}

	@SuppressWarnings("nls")
	@Override
	public void checkPushJob(Map<String, String> params, SvcLogicContext ctx) {
		chefInfo(params);
		String jobID = params.get("jobid");
		int retryTimes = Integer.parseInt(params.get("retryTimes"));
		int retryInterval = Integer.parseInt(params.get("retryInterval"));
		String chefAction = "/pushy/jobs/" + jobID;

		RequestContext rc = new RequestContext(ctx);
		rc.isAlive();
		SvcLogicContext svcLogic = rc.getSvcLogicContext();
		String message = "";
		String status = "";
		for (int i = 0; i < retryTimes; i++) {
			try {
				Thread.sleep(retryInterval); // 1000 milliseconds is one second.
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
			ChefApiClient cac = new ChefApiClient(username, clientPrivatekey, chefserver, organizations);
			ApiMethod am = cac.get(chefAction);
			am.execute();
			int code = am.getReturnCode();
			message = am.getResponseBodyAsString();
			JSONObject obj = new JSONObject(message);
			status = obj.getString("status");
			if (!status.equals("running")) {
				logger.info(i + " time " + code + "   " + status);
				break;
			}

		}
		if (status.equals("complete")) {
			svcLogic.setAttribute("chefServerResult.code", "200");
			svcLogic.setAttribute("chefServerResult.message", message);
		} else {
			if (status.equals("running")) {
				svcLogic.setAttribute("chefServerResult.code", "202");
				svcLogic.setAttribute("chefServerResult.message", "chef client runtime out");
			} else {
				svcLogic.setAttribute("chefServerResult.code", "500");
				svcLogic.setAttribute("chefServerResult.message", message);
			}
		}
	}


	@SuppressWarnings("nls")
	@Override
	public void pushJob(Map<String, String> params, SvcLogicContext ctx) {
		chefInfo(params);
		String pushRequest = params.get("pushRequest");
		String chefAction = "/pushy/jobs";
		RequestContext rc = new RequestContext(ctx);
		rc.isAlive();
		SvcLogicContext svcLogic = rc.getSvcLogicContext();
		ChefApiClient cac = new ChefApiClient(username, clientPrivatekey, chefserver, organizations);
		ApiMethod am = cac.post(chefAction).body(pushRequest);
		;
		am.execute();
		int code = am.getReturnCode();
		String message = am.getResponseBodyAsString();
		if (code == 201) {
			int startIndex = message.indexOf("jobs") + 6;
			int endIndex = message.length() - 2;
			String jobID = message.substring(startIndex, endIndex);
			svcLogic.setAttribute("jobID", jobID);
			logger.info(jobID);
		}
		chefServerResult(rc, Integer.toString(code), message);
	}


	@SuppressWarnings("static-method")
	private void doFailure(RequestContext rc, int code, String message) {
		SvcLogicContext svcLogic = rc.getSvcLogicContext();
		String msg = (message == null) ? Integer.toString(code) : message;
		if (msg.contains("\n")) {
			msg = msg.substring(msg.indexOf("\n"));
		}

		String status;
		try {
			status = Integer.toString(code);
		} catch (Exception e) {
			status = "500";
		}
		svcLogic.setAttribute("chefAgent.code", status);
		svcLogic.setAttribute("chefAgent.message", msg);
	}


	@SuppressWarnings("static-method")
	private void doSuccess(RequestContext rc) {
		SvcLogicContext svcLogic = rc.getSvcLogicContext();
		svcLogic.setAttribute("chefAgent.code", "200");
	}

	@SuppressWarnings("static-method")
	private void chefServerResult(RequestContext rc, String code, String message) {
		String msg = (message == null) ? " " : message;
		SvcLogicContext svcLogic = rc.getSvcLogicContext();
		svcLogic.setStatus(OUTCOME_SUCCESS);
		svcLogic.setAttribute("chefServerResult.code", code);
		svcLogic.setAttribute("chefServerResult.message", message);
	}

	@SuppressWarnings("static-method")
	private void chefClientResult(RequestContext rc, String code, String message) {
		String msg = (message == null) ? " " : message;
		SvcLogicContext svcLogic = rc.getSvcLogicContext();
		svcLogic.setStatus(OUTCOME_SUCCESS);
		svcLogic.setAttribute("chefClientResult.code", code);
		svcLogic.setAttribute("chefClientResult.message", message);
	}


	private void initialize() {

		logger.info("init chef adapter!!!!!");

	}



}
