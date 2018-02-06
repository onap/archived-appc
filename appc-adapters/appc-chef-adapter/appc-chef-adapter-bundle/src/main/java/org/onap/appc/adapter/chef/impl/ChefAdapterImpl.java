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
package org.onap.appc.adapter.chef.impl;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.onap.appc.adapter.chef.ChefAdapter;
import org.onap.appc.adapter.chef.chefapi.ApiMethod;
import org.onap.appc.adapter.chef.chefclient.ChefApiClient;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

/**
 * This class implements the {@link ChefAdapter} interface. This interface
 * defines the behaviors that our service provides.
 */
public class ChefAdapterImpl implements ChefAdapter {

    // chef server Initialize variable
    private String username = StringUtils.EMPTY;
    private String clientPrivatekey = StringUtils.EMPTY;
    private String chefserver = StringUtils.EMPTY;
    private String serverAddress = StringUtils.EMPTY;
    private String organizations = StringUtils.EMPTY;

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

    private static final String CANNOT_FIND_PRIVATE_KEY_STR =
            "Cannot find the private key in the APPC file system, please load the private key to ";

    private static final String POSTING_REQUEST_JSON_ERROR_STR = "Error posting request due to invalid JSON block: ";
    private static final String POSTING_REQUEST_ERROR_STR = "Error posting request: ";
    private static final String CHEF_CLIENT_RESULT_CODE_STR = "chefClientResult.code";
    private static final String CHEF_SERVER_RESULT_CODE_STR = "chefServerResult.code";
    private static final String CHEF_CLIENT_RESULT_MSG_STR = "chefClientResult.message";
    private static final String CHEF_SERVER_RESULT_MSG_STR = "chefServerResult.message";
    private static final String CHEF_ACTION_STR = "chefAction";
    private static final String NODE_LIST_STR = "NodeList";

    /**
     * This default constructor is used as a work around because the activator wasnt
     * getting called
     */
    public ChefAdapterImpl() {
        initialize();
    }

    public ChefAdapterImpl(Properties props) {
        initialize();
    }

    /**
     * This constructor is used primarily in the test cases to bypass initialization
     * of the adapter for isolated, disconnected testing
     *
     * @param initialize
     *        True if the adapter is to be initialized, can false if not
     */

    public ChefAdapterImpl(boolean initialize) {
        if (initialize) {
            initialize();
        }
    }

    public ChefAdapterImpl(String key) {
        initialize();
    }

    @SuppressWarnings("nls")
    @Override
    public void vnfcEnvironment(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        int code;
        try {
            logger.info("environment of VNF-C");
            chefInfo(params, ctx);
            RequestContext rc = new RequestContext(ctx);
            logger.info("Context" + ctx);
            rc.isAlive();
            String env = params.get("Environment");
            logger.info("Environmnet" + env);
            if (env.equals(StringUtils.EMPTY)) {
                chefServerResult(rc, 200, "Skip Environment block ");
            } else {
                JSONObject envJ = new JSONObject(env);
                String envName = envJ.getString("name");
                String message;
                if (privateKeyCheck()) {
                    // update the details of an environment on the Chef server.
                    ChefApiClient cac = new ChefApiClient(username, clientPrivatekey, chefserver, organizations);
                    ApiMethod am = cac.put("/environments/" + envName).body(env);
                    am.execute();
                    code = am.getReturnCode();
                    message = am.getResponseBodyAsString();
                    if (code == 404) {
                        // need create a new environment
                        am = cac.post("/environments").body(env);
                        am.execute();
                        code = am.getReturnCode();
                        message = am.getResponseBodyAsString();
                        logger.info("requestbody" + am.getReqBody());
                    }

                } else {
                    code = 500;
                    message = CANNOT_FIND_PRIVATE_KEY_STR + clientPrivatekey;
                    doFailure(ctx, code, message);
                }
                chefServerResult(rc, code, message);
            }
        }

        catch (JSONException e) {
            code = 401;
            doFailure(ctx, code, POSTING_REQUEST_JSON_ERROR_STR + e.getMessage());
        } catch (Exception e) {
            code = 401;
            doFailure(ctx, code, POSTING_REQUEST_ERROR_STR + e.getMessage());
        }
    }

