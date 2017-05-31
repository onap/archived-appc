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

package org.openecomp.appc.adapter.rest.impl;

import java.net.URI;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.openecomp.appc.Constants;
import org.openecomp.appc.adapter.rest.RestAdapter;
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
import com.att.cdp.zones.model.Server.Status;
import com.att.cdp.zones.model.ServerBootSource;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import org.openecomp.sdnc.sli.SvcLogicContext;

import org.glassfish.grizzly.http.util.HttpStatus;
import org.slf4j.MDC;

import java.net.InetAddress;
import java.util.Locale;
import java.util.UUID;
import static com.att.eelf.configuration.Configuration.*;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.util.EntityUtils;
import java.io.IOException;
import org.apache.http.entity.StringEntity;

import java.net.InetAddress;
import org.json.*;

/**
 * This class implements the {@link RestAdapter} interface. This interface
 * defines the behaviors that our service provides.
 */
public class RestAdapterImpl implements RestAdapter {

	/**
	 * The constant used to define the adapter name in the mapped diagnostic
	 * context
	 */
	

	@SuppressWarnings("nls")
	public static final String MDC_ADAPTER = "adapter";

	/**
	 * The constant used to define the service name in the mapped diagnostic
	 * context
	 */
	@SuppressWarnings("nls")
	public static final String MDC_SERVICE = "service";

	/**
	 * The constant for the status code for a failed outcome
	 */
	@SuppressWarnings("nls")
	public static final String OUTCOME_FAILURE = "failure";

	/**
	 * The constant for the status code for a successful outcome
	 */
	@SuppressWarnings("nls")
	public static final String OUTCOME_SUCCESS = "success";

	/**
	 * A constant for the property token "provider" used in the structured
	 * property specifications
	 */
	@SuppressWarnings("nls")
	public static final String PROPERTY_PROVIDER = "provider";

	/**
	 * A constant for the property token "identity" used in the structured
	 * property specifications
	 */
	@SuppressWarnings("nls")
	public static final String PROPERTY_PROVIDER_IDENTITY = "identity";

	/**
	 * A constant for the property token "name" used in the structured property
	 * specifications
	 */
	@SuppressWarnings("nls")
	public static final String PROPERTY_PROVIDER_NAME = "name";

	/**
	 * A constant for the property token "tenant" used in the structured
	 * property specifications
	 */
	@SuppressWarnings("nls")
	public static final String PROPERTY_PROVIDER_TENANT = "tenant";

	/**
	 * A constant for the property token "tenant name" used in the structured
	 * property specifications
	 */
	@SuppressWarnings("nls")
	public static final String PROPERTY_PROVIDER_TENANT_NAME = "name";

	/**
	 * A constant for the property token "password" used in the structured
	 * property specifications
	 */
	@SuppressWarnings("nls")
	public static final String PROPERTY_PROVIDER_TENANT_PASSWORD = "password"; // NOSONAR

	/**
	 * A constant for the property token "userid" used in the structured
	 * property specifications
	 */
	@SuppressWarnings("nls")
	public static final String PROPERTY_PROVIDER_TENANT_USERID = "userid";

	/**
	 * A constant for the property token "type" used in the structured property
	 * specifications
	 */
	@SuppressWarnings("nls")
	public static final String PROPERTY_PROVIDER_TYPE = "type";

	/**
	 * The name of the service to restart a server
	 */
	@SuppressWarnings("nls")
	public static final String PING_SERVICE = "pingServer";

	/**
	 * The logger to be used
	 */
	private static final EELFLogger logger = EELFManager.getInstance().getLogger(RestAdapterImpl.class);

	/**
	 * The constant for a left parenthesis
	 */
	private static final char LPAREN = '(';

	/**
	 * The constant for a new line control code
	 */
	private static final char NL = '\n';

	/**
	 * The constant for a single quote
	 */
	private static final char QUOTE = '\'';

