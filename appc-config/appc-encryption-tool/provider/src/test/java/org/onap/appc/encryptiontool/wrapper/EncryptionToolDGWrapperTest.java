/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 Ericsson
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
 *
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.encryptiontool.wrapper;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.onap.appc.encryptiontool.fqdn.ParseAdminArtifcat;
import org.onap.ccsdk.sli.adaptors.resource.sql.SqlResource;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource.QueryStatus;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import com.fasterxml.jackson.core.JsonProcessingException;

@RunWith(PowerMockRunner.class)
@PrepareForTest(WrapperEncryptionTool.class)
public class EncryptionToolDGWrapperTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void testRunEncryption() throws SvcLogicException {
        EncryptionToolDGWrapper wrapper = EncryptionToolDGWrapper.initialise();
        Map<String, String> inParams = new HashMap<>();
        inParams.put("userName", "userName");
        inParams.put("password", "password");
        inParams.put("vnf_type", "vnf_type");
        SvcLogicContext ctx = new SvcLogicContext();
        PowerMockito.mockStatic(WrapperEncryptionTool.class);
        wrapper.runEncryption(inParams, ctx);
        PowerMockito.verifyStatic();
    }

    @Test
    public void testRunEncryptionNullVnfType() throws SvcLogicException {
        EncryptionToolDGWrapper wrapper = EncryptionToolDGWrapper.initialise();
        Map<String, String> inParams = new HashMap<>();
        inParams.put("userName", "userName");
        inParams.put("password", "password");
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage("username or Password is missing");
        wrapper.runEncryption(inParams, new SvcLogicContext());
    }

    @Test
    public void testGetPropertyAnsibleWithPayload() throws SvcLogicException {
        SqlResource sqlResource = Mockito.mock(SqlResource.class);
        EncryptionToolDGWrapper wrapper = new EncryptionToolDGWrapper(sqlResource);
        Map<String, String> inParams = new HashMap<>();
        populateParams(inParams);
        inParams.put("payloadFqdn", "payloadFqdn");
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vnf-type", "vnf-type");
        ctx.setAttribute("input.action", "input.action");
        ctx.setAttribute("APPC.protocol.PROTOCOL", "ansible");
        wrapper.getProperty(inParams, ctx);
        Mockito.verify(sqlResource).query(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.any(SvcLogicContext.class));
    }

    @Test
    public void testGetPropertyAnsibleWithoutPayload() throws SvcLogicException, JsonProcessingException, RuntimeException, IOException {
        SqlResource sqlResource = Mockito.mock(SqlResource.class);
        Mockito.when(sqlResource.query(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.any(SvcLogicContext.class))).thenReturn(QueryStatus.FAILURE);
        EncryptionToolDGWrapper wrapper = Mockito.spy(new EncryptionToolDGWrapper(sqlResource));
        Map<String, String> inParams = new HashMap<>();
        populateParams(inParams);
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vnf-type", "vnf-type");
        ctx.setAttribute("input.action", "input.action");
        ctx.setAttribute("APPC.protocol.PROTOCOL", "ansible");
        ctx.setAttribute("MULTIPLE", "2");
        ParseAdminArtifcat artifact = Mockito.mock(ParseAdminArtifcat.class);
        Mockito.when(artifact.retrieveFqdn(Mockito.any(SvcLogicContext.class))).thenReturn(":::");
        Whitebox.setInternalState(wrapper, "artifact", artifact);
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage("Error retrieving credentials");
        wrapper.getProperty(inParams, ctx);
    }

    @Test
    public void testGetPropertyAnsibleWithoutPayloadAndInvalidFQDN() throws SvcLogicException, JsonProcessingException, RuntimeException, IOException {
        SqlResource sqlResource = Mockito.mock(SqlResource.class);
        EncryptionToolDGWrapper wrapper = Mockito.spy(new EncryptionToolDGWrapper(sqlResource));
        Map<String, String> inParams = new HashMap<>();
        populateParams(inParams);
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vnf-type", "vnf-type");
        ctx.setAttribute("input.action", "input.action");
        ctx.setAttribute("APPC.protocol.PROTOCOL", "ansible");
        ctx.setAttribute("MULTIPLE", "2");
        ParseAdminArtifcat artifact = Mockito.mock(ParseAdminArtifcat.class);
        Mockito.when(artifact.retrieveFqdn(Mockito.any(SvcLogicContext.class))).thenReturn("TEST");
        Whitebox.setInternalState(wrapper, "artifact", artifact);
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage(": NOT_FOUND! No FQDN  match found in admin artifact  for ");
        wrapper.getProperty(inParams, ctx);
    }

    @Test
    public void testGetPropertyCountEqualsOne() throws SvcLogicException, JsonProcessingException, RuntimeException, IOException {
        SqlResource sqlResource = Mockito.mock(SqlResource.class);
        EncryptionToolDGWrapper wrapper = Mockito.spy(new EncryptionToolDGWrapper(sqlResource));
        Map<String, String> inParams = new HashMap<>();
        populateParams(inParams);
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vnf-type", "vnf-type");
        ctx.setAttribute("input.action", "input.action");
        ctx.setAttribute("APPC.protocol.PROTOCOL", "ansible");
        ctx.setAttribute("MULTIPLE", "1");
        ctx.setAttribute("USER-NAME", "USER-NAME");
        ctx.setAttribute("PASSWORD", "PASSWORD");
        ctx.setAttribute("PORT-NUMBER", "PORT-NUMBER");
        ctx.setAttribute("URL", "URL");
        ParseAdminArtifcat artifact = Mockito.mock(ParseAdminArtifcat.class);
        Mockito.when(artifact.retrieveFqdn(Mockito.any(SvcLogicContext.class))).thenReturn("TEST");
        Whitebox.setInternalState(wrapper, "artifact", artifact);
        wrapper.getProperty(inParams, ctx);
        assertEquals("PORT-NUMBER", ctx.getAttribute("prefix.port"));
    }

    @Test
    public void testGetPropertyCountZeroFailure() throws SvcLogicException, JsonProcessingException, RuntimeException, IOException {
        SqlResource sqlResource = Mockito.mock(SqlResource.class);
        Mockito.when(sqlResource.query(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.any(SvcLogicContext.class))).thenReturn(QueryStatus.FAILURE);
        EncryptionToolDGWrapper wrapper = Mockito.spy(new EncryptionToolDGWrapper(sqlResource));
        Map<String, String> inParams = new HashMap<>();
        populateParams(inParams);
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vnf-type", "vnf-type");
        ctx.setAttribute("input.action", "input.action");
        ctx.setAttribute("APPC.protocol.PROTOCOL", "ansible");
        ctx.setAttribute("MULTIPLE", "0");
        ParseAdminArtifcat artifact = Mockito.mock(ParseAdminArtifcat.class);
        Mockito.when(artifact.retrieveFqdn(Mockito.any(SvcLogicContext.class))).thenReturn("TEST");
        Whitebox.setInternalState(wrapper, "artifact", artifact);
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage("Error retrieving credentials");
        wrapper.getProperty(inParams, ctx);
    }

    @Test
    public void testGetPropertyCountZeroNotFound() throws SvcLogicException, JsonProcessingException, RuntimeException, IOException {
        SqlResource sqlResource = Mockito.mock(SqlResource.class);
        Mockito.when(sqlResource.query(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.any(SvcLogicContext.class))).thenReturn(QueryStatus.NOT_FOUND);
        EncryptionToolDGWrapper wrapper = Mockito.spy(new EncryptionToolDGWrapper(sqlResource));
        Map<String, String> inParams = new HashMap<>();
        populateParams(inParams);
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vnf-type", "vnf-type");
        ctx.setAttribute("input.action", "input.action");
        ctx.setAttribute("APPC.protocol.PROTOCOL", "ansible");
        ctx.setAttribute("MULTIPLE", "0");
        ParseAdminArtifcat artifact = Mockito.mock(ParseAdminArtifcat.class);
        Mockito.when(artifact.retrieveFqdn(Mockito.any(SvcLogicContext.class))).thenReturn("TEST");
        Whitebox.setInternalState(wrapper, "artifact", artifact);
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage(":: NOT_FOUND! No data found in device_authentication table for");
        wrapper.getProperty(inParams, ctx);
    }

    @Test
    public void testGetPropertyNonAnsibleFailure() throws SvcLogicException, JsonProcessingException, RuntimeException, IOException {
        SqlResource sqlResource = Mockito.mock(SqlResource.class);
        Mockito.when(sqlResource.query(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.any(SvcLogicContext.class))).thenReturn(QueryStatus.FAILURE);
        EncryptionToolDGWrapper wrapper = Mockito.spy(new EncryptionToolDGWrapper(sqlResource));
        Map<String, String> inParams = new HashMap<>();
        populateParams(inParams);
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vnf-type", "vnf-type");
        ctx.setAttribute("input.action", "input.action");
        ctx.setAttribute("APPC.protocol.PROTOCOL", "TEST");
        ctx.setAttribute("MULTIPLE", "0");
        ParseAdminArtifcat artifact = Mockito.mock(ParseAdminArtifcat.class);
        Mockito.when(artifact.retrieveFqdn(Mockito.any(SvcLogicContext.class))).thenReturn("TEST");
        Whitebox.setInternalState(wrapper, "artifact", artifact);
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage("Error retrieving credentials");
        wrapper.getProperty(inParams, ctx);
    }

    @Test
    public void testGetPropertyNonAnsibleNotFound() throws SvcLogicException, JsonProcessingException, RuntimeException, IOException {
        SqlResource sqlResource = Mockito.mock(SqlResource.class);
        Mockito.when(sqlResource.query(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.any(SvcLogicContext.class))).thenReturn(QueryStatus.NOT_FOUND);
        EncryptionToolDGWrapper wrapper = Mockito.spy(new EncryptionToolDGWrapper(sqlResource));
        Map<String, String> inParams = new HashMap<>();
        populateParams(inParams);
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vnf-type", "vnf-type");
        ctx.setAttribute("input.action", "input.action");
        ctx.setAttribute("APPC.protocol.PROTOCOL", "TEST");
        ctx.setAttribute("MULTIPLE", "0");
        ParseAdminArtifcat artifact = Mockito.mock(ParseAdminArtifcat.class);
        Mockito.when(artifact.retrieveFqdn(Mockito.any(SvcLogicContext.class))).thenReturn("TEST");
        Whitebox.setInternalState(wrapper, "artifact", artifact);
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage(":: NOT_FOUND! No data found in device_authentication table for");
        wrapper.getProperty(inParams, ctx);
    }

    private void populateParams(Map<String, String> inParams) {
        inParams.put("prefix", "prefix");
        inParams.put("tenantAai", "tenantAai");
        inParams.put("cldOwnerAai", "cldOwnerAai");
        inParams.put("cldRegionAai", "cldRegionAai");
        inParams.put("payloadFqdn", "");
        inParams.put("payloadTenant", "payloadTenant");
        inParams.put("payloadCloudOwner", "payloadCloudOwner");
        inParams.put("payloadCloudRegion", "payloadCloudRegion");
    }
}
