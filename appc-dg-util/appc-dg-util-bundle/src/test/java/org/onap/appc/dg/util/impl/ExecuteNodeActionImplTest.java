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

package org.onap.appc.dg.util.impl;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.appc.exceptions.APPCException;
import org.onap.ccsdk.sli.adaptors.aai.AAIService;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource.QueryStatus;

@RunWith(MockitoJUnitRunner.class)
public class ExecuteNodeActionImplTest {

    private static final String resourceType = "resourceType";
    private static final String prefix = "prefix";
    private static final String resourceKey = "resourceKey";
    private static final String attributeName = "attributeName";
    private static final String attributeValue = "attributeValue";
    private static final Map<String, String> params = new HashMap<>();
    private static final SvcLogicContext SVC_LOGIC_CONTEXT = new SvcLogicContext();
    private static final SvcLogicResource.QueryStatus SUCCESS_STATUS = SvcLogicResource.QueryStatus.SUCCESS;
    private static final QueryStatus FAILED_STATUS = SvcLogicResource.QueryStatus.FAILURE;

    @Mock
    private AAIServiceFactory aaiServiceFactory;
    @Mock
    private AAIService aaiService;
    private ExecuteNodeActionImpl executeNodeAction;

    @Before
    public void setUp() {
        executeNodeAction = new ExecuteNodeActionImpl(aaiServiceFactory);
        given(aaiServiceFactory.getAAIService()).willReturn(aaiService);

        params.put("resourceType", resourceType);
        params.put("prefix", prefix);
        params.put("resourceKey", resourceKey);
        params.put("attributeName", attributeName);
        params.put("attributeValue", attributeValue);
        params.put("waitTime", "1");
    }

    @Test
    public void testWaitMethod() throws Exception {
        executeNodeAction.waitMethod(params, SVC_LOGIC_CONTEXT);
    }

    @Test
    public void testGetResource() throws Exception {
        given(aaiService.query(any(), Mockito.anyBoolean(),
            any(), any(), any(), any(),
            any(SvcLogicContext.class))).willReturn(SUCCESS_STATUS);

        executeNodeAction.getResource(params, SVC_LOGIC_CONTEXT);

        verify(aaiService, times(1)).query(resourceType, false, null, resourceKey, prefix, null, SVC_LOGIC_CONTEXT);
        assertEquals(SUCCESS_STATUS.toString(), SVC_LOGIC_CONTEXT.getAttribute("getResource_result"));
    }

    @Test
    public void testPostResource() throws Exception {
        given(aaiService.update(eq(resourceType), eq(resourceKey), anyMap(),
            eq(prefix), eq(SVC_LOGIC_CONTEXT))).willReturn(SUCCESS_STATUS);

        executeNodeAction.postResource(params, SVC_LOGIC_CONTEXT);

        verify(aaiService, times(1)).update(eq(resourceType), eq(resourceKey), anyMap(), eq(prefix),
            eq(SVC_LOGIC_CONTEXT));
        assertEquals(SVC_LOGIC_CONTEXT.getAttribute("postResource_result"), SUCCESS_STATUS.toString());
    }

    @Test
    public void testDeleteResource() throws Exception {
        given(aaiService.delete(eq(resourceType), eq(resourceKey),
            eq(SVC_LOGIC_CONTEXT))).willReturn(SUCCESS_STATUS);

        executeNodeAction.deleteResource(params, SVC_LOGIC_CONTEXT);

        verify(aaiService, times(1)).delete(eq(resourceType), eq(resourceKey), eq(SVC_LOGIC_CONTEXT));
        assertEquals(SVC_LOGIC_CONTEXT.getAttribute("deleteResource_result"), SUCCESS_STATUS.toString());
    }

    @Test
    public void testGetVnfHierarchySuccess() throws Exception {
        given(aaiService.query(any(), Mockito.anyBoolean(),
            any(), any(), any(), any(),
            any(SvcLogicContext.class))).willReturn(SUCCESS_STATUS);

        executeNodeAction.getVnfHierarchy(params, SVC_LOGIC_CONTEXT);

        assertEquals("0", SVC_LOGIC_CONTEXT.getAttribute("VNF.VNFCCount"));
        assertEquals("SUCCESS", SVC_LOGIC_CONTEXT.getAttribute("getVnfHierarchy_result"));
    }

    @Test(expected = APPCException.class)
    public void testGetVnfHierarchyFailure() throws Exception {
        given(aaiService.query(any(), Mockito.anyBoolean(),
            any(), any(), any(), any(),
            any(SvcLogicContext.class))).willReturn(FAILED_STATUS);

        executeNodeAction.getVnfHierarchy(params, SVC_LOGIC_CONTEXT);

        assertEquals("0", SVC_LOGIC_CONTEXT.getAttribute("VNF.VNFCCount"));
        assertEquals("FAILURE", SVC_LOGIC_CONTEXT.getAttribute("getVnfHierarchy_result"));
        assertTrue(SVC_LOGIC_CONTEXT.getAttribute("output.status.message") != null);
    }

    @Test
    public void testPopulateVnfcsDetailsinContext() throws Exception {
        Map<String, Set<String>> vnfcHierarchyMap = new HashMap<>();
        Set<String> vServersList = new HashSet<>();
        vnfcHierarchyMap.put("SMP", vServersList);
        vServersList.add("smp-0-url");
        vServersList.add("smp-1-url");

        given(aaiService.query(eq("vnfc"), eq(false), anyString(), eq("vnfc-name = 'SMP'"),
            eq("vnfcRetrived"), anyString(), any(SvcLogicContext.class))).willReturn(SUCCESS_STATUS);

        executeNodeAction.populateVnfcsDetailsinContext(vnfcHierarchyMap, SVC_LOGIC_CONTEXT);

        assertEquals(null, SVC_LOGIC_CONTEXT.getAttribute("VNF.VNFC[0].TYPE"));
        assertEquals(null, SVC_LOGIC_CONTEXT.getAttribute("VNF.VNFC[0].NAME"));
        assertEquals("2", SVC_LOGIC_CONTEXT.getAttribute("VNF.VNFC[0].VM_COUNT"));
        assertTrue(vServersList.contains(SVC_LOGIC_CONTEXT.getAttribute("VNF.VNFC[0].VM[0].URL")));
        assertTrue(vServersList.contains(SVC_LOGIC_CONTEXT.getAttribute("VNF.VNFC[0].VM[1].URL")));
    }
}
