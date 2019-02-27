/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2018-2019 IBM.
 * ================================================================================
 * Modifications Copyright (C) 2019 Ericsson
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

package org.onap.appc.data.services.node;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.onap.appc.data.services.node.ConfigResourceNode.CONFIG_FILES_PREFIX;
import static org.onap.appc.data.services.node.ConfigResourceNode.CONFIG_FILE_ID_PARAM;
import static org.onap.appc.data.services.node.ConfigResourceNode.CONFIG_PARAMS;
import static org.onap.appc.data.services.node.ConfigResourceNode.CONF_ACTION_PREFIX;
import static org.onap.appc.data.services.node.ConfigResourceNode.DEVICE_CONF_FILE_TYPE;
import static org.onap.appc.data.services.node.ConfigResourceNode.DEVICE_CONF_PREFIX;
import static org.onap.appc.data.services.node.ConfigResourceNode.DEVICE_PROTOCOL_PREFIX;
import static org.onap.appc.data.services.node.ConfigResourceNode.FAILURE_FILE_TYPE;
import static org.onap.appc.data.services.node.ConfigResourceNode.FAILURE_PREFIX;
import static org.onap.appc.data.services.node.ConfigResourceNode.FILE_CATEGORY_PARAM;
import static org.onap.appc.data.services.node.ConfigResourceNode.LOG_FILE_TYPE;
import static org.onap.appc.data.services.node.ConfigResourceNode.LOG_PREFIX;
import static org.onap.appc.data.services.node.ConfigResourceNode.MAX_CONF_FILE_PREFIX;
import static org.onap.appc.data.services.node.ConfigResourceNode.PREPARE_RELATIONSHIP_PARAM;
import static org.onap.appc.data.services.node.ConfigResourceNode.SDC_IND;
import static org.onap.appc.data.services.node.ConfigResourceNode.SITE_LOCATION_PARAM;
import static org.onap.appc.data.services.node.ConfigResourceNode.SUCCESS_FILE_TYPE;
import static org.onap.appc.data.services.node.ConfigResourceNode.SUCCESS_PREFIX;
import static org.onap.appc.data.services.node.ConfigResourceNode.TMP_CONVERTCONFIG_ESC_DATA;
import static org.onap.appc.data.services.node.ConfigResourceNode.TMP_MERGE_MERGED_DATA;
import static org.onap.appc.data.services.node.ConfigResourceNode.UNABLE_TO_SAVE_RELATIONSHIP_STR;
import static org.onap.appc.data.services.node.ConfigResourceNode.UPLOAD_CONFIG_ID_PARAM;
import static org.onap.appc.data.services.node.ConfigResourceNode.UPLOAD_CONFIG_INFO_PREFIX;
import static org.onap.appc.data.services.node.ConfigResourceNode.UPLOAD_CONFIG_PREFIX;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.onap.appc.data.services.AppcDataServiceConstant;
import org.onap.appc.data.services.db.DGGeneralDBService;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource.QueryStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

public class ConfigResourceNodeTest {

