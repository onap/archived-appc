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

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.onap.appc.domainmodel.lcm.VNFOperation;
import org.onap.appc.provider.lcm.mock.query.MockQueryHelper;
import org.onap.appc.requesthandler.objects.RequestHandlerInput;
import org.onap.appc.requesthandler.objects.RequestHandlerOutput;

/**
 * Mock Request executor which mocks backend implementation
 * for the set of LCM commands which do not have backend support.
 */
public class MockRequestExecutor {

    private final EELFLogger logger = EELFManager.getInstance().getLogger(MockRequestExecutor.class);

    /**
     * Execute the request.
     * @param request of the RequestHandlerInput
     * @return RequestHandlerOutput if mock is supported, otherwise return null.
     */
    public RequestHandlerOutput executeRequest(RequestHandlerInput requestHandlerInput) {
		VNFOperation vnfOperation = requestHandlerInput.getRequestContext().getAction();
        switch (vnfOperation) {
             case Query:
                logger.debug("Proceed with mock helper for query VNF");
                return new MockQueryHelper().query(requestHandlerInput);
            default:
                // do nothing
        }
        return null;
    }
}
