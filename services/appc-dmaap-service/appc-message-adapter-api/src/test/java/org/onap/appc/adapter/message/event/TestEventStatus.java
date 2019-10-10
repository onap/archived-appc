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
* ============LICENSE_END=========================================================
*/
package org.onap.appc.adapter.message.event;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class TestEventStatus {
    private EventStatus eventStatus;

    @Before
    public void setUp1() {
        eventStatus = new EventStatus(200, "Success");
    }

    @Test
    public void testGetCode() {
        assertNotNull(eventStatus.getCode());
        assertEquals(eventStatus.getCode(),(Integer)200);
    }

    @Test
    public void testGetReason() {
        assertNotNull(eventStatus.getReason());
        assertNotEquals(eventStatus.getReason(), "");
        assertEquals(eventStatus.getReason(), "Success");
    }
    @Test
    public void testToString_ReturnNonEmptyString() {
        assertNotEquals(eventStatus.toString(), "");
        assertNotEquals(eventStatus.toString(), null);
    }

    @Test
    public void testToString_ContainsString() {
        assertTrue(eventStatus.toString().contains("reason"));
    }

}
