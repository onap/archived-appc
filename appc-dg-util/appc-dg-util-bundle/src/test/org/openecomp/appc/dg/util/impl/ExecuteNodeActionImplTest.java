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

package org.openecomp.appc.dg.util.impl;

import com.att.eelf.configuration.EELFLogger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.onap.ccsdk.sli.adaptors.aai.AAIService;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;
import org.openecomp.appc.exceptions.APPCException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyPrivate;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ExecuteNodeActionImpl.class, FrameworkUtil.class, Thread.class})
public class ExecuteNodeActionImplTest {
    @Spy
    private ExecuteNodeActionImpl mockedExecuteNodeActionImpl = new ExecuteNodeActionImpl();
    @Mock
    private EELFLogger eelfLogger;
    @Mock
    private AAIService aaiService;

    private final String resourceType = "resourceType";
    private final String prefix = "prefix";
    private final String resourceKey = "resourceKey";
    private final String attributeName = "attributeName";
    private final String attributeValue = "attributeValue";

    private Map<String, String> params = new HashMap<>();
    private SvcLogicContext svcLogicContext = new SvcLogicContext();
    private SvcLogicResource.QueryStatus queryStatus = SvcLogicResource.QueryStatus.SUCCESS;


    @Before
    public void setUp() throws Exception {
        Whitebox.setInternalState(mockedExecuteNodeActionImpl, "logger", eelfLogger);
        Whitebox.setInternalState(mockedExecuteNodeActionImpl, "aaiService", aaiService);

        params.put("resourceType", resourceType);
        params.put("prefix", prefix);
        params.put("resourceKey", resourceKey);
        params.put("attributeName", attributeName);
        params.put("attributeValue", attributeValue);
    }

    @Test
    public void testInitialize() throws Exception {
        PowerMockito.doNothing().when(mockedExecuteNodeActionImpl, "getAAIservice");
        Whitebox.invokeMethod(mockedExecuteNodeActionImpl, "initialize");
        verifyPrivate(mockedExecuteNodeActionImpl, times(1)).invoke("getAAIservice");
    }

    @Test
    public void testGetAAIservice() throws Exception {
        // sref is not null
        mockStatic(FrameworkUtil.class);
        Bundle mockedBundle = mock(Bundle.class);
        BundleContext mockedBundleContext = mock(BundleContext.class);
        ServiceReference mockedServiceReference = mock(ServiceReference.class);
        PowerMockito.when(FrameworkUtil.getBundle(AAIService.class)).thenReturn(mockedBundle);
        PowerMockito.doReturn(mockedBundleContext).when(mockedBundle).getBundleContext();
        PowerMockito.doReturn(mockedServiceReference).when(mockedBundleContext)
            .getServiceReference(AAIService.class.getName());

        Whitebox.invokeMethod(mockedExecuteNodeActionImpl, "getAAIservice");
        verify(mockedBundleContext, times(1)).getService(mockedServiceReference);

        // sref is null
        PowerMockito.doReturn(null).when(mockedBundleContext)
            .getServiceReference(AAIService.class.getName());
        Whitebox.invokeMethod(mockedExecuteNodeActionImpl, "getAAIservice");
        verify(mockedBundleContext, times(1)).getService(mockedServiceReference);
    }

    @Test
    public void testWaitMethod() throws Exception {
        mockStatic(Thread.class);
        params.put("waitTime", "1");
        mockedExecuteNodeActionImpl.waitMethod(params, svcLogicContext);
        verifyStatic(times(1));
    }

    @Test
    public void testGetResource() throws Exception {
        PowerMockito.doNothing().when(mockedExecuteNodeActionImpl, "initialize");
        PowerMockito.doReturn(queryStatus).when(aaiService).query(resourceType, false, null,
            resourceKey, prefix, null, svcLogicContext);

        mockedExecuteNodeActionImpl.getResource(params, svcLogicContext);

        verifyPrivate(mockedExecuteNodeActionImpl, times(1)).invoke("initialize");
        verify(aaiService, times(1)).query(resourceType, false, null,
            resourceKey, prefix, null, svcLogicContext);
        assertEquals(queryStatus.toString(), svcLogicContext.getAttribute("getResource_result"));
    }

    @Test
    public void testPostResource() throws Exception {


        PowerMockito.doNothing().when(mockedExecuteNodeActionImpl, "initialize");
        PowerMockito.doReturn(queryStatus).when(aaiService).update(eq(resourceType), eq(resourceKey), anyMap(),
            eq(prefix), eq(svcLogicContext));

        mockedExecuteNodeActionImpl.postResource(params, svcLogicContext);

        verifyPrivate(mockedExecuteNodeActionImpl, times(1)).invoke("initialize");
        verify(aaiService, times(1)).update(eq(resourceType), eq(resourceKey), anyMap(),
            eq(prefix), eq(svcLogicContext));
        assertEquals(svcLogicContext.getAttribute("postResource_result"), queryStatus.toString());
    }

