package org.openecomp.appc.i18n;

import static org.junit.Assert.assertNotNull;

import org.junit.Assert;
import org.junit.Test;

public class MsgTest {

    @Test
    public void testNameAndToString() throws Exception {
        for (Msg msg : Msg.values()) {
            Assert.assertEquals(msg.name(), msg.toString());
        }
    }

    @Test
    public void testCONFIGURATION_STARTED() {
        assertNotNull(Msg.valueOf("CONFIGURATION_STARTED"));
    }

    @Test
    public void testCONFIGURATION_CLEARED() {
        assertNotNull(Msg.valueOf("CONFIGURATION_CLEARED"));
    }

    @Test
    public void testLOADING_CONFIGURATION_OVERRIDES() {
        assertNotNull(Msg.valueOf("LOADING_CONFIGURATION_OVERRIDES"));
    }


    @Test
    public void testLOADING_DEFAULTS() {
        assertNotNull(Msg.valueOf("LOADING_DEFAULTS"));
    }


    @Test
    public void testNO_DEFAULTS_FOUND() {
        assertNotNull(Msg.valueOf("NO_DEFAULTS_FOUND"));
    }

    @Test
    public void testPROPERTY_VALUE() {
        assertNotNull(Msg.valueOf("PROPERTY_VALUE"));
    }

    @Test
    public void testNO_OVERRIDE_PROPERTY_FILE_LOADED() {
        assertNotNull(Msg.valueOf("NO_OVERRIDE_PROPERTY_FILE_LOADED"));
    }

    @Test
    public void testSEARCHING_CONFIGURATION_OVERRIDES() {
        assertNotNull(Msg.valueOf("SEARCHING_CONFIGURATION_OVERRIDES"));
    }

    @Test
    public void testLOADING_APPLICATION_OVERRIDES() {
        assertNotNull(Msg.valueOf("LOADING_APPLICATION_OVERRIDES"));
    }

    @Test
    public void testNO_APPLICATION_OVERRIDES() {
        assertNotNull(Msg.valueOf("NO_APPLICATION_OVERRIDES"));
    }

    @Test
    public void testMERGING_SYSTEM_PROPERTIES() {
        assertNotNull(Msg.valueOf("MERGING_SYSTEM_PROPERTIES"));
    }

    @Test
    public void testSETTING_SPECIAL_PROPERTY() {
        assertNotNull(Msg.valueOf("SETTING_SPECIAL_PROPERTY"));
    }

    @Test
    public void testLOADING_RESOURCE_BUNDLE() {
        assertNotNull(Msg.valueOf("LOADING_RESOURCE_BUNDLE"));
    }

    @Test
        public void testLOGGING_ALREADY_INITIALIZED() { assertNotNull(Msg.valueOf("LOGGING_ALREADY_INITIALIZED")); }

    @Test
    public void testSEARCHING_LOG_CONFIGURATION() {
        assertNotNull(Msg.valueOf("SEARCHING_LOG_CONFIGURATION"));
    }

    @Test
    public void testLOADING_DEFAULT_LOG_CONFIGURATION() {
        assertNotNull(Msg.valueOf("LOADING_DEFAULT_LOG_CONFIGURATION"));
    }

    @Test
    public void testNO_LOG_CONFIGURATION() {
        assertNotNull(Msg.valueOf("NO_LOG_CONFIGURATION"));
    }

    @Test
    public void testUNSUPPORTED_LOGGING_FRAMEWORK() {
        assertNotNull(Msg.valueOf("UNSUPPORTED_LOGGING_FRAMEWORK"));
    }

    @Test
    public void testLOADING_LOG_CONFIGURATION() {
        assertNotNull(Msg.valueOf("LOADING_LOG_CONFIGURATION"));
    }

    @Test
    public void testUNKNOWN_PROVIDER() {
        assertNotNull(Msg.valueOf("UNKNOWN_PROVIDER"));
    }

    @Test
    public void testSERVER_STATE_CHANGE_TIMEOUT() {
        assertNotNull(Msg.valueOf("SERVER_STATE_CHANGE_TIMEOUT"));
    }

    @Test
    public void testSERVER_DELETED() {
        assertNotNull(Msg.valueOf("SERVER_DELETED"));
    }

