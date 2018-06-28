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
import org.mockito.Mockito;
import org.hamcrest.CoreMatchers;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Properties;

public class UnmodifiablePropertiesTest {

    private static final String propKey1 = "testKey1";
    private static final String propKey2 = "testKey2";
    private static final String propValue1 = "testValue1";
    private static final String propValue2 = "testValue2";
    private static final String noKey = "unusedKey";
    private static final String noValue = "unusedValue";
    private static final String propHeader = "test header";
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
    public final void testElements() {
        Enumeration<Object> propValues = unmodifiableProperties.elements();
        Assert.assertEquals(propValue2, propValues.nextElement());
        Assert.assertEquals(propValue1, propValues.nextElement());
    }

    @Test
    public final void testEntrySet() {
        // Expect entrySet=[testKey2=testValue2, testKey1=testValue1].
        Assert.assertEquals("Should match my properties K/V entries in setUp", properties.entrySet(),
                unmodifiableProperties.entrySet());
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
    public final void testHashCode() {
        Assert.assertEquals("Should match my properties.hashcode() int.", properties.hashCode(),
                unmodifiableProperties.hashCode());
    }

    @Test
    public final void testIsEmpty() {
        Assert.assertFalse(unmodifiableProperties.isEmpty());
    }

    @Test
    public final void testKeys() {
        Enumeration<Object> propKeys = unmodifiableProperties.keys();
        Assert.assertEquals(propKey2, propKeys.nextElement());
        Assert.assertEquals(propKey1, propKeys.nextElement());
    }

    @Test
    public final void testKeySet() {
        // Expect keySet=[testKey2, testKey1].
        Assert.assertEquals("Should match my properties key entries in SetUp", properties.keySet(),
                unmodifiableProperties.keySet());
    }

    @Test
    public final void testListPrintStream() {
        ByteArrayOutputStream propByteArray = new ByteArrayOutputStream();
        PrintStream listOut = new PrintStream(propByteArray);
        unmodifiableProperties.list(listOut);
        String propList = new String(propByteArray.toByteArray());
        Assert.assertThat(propList, CoreMatchers.containsString("testKey2=testValue2"));
        Assert.assertThat(propList, CoreMatchers.containsString("testKey1=testValue1"));
    }

    @Test
    public final void testListPrintWriter() {
        StringWriter listOut = new StringWriter();
        PrintWriter writer = new PrintWriter(listOut);
        unmodifiableProperties.list(writer);
        String propList = listOut.toString();
        Assert.assertThat(propList, CoreMatchers.containsString("testKey2=testValue2"));
        Assert.assertThat(propList, CoreMatchers.containsString("testKey1=testValue1"));
    }

    @Test
    public final void testLoadInputStream() throws IOException {
        InputStream mockInStream = Mockito.mock(InputStream.class);
        try {
            unmodifiableProperties.load(mockInStream);
        } catch (IOException ex) {
        } catch (UnsupportedOperationException exceptionMessage) {
            Assert.assertEquals(desiredMessage, exceptionMessage.getMessage());
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public final void testLoadReader() throws IOException {
        String dummyPair = "key3=testKey3\nvalue3=testValue3";
        StringReader reader = new StringReader(dummyPair);
        try {
            unmodifiableProperties.load(reader);
        } catch (UnsupportedOperationException exceptionMessage) {
            Assert.assertEquals(desiredMessage, exceptionMessage.getMessage());
            throw exceptionMessage;
        }
    }

    @Test
    public final void testLoadFromXMLInputStream() throws IOException {
        InputStream mockInStream = Mockito.mock(InputStream.class);
        try {
            unmodifiableProperties.loadFromXML(mockInStream);
        } catch (IOException ex) {
        } catch (UnsupportedOperationException exceptionMessage) {
            Assert.assertEquals(desiredMessage, exceptionMessage.getMessage());
        }
    }

    @Test
    public final void testPropertyNames() {
        Enumeration<?> propNames = unmodifiableProperties.propertyNames();
        Assert.assertEquals(propKey2, propNames.nextElement());
        Assert.assertEquals(propKey1, propNames.nextElement());
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
    public final void testPutAllMapOfQextendsObjectQextendsObject() {
        try {
            unmodifiableProperties.putAll(properties);
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

    @Test
    public final void testSaveOutputStreamString() {
        // Appl method is deprecated, but I still added this test since it is reachable.
        OutputStream propByteArray = new ByteArrayOutputStream();
        unmodifiableProperties.save(propByteArray, propHeader);
        Assert.assertThat(propByteArray.toString(), CoreMatchers.startsWith("#test header"));
        Assert.assertThat(propByteArray.toString(), CoreMatchers.containsString("testKey2=testValue2"));
        Assert.assertThat(propByteArray.toString(), CoreMatchers.containsString("testKey1=testValue1"));
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
    public final void testStoreOutputStreamString() throws IOException {
        OutputStream propByteArray = new ByteArrayOutputStream();
        unmodifiableProperties.store(propByteArray, propHeader);
        // adds comment header and streams/appends properties file into propByteArray
        // expected = "#test header\n#<Date>\ntestKey2=testValue2\ntestKey1=testValue1"
        Assert.assertThat(propByteArray.toString(), CoreMatchers.startsWith("#test header"));
        Assert.assertThat(propByteArray.toString(), CoreMatchers.containsString("testKey2=testValue2"));
        Assert.assertThat(propByteArray.toString(), CoreMatchers.containsString("testKey1=testValue1"));
    }

    @Test
    public final void testStoreWriterString() throws IOException {
        StringWriter writer = new StringWriter();
        unmodifiableProperties.store(writer, propHeader);
        Assert.assertThat(writer.toString(), CoreMatchers.startsWith("#test header"));
        Assert.assertThat(writer.toString(), CoreMatchers.containsString("testKey2=testValue2"));
        Assert.assertThat(writer.toString(), CoreMatchers.containsString("testKey1=testValue1"));
    }

    @Test
    public final void testStoreToXMLOutputStreamString() throws IOException {
        OutputStream propByteArray = new ByteArrayOutputStream();
        unmodifiableProperties.storeToXML(propByteArray, propHeader);
        // adds XML comment header and streams/appends XML properties file into propByteArray
        Assert.assertThat(propByteArray.toString(), CoreMatchers.containsString("<comment>test header</comment>"));
        Assert.assertThat(propByteArray.toString(),
                CoreMatchers.containsString("<entry key=\"testKey2\">testValue2</entry>"));
        Assert.assertThat(propByteArray.toString(),
                CoreMatchers.containsString("<entry key=\"testKey1\">testValue1</entry>"));
    }

    @Test
    public final void testStoreToXMLOutputStreamStringString() throws IOException {
        OutputStream propByteArray = new ByteArrayOutputStream();
        unmodifiableProperties.storeToXML(propByteArray, propHeader, "UTF-8");
        // adds XML comment header and streams/appends XML properties file into propByteArray
        Assert.assertThat(propByteArray.toString(), CoreMatchers.containsString("<comment>test header</comment>"));
        Assert.assertThat(propByteArray.toString(),
                CoreMatchers.containsString("<entry key=\"testKey2\">testValue2</entry>"));
        Assert.assertThat(propByteArray.toString(),
                CoreMatchers.containsString("<entry key=\"testKey1\">testValue1</entry>"));
    }

    @Test
    public final void testStringPropertyNames() {
        Assert.assertEquals(properties.stringPropertyNames(), unmodifiableProperties.stringPropertyNames());
    }

    @Test
    public final void testToString() {
        // toString=[{testKey2=testValue2, testKey1=testValue1}]
        Assert.assertEquals(properties.toString(), unmodifiableProperties.toString());
    }

    @Test
    public final void testValues() {
        Assert.assertEquals(properties.values().toString(), unmodifiableProperties.values().toString());
    }
}
