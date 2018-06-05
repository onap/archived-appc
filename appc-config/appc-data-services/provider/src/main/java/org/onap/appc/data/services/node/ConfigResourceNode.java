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
package org.onap.appc.data.services.node;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.onap.appc.data.services.AppcDataServiceConstant;
import org.onap.appc.data.services.db.DGGeneralDBService;
import org.onap.appc.data.services.utils.EscapeUtils;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource.QueryStatus;

public class ConfigResourceNode implements SvcLogicJavaPlugin {

    static final String DEVICE_CONF_PREFIX = "configfilereference-deviceconfig";
    static final String DEVICE_CONF_FILE_TYPE = "device_configuration";

    static final String SUCCESS_PREFIX = "configfilereference-success";
    static final String SUCCESS_FILE_TYPE = "configuration_success";

    static final String FAILURE_PREFIX = "configfilereference-failure";
    static final String FAILURE_FILE_TYPE = "configuration_error";

    static final String LOG_PREFIX = "configfilereference-log";
    static final String LOG_FILE_TYPE = "log";

    static final String DEVICE_PROTOCOL_PREFIX = "tmp.deviceinterfaceprotocol";
    static final String CONF_ACTION_PREFIX = "tmp.configureactiondg";

    static final String CONFIG_FILES_PREFIX = "tmp.configFiles";
    static final String MAX_CONF_FILE_PREFIX = "tmp.configfilesmax";
    static final String UPLOAD_CONFIG_PREFIX = "tmp.uploadConfig";
    static final String UPLOAD_CONFIG_INFO_PREFIX = "tmp.uploadConfigInfo";

    static final String PREPARE_RELATIONSHIP_PARAM = "tmp.preparerel";
    static final String CONFIG_FILE_ID_PARAM = "tmp.configfilesmax.configfileid";
    static final String FILE_CATEGORY_PARAM = "file-category";
    static final String UPLOAD_CONFIG_ID_PARAM = "tmp.uploadConfigInfo.UPLOAD-CONFIG-ID";

    static final String SDC_IND = "N";
    static final String TMP_CONVERTCONFIG_ESC_DATA = "tmp.convertconfig.escapeData";
    static final String CONFIG_PARAMS = "configuration-params";
    static final String TMP_MERGE_MERGED_DATA = "tmp.merge.mergedData";
    static final String DATA_SOURCE = "data-source";
    static final String FILE_CONTENT = "file-content";
    static final String CAPABILITIES = "capabilities";
    static final String NOT_SUPPORTED = "Not-Supported";
    static final String UNABLE_TO_READ_STR = "Unable to Read ";
    static final String UNABLE_TO_SAVE_RELATIONSHIP_STR = "Unable to save prepare_relationship";


    static final String SITE_LOCATION_PARAM = "site-location";

    private static final EELFLogger log = EELFManager.getInstance().getLogger(ConfigResourceNode.class);
    private final DGGeneralDBService db;

    /**
     * Constructor which provide default DB service
     */
    public ConfigResourceNode() {
        db = DGGeneralDBService.initialise();
    }

    /**
     * Constructor which allow to provide custom DB service, prefer to use no-arg constructor
     */
    public ConfigResourceNode(DGGeneralDBService dbService) {
        db = dbService;
    }

