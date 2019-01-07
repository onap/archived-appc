/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 Ericsson
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

package org.onap.appc.dg.aai.impl;

import static org.hamcrest.CoreMatchers.isA;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.onap.appc.dg.aai.exception.AAIQueryException;
import org.onap.appc.dg.aai.objects.AAIQueryResult;
import org.onap.appc.dg.aai.objects.Relationship;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.i18n.Msg;
import org.onap.ccsdk.sli.adaptors.aai.AAIClient;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;
import org.osgi.framework.FrameworkUtil;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.att.eelf.i18n.EELFResourceManager;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.powermock.api.mockito.PowerMockito;

@RunWith(PowerMockRunner.class)
@PrepareForTest(FrameworkUtil.class)
public class AAIPluginImplTest {

    private final BundleContext bundleContext = Mockito.mock(BundleContext.class);
    private final Bundle bundleService = Mockito.mock(Bundle.class);
    private final ServiceReference sref = Mockito.mock(ServiceReference.class);
    private final AAIClient aaiClient = Mockito.mock(AAIClient.class);
    private SvcLogicContext ctx;
    private Map<String, String> params;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        PowerMockito.mockStatic(FrameworkUtil.class);
        PowerMockito.when(FrameworkUtil.getBundle(Matchers.any(Class.class))).thenReturn(bundleService);
        PowerMockito.when(bundleService.getBundleContext()).thenReturn(bundleContext);
        PowerMockito.when(bundleContext.getServiceReference(Matchers.any(Class.class))).thenReturn(sref);
        PowerMockito.when(bundleContext.<AAIClient>getService(sref)).thenReturn(aaiClient);
        params = new HashMap<String, String>();
        params.put(Constants.AAI_INPUT_DATA + ".suffix", "TEST_DATA");
    }

    @Test
    public void testPostGenericVnfDataNotFound() throws APPCException, SvcLogicException {
        SvcLogicResource.QueryStatus status = SvcLogicResource.QueryStatus.NOT_FOUND;
        Mockito.doReturn(status).when(aaiClient).update(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyMap(), Mockito.anyString(), Mockito.any(SvcLogicContext.class));
        ctx = new SvcLogicContext();
        AAIPluginImpl impl = new AAIPluginImpl();
        impl.initialize();
        expectedEx.expect(APPCException.class);
        expectedEx.expectMessage("VNF not found with vnf_id null");
        impl.postGenericVnfData(params, ctx);
    }

    @Test
    public void testPostGenericVnfDataFailure() throws APPCException, SvcLogicException {
        SvcLogicResource.QueryStatus status = SvcLogicResource.QueryStatus.FAILURE;
        Mockito.doReturn(status).when(aaiClient).update(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyMap(), Mockito.anyString(), Mockito.any(SvcLogicContext.class));
        ctx = new SvcLogicContext();
        AAIPluginImpl impl = new AAIPluginImpl();
        impl.initialize();
        expectedEx.expect(APPCException.class);
        expectedEx.expectMessage("Error Querying AAI with vnfID = null");
        impl.postGenericVnfData(params, ctx);
    }

    @Test
    public void testPostGenericVnfDataSucces() throws APPCException, SvcLogicException {
        SvcLogicResource.QueryStatus status = SvcLogicResource.QueryStatus.SUCCESS;
        Mockito.doReturn(status).when(aaiClient).update(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyMap(), Mockito.anyString(), Mockito.any(SvcLogicContext.class));
        ctx = new SvcLogicContext();
        AAIPluginImpl impl = new AAIPluginImpl();
        impl.initialize();
        impl.postGenericVnfData(params, ctx);
        Assert.assertThat(ctx.getAttribute(org.onap.appc.Constants.ATTRIBUTE_SUCCESS_MESSAGE), CoreMatchers.containsString(
                "Operation PostGenericVnfData succeed for VNF ID null"));
    }

    @Test
    public void testPostGenericVnfDataFailureThrownExeption() throws APPCException, SvcLogicException {
        Mockito.doThrow(new SvcLogicException()).when(aaiClient).update(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyMap(), Mockito.anyString(), Mockito.any(SvcLogicContext.class));
        ctx = new SvcLogicContext();
        AAIPluginImpl impl = new AAIPluginImpl();
        impl.initialize();
        expectedEx.expect(APPCException.class);
        expectedEx.expectCause(isA(SvcLogicException.class));
        impl.postGenericVnfData(params, ctx);
    }

    @Test
    public void testGetGenericVnfDataNotFound() throws APPCException, SvcLogicException {
        SvcLogicResource.QueryStatus notFound = SvcLogicResource.QueryStatus.NOT_FOUND;
        Mockito.doReturn(notFound).when(aaiClient).query(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(SvcLogicContext.class));
        ctx = new SvcLogicContext();
        AAIPluginImpl impl = new AAIPluginImpl();
        impl.initialize();
        expectedEx.expect(APPCException.class);
        expectedEx.expectMessage("VNF not found with vnf_id null");
        impl.getGenericVnfData(params, ctx);
    }

    @Test
    public void testGetGenericVnfDataFailure() throws APPCException, SvcLogicException {
        SvcLogicResource.QueryStatus status = SvcLogicResource.QueryStatus.FAILURE;
        Mockito.doReturn(status).when(aaiClient).query(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(SvcLogicContext.class));
        ctx = new SvcLogicContext();
        AAIPluginImpl impl = new AAIPluginImpl();
        impl.initialize();
        expectedEx.expect(APPCException.class);
        expectedEx.expectMessage("Error Querying AAI with vnfID = null");
        impl.getGenericVnfData(params, ctx);
    }

    @Test
    public void testGetGenericVnfDataSucces() throws APPCException, SvcLogicException {
        SvcLogicResource.QueryStatus status = SvcLogicResource.QueryStatus.SUCCESS;
        Mockito.doReturn(status).when(aaiClient).query(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(SvcLogicContext.class));
        ctx = new SvcLogicContext();
        AAIPluginImpl impl = new AAIPluginImpl();
        impl.initialize();
        impl.getGenericVnfData(params, ctx);
        Assert.assertThat(ctx.getAttribute(org.onap.appc.Constants.ATTRIBUTE_SUCCESS_MESSAGE), CoreMatchers.containsString(
                "Operation GetGenericVnfData succeed for VNF ID null"));
    }

    @Test
    public void testGetGenericVnfDataFailureThrownExeption() throws APPCException, SvcLogicException {
        Mockito.doThrow(new SvcLogicException()).when(aaiClient).query(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(SvcLogicContext.class));
        ctx = new SvcLogicContext();
        AAIPluginImpl impl = new AAIPluginImpl();
        impl.initialize();
        expectedEx.expect(APPCException.class);
        expectedEx.expectCause(isA(SvcLogicException.class));
        impl.getGenericVnfData(params, ctx);
    }

    @Test
    public void testGetVnfHierarchyAaiExceptionFlow() throws APPCException, SvcLogicException {
        SvcLogicResource.QueryStatus status = SvcLogicResource.QueryStatus.FAILURE;
        Mockito.doReturn(status).when(aaiClient).query(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(SvcLogicContext.class));
        ctx = new SvcLogicContext();
        params.put(Constants.RESOURCEKEY, "TEST_RESOURCE_KEY");
        AAIPluginImpl impl = new AAIPluginImpl();
        impl.initialize();
        expectedEx.expect(APPCException.class);
        expectedEx.expectMessage("Error Retrieving VNF hierarchy");
        impl.getVnfHierarchy(params, ctx);
    }

    @Test
    public void testGetVnfHierarchyAaiExceptionFlow2() throws APPCException, SvcLogicException {
        Mockito.doThrow(new SvcLogicException()).when(aaiClient).query(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(SvcLogicContext.class));
        ctx = new SvcLogicContext();
        params.put(Constants.RESOURCEKEY, "TEST_RESOURCE_KEY");
        AAIPluginImpl impl = new AAIPluginImpl();
        impl.initialize();
        expectedEx.expect(APPCException.class);
        expectedEx.expectMessage("Error Retrieving VNF hierarchy");
        impl.getVnfHierarchy(params, ctx);
    }

    @Test
    public void testGetVnfHierarchyNoVMs() throws APPCException, SvcLogicException {
        SvcLogicResource.QueryStatus status = SvcLogicResource.QueryStatus.SUCCESS;
        Mockito.doReturn(status).when(aaiClient).query(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(SvcLogicContext.class));
        ctx = new SvcLogicContext();
        params.put(Constants.RESOURCEKEY, "TEST_RESOURCE_KEY");
        AAIPluginImpl impl = new AAIPluginImpl();
        impl.initialize();
        impl.getVnfHierarchy(params, ctx);
        Assert.assertEquals("0", ctx.getAttribute("VNF.VMCount"));
    }

    @Test
    public void testGetVnfHierarchy() throws APPCException, SvcLogicException, AAIQueryException {
        String vnfId = "TEST_RESOURCE_KEY";
        SvcLogicResource.QueryStatus status = SvcLogicResource.QueryStatus.SUCCESS;
        Mockito.doReturn(status).when(aaiClient).query(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(SvcLogicContext.class));
        ctx = new SvcLogicContext();
        params.put(Constants.RESOURCEKEY, "TEST_RESOURCE_KEY");
        AAIPluginImpl impl = Mockito.spy(new AAIPluginImpl());
        AAIQueryResult aaiQueryResult = new AAIQueryResult();
        aaiQueryResult.getAdditionProperties().put("vnf-type", "TEST_VNF_TYPE");
        Relationship relationship = new Relationship();
        relationship.setRelatedTo("vserver");
        aaiQueryResult.getRelationshipList().add(relationship);
        Mockito.doReturn(aaiQueryResult).when(impl).readVnf(vnfId);
        AAIQueryResult vmQueryResult = new AAIQueryResult();
        vmQueryResult.getAdditionProperties().put("vserver-selflink", "TEST_VM_NAME");
        Relationship vmRelationship = new Relationship();
        vmRelationship.setRelatedTo("vnfc");
        vmQueryResult.getRelationshipList().add(vmRelationship);
        Mockito.doReturn(vmQueryResult).when(impl).readVM(null, null, null, null);
        impl.initialize();
        impl.getVnfHierarchy(params, ctx);
        Assert.assertEquals(EELFResourceManager.format(Msg.SUCCESS_EVENT_MESSAGE, "GetVNFHierarchy", "VNF ID " + vnfId),
                ctx.getAttribute(org.onap.appc.Constants.ATTRIBUTE_SUCCESS_MESSAGE));
    }

    @Test
    public void testReadVM() throws AAIQueryException {
        AAIPluginImpl impl = Mockito.spy(new AAIPluginImpl());
        ctx = new SvcLogicContext();
        ctx.setAttribute("VM.relationship-list.relationship_length", "1");
        ctx.setAttribute("VM.relationship-list.relationship[0].relationship-data_length", "1");
        ctx.setAttribute("VM.relationship-list.relationship[0].related-to-property_length", "1");
        Mockito.doReturn(ctx).when(impl).readResource("vserver.vserver-id = 'null' AND tenant.tenant_id = 'null' AND "
                + "cloud-region.cloud-owner = 'null' AND cloud-region.cloud-region-id = 'null'", "VM", "vserver");
        impl.initialize();
        Assert.assertEquals(null, impl.readVM(null, null, null, null).getAdditionProperties().get("resource-version"));
    }

    @Test
    public void testGetResource() throws SvcLogicException, APPCException {
        SvcLogicResource.QueryStatus status = SvcLogicResource.QueryStatus.SUCCESS;
        Mockito.doReturn(status).when(aaiClient).query(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(SvcLogicContext.class));
        ctx = new SvcLogicContext();
        params.put(Constants.RESOURCEKEY, "TEST_RESOURCE_KEY");
        AAIPluginImpl impl = new AAIPluginImpl();
        impl.initialize();
        impl.getResource(params, ctx);
        Assert.assertEquals("SUCCESS",ctx.getAttribute("getResource_result"));
    }

    @Test
    public void testPostResource() throws SvcLogicException, APPCException {
        SvcLogicResource.QueryStatus status = SvcLogicResource.QueryStatus.SUCCESS;
        Mockito.doReturn(status).when(aaiClient).update(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyMap(), Mockito.anyString(), Mockito.any(SvcLogicContext.class));
        ctx = new SvcLogicContext();
        params.put(Constants.RESOURCEKEY, "TEST_RESOURCE_KEY");
        AAIPluginImpl impl = new AAIPluginImpl();
        impl.initialize();
        impl.postResource(params, ctx);
        Assert.assertEquals("SUCCESS",ctx.getAttribute("postResource_result"));
    }

    @Test
    public void testDeleteResource() throws SvcLogicException, APPCException {
        SvcLogicResource.QueryStatus status = SvcLogicResource.QueryStatus.SUCCESS;
        Mockito.doReturn(status).when(aaiClient).delete(Mockito.anyString(), Mockito.anyString(),
                Mockito.any(SvcLogicContext.class));
        ctx = new SvcLogicContext();
        params.put(Constants.RESOURCEKEY, "TEST_RESOURCE_KEY");
        AAIPluginImpl impl = new AAIPluginImpl();
        impl.initialize();
        impl.deleteResource(params, ctx);
        Assert.assertEquals("SUCCESS",ctx.getAttribute("deleteResource_result"));
    }
}
