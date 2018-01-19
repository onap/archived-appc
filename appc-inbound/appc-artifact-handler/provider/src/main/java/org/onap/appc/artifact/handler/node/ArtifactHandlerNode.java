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
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.onap.appc.artifact.handler.dbservices.DBService;
import org.onap.appc.artifact.handler.utils.ArtifactHandlerProviderUtil;
import org.onap.appc.yang.YANGGenerator;
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

public class ArtifactHandlerNode implements SvcLogicJavaPlugin {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(ArtifactHandlerNode.class);

    private static final String REFERENCE_DATA = "reference_data";
    private static final String ARTIFACT_LIST = "artifact-list";
    private static final String CONFIG_MODIFY = "ConfigModify";
    private static final String CAPABILITIES = "capabilities";
    private static final String CONFIGURE = "Configure";
    private static final String POST_DATA = "postData";
    private static final String TOSCA_DOC = "Tosca";
    private static final String YANG_DOC = "Yang";
    private static final String SCOPE = "scope";

    private static final int INVALID_VERSION = 0;

    public void processArtifact(Map<String, String> inParams, SvcLogicContext ctx) throws Exception {
        String responsePrefix = inParams.get("response_prefix");
        try {
            if (inParams.get("postData") != null) {
                log.info("Received request for process Artifact with params: " + inParams.toString());
                String postData = inParams.get(POST_DATA);
                JSONObject input = new JSONObject(postData).getJSONObject("input");
                responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix + ".") : "";
                storeUpdateSdcArtifacts(input);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private boolean storeUpdateSdcArtifacts(JSONObject postDataJson) throws Exception {
        log.info("Starting processing of SDC Artifacs into Handler with Data : " + postDataJson.toString());
        boolean result = false;
        try {
            JSONObject requestInformation = postDataJson.getJSONObject(REQUEST_INFORMATION);
            JSONObject documentInformation = postDataJson.getJSONObject(DOCUMENT_PARAMETERS);
            String artifact_name = documentInformation.getString(ARTIFACT_NAME);
            updateStoreArtifacts(requestInformation, documentInformation);

            if (artifact_name.toLowerCase().startsWith(REFERENCE)) {
                result = storeReferenceData(requestInformation, documentInformation);
            } else if (artifact_name.toLowerCase().startsWith(PD)) {
                result = createDataForPD(requestInformation, documentInformation);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error while processing Request ID : "
                + postDataJson.getJSONObject(REQUEST_INFORMATION)
                .getString(REQUEST_ID)
                + e.getMessage());
        }
        return result;
    }

    private boolean createDataForPD(JSONObject requestInformation, JSONObject documentInformation) throws Exception {

        String fn = "ArtifactHandlerNode.createReferenceDataForPD";
        String artifactName = documentInformation.getString(ARTIFACT_NAME);
        log.info(fn + "Received PD File Name: " + artifactName + " and suffix length " + PD.length());
        try {
            String suffix = artifactName.substring(PD.length());
            createArtifactRecords(requestInformation, documentInformation, suffix);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error while creating PD data records " + e.getMessage());
        }
        return true;
    }

    private void createArtifactRecords(JSONObject requestInformation, JSONObject documentInformation, String suffix)
            throws Exception {

        log.info("Creating Tosca Records and storing into SDC Artifacs");
        String[] docs = {TOSCA_DOC, YANG_DOC};
        ArtifactHandlerProviderUtil ahpUtil = new ArtifactHandlerProviderUtil();
        String PDFileContents = documentInformation.getString(ARTIFACT_CONTENTS);

        // Tosca generation
        OutputStream toscaStream = new ByteArrayOutputStream();
        ArtifactProcessorImpl toscaGenerator = new ArtifactProcessorImpl();
        toscaGenerator.generateArtifact(PDFileContents, toscaStream);
        String toscaContents = toscaStream.toString();
        log.info("Generated Tosca File : " + toscaContents);

        String yangContents = "YANG generation is in Progress";
        String yangName = null;

        for (String doc : docs) {
            documentInformation.put(ARTIFACT_TYPE, doc.concat("Type"));
            documentInformation.put(ARTIFACT_DESRIPTION, doc.concat("Model"));
            if (doc.equals(TOSCA_DOC)) {
                documentInformation.put(ARTIFACT_CONTENTS, ahpUtil.escapeSql(toscaContents));
            }
            else if (doc.equals(YANG_DOC)) {
                documentInformation.put(ARTIFACT_CONTENTS, ahpUtil.escapeSql(yangContents));
            }
            documentInformation.put(ARTIFACT_NAME, doc.concat(suffix));
            yangName = doc.concat(suffix);
            updateStoreArtifacts(requestInformation, documentInformation);
        }

        String artifactId = getArtifactID(yangName);
        OutputStream yangStream = new ByteArrayOutputStream();
        YANGGenerator yangGenerator = YANGGeneratorFactory.getYANGGenerator();
        yangGenerator.generateYANG(artifactId, toscaContents, yangStream);
        yangContents = yangStream.toString();

        if (yangContents != null) {
            updateYangContents(artifactId, ahpUtil.escapeSql(yangContents));
        }

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

    protected boolean updateStoreArtifacts(JSONObject requestInformation, JSONObject documentInformation)
            throws Exception {
        log.info("UpdateStoreArtifactsStarted storing of SDC Artifacts ");

        SvcLogicContext context = new SvcLogicContext();
        DBService dbservice = DBService.initialise();
        ArtifactHandlerProviderUtil ahpUtil = new ArtifactHandlerProviderUtil();
        int intversion = INVALID_VERSION;
        context.setAttribute("artifact_name", documentInformation.getString(ARTIFACT_NAME));
        String internal_version = dbservice.getInternalVersionNumber(context, documentInformation.getString(ARTIFACT_NAME), null);
        log.info("Internal Version number received from Database : " + internal_version);
        if (internal_version != null) {
            intversion = Integer.parseInt(internal_version);
            intversion++;
        }

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

        dbservice.saveArtifacts(context, intversion);
        return true;

    }

    public boolean storeReferenceData(JSONObject requestInformation, JSONObject documentInformation) throws Exception {
        log.info("Started storing of SDC Artifacs into Handler");
        try {
            boolean updateRequired = false;
            boolean pdFile = false;
            String suffix = null;
            String categorySuffix = null;
            DBService dbservice = DBService.initialise();
            ArtifactHandlerProviderUtil ahpUtil = new ArtifactHandlerProviderUtil();
            String contentString = ahpUtil.escapeSql(documentInformation.getString(ARTIFACT_CONTENTS));
            String artifactName = ahpUtil.escapeSql(documentInformation.getString(ARTIFACT_NAME));
            String capabilityArtifactName = StringUtils.replace(artifactName, ARTIFACT_NAME_REFERENCE, ARTIFACT_NAME_CAPABILITY);
            JSONObject capabilities = new JSONObject();
            JSONArray vnfActionList = new JSONArray();
            JSONArray vfModuleActionList = new JSONArray();
            JSONArray vnfcActionList = new JSONArray();
            JSONArray vmActionVnfcFunctionCodesList = new JSONArray();
            JSONArray vmActionList = new JSONArray();
            String vnfType = null;
            JSONObject contentObject = new JSONObject(contentString);
            JSONArray contentArray = contentObject.getJSONArray(REFERENCE_DATA);
            boolean storeCapabilityArtifact = true;
            for (int a = 0; a < contentArray.length(); a++) {
                pdFile = false;
                JSONObject content = contentArray.getJSONObject(a);
                log.info("contentString =" + content.toString());
                JSONObject scope = content.getJSONObject(SCOPE);
                log.info("scope :" + scope);
                SvcLogicContext context = new SvcLogicContext();
                vnfType = scope.getString(VNF_TYPE);
                setAttribute(context, scope::getString, VNF_TYPE);
                setAttribute(context, content::getString, ACTION);
                String actionLevel = content.getString(ACTION_LEVEL);
                setAttribute(context, content::getString, ACTION_LEVEL);
                setAttribute(context, documentInformation::getString, ARTIFACT_TYPE);
                if (null != actionLevel && actionLevel.equalsIgnoreCase(ACTION_LEVEL_VNFC)) {
                    vnfcActionList.put(content.getString(ACTION));
                }
                if (null != actionLevel && actionLevel.equalsIgnoreCase(ACTION_LEVEL_VF_MODULE)) {
                    vfModuleActionList.put(content.getString(ACTION));
                }
                if (null != actionLevel && actionLevel.equalsIgnoreCase(ACTION_LEVEL_VNF)) {
                    vnfActionList.put(content.getString(ACTION));
                }
                if (null != actionLevel && actionLevel.equalsIgnoreCase(ACTION_LEVEL_VM)) {
                if (content.has(VNFC_FUNCTION_CODE_LIST)
                    && !content.isNull(VNFC_FUNCTION_CODE_LIST)
                    && content.get(VNFC_FUNCTION_CODE_LIST) instanceof JSONArray) {
                        log.info("Found vnfc-function-code-list!!");
                        JSONArray vnfcList = content.getJSONArray(VNFC_FUNCTION_CODE_LIST);
                        JSONObject obj = new JSONObject();
                        obj.put(content.getString(ACTION), vnfcList);
                        vmActionVnfcFunctionCodesList.put(obj);
                    } else {
                        log.info("Not getting JSONArray for VNFC FUNCTION CODES");
                    }
                }
                if (scope.has(VNFC_TYPE) && !scope.isNull(VNFC_TYPE)) {
                    String vnfcTypeScope = scope.getString(VNFC_TYPE);
                    if (StringUtils.isNotBlank(vnfcTypeScope)) {
                        setAttribute(context, scope::getString, VNFC_TYPE);
                        storeCapabilityArtifact = false;
                        log.info("No capability Artifact for this reference data as it is at VNFC level!!");
                    }
                    else {
                        context.setAttribute(VNFC_TYPE, null);
                    }
                }
                else {
                    context.setAttribute(VNFC_TYPE, null);
                }

                if (content.has(DEVICE_PROTOCOL))
                    setAttribute(context, content::getString, DEVICE_PROTOCOL);
                if (content.has(USER_NAME))
                    setAttribute(context, content::getString, USER_NAME);
                if (content.has(PORT_NUMBER))
                    setAttribute(context, content::getString, PORT_NUMBER);

                if (content.has(ARTIFACT_LIST)) {
                    JSONArray artifactLists = content.getJSONArray(ARTIFACT_LIST);
                    for (int i = 0; i < artifactLists.length(); i++) {
                        JSONObject artifact = artifactLists.getJSONObject(i);
                        log.info("artifact is " + artifact);
                        setAttribute(context, artifact::getString, ARTIFACT_NAME);
                        context.setAttribute(FILE_CATEGORY, artifact.getString(ARTIFACT_TYPE));

                        if (artifact.getString(ARTIFACT_NAME).toLowerCase().startsWith(PD)) {
                            suffix = artifact.getString(ARTIFACT_NAME).substring(PD.length());
                            categorySuffix = artifact.getString(ARTIFACT_TYPE).substring(PD.length());
                            pdFile = true;
                        }
                        log.info("Artifact-type = " + context.getAttribute(ARTIFACT_TYPE));
                        dbservice.processSdcReferences(context, dbservice.isArtifactUpdateRequired(context, DB_SDC_REFERENCE));

                        cleanArtifactInstanceData(context);
                    }

                    if (pdFile) {
                        context.setAttribute(ARTIFACT_NAME, TOSCA_DOC.concat(suffix));
                        context.setAttribute(FILE_CATEGORY, TOSCA_MODEL);
                        dbservice.processSdcReferences(context, dbservice.isArtifactUpdateRequired(context, DB_SDC_REFERENCE));
                        context.setAttribute(ARTIFACT_NAME, YANG_DOC.concat(suffix));
                        context.setAttribute(FILE_CATEGORY, PARAMETER_YANG);
                        dbservice.processSdcReferences(context, dbservice.isArtifactUpdateRequired(context, DB_SDC_REFERENCE));
                    }
                }
                if (content.getString(ACTION).equals(CONFIGURE) || content.getString(ACTION).equals(CONFIG_MODIFY)) {
                    if (content.has(DOWNLOAD_DG_REFERENCE) && content.getString(DOWNLOAD_DG_REFERENCE).length() > 0) {
                        setAttribute(context, content::getString, DOWNLOAD_DG_REFERENCE);
                        dbservice.processDownloadDgReference(context, dbservice.isArtifactUpdateRequired(context, DB_DOWNLOAD_DG_REFERENCE));
                    }
                    if (StringUtils.isBlank(context.getAttribute(DOWNLOAD_DG_REFERENCE))) {
                        context.setAttribute(DOWNLOAD_DG_REFERENCE, dbservice.getDownLoadDGReference(context));
                    }
                    dbservice.processConfigActionDg(context, dbservice.isArtifactUpdateRequired(context, DB_CONFIG_ACTION_DG));
                    if (content.getString(ACTION).equals(CONFIGURE)) {
                        dbservice.processDeviceInterfaceProtocol(context, dbservice.isArtifactUpdateRequired(context, DB_DEVICE_INTERFACE_PROTOCOL));
                    }

                }
                dbservice.processDeviceAuthentication(context, dbservice.isArtifactUpdateRequired(context, DB_DEVICE_AUTHENTICATION));

                populateProtocolReference(dbservice, content);

                context.setAttribute(VNFC_TYPE, null);

                if (content.has(VM) && content.get(VM) instanceof JSONArray) {
                    JSONArray vmList = (JSONArray) content.get(VM);
                    dbservice.cleanUpVnfcReferencesForVnf(context);
                    for (int i = 0; i < vmList.length(); i++) {
                        JSONObject vmInstance = vmList.getJSONObject(i);
                        setAttribute(context, s -> String.valueOf(vmInstance.getInt(s)), VM_INSTANCE);
                        log.info("VALUE = " + context.getAttribute(VM_INSTANCE));
                        if (vmInstance.get(VNFC) instanceof JSONArray) {
                            JSONArray vnfcInstanceList = vmInstance.getJSONArray(VNFC);
                            for (int k = 0; k < vnfcInstanceList.length(); k++) {
                                JSONObject vnfcInstance = vnfcInstanceList.getJSONObject(k);

                                setAttribute(context, s -> String.valueOf(vnfcInstance.getInt(s)), VNFC_INSTANCE);
                                setAttribute(context, vnfcInstance::getString, VNFC_TYPE);
                                setAttribute(context, vnfcInstance::getString, VNFC_FUNCTION_CODE);

                                if (vnfcInstance.has(IPADDRESS_V4_OAM_VIP))
                                    setAttribute(context, vnfcInstance::getString, IPADDRESS_V4_OAM_VIP);
                                if (vnfcInstance.has(GROUP_NOTATION_TYPE))
                                    setAttribute(context, vnfcInstance::getString, GROUP_NOTATION_TYPE);
                                if (vnfcInstance.has(GROUP_NOTATION_VALUE))
                                    setAttribute(context, vnfcInstance::getString, GROUP_NOTATION_VALUE);
                                if (content.getString(ACTION).equals(CONFIGURE)) {
                                    dbservice.processVnfcReference(context,false);
                                }
                                cleanVnfcInstance(context);
                            }
                            context.setAttribute(VM_INSTANCE, null);
                        }
                    }
                }
            }
            if (storeCapabilityArtifact) {
                capabilities.put("vnf", vnfActionList);
                capabilities.put("vf-module", vfModuleActionList);
                capabilities.put("vnfc", vnfcActionList);
                capabilities.put("vm", vmActionVnfcFunctionCodesList);
                processAndStoreCapablitiesArtifact(dbservice, documentInformation, capabilities, capabilityArtifactName, vnfType);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error While Storing :  " + e.getMessage());
        }

        return true;
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

    private void processAndStoreCapablitiesArtifact(DBService dbservice, JSONObject document_information,
            JSONObject capabilities, String capabilityArtifactName, String vnfType) throws Exception {

        log.info("Begin-->processAndStoreCapabilitiesArtifact ");

        try {
            JSONObject newCapabilitiesObject = new JSONObject();
            newCapabilitiesObject.put(CAPABILITIES, capabilities);
            SvcLogicContext context = new SvcLogicContext();
            context.setAttribute(ARTIFACT_NAME, capabilityArtifactName);
            context.setAttribute(FILE_CATEGORY, CAPABILITY);
            context.setAttribute(ACTION, null);
            context.setAttribute(VNFC_TYPE, null);
            context.setAttribute(ARTIFACT_TYPE, null);
            context.setAttribute(VNF_TYPE, vnfType);
            context.setAttribute(ARTIFACT_CONTENTS, newCapabilitiesObject.toString());
            dbservice.processSdcReferences(context, dbservice.isArtifactUpdateRequired(context, DB_SDC_REFERENCE));
            int intversion = INVALID_VERSION;

            String internalVersion = dbservice.getInternalVersionNumber(context, context.getAttribute(ARTIFACT_NAME), null);
            log.info("Internal Version number received from Database : " + internalVersion);
            if (internalVersion != null) {
                intversion = Integer.parseInt(internalVersion);
                intversion++;
            }

            setAttribute(context, document_information::getString, SERVICE_UUID);
            setAttribute(context, document_information::getString, DISTRIBUTION_ID);
            setAttribute(context, document_information::getString, SERVICE_NAME);
            setAttribute(context, document_information::getString, SERVICE_DESCRIPTION);
            setAttribute(context, document_information::getString, RESOURCE_UUID);
            setAttribute(context, document_information::getString, RESOURCE_INSTANCE_NAME);
            setAttribute(context, document_information::getString, RESOURCE_VERSION);
            setAttribute(context, document_information::getString, RESOURCE_TYPE);
            setAttribute(context, document_information::getString, ARTIFACT_UUID);
            setAttribute(context, document_information::getString, ARTIFACT_VERSION);
            setAttribute(context, document_information::getString, ARTIFACT_DESRIPTION);

            dbservice.saveArtifacts(context, intversion);
        } catch (Exception e) {
            log.error("Error saving capabilities artifact to DB: " + e.toString());
            throw e;
        } finally {
            log.info("End-->processAndStoreCapabilitiesArtifact ");
        }

    }

    private void setAttribute(SvcLogicContext context, Function<String, String> value, String key) {
        context.setAttribute(key, value.apply(key));
    }

    private void populateProtocolReference(DBService dbservice, JSONObject content) throws Exception {
        log.info("Begin-->populateProtocolReference ");
        try {
            SvcLogicContext context = new SvcLogicContext();
            JSONObject scope = content.getJSONObject(SCOPE);
            String vnfType = null;
            String protocol = null;
            String action = null;
            String actionLevel = null;
            String template = null;

            if (scope.has(VNF_TYPE) && !scope.isNull(VNF_TYPE))
                vnfType = scope.getString(VNF_TYPE);
            if (content.has(DEVICE_PROTOCOL))
                protocol = content.getString(DEVICE_PROTOCOL);
            if (content.has(ACTION))
                action = content.getString(ACTION);
            if (content.has(ACTION_LEVEL))
                actionLevel = content.getString(ACTION_LEVEL);
            if (content.has(TEMPLATE) && !content.isNull(TEMPLATE))
                template = content.getString(TEMPLATE);

            boolean isUpdateNeeded=dbservice.isProtocolReferenceUpdateRequired(context, vnfType, protocol, action, actionLevel, template);
            if (isUpdateNeeded) {
                dbservice.updateProtocolReference(context, vnfType, protocol, action, actionLevel, template);
            }
            else {
                dbservice.insertProtocolReference(context, vnfType,protocol,action,actionLevel,template);
            }
        } catch (Exception e) {
            log.error("Error inserting record into protocolReference: " + e.toString());
            throw e;
        } finally {
            log.info("End-->populateProtocolReference ");
        }
    }

}
