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

package org.openecomp.appc.workflow.objects;

import org.openecomp.appc.domainmodel.lcm.RequestContext;
import org.openecomp.appc.domainmodel.lcm.ResponseContext;
import org.openecomp.appc.domainmodel.lcm.VNFContext;


public class WorkflowRequest {

    private RequestContext requestContext;
    private ResponseContext responseContext;
    private VNFContext vnfContext;

    public RequestContext getRequestContext() {
        return requestContext;
	}

    public void setRequestContext(RequestContext requestContext) {
        this.requestContext = requestContext;
	}

    public ResponseContext getResponseContext() {
        return responseContext;
	}

    public void setResponseContext(ResponseContext responseContext) {
        this.responseContext = responseContext;
	}

    public VNFContext getVnfContext() {
        return vnfContext;
	}

    public void setVnfContext(VNFContext vnfContext) {
        this.vnfContext = vnfContext;
	}

	@Override
	public String toString() {
		return "WorkflowRequest{" +
                "requestContext=" + requestContext +
                ", responseContext=" + responseContext +
                ", vnfContext=" + vnfContext +
				'}';
	}
}
