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

public class PrecheckOptionTest {
    
    private PrecheckOption precheckoption;
    private PrecheckOption precheckoption1;
    private PrecheckOption precheckoption2;

    @Before
    public void SetUp() {
        precheckoption = new PrecheckOption();
        precheckoption1 = new PrecheckOption();
        precheckoption2 = new PrecheckOption();
    }

    @Test
    public void testSetpTransactionID() {
        precheckoption.setpTransactionID(1);
        assertNotNull(precheckoption.getpTransactionID());
        assertEquals(precheckoption.getpTransactionID(), 1);
    }

    @Test
    public void testSetParamName() {
        precheckoption.setParamName("abc");
        assertNotNull(precheckoption.getParamName());
        assertEquals(precheckoption.getParamName(), "abc");
    }

    @Test
    public void testSetParamValue() {
        precheckoption.setParamValue("def");
        assertNotNull(precheckoption.getParamValue());
        assertEquals(precheckoption.getParamValue(),"def");
    }

    @Test
    public void testToString() {
        precheckoption.setParamName("abc");
        precheckoption.setParamValue("ghi");
        precheckoption.setpTransactionID(1);
        precheckoption.setRule("jkl");
        assertFalse("toString is not empty", precheckoption.toString().isEmpty());
    }

    @Test
    public void testSetRule() {
        precheckoption.setRule("abc");
        assertNotNull(precheckoption.getRule());
        assertEquals(precheckoption.getRule(), "abc");
    }

    @Test
    public void testHashCode_Print() {
        precheckoption.setpTransactionID(2);
        precheckoption.setParamName("abc");
        precheckoption.setParamValue("def");
        precheckoption.setRule("jkl");
        assertNotNull(precheckoption.hashCode());
    }

    @Test
    public void testEqualsObject() {
        assertTrue(precheckoption1.equals(precheckoption2));
        assertTrue(precheckoption1.equals(precheckoption1));
        assertFalse(precheckoption1.equals(null));
        assertFalse(precheckoption1.equals(""));
        precheckoption1.setpTransactionID(1);
        assertFalse(precheckoption1.equals(precheckoption2));
        precheckoption2.setpTransactionID(1);
        precheckoption2.setParamName("other_param_name");
        assertFalse(precheckoption1.equals(precheckoption2));
        precheckoption1.setParamName("param_name");
        assertFalse(precheckoption1.equals(precheckoption2));
        precheckoption2.setParamName("param_name");
        precheckoption2.setParamValue("other_param_value");
        assertFalse(precheckoption1.equals(precheckoption2));
        precheckoption1.setParamValue("param_value");
        assertFalse(precheckoption1.equals(precheckoption2));
        precheckoption2.setParamValue("param_value");
        precheckoption1.setRule("other_rule");
        assertFalse(precheckoption1.equals(precheckoption2));
    }
}