    @Test
    public void testUNKNOWN_SERVER_STATE() {
        assertNotNull(Msg.valueOf("UNKNOWN_SERVER_STATE"));
    }

    @Test
    public void testCOMPONENT_INITIALIZING() {
        assertNotNull(Msg.valueOf("COMPONENT_INITIALIZING"));
    }

    @Test
    public void testCOMPONENT_INITIALIZED() {
        assertNotNull(Msg.valueOf("COMPONENT_INITIALIZED"));
    }

    @Test
    public void testCOMPONENT_TERMINATING() {
        assertNotNull(Msg.valueOf("COMPONENT_TERMINATING"));
    }

    @Test
    public void testCOMPONENT_TERMINATED() {
        assertNotNull(Msg.valueOf("COMPONENT_TERMINATED"));
    }

    @Test
    public void testIAAS_ADAPTER_UNSUPPORTED_OPERATION() {
        assertNotNull(Msg.valueOf("IAAS_ADAPTER_UNSUPPORTED_OPERATION"));
    }

    @Test
    public void testIAAS_ADAPTER_RPC_CALLED() {
        assertNotNull(Msg.valueOf("IAAS_ADAPTER_RPC_CALLED"));
    }

    @Test
    public void testNO_SERVICE_FOUND() {
        assertNotNull(Msg.valueOf("NO_SERVICE_FOUND"));
    }

    @Test
    public void testCONTEXT_PARAMETERS_DISPLAY() {
        assertNotNull(Msg.valueOf("CONTEXT_PARAMETERS_DISPLAY"));
    }

    @Test
    public void testRESPONSE_PARAMETERS_DISPLAY() {
        assertNotNull(Msg.valueOf("RESPONSE_PARAMETERS_DISPLAY"));
    }

    @Test
    public void testNULL_OR_INVALID_ARGUMENT() {
        assertNotNull(Msg.valueOf("NULL_OR_INVALID_ARGUMENT"));
    }

    @Test
    public void testPROCESSING_REQUEST() {
        assertNotNull(Msg.valueOf("PROCESSING_REQUEST"));
    }

    @Test
    public void testINVALID_SERVICE_REQUEST() {
        assertNotNull(Msg.valueOf("INVALID_SERVICE_REQUEST"));
    }

    @Test
    public void testREGISTERING_SERVICE() {
        assertNotNull(Msg.valueOf("REGISTERING_SERVICE"));
    }

    @Test
    public void testUNREGISTERING_SERVICE() {
        assertNotNull(Msg.valueOf("UNREGISTERING_SERVICE"));
    }

    @Test
    public void testLOADING_PROVIDER_DEFINITIONS() {
        assertNotNull(Msg.valueOf("LOADING_PROVIDER_DEFINITIONS"));
    }

    @Test
    public void testRESTARTING_SERVER() {
        assertNotNull(Msg.valueOf("RESTARTING_SERVER"));
    }

    @Test
    public void testREBUILDING_SERVER() {
        assertNotNull(Msg.valueOf("REBUILDING_SERVER"));
    }

    @Test
    public void testMIGRATING_SERVER() {
        assertNotNull(Msg.valueOf("MIGRATING_SERVER"));
    }

    @Test
    public void testEVACUATING_SERVER() {
        assertNotNull(Msg.valueOf("EVACUATING_SERVER"));
    }

    @Test
    public void testSNAPSHOTING_SERVER() {
        assertNotNull(Msg.valueOf("SNAPSHOTING_SERVER"));
    }

    @Test
    public void testLOOKING_SERVER_UP() {
        assertNotNull(Msg.valueOf("LOOKING_SERVER_UP"));
    }

    @Test
    public void testINVALID_SELF_LINK_URL() {
        assertNotNull(Msg.valueOf("INVALID_SELF_LINK_URL"));
    }

    @Test
    public void testSERVER_FOUND() {
        assertNotNull(Msg.valueOf("SERVER_FOUND"));
    }

    @Test
    public void testSERVER_NOT_FOUND() {
        assertNotNull(Msg.valueOf("SERVER_NOT_FOUND"));
    }

    @Test
    public void testSERVER_OPERATION_EXCEPTION() {
        assertNotNull(Msg.valueOf("SERVER_OPERATION_EXCEPTION"));
    }

