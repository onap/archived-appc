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

package org.onap.appc.provider.lcm.mock;

import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.status.Status;
import org.onap.appc.domainmodel.lcm.ResponseContext;
import org.onap.appc.provider.lcm.service.AbstractBaseUtils;
import org.onap.appc.requesthandler.objects.RequestHandlerOutput;

import java.util.Properties;

/**
 * Mock Helper abstract class
 * provide common methods for all mock services
 */
public class AbstractMockHelper extends AbstractBaseUtils {
    protected RequestHandlerOutput requestHandlerOutput = new RequestHandlerOutput();
    protected Properties properties = new Properties();
    protected Status status;

    public AbstractMockHelper() {
        requestHandlerOutput.setResponseContext(new ResponseContext());
    }

    /**
     * Get request handling status
     * @return Status of this class
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Set output Status to RequestHandlerOutput and return requestHandlerOutput.
     * @return RequestHandlerOutput
     */
    protected RequestHandlerOutput setOutputStatus() {
        org.onap.appc.domainmodel.lcm.Status outputStatus = new org.onap.appc.domainmodel.lcm.Status();
        outputStatus.setCode(status.getCode());
        outputStatus.setMessage(status.getMessage());
        requestHandlerOutput.getResponseContext().setStatus(outputStatus);
        return requestHandlerOutput;
    }
}
