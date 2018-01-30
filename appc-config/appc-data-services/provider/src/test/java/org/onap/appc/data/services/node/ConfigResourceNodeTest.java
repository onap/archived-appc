package org.onap.appc.data.services.node;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.HashMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import org.onap.appc.data.services.AppcDataServiceConstant;
import org.onap.appc.data.services.db.DGGeneralDBService;
import static org.onap.appc.data.services.node.ConfigResourceNode.CONFIG_FILE_ID_PARAM;
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
import static org.onap.appc.data.services.node.ConfigResourceNode.SUCCESS_FILE_TYPE;
import static org.onap.appc.data.services.node.ConfigResourceNode.SUCCESS_PREFIX;
import static org.onap.appc.data.services.node.ConfigResourceNode.CONFIG_FILES_PREFIX;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;

public class ConfigResourceNodeTest {

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
        String subCaps = "[{\"Restart\":[\"SSC\",\"MMC\"]},{\"Rebuild\":[\"SSC\"]},{\"Migrate\":[\"SSC\"]},{\"Snapshot\":[\"SSC\"]},{\"Start\":[\"SSC\"]},{\"Stop\":[\"SSC\"]}]";
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
    public void should_add_attribute_with_success_if_save_config_files_succeed() throws SvcLogicException {
        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder().build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);
        configResourceNode.saveConfigFiles(inParams, contextMock);

