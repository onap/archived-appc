/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.adapter.restHealthcheck.impl;

import java.util.Map;
import java.util.Properties;
import org.apache.http.impl.client.CloseableHttpClient;

import org.onap.appc.Constants;
import org.onap.appc.adapter.restHealthcheck.RestHealthcheckAdapter;
import org.onap.appc.configuration.Configuration;
import org.onap.appc.pool.PoolExtensionException;
import org.onap.appc.util.StructuredPropertyHelper;


import com.att.cdp.zones.ImageService;
import com.att.cdp.zones.Provider;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

import org.glassfish.grizzly.http.util.HttpStatus;

import static com.att.eelf.configuration.Configuration.*;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.util.EntityUtils;
import java.io.IOException;
import java.net.InetAddress;

public class RestHealthcheckAdapterImpl implements RestHealthcheckAdapter {

    /**
     * The constant for the status code for a failed outcome
     */
    @SuppressWarnings("nls")
    public static final String OUTCOME_FAILURE = "failure";
    /**
     * The logger to be used
     */
    private static final EELFLogger logger = EELFManager.getInstance().getLogger(RestHealthcheckAdapterImpl.class);
    /**
     * A reference to the adapter configuration object.
     */
    private Configuration configuration;
    /**
     * This default constructor is used as a work around because the activator
     * wasnt getting called
     */
    public RestHealthcheckAdapterImpl() {
        initialize();
    }
    @Override
    public String getAdapterName() {
        return configuration.getProperty(Constants.PROPERTY_ADAPTER_NAME);
    }
    public void checkHealth(Map<String, String> params, SvcLogicContext ctx) {
        logger.info("VNF rest health check");
        String uri=params.get("VNF.URI");
        String endPoint=params.get("VNF.endpoint");
        String tUrl=uri+"/"+endPoint;
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();
        try(CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(tUrl);
            HttpResponse response ;
            response = httpClient.execute(httpGet);
            int responseCode=response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            String responseOutput=EntityUtils.toString(entity);
            if(responseCode==200)
            {
                doSuccess(rc,responseCode,responseOutput);
            }
            else
            {
                doHealthCheckFailure(rc,responseCode,responseOutput);
            }
        } catch (Exception ex) {
            doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, ex.toString());
        }
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
        svcLogic.setAttribute("healthcheck.result.code", "200");
        svcLogic.setAttribute("healthcheck.result.message", status+" "+msg);
    }
    /**
     * @param rc
     *            The request context that manages the state and recovery of the
     *            request for the life of its processing.
     */
    @SuppressWarnings("static-method")
    private void doHealthCheckFailure(RequestContext rc, int code, String message) {
        SvcLogicContext svcLogic = rc.getSvcLogicContext();
        String msg = Integer.toString(code)+" "+message;
        svcLogic.setAttribute("healthcheck.result.code", "200");
        svcLogic.setAttribute("healthcheck.result.message", msg);
    }
    @SuppressWarnings("static-method")
    private void doSuccess(RequestContext rc, int code, String message) {
        SvcLogicContext svcLogic = rc.getSvcLogicContext();
        String msg = Integer.toString(code)+" "+message;
        svcLogic.setAttribute("healthcheck.result.code", "400");
        svcLogic.setAttribute("healthcheck.result.message", msg);
    }
    /**
     * initialize the provider adapter by building the context cache
     */
    private void initialize() {
        logger.info("init rest health check adapter!!!!!");
    }


}
