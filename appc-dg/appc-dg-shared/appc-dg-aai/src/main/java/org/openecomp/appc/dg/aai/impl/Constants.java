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


public class Constants {
    public final static String VNF_ID_PARAM_NAME = "aai.vnfID";
    public static final String AAI_PREFIX_PARAM_NAME = "aai.prefix";
    public static final String AAI_INPUT_DATA = "aai.input.data";
    public static final String AAI_ERROR_MESSAGE = "org.openecomp.appc.dg.error";

    public static final String CONFIGURE_PATH = "/restconf/config/opendaylight-inventory:nodes/node/";
    public static final String CONNECT_PATH = "/restconf/config/opendaylight-inventory:nodes/node/controller-config/yang-ext:mount/config:modules";
    public static final String CHECK_CONNECTION_PATH = "/restconf/operational/opendaylight-inventory:nodes/node/";
    public static final String DISCONNECT_PATH = "/restconf/config/opendaylight-inventory:nodes/node/controller-config/yang-ext:mount/config:modules/module/odl-sal-netconf-connector-cfg:sal-netconf-connector/";

    public static final String CONTROLLER_IP = "127.0.0.1";
    public static final int CONTROLLER_PORT = 8181;
    public static final String PROTOCOL = "http";

    public static final String VF_LICENSE = "VF_LICENSE";

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

    // constants fo DG
    public static final String VNF_TYPE_FIELD_NAME = "org.openecomp.appc.vftype";
    public static final String VNF_VERSION_FIELD_NAME = "org.openecomp.appc.vfversion";
    public static final String VNF_RESOURCE_VERSION_FIELD_NAME = "org.openecomp.appc.resource-version";
    public static final String TARGET_VNF_TYPE = "target-vnf-type";
    public static final String FILE_CONTENT_FIELD_NAME = "file-content";
    public static final String CONNECTION_DETAILS_FIELD_NAME = "connection-details";
    public static final String CONFIGURATION_FILE_FIELD_NAME = "configuration-file-name";
    public static final String VNF_HOST_IP_ADDRESS_FIELD_NAME = "vnf-host-ip-address";
    public static final String UPGRADE_VERSION = "upgrade-version";
    public static final String DG_ERROR_FIELD_NAME = "org.openecomp.appc.dg.error";
    public static final String ATTRIBUTE_ERROR_MESSAGE = "error-message";
    public static final String RESOURCEKEY = "resourceKey";
    public static final String REQ_ID_FIELD_NAME = "org.openecomp.appc.reqid";
    public static final String API_VERSION_FIELD_NAME = "org.openecomp.appc.apiversion";
    public static final String MODEL_ENTITLMENT_POOL_UUID_NAME = "model.entitlement.pool.uuid";
    public static final String MODEL_LICENSE_KEY_UUID_NAME = "model.license.key.uuid";
    public static final String ENTITLMENT_POOL_UUID_NAME = "entitlement.pool.uuid";
    public static final String LICENSE_KEY_UUID_NAME = "license.key.uuid";
    public static final String IS_ACQUIRE_LICENSE_REQUIRE ="is.acquire-license.require";
    public static final String IS_RELEASE_LICENSE_REQUIRE ="is.release-license.require";

    public static final String AAI_ENTITLMENT_POOL_UUID_NAME = "aai.input.data.entitlement-assignment-group-uuid";
    public static final String AAI_LICENSE_KEY_UUID_NAME = "aai.input.data.license-assignment-group-uuid";
    public static final String AAI_LICENSE_KEY_VALUE = "aai.input.data.license-key";
    public static final String IS_AAI_ENTITLEMENT_UPDATE_REQUIRE = "is.aai-entitlement-update.require";
    public static final String IS_AAI_LICENSE_UPDATE_REQUIRE = "is.aai-license-update.require";
    public static final String IS_ACQUIRE_ENTITLEMENT_REQUIRE ="is.acquire-entitlement.require";
    public static final String IS_RELEASE_ENTITLEMENT_REQUIRE ="is.release-entitlement.require";

    public enum ASDC_ARTIFACTS_FIELDS {
        SERVICE_UUID,
        DISTRIBUTION_ID,
        SERVICE_NAME,
        SERVICE_DESCRIPTION,
        RESOURCE_UUID,
        RESOURCE_INSTANCE_NAME,
        RESOURCE_NAME,
        RESOURCE_VERSION,
        RESOURCE_TYPE,
        ARTIFACT_UUID,
        ARTIFACT_TYPE,
        ARTIFACT_VERSION,
        ARTIFACT_DESCRIPTION,
        INTERNAL_VERSION,
        CREATION_DATE,
        ARTIFACT_NAME,
        ARTIFACT_CONTENT
    }
}
