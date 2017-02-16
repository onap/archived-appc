/*-
 * ============LICENSE_START=======================================================
 * openECOMP : APP-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 						reserved.
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
 */

package org.openecomp.appc.adapter.ssh;

public class Constants {

    private Constants(){}

//    public static final String CONFIGURE_PATH = "/restconf/config/opendaylight-inventory:nodes/node/";
//    public static final String CONNECT_PATH = "/restconf/config/opendaylight-inventory:nodes/node/controller-config/yang-ext:mount/config:modules";
//    public static final String CHECK_CONNECTION_PATH = "/restconf/operational/opendaylight-inventory:nodes/node/";
//    public static final String DISCONNECT_PATH = "/restconf/config/opendaylight-inventory:nodes/node/controller-config/yang-ext:mount/config:modules/module/odl-sal-netconf-connector-cfg:sal-netconf-connector/";
//
//    public static final String CONTROLLER_IP = "127.0.0.1";
//    public static final int CONTROLLER_PORT = 8181;
//    public static final String PROTOCOL = "http";
//
//    public static final String VF_LICENSE = "VF_LICENSE";

    // tables and fields
    public static final String NETCONF_SCHEMA = "sdnctl";
    public static final String SDNCTL_SCHEMA = "sdnctl";
    public static final String DEVICE_AUTHENTICATION_TABLE_NAME = "DEVICE_AUTHENTICATION";
    public static final String CONFIGFILES_TABLE_NAME = "CONFIGFILES";
    public static final String DEVICE_INTERFACE_LOG_TABLE_NAME = "DEVICE_INTERFACE_LOG";
    public static final String FILE_CONTENT_TABLE_FIELD_NAME = "FILE_CONTENT";
    public static final String FILE_NAME_TABLE_FIELD_NAME = "FILE_NAME";
    public static final String USER_NAME_TABLE_FIELD_NAME = "USER_NAME";
    public static final String PASSWORD_TABLE_FIELD_NAME = "PASSWORD";
    public static final String PORT_NUMBER_TABLE_FIELD_NAME = "PORT_NUMBER";
    public static final String VNF_TYPE_TABLE_FIELD_NAME = "VNF_TYPE";
    public static final String SERVICE_INSTANCE_ID_FIELD_NAME = "SERVICE_INSTANCE_ID";
    public static final String REQUEST_ID_FIELD_NAME = "REQUEST_ID";
    public static final String CREATION_DATE_FIELD_NAME = "CREATION_DATE";
    public static final String LOG_FIELD_NAME = "LOG";
    public static final String ASDC_ARTIFACTS_TABLE_NAME = "ASDC_ARTIFACTS";

    // input fields names
    public static final String PAYLOAD = "payload";


    public static final String PARAM_IN_connection_details = "connection-details";
    public static final String SKIP_EXECUTION_INSTALLER_BIN_FILE = "Skip-execution-installer-bin-file";
    public static final String SKIP_DEPLOY = "Skip-deploy";
    public static final String UPGRADE_VERSION = "upgrade-version";

    //command to get number of UP hosts
    public static final String STATE_COMMAND = "/opt/jnetx/skyfall-scp/asp-state.sh | grep -o UP | wc -l";
    public static final int STATE_COMMAND_RESULT = 18;
    //commands to check FE hosts
    public static final String FE_STATE_TRUE_TEST_COMMAND = "ssh -t -q fe1 /opt/omni/bin/swmml -e display-platform-status | grep -o TRUE | wc -l";
    public static final int FE_STATE_TRUE_TEST_RESULT = 22;
    public static final String FE_STATE_FALSE_TEST_COMMAND = "ssh -t -q fe1 /opt/omni/bin/swmml -e display-platform-status | grep -o FALSE | wc -l";
    public static final int FE_STATE_FALSE_TEST_RESULT = 2;
    public static final String FE_OPERATIONAL_TEST_COMMAND = "ssh -t -q fe1 /opt/omni/bin/swmml -e display-platform-status | grep -o 'NOT FULLY OPERATIONAL' | wc -l";
    public static final int FE_OPERATIONAL_TEST_RESULT = 2;
    //rsync command
    public static final String RSYNC_COMMAND = "yes n | /opt/jnetx/skyfall-scp/asp-rsync.sh --check | grep -o 'is active' | wc -l";
    public static final int RSYNC_COMMAND_RESULT = 9;