    @Test
    public void testMISSING_REQUIRED_PROPERTIES() {
        assertNotNull(Msg.valueOf("MISSING_REQUIRED_PROPERTIES"));
    }

    @Test
    public void testSERVER_ERROR_STATE() {
        assertNotNull(Msg.valueOf("SERVER_ERROR_STATE"));
    }

    @Test
    public void testIMAGE_NOT_FOUND() {
        assertNotNull(Msg.valueOf("IMAGE_NOT_FOUND"));
    }

    @Test
    public void testSTATE_CHANGE_TIMEOUT() {
        assertNotNull(Msg.valueOf("STATE_CHANGE_TIMEOUT"));
    }

    @Test
    public void testSTATE_CHANGE_EXCEPTION() {
        assertNotNull(Msg.valueOf("STATE_CHANGE_EXCEPTION"));
    }

    @Test
    public void testSTOP_SERVER() {
        assertNotNull(Msg.valueOf("STOP_SERVER"));
    }

    @Test
    public void testSTART_SERVER() {
        assertNotNull(Msg.valueOf("START_SERVER"));
    }

    @Test
    public void testRESUME_SERVER() {
        assertNotNull(Msg.valueOf("RESUME_SERVER"));
    }

    @Test
    public void testUNPAUSE_SERVER() {
        assertNotNull(Msg.valueOf("UNPAUSE_SERVER"));
    }

    @Test
    public void testREBUILD_SERVER() {
        assertNotNull(Msg.valueOf("REBUILD_SERVER"));
    }

    @Test
    public void testCONNECTION_FAILED_RETRY() {
        assertNotNull(Msg.valueOf("CONNECTION_FAILED_RETRY"));
    }

    @Test
    public void testCONNECTION_FAILED() {
        assertNotNull(Msg.valueOf("CONNECTION_FAILED"));
    }

    @Test
    public void testSTOPPING_SERVER() {
        assertNotNull(Msg.valueOf("STOPPING_SERVER"));
    }

    @Test
    public void testSTARTING_SERVER() {
        assertNotNull(Msg.valueOf("STARTING_SERVER"));
    }

    @Test
    public void testREBUILD_SERVER_FAILED() {
        assertNotNull(Msg.valueOf("REBUILD_SERVER_FAILED"));
    }

    @Test
    public void testPARAMETER_IS_MISSING() {
        assertNotNull(Msg.valueOf("PARAMETER_IS_MISSING"));
    }

    @Test
    public void testPARAMETER_NOT_NUMERIC() {
        assertNotNull(Msg.valueOf("PARAMETER_NOT_NUMERIC"));
    }

    @Test
    public void testDG_FAILED_RESPONSE() {
        assertNotNull(Msg.valueOf("DG_FAILED_RESPONSE"));
    }

    @Test
    public void testEXCEPTION_CALLING_DG() {
        assertNotNull(Msg.valueOf("EXCEPTION_CALLING_DG"));
    }

    @Test
    public void testGRAPH_NOT_FOUND() {
        assertNotNull(Msg.valueOf("GRAPH_NOT_FOUND"));
    }

    @Test
    public void testDEBUG_GRAPH_RESPONSE_HEADER() {
        assertNotNull(Msg.valueOf("DEBUG_GRAPH_RESPONSE_HEADER"));
    }

    @Test
    public void testDEBUG_GRAPH_RESPONSE_DETAIL() {
        assertNotNull(Msg.valueOf("DEBUG_GRAPH_RESPONSE_DETAIL"));
    }

    @Test
    public void testINVALID_REQUIRED_PROPERTY() {
        assertNotNull(Msg.valueOf("INVALID_REQUIRED_PROPERTY"));
    }

    @Test
    public void testMIGRATE_SERVER_FAILED() {
        assertNotNull(Msg.valueOf("MIGRATE_SERVER_FAILED"));
    }

    @Test
    public void testEVACUATE_SERVER_FAILED() {
        assertNotNull(Msg.valueOf("EVACUATE_SERVER_FAILED"));
    }

    @Test
    public void testEVACUATE_SERVER_REBUILD_FAILED() {
        assertNotNull(Msg.valueOf("EVACUATE_SERVER_REBUILD_FAILED"));
    }