    @SuppressWarnings("nls")
    @Override
    public void vnfcNodeobjects(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        logger.info("update the nodeObjects of VNF-C");
        int code;
        try {
            chefInfo(params, ctx);
            String nodeListS = params.get(NODE_LIST_STR);
            String nodeS = params.get("Node");
            if (StringUtils.isNotBlank(nodeListS) && StringUtils.isNotBlank(nodeS)) {
                nodeListS = nodeListS.replace("[", StringUtils.EMPTY);
                nodeListS = nodeListS.replace("]", StringUtils.EMPTY);
                nodeListS = nodeListS.replace("\"", StringUtils.EMPTY);
                nodeListS = nodeListS.replace(" ", StringUtils.EMPTY);
                List<String> nodes = Arrays.asList(nodeListS.split("\\s*,\\s*"));
                RequestContext rc = new RequestContext(ctx);
                rc.isAlive();
                code = 200;
                String message = null;
                if (privateKeyCheck()) {
                    ChefApiClient cac = new ChefApiClient(username, clientPrivatekey, chefserver, organizations);

                    for (int i = 0; i < nodes.size(); i++) {
                        String nodeName = nodes.get(i);
                        JSONObject nodeJ = new JSONObject(nodeS);
                        nodeJ.remove("name");
                        nodeJ.put("name", nodeName);
                        String nodeObject = nodeJ.toString();
                        logger.info(nodeObject);
                        ApiMethod am = cac.put("/nodes/" + nodeName).body(nodeObject);
                        am.execute();
                        code = am.getReturnCode();
                        message = am.getResponseBodyAsString();
                        if (code != 200) {
                            break;
                        }
                    }
                } else {
                    code = 500;
                    message = CANNOT_FIND_PRIVATE_KEY_STR + clientPrivatekey;
                    doFailure(ctx, code, message);
                }
                chefServerResult(rc, code, message);
            }
            else {
                throw new SvcLogicException("Missing Mandatory param(s) Node , NodeList ");
            }
        } catch (JSONException e) {
            code = 401;
            doFailure(ctx, code, POSTING_REQUEST_JSON_ERROR_STR + e.getMessage());
        } catch (Exception ex) {
            code = 401;
            doFailure(ctx, code, POSTING_REQUEST_ERROR_STR + ex.getMessage());
        }
    }

    @Override
    public void vnfcPushJob(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        int code;
        try {
            chefInfo(params, ctx);
            String nodeList = params.get(NODE_LIST_STR);
            if (StringUtils.isNotBlank(nodeList)) {
                String isCallback = params.get("CallbackCapable");
                String chefAction = "/pushy/jobs";
                // need work on this
                String pushRequest;
                if ("true".equals(isCallback)) {
                    String requestId = params.get("RequestId");
                    String callbackUrl = params.get("CallbackUrl");
                    pushRequest = "{" + "\"command\": \"chef-client\"," + "\"run_timeout\": 300," + "\"nodes\":"
                            + nodeList + "," + "\"env\": {\"RequestId\": \"" + requestId + "\", \"CallbackUrl\": \""
                            + callbackUrl + "\"}," + "\"capture_output\": true" + "}";
                } else {
                    pushRequest = "{" + "\"command\": \"chef-client\"," + "\"run_timeout\": 300," + "\"nodes\":"
                            + nodeList + "," + "\"env\": {}," + "\"capture_output\": true" + "}";
                }
                RequestContext rc = new RequestContext(ctx);

                rc.isAlive();
                SvcLogicContext svcLogic = rc.getSvcLogicContext();
                ChefApiClient cac = new ChefApiClient(username, clientPrivatekey, chefserver, organizations);
                ApiMethod am = cac.post(chefAction).body(pushRequest);
                am.execute();
                code = am.getReturnCode();
                logger.info("pushRequest:" + pushRequest);
                logger.info("requestbody:" + am.getReqBody());
                String message = am.getResponseBodyAsString();
                if (code == 201) {
                    int startIndex = message.indexOf("jobs") + 5;
                    int endIndex = message.length() - 2;
                    String jobID = message.substring(startIndex, endIndex);
                    svcLogic.setAttribute("jobID", jobID);
                    logger.info(jobID);
                }
                chefServerResult(rc, code, message);
            }
            else {
                throw new SvcLogicException("Missing Mandatory param(s)  NodeList ");
            }
        } catch (JSONException e) {
            code = 401;
            doFailure(ctx, code, POSTING_REQUEST_JSON_ERROR_STR + e.getMessage());
        } catch (Exception e) {
            code = 401;
            doFailure(ctx, code, POSTING_REQUEST_ERROR_STR + e.getMessage());
        }
    }

