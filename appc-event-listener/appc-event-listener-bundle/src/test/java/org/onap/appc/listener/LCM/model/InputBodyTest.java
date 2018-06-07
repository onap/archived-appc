/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2018 Nokia Solutions and Networks
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
package org.onap.appc.listener.LCM.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.onap.appc.listener.TestUtil.buildActionIdentifiers;
import static org.onap.appc.listener.TestUtil.buildCommonHeader;

import org.junit.Before;
import org.junit.Test;

public class InputBodyTest {

    private InputBody inputBody;

    @Before
    public void setup() {
        inputBody = new InputBody();
    }

    @Test
    public void should_set_properties() {

        CommonHeader testCommonHeader = buildCommonHeader();
        ActionIdentifiers testActionIdentifiers = buildActionIdentifiers();

        inputBody.setCommonHeader(testCommonHeader);
        inputBody.setActionIdentifiers(testActionIdentifiers);
        inputBody.setAction("test-action");
        inputBody.setPayload("{\"payload\": \"value\"");

        assertEquals(testCommonHeader, inputBody.getCommonHeader());
        assertEquals(testActionIdentifiers, inputBody.getActionIdentifiers());
        assertEquals("test-action", inputBody.getAction());
        assertEquals("{\"payload\": \"value\"", inputBody.getPayload());
    }

    @Test
    public void should_verify_if_is_valid() {

        assertFalse(inputBody.isValid());
        inputBody.setCommonHeader(buildCommonHeader());
        assertTrue(inputBody.isValid());
    }


}
