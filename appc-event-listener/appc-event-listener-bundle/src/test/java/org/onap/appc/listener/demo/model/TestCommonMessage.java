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

package org.onap.appc.listener.demo.model;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class TestCommonMessage {

    private CommonMessage commonMessage;
    private CommonMessage.Payload payload;

    @Before
    public void setUp() {
        commonMessage = new CommonMessage();
        payload = new CommonMessage.Payload();
    }

    @Test
    public void testToJson() {
        assertTrue(commonMessage.toJson() instanceof JSONObject);
    }

}