    @SuppressWarnings("nls")
    @Override
    public void fetchResults(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        int code = 200;
        try {
            chefInfo(params, ctx);
            String nodeListS = params.get(NODE_LIST_STR);
            if (StringUtils.isNotBlank(nodeListS)) {
                nodeListS = nodeListS.replace("[", StringUtils.EMPTY);
                nodeListS = nodeListS.replace("]", StringUtils.EMPTY);
                nodeListS = nodeListS.replace("\"", StringUtils.EMPTY);
                nodeListS = nodeListS.replace(" ", StringUtils.EMPTY);
                List<String> nodes = Arrays.asList(nodeListS.split("\\s*,\\s*"));
                JSONObject result = new JSONObject();
                String returnMessage = StringUtils.EMPTY;

                for (String node : nodes) {
                    String chefAction = "/nodes/" + node;
                    String message;
                    if (privateKeyCheck()) {
                        ApiMethod am = getApiMethod(chefAction);
                        code = am.getReturnCode();
                        message = am.getResponseBodyAsString();
                    } else {
                        code = 500;
                        message = CANNOT_FIND_PRIVATE_KEY_STR + clientPrivatekey;
                        doFailure(ctx, code, message);
                    }
                    if (code == 200) {
                        JSONObject nodeResult = new JSONObject();
                        JSONObject allNodeData = new JSONObject(message);
                        allNodeData = allNodeData.getJSONObject("normal");
                        String attribute = "PushJobOutput";

                        String resultData = allNodeData.optString(attribute);
                        if (resultData == null) {
                            resultData = allNodeData.optJSONObject(attribute).toString();

                            if (resultData == null) {
                                resultData = allNodeData.optJSONArray(attribute).toString();

                                if (resultData == null) {
                                    code = 500;
                                    returnMessage = "Cannot find " + attribute;
                                    break;
                                }
                            }
                        }
                        nodeResult.put(attribute, resultData);
                        result.put(node, nodeResult);
                        returnMessage = result.toString();
                    } else {
                        code = 500;
                        returnMessage = message + " Cannot access: " + node;
                        doFailure(ctx, code, message);
                        break;
                    }
                }

                RequestContext rc = new RequestContext(ctx);
                rc.isAlive();
                chefServerResult(rc, code, returnMessage);
            } else {
                throw new SvcLogicException("Missing Mandatory param(s)  NodeList ");
            }
        } catch (JSONException e) {
            code = 401;
            doFailure(ctx, code, POSTING_REQUEST_JSON_ERROR_STR + e.getMessage());
        } catch (Exception ex) {
            code = 401;
            doFailure(ctx, code, POSTING_REQUEST_ERROR_STR + ex.getMessage());
        }
    }

    private ApiMethod getApiMethod(String chefAction) {
        ChefApiClient cac = new ChefApiClient(username, clientPrivatekey, chefserver, organizations);

        return cac.get(chefAction).execute();
    }

