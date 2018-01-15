/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.artifact.handler.node;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.google.common.base.Strings;
import java.sql.SQLException;
import java.util.Optional;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.onap.appc.artifact.handler.dbservices.DBService;
import org.onap.appc.artifact.handler.utils.ArtifactHandlerProviderUtil;
import org.onap.appc.yang.YANGGenerator;
import org.onap.appc.yang.exception.YANGGenerationException;
import org.onap.appc.yang.impl.YANGGeneratorFactory;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;
import org.onap.sdnc.config.params.transformer.tosca.ArtifactProcessorImpl;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.function.Function;

import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.ACTION;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.ACTION_LEVEL;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.ACTION_LEVEL_VF_MODULE;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.ACTION_LEVEL_VM;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.ACTION_LEVEL_VNF;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.ACTION_LEVEL_VNFC;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.ARTIFACT_CONTENTS;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.ARTIFACT_DESRIPTION;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.ARTIFACT_NAME;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.ARTIFACT_NAME_CAPABILITY;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.ARTIFACT_NAME_REFERENCE;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.ARTIFACT_TYPE;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.ARTIFACT_UUID;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.ARTIFACT_VERSION;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.CAPABILITY;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.DB_CONFIG_ACTION_DG;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.DB_DEVICE_AUTHENTICATION;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.DB_DEVICE_INTERFACE_PROTOCOL;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.DB_DOWNLOAD_DG_REFERENCE;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.DB_SDC_REFERENCE;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.DEVICE_PROTOCOL;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.DISTRIBUTION_ID;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.DOCUMENT_PARAMETERS;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.DOWNLOAD_DG_REFERENCE;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.FILE_CATEGORY;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.GROUP_NOTATION_TYPE;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.GROUP_NOTATION_VALUE;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.IPADDRESS_V4_OAM_VIP;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.PARAMETER_YANG;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.PD;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.PORT_NUMBER;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.REFERENCE;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.REQUEST_INFORMATION;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.REQUEST_ID;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.RESOURCE_INSTANCE_NAME;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.RESOURCE_TYPE;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.RESOURCE_UUID;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.RESOURCE_VERSION;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.SERVICE_DESCRIPTION;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.SERVICE_NAME;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.SERVICE_UUID;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.TEMPLATE;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.TOSCA_MODEL;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.USER_NAME;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.VM;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.VM_INSTANCE;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.VNFC;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.VNFC_FUNCTION_CODE;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.VNFC_FUNCTION_CODE_LIST;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.VNFC_INSTANCE;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.VNFC_TYPE;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.VNF_TYPE;
import org.onap.sdnc.config.params.transformer.tosca.exceptions.ArtifactProcessorException;

public class ArtifactHandlerNode implements SvcLogicJavaPlugin {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(ArtifactHandlerNode.class);

    private static final String CONFIG_MODIFY_ACTION = "ConfigModify";
    private static final String CONFIGURE_ACTION = "Configure";
    private static final String TOSCA_DOC = "Tosca";
    private static final String YANG_DOC = "Yang";
    private static final int INVALID_VERSION = 0;

    private final ArtifactHandlerProviderUtil ahpUtil = new ArtifactHandlerProviderUtil();

    public void processArtifact(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException, ConfigurationException {
        String responsePrefix = inParams.get("response_prefix");
        if (inParams.get("postData") != null) {
            log.info("Received request for process Artifact with params: " + inParams.toString());
            String postData = inParams.get("postData");
            JSONObject input = new JSONObject(postData).getJSONObject("input");
            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix + ".") : "";
            storeUpdateSdcArtifacts(input);
        }
    }

