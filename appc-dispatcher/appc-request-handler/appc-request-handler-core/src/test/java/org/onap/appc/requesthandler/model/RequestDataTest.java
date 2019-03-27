/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 IBM.
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

package org.onap.appc.requesthandler.model;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class RequestDataTest {

    private RequestData requestData;

    @Before
    public void setUp() {
        requestData = new RequestData();
    }

    @Test
    public void testVnfID() {
        requestData.setVnfID("VnfID");
        assertEquals("VnfID", requestData.getVnfID());
    }

    @Test
    public void testCurrentRequest() {
        RequestModel requestModel = new RequestModel();
        requestData.setCurrentRequest(requestModel);
        assertEquals(requestModel, requestData.getCurrentRequest());
    }

}
