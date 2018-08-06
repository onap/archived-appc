/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 IBM
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

package org.onap.appc.interfaceService.serviceExecutor;

import static org.junit.Assert.*;

import org.junit.Test;
import org.onap.appc.interfaces.service.executor.RequestValidator;

public class TestRequestValidator {

    @Test(expected = Exception.class)
    public void TestRequestDataException() throws Exception{
        String action ="";
        String requestData ="";
        String requestDataType = "";
        RequestValidator.validate(action, requestData, requestDataType);
    }

    @Test(expected = Exception.class)
    public void TestVNFDataException() throws Exception{
        String action ="";
        String requestData = "{\"current-request\" :{\"action\" : \"Audit\",\"action-identifiers\" : {\"service-instance-id\" : \"service-instance-id\",\"vnf-id\" : \"vnf-id\",\"vnfc-name\" : \"vnfc-name\",\"vf-module-id\" : \"vf-module-id\",\"vserver-id\": \"vserver-id\"}},\"in-progress-requests\" :[{\"action\" : \"HealthCheck\",\"action-identifiers\" : {\"service-instance-id\" : \"service-instance-id1\",\"vnf-id\" : \"vnf-id1\",\"vnfc-name\" : \"vnfc-name1\",\"vf-module-id\" : \"vf-module-id\",\"vserver-id\": \"vserver-id1\"}},{\"action\" : \"CheckLock\",\"action-identifiers\" : {\"service-instance-id\" : \"service-instance-id2\",\"vnf-id\" : \"vnf-id2\",\"vnfc-name\" : \"vnfc-name2\",\"vf-module-id\" : \"vf-module-id2\",\"vserver-id\": \"vserver-id2\"}}]}";
        String requestDataType = "";
        RequestValidator.validate(action, requestData, requestDataType);
    }

    @Test(expected = Exception.class)
    public void TestCRException() throws Exception{
        String action ="";
        String requestData = "{\"vnf-id\":\"\"}";
        String requestDataType = "";
        RequestValidator.validate(action, requestData, requestDataType);
    }

    @Test(expected = Exception.class)
    public void TestCRequestAction() throws Exception{
        String action ="";
        String requestData = "{\"vnf-id\":\"\",\"current-request\" :{\"action-identifiers\" : {\"service-instance-id\" : \"service-instance-id\",\"vnf-id\" : \"vnf-id\",\"vnfc-name\" : \"vnfc-name\",\"vf-module-id\" : \"vf-module-id\",\"vserver-id\": \"vserver-id\"}}}}";
        String requestDataType = "";
        RequestValidator.validate(action, requestData, requestDataType);
    }

    @Test(expected = Exception.class)
    public void TestCRequestActionIdentifier() throws Exception{
        String action ="";
        String requestData = "{\"vnf-id\":\"\",\"current-request\" :{\"action\" : \"Audit\"}}}}";
        String requestDataType = "";
        RequestValidator.validate(action, requestData, requestDataType);
    }

    @Test
    public void TestCRequest() throws Exception{
        String action ="";
        String requestData = "{\"vnf-id\":\"\",\"current-request\" :{\"action\" : \"Audit\",\"action-identifiers\" : {\"service-instance-id\" : \"service-instance-id\"}}}}";
        String requestDataType = "";
        RequestValidator.validate(action, requestData, requestDataType);
    }
}
