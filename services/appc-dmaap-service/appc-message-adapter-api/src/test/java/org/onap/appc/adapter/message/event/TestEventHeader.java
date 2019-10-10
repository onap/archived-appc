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

public class TestEventHeader {
    private EventHeader eventHeader;

    @Before
    public void setUp() {
        eventHeader = new EventHeader("2016-03-15T10:59:33.79Z", "1.01", "<ECOMP_EVENT_ID>");
    }

    @Test
    public void testEventTime() {
        assertNotNull(eventHeader.getEventTime());
        assertNotEquals(eventHeader.getEventTime(), "");
        assertEquals(eventHeader.getEventTime(), "2016-03-15T10:59:33.79Z");
    }

    @Test
    public void testEventApiVer() {
        assertNotNull(eventHeader.getApiVer());
        assertNotEquals(eventHeader.getApiVer(), "");
        assertEquals(eventHeader.getApiVer(), "1.01");
    }

    @Test
    public void testEventId() {
        assertNotNull(eventHeader.getEventId());
        assertNotEquals(eventHeader.getEventId(), "");
        assertEquals(eventHeader.getEventId(), "<ECOMP_EVENT_ID>");
    }

    @Test
    public void testToString_ReturnNonEmptyString() {
        assertNotEquals(eventHeader.toString(), "");
        assertNotEquals(eventHeader.toString(), null);
    }

    @Test
    public void testToString_ContainsString() {
        assertTrue(eventHeader.toString().contains("eventId"));
    }

}
