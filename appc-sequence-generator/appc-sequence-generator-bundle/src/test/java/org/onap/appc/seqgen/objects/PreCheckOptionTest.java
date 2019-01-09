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
package org.onap.appc.seqgen.objects;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PreCheckOptionTest {

    private PreCheckOption precheckoption;

    @Before
    public void SetUp() {
        precheckoption = new PreCheckOption();
    }

    @Test
    public void testSetpTransactionID() {
        precheckoption.setPreTransactionId(Integer.valueOf(1));
        assertNotNull(precheckoption.getPreTransactionId());
        assertEquals(precheckoption.getPreTransactionId(), Integer.valueOf(1));
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
        assertEquals(precheckoption.getParamValue(), "def");
    }
    @Test
    public void testSetRule() {
        precheckoption.setRule("rule");
        assertNotNull(precheckoption.getRule());
        assertEquals(precheckoption.getRule(), "rule");
    }

}