    /**
     * build node object
     */
    @SuppressWarnings("nls")
    @Override
    public void nodeObejctBuilder(Map<String, String> params, SvcLogicContext ctx) {
        logger.info("nodeObejctBuilder");
        String name = params.get("nodeobject.name");
        String normal = params.get("nodeobject.normal");
        String overrides = params.get("nodeobject.overrides");
        String defaults = params.get("nodeobject.defaults");
        String runList = params.get("nodeobject.run_list");
        String chefEnvironment = params.get("nodeobject.chef_environment");
        String nodeObject = "{\"json_class\":\"Chef::Node\",\"default\":{" + defaults
                + "},\"chef_type\":\"node\",\"run_list\":[" + runList + "],\"override\":{" + overrides
                + "},\"normal\": {" + normal + "},\"automatic\":{},\"name\":\"" + name + "\",\"chef_environment\":\""
                + chefEnvironment + "\"}";
        logger.info(nodeObject);
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();
        SvcLogicContext svcLogic = rc.getSvcLogicContext();
        svcLogic.setAttribute("chef.nodeObject", nodeObject);
    }

    /**
     * send get request to chef server
     * 
     * @throws SvcLogicException
     */
    private void chefInfo(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {

        username = params.get("username");
        serverAddress = params.get("serverAddress");
        organizations = params.get("organizations");
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(serverAddress)
                && StringUtils.isNotBlank(organizations)) {
            chefserver = "https://" + serverAddress + "/organizations/" + organizations;
            clientPrivatekey = "/opt/app/bvc/chef/" + serverAddress + "/" + organizations + "/" + username + ".pem";
            logger.info(" clientPrivatekey  " + clientPrivatekey);
        } else {
            doFailure(ctx, 401, "Missing mandatory param(s) such as username, serverAddress, organizations");
        }
    }

    private Boolean privateKeyCheck() {
        File f = new File(clientPrivatekey);
        if (f.exists()) {
            logger.info("Key exists");
            return true;
        } else {
            logger.info("Key doesn't exists");
            return false;
        }
    }