    private boolean storeUpdateSdcArtifacts(JSONObject postDataJson) throws SvcLogicException, ConfigurationException {
        log.info("Starting processing of SDC Artifacts into Handler with Data : " + postDataJson.toString());
        boolean result = false;
        try {
            JSONObject requestInformation = postDataJson.getJSONObject(REQUEST_INFORMATION);
            JSONObject documentInformation = postDataJson.getJSONObject(DOCUMENT_PARAMETERS);
            String artifactName = documentInformation.getString(ARTIFACT_NAME);
            updateStoreArtifacts(requestInformation, documentInformation);
            if (artifactName.toLowerCase().startsWith(REFERENCE)) {
                result = storeReferenceData(requestInformation, documentInformation);
            } else if (artifactName.toLowerCase().startsWith(PD)) {
                result = createDataForPD(requestInformation, documentInformation);
            }
        } catch (YANGGenerationException | SQLException | ArtifactProcessorException e) {
            String requestId = postDataJson.getJSONObject(REQUEST_INFORMATION).getString(REQUEST_ID);
            throw new SvcLogicException("Error while processing Request ID : " + requestId, e);
        }
        return result;
    }

    private boolean createDataForPD(JSONObject requestInformation, JSONObject documentInformation)
        throws SvcLogicException, YANGGenerationException, ArtifactProcessorException {

        String fn = "ArtifactHandlerNode.createReferenceDataForPD";
        String artifactName = documentInformation.getString(ARTIFACT_NAME);

        log.info(fn + "Received PD File Name: " + artifactName + " and suffix length " + PD.length());

        String suffix = artifactName.substring(PD.length());
        createArtifactRecords(requestInformation, documentInformation, suffix);

        return true;
    }

    private void createArtifactRecords(JSONObject requestInformation, JSONObject documentInformation, String suffix)
        throws ArtifactProcessorException, SvcLogicException, YANGGenerationException {

        log.info("Creating Tosca Records and storing into SDC Artifacts");

        String[] docs = { TOSCA_DOC, YANG_DOC };
        String pdFileContents = documentInformation.getString(ARTIFACT_CONTENTS);

        OutputStream toscaStream = new ByteArrayOutputStream();
        ArtifactProcessorImpl toscaGenerator = new ArtifactProcessorImpl();
        toscaGenerator.generateArtifact(pdFileContents, toscaStream);
        String toscaContents = toscaStream.toString();

        log.info("Generated Tosca File : " + toscaContents);

        String yangContents = "YANG generation is in Progress";

        String yangName = null;
        for (String doc : docs) {
            documentInformation.put(ARTIFACT_TYPE, doc.concat("Type"));
            documentInformation.put(ARTIFACT_DESRIPTION, doc.concat("Model"));
            documentInformation.put(ARTIFACT_NAME, doc.concat(suffix));

            String value = null;
            if (TOSCA_DOC.equals(doc)) {
                value = ahpUtil.escapeSql(toscaContents);
            } else if (YANG_DOC.equals(doc)) {
                value = ahpUtil.escapeSql(yangContents);
            }
            documentInformation.put(ARTIFACT_CONTENTS, value);

            yangName = doc.concat(suffix);
            updateStoreArtifacts(requestInformation, documentInformation);
        }

        String artifactId = getArtifactID(yangName);
        OutputStream yangStream = new ByteArrayOutputStream();
        YANGGenerator yangGenerator = YANGGeneratorFactory.getYANGGenerator();
        yangGenerator.generateYANG(artifactId, toscaContents, yangStream);
        updateYangContents(artifactId, ahpUtil.escapeSql(yangStream.toString()));
    }

    private void updateYangContents(String artifactId, String yangContents) throws SvcLogicException {
        SvcLogicContext context = new SvcLogicContext();
        DBService dbservice = DBService.initialise();
        dbservice.updateYangContents(context, artifactId, yangContents);
    }

    private String getArtifactID(String yangName) throws SvcLogicException {
        SvcLogicContext context = new SvcLogicContext();
        DBService dbservice = DBService.initialise();

        return dbservice.getArtifactID(context, yangName);
    }

