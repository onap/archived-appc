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

package org.openecomp.appc.adapter.rest.impl;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.json.JSONObject;
import org.openecomp.appc.Constants;
import org.openecomp.appc.adapter.rest.RestAdapter;
import org.openecomp.appc.adapter.rest.RequestFactory;
import org.openecomp.appc.configuration.Configuration;
import org.openecomp.appc.configuration.ConfigurationFactory;
import org.openecomp.appc.util.httpClient;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;

/**
 * This class implements the {@link RestAdapter} interface. This interface
 * defines the behaviors that our service provides.
 */
public class RestAdapterImpl implements RestAdapter {

    /**
     * The constant for the status code for a failed outcome
     */
    @SuppressWarnings("nls")
    private static final String OUTCOME_FAILURE = "failure";

    /**
     * The constant for the status code for a successful outcome
     */
    @SuppressWarnings("nls")
    private static final String OUTCOME_SUCCESS = "success";

    /**
     * The logger to be used
     */
    private final EELFLogger logger = EELFManager.getInstance().getLogger(RestAdapterImpl.class);

    /**
     * A reference to the adapter configuration object.
     */
    private Configuration configuration;

    /**
     * This default constructor is used as a work around because the activator
     * wasnt getting called
     */
    public RestAdapterImpl() {
        initialize();

    }

    /**
     * Returns the symbolic name of the adapter
     *
     * @return The adapter name
     * @see org.openecomp.appc.adapter.rest.RestAdapter#getAdapterName()
     */
    @Override
    public String getAdapterName() {
        return configuration.getProperty(Constants.PROPERTY_ADAPTER_NAME);
    }

    public void commonGet(Map<String, String> params, SvcLogicContext ctx) {
        logger.info("Run get method");

        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();

        HttpGet httpGet = (HttpGet) createHttpRequest("GET", params, rc);
        executeHttpRequest(httpGet, rc);
    }

    public void commonDelete(Map<String, String> params, SvcLogicContext ctx) {
        logger.info("Run Delete method");

        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();

        HttpDelete httpDelete = (HttpDelete) createHttpRequest("DELETE", params, rc);
        executeHttpRequest(httpDelete, rc);
    }

    public void commonPost(Map<String, String> params, SvcLogicContext ctx) {
        logger.info("Run post method");

        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();

        HttpPost httpPost = (HttpPost) createHttpRequest("POST", params, rc);
        executeHttpRequest(httpPost, rc);
    }

    public void commonPut(Map<String, String> params, SvcLogicContext ctx) {
        logger.info("Run put method");

        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();

        HttpPut httpPut = (HttpPut) createHttpRequest("PUT", params, rc);
        executeHttpRequest(httpPut, rc);
    }

    @SuppressWarnings("static-method")
    private void doFailure(RequestContext rc, HttpStatus code, String message) {
        SvcLogicContext svcLogic = rc.getSvcLogicContext();
        String msg = (message == null) ? code.getReasonPhrase() : message;
        if (msg.contains("\n")) {
            msg = msg.substring(msg.indexOf("\n"));
        }

        String status;
        try {
            status = Integer.toString(code.getStatusCode());
        } catch (Exception e) {
            status = "500";
        }
        svcLogic.setStatus(OUTCOME_FAILURE);
        svcLogic.setAttribute(Constants.ATTRIBUTE_ERROR_CODE, status);
        svcLogic.setAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE, msg);
        svcLogic.setAttribute("org.openecomp.rest.result.code", status);
        svcLogic.setAttribute("org.openecomp.rest.result.message", msg);
    }


    /**
     * @param rc
     *            The request context that manages the state and recovery of the
     *            request for the life of its processing.
     */
    @SuppressWarnings("static-method")
    private void doSuccess(RequestContext rc, int code, String message) {
        SvcLogicContext svcLogic = rc.getSvcLogicContext();
        svcLogic.setStatus(OUTCOME_SUCCESS);
        svcLogic.setAttribute(Constants.ATTRIBUTE_ERROR_CODE, Integer.toString(HttpStatus.OK_200.getStatusCode()));
        svcLogic.setAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE, message);
        svcLogic.setAttribute("org.openecomp.rest.agent.result.code",Integer.toString(code));
        svcLogic.setAttribute("org.openecomp.rest.agent.result.message",message);
        svcLogic.setAttribute("org.openecomp.rest.result.code",Integer.toString(HttpStatus.OK_200.getStatusCode()));
    }

    public void executeHttpRequest(HttpRequestBase httpRequest, RequestContext rc){
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpResponse response = httpClient.execute(httpRequest);
            int responseCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            String responseOutput = EntityUtils.toString(entity);
            if(responseCode == 200){
                doSuccess(rc,responseCode,responseOutput);
            } else {
                doFailure(rc, HttpStatus.getHttpStatus(responseCode), response.getStatusLine().getReasonPhrase());
            }
        }
        catch (Exception ex) {
            doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, ex.toString());
        }
		finally {
			httpClient.close();
		}
    }

    public HttpRequestBase createHttpRequest(String method, Map<String, String> params, RequestContext rc){
        HttpRequestBase httpRequest = null;
        try {
            String tUrl = params.get("org.openecomp.appc.instance.URI");
            String haveHeader = params.get("org.openecomp.appc.instance.haveHeader");
            String headers = params.get("org.openecomp.appc.instance.headers");

            Supplier<RequestFactory> requestFactory =  RequestFactory::new;
            httpRequest = requestFactory.get().getHttpRequest(method, tUrl);

            if (haveHeader.equals("true")) {
                JSONObject JsonHeaders = new JSONObject(headers);
                Iterator keys = JsonHeaders.keys();
                while (keys.hasNext()) {
                    String String1 = (String) keys.next();
                    String String2 = JsonHeaders.getString(String1);
                    httpRequest.addHeader(String1, String2);
                }
            }
            if (params.containsKey("org.openecomp.appc.instance.requestBody")) {
                String body = params.get("org.openecomp.appc.instance.requestBody");
                StringEntity bodyParams = new StringEntity (body,"UTF-8");
                if (method.equals("PUT")){
                    HttpPut httpPut = (HttpPut) httpRequest;
                    httpPut.setEntity(bodyParams);
                }
                if (method.equals("POST")){
                    HttpPost httpPost = (HttpPost) httpRequest;
                    httpPost.setEntity(bodyParams);
                }
            }
        } catch (Exception ex) {
            doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, ex.toString());
			httpRequest.close();
        }
        return httpRequest;
    }


    /**
     * initialize the provider adapter by building the context cache
     */
    private void initialize() {
        configuration = ConfigurationFactory.getConfiguration();

        logger.info("init rest adapter!!!!!");
    }

}