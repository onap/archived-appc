/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
 * Modification Copyright (C) 2018 IBM.
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

package org.onap.appc.configuration;

import static org.mockito.Mockito.mock;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;

public class DefaultConfigurationTest {
    private static final String propKey1 = "testKey1";
    private static final String propKey2 = "testKey2";
    private static final String propValue1 = "testValue1";
    private static final String propValue2 = "testValue2";

    private Properties prop = new Properties();
    private DefaultConfiguration defaultConfiguration;

    @Before
    public void setUp() throws Exception {
        prop.setProperty(propKey1, propValue1);
        prop.setProperty(propKey2, propValue2);

        defaultConfiguration = new DefaultConfiguration();
    }

    @Test
    public void testClear() throws Exception {
        Whitebox.setInternalState(defaultConfiguration, "properties", prop);
        defaultConfiguration.clear();
        Properties internalProp = Whitebox.getInternalState(defaultConfiguration, "properties");
        Assert.assertTrue("internal properties should be cleared", internalProp.isEmpty());
    }

    @Test
    public void testClone() throws Exception {
        Object clonedObject = defaultConfiguration.clone();
        Assert.assertTrue("Should be DefaultConfiguration",
                clonedObject instanceof DefaultConfiguration);
        Properties internalProp = Whitebox.getInternalState(defaultConfiguration, "properties");
        Properties clonedInternalProp = Whitebox.getInternalState(clonedObject, "properties");
        Assert.assertEquals(internalProp, clonedInternalProp);
    }

    @Test
    public void testEquals() throws Exception {
        // test compare with null
        Assert.assertFalse(defaultConfiguration.equals(null));
        // test with non-DefaultConfiguration object
        Assert.assertFalse(defaultConfiguration.equals("abc"));

        // test with not match DefaultConfiguration object
        defaultConfiguration.setProperties(prop);
        DefaultConfiguration newConfig = new DefaultConfiguration();
        Assert.assertFalse(defaultConfiguration.equals(newConfig));

        // test with matching DefaultConfiguration object
        newConfig.setProperties(prop);
        Assert.assertTrue(defaultConfiguration.equals(newConfig));
    }

    @Test
    public void testSetPropAndGetBooleanProperty() throws Exception {
        String booleanKey = "booleanKey";
        // test default value
        Assert.assertFalse(defaultConfiguration.getBooleanProperty(booleanKey));
        // test match value true
        defaultConfiguration.setProperty(booleanKey, "true");
        Assert.assertTrue(defaultConfiguration.getBooleanProperty(booleanKey));
        defaultConfiguration.setProperty(booleanKey, "True");
        Assert.assertTrue(defaultConfiguration.getBooleanProperty(booleanKey));
        defaultConfiguration.setProperty(booleanKey, "TrUe");
        Assert.assertTrue(defaultConfiguration.getBooleanProperty(booleanKey));
        // test not matching true values
        defaultConfiguration.setProperty(booleanKey, "false");
        Assert.assertFalse(defaultConfiguration.getBooleanProperty(booleanKey));
        defaultConfiguration.setProperty(booleanKey, "abc");
        Assert.assertFalse(defaultConfiguration.getBooleanProperty(booleanKey));
    }
    
    @Test
    public void testSetPropAndGetBooleanPropertyForEncryptedValue()
    {
        String booleanKey = "booleanKey";
        defaultConfiguration.setProperty(booleanKey, "enc:true");
        Assert.assertFalse(defaultConfiguration.getBooleanProperty(booleanKey));
    }
    
   
    @Test
    public void testSetPropAndGetBooleanPropertyWithDefaultValue() throws Exception {
        String booleanKey = "booleanKey";
        // test default value
        Assert.assertFalse(defaultConfiguration.getBooleanProperty(booleanKey, false));
        Assert.assertTrue(defaultConfiguration.getBooleanProperty(booleanKey, true));
        // test match value true
        defaultConfiguration.setProperty(booleanKey, "true");
        Assert.assertTrue(defaultConfiguration.getBooleanProperty(booleanKey, false));
        defaultConfiguration.setProperty(booleanKey, "True");
        Assert.assertTrue(defaultConfiguration.getBooleanProperty(booleanKey, false));
        defaultConfiguration.setProperty(booleanKey, "TrUe");
        Assert.assertTrue(defaultConfiguration.getBooleanProperty(booleanKey, false));
        // test not matching true values
        defaultConfiguration.setProperty(booleanKey, "false");
        Assert.assertFalse(defaultConfiguration.getBooleanProperty(booleanKey, true));
        defaultConfiguration.setProperty(booleanKey, "abc");
        Assert.assertFalse(defaultConfiguration.getBooleanProperty(booleanKey, true));
    }

