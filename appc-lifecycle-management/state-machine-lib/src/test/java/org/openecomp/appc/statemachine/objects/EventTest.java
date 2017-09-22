/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.appc.statemachine.objects;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;

public class EventTest {
    private final String EVENT_NAME = "Testing Event";
    private Event event = new Event(EVENT_NAME);

    @Test
    public void testConstructor() {
        Event event = new Event(EVENT_NAME);
        Assert.assertEquals("Should set eventName",
                EVENT_NAME, Whitebox.getInternalState(event, "eventName"));
        Assert.assertEquals("Should set hash code",
                EVENT_NAME.toLowerCase().hashCode(), (int)Whitebox.getInternalState(event, "hashCode"));
    }

    @Test
    public void testHashCode() throws Exception {
        Assert.assertEquals("Should return proper hash code",
                EVENT_NAME.toLowerCase().hashCode(), event.hashCode());
    }

    @Test
    public void testEquals() throws Exception {
        Assert.assertFalse("should return false for null", event.equals(null));
        Assert.assertFalse("should return false for object", event.equals(new State(EVENT_NAME)));
        Assert.assertFalse("should return false for different event",
                event.equals(new Event("Another")));
        Assert.assertTrue("should return true", event.equals(new Event(EVENT_NAME)));
        Assert.assertTrue("should return true (lower case)", event.equals(new Event(EVENT_NAME.toLowerCase())));
    }

    @Test
    public void testGetEventName() throws Exception {
        Assert.assertEquals("Should return EVENT_NAME", EVENT_NAME, event.getEventName());
    }

    @Test
    public void testToString() throws Exception {
        Assert.assertEquals("Should return EVENT_NAME", EVENT_NAME, event.toString());
    }

}
