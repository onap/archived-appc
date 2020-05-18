/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2019 Ericsson
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

package org.onap.appc.flow.controller.data;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

public class ParametersTest {
    private Parameters param;
    private Parameters param1;
    private Parameters param2;

    @Before
    public void SetUp() {
        param = new Parameters();
        param1 = new Parameters();
        param2 = new Parameters();
    }

    @Test
    public void testSetParamValue() {
        param.setParamValue("1");
        assertNotNull(param.getParamValue());
        assertEquals(param.getParamValue(),"1");
    }

    @Test
    public void testSetParamName() {
        param.setParamName("abc");
        assertNotNull(param.getParamName());
        assertEquals(param.getParamName(),"abc");
    }

    @Test
    public void testHashCode_Print() {
        param.setParamName("2");
        param.setParamValue("def");
        System.out.println("param hashcode is " + param.hashCode());
        assertNotNull(param);
    }

    @Test
    public void testToString() {
        param.setParamName("3");
        param.setParamValue("ghi");
        String ret = param.toString();
        assertFalse("toString is not empty", ret.isEmpty());
    }

    @Test
    public void testEqualsObject() {
        assertTrue(param1.equals(param2) && param2.equals(param1));
        assertFalse(param1.equals(null));
        assertFalse(param1.equals(""));
        param2.setParamName("other_param_name");
        assertFalse(param1.equals(param2));
        param1.setParamName("param_name");
        assertFalse(param1.equals(param2));
        param2.setParamName("param_name");
        param2.setParamValue("other_param_value");
        assertFalse(param1.equals(param2));
        param1.setParamValue("param_value");
        assertFalse(param1.equals(param2));
        param2.setParamValue("param_value");
        assertTrue(param1.equals(param1));
    }
}
