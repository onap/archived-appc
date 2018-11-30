/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
 * Modifications Copyright (C) 2018 IBM.
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

package org.onap.appc.oam.util;

import com.att.eelf.configuration.EELFLogger;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.appc.configuration.Configuration;
import org.powermock.reflect.Whitebox;

import static org.mockito.Mockito.mock;

public class ConfigurationHelperTest {
    private ConfigurationHelper configurationHelper;

    private Configuration mockConf;
    private Configuration origConf;

    @Before
    public void setUp() throws Exception {
        mockConf = mock(Configuration.class);

        configurationHelper = new ConfigurationHelper(null);

        // to avoid operation on logger fail, mock up the logger
        EELFLogger fakeLogger = mock(EELFLogger.class);
        Whitebox.setInternalState(configurationHelper, "logger", fakeLogger);
    }

    private void setMockConf() {
        origConf = Whitebox.getInternalState(configurationHelper, "configuration");
        Whitebox.setInternalState(configurationHelper, "configuration", mockConf);
    }

    private void resetOrigConfig() {
        Whitebox.setInternalState(configurationHelper, "configuration", origConf);
        origConf = null;
    }

    @Test
    public void getAppcName() throws Exception {
        // test with existing properties file
        Assert.assertEquals("Should return value(APPC).", "APPC", configurationHelper.getAppcName());

        // test with mockup
        setMockConf();

        String propValue = "testing";
        Mockito.doReturn(propValue).when(mockConf).getProperty(ConfigurationHelper.PROP_KEY_APPC_NAME);
        Assert.assertEquals(String.format("Should return value(%s).", propValue), propValue,
                configurationHelper.getAppcName());

        resetOrigConfig();
    }

    @Test
    public void isMetricEnabled() throws Exception {
        // test with mockup
        setMockConf();

        Mockito.doReturn(false).when(mockConf).getBooleanProperty(
                ConfigurationHelper.PROP_KEY_METRIC_STATE, false);
        Assert.assertFalse("Should return false", configurationHelper.isMetricEnabled());

        Mockito.doReturn(true).when(mockConf).getBooleanProperty(
                ConfigurationHelper.PROP_KEY_METRIC_STATE, false);
        Assert.assertTrue("Should return true", configurationHelper.isMetricEnabled());
    }

    @Test
    public void testReadPropertyNotStop() throws Exception {
        String[] str = configurationHelper.readProperty("appc.OAM.AppcBundlesToNotStop");
        Assert.assertTrue(str.length > 0);
        Assert.assertTrue(str[0].equals(".*appc.oam.*"));
    }

    @Test
    public void testReadPropertyStop() throws Exception {
        String[] str = configurationHelper.readProperty("appc.OAM.AppcBundlesToStop");
        Assert.assertTrue(str.length > 0);
        Assert.assertTrue(str[0].equals(".*appc.*"));
    }

    @Test
    public void testReadPropertyWithMockup() throws Exception {
        setMockConf();

        String propKey = "testing";
        // Property does not exist
        Mockito.doReturn(null).when(mockConf).getProperty(propKey);
        String[] propResult = configurationHelper.readProperty(propKey);
        Assert.assertArrayEquals("PropertyResult should be empty string array",
                ArrayUtils.EMPTY_STRING_ARRAY, propResult);
        // Property has one entry
        String propValue = "1234";
        Mockito.doReturn(propValue).when(mockConf).getProperty(propKey);
        propResult = configurationHelper.readProperty(propKey);
        Assert.assertTrue("PropertyResult should have only one element", propResult.length == 1);
        Assert.assertEquals("PropertyResult should martch propertyValue", propValue, propResult[0]);

        resetOrigConfig();
    }
    
    @Test
    public void testGetOAMOperationTimeoutValue()
    {
    	long timeoutValue = configurationHelper.getOAMOperationTimeoutValue(5000);
    	Assert.assertEquals(5000000, timeoutValue);
    }
}
