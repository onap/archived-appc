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

import java.io.File;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.openecomp.appc.Constants;
import org.openecomp.appc.adapter.chef.ChefAdapter;
import org.openecomp.appc.adapter.chef.chefapi.ApiMethod;
import org.openecomp.appc.adapter.chef.chefclient.ChefApiClient;
import org.openecomp.appc.configuration.Configuration;
import org.openecomp.appc.configuration.ConfigurationFactory;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

/**
 * This class implements the {@link ChefAdapter} interface. This interface
 * defines the behaviors that our service provides.
 */
public class ChefAdapterImpl implements ChefAdapter {
    // chef server Initialize variable
    private String clientName = "";
    private String clientPrivatekey = "";
    private String chefserver = "";
    private String serverAddress = "";
    private String organizations = "";


    /**
     * The constant for the status code for a successful outcome
     */
    private static final String OUTCOME_SUCCESS = "success";

    /**
     * The logger to be used
     */
    private final EELFLogger logger = EELFManager.getInstance().getLogger(ChefAdapterImpl.class);

    private final String CANNOT_FIND_PRIVATE_KEY_STR = "Cannot find the private key in the APPC file system, please load the private key to ";
    private final String CHEF_ACTION_STR = "org.openecomp.appc.instance.chefAction";
    private final String ORGANIZATIONS_STR = "/organizations/";
    /**
     * A reference to the adapter configuration object.
     */
    private Configuration configuration;

    /**
     * This default constructor is used as a work around because the activator wasnt
     * getting called
     */
    public ChefAdapterImpl() {
        initialize();
    }

    /**
     * This constructor is used primarily in the test cases to bypass initialization
     * of the adapter for isolated, disconnected testing
     *
     * @param initialize
     *            True if the adapter is to be initialized, can false if not
     */
    public ChefAdapterImpl(boolean initialize) {
        configuration = ConfigurationFactory.getConfiguration();
        if (initialize) {
            initialize();
        }
    }

    public ChefAdapterImpl(String key) {
        initialize(key);
    }

    /**
     * Returns the symbolic name of the adapter
     *
     * @return The adapter name
     * @see org.openecomp.appc.adapter.chef.ChefAdapter#getAdapterName()
     */
    @Override
    public String getAdapterName() {
        return configuration.getProperty(Constants.PROPERTY_ADAPTER_NAME);
    }

    /**
     * build node object
     */
    @Override
    public void nodeObejctBuilder(Map<String, String> params, SvcLogicContext ctx) {
        logger.info("nodeObejctBuilder");
        String name = params.get("org.openecomp.appc.instance.nodeobject.name");
        String normal = params.get("org.openecomp.appc.instance.nodeobject.normal");
        String overrides = params.get("org.openecomp.appc.instance.nodeobject.overrides");
        String defaults = params.get("org.openecomp.appc.instance.nodeobject.defaults");
        String runList = params.get("org.openecomp.appc.instance.nodeobject.run_list");
        String chefEnvironment = params.get("org.openecomp.appc.instance.nodeobject.chef_environment");
        String nodeObject = "{\"json_class\":\"Chef::Node\",\"default\":{" + defaults
                + "},\"chef_type\":\"node\",\"run_list\":[" + runList + "],\"override\":{" + overrides
                + "},\"normal\": {" + normal + "},\"automatic\":{},\"name\":\"" + name + "\",\"chef_environment\":\""
                + chefEnvironment + "\"}";
        logger.info(nodeObject);

        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();
        SvcLogicContext svcLogic = rc.getSvcLogicContext();
        svcLogic.setAttribute("org.openecomp.appc.chef.nodeObject", nodeObject);
    }

    /**
     * send get request to chef server
     */
    public void chefInfo(Map<String, String> params) {
        clientName = params.get("org.openecomp.appc.instance.username");
        serverAddress = params.get("org.openecomp.appc.instance.serverAddress");
        organizations = params.get("org.openecomp.appc.instance.organizations");
        chefserver = "https://" + serverAddress + ORGANIZATIONS_STR + organizations;
        if (params.containsKey("org.openecomp.appc.instance.pemPath")) {
            clientPrivatekey = params.get("org.openecomp.appc.instance.pemPath");
        } else {
            clientPrivatekey = "/opt/app/bvc/chef/" + serverAddress + "/" + organizations + "/" + clientName + ".pem";
        }
    }

    public Boolean privateKeyCheck() {
        File f = new File(clientPrivatekey);
        return f.exists();
    }