    @Test
    public void testDeleteResource() throws Exception {
        PowerMockito.doNothing().when(mockedExecuteNodeActionImpl, "initialize");

        PowerMockito.doReturn(queryStatus).when(aaiService).delete(eq(resourceType), eq(resourceKey),
            eq(svcLogicContext));

        mockedExecuteNodeActionImpl.deleteResource(params, svcLogicContext);

        verifyPrivate(mockedExecuteNodeActionImpl, times(1)).invoke("initialize");
        verify(aaiService, times(1)).delete(eq(resourceType), eq(resourceKey),
            eq(svcLogicContext));
        assertEquals(svcLogicContext.getAttribute("deleteResource_result"), queryStatus.toString());
    }

    @Test
    public void testGetVnfHierarchySuccess() throws Exception {
        PowerMockito.doNothing().when(mockedExecuteNodeActionImpl, "initialize");
        PowerMockito.doNothing().when(mockedExecuteNodeActionImpl, "populateVnfcsDetailsinContext", anyMap(), eq
            (svcLogicContext));
        PowerMockito.when(aaiService.query(any(), eq(false), anyString(), any(), any(), anyString(),
            any(SvcLogicContext.class))).thenReturn(queryStatus);

        mockedExecuteNodeActionImpl.getVnfHierarchy(params, svcLogicContext);

        verifyPrivate(mockedExecuteNodeActionImpl, times(1)).invoke("initialize");
        assertEquals("0", svcLogicContext.getAttribute("VNF.VNFCCount"));
        assertEquals("SUCCESS", svcLogicContext.getAttribute("getVnfHierarchy_result"));
    }

    @Test(expected = APPCException.class)
    public void testGetVnfHierarchyFailure() throws Exception {
        queryStatus = SvcLogicResource.QueryStatus.FAILURE;
        PowerMockito.doNothing().when(mockedExecuteNodeActionImpl, "initialize");
        PowerMockito.doNothing().when(mockedExecuteNodeActionImpl, "populateVnfcsDetailsinContext", anyMap(),
            eq(svcLogicContext));
        PowerMockito.when(aaiService.query(any(), eq(false), anyString(), any(), any(), anyString(),
            any(SvcLogicContext.class))).thenReturn(queryStatus);

        mockedExecuteNodeActionImpl.getVnfHierarchy(params, svcLogicContext);

        verifyPrivate(mockedExecuteNodeActionImpl, times(1)).invoke("initialize");
        assertEquals("0", svcLogicContext.getAttribute("VNF.VNFCCount"));
        assertEquals("FAILURE", svcLogicContext.getAttribute("getVnfHierarchy_result"));
        assertTrue(svcLogicContext.getAttribute("output.status.message") != null);
    }

    @Test
    public void testPopulateVnfcsDetailsinContext() throws Exception {
        Map<String, Set<String>> vnfcHierarchyMap = new HashMap<>();
        Set<String> vServersList = new HashSet<>();
        vnfcHierarchyMap.put("SMP", vServersList);
        vServersList.add("smp-0-url");
        vServersList.add("smp-1-url");

        PowerMockito.doNothing().when(mockedExecuteNodeActionImpl, "initialize");
        PowerMockito.when(aaiService.query(eq("vnfc"), eq(false), anyString(),
            eq("vnfc-name = 'SMP'"), eq("vnfcRetrived"), anyString(), any(SvcLogicContext.class)))
            .thenReturn(queryStatus);

        Whitebox.invokeMethod(mockedExecuteNodeActionImpl, "populateVnfcsDetailsinContext",
            vnfcHierarchyMap, svcLogicContext);

        verify(mockedExecuteNodeActionImpl, times(1)).getResource(anyMap(),
            any(SvcLogicContext.class));
        verifyPrivate(mockedExecuteNodeActionImpl, times(1)).invoke("initialize");
        assertEquals(null, svcLogicContext.getAttribute("VNF.VNFC[0].TYPE"));
        assertEquals(null, svcLogicContext.getAttribute("VNF.VNFC[0].NAME"));
        assertEquals("2", svcLogicContext.getAttribute("VNF.VNFC[0].VM_COUNT"));
        assertTrue(vServersList.contains(svcLogicContext.getAttribute("VNF.VNFC[0].VM[0].URL")));
        assertTrue(vServersList.contains(svcLogicContext.getAttribute("VNF.VNFC[0].VM[1].URL")));
    }
}