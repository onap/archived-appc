/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

public class UnmodifiablePropertiesTest {

    private static final String propKey1 = "testKey1";
    private static final String propKey2 = "testKey2";
    private static final String propValue1 = "testValue1";
    private static final String propValue2 = "testValue2";
    private static final String noKey = "unusedKey";
    private static final String noValue = "unusedValue";
    private Properties properties = new Properties();

    private UnmodifiableProperties unmodifiableProperties = new UnmodifiableProperties(properties);
    private String desiredMessage = "Property cannot be modified!";

    @Before
    public void setUp() throws Exception {
        properties.setProperty(propKey1, propValue1);
        properties.setProperty(propKey2, propValue2);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testClear() {
        try {
            unmodifiableProperties.clear();
        } catch (UnsupportedOperationException exceptionMessage) {
            Assert.assertEquals(desiredMessage, exceptionMessage.getMessage());
            throw exceptionMessage;
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testClone() {
        try {
            unmodifiableProperties.clone();
        } catch (UnsupportedOperationException exceptionMessage) {
            Assert.assertEquals(desiredMessage, exceptionMessage.getMessage());
            throw exceptionMessage;
        }
    }

    @Test
    public final void testContainsObject() {
        Assert.assertTrue(unmodifiableProperties.contains(propValue2));
        Assert.assertFalse(unmodifiableProperties.contains(noValue));
    }

    @Test
    public final void testContainsKeyObject() {
        Assert.assertTrue(unmodifiableProperties.containsKey(propKey1));
        Assert.assertFalse(unmodifiableProperties.containsKey(noKey));
    }

    @Test
    public final void testContainsValueObject() {
        Assert.assertTrue(unmodifiableProperties.containsValue(propValue1));
        Assert.assertFalse(unmodifiableProperties.containsValue(noValue));
    }

    @Test
    public final void testEntrySet() {
        // Should match my properties K/V entries in setUp.
        // Expect entrySet=[testKey2=testValue2, testKey1=testValue1].
        Assert.assertEquals(properties.entrySet(), unmodifiableProperties.entrySet());
    }

    @Test
    public final void testEqualsObject() {
        Assert.assertTrue(unmodifiableProperties.equals(properties));
    }

    @Test
    public final void testGetObject() {
        Assert.assertEquals(propValue2, unmodifiableProperties.get(propKey2));
    }

    @Test
    public final void testGetPropertyString() {
        Assert.assertEquals(propValue1, unmodifiableProperties.getProperty("testKey1"));
    }

    @Test
    public final void testGetPropertyStringString() {
        Assert.assertEquals(propValue2, unmodifiableProperties.getProperty(propKey2, noValue));
        Assert.assertEquals(propValue2, unmodifiableProperties.getProperty(noKey, propValue2));
    }

    @Test
    public final void testIsEmpty() {
        Assert.assertFalse(unmodifiableProperties.isEmpty());
    }

    @Test(expected = UnsupportedOperationException.class)
    public final void testPutObjectObject() {
        try {
            unmodifiableProperties.put(propKey2, propValue1);
        } catch (UnsupportedOperationException exceptionMessage) {
            Assert.assertEquals(desiredMessage, exceptionMessage.getMessage());
            throw exceptionMessage;
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public final void testRehash() {
        try {
            unmodifiableProperties.rehash();
        } catch (UnsupportedOperationException exceptionMessage) {
            Assert.assertEquals(desiredMessage, exceptionMessage.getMessage());
            throw exceptionMessage;
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public final void testRemoveObject() {
        try {
            unmodifiableProperties.remove(propKey1);
        } catch (UnsupportedOperationException exceptionMessage) {
            Assert.assertEquals(desiredMessage, exceptionMessage.getMessage());
            throw exceptionMessage;
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public final void testSetPropertyStringString() {
        try {
            unmodifiableProperties.setProperty(propKey1, propValue2);
        } catch (UnsupportedOperationException exceptionMessage) {
            Assert.assertEquals(desiredMessage, exceptionMessage.getMessage());
            throw exceptionMessage;
        }
    }

    @Test
    public final void testSize() {
        Assert.assertEquals(2, unmodifiableProperties.size());
    }

    @Test
    public final void testStringPropertyNames() {
        Assert.assertEquals(properties.stringPropertyNames(),unmodifiableProperties.stringPropertyNames());
    }

    @Test
    public final void testToString() {
        // toString=[{testKey2=testValue2, testKey1=testValue1}]
        Assert.assertEquals(properties.toString(),unmodifiableProperties.toString());
    }
}