	/**
	 * The constant for a right parenthesis
	 */
	private static final char RPAREN = ')';

	/**
	 * The constant for a space
	 */
	private static final char SPACE = ' ';

	/**
	 * A reference to the adapter configuration object.
	 */
	private Configuration configuration;

	/**
	 * A cache of providers that are predefined.
	 */
	// private Map<String /* provider name */, ProviderCache> providerCache;

	/**
	 * This default constructor is used as a work around because the activator
	 * wasnt getting called
	 */
	/**
	 * A cache of providers that are predefined.
	 */
	// private Map<String /* provider name */, ProviderCache> providerCache;

	/**
	 * This default constructor is used as a work around because the activator
	 * wasnt getting called
	 */
	public RestAdapterImpl() {
		initialize();

	}

	/**
	 * This constructor is used primarily in the test cases to bypass
	 * initialization of the adapter for isolated, disconnected testing
	 * 
	 * @param initialize
	 *            True if the adapter is to be initialized, can false if not
	 */
	public RestAdapterImpl(boolean initialize) {
		configuration = ConfigurationFactory.getConfiguration();
		if (initialize) {
			initialize();

		}
	}

	/**
	 * @param props
	 *            not used
	 */
	public RestAdapterImpl(Properties props) {
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

	public HttpRequestBase addHeaders(HttpRequestBase method,String headers){
		if(headers.length()==0)
			{
				return method;
			}else{
				JSONObject JsonHeaders= new JSONObject(headers);
				Iterator keys = JsonHeaders.keys();
				while(keys.hasNext()) {
			        String String1 = (String)keys.next();
			        String String2 = JsonHeaders.getString(String1);
			        method.addHeader(String1,String2);
			    }
				return method;
			}
	}
	
	
	public void commonGet(Map<String, String> params, SvcLogicContext ctx) {
		logger.info("Run get method");
		String haveHeader="false";
		String tUrl=params.get("org.openecomp.appc.instance.URI");
		haveHeader=params.get("org.openecomp.appc.instance.haveHeader");
		String headers=params.get("org.openecomp.appc.instance.headers");
		RequestContext rc = new RequestContext(ctx);
		rc.isAlive();
	
		try {
			HttpGet httpGet = new HttpGet(tUrl);
			
			if(haveHeader.equals("true"))
			{
				JSONObject JsonHeaders= new JSONObject(headers);
				Iterator keys = JsonHeaders.keys();
				while(keys.hasNext()) {
			        String String1 = (String)keys.next();
			        String String2 = JsonHeaders.getString(String1);
			        httpGet.addHeader(String1,String2);
			    }

			}
			
			HttpClient httpClient = HttpClients.createDefault();
			HttpResponse response = null;
			response = httpClient.execute(httpGet);
			int responseCode=response.getStatusLine().getStatusCode();
			HttpEntity entity = response.getEntity();
			String responseOutput=EntityUtils.toString(entity);
			doSuccess(rc,responseCode,responseOutput);
		} catch (Exception ex) {
			doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, ex.toString());
		}
	}

	
	public void commonDelete(Map<String, String> params, SvcLogicContext ctx) {
		logger.info("Run Delete method");
		String haveHeader="false";
		String tUrl=params.get("org.openecomp.appc.instance.URI");
		haveHeader=params.get("org.openecomp.appc.instance.haveHeader");
		String headers=params.get("org.openecomp.appc.instance.headers");
		RequestContext rc = new RequestContext(ctx);
		rc.isAlive();

		try {
			HttpDelete httpDelete = new HttpDelete(tUrl);
			if(haveHeader.equals("true"))
			{
				JSONObject JsonHeaders= new JSONObject(headers);
				Iterator keys = JsonHeaders.keys();
				while(keys.hasNext()) {
			        String String1 = (String)keys.next();
			        String String2 = JsonHeaders.getString(String1);
			        httpDelete.addHeader(String1,String2);
			    }

			}
			HttpClient httpClient = HttpClients.createDefault();
			HttpResponse response = null;
			response = httpClient.execute(httpDelete);
			int responseCode=response.getStatusLine().getStatusCode();
			HttpEntity entity = response.getEntity();
			String responseOutput=EntityUtils.toString(entity);
			doSuccess(rc,responseCode,responseOutput);
		} catch (Exception ex) {
			doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, ex.toString());
		}
	}
	
	
	
	public void commonPost(Map<String, String> params, SvcLogicContext ctx) {
		logger.info("Run post method");
		String haveHeader="false";
		String tUrl=params.get("org.openecomp.appc.instance.URI");
		String body=params.get("org.openecomp.appc.instance.requestBody");
		haveHeader=params.get("org.openecomp.appc.instance.haveHeader");
		String headers=params.get("org.openecomp.appc.instance.headers");
		RequestContext rc = new RequestContext(ctx);
		rc.isAlive();
		
		try {
			HttpPost httpPost = new HttpPost(tUrl);
			if(haveHeader.equals("true"))
			{
				JSONObject JsonHeaders= new JSONObject(headers);
				Iterator keys = JsonHeaders.keys();
				while(keys.hasNext()) {
			        String String1 = (String)keys.next();
			        String String2 = JsonHeaders.getString(String1);
			        httpPost.addHeader(String1,String2);
			    }

			}
			StringEntity bodyParams =new StringEntity (body,"UTF-8");
			httpPost.setEntity(bodyParams);
			HttpClient httpClient = HttpClients.createDefault();
			HttpResponse response = null;
			response = httpClient.execute(httpPost);
			int responseCode=response.getStatusLine().getStatusCode();
			HttpEntity entity = response.getEntity();
			String responseOutput=EntityUtils.toString(entity);
			doSuccess(rc,responseCode,responseOutput);
		} catch (Exception ex) {
			doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, ex.toString());
		}
	}
	
	public void commonPut(Map<String, String> params, SvcLogicContext ctx) {
		logger.info("Run put method");
		String haveHeader="false";
		String tUrl=params.get("org.openecomp.appc.instance.URI");
		String body=params.get("org.openecomp.appc.instance.requestBody");
		haveHeader=params.get("org.openecomp.appc.instance.haveHeader");
		String headers=params.get("org.openecomp.appc.instance.headers");
		RequestContext rc = new RequestContext(ctx);
		rc.isAlive();
		
	try {
			HttpPut httpPut = new HttpPut(tUrl);
			if(haveHeader.equals("true"))
			{
				JSONObject JsonHeaders= new JSONObject(headers);
				Iterator keys = JsonHeaders.keys();
				while(keys.hasNext()) {
			        String String1 = (String)keys.next();
			        String String2 = JsonHeaders.getString(String1);
			        httpPut.addHeader(String1,String2);
			    }

			}
			StringEntity bodyParams =new StringEntity (body,"UTF-8");
			httpPut.setEntity(bodyParams);
			HttpClient httpClient = HttpClients.createDefault();
			HttpResponse response = null;
			response = httpClient.execute(httpPut);
			int responseCode=response.getStatusLine().getStatusCode();
			HttpEntity entity = response.getEntity();
			String responseOutput=EntityUtils.toString(entity);
			if(responseCode == 200){
			doSuccess(rc,responseCode,responseOutput);
			} else {
				doFailure(rc, HttpStatus.getHttpStatus(responseCode), response.getStatusLine().getReasonPhrase());
			}
			} 
			catch (Exception ex) {
				doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, ex.toString());
			}
/*		} catch (Exception ex) {
			doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, ex.getMessage());
		}*/
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
	

	/**
	 * initialize the provider adapter by building the context cache
	 */
	private void initialize() {
		configuration = ConfigurationFactory.getConfiguration();
	
		logger.info("init rest adapter!!!!!");
	}

}