        verify(contextMock).setAttribute(anyString(), eq(AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS));
    }

    @Test
    public void should_throw_exception_on_device_config_missing() throws Exception {
        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
            .getConfigFileReferenceByFileTypeNVnfType(DEVICE_CONF_PREFIX, DEVICE_CONF_FILE_TYPE, SvcLogicResource.QueryStatus.NOT_FOUND)
            .build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read ConfigFileReference:device-configuration");
        configResourceNode.getConfigFileReference(inParams, contextMock);
    }

    @Test
    public void should_throw_exception_on_device_config_failure() throws Exception {
        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
            .getConfigFileReferenceByFileTypeNVnfType(DEVICE_CONF_PREFIX, DEVICE_CONF_FILE_TYPE, SvcLogicResource.QueryStatus.FAILURE)
            .build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read ConfigFileReference:device-configuration");
        configResourceNode.getConfigFileReference(inParams, contextMock);
    }

    @Test
    public void should_throw_exception_on_success_param_missing() throws Exception {

        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
            .getConfigFileReferenceByFileTypeNVnfType(SUCCESS_PREFIX, SUCCESS_FILE_TYPE, SvcLogicResource.QueryStatus.NOT_FOUND)
            .build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read ConfigFileReference:configuration_success");
        configResourceNode.getConfigFileReference(inParams, contextMock);
    }

    @Test
    public void should_throw_exception_on_success_param_failure() throws Exception {
        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
            .getConfigFileReferenceByFileTypeNVnfType(SUCCESS_PREFIX, SUCCESS_FILE_TYPE, SvcLogicResource.QueryStatus.FAILURE)
            .build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read ConfigFileReference:configuration_success");
        configResourceNode.getConfigFileReference(inParams, contextMock);
    }

    @Test
    public void should_throw_exception_on_failure_param_missing() throws Exception {

        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
            .getConfigFileReferenceByFileTypeNVnfType(FAILURE_PREFIX, FAILURE_FILE_TYPE, SvcLogicResource.QueryStatus.NOT_FOUND)
            .build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read ConfigFileReference:configuration_error");
        configResourceNode.getConfigFileReference(inParams, contextMock);
    }

    @Test
    public void should_throw_exception_on_failure_param_failure() throws Exception {
        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
            .getConfigFileReferenceByFileTypeNVnfType(FAILURE_PREFIX, FAILURE_FILE_TYPE, SvcLogicResource.QueryStatus.FAILURE)
            .build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read ConfigFileReference:configuration_error");
        configResourceNode.getConfigFileReference(inParams, contextMock);
    }

    @Test
    public void should_throw_exception_on_log_param_missing() throws Exception {

        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
            .getConfigFileReferenceByFileTypeNVnfType(LOG_PREFIX, LOG_FILE_TYPE, SvcLogicResource.QueryStatus.NOT_FOUND)
            .build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read ConfigFileReference:configuration_log");
        configResourceNode.getConfigFileReference(inParams, contextMock);
    }

    @Test
    public void should_throw_exception_on_log_param_failure() throws Exception {
        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
            .getConfigFileReferenceByFileTypeNVnfType(LOG_PREFIX, LOG_FILE_TYPE, SvcLogicResource.QueryStatus.FAILURE)
            .build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read ConfigFileReference:configuration_log");
        configResourceNode.getConfigFileReference(inParams, contextMock);
    }

    @Test
    public void should_throw_exception_on_device_protocol_missing() throws SvcLogicException {
        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
            .getDeviceProtocolByVnfType(DEVICE_PROTOCOL_PREFIX, SvcLogicResource.QueryStatus.NOT_FOUND)
            .build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read device_interface_protocol");
        configResourceNode.getCommonConfigInfo(inParams, contextMock);
    }

    @Test
    public void should_throw_exception_on_device_protocol_failure() throws SvcLogicException {
        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
            .getDeviceProtocolByVnfType(DEVICE_PROTOCOL_PREFIX, SvcLogicResource.QueryStatus.FAILURE)
            .build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read device_interface_protocol");
        configResourceNode.getCommonConfigInfo(inParams, contextMock);
    }

    @Test
    public void should_throw_exception_on_conf_action_by_vnf_action_failure() throws SvcLogicException {
        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
            .getConfigureActionDGByVnfTypeNAction(CONF_ACTION_PREFIX, SvcLogicResource.QueryStatus.FAILURE)
            .build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read configure_action_dg");
        configResourceNode.getCommonConfigInfo(inParams, contextMock);
    }

    @Test
    public void should_throw_exception_on_conf_action_missing() throws SvcLogicException {
        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
            .getConfigureActionDGByVnfTypeNAction(CONF_ACTION_PREFIX, SvcLogicResource.QueryStatus.NOT_FOUND)
            .getConfigureActionDGByVnfType(CONF_ACTION_PREFIX, SvcLogicResource.QueryStatus.NOT_FOUND)
            .build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read configure_action_dg");
        configResourceNode.getCommonConfigInfo(inParams, contextMock);
    }

    @Test
    public void should_throw_exception_on_conf_action_failure() throws SvcLogicException {
        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
            .getConfigureActionDGByVnfTypeNAction(CONF_ACTION_PREFIX, SvcLogicResource.QueryStatus.NOT_FOUND)
            .getConfigureActionDGByVnfType(CONF_ACTION_PREFIX, SvcLogicResource.QueryStatus.FAILURE)
            .build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read configure_action_dg");
        configResourceNode.getCommonConfigInfo(inParams, contextMock);
    }

    @Test
    public void should_throw_exception_on_db_template_failure() throws SvcLogicException {
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX, "some prefix");
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_FILE_CATEGORY, "some file category");

        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
            .getTemplate("some prefix", "some file category", SvcLogicResource.QueryStatus.FAILURE)
            .build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read some file category");
        configResourceNode.getTemplate(inParams, contextMock);
    }

    @Test
    public void should_throw_exception_on_db_template_by_action_failure() throws SvcLogicException {
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX, "some prefix");
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_FILE_CATEGORY, "some file category");

        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
            .getTemplate("some prefix", "some file category", SvcLogicResource.QueryStatus.NOT_FOUND)
            .getTemplateByVnfTypeNAction("some prefix", "some file category", SvcLogicResource.QueryStatus.FAILURE)
            .build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read some file category");
        configResourceNode.getTemplate(inParams, contextMock);
    }

    @Test
    public void should_throw_exception_on_db_template_by_action_missing() throws SvcLogicException {
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX, "some prefix");
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_FILE_CATEGORY, "some file category");

        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
            .getTemplate("some prefix", "some file category", SvcLogicResource.QueryStatus.NOT_FOUND)
            .getTemplateByVnfTypeNAction("some prefix", "some file category", SvcLogicResource.QueryStatus.NOT_FOUND)
            .build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read some file category");
        configResourceNode.getTemplate(inParams, contextMock);
    }
    
    @Test
    public void should_throw_exception_on_db_template_by_name_missing() throws SvcLogicException {
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX, "some prefix");
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_FILE_CATEGORY, "some file category");

        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("template-name", "test template");

        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
            .getTemplateByTemplateName("some prefix", "test template", SvcLogicResource.QueryStatus.NOT_FOUND)
            .build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read some file category template");
        configResourceNode.getTemplate(inParams, context);
    }

    @Test
    public void should_throw_exception_on_db_template_by_name_failure() throws SvcLogicException {
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX, "some prefix");
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_FILE_CATEGORY, "some file category");

        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("template-name", "test template");

        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
            .getTemplateByTemplateName("some prefix", "test template", SvcLogicResource.QueryStatus.FAILURE)
            .build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read some file category template");
        configResourceNode.getTemplate(inParams, context);
    }

    @Test
    public void should_throw_exception_on_save_config_failure() throws SvcLogicException {
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute(FILE_CATEGORY_PARAM, "some file category");

        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
            .saveConfigFiles(CONFIG_FILES_PREFIX, SvcLogicResource.QueryStatus.FAILURE)
            .build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Save some file category in configfiles");
        configResourceNode.saveConfigFiles(inParams, context);
    }

    @Test
    public void should_throw_exception_on_get_max_config_id_missing() throws SvcLogicException {
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute(FILE_CATEGORY_PARAM, "some file category");

        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
            .getMaxConfigFileId(MAX_CONF_FILE_PREFIX, "some file category", SvcLogicResource.QueryStatus.NOT_FOUND)
            .build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to get some file category from configfiles");

        configResourceNode.saveConfigFiles(inParams, context);
    }

    @Test
    public void should_throw_exception_on_save_config_files_failure() throws SvcLogicException {
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute(CONFIG_FILE_ID_PARAM, "some file id");

        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
            .savePrepareRelationship(PREPARE_RELATIONSHIP_PARAM, "some file id", SDC_IND, SvcLogicResource.QueryStatus.FAILURE)
            .build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to save prepare_relationship");
        configResourceNode.saveConfigFiles(inParams, context);
    }

}