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

package org.openecomp.appc.adapter.chef.chefapi;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.util.EntityUtils;
import org.openecomp.appc.adapter.chef.chefclient.Utils;

import javax.net.ssl.SSLContext;
import java.io.File;
import org.apache.http.HttpEntity;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;

public class ApiMethod {
	private HttpClient client = null;
	protected HttpRequestBase method = null;
	protected HttpResponse response = null;
	protected String reqBody = "";
	protected String userId = "";
	protected String pemPath = "";
	protected String chefPath = "";
	protected String organizations = "";
	protected int resCode=0;
	protected String responseBody="";
	private String methodName = "GET";
	public String test = "";
	private int returnCode;

	public ApiMethod(String methodName) {
		client=HttpClients.createDefault();
		this.methodName = methodName;
	}

	public ApiMethod execute() {
		String hashedPath = Utils.sha1AndBase64("/organizations/"+organizations+chefPath);
		String hashedBody = Utils.sha1AndBase64(reqBody);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		String timeStamp = sdf.format(new Date());
		timeStamp = timeStamp.replace(" ", "T");
		timeStamp = timeStamp + "Z";

		StringBuilder sb = new StringBuilder();
		sb.append("Method:").append(methodName).append("\n");
		sb.append("Hashed Path:").append(hashedPath).append("\n");
		sb.append("X-Ops-Content-Hash:").append(hashedBody).append("\n");
		sb.append("X-Ops-Timestamp:").append(timeStamp).append("\n");
		sb.append("X-Ops-UserId:").append(userId);
		test = test + "sb " + sb + "\n";

		String auth_String = Utils.signWithRSA(sb.toString(), pemPath);
		String[] auth_headers = Utils.splitAs60(auth_String);

		method.addHeader("Content-type", "application/json");
		method.addHeader("X-Ops-Timestamp", timeStamp);
		method.addHeader("X-Ops-Userid", userId);
		method.addHeader("X-Chef-Version", "12.4.1");
		method.addHeader("Accept", "application/json");
		method.addHeader("X-Ops-Content-Hash", hashedBody);
		method.addHeader("X-Ops-Sign", "version=1.0");

		for (int i = 0; i < auth_headers.length; i++) {
			method.addHeader("X-Ops-Authorization-" + (i + 1), auth_headers[i]);
		}
		try{
		response = client.execute(method);
		resCode = response.getStatusLine().getStatusCode();
		HttpEntity entity1 = response.getEntity();
		responseBody = EntityUtils.toString(entity1);}
		catch(Exception ex){
			resCode=500;
			responseBody=ex.getMessage();
		}
		return this;
	}

	public void setHeaders(Header[] headers) {
		for (Header header : headers) {
			this.method.addHeader(header);
		}
	}

	public String getResponseBodyAsString() {
		return responseBody;
	}

	public int getReturnCode() {
		return resCode;
	}

	public String getReqBody() {
		return reqBody;
	}

	public void setReqBody(String body) {
		this.reqBody = body;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPemPath() {
		return pemPath;
	}

	public void setPemPath(String pemPath) {
		this.pemPath = pemPath;
	}
	
	public String getChefPath() {
		return chefPath;
	}

	public void setChefPath(String chefPath) {
		this.chefPath = chefPath;
	}
	
	public String getOrganizations() {
		return organizations;
	}

	public void setOrganizations(String organizations) {
		this.organizations = organizations;
	}
}
