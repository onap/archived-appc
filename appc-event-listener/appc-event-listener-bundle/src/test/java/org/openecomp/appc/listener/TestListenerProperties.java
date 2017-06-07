/*-
 * ============LICENSE_START=======================================================
 * APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Amdocs
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
 * ============LICENSE_END=========================================================
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */

package org.openecomp.appc.listener;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.appc.adapter.factory.MessageService;
import org.openecomp.appc.listener.AbstractListener;
import org.openecomp.appc.listener.ListenerProperties;
import org.openecomp.appc.listener.ListenerProperties.KEYS;

public class TestListenerProperties {

    private Properties good, bad, both;
    private String prefix;

    private ListenerProperties props;

    @Before
    public void setup() {
        prefix = "test";
        good = new Properties();
        bad = new Properties();
        both = new Properties();

        good.setProperty(String.format("%s.%s", prefix, "a"), "1");
        good.setProperty(String.format("%s.%s", prefix, "a.b"), "2");
        good.setProperty(String.format("%s.%s", prefix, "a.b.c"), "3");

        bad.setProperty(prefix, "NA");
        bad.setProperty(prefix + ".", "NA");
        bad.setProperty(String.format("%s.%s", prefix + "x", "bad"), "NA");
        bad.setProperty(String.format("%s.%s", "x" + prefix, "bad"), "NA");

        for (String key : good.stringPropertyNames()) {
            both.put(key, good.getProperty(key));
        }
        for (String key : bad.stringPropertyNames()) {
            both.put(key, bad.getProperty(key));
        }

        props = new ListenerProperties(prefix, both);
    }

    @Test
    public void testConstructor() {
        props = new ListenerProperties(prefix, good);
        assertEquals(prefix, props.getPrefix());
        assertEquals(good.size(), props.getProperties().size());

        props = new ListenerProperties(prefix, bad);
        assertEquals(prefix, props.getPrefix());
        assertTrue(props.getProperties().isEmpty());

        props = new ListenerProperties(prefix, both);
        assertEquals(prefix, props.getPrefix());
        assertEquals(good.size(), props.getProperties().size());

        for (Object val : props.getProperties().values()) {
            assertFalse("NA".equals(val.toString()));
        }

        assertTrue(props.toString().contains(prefix));
    }

    @Test
    public void testGetClass() {
        assertNull(props.getListenerClass());
        props.setListenerClass(AbstractListener.class);
        assertNotNull(props.getListenerClass());
        assertEquals(AbstractListener.class, props.getListenerClass());
    }

    @Test
    public void testMessageServices() {
        // Hardcode count so tests must be updated when values are added
        assertEquals(1, MessageService.values().length);

        // Bad Input
        MessageService def = MessageService.DMaaP;
        assertEquals(def, MessageService.parse(null));
        assertEquals(def, MessageService.parse(""));
        assertEquals(def, MessageService.parse("NotDMaaP"));
       
        // DMaaP case sensitivity
        assertEquals(MessageService.DMaaP, MessageService.parse("dmaap"));
        assertEquals(MessageService.DMaaP, MessageService.parse("DMAAP"));
        assertEquals(MessageService.DMaaP, MessageService.parse("DMaaP"));
    }

    @Test
    public void testKeys() {
        // Hardcode count so tests must be updated when values are added
        assertEquals(15, ListenerProperties.KEYS.values().length);

        Properties tmp = new Properties();
        try {
            tmp.load(getClass().getResourceAsStream("/org/openecomp/appc/default.properties"));
        } catch (Exception e) {
            fail("Could not load properties to test");
        }
        String realPrefix = tmp.getProperty("test.prefix");
        assertNotNull(realPrefix);
        props = new ListenerProperties(realPrefix, tmp);

        for (KEYS key : ListenerProperties.KEYS.values()) {
            assertNotNull(key.getFullProp(realPrefix));
            assertNotNull(props.getProperty(key));
            assertNotNull(props.getProperty(key.getPropertySuffix()));
        }
    }

    @Test
    public void testDisabled() throws Exception {
        assertFalse(props.isDisabled());
        props.getProperties().put(KEYS.DISABLED.getPropertySuffix(), "TRUE");
        assertTrue(props.isDisabled());
        props.getProperties().put(KEYS.DISABLED.getPropertySuffix(), "N/A");
        assertFalse(props.isDisabled());
        props.getProperties().put(KEYS.DISABLED.getPropertySuffix(), "fAlse");
        assertFalse(props.isDisabled());
        props.getProperties().remove(KEYS.DISABLED.getPropertySuffix());
        assertFalse(props.isDisabled());
    }

}
