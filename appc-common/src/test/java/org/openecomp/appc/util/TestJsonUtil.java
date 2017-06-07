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

package org.openecomp.appc.util;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.appc.util.JsonUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.*;



public class TestJsonUtil {

    @Test
    public void testConvertJsonStringToFlatMap() {
        try {
            String jsonString = "{\"A\":\"A-value\",\"B\":{\"C\":\"B.C-value\",\"D\":\"B.D-value\"}}";
            Map<String, String> flatMap = JsonUtil.convertJsonStringToFlatMap(jsonString);
            assertNotNull(flatMap);
            Map<String, String> expectedMap = new HashMap<>();
            expectedMap.put("A","A-value");
            expectedMap.put("B.C","B.C-value");
            expectedMap.put("B.D","B.D-value");
            assertEquals(expectedMap,flatMap);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail(e.toString());
        }
    }

    @Test
    public void testConvertJsonStringToFlatMapWithInnerJson() {
        try {
            String jsonString = "{\"A\":\"A-value\",\"B\":\"{\\\"C\\\":\\\"C-value\\\",\\\"D\\\":\\\"D-value\\\"}\"}";
            Map<String, String> flatMap = JsonUtil.convertJsonStringToFlatMap(jsonString);
            assertNotNull(flatMap);
            Map<String, String> expectedMap = new HashMap<>();
            expectedMap.put("A","A-value");
            expectedMap.put("B","{\"C\":\"C-value\",\"D\":\"D-value\"}");
            assertEquals(expectedMap,flatMap);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail(e.toString());
        }
    }
}