    private static final String TEST_MESSAGE = "TEST_MESSAGE";
    private static final String SOME_FILE_CATEGORY = "some file category";
    private static final String SOME_FILE_ID = "some file id";
    private static final String SOME_PREFIX = "some prefix";
    private static final String TEST = "test";
    private HashMap<String, String> inParams;
    private SvcLogicContext contextMock;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        contextMock = mock(SvcLogicContext.class);
        inParams = new HashMap<>();
    }

    /**
     * Example data:
     * <p>
     * {"capabilities":{"vnfc":[],"vm":[{"ConfigureTest":["SSC","MMSC"]}],"vf-module":[],"vnf":["ConfigModify","HealthCheck"]}}
     */
    @Test
    public void shouldProcessCapabilitiesForVMLevel() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        ConfigResourceNode node = new ConfigResourceNode(DGGeneralDBService.initialise());
        String findCapability = "Restart";
        JsonNodeFactory.instance.objectNode();
        String subCaps =
                "[{\"Restart\":[\"SSC\",\"MMC\"]},{\"Rebuild\":[\"SSC\"]},{\"Migrate\":[\"SSC\"]},{\"Snapshot\":[\"SSC\"]},{\"Start\":[\"SSC\"]},{\"Stop\":[\"SSC\"]}]";
        ObjectMapper m = new ObjectMapper();
        JsonNode subCapabilities = m.readTree(subCaps);
        String vServerId = "testServer";
        ctx.setAttribute("tmp.vnfInfo.vm.vnfc.vnfc-function-code", "MMC");
        ctx.setAttribute("tmp.vnfInfo.vm.vnfc.vnfc-name", "testVnfc");
        node.processCapabilitiesForVMLevel(vServerId, ctx, findCapability, subCapabilities);
        String result = ctx.getAttribute("capabilities");
        assertEquals(result, "Supported");
    }

    @Test
    public void shouldProcessCapabilitiesForVMLevelNoSubCapabilities() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        ConfigResourceNode node = new ConfigResourceNode(DGGeneralDBService.initialise());
        String findCapability = "Restart";
        JsonNodeFactory.instance.objectNode();
        String subCaps = "{}";
        ObjectMapper m = new ObjectMapper();
        JsonNode subCapabilities = m.readTree(subCaps);
        String vServerId = "testServer";
        ctx.setAttribute("tmp.vnfInfo.vm.vnfc.vnfc-function-code", "MMC");
        ctx.setAttribute("tmp.vnfInfo.vm.vnfc.vnfc-name", "testVnfc");
        node.processCapabilitiesForVMLevel(vServerId, ctx, findCapability, subCapabilities);
        assertEquals("None", ctx.getAttribute(ConfigResourceNode.CAPABILITIES));
    }

    @Test
    public void shouldProcessCapabilitiesForVMLevelBlankVnfcFunctionCode() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        ConfigResourceNode node = new ConfigResourceNode(DGGeneralDBService.initialise());
        String findCapability = "Restart";
        JsonNodeFactory.instance.objectNode();
        String subCaps =
                "[{\"Restart\":[\"SSC\",\"MMC\"]},{\"Rebuild\":[\"SSC\"]},{\"Migrate\":[\"SSC\"]},"
                + "{\"Snapshot\":[\"SSC\"]},{\"Start\":[\"SSC\"]},{\"Stop\":[\"SSC\"]}]";
        ObjectMapper m = new ObjectMapper();
        JsonNode subCapabilities = m.readTree(subCaps);
        String vServerId = "testServer";
        ctx.setAttribute("tmp.vnfInfo.vm.vnfc.vnfc-function-code", "");
        ctx.setAttribute("tmp.vnfInfo.vm.vnfc.vnfc-name", "testVnfc");
        node.processCapabilitiesForVMLevel(vServerId, ctx, findCapability, subCapabilities);
        assertEquals(ConfigResourceNode.NOT_SUPPORTED, ctx.getAttribute(ConfigResourceNode.CAPABILITIES));
    }

    @Test
    public void shouldCheckIfCapabilityCheckNeeded() {
        ConfigResourceNode node = new ConfigResourceNode(DGGeneralDBService.initialise());
        String findCapability = "Start";
        String capLevel = "vnf";
        boolean result = node.checkIfCapabilityCheckNeeded(capLevel, findCapability);
        assertFalse(result);
    }

    @Test
    public void should_add_attribute_with_success_if_get_config_file_succeed() throws SvcLogicException {
        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder().build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);
        configResourceNode.getConfigFileReference(inParams, contextMock);

        verify(contextMock).setAttribute(anyString(), eq(AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS));
    }

    @Test
    public void should_add_attribute_with_success_if_get_config_info_succeed() throws SvcLogicException {
        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder().build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);
        configResourceNode.getCommonConfigInfo(inParams, contextMock);

        verify(contextMock).setAttribute(anyString(), eq(AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS));
    }

    @Test
    public void should_add_attribute_with_success_if_get_template_succeed() throws SvcLogicException {
        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder().build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);
        configResourceNode.getTemplate(inParams, contextMock);
        verify(contextMock).setAttribute(anyString(), eq(AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS));
    }

    @Test
    public void testGetVnfcReference() throws Exception {
        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder().build();
        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);
        contextMock.setAttribute("template-model-id", "testModelId");
        contextMock.setAttribute("vnfc-type", "testVnfcType");
        configResourceNode.getVnfcReference(inParams, contextMock);
        verify(contextMock).setAttribute(anyString(), eq(AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS));
    }

    @Test
    public void testGetVnfcReferenceWithVnfcType() throws Exception {
        SvcLogicContext context = new SvcLogicContext();
        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
                .getVnfcReferenceByVnfcTypeNAction(TEST, SvcLogicResource.QueryStatus.FAILURE).build();
        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX, TEST);
        context.setAttribute("vnfc-type", "testVnfcType");
        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read vnfc-reference");
        configResourceNode.getVnfcReference(inParams, context);
    }

    @Test
    public void testGetVnfcReferenceWithVnfcTypeAndTemplateModelId() throws Exception {
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute(AppcDataServiceConstant.TEMPLATE_MODEL_ID, TEST);
        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
                .getVnfcReferenceByVnfcTypeNAction(TEST, SvcLogicResource.QueryStatus.SUCCESS)
                .getVnfcReferenceByVnfTypeNActionWithTemplateModelId(TEST, TEST,
                SvcLogicResource.QueryStatus.FAILURE).build();
        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX, TEST);
        context.setAttribute("vnfc-type", "testVnfcType");
        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read vnfc-reference with "
                + AppcDataServiceConstant.TEMPLATE_MODEL_ID);
        configResourceNode.getVnfcReference(inParams, context);
    }

    @Test
    public void testGetVnfcReferenceWithVnfcTypeAndTemplateModelIdNotFound() throws Exception {
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute(AppcDataServiceConstant.TEMPLATE_MODEL_ID, TEST);
        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
                .getVnfcReferenceByVnfcTypeNAction(TEST, SvcLogicResource.QueryStatus.SUCCESS)
                .getVnfcReferenceByVnfTypeNActionWithTemplateModelId(TEST, TEST,
                SvcLogicResource.QueryStatus.NOT_FOUND).getVnfcReferenceByVnfTypeNAction(TEST,
                SvcLogicResource.QueryStatus.FAILURE).build();
        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX, TEST);
        context.setAttribute("vnfc-type", "testVnfcType");
        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read vnfc reference");
        configResourceNode.getVnfcReference(inParams, context);
    }

    @Test
    public void testGetCapability() throws Exception {
        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder().build();
        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);
        contextMock.setAttribute("responsePrefix", "testResponsePrefix");
        contextMock.setAttribute("caplevel", "testCapLevel");
        contextMock.setAttribute("checkCapability", "testCheckCapability");
        contextMock.setAttribute("vServerId", "testVServerId");
        contextMock.setAttribute("vnf-type", "testVnfType");
        configResourceNode.getCapability(inParams, contextMock);
        verify(contextMock).setAttribute(anyString(), eq(AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS));
    }

    @Test
    public void testGetCapabilityNoCheckRequired() throws Exception {
        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder().build();
        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);
        SvcLogicContext context = new SvcLogicContext();
        inParams.put("caplevel", "start");
        inParams.put("checkCapability", "start");
        configResourceNode.getCapability(inParams, context);
        assertEquals(AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS,
                context.getAttribute(AppcDataServiceConstant.OUTPUT_PARAM_STATUS));
    }

    @Test
    public void testGetCapabilityWithVnfType() throws Exception {
        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder().getCapability(TEST,
                "{\"capabilities\": {\"vm\":\"test\"}}").build();
        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);
        SvcLogicContext context = new SvcLogicContext();
        inParams.put("vnf-type", TEST);
        inParams.put("caplevel", "vm");
        configResourceNode.getCapability(inParams, context);
        assertEquals(AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS,
                context.getAttribute(AppcDataServiceConstant.OUTPUT_PARAM_STATUS));
    }

    @Test
    public void testGetCapabilityWithVnfTypeAndSubCapabilities() throws Exception {
        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder().getCapability(TEST,
                "{\"capabilities\": {\"vm\": {\"start\":\"test\"}}}").build();
        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);
        SvcLogicContext context = new SvcLogicContext();
        inParams.put("vnf-type", TEST);
        inParams.put("caplevel", "vm");
        inParams.put("checkCapability", "start");
        configResourceNode.getCapability(inParams, context);
        assertEquals(AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS,
                context.getAttribute(AppcDataServiceConstant.OUTPUT_PARAM_STATUS));
    }

    @Test
    public void testGetCapabilityNotSupported() throws Exception {
        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder().getCapability(TEST,
                "{\"capabilities\": {\"vm\": {\"stop\":\"test\"}}}").build();
        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);
        SvcLogicContext context = new SvcLogicContext();
        inParams.put("vnf-type", TEST);
        inParams.put("caplevel", "vm");
        inParams.put("checkCapability", "start");
        configResourceNode.getCapability(inParams, context);
        assertEquals(ConfigResourceNode.NOT_SUPPORTED, context.getAttribute(ConfigResourceNode.CAPABILITIES));
    }

    @Test
    public void testGetCapabilityNoFindCapability() throws Exception {
        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder().getCapability(TEST,
                "{\"capabilities\": {\"vm\": {\"stop\":\"test\"}}}").build();
        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);
        SvcLogicContext context = new SvcLogicContext();
        inParams.put("vnf-type", TEST);
        inParams.put("caplevel", "vm");
        configResourceNode.getCapability(inParams, context);
        assertEquals("{\"stop\":\"test\"}", context.getAttribute("capabilities.vm"));
    }

    @Test
    public void testGetCapabilityException() throws Exception {
        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder().getCapability(TEST, "\\\\").build();
        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);
        SvcLogicContext context = new SvcLogicContext();
        inParams.put("vnf-type", TEST);
        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unexpected character");
        configResourceNode.getCapability(inParams, context);
    }

    @Test
    public void testGetConfigFilesByVnfVmNCategory() throws Exception {
        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder().build();
        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);
        contextMock.setAttribute("responsePrefix", "testResponsePrefix");
        contextMock.setAttribute("fileCategory", "testFileCategory");
        contextMock.setAttribute("vnfId", "testVnfId");
        contextMock.setAttribute("vmName", "testVmName");
        configResourceNode.getConfigFilesByVnfVmNCategory(inParams, contextMock);
        verify(contextMock).setAttribute(anyString(), eq(AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS));
    }

    @Test
    public void testGetConfigFilesByVnfVmNCategoryException() throws SvcLogicException {
        SvcLogicContext context = new SvcLogicContext();
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX, TEST);
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_FILE_CATEGORY, SOME_FILE_ID);
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_VNF_ID, TEST);
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_VM_NAME, TEST);
        context.setAttribute("fileCategory", TEST);
        DGGeneralDBService dbServiceMock =
                new MockDbServiceBuilder().getConfigFilesByVnfVmNCategory(TEST, SOME_FILE_ID, TEST,
                TEST, SvcLogicResource.QueryStatus.NOT_FOUND).build();
        ConfigResourceNode configResourceNode = Mockito.spy(new ConfigResourceNode(dbServiceMock));
        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to get test from configfiles");
        configResourceNode.getConfigFilesByVnfVmNCategory(inParams, context);
    }

    @Test
    public void should_add_attribute_with_success_if_save_config_files_succeed() throws SvcLogicException {
        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder().build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);
        configResourceNode.saveConfigFiles(inParams, contextMock);

        verify(contextMock).setAttribute(anyString(), eq(AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS));
    }

    @Test
    public void should_add_attribute_with_success_if_update_upload_config_succeed() throws SvcLogicException {
        when(contextMock.getAttribute(UPLOAD_CONFIG_ID_PARAM)).thenReturn("1234");

        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder().build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);
        configResourceNode.updateUploadConfig(inParams, contextMock);

        verify(contextMock).setAttribute(anyString(), eq(AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS));
    }

    @Test
    public void should_add_attribute_with_success_if_get_download_config_template_by_vnf_type_succeed()
            throws SvcLogicException {
        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder().build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);
        configResourceNode.getDownloadConfigTemplateByVnf(inParams, contextMock);

        verify(contextMock).setAttribute(anyString(), eq(AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS));
    }

    @Test
    public void should_add_attribute_with_success_if_get_ssm_chain_succeed() throws SvcLogicException {
        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder().build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);
        configResourceNode.getSmmChainKeyFiles(inParams, contextMock);

        verify(contextMock).setAttribute(anyString(), eq(AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS));
    }

    @Test
    public void should_add_attribute_with_success_if_save_prepare_relationship_succeed() throws SvcLogicException {
        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder().build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);
        configResourceNode.savePrepareRelationship(inParams, contextMock);

        verify(contextMock).setAttribute(anyString(), eq(AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS));
    }

    @Test
    public void should_throw_exception_on_device_config_missing() throws Exception {
        DGGeneralDBService dbServiceMock =
                new MockDbServiceBuilder().getConfigFileReferenceByFileTypeNVnfType(DEVICE_CONF_PREFIX,
                        DEVICE_CONF_FILE_TYPE, SvcLogicResource.QueryStatus.NOT_FOUND).build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read ConfigFileReference:device-configuration");
        configResourceNode.getConfigFileReference(inParams, contextMock);
    }

    @Test
    public void should_throw_exception_on_device_config_failure() throws Exception {
        DGGeneralDBService dbServiceMock =
                new MockDbServiceBuilder().getConfigFileReferenceByFileTypeNVnfType(DEVICE_CONF_PREFIX,
                        DEVICE_CONF_FILE_TYPE, SvcLogicResource.QueryStatus.FAILURE).build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read ConfigFileReference:device-configuration");
        configResourceNode.getConfigFileReference(inParams, contextMock);
    }

    @Test
    public void should_throw_exception_on_success_param_missing() throws Exception {

        DGGeneralDBService dbServiceMock =
                new MockDbServiceBuilder().getConfigFileReferenceByFileTypeNVnfType(SUCCESS_PREFIX, SUCCESS_FILE_TYPE,
                        SvcLogicResource.QueryStatus.NOT_FOUND).build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read ConfigFileReference:configuration_success");
        configResourceNode.getConfigFileReference(inParams, contextMock);
    }

    @Test
    public void should_throw_exception_on_success_param_failure() throws Exception {
        DGGeneralDBService dbServiceMock =
                new MockDbServiceBuilder().getConfigFileReferenceByFileTypeNVnfType(SUCCESS_PREFIX, SUCCESS_FILE_TYPE,
                        SvcLogicResource.QueryStatus.FAILURE).build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read ConfigFileReference:configuration_success");
        configResourceNode.getConfigFileReference(inParams, contextMock);
    }

    @Test
    public void should_throw_exception_on_failure_param_missing() throws Exception {

        DGGeneralDBService dbServiceMock =
                new MockDbServiceBuilder().getConfigFileReferenceByFileTypeNVnfType(FAILURE_PREFIX, FAILURE_FILE_TYPE,
                        SvcLogicResource.QueryStatus.NOT_FOUND).build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read ConfigFileReference:configuration_error");
        configResourceNode.getConfigFileReference(inParams, contextMock);
    }

    @Test
    public void should_throw_exception_on_failure_param_failure() throws Exception {
        DGGeneralDBService dbServiceMock =
                new MockDbServiceBuilder().getConfigFileReferenceByFileTypeNVnfType(FAILURE_PREFIX, FAILURE_FILE_TYPE,
                        SvcLogicResource.QueryStatus.FAILURE).build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read ConfigFileReference:configuration_error");
        configResourceNode.getConfigFileReference(inParams, contextMock);
    }

    @Test
    public void should_throw_exception_on_log_param_missing() throws Exception {

        DGGeneralDBService dbServiceMock =
                new MockDbServiceBuilder().getConfigFileReferenceByFileTypeNVnfType(LOG_PREFIX, LOG_FILE_TYPE,
                        SvcLogicResource.QueryStatus.NOT_FOUND).build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read ConfigFileReference:configuration_log");
        configResourceNode.getConfigFileReference(inParams, contextMock);
    }

    @Test
    public void should_throw_exception_on_log_param_failure() throws Exception {
        DGGeneralDBService dbServiceMock =
                new MockDbServiceBuilder().getConfigFileReferenceByFileTypeNVnfType(LOG_PREFIX, LOG_FILE_TYPE,
                        SvcLogicResource.QueryStatus.FAILURE).build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read ConfigFileReference:configuration_log");
        configResourceNode.getConfigFileReference(inParams, contextMock);
    }

    @Test
    public void should_throw_exception_on_device_protocol_missing() throws SvcLogicException {
        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
                .getDeviceProtocolByVnfType(DEVICE_PROTOCOL_PREFIX, SvcLogicResource.QueryStatus.NOT_FOUND).build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read device_interface_protocol");
        configResourceNode.getCommonConfigInfo(inParams, contextMock);
    }

    @Test
    public void should_throw_exception_on_device_protocol_failure() throws SvcLogicException {
        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
                .getDeviceProtocolByVnfType(DEVICE_PROTOCOL_PREFIX, SvcLogicResource.QueryStatus.FAILURE).build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read device_interface_protocol");
        configResourceNode.getCommonConfigInfo(inParams, contextMock);
    }

    @Test
    public void should_throw_exception_on_conf_action_by_vnf_action_failure() throws SvcLogicException {
        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
                .getConfigureActionDGByVnfTypeNAction(CONF_ACTION_PREFIX, SvcLogicResource.QueryStatus.FAILURE).build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read configure_action_dg");
        configResourceNode.getCommonConfigInfo(inParams, contextMock);
    }

    @Test
    public void should_throw_exception_on_conf_action_missing() throws SvcLogicException {
        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
                .getConfigureActionDGByVnfTypeNAction(CONF_ACTION_PREFIX, SvcLogicResource.QueryStatus.NOT_FOUND)
                .getConfigureActionDGByVnfType(CONF_ACTION_PREFIX, SvcLogicResource.QueryStatus.NOT_FOUND).build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read configure_action_dg");
        configResourceNode.getCommonConfigInfo(inParams, contextMock);
    }

    @Test
    public void should_throw_exception_on_conf_action_failure() throws SvcLogicException {
        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
                .getConfigureActionDGByVnfTypeNAction(CONF_ACTION_PREFIX, SvcLogicResource.QueryStatus.NOT_FOUND)
                .getConfigureActionDGByVnfType(CONF_ACTION_PREFIX, SvcLogicResource.QueryStatus.FAILURE).build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read configure_action_dg");
        configResourceNode.getCommonConfigInfo(inParams, contextMock);
    }

    @Test
    public void should_throw_exception_on_db_template_failure() throws SvcLogicException {
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX, SOME_PREFIX);
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_FILE_CATEGORY, SOME_FILE_CATEGORY);

        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
                .getTemplate(SOME_PREFIX, SOME_FILE_CATEGORY, SvcLogicResource.QueryStatus.FAILURE).build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read some file category");
        configResourceNode.getTemplate(inParams, contextMock);
    }

    @Test
    public void should_throw_exception_on_db_template_by_action_failure() throws SvcLogicException {
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX, SOME_PREFIX);
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_FILE_CATEGORY, SOME_FILE_CATEGORY);

        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
                .getTemplate(SOME_PREFIX, SOME_FILE_CATEGORY, SvcLogicResource.QueryStatus.NOT_FOUND)
                .getTemplateByVnfTypeNAction(SOME_PREFIX, SOME_FILE_CATEGORY, SvcLogicResource.QueryStatus.FAILURE)
                .build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read some file category");
        configResourceNode.getTemplate(inParams, contextMock);
    }

    @Test
    public void should_throw_exception_on_db_template_by_action_missing() throws SvcLogicException {
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX, SOME_PREFIX);
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_FILE_CATEGORY, SOME_FILE_CATEGORY);

        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
                .getTemplate(SOME_PREFIX, SOME_FILE_CATEGORY, SvcLogicResource.QueryStatus.NOT_FOUND)
                .getTemplateByVnfTypeNAction(SOME_PREFIX, SOME_FILE_CATEGORY,
                        SvcLogicResource.QueryStatus.NOT_FOUND)
                .build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read some file category");
        configResourceNode.getTemplate(inParams, contextMock);
    }

    @Test
    public void should_throw_exception_on_db_template_by_name_missing() throws SvcLogicException {
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX, SOME_PREFIX);
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_FILE_CATEGORY, SOME_FILE_CATEGORY);

        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute(AppcDataServiceConstant.TEMPLATE_NAME, "test template");

        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
                .getTemplateByTemplateName(SOME_PREFIX, "test template", SvcLogicResource.QueryStatus.NOT_FOUND)
                .build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read some file category template");
        configResourceNode.getTemplate(inParams, context);
    }

    @Test
    public void should_throw_exception_on_db_template_by_name_failure() throws SvcLogicException {
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX, SOME_PREFIX);
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_FILE_CATEGORY, SOME_FILE_CATEGORY);

        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute(AppcDataServiceConstant.TEMPLATE_NAME, "test template");

        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
                .getTemplateByTemplateName(SOME_PREFIX, "test template", SvcLogicResource.QueryStatus.FAILURE)
                .build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read some file category template");
        configResourceNode.getTemplate(inParams, context);
    }

    @Test
    public void should_throw_exception_on_save_config_failure() throws SvcLogicException {
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute(FILE_CATEGORY_PARAM, SOME_FILE_CATEGORY);

        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
                .saveConfigFiles(CONFIG_FILES_PREFIX, SvcLogicResource.QueryStatus.FAILURE).build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Save some file category in configfiles");
        configResourceNode.saveConfigFiles(inParams, context);
    }

    @Test
    public void should_throw_exception_on_get_max_config_id_missing() throws SvcLogicException {
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute(FILE_CATEGORY_PARAM, SOME_FILE_CATEGORY);

        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
                .getMaxConfigFileId(MAX_CONF_FILE_PREFIX, SOME_FILE_CATEGORY, SvcLogicResource.QueryStatus.NOT_FOUND)
                .build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to get some file category from configfiles");

        configResourceNode.saveConfigFiles(inParams, context);
    }

    @Test
    public void should_throw_exception_on_save_config_files_failure() throws SvcLogicException {
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute(CONFIG_FILE_ID_PARAM, SOME_FILE_ID);

        DGGeneralDBService dbServiceMock =
                new MockDbServiceBuilder().savePrepareRelationship(PREPARE_RELATIONSHIP_PARAM, SOME_FILE_ID, SDC_IND,
                        SvcLogicResource.QueryStatus.FAILURE).build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to save prepare_relationship");
        configResourceNode.saveConfigFiles(inParams, context);
    }

    @Test
    public void should_throw_exception_on_save_upload_config_failure() throws SvcLogicException {
        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
                .saveUploadConfig(UPLOAD_CONFIG_PREFIX, SvcLogicResource.QueryStatus.FAILURE).build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Save configuration in upload_config");
        configResourceNode.updateUploadConfig(inParams, contextMock);
    }

    @Test
    public void should_throw_exception_on_get_upload_config_failure() throws SvcLogicException {
        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
                .getUploadConfigInfo(UPLOAD_CONFIG_INFO_PREFIX, SvcLogicResource.QueryStatus.FAILURE).build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to get record from upload_config");
        configResourceNode.updateUploadConfig(inParams, contextMock);
    }

    @Test
    public void should_throw_exception_on_get_upload_config_missing() throws SvcLogicException {
        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
                .getUploadConfigInfo(UPLOAD_CONFIG_INFO_PREFIX, SvcLogicResource.QueryStatus.NOT_FOUND).build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to get record from upload_config");
        configResourceNode.updateUploadConfig(inParams, contextMock);
    }

    @Test
    public void should_throw_exception_on_update_upload_config_failure() throws SvcLogicException {
        when(contextMock.getAttribute(UPLOAD_CONFIG_ID_PARAM)).thenReturn("1234");

        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
                .updateUploadConfig(UPLOAD_CONFIG_PREFIX, 1234, SvcLogicResource.QueryStatus.FAILURE).build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to upload upload_config");
        configResourceNode.updateUploadConfig(inParams, contextMock);
    }

    @Test
    public void should_throw_exception_on_get_download_config_failure() throws SvcLogicException {
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX, SOME_PREFIX);

        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
                .getDownloadConfigTemplateByVnf(SOME_PREFIX, SvcLogicResource.QueryStatus.FAILURE).build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to get download config template.");
        configResourceNode.getDownloadConfigTemplateByVnf(inParams, contextMock);
    }

    @Test
    public void should_throw_exception_on_get_ssm_chain_failure() throws SvcLogicException {
        when(contextMock.getAttribute(SITE_LOCATION_PARAM)).thenReturn("some location");

        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
                .getTemplateByArtifactType("smm", "smm", "some location", SvcLogicResource.QueryStatus.FAILURE).build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read smm file");
        configResourceNode.getSmmChainKeyFiles(inParams, contextMock);
    }

    @Test
    public void should_throw_exception_on_get_ca_chain_failure() throws SvcLogicException {
        when(contextMock.getAttribute(SITE_LOCATION_PARAM)).thenReturn("some location");

        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder().getTemplateByArtifactType("intermediate-ca-chain",
                "intermediate_ca_chain", "some location", SvcLogicResource.QueryStatus.FAILURE).build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read intermediate_ca_chain file");
        configResourceNode.getSmmChainKeyFiles(inParams, contextMock);
    }

    @Test
    public void should_throw_exception_on_get_server_certificate_and_key_failure() throws SvcLogicException {
        when(contextMock.getAttribute(SITE_LOCATION_PARAM)).thenReturn("some location");

        DGGeneralDBService dbServiceMock =
                new MockDbServiceBuilder().getTemplateByArtifactType("server-certificate-and-key",
                        "server_certificate_and_key", "some location", SvcLogicResource.QueryStatus.FAILURE).build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read server_certificate_and_key file");
        configResourceNode.getSmmChainKeyFiles(inParams, contextMock);
    }

    @Test
    public void should_throw_exception_on_save_prepare_relationship_failure() throws SvcLogicException {
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_SDC_ARTIFACT_IND, "some sdnc index");
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_FILE_ID, SOME_FILE_ID);

        DGGeneralDBService dbServiceMock =
                new MockDbServiceBuilder().savePrepareRelationship(PREPARE_RELATIONSHIP_PARAM, SOME_FILE_ID,
                        "some sdnc index", QueryStatus.FAILURE).build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage(UNABLE_TO_SAVE_RELATIONSHIP_STR);
        configResourceNode.savePrepareRelationship(inParams, contextMock);
    }

    @Test
    public void should_save_save_config_files_for_empty_config_params() throws SvcLogicException {

        when(contextMock.getAttribute(TMP_CONVERTCONFIG_ESC_DATA)).thenReturn("some esc data");
        when(contextMock.getAttribute("configuration")).thenReturn("some configuration");
        when(contextMock.getAttribute(CONFIG_PARAMS)).thenReturn("");

        DGGeneralDBService dbServiceMock =
                new MockDbServiceBuilder().savePrepareRelationship(PREPARE_RELATIONSHIP_PARAM, SOME_FILE_ID,
                        "some sdnc index", QueryStatus.FAILURE).build();

        ConfigResourceNode configResourceNode = spy(new ConfigResourceNode(dbServiceMock));
        configResourceNode.saveConfigBlock(inParams, contextMock);

        verify(configResourceNode).saveDeviceConfiguration(inParams, contextMock, "Request", "some esc data",
                "some configuration");
        verify(contextMock, atLeastOnce()).setAttribute(anyString(), eq(AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS));
    }

    @Test
    public void should_save_save_config_files_for_non_empty_config_params() throws SvcLogicException {

        when(contextMock.getAttribute(TMP_CONVERTCONFIG_ESC_DATA)).thenReturn("some esc data");
        when(contextMock.getAttribute(TMP_MERGE_MERGED_DATA)).thenReturn("merged data");
        when(contextMock.getAttribute(CONFIG_PARAMS)).thenReturn("non empty");

        DGGeneralDBService dbServiceMock =
                new MockDbServiceBuilder().savePrepareRelationship(PREPARE_RELATIONSHIP_PARAM, SOME_FILE_ID,
                        "some sdnc index", QueryStatus.FAILURE).build();

        ConfigResourceNode configResourceNode = spy(new ConfigResourceNode(dbServiceMock));
        configResourceNode.saveConfigBlock(inParams, contextMock);

        verify(configResourceNode).saveDeviceConfiguration(inParams, contextMock, "Configurator", "some esc data",
                "merged data");
        verify(configResourceNode).saveConfigBlock(inParams, contextMock);
        verify(contextMock, atLeastOnce()).setAttribute(anyString(), eq(AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS));
    }

    @Test
    public void testSaveTemplateConfig() throws SvcLogicException
    {
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute(CONFIG_FILE_ID_PARAM, SOME_FILE_ID);

        DGGeneralDBService dbServiceMock =
                new MockDbServiceBuilder().savePrepareRelationship(PREPARE_RELATIONSHIP_PARAM, SOME_FILE_ID, SDC_IND,
                        SvcLogicResource.QueryStatus.FAILURE).build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);
        configResourceNode.saveTemplateConfig(inParams, contextMock);

        verify(contextMock, atLeastOnce()).setAttribute(anyString(), eq(AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS));
    }

    @Test
    public void testSaveTemplateConfigWithConfigParams() throws SvcLogicException
    {
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute(CONFIG_FILE_ID_PARAM, SOME_FILE_ID);
        context.setAttribute(CONFIG_PARAMS, TEST);
        DGGeneralDBService dbServiceMock =
                new MockDbServiceBuilder().savePrepareRelationship(PREPARE_RELATIONSHIP_PARAM, SOME_FILE_ID, SDC_IND,
                        SvcLogicResource.QueryStatus.FAILURE).build();
        ConfigResourceNode configResourceNode = Mockito.spy(new ConfigResourceNode(dbServiceMock));
        Mockito.doNothing().when(configResourceNode).saveConfigurationData(Mockito.anyMap(), Mockito.any(SvcLogicContext.class));
        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage(ConfigResourceNode.UNABLE_TO_SAVE_RELATIONSHIP_STR);
        configResourceNode.saveTemplateConfig(inParams, context);
    }

    @Test
    public void testSaveTemplateConfigWithConfigParamsAndQueryStatusFailure() throws SvcLogicException
    {
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute(CONFIG_FILE_ID_PARAM, SOME_FILE_ID);
        context.setAttribute(CONFIG_PARAMS, TEST);
        DGGeneralDBService dbServiceMock =
                new MockDbServiceBuilder().savePrepareRelationship(PREPARE_RELATIONSHIP_PARAM, null, "Y",
                        SvcLogicResource.QueryStatus.FAILURE).build();
        ConfigResourceNode configResourceNode = Mockito.spy(new ConfigResourceNode(dbServiceMock));
        Mockito.doNothing().when(configResourceNode).saveConfigurationData(Mockito.anyMap(), Mockito.any(SvcLogicContext.class));
        Mockito.doNothing().when(configResourceNode).saveDeviceConfiguration(Mockito.anyMap(), Mockito.any(SvcLogicContext.class),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage(ConfigResourceNode.UNABLE_TO_SAVE_RELATIONSHIP_STR);
        configResourceNode.saveTemplateConfig(inParams, context);
    }

    @Test
    public void testSaveStyleSheetConfig() throws SvcLogicException
    {
        SvcLogicContext context = new SvcLogicContext();
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX, TEST);
        context.setAttribute("tmp.merge.mergedData", TEST);
        context.setAttribute("tmp.convertconfig.escapeData", TEST);
        context.setAttribute("tmp.merge.mergedData", TEST);
        DGGeneralDBService dbServiceMock =
                new MockDbServiceBuilder().savePrepareRelationship(PREPARE_RELATIONSHIP_PARAM, SOME_FILE_ID, SDC_IND,
                        SvcLogicResource.QueryStatus.FAILURE).build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);
        configResourceNode.saveStyleSheetConfig(inParams, contextMock);

        verify(contextMock, atLeastOnce()).setAttribute(anyString(), eq(AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS));
    }

    @Test
    public void testSaveStyleSheetConfigException() throws SvcLogicException
    {
        SvcLogicContext context = new SvcLogicContext();
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX, TEST);
        context.setAttribute("tmp.merge.mergedData", TEST);
        context.setAttribute("tmp.convertconfig.escapeData", TEST);
        context.setAttribute("tmp.merge.mergedData", TEST);
        DGGeneralDBService dbServiceMock =
                new MockDbServiceBuilder().savePrepareRelationship(PREPARE_RELATIONSHIP_PARAM, SOME_FILE_ID, SDC_IND,
                        SvcLogicResource.QueryStatus.FAILURE).build();

        ConfigResourceNode configResourceNode = Mockito.spy(new ConfigResourceNode(dbServiceMock));
        Mockito.doThrow(new SvcLogicException(TEST_MESSAGE)).when(configResourceNode).saveDeviceConfiguration(Mockito.anyMap(), Mockito.any(SvcLogicContext.class),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage(TEST_MESSAGE);
        configResourceNode.saveStyleSheetConfig(inParams, contextMock);
    }

    @Test
    public void testSaveConfigTransactionLog() throws SvcLogicException
    {
        SvcLogicContext context = new SvcLogicContext();
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX, TEST);
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_MESSAGE_TYPE, TEST);
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_MESSAGE, TEST);

        context.setAttribute("request-id", TEST);
        DGGeneralDBService dbServiceMock =
                new MockDbServiceBuilder().savePrepareRelationship(PREPARE_RELATIONSHIP_PARAM, SOME_FILE_ID, SDC_IND,
                        SvcLogicResource.QueryStatus.FAILURE).build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);
        configResourceNode.saveConfigTransactionLog(inParams, contextMock);
        assertEquals(null, contextMock.getAttribute("log-message"));
    }

    @Test
    public void testSaveConfigTransactionLogException() throws SvcLogicException
    {
        SvcLogicContext context = new SvcLogicContext();
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX, TEST);
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_MESSAGE_TYPE, TEST);
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_MESSAGE, TEST);

        context.setAttribute("request-id", TEST);
        DGGeneralDBService dbServiceMock =
                new MockDbServiceBuilder().saveConfigTransactionLog("test.", SvcLogicResource.QueryStatus.FAILURE)
                    .build();
        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);
        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to insert into config_transaction_log");
        configResourceNode.saveConfigTransactionLog(inParams, contextMock);
    }

    @Test
    public void testGetTemplateByTemplateModelIdFailedQuery() throws SvcLogicException {
        SvcLogicContext context = new SvcLogicContext();
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX, TEST);
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_MESSAGE_TYPE, TEST);
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_MESSAGE, TEST);
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_FILE_CATEGORY, SOME_FILE_ID);

        context.setAttribute(AppcDataServiceConstant.TEMPLATE_NAME, "");
        context.setAttribute(AppcDataServiceConstant.TEMPLATE_MODEL_ID, TEST);
        DGGeneralDBService dbServiceMock =
                new MockDbServiceBuilder().getTemplateByTemplateModelId(TEST, SOME_FILE_ID, TEST,
                        SvcLogicResource.QueryStatus.FAILURE).build();
        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);
        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage(ConfigResourceNode.UNABLE_TO_READ_STR);
        configResourceNode.getTemplate(inParams, context);
    }

    @Test
    public void testGetTemplateByTemplateModelIdSuccessQuery() throws SvcLogicException {
        SvcLogicContext context = new SvcLogicContext();
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX, TEST);
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_MESSAGE_TYPE, TEST);
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_MESSAGE, TEST);
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_FILE_CATEGORY, SOME_FILE_ID);

        context.setAttribute(AppcDataServiceConstant.TEMPLATE_NAME, "");
        context.setAttribute(AppcDataServiceConstant.TEMPLATE_MODEL_ID, TEST);
        DGGeneralDBService dbServiceMock =
                new MockDbServiceBuilder().getTemplateByTemplateModelId(TEST, SOME_FILE_ID, TEST,
                        SvcLogicResource.QueryStatus.SUCCESS).build();
        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);
        configResourceNode.getTemplate(inParams, context);
        assertEquals(AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS, context.getAttribute("test."
                + AppcDataServiceConstant.OUTPUT_PARAM_STATUS));
    }

    @Test
    public void testGetTemplateByTemplateModelIdNotFoundQuery() throws SvcLogicException {
        SvcLogicContext context = new SvcLogicContext();
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX, TEST);
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_MESSAGE_TYPE, TEST);
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_MESSAGE, TEST);
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_FILE_CATEGORY, SOME_FILE_ID);

        context.setAttribute(AppcDataServiceConstant.TEMPLATE_NAME, "");
        context.setAttribute(AppcDataServiceConstant.TEMPLATE_MODEL_ID, TEST);

        DGGeneralDBService dbServiceMock =
                new MockDbServiceBuilder().getTemplateByTemplateModelId(TEST, SOME_FILE_ID, TEST,
                        SvcLogicResource.QueryStatus.NOT_FOUND).getTemplate(TEST, SOME_FILE_ID,
                        SvcLogicResource.QueryStatus.NOT_FOUND).build();
        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);
        configResourceNode.getTemplate(inParams, context);
        assertEquals(AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS, context.getAttribute("test."
                + AppcDataServiceConstant.OUTPUT_PARAM_STATUS));
    }

    @Test
    public void testGetTemplateByTemplateModelIdNotFoundByVnfTypeNAction() throws SvcLogicException {
        SvcLogicContext context = new SvcLogicContext();
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX, TEST);
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_MESSAGE_TYPE, TEST);
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_MESSAGE, TEST);
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_FILE_CATEGORY, SOME_FILE_ID);

        context.setAttribute(AppcDataServiceConstant.TEMPLATE_NAME, "");
        context.setAttribute(AppcDataServiceConstant.TEMPLATE_MODEL_ID, TEST);

        DGGeneralDBService dbServiceMock =
                new MockDbServiceBuilder().getTemplateByTemplateModelId(TEST, SOME_FILE_ID, TEST,
                        SvcLogicResource.QueryStatus.NOT_FOUND).getTemplate(TEST, SOME_FILE_ID,
                        SvcLogicResource.QueryStatus.NOT_FOUND).getTemplateByVnfTypeNActionWithTemplateModelId
                        (TEST, SOME_FILE_ID, TEST, SvcLogicResource.QueryStatus.FAILURE).build();
        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);
        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage(ConfigResourceNode.UNABLE_TO_READ_STR);
        configResourceNode.getTemplate(inParams, context);
    }

    @Test
    public void testSaveConfigBlock() throws SvcLogicException {
        SvcLogicContext context = new SvcLogicContext();
        DGGeneralDBService dbServiceMock =
                new MockDbServiceBuilder().build();
        ConfigResourceNode configResourceNode = Mockito.spy(new ConfigResourceNode(dbServiceMock));
        Mockito.doThrow(new SvcLogicException(TEST_MESSAGE)).when(configResourceNode).saveDeviceConfiguration(Mockito.anyMap(), Mockito.any(SvcLogicContext.class),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage(TEST_MESSAGE);
        configResourceNode.saveConfigBlock(inParams, context);
    }
}
