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

package org.openecomp.appc.dg.aai.impl;

import org.junit.runner.RunWith;
import org.openecomp.appc.dg.aai.Constants;
import org.openecomp.appc.dg.aai.impl.AAIPluginImpl;
import org.openecomp.appc.dg.common.dao.DAOService;
import org.openecomp.appc.dg.common.impl.LicenseManagerImpl;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.sdnc.sli.SvcLogicContext;
import org.openecomp.sdnc.sli.SvcLogicResource;
import org.openecomp.sdnc.sli.aai.AAIClient;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;


@RunWith(PowerMockRunner.class)
@PrepareForTest({AAIPluginImpl.class, FrameworkUtil.class})
public class AAIPluginImplTest {
    private AAIPluginImpl aaiPlugin;
    private AAIClientMock aaiClient;

    private final BundleContext bundleContext= Mockito.mock(BundleContext.class);
    private final Bundle bundleService=Mockito.mock(Bundle.class);
    private final ServiceReference sref=Mockito.mock(ServiceReference.class);

    String prefix = "aai.input.data";
    String vnfId = "test_VNF";
    String vnfId1 = "test_VNF1";
    String vnfId2 = "test_VNF2";
    String vnfId3 = "test_VNF3";

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        aaiClient = new AAIClientMock();
        PowerMockito.mockStatic(FrameworkUtil.class);
        PowerMockito.when(FrameworkUtil.getBundle(Matchers.any(Class.class))).thenReturn(bundleService);
        PowerMockito.when(bundleService.getBundleContext()).thenReturn(bundleContext);
        PowerMockito.when(bundleContext.getServiceReference(Matchers.any(Class.class))).thenReturn(sref);
        PowerMockito.when(bundleContext.getService(sref)).thenReturn(aaiClient);
        aaiPlugin = new AAIPluginImpl();


    }




    @Test
    public void testPostGenericVnfData() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(prefix+"."+"license-key-uuid", "123");
        params.put(prefix+"."+"license-assignment-group-uuid", "1234");
        params.put(prefix+"."+"data.license-key", "12345");

        HashMap<String, String> mockAAI = new HashMap<>();
        aaiClient.setMockAAI(mockAAI);
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("aai.vnfID", vnfId);
        ctx.setAttribute("aai.prefix", prefix);

        aaiPlugin.postGenericVnfData(params, ctx);

        Assert.assertEquals("wrong license-key-uuid","123", mockAAI.get("license-key-uuid"));
        Assert.assertEquals("wrong license-assignment-group-uuid","1234", mockAAI.get("license-assignment-group-uuid"));
        Assert.assertEquals("wrong data.license-key","12345", mockAAI.get("data.license-key"));
    }


    @Test
    public void testPostGenericVnfDataNegativeVnfNotFound() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(prefix+"."+"license-key-uuid", "123");
        params.put(prefix+"."+"license-assignment-group-uuid", "1234");
        params.put(prefix+"."+"data.license-key", "12345");

        HashMap<String, String> mockAAI = new HashMap<>();

        aaiClient.setMockAAI(mockAAI);
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("aai.vnfID", vnfId1);
        ctx.setAttribute("aai.prefix", prefix);

        try {
            aaiPlugin.postGenericVnfData(params, ctx);
            Assert.assertTrue(false);
        } catch (APPCException e) {
            Assert.assertNotNull(ctx.getAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE));
        }

    }


    @Test
    public void testPostGenericVnfDataNegativeFailure() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(prefix+"."+"license-key-uuid", "123");
        params.put(prefix+"."+"license-assignment-group-uuid", "1234");
        params.put(prefix+"."+"data.license-key", "12345");

        HashMap<String, String> mockAAI = new HashMap<>();

        aaiClient.setMockAAI(mockAAI);
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("aai.vnfID", vnfId2);
        ctx.setAttribute("aai.prefix", prefix);

        try {
            aaiPlugin.postGenericVnfData(params, ctx);
            Assert.assertTrue(false);
        } catch (APPCException e) {
            Assert.assertNotNull(ctx.getAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE));
        }

    }


    @Test
    public void testPostGenericVnfDataNegativeSvcLogicException() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(prefix+"."+"license-key-uuid", "123");
        params.put(prefix+"."+"license-assignment-group-uuid", "1234");
        params.put(prefix+"."+"data.license-key", "12345");

        HashMap<String, String> mockAAI = new HashMap<>();

        aaiClient.setMockAAI(mockAAI);
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("aai.vnfID", vnfId3);
        ctx.setAttribute("aai.prefix", prefix);

        try {
            aaiPlugin.postGenericVnfData(params, ctx);
            Assert.assertTrue(false);
        } catch (APPCException e) {
            Assert.assertNotNull(ctx.getAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE));
        }

    }

    @Test
    public void testGetGenericVnfData() throws Exception {
        String vnfNameKey = "vnf-name";
        String vnfType = "VSCP";
        String vnfTypeKey = "vnf-type";
        String provStatus = "Active";
        String provStatusKey = "prov-status";
        String orchestrationStatus = "Running";
        String orchestrationStatusKey = "orchestration-status";

        Map<String, String> params = new HashMap<>();
        HashMap<String, String> mockAAI = new HashMap<>();
        mockAAI.put(vnfNameKey,vnfId);
        mockAAI.put(vnfTypeKey,vnfType);
        mockAAI.put(provStatusKey, provStatus);
        mockAAI.put(orchestrationStatusKey, orchestrationStatus);
        aaiClient.setMockAAI(mockAAI);

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("aai.vnfID", vnfId);
        ctx.setAttribute("aai.prefix", prefix);


        aaiPlugin.getGenericVnfData(params, ctx);

        Assert.assertEquals("wrong "+vnfNameKey,vnfId, ctx.getAttribute(prefix + "." + vnfNameKey));
        Assert.assertEquals("wrong "+orchestrationStatusKey,orchestrationStatus, ctx.getAttribute(prefix + "." + orchestrationStatusKey));
        Assert.assertEquals("wrong "+vnfTypeKey,vnfType, ctx.getAttribute(prefix + "." +  vnfTypeKey));
        Assert.assertEquals("wrong "+provStatusKey,provStatus, ctx.getAttribute(prefix + "." + provStatusKey ));
    }




    @Test
    public void testGetGenericVnfDataNegativeVnfNotFound() throws Exception {

        Map<String, String> params = new HashMap<>();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("aai.vnfID", vnfId1);
        ctx.setAttribute("aai.prefix", prefix);


        try {
            aaiPlugin.getGenericVnfData(params, ctx);
            Assert.assertTrue(false);
        } catch (APPCException e) {
            Assert.assertNotNull(ctx.getAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE));
        }
    }


    @Test
    public void testGetGenericVnfDataNegativeFailure() throws Exception {

        Map<String, String> params = new HashMap<>();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("aai.vnfID", vnfId2);
        ctx.setAttribute("aai.prefix", prefix);

        try {
            aaiPlugin.getGenericVnfData(params, ctx);
            Assert.assertTrue(false);
        } catch (APPCException e) {
            Assert.assertNotNull(ctx.getAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE));
        }
    }


    @Test
    public void testGetGenericVnfDataNegativeSvcLogicException() throws Exception {

        Map<String, String> params = new HashMap<>();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("aai.vnfID", vnfId3);
        ctx.setAttribute("aai.prefix", prefix);

        try {
            aaiPlugin.getGenericVnfData(params, ctx);
            Assert.assertTrue(false);
        } catch (APPCException e) {
            Assert.assertNotNull(ctx.getAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE));
        }
    }

}
