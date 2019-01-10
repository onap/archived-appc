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

import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;

public class RequestInfoTest {

    private RequestInfo requestInfo;

    @Before
    public void setUp() {
        requestInfo = new RequestInfo();
    }

    @Test
    public void get_set_Action() {
        String action = "some_action";
        requestInfo.setAction(action);
        assertEquals(action, requestInfo.getAction());
    }

    @Test
    public void get_set_Action_Level() {
        String actionLevel = "some_action_level";
        requestInfo.setActionLevel(actionLevel);
        assertEquals(actionLevel, requestInfo.getActionLevel());
    }

    @Test
    public void get_set_Action_Identifier() {
        ActionIdentifier actionIdentifier = mock(ActionIdentifier.class);
        requestInfo.setActionIdentifier(actionIdentifier);
        assertEquals(actionIdentifier, requestInfo.getActionIdentifier());
    }

    @Test
    public void get_set_Payload() {
        String payload = "some_payload";
        requestInfo.setPayload(payload);
        assertEquals(payload, requestInfo.getPayload());
    }

}