    public void getConfigFileReference(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {

        log.info("Received getConfigFiles call with params : " + inParams);
        String responsePrefix = inParams.get(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX);

        try {
            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix + ".") : "";
            QueryStatus status = db
                .getConfigFileReferenceByFileTypeNVnfType(ctx, DEVICE_CONF_PREFIX, DEVICE_CONF_FILE_TYPE);

            if (status == QueryStatus.NOT_FOUND || status == QueryStatus.FAILURE) {
                throw new QueryException("Unable to Read ConfigFileReference:device-configuration");
            }

            status = db.getConfigFileReferenceByFileTypeNVnfType(ctx, SUCCESS_PREFIX, SUCCESS_FILE_TYPE);

            if (status == QueryStatus.NOT_FOUND || status == QueryStatus.FAILURE) {
                throw new QueryException("Unable to Read ConfigFileReference:configuration_success");
            }

            status = db.getConfigFileReferenceByFileTypeNVnfType(ctx, FAILURE_PREFIX, FAILURE_FILE_TYPE);

            if (status == QueryStatus.NOT_FOUND || status == QueryStatus.FAILURE) {
                throw new QueryException("Unable to Read ConfigFileReference:configuration_error");
            }

            status = db.getConfigFileReferenceByFileTypeNVnfType(ctx, LOG_PREFIX, LOG_FILE_TYPE);

            if (status == QueryStatus.NOT_FOUND || status == QueryStatus.FAILURE) {
                throw new QueryException("Unable to Read ConfigFileReference:configuration_log");
            }

            ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
                AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS);
            log.info("GetConfigFileReference Successful ");
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
                AppcDataServiceConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
            log.error("Failed in GetConfigFileReference", e);
            throw new SvcLogicException(e.getMessage());
        }
    }

    public void getCommonConfigInfo(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {

        log.info("Received getDeviceInfo call with params : " + inParams);
        String responsePrefix = inParams.get(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX);

        try {
            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix + ".") : "";
            QueryStatus status = db.getDeviceProtocolByVnfType(ctx, DEVICE_PROTOCOL_PREFIX);

            if (status == QueryStatus.NOT_FOUND || status == QueryStatus.FAILURE) {
                throw new QueryException("Unable to Read device_interface_protocol");
            }

            status = db.getConfigureActionDGByVnfTypeNAction(ctx, CONF_ACTION_PREFIX);
            if (status == QueryStatus.FAILURE) {
                throw new QueryException("Unable to Read configure_action_dg");
            }

            if (status == QueryStatus.NOT_FOUND) {
                status = db.getConfigureActionDGByVnfType(ctx, CONF_ACTION_PREFIX);

                if (status == QueryStatus.NOT_FOUND || status == QueryStatus.FAILURE) {
                    throw new QueryException("Unable to Read configure_action_dg");
                }
            }

            ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
                AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS);
            log.info("getCommonConfigInfo Successful ");
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
                AppcDataServiceConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
            log.error("Failed in getCommonConfigInfo", e);
            throw new SvcLogicException(e.getMessage());
        }
    }

    /**
     * FileCategory can be config_template, parameter_definitions, parameter_yang
     */
    public void getTemplate(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {

        log.info("Received getTemplate call with params : " + inParams);

        String responsePrefix = inParams.get(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX);
        String fileCategory = inParams.get(AppcDataServiceConstant.INPUT_PARAM_FILE_CATEGORY);
        String templateName = ctx.getAttribute("template-name");
        String templateModelId = ctx.getAttribute("template-model-id");
        QueryStatus status;
        String responsePrefix1 = "";

        try {

            responsePrefix1 = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix + ".") : "";
            log.info("RESPONSEPREFIX : " + responsePrefix);
            log.info("RESPONSEPREFIX1 : " + responsePrefix1);

            if (StringUtils.isBlank(templateName)) {
                if (StringUtils.isNotBlank(templateModelId)) {
                    status = db.getTemplateWithTemplateModelId(ctx, responsePrefix, fileCategory,templateModelId);
                    if (status == QueryStatus.FAILURE) {
                        throw new QueryException(UNABLE_TO_READ_STR + fileCategory);
                    }
                    if (!(status == QueryStatus.NOT_FOUND) ) {
                        ctx.setAttribute(responsePrefix1 + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
                                AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS);
                        log.info("GetTemplate Successful ");
                        return;
                    }
                }
                status = db.getTemplate(ctx, responsePrefix, fileCategory);
                if (status == QueryStatus.FAILURE) {
                    throw new QueryException(UNABLE_TO_READ_STR + fileCategory);
                }

                if (status == QueryStatus.NOT_FOUND) {
                    if (StringUtils.isNotBlank(templateModelId)) {
                        status = db.getTemplateByVnfTypeNActionWithTemplateModelId(ctx, responsePrefix, fileCategory,templateModelId);
                        if (status == QueryStatus.FAILURE) {
                            throw new QueryException(UNABLE_TO_READ_STR + fileCategory);
                        }
                        if (!(status == QueryStatus.NOT_FOUND) ) {
                            ctx.setAttribute(responsePrefix1 + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
                                    AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS);
                            log.info("GetTemplate Successful ");
                            return;
                        }
                    }
                    if (status == QueryStatus.NOT_FOUND) {


                        status = db.getTemplateByVnfTypeNAction(ctx, responsePrefix, fileCategory);

                        if (status == QueryStatus.FAILURE) {
                            throw new QueryException(UNABLE_TO_READ_STR + fileCategory);
                        }

                        if (status == QueryStatus.NOT_FOUND) {
                            throw new QueryException(UNABLE_TO_READ_STR + fileCategory);
                        }
                    }


                }
            } else {

                status = db.getTemplateByTemplateName(ctx, responsePrefix, templateName);

                if (status == QueryStatus.NOT_FOUND || status == QueryStatus.FAILURE) {
                    throw new QueryException(UNABLE_TO_READ_STR + fileCategory + " template");
                }
            }

            ctx.setAttribute(responsePrefix1 + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
                AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS);
            log.info("GetTemplate Successful ");
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix1 + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
                AppcDataServiceConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix1 + AppcDataServiceConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
            log.error("Failed in getTemplate", e);

            throw new SvcLogicException(e.getMessage());
        }
    }

    void saveConfigFiles(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {

        log.info("Received saveConfigFiles call with params : " + inParams);

        String responsePrefix = inParams.get(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX);

        try {

            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix + ".") : "";
            QueryStatus status = db.saveConfigFiles(ctx, CONFIG_FILES_PREFIX);

            if (status == QueryStatus.FAILURE) {
                throw new QueryException("Unable to Save " + ctx.getAttribute(FILE_CATEGORY_PARAM) + " in configfiles");
            }

            status = db.getMaxConfigFileId(ctx, MAX_CONF_FILE_PREFIX, ctx.getAttribute(FILE_CATEGORY_PARAM));

            if (status == QueryStatus.NOT_FOUND || status == QueryStatus.FAILURE) {
                throw new QueryException(
                    "Unable to get " + ctx.getAttribute(FILE_CATEGORY_PARAM) + " from configfiles");
            }

            status = db.savePrepareRelationship(ctx, PREPARE_RELATIONSHIP_PARAM,
                ctx.getAttribute(CONFIG_FILE_ID_PARAM), SDC_IND);
            if (status == QueryStatus.FAILURE) {
                throw new QueryException(UNABLE_TO_SAVE_RELATIONSHIP_STR);
            }

            ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
                AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS);
            log.info("saveConfigFiles Successful ");
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
                AppcDataServiceConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
            log.error("Failed in saveConfigFiles", e);

            throw new SvcLogicException(e.getMessage());
        }
    }

    public void updateUploadConfig(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {

        log.info("Received updateUploadConfig call with params : " + inParams);

        String responsePrefix = inParams.get(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX);

        try {

            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix + ".") : "";

            ctx.setAttribute("tmp.escaped.devicerunningconfig",
                EscapeUtils.escapeSql(ctx.getAttribute("device-running-config")));

            QueryStatus status = db.saveUploadConfig(ctx, UPLOAD_CONFIG_PREFIX);

            if (status == QueryStatus.FAILURE) {
                throw new QueryException("Unable to Save configuration in upload_config");
            }

            status = db.getUploadConfigInfo(ctx, UPLOAD_CONFIG_INFO_PREFIX);

            if (status == QueryStatus.NOT_FOUND || status == QueryStatus.FAILURE) {
                throw new QueryException("Unable to get record from upload_config");
            }

            status = db.updateUploadConfig(ctx, UPLOAD_CONFIG_PREFIX,
                    Integer.parseInt(ctx.getAttribute(UPLOAD_CONFIG_ID_PARAM)));
            if (status == QueryStatus.FAILURE)
                throw new QueryException("Unable to upload upload_config");

            ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
                AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS);
            log.info("updateUploadConfig Successful ");
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
                AppcDataServiceConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
            log.error("Failed in updateUploadConfig", e);

            throw new SvcLogicException(e.getMessage());
        }
    }

    public void savePrepareRelationship(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {

        log.info("Received savePrepareRelationship call with params : " + inParams);

        String responsePrefix = inParams.get(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX);
        String sdcArtifactInd = inParams.get(AppcDataServiceConstant.INPUT_PARAM_SDC_ARTIFACT_IND);
        String fileId = inParams.get(AppcDataServiceConstant.INPUT_PARAM_FILE_ID);
        try {

            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix + ".") : "";

            QueryStatus status = db.savePrepareRelationship(ctx, PREPARE_RELATIONSHIP_PARAM, fileId, sdcArtifactInd);
            if (status == QueryStatus.FAILURE) {
                throw new QueryException(UNABLE_TO_SAVE_RELATIONSHIP_STR);
            }

            ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
                AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS);
            log.info("savePrepareRelationship Successful ");
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
                AppcDataServiceConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
            log.error("Failed in saveConfigFiles", e);

            throw new SvcLogicException(e.getMessage());
        }
    }

    public void saveConfigBlock(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {

        log.info("Received saveConfigBlock call with params : " + inParams);

        String responsePrefix = inParams.get(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX);

        try {
            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix + ".") : "";
            ctx.setAttribute(TMP_CONVERTCONFIG_ESC_DATA, EscapeUtils.escapeSql(ctx.getAttribute("configuration")));

            if (StringUtils.isBlank(ctx.getAttribute(CONFIG_PARAMS))) {
                saveDeviceConfiguration(inParams, ctx, "Request", ctx.getAttribute(TMP_CONVERTCONFIG_ESC_DATA),
                    ctx.getAttribute("configuration"));
            } else {

                saveConfigurationBlock(inParams, ctx);

                ctx.setAttribute(TMP_CONVERTCONFIG_ESC_DATA,
                    EscapeUtils.escapeSql(ctx.getAttribute(TMP_MERGE_MERGED_DATA)));
                saveDeviceConfiguration(inParams, ctx, "Configurator", ctx.getAttribute(TMP_CONVERTCONFIG_ESC_DATA),
                    ctx.getAttribute(TMP_MERGE_MERGED_DATA));

                saveConfigurationData(inParams, ctx);
            }

            ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
                AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS);
            log.info("saveConfigBlock Successful ");
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
                AppcDataServiceConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
            log.error("Failed in saveConfigBlock", e);

            throw new SvcLogicException(e.getMessage());
        }
    }

    public void saveTemplateConfig(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {

        log.info("Received saveTemplateConfig call with params : " + inParams);

        String responsePrefix = inParams.get(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX);

        try {
            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix + ".") : "";

            if (StringUtils.isBlank(ctx.getAttribute(CONFIG_PARAMS))) {

                ctx.setAttribute(TMP_CONVERTCONFIG_ESC_DATA,
                    EscapeUtils.escapeSql(ctx.getAttribute("config-template.file-content")));
                saveDeviceConfiguration(inParams, ctx, "Template", ctx.getAttribute(TMP_CONVERTCONFIG_ESC_DATA),
                    ctx.getAttribute("config-template.file-content"));

            } else {
                saveConfigurationData(inParams, ctx);

                ctx.setAttribute(TMP_CONVERTCONFIG_ESC_DATA,
                    EscapeUtils.escapeSql(ctx.getAttribute(TMP_MERGE_MERGED_DATA)));
                saveDeviceConfiguration(inParams, ctx, "Configurator", ctx.getAttribute(TMP_CONVERTCONFIG_ESC_DATA),
                    ctx.getAttribute(TMP_MERGE_MERGED_DATA));

            }

            QueryStatus status = db.savePrepareRelationship(ctx, PREPARE_RELATIONSHIP_PARAM,
                ctx.getAttribute("config-template.config-file-id"), "Y");
            if (status == QueryStatus.FAILURE) {
                throw new QueryException(UNABLE_TO_SAVE_RELATIONSHIP_STR);
            }

            ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
                AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS);
            log.info("saveTemplateConfig Successful ");
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
                AppcDataServiceConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
            log.error("Failed in saveTemplateConfig", e);

            throw new SvcLogicException(e.getMessage());
        }
    }

    public void saveStyleSheetConfig(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {

        log.info("Received saveStyleSheet call with params : " + inParams);

        String responsePrefix = inParams.get(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX);

        try {

            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix + ".") : "";
            ctx.setAttribute(TMP_CONVERTCONFIG_ESC_DATA,
                EscapeUtils.escapeSql(ctx.getAttribute(TMP_MERGE_MERGED_DATA)));
            saveDeviceConfiguration(inParams, ctx, "StyleSheet", ctx.getAttribute(TMP_CONVERTCONFIG_ESC_DATA),
                ctx.getAttribute(TMP_MERGE_MERGED_DATA));

            ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
                AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS);
            log.info("saveStyleSheet Successful ");
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
                AppcDataServiceConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
            log.error("Failed in saveStyleSheet", e);

            throw new SvcLogicException(e.getMessage());
        }
    }

    public void getSmmChainKeyFiles(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {

        log.info("Received saveStyleSheet call with params : " + inParams);

        String responsePrefix = inParams.get(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX);
        String siteLocation = ctx.getAttribute(SITE_LOCATION_PARAM);

        QueryStatus status;

        try {

            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix + ".") : "";

            status = db.getTemplateByArtifactType(ctx, "smm", "smm", siteLocation);

            if (status == QueryStatus.FAILURE) {
                throw new QueryException("Unable to Read smm file");
            }

            status = db.getTemplateByArtifactType(ctx, "intermediate-ca-chain", "intermediate_ca_chain", siteLocation);

            if (status == QueryStatus.FAILURE) {
                throw new QueryException("Unable to Read intermediate_ca_chain file");
            }

            status = db.getTemplateByArtifactType(ctx, "server-certificate-and-key", "server_certificate_and_key",
                siteLocation);

            if (status == QueryStatus.FAILURE) {
                throw new QueryException("Unable to Read server_certificate_and_key file");
            }

            ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
                AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS);
            log.info("saveStyleSheet Successful ");
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
                AppcDataServiceConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
            log.error("Failed in saveStyleSheet", e);

            throw new SvcLogicException(e.getMessage());
        }
    }

    public void saveDeviceConfiguration(Map<String, String> inParams, SvcLogicContext ctx, String dataSource,
                                        String fileContent, String deviceConfig) throws SvcLogicException {
        ctx.setAttribute(DATA_SOURCE, dataSource);
        ctx.setAttribute(FILE_CONTENT, fileContent);
        ctx.setAttribute(FILE_CATEGORY_PARAM, "device_configuration");
        ctx.setAttribute("deviceconfig-file-content", deviceConfig);

        saveConfigFiles(inParams, ctx);
    }

    private void saveConfigurationBlock(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {
        ctx.setAttribute(DATA_SOURCE, "Request");
        ctx.setAttribute(FILE_CONTENT, ctx.getAttribute(TMP_CONVERTCONFIG_ESC_DATA));
        ctx.setAttribute(FILE_CATEGORY_PARAM, "configuration_block");
        saveConfigFiles(inParams, ctx);
    }

    private void saveConfigurationData(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {
        ctx.setAttribute(DATA_SOURCE, ctx.getAttribute("originator-id"));
        ctx.setAttribute(FILE_CONTENT, ctx.getAttribute(CONFIG_PARAMS));
        ctx.setAttribute(FILE_CATEGORY_PARAM, "config_data");
        saveConfigFiles(inParams, ctx);
    }

    public void getConfigFilesByVnfVmNCategory(Map<String, String> inParams, SvcLogicContext ctx)
        throws SvcLogicException {

        log.info("Received getConfigFilesByVnfVmNCategory call with params : " + inParams);

        String responsePrefix = inParams.get(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX);
        String fileCategory = inParams.get(AppcDataServiceConstant.INPUT_PARAM_FILE_CATEGORY);
        String vnfId = inParams.get(AppcDataServiceConstant.INPUT_PARAM_VNF_ID);
        String vmName = inParams.get(AppcDataServiceConstant.INPUT_PARAM_VM_NAME);
        try {
            QueryStatus status = db.getConfigFilesByVnfVmNCategory(ctx, responsePrefix, fileCategory, vnfId, vmName);

            if (status == QueryStatus.NOT_FOUND || status == QueryStatus.FAILURE) {
                throw new QueryException("Unable to get " + ctx.getAttribute("fileCategory") + " from configfiles");
            }

            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix + ".") : "";
            ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
                AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS);
            log.info("getConfigFilesByVnfVmNCategory Successful "
                + ctx.getAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS));
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
                AppcDataServiceConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
            log.error("Failed in getConfigFilesByVnfVmNCategory", e);

            throw new SvcLogicException(e.getMessage());
        }
    }

    public void getDownloadConfigTemplateByVnf(Map<String, String> inParams, SvcLogicContext ctx)
        throws SvcLogicException {

        log.info("Received getDownloadConfigTemplateByVnfNProtocol call with params : " + inParams);

        String responsePrefix = inParams.get(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX);
        try {
            QueryStatus status = db.getDownloadConfigTemplateByVnf(ctx, responsePrefix);

            if (status == QueryStatus.NOT_FOUND || status == QueryStatus.FAILURE) {
                throw new QueryException("Unable to get download config template.");
            }

            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix + ".") : "";
            ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
                AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS);
            log.info("getDownloadConfigTemplateByVnf Successful "
                + ctx.getAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS));
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
                AppcDataServiceConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
            log.error("Failed in getDownloadConfigTemplateByVnf", e);

            throw new SvcLogicException(e.getMessage());
        }
    }

    public void saveConfigTransactionLog(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {

        String responsePrefix = inParams.get(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX);

        String messageType = inParams.get(AppcDataServiceConstant.INPUT_PARAM_MESSAGE_TYPE);
        String message = inParams.get(AppcDataServiceConstant.INPUT_PARAM_MESSAGE);

        try {

            SvcLogicContext logctx = new SvcLogicContext();
            String escapedMessage = EscapeUtils.escapeSql(message);

            logctx.setAttribute("request-id", ctx.getAttribute("request-id"));
            logctx.setAttribute("log-message-type", messageType);
            logctx.setAttribute("log-message", escapedMessage);

            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix + ".") : "";
            QueryStatus status = db.saveConfigTransactionLog(logctx, responsePrefix);

            logctx.setAttribute("log-message", null);

            if (status == QueryStatus.FAILURE) {
                throw new QueryException("Unable to insert into config_transaction_log");
            }

        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
                AppcDataServiceConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
            log.error("Failed in saveConfigTransactionLog", e);
            throw new SvcLogicException(e.getMessage());
        }
    }

    public void getVnfcReference(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {

        log.info("Received getVnfcReference call with params : " + inParams);

        String responsePrefix = inParams.get(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX);
        String templateModelId = ctx.getAttribute("template-model-id");
        log.info("getVnfcReference():::"+templateModelId);
        QueryStatus status = null;

        try {
            if (!StringUtils.isBlank(ctx.getAttribute("vnfc-type"))) {


                status = db.getVnfcReferenceByVnfcTypeNAction(ctx, responsePrefix);

                if (status == QueryStatus.FAILURE) {
                    throw new QueryException("Unable to Read vnfc-reference");
                }

            }
            if (StringUtils.isNotBlank(templateModelId)) {
                status = db.getVnfcReferenceByVnfTypeNActionWithTemplateModelId(ctx, responsePrefix,templateModelId);
                if (status == QueryStatus.FAILURE) {
                    throw new QueryException("Unable to Read vnfc-reference with template-model-id");
                }
            }
            if (StringUtils.isBlank(templateModelId) || (StringUtils.isNotBlank(templateModelId) && (status == QueryStatus.NOT_FOUND))) {
                status = db.getVnfcReferenceByVnfTypeNAction(ctx, responsePrefix);

                if (status == QueryStatus.NOT_FOUND || status == QueryStatus.FAILURE) {
                    throw new QueryException("Unable to Read vnfc reference");
                }
            }

            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix + ".") : "";
            ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
                AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS);
            log.info("getVnfcReference Successful ");
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
                AppcDataServiceConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
            log.error("Failed in getVnfcReference", e);

            throw new SvcLogicException(e.getMessage());
        }
    }

    public void getCapability(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {
        log.info("Received getCapability call with params : " + inParams);
        String responsePrefix = inParams.get(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX);
        responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix + ".") : "";
        String caplevel = inParams.get("caplevel");
        String findCapability = inParams.get("checkCapability");
        String vServerId = inParams.get("vServerId");
        if (!checkIfCapabilityCheckNeeded(caplevel, findCapability)) {
            ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
                AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS);
            log.info("getCapability Successful - No need for capability check for this action");
            return;
        }
        try {
            String cap = db.getCapability(ctx, inParams.get("vnf-type"));
            log.info("getCapability::returned from DB::+cap");
            if (StringUtils.isBlank(cap)) {
                ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
                    AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS);
                log.info("getCapability Successful - No capability blocks found");
                return;
            }
            ObjectMapper mapper = new ObjectMapper();
            JsonNode caps = mapper.readTree(cap);
            log.info("From DB =   " + caps);
            JsonNode capabilities = caps.get(CAPABILITIES);
            log.info("capabilities =   " + capabilities);
            if (caplevel != null && !caplevel.isEmpty()) {
                JsonNode subCapabilities = capabilities.get(caplevel);
                log.info("subCapabilities =  " + caplevel + " : " + subCapabilities);
                if (caplevel.equalsIgnoreCase(AppcDataServiceConstant.CAPABILITY_VM_LEVEL)
                    && (null == subCapabilities || subCapabilities.isNull() || subCapabilities.size() == 0)) {
                    ctx.setAttribute(CAPABILITIES, "None");
                    ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
                        AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS);
                    log.info("getCapability Successful ");
                    return;
                }
                if (findCapability != null && !findCapability.isEmpty()) {
                    if (subCapabilities != null && subCapabilities.toString().contains(findCapability)) {
                        if (caplevel.equalsIgnoreCase(AppcDataServiceConstant.CAPABILITY_VM_LEVEL)) {
                            processCapabilitiesForVMLevel(vServerId, ctx, findCapability, subCapabilities);
                        } else {
                            ctx.setAttribute(CAPABILITIES, "Supported");
                        }
                    } else {
                        ctx.setAttribute(CAPABILITIES, NOT_SUPPORTED);
                    }
                } else {
                    ctx.setAttribute(responsePrefix + "capabilities." + caplevel, subCapabilities.toString());
                }

            } else {
                ctx.setAttribute(responsePrefix + CAPABILITIES, capabilities.toString());
            }
            ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
                AppcDataServiceConstant.OUTPUT_STATUS_SUCCESS);
            log.info("getCapability Successful ");
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_STATUS,
                AppcDataServiceConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + AppcDataServiceConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
            log.error("Failed in getCapability", e);

            throw new SvcLogicException(e.getMessage());
        }
    }

    public void processCapabilitiesForVMLevel(String vServerId, SvcLogicContext ctx, String findCapability,
        JsonNode subCapabilities) {
        log.info("processCapabilitiesForVMLevel():::subCapabilities::" + subCapabilities.toString() + ",vServerId::"
            + vServerId);
        if (subCapabilities.size() == 0) {
            ctx.setAttribute(CAPABILITIES, "None");
            log.info("processCapabilitiesForVMLevel :: No VM block found!!");
            return;
        }
        JsonNode vmCaps = null;
        for (JsonNode cap : subCapabilities) {
            if (null != cap && null != cap.get(findCapability)
                && StringUtils.isNotBlank(cap.get(findCapability).toString())) {
                vmCaps = cap.get(findCapability);
                log.info("processCapabilitiesForVMLevel()::vmCaps found" + vmCaps.toString());
                break;
            }
        }

        if (null == vmCaps || vmCaps.isNull() || vmCaps.size() == 0) {
            ctx.setAttribute(CAPABILITIES, NOT_SUPPORTED);
            log.info("processCapabilitiesForVMLevel :: Found non-empty VM block but Not desired capability!!");
            return;
        }

        String vnfcFunctionCode = getVnfcFunctionCodeForVserver(ctx, vServerId);
        if (StringUtils.isBlank(vnfcFunctionCode)) {
            log.info("processCapabilitiesForVMLevel() :: vnfcFunctionCode is not present in context!!!");
            ctx.setAttribute(CAPABILITIES, NOT_SUPPORTED);
            return;
        }

        if (vmCaps.toString().contains(vnfcFunctionCode)) {
            ctx.setAttribute(CAPABILITIES, "Supported");
        } else {
            ctx.setAttribute(CAPABILITIES, NOT_SUPPORTED);
        }
        log.info("End processCapabilitiesForVMLevel():capabilities is ::" + ctx.getAttribute(CAPABILITIES));
    }

    private String getVnfcFunctionCodeForVserver(SvcLogicContext ctx, String vServerId) {
        log.info("getVnfcFunctionCodeForVserver()::vServerId=" + vServerId);
        for (Object key : ctx.getAttributeKeySet()) {
            String parmName = (String) key;
            String parmValue = ctx.getAttribute(parmName);
            log.info(parmName + "=" + parmValue);

        }
        String vnfcFunctionCode = ctx.getAttribute("tmp.vnfInfo.vm.vnfc.vnfc-function-code");
        log.info("getVnfcFunctionCodeForVserver()::vnfcFunctionCode=" + vnfcFunctionCode);
        return vnfcFunctionCode;
    }

    public boolean checkIfCapabilityCheckNeeded(String caplevel, String findCapability) {
        boolean capabilityCheckNeeded = true;
        if (!StringUtils.equalsIgnoreCase(caplevel, AppcDataServiceConstant.CAPABILITY_VM_LEVEL)) {
            List<AppcDataServiceConstant.ACTIONS> actionList = new ArrayList<>(
                Arrays.asList(AppcDataServiceConstant.ACTIONS.values()));
            for (AppcDataServiceConstant.ACTIONS action : actionList) {
                if (StringUtils.equalsIgnoreCase(action.toString(), findCapability)) {
                    capabilityCheckNeeded = false;
                    break;
                }
            }
        }
        return capabilityCheckNeeded;
    }

}