    @Override
    public void retrieveData(Map<String, String> params, SvcLogicContext ctx) {
        String allConfigData = params.get("org.openecomp.appc.instance.allConfig");
        String key = params.get("org.openecomp.appc.instance.key");
        String dgContext = params.get("org.openecomp.appc.instance.dgContext");
        JSONObject josnConfig = new JSONObject(allConfigData);

        String contextData;
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

    @Override
    public void combineStrings(Map<String, String> params, SvcLogicContext ctx) {

        String string1 = params.get("org.openecomp.appc.instance.String1");
        String string2 = params.get("org.openecomp.appc.instance.String2");
        String dgContext = params.get("org.openecomp.appc.instance.dgContext");
        String contextData = string1 + string2;
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();
        SvcLogicContext svcLogic = rc.getSvcLogicContext();
        svcLogic.setAttribute(dgContext, contextData);
    }

    /**
     * Send GET request to chef server
     */
    @Override
    public void chefGet(Map<String, String> params, SvcLogicContext ctx) {
        logger.info("chef get method");
        chefInfo(params);
        String chefAction = params.get(CHEF_ACTION_STR);
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();
        int code;
        String message;
        if (privateKeyCheck()) {
            ChefApiClient cac = new ChefApiClient(clientName, clientPrivatekey, chefserver, organizations);
            ApiMethod am = cac.get(chefAction);
            am.execute();
            code = am.getReturnCode();
            message = am.getResponseBodyAsString();
        } else {
            code = 500;
            message = CANNOT_FIND_PRIVATE_KEY_STR + clientPrivatekey;
        }
        chefServerResult(rc, Integer.toString(code), message);
    }

    /**
     * Send PUT request to chef server
     */
    @Override
    public void chefPut(Map<String, String> params, SvcLogicContext ctx) {
        chefInfo(params);
        String chefAction = params.get(CHEF_ACTION_STR);
        String chefNodeStr = params.get("org.openecomp.appc.instance.chefRequestBody");
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();
        int code;
        String message;
        if (privateKeyCheck()) {
            ChefApiClient cac = new ChefApiClient(clientName, clientPrivatekey, chefserver, organizations);

            ApiMethod am = cac.put(chefAction).body(chefNodeStr);
            am.execute();
            code = am.getReturnCode();
            message = am.getResponseBodyAsString();
        } else {
            code = 500;
            message = CANNOT_FIND_PRIVATE_KEY_STR + clientPrivatekey;
        }
        logger.info(code + "   " + message);
        chefServerResult(rc, Integer.toString(code), message);
    }

    /**
     *  send Post request to chef server
     */
    @Override
    public void chefPost(Map<String, String> params, SvcLogicContext ctx) {
        chefInfo(params);
        logger.info("chef Post method");
        logger.info(clientName + " " + clientPrivatekey + " " + chefserver + " " + organizations);
        String chefNodeStr = params.get("org.openecomp.appc.instance.chefRequestBody");
        String chefAction = params.get(CHEF_ACTION_STR);

        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();
        int code;
        String message;
        // should load pem from somewhere else
        if (privateKeyCheck()) {
            ChefApiClient cac = new ChefApiClient(clientName, clientPrivatekey, chefserver, organizations);

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
        chefServerResult(rc, Integer.toString(code), message);
    }

    /**
     * send delete request to chef server
     */
    @Override
    public void chefDelete(Map<String, String> params, SvcLogicContext ctx) {
        logger.info("chef delete method");
        chefInfo(params);
        String chefAction = params.get(CHEF_ACTION_STR);
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();
        int code;
        String message;
        if (privateKeyCheck()) {
            ChefApiClient cac = new ChefApiClient(clientName, clientPrivatekey, chefserver, organizations);
            ApiMethod am = cac.delete(chefAction);
            am.execute();
            code = am.getReturnCode();
            message = am.getResponseBodyAsString();
        } else {
            code = 500;
            message = CANNOT_FIND_PRIVATE_KEY_STR + clientPrivatekey;
        }
        logger.info(code + "   " + message);
        chefServerResult(rc, Integer.toString(code), message);
    }

    /**
     * Trigger target vm run chef
     */
    @Override
    public void trigger(Map<String, String> params, SvcLogicContext ctx) {
        logger.info("Run trigger method");
        String tVmIp = params.get("org.openecomp.appc.instance.ip");
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(tVmIp);
            HttpResponse response;
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

    @Override
    public void checkPushJob(Map<String, String> params, SvcLogicContext ctx) {
        chefInfo(params);
        String jobID = params.get("org.openecomp.appc.instance.jobid");
        int retryTimes = Integer.parseInt(params.get("org.openecomp.appc.instance.retryTimes"));
        int retryInterval = Integer.parseInt(params.get("org.openecomp.appc.instance.retryInterval"));
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
            ChefApiClient cac = new ChefApiClient(clientName, clientPrivatekey, chefserver, organizations);
            ApiMethod am = cac.get(chefAction);
            am.execute();
            int code = am.getReturnCode();
            message = am.getResponseBodyAsString();
            JSONObject obj = new JSONObject(message);
            status = obj.getString("status");
            if (!"running".equals(status)) {
                logger.info(i + " time " + code + "   " + status);
                break;
            }

        }
        if ("complete".equals(status)) {
            svcLogic.setAttribute("org.openecomp.appc.chefServerResult.code", "200");
            svcLogic.setAttribute("org.openecomp.appc.chefServerResult.message", message);
        } else {
            if ("running".equals(status)) {
                svcLogic.setAttribute("org.openecomp.appc.chefServerResult.code", "202");
                svcLogic.setAttribute("org.openecomp.appc.chefServerResult.message", "chef client runtime out");
            } else {
                svcLogic.setAttribute("org.openecomp.appc.chefServerResult.code", "500");
                svcLogic.setAttribute("org.openecomp.appc.chefServerResult.message", message);
            }
        }
    }

    @Override
    public void pushJob(Map<String, String> params, SvcLogicContext ctx) {
        chefInfo(params);
        String pushRequest = params.get("org.openecomp.appc.instance.pushRequest");
        String chefAction = "/pushy/jobs";
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();
        SvcLogicContext svcLogic = rc.getSvcLogicContext();
        ChefApiClient cac = new ChefApiClient(clientName, clientPrivatekey, chefserver, organizations);
        ApiMethod am = cac.post(chefAction).body(pushRequest);

        am.execute();
        int code = am.getReturnCode();
        String message = am.getResponseBodyAsString();
        if (code == 201) {
            int startIndex = message.indexOf("jobs") + 6;
            int endIndex = message.length() - 2;
            String jobID = message.substring(startIndex, endIndex);
            svcLogic.setAttribute("org.openecomp.appc.jobID", jobID);
            logger.info(jobID);
        }
        chefServerResult(rc, Integer.toString(code), message);
    }

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
            logger.info("Couldn't covert " + code + " to an Integer, defaulting status to 500", e);
            status = "500";
        }
        svcLogic.setAttribute("org.openecomp.appc.chefAgent.code", status);
        svcLogic.setAttribute("org.openecomp.appc.chefAgent.message", msg);
    }