    @Test
    public void testAPPC_TOO_BUSY() {
        assertNotNull(Msg.valueOf("APPC_TOO_BUSY"));
    }

    @Test
    public void testVF_SERVER_BUSY() {
        assertNotNull(Msg.valueOf("VF_SERVER_BUSY"));
    }

    @Test
    public void testVF_ILLEGAL_COMMAND() {
        assertNotNull(Msg.valueOf("VF_ILLEGAL_COMMAND"));
    }

    @Test
    public void testVF_UNDEFINED_STATE() {
        assertNotNull(Msg.valueOf("VF_UNDEFINED_STATE"));
    }

    @Test
    public void testAPPC_NO_RESOURCE_FOUND() {
        assertNotNull(Msg.valueOf("APPC_NO_RESOURCE_FOUND"));
    }

    @Test
    public void testAPPC_EXPIRED_REQUEST() {
        assertNotNull(Msg.valueOf("APPC_EXPIRED_REQUEST"));
    }

    @Test
    public void testAPPC_WORKFLOW_NOT_FOUND() {
        assertNotNull(Msg.valueOf("APPC_WORKFLOW_NOT_FOUND"));
    }

    @Test
    public void testAPPC_INVALID_INPUT() {
        assertNotNull(Msg.valueOf("APPC_INVALID_INPUT"));
    }

    @Test
    public void testAPPC_AUDIT_MSG() {
        assertNotNull(Msg.valueOf("APPC_AUDIT_MSG"));
    }

    @Test
    public void testAAI_CONNECTION_FAILED() {
        assertNotNull(Msg.valueOf("AAI_CONNECTION_FAILED"));
    }

    @Test
    public void testAAI_UPDATE_FAILED() {
        assertNotNull(Msg.valueOf("AAI_UPDATE_FAILED"));
    }

    @Test
    public void testAAI_GET_DATA_FAILED() {
        assertNotNull(Msg.valueOf("AAI_GET_DATA_FAILED"));
    }

    @Test
    public void testAAI_CONNECTION_FAILED_RETRY() {
        assertNotNull(Msg.valueOf("AAI_CONNECTION_FAILED_RETRY"));
    }

    @Test
    public void testAAI_DELETE_FAILED() {
        assertNotNull(Msg.valueOf("AAI_DELETE_FAILED"));
    }

    @Test
    public void testAAI_QUERY_FAILED() {
        assertNotNull(Msg.valueOf("AAI_QUERY_FAILED"));
    }

    @Test
    public void testVNF_CONFIGURED() {
        assertNotNull(Msg.valueOf("VNF_CONFIGURED"));
    }

    @Test
    public void testVNF_CONFIGURATION_STARTED() {
        assertNotNull(Msg.valueOf("VNF_CONFIGURATION_STARTED"));
    }

    @Test
    public void testVNF_CONFIGURATION_FAILED() {
        assertNotNull(Msg.valueOf("VNF_CONFIGURATION_FAILED"));
    }

    @Test
    public void testVNF_TEST_STARTED() {
        assertNotNull(Msg.valueOf("VNF_TEST_STARTED"));
    }

    @Test
    public void testVNF_TESTED() {
        assertNotNull(Msg.valueOf("VNF_TESTED"));
    }

    @Test
    public void testVNF_TEST_FAILED() {
        assertNotNull(Msg.valueOf("VNF_TEST_FAILED"));
    }

    @Test
    public void testVNF_NOT_FOUND() {
        assertNotNull(Msg.valueOf("VNF_NOT_FOUND"));
    }

    @Test
    public void testVNF_HEALTHCECK_FAILED() {
        assertNotNull(Msg.valueOf("VNF_HEALTHCECK_FAILED"));
    }

    @Test
    public void testVM_HEALTHCECK_FAILED() {
        assertNotNull(Msg.valueOf("VM_HEALTHCECK_FAILED"));
    }

    @Test
    public void testSTOP_SERVER_FAILED() {
        assertNotNull(Msg.valueOf("STOP_SERVER_FAILED"));
    }

    @Test
    public void testTERMINATE_SERVER_FAILED() {
        assertNotNull(Msg.valueOf("TERMINATE_SERVER_FAILED"));
    }

