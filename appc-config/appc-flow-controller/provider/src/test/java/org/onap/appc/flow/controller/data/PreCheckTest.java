/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
 * =============================================================================
 * Modifications Copyright (C) 2018 IBM.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class PreCheckTest {
    
    private PreCheck preCheck;
    private PreCheck preCheck1;
    private PreCheck preCheck2;

    @Before
    public void SetUp() {
        preCheck = new PreCheck();
        preCheck1 = new PreCheck();
        preCheck2 = new PreCheck();
    }

    @Test
    public void testSetPrecheckOperator() {
        preCheck.setPrecheckOperator("op");
        assertNotNull(preCheck.getPrecheckOperator());
        assertEquals(preCheck.getPrecheckOperator(),"op");
    }

    @Test
    public void testSetPrecheckOptions() {
        List<PrecheckOption> precheckOptionList = new LinkedList<>();
        preCheck.setPrecheckOptions(precheckOptionList);
        assertNotNull(preCheck.getPrecheckOptions());
        assertEquals(preCheck.getPrecheckOptions(), precheckOptionList);
    }

    @Test
    public void testHashCode() {
        preCheck.setPrecheckOperator("1");
        System.out.println(" precheck hashcode is "  + preCheck.hashCode());
    }

    @Test
    public void testToString() {
        preCheck.setPrecheckOperator("A");
        List<PrecheckOption> precheckOptionList = new LinkedList<>();
        preCheck.setPrecheckOptions(precheckOptionList);
        String ret = preCheck.toString();
        System.out.println("ret is " + ret);

    }
    
    @Test
    public void testEqualsObject() {
        assertTrue(preCheck1.equals(preCheck2) && preCheck2.equals(preCheck1));
        assertTrue(preCheck1.equals(preCheck1));
        assertFalse(preCheck1.equals(null));
    }

}