    protected boolean updateStoreArtifacts(JSONObject requestInformation, JSONObject documentInformation) throws SvcLogicException {
        log.info("UpdateStoreArtifactsStarted storing of SDC Artifacts ");

        SvcLogicContext context = new SvcLogicContext();
        DBService dbservice = DBService.initialise();

        context.setAttribute("artifact_name", documentInformation.getString(ARTIFACT_NAME));

        Optional<String> internalVersion = dbservice
            .getInternalVersionNumber(context, documentInformation.getString(ARTIFACT_NAME), null);

        log.info("Internal Version number received from Database : " + internalVersion.orElse(null));

        int intVersion = parseInternalVersion(internalVersion);

        setAttribute(context, documentInformation::getString, SERVICE_UUID);
        setAttribute(context, documentInformation::getString, DISTRIBUTION_ID);
        setAttribute(context, documentInformation::getString, SERVICE_NAME);
        setAttribute(context, documentInformation::getString, SERVICE_DESCRIPTION);
        setAttribute(context, documentInformation::getString, RESOURCE_UUID);
        setAttribute(context, documentInformation::getString, RESOURCE_INSTANCE_NAME);
        setAttribute(context, documentInformation::getString, RESOURCE_VERSION);
        setAttribute(context, documentInformation::getString, RESOURCE_TYPE);
        setAttribute(context, documentInformation::getString, ARTIFACT_UUID);
        setAttribute(context, documentInformation::getString, ARTIFACT_TYPE);
        setAttribute(context, documentInformation::getString, ARTIFACT_VERSION);
        setAttribute(context, documentInformation::getString, ARTIFACT_DESRIPTION);
        setAttribute(context, documentInformation::getString, ARTIFACT_NAME);

        setAttribute(context, s -> ahpUtil.escapeSql(documentInformation.getString(s)), ARTIFACT_CONTENTS);

        dbservice.saveArtifacts(context, intVersion);

        return true;
    }

    private int parseInternalVersion(Optional<String> internalVersion) {
        return internalVersion
            .map(Integer::parseInt)
            .map(i -> i + 1)
            .orElse(INVALID_VERSION);
    }

    public boolean storeReferenceData(JSONObject requestInformation, JSONObject documentInformation)
        throws SvcLogicException, SQLException, ConfigurationException {
        log.info("Started storing of SDC Artifacs into Handler");

        DBService dbservice = DBService.initialise();
        String contentString = ahpUtil.escapeSql(documentInformation.getString(ARTIFACT_CONTENTS));

        JSONObject contentObject = new JSONObject(contentString);
        JSONArray contentArray = contentObject.getJSONArray("reference_data");

        for (int i = 0; i < contentArray.length(); i++) {
            JSONObject content = contentArray.getJSONObject(i);
            processContent(dbservice, content, documentInformation);
        }
        return true;
    }

