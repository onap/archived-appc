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

package org.onap.sdnc.config.audit.node;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class TestParameters {
    private Parameters parameters;

    @Before
    public void setUp() {
        Map<String, String> inParams = new HashMap<>();
        inParams.put("compareType", "testcompareType");
        inParams.put("compareDataType", "testcompareDataType");
        inParams.put("sourceData", "testsourceData");
        inParams.put("targetData", "testtargetData");
        inParams.put("sourceDataType", "testsourceDataType");
        inParams.put("targetDataType", "testtargetDataType");
        inParams.put("requestIdentifier", "testrequestIdentifier");
        parameters = new Parameters(inParams);
    }

    @Test
    public void testPayloadX() {
        parameters.setPayloadX("PayloadX");
        assertEquals("PayloadX", parameters.getPayloadX());
    }

    @Test
    public void testPayloadXtype() {
        parameters.setPayloadXtype("PayloadXtype");
        assertEquals("PayloadXtype", parameters.getPayloadXtype());
    }

    @Test
    public void testPayloadY() {
        parameters.setPayloadY("PayloadY");
        assertEquals("PayloadY", parameters.getPayloadY());
    }

    @Test
    public void testPayloadYtype() {
        parameters.setPayloadYtype("PayloadYtype");
        assertEquals("PayloadYtype", parameters.getPayloadYtype());
    }

    @Test
    public void testCompareDataType() {
        parameters.setCompareDataType("CompareDataType");
        assertEquals("CompareDataType", parameters.getCompareDataType());
    }

    @Test
    public void testCompareType() {
        parameters.setCompareType("CompareType");
        assertEquals("CompareType", parameters.getCompareType());
    }
    
    @Test
    public void testRequestIdentifier() {
        parameters.setRequestIdentifier("RequestIdentifier");
        assertEquals("RequestIdentifier", parameters.getRequestIdentifier());
    }

}