    @Test
    public void testTERMINATING_SERVER() {
        assertNotNull(Msg.valueOf("TERMINATING_SERVER"));
    }

    @Test
    public void testTERMINATE_SERVER() {
        assertNotNull(Msg.valueOf("TERMINATE_SERVER"));
    }

    @Test
    public void testMIGRATE_COMPLETE() {
        assertNotNull(Msg.valueOf("MIGRATE_COMPLETE"));
    }

    @Test
    public void testRESTART_COMPLETE() {
        assertNotNull(Msg.valueOf("RESTART_COMPLETE"));
    }

    @Test
    public void testREBUILD_COMPLETE() {
        assertNotNull(Msg.valueOf("REBUILD_COMPLETE"));
    }

    @Test
    public void testSTACK_FOUND() {
        assertNotNull(Msg.valueOf("STACK_FOUND"));
    }

    @Test
    public void testTERMINATING_STACK() {
        assertNotNull(Msg.valueOf("TERMINATING_STACK"));
    }

    @Test
    public void testTERMINATE_STACK() {
        assertNotNull(Msg.valueOf("TERMINATE_STACK"));
    }

    @Test
    public void testSTACK_NOT_FOUND() {
        assertNotNull(Msg.valueOf("STACK_NOT_FOUND"));
    }

    @Test
    public void testSTACK_OPERATION_EXCEPTION() {
        assertNotNull(Msg.valueOf("STACK_OPERATION_EXCEPTION"));
    }

    @Test
    public void testTERMINATE_STACK_FAILED() {
        assertNotNull(Msg.valueOf("TERMINATE_STACK_FAILED"));
    }

    @Test
    public void testCLOSE_CONTEXT_FAILED() {
        assertNotNull(Msg.valueOf("CLOSE_CONTEXT_FAILED"));
    }

    @Test
    public void testSNAPSHOTING_STACK() {
        assertNotNull(Msg.valueOf("SNAPSHOTING_STACK"));
    }

    @Test
    public void testSTACK_SNAPSHOTED() {
        assertNotNull(Msg.valueOf("STACK_SNAPSHOTED"));
    }

    @Test
    public void testRESTORING_STACK() {
        assertNotNull(Msg.valueOf("RESTORING_STACK"));
    }

    @Test
    public void testSTACK_RESTORED() {
        assertNotNull(Msg.valueOf("STACK_RESTORED"));
    }

    @Test
    public void testCHECKING_SERVER() {
        assertNotNull(Msg.valueOf("CHECKING_SERVER"));
    }

    @Test
    public void testMISSING_PARAMETER_IN_REQUEST() {
        assertNotNull(Msg.valueOf("MISSING_PARAMETER_IN_REQUEST"));
    }

    @Test
    public void testCANNOT_ESTABLISH_CONNECTION() {
        assertNotNull(Msg.valueOf("CANNOT_ESTABLISH_CONNECTION"));
    }

    @Test
    public void testAPPC_METRIC_MSG() {
        assertNotNull(Msg.valueOf("APPC_METRIC_MSG"));
    }

    @Test
    public void testINPUT_PAYLOAD_PARSING_FAILED() {
        assertNotNull(Msg.valueOf("INPUT_PAYLOAD_PARSING_FAILED"));
    }

    @Test
    public void testAPPC_EXCEPTION() {
        assertNotNull(Msg.valueOf("APPC_EXCEPTION"));
    }

    @Test
    public void testSSH_DATA_EXCEPTION() {
        assertNotNull(Msg.valueOf("SSH_DATA_EXCEPTION"));
    }

    @Test
    public void testJSON_PROCESSING_EXCEPTION() {
        assertNotNull(Msg.valueOf("JSON_PROCESSING_EXCEPTION"));
    }

    @Test
    public void testSUCCESS_EVENT_MESSAGE() {
        assertNotNull(Msg.valueOf("SUCCESS_EVENT_MESSAGE"));
    }

    @Test
    public void testDEPENDENCY_MODEL_NOT_FOUND() {
        assertNotNull(Msg.valueOf("DEPENDENCY_MODEL_NOT_FOUND"));
    }