    private void processContent(DBService dbservice, JSONObject content, JSONObject documentInformation)
        throws SvcLogicException, SQLException, ConfigurationException {
        log.info("contentString =" + content.toString());
        JSONObject scope = content.getJSONObject("scope");
        log.info("scope :" + scope);

        SvcLogicContext context = new SvcLogicContext();

        setAttribute(context, scope::getString, VNF_TYPE);
        setAttribute(context, content::getString, ACTION);
        setAttribute(context, content::getString, ACTION_LEVEL);
        setAttribute(context, documentInformation::getString, ARTIFACT_TYPE);

        JSONArray vnfActionList = new JSONArray();
        JSONArray vnfcActionList = new JSONArray();
        JSONArray vfModuleActionList = new JSONArray();
        JSONArray vmActionVnfcFunctionCodesList = new JSONArray();

        String actionLevel = content.getString(ACTION_LEVEL).toUpperCase();
        switch (actionLevel) {
            case ACTION_LEVEL_VNFC:
                vnfcActionList.put(content.getString(ACTION));
                break;
            case ACTION_LEVEL_VF_MODULE:
                vfModuleActionList.put(content.getString(ACTION));
                break;
            case ACTION_LEVEL_VNF:
                vnfActionList.put(content.getString(ACTION));
                break;
            case ACTION_LEVEL_VM:
                JSONArray vnfcFunctionCodeList = content.optJSONArray(VNFC_FUNCTION_CODE_LIST);
                if (vnfcFunctionCodeList != null) {
                    log.info("Found vnfc-function-code-list!!");
                    JSONObject obj = new JSONObject();
                    obj.put(content.getString(ACTION), vnfcFunctionCodeList);
                    vmActionVnfcFunctionCodesList.put(obj);
                } else {
                    log.info("Not getting JSONArray for VNFC FUNCTION CODES");
                }
                break;
            default:
                break;
        }

        String vnfcTypeScope = scope.optString(VNFC_TYPE);
        context.setAttribute(VNFC_TYPE, vnfcTypeScope);

        if (!Strings.isNullOrEmpty(vnfcTypeScope)) {
            JSONObject capabilities = new JSONObject();

            capabilities.put("vnf", vnfActionList);
            capabilities.put("vf-module", vfModuleActionList);
            capabilities.put("vnfc", vnfcActionList);
            capabilities.put("vm", vmActionVnfcFunctionCodesList);

            String vnfType = scope.getString(VNF_TYPE);

            String artifactName = ahpUtil.escapeSql(documentInformation.getString(ARTIFACT_NAME));
            String capabilityArtifactName = StringUtils.replace(artifactName, ARTIFACT_NAME_REFERENCE, ARTIFACT_NAME_CAPABILITY);
            processAndStoreCapabilitiesArtifact(dbservice, documentInformation, capabilities, capabilityArtifactName, vnfType);
        } else {
            log.info("No capability Artifact for this reference data as it is at VNFC level!!");
        }

        setAttribute(context, content::optString, DEVICE_PROTOCOL);
        setAttribute(context, content::optString, USER_NAME);
        setAttribute(context, content::optString, PORT_NUMBER);

        processArtifacts(dbservice, content, context);

        if (content.getString(ACTION).equals(CONFIGURE_ACTION) || content.getString(ACTION).equals(CONFIG_MODIFY_ACTION)) {
            if (!Strings.isNullOrEmpty(content.optString(DOWNLOAD_DG_REFERENCE))) {
                setAttribute(context, content::getString, DOWNLOAD_DG_REFERENCE);
                dbservice.processDownloadDgReference(context, dbservice.isArtifactUpdateRequired(context, DB_DOWNLOAD_DG_REFERENCE));
            }
            if (Strings.isNullOrEmpty(context.getAttribute(DOWNLOAD_DG_REFERENCE))) {
                context.setAttribute(DOWNLOAD_DG_REFERENCE, dbservice.getDownLoadDGReference(context));
            }
            dbservice.processConfigActionDg(context, dbservice.isArtifactUpdateRequired(context, DB_CONFIG_ACTION_DG));
            if (content.getString(ACTION).equals(CONFIGURE_ACTION)) {
                dbservice.processDeviceInterfaceProtocol(context, dbservice.isArtifactUpdateRequired(context, DB_DEVICE_INTERFACE_PROTOCOL));
            }
        }
        dbservice.processDeviceAuthentication(context, dbservice.isArtifactUpdateRequired(context, DB_DEVICE_AUTHENTICATION));
        populateProtocolReference(dbservice, content);

        context.setAttribute(VNFC_TYPE, null);

        processVms(dbservice, content, context);
    }

