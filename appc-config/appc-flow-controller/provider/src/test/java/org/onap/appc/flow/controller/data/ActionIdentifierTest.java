/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
 * =============================================================================
 * Modifications Copyright (C) 2018-2019 IBM.
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
import org.junit.Test;

public class ActionIdentifierTest {

    @Test
    public void testHashCode() {
        ActionIdentifier actionId1 = new ActionIdentifier();
        ActionIdentifier actionId2 = new ActionIdentifier();
        assertTrue(actionId1.hashCode() == actionId2.hashCode());

        if (actionId1.equals(actionId2)) {
            assertTrue(actionId1.hashCode() == actionId2.hashCode());
        }

        actionId2.setVnfcName("vnfcName");
        assertFalse(actionId1.hashCode() == actionId2.hashCode());

        actionId2.setVnfcName("");
        assertTrue(actionId1.hashCode() == actionId2.hashCode());

        actionId2.setVnfId("vnfId");
        assertFalse(actionId1.hashCode() == actionId2.hashCode());

        actionId2.setVnfId("");
        assertTrue(actionId1.hashCode() == actionId2.hashCode());

        actionId2.setVserverId("vserverId");
        assertFalse(actionId1.hashCode() == actionId2.hashCode());

        actionId2.setVserverId("");
        assertTrue(actionId1.hashCode() == actionId2.hashCode());
    }

    @Test
    public void testEquals() {
        ActionIdentifier actionId1 = new ActionIdentifier();
        ActionIdentifier actionId2 = new ActionIdentifier();
        
        actionId1.setVnfcName("vnfcName");
        actionId1.setVnfId("vnfId");
        actionId1.setVserverId("vserverId");
        actionId2.setVnfcName("vnfcName");
        actionId2.setVnfId("vnfId");
        actionId2.setVserverId("vserverId");
        assertTrue(actionId1.equals(actionId2));
        assertTrue(actionId2.equals(actionId1));
    }

    @Test
    public void testSettersAndGetters() {
        ActionIdentifier actionId = new ActionIdentifier();
        actionId.setVserverId("vserverId");
        assertEquals("vserverId", actionId.getVserverId());

        actionId.setVnfcName("vnfcName");
        assertEquals("vnfcName", actionId.getVnfcName());

        actionId.setVnfId("vnfId");
        assertEquals("vnfId", actionId.getVnfId());
    }

    @Test
    public void testtoString() {
        ActionIdentifier actionId = new ActionIdentifier();
        actionId.setVnfcName("vnfcName");
        String ret = actionId.toString();
        assertFalse("toString is not empty", ret.isEmpty());
    }

}
