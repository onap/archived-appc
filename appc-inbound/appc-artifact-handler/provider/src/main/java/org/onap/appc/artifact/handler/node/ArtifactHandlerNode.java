/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
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

package org.onap.appc.artifact.handler.node;

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
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.REQUEST_ID;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.REQUEST_INFORMATION;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.RESOURCE_INSTANCE_NAME;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.RESOURCE_TYPE;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.RESOURCE_UUID;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.RESOURCE_VERSION;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.SERVICE_DESCRIPTION;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.SERVICE_NAME;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.SERVICE_UUID;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.TEMPLATE;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.TEMPLATE_ID;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.TOSCA_MODEL;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.USER_NAME;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.VM;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.VM_INSTANCE;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.VNFC;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.VNFC_FUNCTION_CODE;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.VNFC_FUNCTION_CODE_LIST;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.VNFC_INSTANCE;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.VNFC_TYPE;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.VNFC_TYPE_LIST;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.VNF_TYPE;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.URL;
import static org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants.OPENSTACK;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Map;
import java.util.function.Function;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.onap.appc.artifact.handler.dbservices.DBException;
import org.onap.appc.artifact.handler.dbservices.DBService;
import org.onap.appc.artifact.handler.utils.ArtifactHandlerProviderUtil;
import org.onap.appc.yang.YANGGenerator;
import org.onap.appc.yang.impl.YANGGeneratorFactory;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;
import org.onap.sdnc.config.params.transformer.tosca.ArtifactProcessorImpl;
import org.onap.sdnc.config.params.transformer.tosca.exceptions.ArtifactProcessorException;

public class ArtifactHandlerNode implements SvcLogicJavaPlugin {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(ArtifactHandlerNode.class);
    private static final String TOSCA_PARAM = "Tosca";
    private static final String YANG_PARAM = "Yang";
    private static final String ARTIFACT_LIST_PARAM = "artifact-list";
    private static final String CONFIGURE_PARAM = "Configure";
    private static final String CONFIG_SCALE_OUT_PARAM = "ConfigScaleOut";
    private static final String CONFIG_MODIFY_PARAM = "ConfigModify";