    @Test
    public void testSetPropAndGetDoubleProperty() throws Exception {
        String doubleKey = "doubleKey";
        // test default value
        Assert.assertTrue(0.0 == defaultConfiguration.getDoubleProperty(doubleKey));
        // test NumberFormatException
        defaultConfiguration.setProperty(doubleKey, "abc");
        Assert.assertTrue(0.0 == defaultConfiguration.getDoubleProperty(doubleKey));
        // test normal
        defaultConfiguration.setProperty(doubleKey, "1.1");
        Assert.assertTrue(1.1 == defaultConfiguration.getDoubleProperty(doubleKey));
    }

    @Test
    public void testSetPropAndGetDoublePropertyWithDefaultValue() throws Exception {
        String doubleKey = "doubleKey";
        // test default value
        Assert.assertTrue(2.2 == defaultConfiguration.getDoubleProperty(doubleKey, 2.2));
        // test NumberFormatException
        defaultConfiguration.setProperty(doubleKey, "abc");
        Assert.assertTrue(0.0 == defaultConfiguration.getDoubleProperty(doubleKey, 2.2));
        // test normal
        defaultConfiguration.setProperty(doubleKey, "1.1");
        Assert.assertTrue(1.1 == defaultConfiguration.getDoubleProperty(doubleKey, 2.2));
    }

    @Test
    public void testSetPropAndGetIntegerProperty() throws Exception {
        String integerKey = "integerKey";
        // test default value
        Assert.assertTrue(0 == defaultConfiguration.getIntegerProperty(integerKey));
        // test NumberFormatException
        defaultConfiguration.setProperty(integerKey, "abc");
        Assert.assertTrue(0 == defaultConfiguration.getIntegerProperty(integerKey));
        // test normal
        defaultConfiguration.setProperty(integerKey, "100");
        Assert.assertTrue(100 == defaultConfiguration.getIntegerProperty(integerKey));
    }

    @Test
    public void testSetPropAndGetIntegerPropertyWithDefaultValue() throws Exception {
        String integerKey = "integerKey";
        // test default value
        Assert.assertTrue(100 == defaultConfiguration.getIntegerProperty(integerKey, 100));
        // test NumberFormatException
        defaultConfiguration.setProperty(integerKey, "abc");
        Assert.assertTrue(0 == defaultConfiguration.getIntegerProperty(integerKey, 100));
        // test normal
        defaultConfiguration.setProperty(integerKey, "100");
        Assert.assertTrue(100 == defaultConfiguration.getIntegerProperty(integerKey, 10));
    }

    @Test
    public void testSetPropAndGetLongProperty() throws Exception {
        String longKey = "longKey";
        // test default value
        Assert.assertTrue(0 == defaultConfiguration.getLongProperty(longKey));
        // test NumberFormatException
        defaultConfiguration.setProperty(longKey, "abc");
        Assert.assertTrue(0 == defaultConfiguration.getLongProperty(longKey));
        // test normal
        defaultConfiguration.setProperty(longKey, "100");
        Assert.assertTrue(100 == defaultConfiguration.getLongProperty(longKey));
    }

    @Test
    public void testSetPropAndGetLongPropertyWithDefaultVaue() throws Exception {
        String longKey = "longKey";
        // test default value
        Assert.assertTrue(10 == defaultConfiguration.getLongProperty(longKey, 10));
        // test NumberFormatException
        defaultConfiguration.setProperty(longKey, "abc");
        Assert.assertTrue(0 == defaultConfiguration.getLongProperty(longKey, 10));
        // test normal
        defaultConfiguration.setProperty(longKey, "100");
        Assert.assertTrue(100 == defaultConfiguration.getLongProperty(longKey, 10));
    }

    @Test
    public void testSetAndGetProperties() throws Exception {
        Properties internalProp = Whitebox.getInternalState(defaultConfiguration, "properties");
        Assert.assertEquals(internalProp, defaultConfiguration.getProperties());

        defaultConfiguration.setProperties(prop);
        internalProp = Whitebox.getInternalState(defaultConfiguration, "properties");
        Assert.assertEquals(internalProp, defaultConfiguration.getProperties());
    }

    @Test
    public void testSetAndGetProperty() throws Exception {
        String key = "key";
        // test default value
        Assert.assertTrue(null == defaultConfiguration.getProperty(key));
        // test normal
        defaultConfiguration.setProperty(key, "abc");
        Assert.assertEquals("abc", defaultConfiguration.getProperty(key));
    }

