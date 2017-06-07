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

package org.openecomp.appc.requesthandler.objects;

import org.openecomp.appc.domainmodel.lcm.RequestContext;

public class RequestHandlerInput {


    private RequestContext requestContext;
	private String rpcName;

    public String getRpcName() {
        return rpcName;
	}

    public void setRpcName(String rpcName) {
        this.rpcName = rpcName;
	}

    public RequestContext getRequestContext() {
        return requestContext;
	}

    public RequestHandlerInput(){
	}


    public void setRequestContext(RequestContext requestContext) {
        this.requestContext = requestContext;
	}

	@Override
	public String toString() {
		return "RequestHandlerInput{" +
                "requestContext=" + requestContext +
				'}';
	}
}
