/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.adapter.messaging.dmaap.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.appc.configuration.ConfigurationFactory;

public class TestDmaapUtil {
    private static Class<?>   configurationFactoryClass;
    private static Field      configField;

    @Test
    public void testCreateConsumerPropFile() {
        String topic = "JunitTopicOne";
        Properties junitProps = new Properties();
        junitProps.put("host", "192.168.10.10");
        junitProps.put("group", "junit-client");
        junitProps.put("id", "junit-consumer-one");
        junitProps.put("filter", "none");

        String junitFile = null;

        // ensure file path property is not set
        if (System.getProperty(DmaapUtil.DMAAP_PROPERTIES_PATH) != null) {
            System.clearProperty(DmaapUtil.DMAAP_PROPERTIES_PATH);

            // set configuration to null to force reloading of properties
            try {
                configField.set(null, null);
            } catch (IllegalArgumentException | IllegalAccessException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
        try {
            junitFile = DmaapUtil.createConsumerPropFile(topic, junitProps);
        } catch (IOException e) {
            e.printStackTrace();
            fail("Exception creating consumer property file");
        }

        assertNotNull(junitFile);

        // open file and verify properties
        File testFile = new File(junitFile);
        assertTrue(testFile.exists());

        InputStream is = null;
        Properties testProps = new Properties();
        try {
            is = new FileInputStream(testFile);
            testProps.load(is);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail("Exception opening consumer property file");
        } catch (IOException e) {
            e.printStackTrace();
            fail("Exception opening consumer property file");
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                fail("Exception closing consumer property file");
            }
        }

        assertFalse(testProps.isEmpty());

        assertEquals(testProps.get("host"), "192.168.10.10");
        assertEquals(testProps.get("group"), "junit-client");
        assertEquals(testProps.get("id"), "junit-consumer-one");
        assertEquals(testProps.get("filter"), "none");
        assertEquals(testProps.get("TransportType"), "HTTPNOAUTH");
    }

    @Test
    public void testCreateConsumerPropFileWithCustomProfile() {
        String topic = "JunitTopicOne";
        Properties junitProps = new Properties();
        junitProps.put("host", "192.168.10.10");
        junitProps.put("group", "junit-client");
        junitProps.put("id", "junit-consumer-two");
        junitProps.put("filter", "none");

        String junitFile = null;

        // set property for DMaaP profile
        System.setProperty(DmaapUtil.DMAAP_PROPERTIES_PATH, "src/test/resources/org/onap/appc");

        // set configuration to null to force reloading of properties
        try {
            configField.set(null, null);
        } catch (IllegalArgumentException | IllegalAccessException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        try {
            junitFile = DmaapUtil.createConsumerPropFile(topic, junitProps);
        } catch (IOException e) {
            e.printStackTrace();
            fail("Exception creating consumer property file");
        }

        assertNotNull(junitFile);

        // open file and verify properties
        File testFile = new File(junitFile);
        assertTrue(testFile.exists());

        InputStream is = null;
        Properties testProps = new Properties();
        try {
            is = new FileInputStream(testFile);
            testProps.load(is);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail("Exception opening consumer property file");
        } catch (IOException e) {
            e.printStackTrace();
            fail("Exception opening consumer property file");
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                fail("Exception closing consumer property file");
            }
        }

        assertFalse(testProps.isEmpty());

        assertEquals(testProps.get("host"), "192.168.10.10");
        assertEquals(testProps.get("group"), "junit-client");
        assertEquals(testProps.get("id"), "junit-consumer-two");
        assertEquals(testProps.get("filter"), "none");
        assertEquals(testProps.get("TransportType"), "HTTPAAF");
    }

    @Test
    public void testCreateProducerPropFile() {
        String topic = "JunitTopicOne";
        Properties junitProps = new Properties();
        junitProps.put("host", "192.168.10.10");
        junitProps.put("group", "junit-client");
        junitProps.put("id", "junit-producer-one");
        junitProps.put("filter", "none");

        String junitFile = null;

        // ensure file path property is not set
        if (System.getProperty(DmaapUtil.DMAAP_PROPERTIES_PATH) != null) {
            System.clearProperty(DmaapUtil.DMAAP_PROPERTIES_PATH);

            // set configuration to null to force reloading of properties
            try {
                configField.set(null, null);
            } catch (IllegalArgumentException | IllegalAccessException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }

        try {
            junitFile = DmaapUtil.createProducerPropFile(topic, junitProps);
        } catch (IOException e) {
            e.printStackTrace();
            fail("Exception creating consumer property file");
        }

        assertNotNull(junitFile);

        // open file and verify properties
        File testFile = new File(junitFile);
        assertTrue(testFile.exists());

        InputStream is = null;
        Properties testProps = new Properties();
        try {
            is = new FileInputStream(testFile);
            testProps.load(is);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail("Exception opening consumer property file");
        } catch (IOException e) {
            e.printStackTrace();
            fail("Exception opening consumer property file");
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                fail("Exception closing consumer property file");
            }
        }

        assertFalse(testProps.isEmpty());

        assertEquals(testProps.get("host"), "192.168.10.10");
        assertEquals(testProps.get("group"), "junit-client");
        assertEquals(testProps.get("id"), "junit-producer-one");
        assertEquals(testProps.get("filter"), "none");
        assertEquals("HTTPNOAUTH", testProps.get("TransportType"));
    }

    /**
     * Use reflection to locate fields and methods so that they can be
     * manipulated during the test to change the internal state accordingly.
     * 
     * @throws NoSuchFieldException
     *             if the field(s) dont exist
     * @throws SecurityException
     *             if reflective access is not allowed
     * @throws NoSuchMethodException
     *             If the method(s) dont exist
     */
    @SuppressWarnings("nls")
    @BeforeClass
    public static void once() throws NoSuchFieldException, SecurityException, NoSuchMethodException {
        configurationFactoryClass = ConfigurationFactory.class;

        configField = configurationFactoryClass.getDeclaredField("config");
        configField.setAccessible(true);
    }
}