    @Test
    public void testSetPropAndGetPropertyWithDefaultValue() throws Exception {
        String key = "key";
        // test default value
        Assert.assertTrue(null == defaultConfiguration.getProperty(key, null));
        Assert.assertEquals("abc", defaultConfiguration.getProperty(key, "abc"));
        // test normal
        defaultConfiguration.setProperty(key, "abc");
        Assert.assertEquals("abc", defaultConfiguration.getProperty(key, "abcd"));
    }

    @Test
    public void testHashCode() throws Exception {
        Properties properties = null;
        Whitebox.setInternalState(defaultConfiguration, "properties", properties);
        Assert.assertEquals(0, defaultConfiguration.hashCode());


        Whitebox.setInternalState(defaultConfiguration, "properties", prop);
        Assert.assertEquals(prop.hashCode(), defaultConfiguration.hashCode());
    }

    @Test
    public void testIsPropertyDefined() throws Exception {
        String key = "key";
        // test not exist
        Assert.assertFalse(defaultConfiguration.isPropertyDefined(key));
        // test exist
        defaultConfiguration.setProperty(key, "abc");
        Assert.assertTrue(defaultConfiguration.isPropertyDefined(key));
    }

    @Test
    public void testIsValidBoolean() throws Exception {
        String key = "key";
        // test not exist
        Assert.assertFalse(defaultConfiguration.isValidBoolean(key));
        // test exist with invalid
        defaultConfiguration.setProperty(key, "abc");
        Assert.assertFalse(defaultConfiguration.isValidBoolean(key));
        // test exist with valid
        defaultConfiguration.setProperty(key, "True");
        Assert.assertTrue(defaultConfiguration.isPropertyDefined(key));
        defaultConfiguration.setProperty(key, "FaLse");
        Assert.assertTrue(defaultConfiguration.isPropertyDefined(key));
    }

    @Test
    public void testIsValidDouble() throws Exception {
        String key = "key";
        // test not exist
        Assert.assertFalse(defaultConfiguration.isValidDouble(key));
        // test exist with invalid
        defaultConfiguration.setProperty(key, "abc");
        Assert.assertFalse(defaultConfiguration.isValidDouble(key));
        // test exist with valid
        defaultConfiguration.setProperty(key, "2");
        Assert.assertTrue(defaultConfiguration.isValidDouble(key));
        defaultConfiguration.setProperty(key, "3.45");
        Assert.assertTrue(defaultConfiguration.isValidDouble(key));
    }

    @Test
    public void testIsValidInteger() throws Exception {
        String key = "key";
        // test not exist
        Assert.assertFalse(defaultConfiguration.isValidInteger(key));
        // test exist with invalid
        defaultConfiguration.setProperty(key, "abc");
        Assert.assertFalse(defaultConfiguration.isValidInteger(key));
        defaultConfiguration.setProperty(key, "3.45");
        Assert.assertFalse(defaultConfiguration.isValidInteger(key));
        // test exist with valid
        defaultConfiguration.setProperty(key, "2");
        Assert.assertTrue(defaultConfiguration.isValidInteger(key));
    }

    @Test
    public void testIsValidLong() throws Exception {
        String key = "key";
        // test not exist
        Assert.assertFalse(defaultConfiguration.isValidLong(key));
        // test exist with invalid
        defaultConfiguration.setProperty(key, "abc");
        Assert.assertFalse(defaultConfiguration.isValidLong(key));
        defaultConfiguration.setProperty(key, "3.45");
        Assert.assertFalse(defaultConfiguration.isValidLong(key));
        // test exist with valid
        defaultConfiguration.setProperty(key, "2");
        Assert.assertTrue(defaultConfiguration.isValidLong(key));
    }

    @Test
    public void testSetPropertiesWithInputStream() throws Exception {
        InputStream mockIS = mock(InputStream.class);
        defaultConfiguration.setProperties(mockIS);

        Properties mockProp = mock(Properties.class);
        Mockito.doThrow(new IOException("testing exception")).when(mockProp).load(mockIS);
        Whitebox.setInternalState(defaultConfiguration, "properties", mockProp);
        defaultConfiguration.setProperties(mockIS);
        // Should come here without exception
    }

    @Test
    public void testToString() throws Exception {
        Properties internalProp = Whitebox.getInternalState(defaultConfiguration, "properties");
        Assert.assertEquals(String.format("Configuration: %d properties, keys:[%s]",
                internalProp.size(), internalProp.keySet().toString()),
                defaultConfiguration.toString());
    }
}