    @Test
    public void testINVALID_DEPENDENCY_MODEL() {
        assertNotNull(Msg.valueOf("INVALID_DEPENDENCY_MODEL"));
    }

    @Test
    public void testFAILURE_RETRIEVE_VNFC_DG() {
        assertNotNull(Msg.valueOf("FAILURE_RETRIEVE_VNFC_DG"));
    }

    @Test
    public void testSERVER_NETWORK_ERROR() {
        assertNotNull(Msg.valueOf("SERVER_NETWORK_ERROR"));
    }

    @Test
    public void testHYPERVISOR_DOWN_ERROR() {
        assertNotNull(Msg.valueOf("HYPERVISOR_DOWN_ERROR"));
    }

    @Test
    public void testHYPERVISOR_STATUS_UKNOWN() {
        assertNotNull(Msg.valueOf("HYPERVISOR_STATUS_UKNOWN"));
    }

    @Test
    public void testHYPERVISOR_NETWORK_ERROR() {
        assertNotNull(Msg.valueOf("HYPERVISOR_NETWORK_ERROR"));
    }

    @Test
    public void testAPPLICATION_RESTART_FAILED() {
        assertNotNull(Msg.valueOf("APPLICATION_RESTART_FAILED"));
    }

    @Test
    public void testAPPLICATION_START_FAILED() {
        assertNotNull(Msg.valueOf("APPLICATION_START_FAILED"));
    }

    @Test
    public void testAPPLICATION_STOP_FAILED() {
        assertNotNull(Msg.valueOf("APPLICATION_STOP_FAILED"));
    }

    @Test
    public void testRESTART_APPLICATION() {
        assertNotNull(Msg.valueOf("RESTART_APPLICATION"));
    }

    @Test
    public void testSTART_APPLICATION() {
        assertNotNull(Msg.valueOf("START_APPLICATION"));
    }

    @Test
    public void testSTOP_APPLICATION() {
        assertNotNull(Msg.valueOf("STOP_APPLICATION"));
    }

    @Test
    public void testLCM_OPERATIONS_DISABLED() {
        assertNotNull(Msg.valueOf("LCM_OPERATIONS_DISABLED"));
    }

    @Test
    public void testOAM_OPERATION_EXCEPTION() {
        assertNotNull(Msg.valueOf("OAM_OPERATION_EXCEPTION"));
    }

    @Test
    public void testOAM_OPERATION_ENTERING_MAINTENANCE_MODE() {
        assertNotNull(Msg.valueOf("OAM_OPERATION_ENTERING_MAINTENANCE_MODE"));
    }

    @Test
    public void testOAM_OPERATION_MAINTENANCE_MODE() {
        assertNotNull(Msg.valueOf("OAM_OPERATION_MAINTENANCE_MODE"));
    }

    @Test
    public void testOAM_OPERATION_STARTING() {
        assertNotNull(Msg.valueOf("OAM_OPERATION_STARTING"));
    }

    @Test
    public void testOAM_OPERATION_STARTED() {
        assertNotNull(Msg.valueOf("OAM_OPERATION_STARTED"));
    }

    @Test
    public void testOAM_OPERATION_STOPPING() {
        assertNotNull(Msg.valueOf("OAM_OPERATION_STOPPING"));
    }

    @Test
    public void testOAM_OPERATION_STOPPED() {
        assertNotNull(Msg.valueOf("OAM_OPERATION_STOPPED"));
    }

    @Test
    public void testINVALID_STATE_TRANSITION() {
        assertNotNull(Msg.valueOf("INVALID_STATE_TRANSITION"));
    }

    @Test
    public void testREQUEST_HANDLER_UNAVAILABLE() {
        assertNotNull(Msg.valueOf("REQUEST_HANDLER_UNAVAILABLE"));
    }

    @Test
    public void testOAM_OPERATION_RESTARTING() {
        assertNotNull(Msg.valueOf("OAM_OPERATION_RESTARTING"));
    }

    @Test
    public void testOAM_OPERATION_RESTARTED() {
        assertNotNull(Msg.valueOf("OAM_OPERATION_RESTARTED"));
    }

    @Test
    public void testOAM_OPERATION_INVALID_INPUT() {
        assertNotNull(Msg.valueOf("OAM_OPERATION_INVALID_INPUT"));
    }
}
