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
import org.onap.sdnc.config.params.transformer.tosca.exceptions.ArtifactProcessorException;

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
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.TEMPLATE_ID;
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

    public void processArtifact(Map<String, String> inParams, SvcLogicContext ctx) throws Exception {
        if (inParams == null || inParams.isEmpty()) {
            return;
        }
        String postData = inParams.get("postData");
        if (postData == null || postData.isEmpty()) {
            return;
        }
        try {
            log.info("Received request for process Artifact with params: " + inParams.toString());
            JSONObject input = new JSONObject(postData).getJSONObject("input");
            String responsePrefix = inParams.get("response_prefix");
            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix + ".") : "";
            storeUpdateSdcArtifacts(input);
        } catch (Exception e) {
            log.error("Error when processing artifact", e);
            throw new ArtifactProcessorException("Error occurred while processing artifact", e);
        }
    }

    private boolean storeUpdateSdcArtifacts(JSONObject postDataJson) throws ArtifactHandlerInternalException {
        log.info("Starting processing of SDC Artifacs into Handler with Data : " + postDataJson.toString());
        try {
            JSONObject request_information =
                (JSONObject) postDataJson.get(REQUEST_INFORMATION);
            JSONObject document_information =
                (JSONObject) postDataJson.get(DOCUMENT_PARAMETERS);
            String artifact_name = document_information.getString(ARTIFACT_NAME);
            if (artifact_name != null) {
                updateStoreArtifacts(request_information, document_information);
                if (artifact_name.toLowerCase().startsWith(REFERENCE)) {
                    return storeReferenceData(request_information, document_information);
                } else if (artifact_name.toLowerCase().startsWith(PD)) {
                    return createDataForPD(request_information, document_information);
                }

            } else {
                throw new ArtifactHandlerInternalException("Missing Artifact Name for Request: "
                    + request_information.getString(REQUEST_ID));
            }
        } catch (Exception e) {
            log.error("Error while processing request with id: "
                + ((JSONObject) postDataJson.get(REQUEST_INFORMATION))
                .getString(REQUEST_ID), e);

            throw new ArtifactHandlerInternalException("Error while processing request with id: "
                + ((JSONObject) postDataJson.get(REQUEST_INFORMATION))
                .getString(REQUEST_ID), e);
        }
        return false;

    }

    private boolean createDataForPD(JSONObject request_information, JSONObject document_information)
        throws ArtifactHandlerInternalException {

        String fn = "ArtifactHandlerNode.createReferenceDataForPD";
        String artifact_name = document_information.getString(ARTIFACT_NAME);
        log.info(fn + "Received PD File Name: " + artifact_name + " and suffix lenght "
            + PD.length());
        try {

            String suffix = artifact_name.substring(PD.length());
            createArtifactRecords(request_information, document_information, suffix);
        } catch (Exception e) {
            log.error("Error while creating PD data records", e);
            throw new ArtifactHandlerInternalException("Error while creating PD data records", e);
        }
        return true;
    }

    private void createArtifactRecords(JSONObject request_information, JSONObject document_information, String suffix)
        throws ArtifactHandlerInternalException {

        try {
            log.info("Creating Tosca Records and storing into SDC Artifacs");
            String[] docs = {"Tosca", "Yang"};
            ArtifactHandlerProviderUtil ahpUtil = new ArtifactHandlerProviderUtil();
            String PDFileContents = document_information.getString(ARTIFACT_CONTENTS);

            // Tosca generation
            OutputStream toscaStream = new ByteArrayOutputStream();
            String toscaContents = null;
            ArtifactProcessorImpl toscaGenerator = new ArtifactProcessorImpl();
            toscaGenerator.generateArtifact(PDFileContents, toscaStream);
            toscaContents = toscaStream.toString();
            log.info("Generated Tosca File : " + toscaContents);

            String yangContents = "YANG generation is in Progress";
            String yangName = null;

            for (String doc : docs) {
                document_information.put(ARTIFACT_TYPE, doc.concat("Type"));
                document_information.put(ARTIFACT_DESRIPTION, doc.concat("Model"));
                if (doc.equals("Tosca")) {
                    document_information.put(ARTIFACT_CONTENTS,
                        ahpUtil.escapeSql(toscaContents));
                } else if (doc.equals("Yang")) {
                    document_information.put(ARTIFACT_CONTENTS,
                        ahpUtil.escapeSql(yangContents));
                }
                document_information.put(ARTIFACT_NAME, doc.concat(suffix));
                yangName = doc.concat(suffix);
                updateStoreArtifacts(request_information, document_information);
            }

            String artifactId = getArtifactID(yangName);
            OutputStream yangStream = new ByteArrayOutputStream();
            YANGGenerator yangGenerator = YANGGeneratorFactory.getYANGGenerator();
            yangGenerator.generateYANG(artifactId, toscaContents, yangStream);
            yangContents = yangStream.toString();

            if (yangContents != null) {
                updateYangContents(artifactId, ahpUtil.escapeSql(yangContents));
            }

        } catch (Exception e) {
            log.error("Error while creating artifact records", e);
            throw new ArtifactHandlerInternalException("Error while creating artifact records", e);
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

    protected boolean updateStoreArtifacts(JSONObject request_information, JSONObject document_information)
        throws SvcLogicException {
        log.info("UpdateStoreArtifactsStarted storing of SDC Artifacs ");

        SvcLogicContext context = new SvcLogicContext();
        DBService dbservice = DBService.initialise();
        ArtifactHandlerProviderUtil ahpUtil = new ArtifactHandlerProviderUtil();
        int intversion = 0;
        context.setAttribute("artifact_name",
            document_information.getString(ARTIFACT_NAME));
        String internal_version = dbservice.getInternalVersionNumber(context,
            document_information.getString(ARTIFACT_NAME), null);
        log.info("Internal Version number received from Database : " + internal_version);
        if (internal_version != null) {
            intversion = Integer.parseInt(internal_version);
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
        setAttribute(context, document_information::getString, ARTIFACT_TYPE);
        setAttribute(context, document_information::getString, ARTIFACT_VERSION);
        setAttribute(context, document_information::getString, ARTIFACT_DESRIPTION);
        setAttribute(context, document_information::getString, ARTIFACT_NAME);

        setAttribute(context, s -> ahpUtil.escapeSql(document_information.getString(s)), ARTIFACT_CONTENTS);

        dbservice.saveArtifacts(context, intversion);
        return true;

    }

    public boolean storeReferenceData(JSONObject request_information, JSONObject document_information)
        throws ArtifactHandlerInternalException {
        log.info("Started storing of SDC Artifacs into Handler");
        try {
            boolean updateRequired = false;
            boolean pdFile = false;
            String suffix = null;
            String categorySuffix = null;
            DBService dbservice = DBService.initialise();
            ArtifactHandlerProviderUtil ahpUtil = new ArtifactHandlerProviderUtil();
            String contentString =
                ahpUtil.escapeSql(document_information.getString(ARTIFACT_CONTENTS));
            String artifactName =
                ahpUtil.escapeSql(document_information.getString(ARTIFACT_NAME));
            String capabilityArtifactName =
                StringUtils.replace(artifactName, ARTIFACT_NAME_REFERENCE,
                    ARTIFACT_NAME_CAPABILITY);
            JSONObject capabilities = new JSONObject();
            JSONArray vnfActionList = new JSONArray();
            JSONArray vfModuleActionList = new JSONArray();
            JSONArray vnfcActionList = new JSONArray();
            JSONArray vmActionVnfcFunctionCodesList = new JSONArray();
            JSONArray vmActionList = new JSONArray();
            String vnfType = null;
            JSONObject contentObject = new JSONObject(contentString);
            JSONArray contentArray = contentObject.getJSONArray("reference_data");
            boolean storeCapabilityArtifact = true;
            for (int a = 0; a < contentArray.length(); a++) {
                pdFile = false;
                JSONObject content = (JSONObject) contentArray.get(a);
                log.info("contentString =" + content.toString());
                JSONObject scope = content.getJSONObject("scope");
                log.info("scope :" + scope);
                SvcLogicContext context = new SvcLogicContext();
                vnfType = scope.getString(VNF_TYPE);
                setAttribute(context, scope::getString, VNF_TYPE);
                setAttribute(context, content::getString, ACTION);
                String actionLevel = content.getString(ACTION_LEVEL);
                setAttribute(context, content::getString, ACTION_LEVEL);
                setAttribute(context, document_information::getString, ARTIFACT_TYPE);
                if ((null != actionLevel)
                    && actionLevel.equalsIgnoreCase(ACTION_LEVEL_VNFC)) {
                    vnfcActionList.put(content.getString(ACTION));
                }
                if (null != actionLevel
                    && actionLevel.equalsIgnoreCase(ACTION_LEVEL_VF_MODULE)) {
                    vfModuleActionList.put(content.getString(ACTION));
                }
                if (null != actionLevel && actionLevel.equalsIgnoreCase(ACTION_LEVEL_VNF)) {
                    vnfActionList.put(content.getString(ACTION));
                }
                if (null != actionLevel && actionLevel.equalsIgnoreCase(ACTION_LEVEL_VM)) {
                    if (content.has(VNFC_FUNCTION_CODE_LIST)
                        && !content.isNull(VNFC_FUNCTION_CODE_LIST) && content.get(
                        VNFC_FUNCTION_CODE_LIST) instanceof JSONArray) {
                        log.info("Found vnfc-function-code-list!!");
                        JSONArray vnfcList = content.getJSONArray(VNFC_FUNCTION_CODE_LIST);
                        JSONObject obj = new JSONObject();
                        obj.put(content.getString(ACTION), vnfcList);
                        vmActionVnfcFunctionCodesList.put(obj);
                    } else {
                        log.info("Not getting JSONArray for VNFC FUNCTION CODES");
                    }
                }
                if (scope.has(VNFC_TYPE)
                    && !scope.isNull(VNFC_TYPE)) {
                    String vnfcTypeScope = scope.getString(VNFC_TYPE);
                    if (StringUtils.isNotBlank(vnfcTypeScope)) {
                        setAttribute(context, scope::getString, VNFC_TYPE);
                        storeCapabilityArtifact = false;
                        log.info("No capability Artifact for this reference data as it is at VNFC level!!");
                    } else {
                        context.setAttribute(VNFC_TYPE, null);
                    }
                } else {
                    context.setAttribute(VNFC_TYPE, null);
                }
                if (content.has(DEVICE_PROTOCOL)) {
                    setAttribute(context, content::getString, DEVICE_PROTOCOL);
                }
                if (content.has(USER_NAME)) {
                    setAttribute(context, content::getString, USER_NAME);
                }
                if (content.has(PORT_NUMBER)) {
                    setAttribute(context, content::getString, PORT_NUMBER);
                }
                //context.setAttribute(ARTIFACT_TYPE, "");
                if (content.has("artifact-list") && content.get("artifact-list") instanceof JSONArray) {
                    JSONArray artifactLists = (JSONArray) content.get("artifact-list");
                    for (int i = 0; i < artifactLists.length(); i++) {
                        JSONObject artifact = (JSONObject) artifactLists.get(i);
                        log.info("artifact is " + artifact);
                        setAttribute(context, artifact::getString, ARTIFACT_NAME);
                        context.setAttribute(FILE_CATEGORY,
                            artifact.getString(ARTIFACT_TYPE));

                        if (artifact.getString(ARTIFACT_NAME) != null
                            && artifact.getString(ARTIFACT_NAME).toLowerCase()
                            .startsWith(PD)) {
                            suffix = artifact.getString(ARTIFACT_NAME)
                                .substring(PD.length());
                            categorySuffix = artifact.getString(ARTIFACT_TYPE)
                                .substring(PD.length());
                            pdFile = true;
                        }
                        log.info("Artifact-type = " + context.getAttribute(ARTIFACT_TYPE));
                        dbservice.processSdcReferences(context, dbservice.isArtifactUpdateRequired(context,
                            DB_SDC_REFERENCE));

                        cleanArtifactInstanceData(context);
                    }

                    if (pdFile) {
                        context.setAttribute(ARTIFACT_NAME, "Tosca".concat(suffix));
                        context.setAttribute(FILE_CATEGORY, TOSCA_MODEL);
                        dbservice.processSdcReferences(context,
                            dbservice.isArtifactUpdateRequired(context, DB_SDC_REFERENCE));
                        context.setAttribute(ARTIFACT_NAME, "Yang".concat(suffix));
                        context.setAttribute(FILE_CATEGORY, PARAMETER_YANG);
                        dbservice.processSdcReferences(context,
                            dbservice.isArtifactUpdateRequired(context, DB_SDC_REFERENCE));
                    }
                }

                processConfigTypeActions(content,dbservice,context);
                boolean saved=dbservice.processDeviceAuthentication(context, dbservice.isArtifactUpdateRequired(context,
                                                                DB_DEVICE_AUTHENTICATION));
                dbservice.performSftp(context,saved);
                populateProtocolReference(dbservice, content);
                context.setAttribute(VNFC_TYPE, null);
                if (content.has(VM)
                    && content.get(VM) instanceof JSONArray) {
                    processVmList(content, context, dbservice);
                }
            }
            if (storeCapabilityArtifact) {
                capabilities.put("vnf", vnfActionList);
                capabilities.put("vf-module", vfModuleActionList);
                capabilities.put("vnfc", vnfcActionList);
                capabilities.put("vm", vmActionVnfcFunctionCodesList);
                processAndStoreCapabilitiesArtifact(dbservice, document_information, capabilities,
                    capabilityArtifactName,
                    vnfType);
            }

        } catch (Exception e) {

            log.error("Error while storing reference data", e);
            throw new ArtifactHandlerInternalException("Error while storing reference data", e);
        }

        return true;
    }

    public void processConfigTypeActions(JSONObject content, DBService dbservice, SvcLogicContext context)throws Exception {
        if (content.getString(ACTION).equals("Configure")
                || content.getString(ACTION).equals("ConfigModify") || content.getString(ACTION).equals("ConfigScaleOut")) {
                if (content.has(DOWNLOAD_DG_REFERENCE)
                    && content.getString(DOWNLOAD_DG_REFERENCE).length() > 0) {
                    setAttribute(context, content::getString, DOWNLOAD_DG_REFERENCE);
                    dbservice.processDownloadDgReference(context,
                        dbservice.isArtifactUpdateRequired(context, DB_DOWNLOAD_DG_REFERENCE));
                }
                if (StringUtils.isBlank(context.getAttribute(DOWNLOAD_DG_REFERENCE))) {
                    context.setAttribute(DOWNLOAD_DG_REFERENCE,
                        dbservice.getDownLoadDGReference(context));
                }
                dbservice.processConfigActionDg(context, dbservice.isArtifactUpdateRequired(context,
                    DB_CONFIG_ACTION_DG));
                if (content.getString(ACTION).equals("Configure") || content.getString(ACTION).equals("ConfigScaleOut")) {
                    boolean isPresent=dbservice.isArtifactUpdateRequired(context,DB_DEVICE_INTERFACE_PROTOCOL);
                    if (content.getString(ACTION).equals("Configure") || (content.getString(ACTION).equals("ConfigScaleOut") && !isPresent))
                        dbservice.processDeviceInterfaceProtocol(context, isPresent);
                }

            }

    }

    public void processVmList(JSONObject content, SvcLogicContext context, DBService dbservice) throws Exception{
        JSONArray vmList = (JSONArray) content.get(VM);
        dbservice.cleanUpVnfcReferencesForVnf(context);
        for (int i = 0; i < vmList.length(); i++) {
            JSONObject vmInstance = (JSONObject) vmList.get(i);
            setAttribute(context, s -> String.valueOf(vmInstance.getInt(s)), VM_INSTANCE);
            log.info("VALUE = " + context.getAttribute(VM_INSTANCE));
            String templateId = vmInstance.optString(TEMPLATE_ID);
            if (StringUtils.isNotBlank(templateId)) {
                setAttribute(context, vmInstance::optString, TEMPLATE_ID);
            }
            if (vmInstance.get(VNFC) instanceof JSONArray) {
                JSONArray vnfcInstanceList = (JSONArray) vmInstance.get(VNFC);
                for (int k = 0; k < vnfcInstanceList.length(); k++) {
                    JSONObject vnfcInstance = (JSONObject) vnfcInstanceList.get(k);

                    setAttribute(context, s -> String.valueOf(vnfcInstance.getInt(s)), VNFC_INSTANCE);
                    setAttribute(context, vnfcInstance::getString, VNFC_TYPE);
                    setAttribute(context, vnfcInstance::getString, VNFC_FUNCTION_CODE);

                    if (vnfcInstance.has(IPADDRESS_V4_OAM_VIP)) {
                        setAttribute(context, vnfcInstance::getString, IPADDRESS_V4_OAM_VIP);
                    }
                    if (vnfcInstance.has(GROUP_NOTATION_TYPE)) {
                        setAttribute(context, vnfcInstance::getString, GROUP_NOTATION_TYPE);
                    }
                    if (vnfcInstance.has(GROUP_NOTATION_VALUE)) {
                        setAttribute(context, vnfcInstance::getString, GROUP_NOTATION_VALUE);
                    }
                    if (content.getString(ACTION).equals("Configure")
                            || content.getString(ACTION).equals("ConfigScaleOut")) {
                        dbservice.processVnfcReference(context, false);
                    }
                    cleanVnfcInstance(context);
                }
                context.setAttribute(VM_INSTANCE, null);
                context.setAttribute(TEMPLATE_ID, null);
            }
        }

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

    private void processAndStoreCapabilitiesArtifact(DBService dbservice, JSONObject document_information,
        JSONObject capabilities, String capabilityArtifactName, String vnfType)
        throws ArtifactHandlerInternalException {

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
            dbservice.processSdcReferences(context,
                dbservice.isArtifactUpdateRequired(context, DB_SDC_REFERENCE));
            int intversion = 0;

            String internal_version = dbservice.getInternalVersionNumber(context,
                context.getAttribute(ARTIFACT_NAME), null);
            log.info("Internal Version number received from Database : " + internal_version);
            if (internal_version != null) {
                intversion = Integer.parseInt(internal_version);
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
            log.error("Error saving capabilities artifact to DB", e);
            throw new ArtifactHandlerInternalException("Error saving capabilities artifact to DB", e);
        } finally {
            log.info("End-->processAndStoreCapabilitiesArtifact ");
        }

    }

    private void setAttribute(SvcLogicContext context, Function<String, String> value, String key) {
        context.setAttribute(key, value.apply(key));
    }

    private void populateProtocolReference(DBService dbservice, JSONObject content)
        throws ArtifactHandlerInternalException {
        log.info("Begin-->populateProtocolReference ");
        try {
            SvcLogicContext context = new SvcLogicContext();
            JSONObject scope = content.getJSONObject("scope");
            String vnfType = null, protocol = null, action = null, actionLevel = null, template = null;
            if (scope.has(VNF_TYPE) && !scope.isNull(VNF_TYPE)) {
                vnfType = scope.getString(VNF_TYPE);
            }
            if (content.has(DEVICE_PROTOCOL)) {
                protocol = content.getString(DEVICE_PROTOCOL);
            }
            if (content.has(ACTION)) {
                action = content.getString(ACTION);
            }
            if (content.has(ACTION_LEVEL)) {
                actionLevel = content.getString(ACTION_LEVEL);
            }
            if (content.has(TEMPLATE)
                && !content.isNull(TEMPLATE)) {
                template = content.getString(TEMPLATE);
            }
            boolean isUpdateNeeded = dbservice
                .isProtocolReferenceUpdateRequired(context, vnfType, protocol, action, actionLevel, template);
            if (isUpdateNeeded) {
                dbservice.updateProtocolReference(context, vnfType, protocol, action, actionLevel, template);
            } else {
                dbservice.insertProtocolReference(context, vnfType, protocol, action, actionLevel, template);
            }
        } catch (Exception e) {
            log.error("Error inserting record into protocolReference", e);
            throw new ArtifactHandlerInternalException("Error inserting record into protocolReference", e);
        } finally {
            log.info("End-->populateProtocolReference ");
        }
    }

}
