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

import org.junit.Assert;
import org.junit.Test;

public class EventMessageTest {

    private EventHeader eventHeader = new EventHeader("2016-03-15T10:59:33.79Z", "1.01", "<ECOMP_EVENT_ID>");
    private EventStatus eventStatus = new EventStatus(200, "Success");
    private EventMessage eventMessage = new EventMessage(eventHeader, eventStatus);

    @Test
    public void testGetEventHeader() {
        eventMessage.setEventHeader(eventHeader);
        Assert.assertEquals(new EventHeader("2016-03-15T10:59:33.79Z", "1.01", "<ECOMP_EVENT_ID>").getEventId(),
                eventMessage.getEventHeader().getEventId());

    }

    @Test
    public void testGetEventStatus() {
        eventMessage.setEventStatus(eventStatus);
        Assert.assertEquals(new EventStatus(200, "Success").getCode(), eventMessage.getEventStatus().getCode());
    }

    @Test
    public void testToJson_Equal() {
        EventMessage message = new EventMessage(eventHeader, eventStatus);
        Assert.assertEquals(message.toJson(), eventMessage.toJson());

    }

    @Test
    public void testToJson_NotEqual() {
        EventMessage message = new EventMessage(new EventHeader("2016-03-15T10:59:33.79Z", "1.01", "<ECOMP_EVENT>"),
                new EventStatus(200, "Succs"));
        Assert.assertNotEquals(message.toJson(), eventMessage.toJson());
    }

    @Test
    public void testToString_ReturnNonEmptyString() {
        Assert.assertNotEquals(eventMessage.toString(), "");
    }

    @Test
    public void testToString_ContainsString() {
        Assert.assertTrue(eventMessage.toString().contains("EventMessage"));
    }
}
