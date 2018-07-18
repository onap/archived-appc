/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 IBM
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
 *
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.flow.executor.node;


import org.onap.appc.flow.controller.dbervices.FlowControlDBService;

public class MockDBService extends FlowControlDBService {
     private static MockDBService mockDgGeneralDBService = null;
        private static MockSvcLogicResource serviceLogic = new MockSvcLogicResource();

        public MockDBService() {
            super(serviceLogic);
            if (mockDgGeneralDBService != null) {
                mockDgGeneralDBService = new MockDBService(serviceLogic);
            }

        }

        public MockDBService(MockSvcLogicResource serviceLogic2) {
            super(serviceLogic);
        }

        public static MockDBService initialise() {
            if (mockDgGeneralDBService == null) {
                mockDgGeneralDBService = new MockDBService(serviceLogic);
            }
            return mockDgGeneralDBService;
        }
}
