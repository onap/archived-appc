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

package org.openecomp.appc.flow.controller.executorImpl;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.openecomp.appc.flow.controller.data.Response;
import org.openecomp.appc.flow.controller.data.Transaction;
import org.openecomp.appc.flow.controller.interfaces.FlowExecutorInterface;
import org.openecomp.appc.flow.controller.utils.FlowControllerConstants;
import org.openecomp.sdnc.sli.SvcLogicContext;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;


public class RestExecutor implements FlowExecutorInterface {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(RestExecutor.class);
    private static final String SDNC_CONFIG_DIR_VAR = "SDNC_CONFIG_DIR";
    Properties props = new Properties();
    public RestExecutor() throws Exception {    
        String propDir = System.getenv(SDNC_CONFIG_DIR_VAR);
        if (propDir == null)
            throw new Exception(" Cannot find Property file -" + SDNC_CONFIG_DIR_VAR);
        String propFile = propDir + FlowControllerConstants.APPC_FLOW_CONTROLLER;
        InputStream propStream = new FileInputStream(propFile);
        try{
            props.load(propStream);
        }
        catch (Exception e){
            throw new Exception("Could not load properties file " + propFile, e);
        }
        finally{
            try{
                propStream.close();
            }
            catch (Exception e){
                log.warn("Could not close FileInputStream", e);
            }
        }        
    }
    @Override
    public HashMap<String, String> execute(Transaction transaction, SvcLogicContext ctx) throws Exception{    
        log.info("Configuring Rest Operation....." + transaction.toString());
        Response response = new Response();
        HashMap<String, String> outputMessage = new HashMap<String, String>();
        Client client = null;
        WebResource webResource = null;
        ClientResponse clientResponse = null;
        String responseDataType=MediaType.APPLICATION_JSON;
        String requestDataType=MediaType.APPLICATION_JSON;
        
        
        try{
            DefaultClientConfig defaultClientConfig = new DefaultClientConfig();
            System.setProperty("jsse.enableSNIExtension", "false");
            SSLContext sslContext = null;
            SecureRestClientTrustManager secureRestClientTrustManager = new SecureRestClientTrustManager();
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new javax.net.ssl.TrustManager[] { secureRestClientTrustManager }, null);
            defaultClientConfig.getProperties().put(
                    com.sun.jersey.client.urlconnection.HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
                    new com.sun.jersey.client.urlconnection.HTTPSProperties(getHostnameVerifier(), sslContext));
            client = Client.create(defaultClientConfig);
            client.addFilter(new HTTPBasicAuthFilter(transaction.getuId(), transaction.getPswd()));
            webResource = client.resource(new URI(transaction.getExecutionEndPoint()));
            webResource.setProperty("Content-Type", "application/json;charset=UTF-8");

            log.info("Starting Rest Operation.....");
            if(HttpMethod.GET.equalsIgnoreCase(transaction.getExecutionRPC())){
                clientResponse = webResource.accept(responseDataType).get(ClientResponse.class);
            }else if(HttpMethod.POST.equalsIgnoreCase(transaction.getExecutionRPC())){
                clientResponse = webResource.type(requestDataType).post(ClientResponse.class, transaction.getPayload());
            }else if(HttpMethod.PUT.equalsIgnoreCase(transaction.getExecutionRPC())){
                clientResponse = webResource.type(requestDataType).put(ClientResponse.class,transaction.getPayload());
            }else if(HttpMethod.DELETE.equalsIgnoreCase(transaction.getExecutionRPC())){
                clientResponse = webResource.delete(ClientResponse.class);
            }

            if(clientResponse.getStatus() == 200){
                response.setResponseCode(String.valueOf(clientResponse.getStatus()));
                ArrayList<Response> responses = new ArrayList<Response>();
                responses.add(response);
                transaction.setResponses(responses);    
                outputMessage.put("restResponse", clientResponse.getEntity(String.class));
            }
            else{
                throw new Exception("Can not determine the state of : " + transaction.getActionLevel()  + " HTTP error code : "
                        + clientResponse.getStatus());
                
            }
            
            log.info("Completed Rest Operation.....");

        }catch (Exception e) {
            e.printStackTrace();
            log.debug("failed in RESTCONT Action ("+transaction.getExecutionRPC()+") for the resource " + transaction.getExecutionEndPoint() + ", falut message :"+e.getMessage());
            throw new Exception("Error While Sending Rest Request" + e.getMessage());
        }
        finally {
            // clean up.
            webResource = null;
            if(client != null){
                client.destroy();
                client = null;
            }
        }

        return outputMessage;
    }
    
private HostnameVerifier getHostnameVerifier() {
    return new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession) {
            return true;
        }
    };
}

}
