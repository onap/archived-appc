/*
* ============LICENSE_START=======================================================
* ONAP : APPC
* ================================================================================
* Copyright 2018 TechMahindra
*=================================================================================
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.onap.appc.executor.objects;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class TestParams {

    private Params params;

    @Before
    public void setUp() {
        params = new Params();
    }

    @Test
    public void testGetParams() {
        Map<String, java.lang.Object> paramsMap = new HashMap<String, java.lang.Object>();
        paramsMap.put("paramDgNameSpace", "dg.status.message.param.");
        params.setParams(paramsMap);
        assertNotNull(params.getParams());
        assertEquals(params.getParams().size(), 1);
    }

    @Test
    public void testAddParam() {
        assertEquals(1, params.addParam("paramDgNameSpace", "dg.status.message.param.").getParams().size());
        assertNotNull(params);
    }

    @Test
    public void testTostring() {
        assertTrue(params.toString().contains("Params"));
    }

    @Test
    public void testToString_ReturnNonEmptyString() {
        assertNotEquals(params.toString(), "");
        assertNotEquals(params.toString(), null);

    }
}