    private void processArtifacts(DBService dbservice, JSONObject content, SvcLogicContext context)
        throws SQLException, ConfigurationException, SvcLogicException {

        JSONArray artifactLists = content.optJSONArray("artifact-list");
        if (artifactLists != null) {

            String suffix = null;
            boolean isPdFile = false;
            for (int i = 0; i < artifactLists.length(); i++) {
                JSONObject artifact = artifactLists.getJSONObject(i);
                log.info("artifact is " + artifact);
                setAttribute(context, artifact::getString, ARTIFACT_NAME);
                context.setAttribute(FILE_CATEGORY, artifact.getString(ARTIFACT_TYPE));

                if (artifact.getString(ARTIFACT_NAME).toLowerCase().startsWith(PD)) {
                    suffix = artifact.getString(ARTIFACT_NAME).substring(PD.length());
                    isPdFile = true;
                }
                log.info("Artifact-type = " + context.getAttribute(ARTIFACT_TYPE));
                dbservice.processSdcReferences(context, dbservice.isArtifactUpdateRequired(context, DB_SDC_REFERENCE));

                cleanArtifactInstanceData(context);
            }

            if (isPdFile) {
                context.setAttribute(ARTIFACT_NAME, TOSCA_DOC.concat(suffix));
                context.setAttribute(FILE_CATEGORY, TOSCA_MODEL);
                dbservice.processSdcReferences(context, dbservice.isArtifactUpdateRequired(context, DB_SDC_REFERENCE));
                context.setAttribute(ARTIFACT_NAME, YANG_DOC.concat(suffix));
                context.setAttribute(FILE_CATEGORY, PARAMETER_YANG);
                dbservice.processSdcReferences(context, dbservice.isArtifactUpdateRequired(context, DB_SDC_REFERENCE));
            }
        }
    }

    private void processVms(DBService dbservice, JSONObject content, SvcLogicContext context) throws SvcLogicException {
        JSONArray vmList = content.optJSONArray(VM);
        if (vmList != null) {
            dbservice.cleanUpVnfcReferencesForVnf(context);
            for (int i = 0; i < vmList.length(); i++) {
                JSONObject vmInstance = vmList.getJSONObject(i);
                setAttribute(context, s -> String.valueOf(vmInstance.getInt(s)), VM_INSTANCE);

                log.info("VALUE = " + context.getAttribute(VM_INSTANCE));

                JSONArray vnfcInstanceList = vmInstance.getJSONArray(VNFC);
                for (int k = 0; k < vnfcInstanceList.length(); k++) {
                    processVnfcInstance(dbservice, content, context, vnfcInstanceList.getJSONObject(k));
                }
                context.setAttribute(VM_INSTANCE, null);
            }
        }
    }

    private void processVnfcInstance(DBService dbservice, JSONObject content, SvcLogicContext context, JSONObject vnfcInstance)
        throws SvcLogicException {

        setAttribute(context, vnfcInstance::getString, VNFC_TYPE);
        setAttribute(context, vnfcInstance::getString, VNFC_FUNCTION_CODE);
        setAttribute(context, s -> String.valueOf(vnfcInstance.getInt(s)), VNFC_INSTANCE);

        setAttribute(context, vnfcInstance::optString, IPADDRESS_V4_OAM_VIP);
        setAttribute(context, vnfcInstance::optString, GROUP_NOTATION_TYPE);
        setAttribute(context, vnfcInstance::optString, GROUP_NOTATION_VALUE);

        if (content.getString(ACTION).equals(CONFIGURE_ACTION)) {
            dbservice.processVnfcReference(context, false);
        }

        cleanVnfcInstance(context);
    }

    private void cleanArtifactInstanceData(SvcLogicContext context) {
        context.setAttribute(ARTIFACT_NAME, null);
        context.setAttribute(FILE_CATEGORY, null);
    }

    private void cleanVnfcInstance(SvcLogicContext context) {
        context.setAttribute(VNFC_INSTANCE, null);
        context.setAttribute(VNFC_TYPE, null);
        context.setAttribute(VNFC_FUNCTION_CODE, null);
        context.setAttribute(IPADDRESS_V4_OAM_VIP, null);
        context.setAttribute(GROUP_NOTATION_TYPE, null);
        context.setAttribute(GROUP_NOTATION_VALUE, null);
    }

