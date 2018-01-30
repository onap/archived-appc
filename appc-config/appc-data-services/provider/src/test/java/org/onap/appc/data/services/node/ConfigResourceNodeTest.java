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
import static org.mockito.Mockito.mock;
import org.onap.appc.data.services.db.DGGeneralDBService;
import static org.onap.appc.data.services.node.ConfigResourceNode.DEVICE_CONF_FILE_TYPE;
import static org.onap.appc.data.services.node.ConfigResourceNode.DEVICE_CONF_PREFIX;
import static org.onap.appc.data.services.node.ConfigResourceNode.FAILURE_FILE_TYPE;
import static org.onap.appc.data.services.node.ConfigResourceNode.FAILURE_PREFIX;
import static org.onap.appc.data.services.node.ConfigResourceNode.LOG_FILE_TYPE;
import static org.onap.appc.data.services.node.ConfigResourceNode.LOG_PREFIX;
import static org.onap.appc.data.services.node.ConfigResourceNode.SUCCESS_FILE_TYPE;
import static org.onap.appc.data.services.node.ConfigResourceNode.SUCCESS_PREFIX;
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
     *
     * {"capabilities":{"vnfc":[],"vm":[{"ConfigureTest":["SSC","MMSC"]}],"vf-module":[],"vnf":["ConfigModify","HealthCheck"]}}
     *
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
    public void should_not_throw_if_all_db_params_return_success() throws SvcLogicException {
        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder().build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);
        configResourceNode.getConfigFileReference(inParams, contextMock);
    }

    @Test
    public void should_throw_exception_on_device_config_missing() throws Exception {
        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
            .configFileReference(DEVICE_CONF_PREFIX, DEVICE_CONF_FILE_TYPE, SvcLogicResource.QueryStatus.NOT_FOUND)
            .build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read ConfigFileReference:device-configuration");
        configResourceNode.getConfigFileReference(inParams, contextMock);
    }

    @Test
    public void should_throw_exception_on_device_config_failure() throws Exception {
        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
            .configFileReference(DEVICE_CONF_PREFIX, DEVICE_CONF_FILE_TYPE, SvcLogicResource.QueryStatus.FAILURE)
            .build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read ConfigFileReference:device-configuration");
        configResourceNode.getConfigFileReference(inParams, contextMock);
    }

    @Test
    public void should_throw_exception_on_success_param_missing() throws Exception {

        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
            .configFileReference(SUCCESS_PREFIX, SUCCESS_FILE_TYPE, SvcLogicResource.QueryStatus.NOT_FOUND)
            .build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read ConfigFileReference:configuration_success");
        configResourceNode.getConfigFileReference(inParams, contextMock);
    }

    @Test
    public void should_throw_exception_on_success_param_failure() throws Exception {
        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
            .configFileReference(SUCCESS_PREFIX, SUCCESS_FILE_TYPE, SvcLogicResource.QueryStatus.FAILURE)
            .build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read ConfigFileReference:configuration_success");
        configResourceNode.getConfigFileReference(inParams, contextMock);
    }

    @Test
    public void should_throw_exception_on_failure_param_missing() throws Exception {

        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
            .configFileReference(FAILURE_PREFIX, FAILURE_FILE_TYPE, SvcLogicResource.QueryStatus.NOT_FOUND)
            .build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read ConfigFileReference:configuration_error");
        configResourceNode.getConfigFileReference(inParams, contextMock);
    }

    @Test
    public void should_throw_exception_on_failure_param_failure() throws Exception {
        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
            .configFileReference(FAILURE_PREFIX, FAILURE_FILE_TYPE, SvcLogicResource.QueryStatus.FAILURE)
            .build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read ConfigFileReference:configuration_error");
        configResourceNode.getConfigFileReference(inParams, contextMock);
    }

    @Test
    public void should_throw_exception_on_log_param_missing() throws Exception {

        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
            .configFileReference(LOG_PREFIX, LOG_FILE_TYPE, SvcLogicResource.QueryStatus.NOT_FOUND)
            .build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read ConfigFileReference:configuration_log");
        configResourceNode.getConfigFileReference(inParams, contextMock);
    }

    @Test
    public void should_throw_exception_on_log_param_failure() throws Exception {
        DGGeneralDBService dbServiceMock = new MockDbServiceBuilder()
            .configFileReference(LOG_PREFIX, LOG_FILE_TYPE, SvcLogicResource.QueryStatus.FAILURE)
            .build();

        ConfigResourceNode configResourceNode = new ConfigResourceNode(dbServiceMock);

        expectedException.expect(SvcLogicException.class);
        expectedException.expectMessage("Unable to Read ConfigFileReference:configuration_log");
        configResourceNode.getConfigFileReference(inParams, contextMock);
    }

}