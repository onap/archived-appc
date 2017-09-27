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

package org.openecomp.appc.oam.util;

import com.att.eelf.configuration.EELFLogger;
import org.apache.commons.lang.ArrayUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openecomp.appc.configuration.Configuration;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.oam.AppcOam;
import org.openecomp.appc.statemachine.impl.readers.AppcOamStates;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.support.membermodification.MemberMatcher;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;

@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(PowerMockRunner.class)
@PrepareForTest({BundleHelper.class, FrameworkUtil.class})
public class BundleHelperTest {
    private BundleHelper bundleHelper;
    private AsyncTaskHelper mockTaskHelper = mock(AsyncTaskHelper.class);

    @Before
    public void setUp() throws Exception {
        bundleHelper = spy(new BundleHelper(null, null, null));

        // to avoid operation on logger fail, mock up the logger
        EELFLogger mockLogger = mock(EELFLogger.class);
        Whitebox.setInternalState(bundleHelper, "logger", mockLogger);
    }

    @Test
    public void testBundleOperations() throws Exception {
        // spy mocked bundle for calls to method statr or stop.
        // Note: the time of method calls are accumulated in this test method.
        Bundle mockBundle = spy(Mockito.mock(Bundle.class));
        Map<String, Bundle> mapFromGetAppcLcmBundles = new HashMap<>();
        mapFromGetAppcLcmBundles.put("BundleString", mockBundle);

        PowerMockito.doReturn(mapFromGetAppcLcmBundles).when(bundleHelper, MemberMatcher.method(
            BundleHelper.class, "getAppcLcmBundles")).withNoArguments();

        StateHelper mockStateHelper = mock(StateHelper.class);
        Whitebox.setInternalState(bundleHelper, "stateHelper", mockStateHelper);

        AppcOamStates appcOamStates = AppcOamStates.Stopped;
        Mockito.doReturn(appcOamStates).when(mockStateHelper).getState();

        // test start
        Mockito.doReturn(true).when(mockStateHelper).isSameState(appcOamStates);
        boolean result = bundleHelper.bundleOperations(AppcOam.RPC.start, new HashMap<>(), mockTaskHelper,null);
        Assert.assertTrue("Should be completed", result);
        Mockito.verify(mockTaskHelper, times(1)).submitBaseSubCallable(any());

        // test start aborted
        Mockito.doReturn(false).when(mockStateHelper).isSameState(appcOamStates);
        result = bundleHelper.bundleOperations(AppcOam.RPC.start, new HashMap<>(), mockTaskHelper,null);
        Assert.assertFalse("Should be abort", result);
        Mockito.verify(mockTaskHelper, times(1)).submitBaseSubCallable(any());

        // test stop
        result = bundleHelper.bundleOperations(AppcOam.RPC.stop, new HashMap<>(), mockTaskHelper,null);
        Assert.assertTrue("Should be completed", result);
        Mockito.verify(mockTaskHelper, times(2)).submitBaseSubCallable(any());
    }

    @Test(expected = APPCException.class)
    public void testBundleOperationsRpcException() throws Exception {
        bundleHelper.bundleOperations(AppcOam.RPC.maintenance_mode, new HashMap<>(), mockTaskHelper,null);
    }

    @Test
    public void testGetBundleList() throws Exception {
        mockStatic(FrameworkUtil.class);
        Bundle myBundle = mock(Bundle.class);
        PowerMockito.when(FrameworkUtil.getBundle(any())).thenReturn(myBundle);

        // test bundle context is null
        Mockito.when(myBundle.getBundleContext()).thenReturn(null);
        Assert.assertTrue("Should return null", bundleHelper.getBundleList() == null);

        BundleContext myBundleContext = mock(BundleContext.class);
        Mockito.when(myBundle.getBundleContext()).thenReturn(myBundleContext);

        // test bundle list is empty
        Bundle[] bundleArray = {};
        Mockito.when(myBundleContext.getBundles()).thenReturn(bundleArray);
        Bundle[] results = bundleHelper.getBundleList();
        Assert.assertTrue("Should not be null", results != null);
        Assert.assertTrue("Should not have any element", results.length == 0);

        // test bundle list has at one bundle
        bundleArray = new Bundle[] { myBundle };
        Mockito.when(myBundleContext.getBundles()).thenReturn(bundleArray);
        results = bundleHelper.getBundleList();
        Assert.assertTrue("Should not be null", results != null);
        Assert.assertTrue("Should have one element", results.length == 1);
        Assert.assertEquals("Should be the mock bundle", myBundle, results[0]);
    }

    @Test
    public void testReadPropsFromPropListName() throws Exception {
        // mock configuarion helper
        ConfigurationHelper configurationHelper = new ConfigurationHelper(null);
        EELFLogger fakeLogger = mock(EELFLogger.class);
        Whitebox.setInternalState(configurationHelper, "logger", fakeLogger);
        Configuration fakeConf = mock(Configuration.class);
        Whitebox.setInternalState(configurationHelper, "configuration", fakeConf);

        Whitebox.setInternalState(bundleHelper, "configurationHelper", configurationHelper);

        String propKey = "testing";
        // Property does not exist
        Mockito.doReturn(null).when(fakeConf).getProperty(propKey);
        String[] propResult = bundleHelper.readPropsFromPropListName(propKey);
        Assert.assertArrayEquals("PropertyResult should be empty string array",
            ArrayUtils.EMPTY_STRING_ARRAY, propResult);
        // Property has one entry
        String propValue1 = "1234";
        String propValue2 = "5678";
        Mockito.doReturn(propValue1).when(fakeConf).getProperty(propKey);
        Mockito.doReturn(propValue2).when(fakeConf).getProperty(propValue1);
        propResult = bundleHelper.readPropsFromPropListName(propKey);
        Assert.assertTrue("PropertyResult should have only one element", propResult.length == 1);
        Assert.assertEquals("PropertyResult should martch propertyValue", propValue2, propResult[0]);
        // Property has two entries
        propValue1 = "1234\n,4321";
        String propValue3 = "8765";
        Mockito.doReturn(propValue1).when(fakeConf).getProperty(propKey);
        Mockito.doReturn(propValue2).when(fakeConf).getProperty(propValue1);
        Mockito.doReturn(propValue3).when(fakeConf).getProperty("4321");
        propResult = bundleHelper.readPropsFromPropListName(propKey);
        Assert.assertTrue("PropertyResult should have two elements", propResult.length == 2);
        List propResultList = Arrays.asList(propResult);
        Assert.assertTrue("PropertyResult should have propertyValue2", propResultList.contains(propValue2));
        Assert.assertTrue("PropertyResult should have propertyValue2", propResultList.contains(propValue3));
    }
}