    private void processAndStoreCapabilitiesArtifact(DBService dbservice, JSONObject documentInformation,
                                                     JSONObject capabilities, String capabilityArtifactName, String vnfType)
        throws SvcLogicException, SQLException, ConfigurationException {

        log.info("Begin-->processAndStoreCapabilitiesArtifact ");

        try {
            JSONObject newCapabilitiesObject = new JSONObject();
            newCapabilitiesObject.put("capabilities", capabilities);

            SvcLogicContext context = new SvcLogicContext();
            context.setAttribute(ARTIFACT_NAME, capabilityArtifactName);
            context.setAttribute(FILE_CATEGORY, CAPABILITY);
            context.setAttribute(ACTION, null);
            context.setAttribute(VNFC_TYPE, null);
            context.setAttribute(ARTIFACT_TYPE, null);
            context.setAttribute(VNF_TYPE, vnfType);
            context.setAttribute(ARTIFACT_CONTENTS, newCapabilitiesObject.toString());

            boolean updateRequired = dbservice.isArtifactUpdateRequired(context, DB_SDC_REFERENCE);
            dbservice.processSdcReferences(context, updateRequired);

            Optional<String> internalVersion = dbservice
                .getInternalVersionNumber(context, context.getAttribute(ARTIFACT_NAME), null);

            log.info("Internal Version number received from Database : " + internalVersion.orElse(null));

            int intVersion = parseInternalVersion(internalVersion);

            setAttribute(context, documentInformation::getString, SERVICE_UUID);
            setAttribute(context, documentInformation::getString, DISTRIBUTION_ID);
            setAttribute(context, documentInformation::getString, SERVICE_NAME);
            setAttribute(context, documentInformation::getString, SERVICE_DESCRIPTION);
            setAttribute(context, documentInformation::getString, RESOURCE_UUID);
            setAttribute(context, documentInformation::getString, RESOURCE_INSTANCE_NAME);
            setAttribute(context, documentInformation::getString, RESOURCE_VERSION);
            setAttribute(context, documentInformation::getString, RESOURCE_TYPE);
            setAttribute(context, documentInformation::getString, ARTIFACT_UUID);
            setAttribute(context, documentInformation::getString, ARTIFACT_VERSION);
            setAttribute(context, documentInformation::getString, ARTIFACT_DESRIPTION);

            dbservice.saveArtifacts(context, intVersion);

        } catch (SvcLogicException | SQLException | ConfigurationException e) {
            log.error("Error saving capabilities artifact to DB: " + e.toString());
            throw e;
        } finally {
            log.info("End-->processAndStoreCapabilitiesArtifact ");
        }
    }

    private void setAttribute(SvcLogicContext context, Function<String, String> value, String key) {
        context.setAttribute(key, value.apply(key));
    }

    private void populateProtocolReference(DBService dbservice, JSONObject content) throws SvcLogicException {
        log.info("Begin-->populateProtocolReference ");

        SvcLogicContext context = new SvcLogicContext();
        JSONObject scope = content.getJSONObject("scope");

        String vnfType = scope.getString(VNF_TYPE);
        String protocol = content.getString(DEVICE_PROTOCOL);
        String action = content.getString(ACTION);
        String actionLevel = content.getString(ACTION_LEVEL);
        String template = content.optString(TEMPLATE);

        try {
            updateIfNeeded(dbservice, context, vnfType, protocol, action, actionLevel, template);
        } catch (SvcLogicException e) {
            log.error("Error inserting record into protocolReference: " + e.toString());
            throw e;
        }
        log.info("End-->populateProtocolReference ");
    }

    private void updateIfNeeded(DBService dbservice, SvcLogicContext context, String vnfType, String protocol,
                                String action, String actionLevel, String template) throws SvcLogicException {
        boolean isUpdateNeeded = dbservice.isProtocolReferenceUpdateRequired(context, vnfType, protocol, action, actionLevel, template);
        if (isUpdateNeeded) {
            dbservice.updateProtocolReference(context, vnfType, protocol, action, actionLevel, template);
        } else {
            dbservice.insertProtocolReference(context, vnfType, protocol, action, actionLevel, template);
        }
    }
}