    @SuppressWarnings("nls")
    @Override
    public void retrieveData(Map<String, String> params, SvcLogicContext ctx) {
        String contextData;
        String allConfigData = params.get("allConfig");
        String key = params.get("key");
        String dgContext = params.get("dgContext");
        JSONObject josnConfig = new JSONObject(allConfigData);

        try {
            contextData = josnConfig.getString(key);
        } catch (Exception e) {
            try {
                contextData = josnConfig.getJSONObject(key).toString();
            } catch (Exception ex) {
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
        String string1 = params.get("String1");
        String string2 = params.get("String2");
        String dgContext = params.get("dgContext");
        String contextData = string1 + string2;
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();
        SvcLogicContext svcLogic = rc.getSvcLogicContext();
        svcLogic.setAttribute(dgContext, contextData);
    }

    /**
     * Send GET request to chef server
     * 
     * @throws SvcLogicException
     */
    @SuppressWarnings("nls")

    @Override
    public void chefGet(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        logger.info("chef get method");
        chefInfo(params, ctx);
        String chefAction = params.get(CHEF_ACTION_STR);
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();
        int code;
        String message;

        if (privateKeyCheck()) {
            ApiMethod am = getApiMethod(chefAction);
            code = am.getReturnCode();
            message = am.getResponseBodyAsString();
        } else {
            code = 500;
            message = CANNOT_FIND_PRIVATE_KEY_STR + clientPrivatekey;
        }
        chefServerResult(rc, code, message);
    }

    /**
     * Send PUT request to chef server
     * 
     * @throws SvcLogicException
     */
    @SuppressWarnings("nls")

    @Override
    public void chefPut(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        chefInfo(params, ctx);
        String chefAction = params.get(CHEF_ACTION_STR);
        String chefNodeStr = params.get("chefRequestBody");
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();
        int code;
        String message;
        if (privateKeyCheck()) {
            ChefApiClient cac = new ChefApiClient(username, clientPrivatekey, chefserver, organizations);

            ApiMethod am = cac.put(chefAction).body(chefNodeStr);
            am.execute();
            code = am.getReturnCode();
            message = am.getResponseBodyAsString();
        } else {
            code = 500;
            message = CANNOT_FIND_PRIVATE_KEY_STR + clientPrivatekey;
        }
        logger.info(code + "   " + message);
        chefServerResult(rc, code, message);
    }

    /**
     * send Post request to chef server
     * 
     * @throws SvcLogicException
     */
    @Override
    public void chefPost(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        chefInfo(params, ctx);
        logger.info("chef Post method");
        logger.info(username + " " + clientPrivatekey + " " + chefserver + " " + organizations);
        String chefNodeStr = params.get("chefRequestBody");
        String chefAction = params.get(CHEF_ACTION_STR);

        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();
        int code;
        String message;
        // should load pem from somewhere else
        if (privateKeyCheck()) {
            ChefApiClient cac = new ChefApiClient(username, clientPrivatekey, chefserver, organizations);

            // need pass path into it
            // "/nodes/testnode"
            ApiMethod am = cac.post(chefAction).body(chefNodeStr);
            am.execute();
            code = am.getReturnCode();
            message = am.getResponseBodyAsString();
        } else {
            code = 500;
            message = CANNOT_FIND_PRIVATE_KEY_STR + clientPrivatekey;
        }
        logger.info(code + "   " + message);
        chefServerResult(rc, code, message);
    }

    /**
     * send delete request to chef server
     * 
     * @throws SvcLogicException
     */
    @Override
    public void chefDelete(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        logger.info("chef delete method");
        chefInfo(params, ctx);
        String chefAction = params.get(CHEF_ACTION_STR);
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();
        int code;
        String message;
        if (privateKeyCheck()) {
            ChefApiClient cac = new ChefApiClient(username, clientPrivatekey, chefserver, organizations);
            ApiMethod am = cac.delete(chefAction);
            am.execute();
            code = am.getReturnCode();
            message = am.getResponseBodyAsString();
        } else {
            code = 500;
            message = CANNOT_FIND_PRIVATE_KEY_STR + clientPrivatekey;
        }
        logger.info(code + "   " + message);
        chefServerResult(rc, code, message);
    }

    /**
     * Trigger target vm run chef
     */
    @Override
    public void trigger(Map<String, String> params, SvcLogicContext ctx) {
        logger.info("Run trigger method");
        String tVmIp = params.get("ip");
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(tVmIp);
            HttpResponse response = httpClient.execute(httpGet);
            int responseCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            String responseOutput = EntityUtils.toString(entity);
            chefClientResult(rc, responseCode, responseOutput);
            doSuccess(rc);
        } catch (Exception e) {
            doFailure(rc, 500, e.toString());
        }
    }

    @SuppressWarnings("nls")
    @Override
    public void checkPushJob(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        int code;
        try {
            chefInfo(params, ctx);
            String jobID = params.get("jobid");
            String retry = params.get("retryTimes");
            String intrva = params.get("retryInterval");
            if (StringUtils.isNotBlank(retry) && StringUtils.isNotBlank(intrva)) {

                int retryTimes = Integer.parseInt(params.get("retryTimes"));
                int retryInterval = Integer.parseInt(params.get("retryInterval"));

                String chefAction = "/pushy/jobs/" + jobID;

                RequestContext rc = new RequestContext(ctx);
                rc.isAlive();
                SvcLogicContext svcLogic = rc.getSvcLogicContext();
                String message = StringUtils.EMPTY;
                String status = StringUtils.EMPTY;
                for (int i = 0; i < retryTimes; i++) {
                    sleepFor(retryInterval);
                    ApiMethod am = getApiMethod(chefAction);
                    code = am.getReturnCode();
                    message = am.getResponseBodyAsString();
                    JSONObject obj = new JSONObject(message);
                    status = obj.getString("status");
                    if (!"running".equals(status)) {
                        logger.info(i + " time " + code + "   " + status);
                        break;
                    }
                }
                if ("complete".equals(status)) {
                    svcLogic.setAttribute(CHEF_SERVER_RESULT_CODE_STR, "200");
                    svcLogic.setAttribute(CHEF_SERVER_RESULT_MSG_STR, message);
                } else {
                    if ("running".equals(status)) {
                        svcLogic.setAttribute(CHEF_SERVER_RESULT_CODE_STR, "202");
                        svcLogic.setAttribute(CHEF_SERVER_RESULT_MSG_STR, "chef client runtime out");
                    } else {
                        svcLogic.setAttribute(CHEF_SERVER_RESULT_CODE_STR, "500");
                        svcLogic.setAttribute(CHEF_SERVER_RESULT_MSG_STR, message);
                    }
                }
            }
            else {
                throw new SvcLogicException("Missing Mandatory param(s) retryTimes , retryInterval ");
            }
        } catch (Exception e) {
            code = 401;
            doFailure(ctx, code, e.getMessage());
        }
    }

    private void sleepFor(int retryInterval) {
        try {
            Thread.sleep(retryInterval); // 1000 milliseconds is one second.
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    @SuppressWarnings("nls")
    @Override
    public void pushJob(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        int code;
        try {
            chefInfo(params, ctx);
            String pushRequest = params.get("pushRequest");
            String chefAction = "/pushy/jobs";
            RequestContext rc = new RequestContext(ctx);
            rc.isAlive();
            SvcLogicContext svcLogic = rc.getSvcLogicContext();
            ChefApiClient cac = new ChefApiClient(username, clientPrivatekey, chefserver, organizations);
            ApiMethod am = cac.post(chefAction).body(pushRequest);

            am.execute();
            code = am.getReturnCode();
            String message = am.getResponseBodyAsString();
            if (code == 201) {
                int startIndex = message.indexOf("jobs") + 6;
                int endIndex = message.length() - 2;
                String jobID = message.substring(startIndex, endIndex);
                svcLogic.setAttribute("jobID", jobID);
                logger.info(jobID);
            }
            chefServerResult(rc, code, message);
        } catch (Exception e) {
            code = 401;
            doFailure(ctx, code, e.getMessage());
        }
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

    /**
     * @param rc
     *        The request context that manages the state and recovery of the
     *        request for the life of its processing.
     */
    @SuppressWarnings("static-method")
    private void doSuccess(RequestContext rc) {
        SvcLogicContext svcLogic = rc.getSvcLogicContext();
        svcLogic.setAttribute("chefAgent.code", "200");
    }

    @SuppressWarnings("static-method")
    private void chefServerResult(RequestContext rc, int  code, String message) {
      initSvcLogic(rc, code, message, "server");
    }

    @SuppressWarnings("static-method")
    private void chefClientResult(RequestContext rc, int code, String message) {
        initSvcLogic(rc, code, message, "client");
    }

    private void initSvcLogic(RequestContext rc, int code, String message, String target) {

        SvcLogicContext svcLogic = rc.getSvcLogicContext();
        String codeStr = "server".equals(target) ? CHEF_SERVER_RESULT_CODE_STR : CHEF_CLIENT_RESULT_CODE_STR;
        String messageStr = "client".equals(target) ? CHEF_SERVER_RESULT_MSG_STR : CHEF_CLIENT_RESULT_MSG_STR;

        svcLogic.setStatus(OUTCOME_SUCCESS);
        svcLogic.setAttribute(codeStr, Integer.toString(code));
        svcLogic.setAttribute(messageStr, message);
        logger.info(codeStr + ": " + svcLogic.getAttribute(codeStr));
        logger.info(messageStr + ": " + svcLogic.getAttribute(messageStr));
    }


    /**
     * initialize the provider adapter by building the context cache
     */
    private void initialize() {

        logger.info("Initialize Chef Adapter");
    }

    @SuppressWarnings("static-method")
    private void doFailure(SvcLogicContext svcLogic, int code, String message) throws SvcLogicException {

        String cutMessage = message.contains("\n") ? message.substring(message.indexOf('\n')) : message;

        svcLogic.setStatus(OUTCOME_FAILURE);
        svcLogic.setAttribute(CHEF_SERVER_RESULT_CODE_STR, Integer.toString(code));
        svcLogic.setAttribute(CHEF_SERVER_RESULT_MSG_STR, cutMessage);

        throw new SvcLogicException("Chef Adapter error:" + cutMessage);
    }
}
