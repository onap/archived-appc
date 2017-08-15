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

package org.openecomp.appc.instar.dme2client;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Properties;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.openecomp.appc.instar.utils.InstarClientConstant;


import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;


public class Dme2Client {

	private static final EELFLogger log = EELFManager.getInstance().getLogger(Dme2Client.class);
	private static final String SDNC_CONFIG_DIR_VAR = "SDNC_CONFIG_DIR";
	//DME2Client client = null;
	Properties props = new Properties();
	String operationName ;
	String appendContext; 

	public Dme2Client(String optName, String subCtxt, HashMap<String, String> data) throws Exception{
		log.info("Setting Properties for DME2 Client for INSTAR connection");
		this.operationName=optName;
		this.appendContext = data.get(subCtxt);
		String propDir = System.getenv(SDNC_CONFIG_DIR_VAR);
		if (propDir == null)
			throw new Exception(" Cannot find Property file -" + SDNC_CONFIG_DIR_VAR);
		String propFile = propDir + InstarClientConstant.OUTBOUND_PROPERTIES;
		InputStream propStream = new FileInputStream(propFile);
		try
		{
			props.load(propStream);
		}
		catch (Exception e)
		{
			throw new Exception("Could not load properties file " + propFile, e);
		}
		finally
		{
			try
			{
				propStream.close();
			}
			catch (Exception e)
			{
				log.warn("Could not close FileInputStream", e);
			}
		}
	}

	public  ClientResponse  sendtoInstar() throws Exception {

		log.info("Called Send with operation Name=" + this.operationName + "and = " + props.getProperty(operationName+InstarClientConstant.BASE_URL));
		String resourceUri = props.getProperty(operationName+InstarClientConstant.BASE_URL)+ 
				props.getProperty(operationName + InstarClientConstant.URL_SUFFIX)  + 
				props.getProperty(operationName + InstarClientConstant.SUB_CONTEXT)+ appendContext ;           

		log.info("DME Endpoint URI:" + resourceUri);     
		Client client = null;
		WebResource webResource = null;
		ClientResponse clientResponse = null;
		String authorization = props.getProperty("authorization");
		String requestDataType = "application/json";
		String responseDataType=	MediaType.APPLICATION_JSON;
		String methodType =  props.getProperty("getIpAddressByVnf_method");
		String request = "";
		String userId=props.getProperty("MechID");
		String password=props.getProperty("MechPass");
		
		log.info("authorization = " + authorization + "methodType= " + methodType);
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
			client.addFilter(new HTTPBasicAuthFilter(userId, password));

			webResource = client.resource(new URI(resourceUri));
			webResource.setProperty("Content-Type", "application/json;charset=UTF-8");

			if(HttpMethod.GET.equalsIgnoreCase(methodType)){
				clientResponse = webResource.accept(responseDataType).get(ClientResponse.class);
			}else if(HttpMethod.POST.equalsIgnoreCase(methodType)){
				clientResponse = webResource.type(requestDataType).post(ClientResponse.class, request);
			}else if(HttpMethod.PUT.equalsIgnoreCase(methodType)){
				clientResponse = webResource.type(requestDataType).put(ClientResponse.class,request);
			}else if(HttpMethod.DELETE.equalsIgnoreCase(methodType)){
				clientResponse = webResource.delete(ClientResponse.class);
			}

			return clientResponse;

		}catch (Exception e) {
			log.info("failed in RESTCONT Action ("+methodType+") for the resource " + resourceUri + ", falut message :"+e.getMessage());
			throw new Exception("Error While gettting Data from INSTAR" + e.getMessage());
		}
		finally {
			// clean up.
			webResource = null;
			if(client != null){
				client.destroy();
				client = null;
			}
		}


	}

	public String send() {
		String response = null;
		try{

			if(props !=null && 
					props.getProperty(InstarClientConstant.MOCK_INSTAR) != null &&
					props.getProperty(InstarClientConstant.MOCK_INSTAR).equalsIgnoreCase("true"))
				return  IOUtils.toString(Dme2Client.class.getClassLoader().getResourceAsStream("/tmp/sampleResponse"), Charset.defaultCharset());

			ClientResponse clientResponse = sendtoInstar();
			if(clientResponse != null){
				response = clientResponse.getEntity(String.class);
				log.info(clientResponse.getStatus() + " Status, Response :" + response);

			}
		} catch (Exception t) {
			t.printStackTrace();
		}
		return response;
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
