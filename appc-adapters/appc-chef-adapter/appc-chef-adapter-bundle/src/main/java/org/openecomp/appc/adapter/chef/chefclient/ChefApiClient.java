/*-
 * ============LICENSE_START=======================================================
 * openECOMP : APP-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 						reserved.
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

package org.openecomp.appc.adapter.chef.chefclient;
import org.apache.http.client.methods.*;
import org.openecomp.appc.adapter.chef.chefapi.*;

public class ChefApiClient {
	private String endpoint;
	private String userId;
	private String pemPath;
	private String organizations;

	

	 
	/**
	 *
	 * @param userId user name correspond to the pem key
	 * @param pemPath path of the auth key
	 * @param endpoint chef api server address
	 */
	public ChefApiClient(String userId, String pemPath, String endpoint,String organizations){
		this.userId = userId;
		this.pemPath = pemPath;
		this.endpoint = endpoint;
		this.organizations=organizations;
	}

	/**
	 *
	 * @param path in the endpoint. e.g /clients
	 * @return
	 */
	public Get get(String path){
		Get get = new Get(new HttpGet(endpoint+path));
		get.setPemPath(pemPath);
		get.setUserId(userId);
		get.setOrganizations(organizations);
		get.setChefPath(path);
		return get;
	}

	public Put put(String path){
		Put put = new Put(new HttpPut(endpoint+path));
		put.setPemPath(pemPath);
		put.setUserId(userId);
		put.setOrganizations(organizations);
		put.setChefPath(path);
		return put;
   }
}