    /**
     * @param rc
     *            The request context that manages the state and recovery of the
     *            request for the life of its processing.
     */
    private void doSuccess(RequestContext rc) {
        SvcLogicContext svcLogic = rc.getSvcLogicContext();
        svcLogic.setAttribute("org.openecomp.appc.chefAgent.code", "200");
    }

    private void chefServerResult(RequestContext rc, String code, String message) {
        SvcLogicContext svcLogic = rc.getSvcLogicContext();
        svcLogic.setStatus(OUTCOME_SUCCESS);
        svcLogic.setAttribute("org.openecomp.appc.chefServerResult.code", code);
        svcLogic.setAttribute("org.openecomp.appc.chefServerResult.message", message);
    }

    private void chefClientResult(RequestContext rc, String code, String message) {
        SvcLogicContext svcLogic = rc.getSvcLogicContext();
        svcLogic.setStatus(OUTCOME_SUCCESS);
        svcLogic.setAttribute("org.openecomp.appc.chefClientResult.code", code);
        svcLogic.setAttribute("org.openecomp.appc.chefClientResult.message", message);
    }

    /**
     * initialize the provider adapter by building the context cache
     */
    private void initialize() {
        configuration = ConfigurationFactory.getConfiguration();
        // need to fetch data from appc configurator or form some file in the appc vms
        clientName = "testnode";
        clientPrivatekey = System.getProperty("user.dir") + "/src/test/resources/testclient.pem";
        serverAddress = "http://example.com";
        organizations = "test";
        chefserver = serverAddress + ORGANIZATIONS_STR + organizations;
        logger.info("Initialize Chef Adapter");
    }

    private void initialize(String key) {
        configuration = ConfigurationFactory.getConfiguration();
        // need to fetch data from appc configurator or form some file in the appc vms
        clientName = "testnode";
        clientPrivatekey = key;
        serverAddress = "http://example.com";
        organizations = "test";
        chefserver = serverAddress + ORGANIZATIONS_STR + organizations;
        logger.info("Initialize Chef Adapter");
    }

}