    public static final String PARAM_IN_TIMEOUT = "timeout";
    public static final String PARAM_IN_FILE_URL = "source-file-url";
    public static final String DOWNLOAD_COMMAND = "wget -N %s";

    // pre-define jnetx VM names
    public static final String[] VM_NAMES = {"fe1", "fe2", "be1", "be2", "be3", "be4", "be5", "smp1", "smp2"};

    public static final String DEFAULT_DISK_SPACE = "10240000";
    public static final String DF_COMMAND_TEMPLATE = "ssh %s df | grep vda1 | grep -v grep | tr -s ' '|cut -d ' ' -f4";

    public static final String DG_OUTPUT_STATUS_MESSAGE = "output.status.message";


    // constants fo DG
//    public static final String VNF_TYPE_FIELD_NAME = "org.openecomp.appc.vftype";
//    public static final String VNF_VERSION_FIELD_NAME = "org.openecomp.appc.vfversion";
//    public static final String VNF_RESOURCE_VERSION_FIELD_NAME = "org.openecomp.appc.resource-version";
//    public static final String TARGET_VNF_TYPE = "target-vnf-type";
//    public static final String FILE_CONTENT_FIELD_NAME = "file-content";
    public static final String CONNECTION_DETAILS_FIELD_NAME = "connection-details";
//    public static final String CONFIGURATION_FILE_FIELD_NAME = "configuration-file-name";
    public static final String VNF_HOST_IP_ADDRESS_FIELD_NAME = "vnf-host-ip-address";
//    public static final String UPGRADE_VERSION = "upgrade-version";
    public static final String DG_ERROR_FIELD_NAME = "org.openecomp.appc.dg.error";
//    public static final String RESOURCEKEY = "resourceKey";
//    public static final String REQ_ID_FIELD_NAME = "org.openecomp.appc.reqid";
//    public static final String API_VERSION_FIELD_NAME = "org.openecomp.appc.apiversion";
//    public static final String MODEL_ENTITLMENT_POOL_UUID_NAME = "model.entitlement.pool.uuid";
//    public static final String MODEL_LICENSE_KEY_UUID_NAME = "model.license.key.uuid";
//    public static final String ENTITLMENT_POOL_UUID_NAME = "entitlement.pool.uuid";
//    public static final String LICENSE_KEY_UUID_NAME = "license.key.uuid";
//    public static final String IS_ACQUIRE_LICENSE_REQUIRE ="is.acquire-license.require";
//    public static final String IS_RELEASE_LICENSE_REQUIRE ="is.release-license.require";
//
//    public static final String AAI_ENTITLMENT_POOL_UUID_NAME = "aai.input.data.entitlement-assignment-group-uuid";
//    public static final String AAI_LICENSE_KEY_UUID_NAME = "aai.input.data.license-assignment-group-uuid";
//    public static final String AAI_LICENSE_KEY_VALUE = "aai.input.data.license-key";
//    public static final String IS_AAI_ENTITLEMENT_UPDATE_REQUIRE = "is.aai-entitlement-update.require";
//    public static final String IS_AAI_LICENSE_UPDATE_REQUIRE = "is.aai-license-update.require";
//    public static final String IS_ACQUIRE_ENTITLEMENT_REQUIRE ="is.acquire-entitlement.require";
//    public static final String IS_RELEASE_ENTITLEMENT_REQUIRE ="is.release-entitlement.require";
//
//    public enum ASDC_ARTIFACTS_FIELDS {
//        SERVICE_UUID,
//        DISTRIBUTION_ID,
//        SERVICE_NAME,
//        SERVICE_DESCRIPTION,
//        RESOURCE_UUID,
//        RESOURCE_INSTANCE_NAME,
//        RESOURCE_NAME,
//        RESOURCE_VERSION,
//        RESOURCE_TYPE,
//        ARTIFACT_UUID,
//        ARTIFACT_TYPE,
//        ARTIFACT_VERSION,
//        ARTIFACT_DESCRIPTION,
//        INTERNAL_VERSION,
//        CREATION_DATE,
//        ARTIFACT_NAME,
//        ARTIFACT_CONTENT
//    }
}