    public void processArtifact(Map<String, String> inParams, SvcLogicContext ctx) throws ArtifactProcessorException {

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
            storeUpdateSdcArtifacts(input);
        } catch (Exception e) {
            log.error("Error when processing artifact", e);
            throw new ArtifactProcessorException("Error occurred while processing artifact", e);
        }
    }

    private boolean storeUpdateSdcArtifacts(JSONObject postDataJson) throws ArtifactHandlerInternalException {
        log.info("Starting processing of SDC Artifacs into Handler with Data : " + postDataJson.toString());
        try {
            JSONObject requestInfo = (JSONObject) postDataJson.get(REQUEST_INFORMATION);
            JSONObject documentInfo = (JSONObject) postDataJson.get(DOCUMENT_PARAMETERS);
            String artifactName = documentInfo.getString(ARTIFACT_NAME);
            if (artifactName != null) {
                updateStoreArtifacts(requestInfo, documentInfo);
                if (artifactName.toLowerCase().startsWith(REFERENCE)) {
                    return storeReferenceData(requestInfo, documentInfo);
                } else if (artifactName.toLowerCase().startsWith(PD)) {
                    return createDataForPD(requestInfo, documentInfo);
                }

            } else {
                throw new ArtifactHandlerInternalException("Missing Artifact Name for Request: "
                    + requestInfo.getString(REQUEST_ID));
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error while processing request with id: "
                + ((JSONObject) postDataJson.get(REQUEST_INFORMATION)).getString(REQUEST_ID), e);

            throw new ArtifactHandlerInternalException("Error while processing request with id: "
                + ((JSONObject) postDataJson.get(REQUEST_INFORMATION)).getString(REQUEST_ID), e);
        }
        return false;
    }

    private boolean createDataForPD(JSONObject requestInfo, JSONObject documentInfo)
        throws ArtifactHandlerInternalException {

        String fn = "ArtifactHandlerNode.createReferenceDataForPD";
        String artifactName = documentInfo.getString(ARTIFACT_NAME);
        log.info(fn + "Received PD File Name: " + artifactName + " and suffix length "
            + PD.length());
        try {

            String suffix = artifactName.substring(PD.length());
            createArtifactRecords(requestInfo, documentInfo, suffix);
        } catch (Exception e) {
            log.error("Error while creating PD data records", e);
            throw new ArtifactHandlerInternalException("Error while creating PD data records", e);
        }
        return true;
    }

    private void createArtifactRecords(JSONObject requestInfo, JSONObject documentInfo, String suffix)
        throws ArtifactHandlerInternalException {

        try {
            log.info("Creating Tosca Records and storing into SDC Artifacs");
            String[] docs = {TOSCA_PARAM, YANG_PARAM};
            ArtifactHandlerProviderUtil ahpUtil = new ArtifactHandlerProviderUtil();
            String pdFileContents = documentInfo.getString(ARTIFACT_CONTENTS);

            // Tosca generation
            OutputStream toscaStream = new ByteArrayOutputStream();
            String toscaContents;
            ArtifactProcessorImpl toscaGenerator = getArtifactProcessorImpl();
            toscaGenerator.generateArtifact(pdFileContents, toscaStream);
            toscaContents = toscaStream.toString();
            log.info("Generated Tosca File : " + toscaContents);

            String yangContents = "YANG generation is in Progress";
            String yangName = null;

            for (String doc : docs) {
                documentInfo.put(ARTIFACT_TYPE, doc.concat("Type"));
                documentInfo.put(ARTIFACT_DESRIPTION, doc.concat("Model"));
                if (doc.equals(TOSCA_PARAM)) {
                    documentInfo.put(ARTIFACT_CONTENTS, ahpUtil.escapeSql(toscaContents));
                } else if (doc.equals(YANG_PARAM)) {
                    documentInfo.put(ARTIFACT_CONTENTS, ahpUtil.escapeSql(yangContents));
                }
                documentInfo.put(ARTIFACT_NAME, doc.concat(suffix));
                yangName = doc.concat(suffix);
                updateStoreArtifacts(requestInfo, documentInfo);
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

    protected boolean updateStoreArtifacts(JSONObject requestInfo, JSONObject documentInfo)
        throws SvcLogicException {
        log.info("UpdateStoreArtifactsStarted storing of SDC Artifacs ");
        SvcLogicContext context = new SvcLogicContext();
        DBService dbservice = DBService.initialise();
        ArtifactHandlerProviderUtil ahpUtil = new ArtifactHandlerProviderUtil();
        int intversion = 0;
        context.setAttribute("artifact_name",
            documentInfo.getString(ARTIFACT_NAME));
        String internalVersion = dbservice.getInternalVersionNumber(context,
            documentInfo.getString(ARTIFACT_NAME), null);
        log.info("Internal Version number received from Database : " + internalVersion);
        if (internalVersion != null) {
            intversion = Integer.parseInt(internalVersion);
            intversion++;
        }
        setAttribute(context, documentInfo::getString, SERVICE_UUID);
        setAttribute(context, documentInfo::getString, DISTRIBUTION_ID);
        setAttribute(context, documentInfo::getString, SERVICE_NAME);
        setAttribute(context, documentInfo::getString, SERVICE_DESCRIPTION);
        setAttribute(context, documentInfo::getString, RESOURCE_UUID);
        setAttribute(context, documentInfo::getString, RESOURCE_INSTANCE_NAME);
        setAttribute(context, documentInfo::getString, RESOURCE_VERSION);
        setAttribute(context, documentInfo::getString, RESOURCE_TYPE);
        setAttribute(context, documentInfo::getString, ARTIFACT_UUID);
        setAttribute(context, documentInfo::getString, ARTIFACT_TYPE);
        setAttribute(context, documentInfo::getString, ARTIFACT_VERSION);
        setAttribute(context, documentInfo::getString, ARTIFACT_DESRIPTION);
        setAttribute(context, documentInfo::getString, ARTIFACT_NAME);
        setAttribute(context, s -> ahpUtil.escapeSql(documentInfo.getString(s)), ARTIFACT_CONTENTS);

        dbservice.saveArtifacts(context, intversion);
        return true;
    }

    public boolean storeReferenceData(JSONObject requestInfo, JSONObject documentInfo)
        throws ArtifactHandlerInternalException {

        log.info("Started storing of SDC Artifacs into Handler");
        try {
            DBService dbservice = DBService.initialise();
            ArtifactHandlerProviderUtil ahpUtil = new ArtifactHandlerProviderUtil();
            String contentString =
                ahpUtil.escapeSql(documentInfo.getString(ARTIFACT_CONTENTS));
            String artifactName =
                ahpUtil.escapeSql(documentInfo.getString(ARTIFACT_NAME));
            String capabilityArtifactName =
                StringUtils.replace(artifactName, ARTIFACT_NAME_REFERENCE,
                    ARTIFACT_NAME_CAPABILITY);
            JSONObject capabilities = new JSONObject();
            JSONArray vnfActionList = new JSONArray();
            JSONArray vfModuleActionList = new JSONArray();
            JSONArray vnfcActionList = new JSONArray();
            JSONArray vmActionVnfcFunctionCodesList = new JSONArray();
            String vnfType = null;
            JSONObject contentObject = new JSONObject(contentString);
            JSONArray contentArray = contentObject.getJSONArray("reference_data");
            boolean storeCapabilityArtifact = true;
            for (int a = 0; a < contentArray.length(); a++) {
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
                setAttribute(context, documentInfo::getString, ARTIFACT_TYPE);
                processActionLists(content, actionLevel, vnfcActionList, vfModuleActionList, vnfActionList,
                    vmActionVnfcFunctionCodesList);
                JSONArray vnfcTypeList = setVnfcTypeInformation(scope, context);
                storeCapabilityArtifact = isCapabilityArtifactNeeded(context);
                if (content.has(DEVICE_PROTOCOL)) {
                    setAttribute(context, content::getString, DEVICE_PROTOCOL);
                }
                if (content.has(USER_NAME)) {
                    setAttribute(context, content::getString, USER_NAME);
                }
                if (content.has(PORT_NUMBER)) {
                    setAttribute(context, content::getString, PORT_NUMBER);
                }
                if (content.has(URL)) {
                    setAttribute(context, content::getString, URL);
                }
                processArtifactList(content, dbservice, context, vnfcTypeList);
                processConfigTypeActions(content, dbservice, context);
                dbservice.processDeviceAuthentication(context,
                    dbservice.isArtifactUpdateRequired(context, DB_DEVICE_AUTHENTICATION));

                String actionProtocol = tryGetProtocol(content);
                if (!StringUtils.equalsIgnoreCase(actionProtocol, OPENSTACK)) {
                     populateProtocolReference(dbservice, content);
                }

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
                processAndStoreCapabilitiesArtifact(dbservice, documentInfo, capabilities,
                    capabilityArtifactName,
                    vnfType);
            }

        } catch (Exception e) {

            log.error("Error while storing reference data", e);
            throw new ArtifactHandlerInternalException("Error while storing reference data", e);
        }

        return true;
    }

    public boolean isCapabilityArtifactNeeded(SvcLogicContext context) {
        String vnfcType = context.getAttribute(VNFC_TYPE);
        if (StringUtils.isNotBlank(vnfcType)) {
            log.info("No capability Artifact for this reference data as it is at VNFC level!!" );
            return false;
        }
        else {
            return true;
        }
    }

    public JSONArray setVnfcTypeInformation(JSONObject scope, SvcLogicContext context) {
        JSONArray vnfcTypeList = null;
        if (scope.has(VNFC_TYPE)
            && !scope.isNull(VNFC_TYPE)) {
            String vnfcTypeScope = scope.getString(VNFC_TYPE);
            if (StringUtils.isNotBlank(vnfcTypeScope)) {
                setAttribute(context, scope::getString, VNFC_TYPE);
                log.info("VNFC Type has been set for this reference artifact!!"+vnfcTypeScope);
            } else {
                context.setAttribute(VNFC_TYPE, null);
            }
        } else {
            context.setAttribute(VNFC_TYPE, null);
        }
        if (scope.has(VNFC_TYPE_LIST) && !scope.isNull(VNFC_TYPE_LIST)
            && scope.get(VNFC_TYPE_LIST) instanceof JSONArray) {
            vnfcTypeList = scope.getJSONArray(VNFC_TYPE_LIST);
            log.info("VNFC TYPE LIST found for this artifact!! "+ vnfcTypeList.toString());
        }
        return vnfcTypeList;
    }

    public void processActionLists(JSONObject content, String actionLevel, JSONArray vnfcActionList,
        JSONArray vfModuleActionList,
        JSONArray vnfActionList, JSONArray vmActionVnfcFunctionCodesList) {
        if (validateActionLevel(actionLevel, ACTION_LEVEL_VNFC)) {
            vnfcActionList.put(content.getString(ACTION));
        }
        if (validateActionLevel(actionLevel, ACTION_LEVEL_VF_MODULE)) {
            vfModuleActionList.put(content.getString(ACTION));
        }
        if (validateActionLevel(actionLevel, ACTION_LEVEL_VNF)) {
            vnfActionList.put(content.getString(ACTION));
        }
        if (validateActionLevel(actionLevel, ACTION_LEVEL_VM)) {
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

    }

    private boolean validateActionLevel(String actionLevel, String actionLevelVnfc) {
        return null != actionLevel && actionLevel.equalsIgnoreCase(actionLevelVnfc);
    }

    public void processArtifactList(JSONObject content, DBService dbservice, SvcLogicContext context, JSONArray vnfcTypeList)
        throws ArtifactHandlerInternalException {


        try {
            if (content.has(ARTIFACT_LIST_PARAM) && content.get(ARTIFACT_LIST_PARAM) instanceof JSONArray) {
                JSONArray artifactLists = (JSONArray) content.get(ARTIFACT_LIST_PARAM);
                JSONArray templateIdList = null;
                if (content.has("template-id-list") && null != content.get("template-id-list")
                        && content.get("template-id-list") instanceof JSONArray) {
                        templateIdList = content.getJSONArray("template-id-list");
                }
                doProcessArtifactList(dbservice, context, artifactLists, templateIdList, vnfcTypeList);

            }
        } catch (Exception e) {
            log.error("An error occurred when processing artifact list", e);
            throw new ArtifactHandlerInternalException(e);
        }
    }

    private void doProcessArtifactList(DBService dbservice, SvcLogicContext context, JSONArray artifactLists,
        JSONArray templateIdList, JSONArray vnfcTypeList)
        throws SvcLogicException, SQLException, ConfigurationException, DBException {
        boolean pdFile = false;
        int modelInd = 0,  vnfcRefInd = 0;
        for (int i = 0; i < artifactLists.length(); i++) {
            String suffix = null;
            String model = null;
            JSONObject artifact = (JSONObject) artifactLists.get(i);
            log.info("artifact is " + artifact);

            //Get Model details
            if (null != templateIdList && i>0 && i%2 == 0) {//Should this be changed to 3 to account for 3 artifacts
                modelInd++;
            }
            if (null != vnfcTypeList && i>0 && i%3 == 0) { 
            	//TDP 517180 - CD tool has made changes to send 3 artifacts instead of 2
                vnfcRefInd++;
            }
            setAttribute(context, artifact::getString, ARTIFACT_NAME);
            context.setAttribute(FILE_CATEGORY,
                artifact.getString(ARTIFACT_TYPE));

            if (artifact.getString(ARTIFACT_NAME) != null
                && artifact.getString(ARTIFACT_NAME).toLowerCase().startsWith(PD)) {

                suffix = artifact.getString(ARTIFACT_NAME).substring(PD.length());
                pdFile = true;
            }
            log.info("Artifact-type = " + context.getAttribute(FILE_CATEGORY));
            log.info("Artifact-name = " + context.getAttribute(ARTIFACT_NAME));

            if (null != templateIdList  && modelInd < templateIdList.length()) {
                model = templateIdList.getString(modelInd);
                log.info("Model is ::: "+model+"  ,modelInd = " + modelInd);
            }
            if (null != vnfcTypeList && vnfcRefInd < vnfcTypeList.length() ) {
                String vnfcType = vnfcTypeList.getString(vnfcRefInd);
                if (StringUtils.isNotBlank(vnfcType)) {
                    context.setAttribute(VNFC_TYPE, vnfcType);
                }
                log.info("Setting vnfc type from vnfc-type-list ::" + vnfcType);
            }
            if (StringUtils.isNotBlank(model)) {
                dbservice.processSdcReferences(context, dbservice.isArtifactUpdateRequired(context,
                    DB_SDC_REFERENCE, model), model);
            }
            else {
                dbservice.processSdcReferences(context, dbservice.isArtifactUpdateRequired(context,
                DB_SDC_REFERENCE));
            }

            cleanArtifactInstanceData(context);
            //Moving this into the for loop to account for mulitple artifact sets with pds
            if (pdFile) {
                log.info("Sending information related to pdfile Artifact");
                tryUpdateContext(dbservice, context, pdFile, suffix, model);
                pdFile = false;//set to false afterprocessing yang and Tosca
            }
        }

    }

    private void tryUpdateContext(DBService dbservice, SvcLogicContext context, boolean pdFile,
            String suffix, String model)
        throws SvcLogicException, SQLException, ConfigurationException, DBException {
        if (pdFile) {
            context.setAttribute(ARTIFACT_NAME, "Tosca".concat(suffix));
            context.setAttribute(FILE_CATEGORY, TOSCA_MODEL);
            dbservice.processSdcReferences(context,
                dbservice.isArtifactUpdateRequired(context, DB_SDC_REFERENCE, model), model);
            context.setAttribute(ARTIFACT_NAME, "Yang".concat(suffix));
            context.setAttribute(FILE_CATEGORY, PARAMETER_YANG);
            dbservice.processSdcReferences(context,
                dbservice.isArtifactUpdateRequired(context, DB_SDC_REFERENCE, model), model);
        }
    }

    public void processConfigTypeActions(JSONObject content, DBService dbservice, SvcLogicContext context)
        throws ArtifactHandlerInternalException {

        try {
            if (contentsActionEquals(content, CONFIGURE_PARAM)
                || contentsActionEquals(content, CONFIG_MODIFY_PARAM)
                || contentsActionEquals(content, CONFIG_SCALE_OUT_PARAM)) {

                if (content.has(DOWNLOAD_DG_REFERENCE) && content.getString(DOWNLOAD_DG_REFERENCE).length() > 0) {

                    setAttribute(context, content::getString, DOWNLOAD_DG_REFERENCE);
                    dbservice.processDownloadDgReference(context,
                        dbservice.isArtifactUpdateRequired(context, DB_DOWNLOAD_DG_REFERENCE));
                }
                if (StringUtils.isBlank(context.getAttribute(DOWNLOAD_DG_REFERENCE))) {
                    context.setAttribute(DOWNLOAD_DG_REFERENCE,
                        dbservice.getDownLoadDGReference(context));
                }
                dbservice
                    .processConfigActionDg(context, dbservice.isArtifactUpdateRequired(context, DB_CONFIG_ACTION_DG));

                tryProcessInterfaceProtocol(content, dbservice, context);
            }
        } catch (Exception e) {
            log.error("An error occurred when processing config type actions", e);
            throw new ArtifactHandlerInternalException(e);
        }
    }

    private void tryProcessInterfaceProtocol(JSONObject content, DBService dbservice, SvcLogicContext context)
        throws SvcLogicException, SQLException, ConfigurationException, DBException {

        if (contentsActionEquals(content, CONFIGURE_PARAM) || contentsActionEquals(content, CONFIG_SCALE_OUT_PARAM)) {
            boolean isUpdateRequired = dbservice.isArtifactUpdateRequired(context, DB_DEVICE_INTERFACE_PROTOCOL);
            if (contentsActionEquals(content, CONFIGURE_PARAM)
                || (contentsActionEquals(content, CONFIG_SCALE_OUT_PARAM)
                && !isUpdateRequired)) {

                dbservice.processDeviceInterfaceProtocol(context, isUpdateRequired);
            }
        }
    }

    private boolean contentsActionEquals(JSONObject content, String action) {
        return content.getString(ACTION).equals(action);
    }

    public void processVmList(JSONObject content, SvcLogicContext context, DBService dbservice)
        throws SvcLogicException {
        JSONArray vmList = (JSONArray) content.get(VM);
        dbservice.cleanUpVnfcReferencesForVnf(context);
        for (int i = 0; i < vmList.length(); i++) {
            JSONObject vmInstance = (JSONObject) vmList.get(i);
            setAttribute(context, s -> String.valueOf(vmInstance.getInt(s)), VM_INSTANCE);
            log.info("VALUE = " + context.getAttribute(VM_INSTANCE));
            String templateId = vmInstance.optString(TEMPLATE_ID);
            trySetContext(context, vmInstance, templateId);
            if (vmInstance.get(VNFC) instanceof JSONArray) {
                JSONArray vnfcInstanceList = (JSONArray) vmInstance.get(VNFC);
                for (int k = 0; k < vnfcInstanceList.length(); k++) {
                    JSONObject vnfcInstance = (JSONObject) vnfcInstanceList.get(k);

                    setAttribute(context, s -> String.valueOf(vnfcInstance.getInt(s)), VNFC_INSTANCE);
                    setAttribute(context, vnfcInstance::getString, VNFC_TYPE);
                    setAttribute(context, vnfcInstance::getString, VNFC_FUNCTION_CODE);
                    resolveContext(context, vnfcInstance);
                    tryProcessVnfcReference(content, context, dbservice);
                    cleanVnfcInstance(context);
                }
                context.setAttribute(VM_INSTANCE, null);
                context.setAttribute(TEMPLATE_ID, null);
            }
        }
    }

    private void trySetContext(SvcLogicContext context, JSONObject vmInstance, String templateId) {
        if (StringUtils.isNotBlank(templateId)) {
            setAttribute(context, vmInstance::optString, TEMPLATE_ID);
        }
    }

    private void tryProcessVnfcReference(JSONObject content, SvcLogicContext context, DBService dbservice)
        throws SvcLogicException {
        if (content.getString(ACTION).equals(CONFIGURE_PARAM)
            || content.getString(ACTION).equals(CONFIG_SCALE_OUT_PARAM)) {

            dbservice.processVnfcReference(context, false);
        }
    }

    private void resolveContext(SvcLogicContext context, JSONObject vnfcInstance) {
        if (vnfcInstance.has(IPADDRESS_V4_OAM_VIP)) {
            setAttribute(context, vnfcInstance::getString, IPADDRESS_V4_OAM_VIP);
        }
        if (vnfcInstance.has(GROUP_NOTATION_TYPE)) {
            setAttribute(context, vnfcInstance::getString, GROUP_NOTATION_TYPE);
        }
        if (vnfcInstance.has(GROUP_NOTATION_VALUE)) {
            setAttribute(context, vnfcInstance::getString, GROUP_NOTATION_VALUE);
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

    private void processAndStoreCapabilitiesArtifact(DBService dbService, JSONObject documentInfo,
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
            dbService.processSdcReferences(context, dbService.isArtifactUpdateRequired(context, DB_SDC_REFERENCE));
            int intVersion = 0;
            String internalVersion = dbService.getInternalVersionNumber(context,
                context.getAttribute(ARTIFACT_NAME), null);
            log.info("Internal Version number received from Database : " + internalVersion);
            if (internalVersion != null) {
                intVersion = Integer.parseInt(internalVersion) + 1;
            }
            setAttribute(context, documentInfo::getString, SERVICE_UUID);
            setAttribute(context, documentInfo::getString, DISTRIBUTION_ID);
            setAttribute(context, documentInfo::getString, SERVICE_NAME);
            setAttribute(context, documentInfo::getString, SERVICE_DESCRIPTION);
            setAttribute(context, documentInfo::getString, RESOURCE_UUID);
            setAttribute(context, documentInfo::getString, RESOURCE_INSTANCE_NAME);
            setAttribute(context, documentInfo::getString, RESOURCE_VERSION);
            setAttribute(context, documentInfo::getString, RESOURCE_TYPE);
            setAttribute(context, documentInfo::getString, ARTIFACT_UUID);
            setAttribute(context, documentInfo::getString, ARTIFACT_VERSION);
            setAttribute(context, documentInfo::getString, ARTIFACT_DESRIPTION);
            dbService.saveArtifacts(context, intVersion);
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
            String vnfType = tryGetVnfType(scope);
            String protocol = tryGetProtocol(content);
            String action = tryGetAction(content);
            String actionLevel = tryGetActionLevel(content);
            String template = tryGetTemplate(content);

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

    private String tryGetVnfType(JSONObject scope) {
        if (scope.has(VNF_TYPE) && !scope.isNull(VNF_TYPE)) {
            return scope.getString(VNF_TYPE);
        }
        return null;
    }

    private String tryGetProtocol(JSONObject content) {
        if (content.has(DEVICE_PROTOCOL)) {
            return content.getString(DEVICE_PROTOCOL);
        }
        return null;
    }

    private String tryGetAction(JSONObject content) {
        if (content.has(ACTION)) {
            return content.getString(ACTION);
        }
        return null;
    }

    private String tryGetActionLevel(JSONObject content) {
        if (content.has(ACTION_LEVEL)) {
            return content.getString(ACTION_LEVEL);
        }
        return null;
    }

    private String tryGetTemplate(JSONObject content) {
        if (content.has(TEMPLATE) && !content.isNull(TEMPLATE)) {
            return content.getString(TEMPLATE);
        }
        return null;
    }

    protected ArtifactProcessorImpl getArtifactProcessorImpl() {
        return new ArtifactProcessorImpl();
    }
}
