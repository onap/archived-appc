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

package org.openecomp.appc.artifact.handler.node;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openecomp.appc.artifact.handler.dbservices.DBService;
import org.openecomp.appc.artifact.handler.utils.ArtifactHandlerProviderUtil;
import org.openecomp.appc.artifact.handler.utils.SdcArtifactHandlerConstants;
import org.openecomp.appc.yang.YANGGenerator;
import org.openecomp.appc.yang.impl.YANGGeneratorFactory;
import org.openecomp.sdnc.config.params.transformer.tosca.ArtifactProcessorImpl;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;



public class ArtifactHandlerNode implements SvcLogicJavaPlugin {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(ArtifactHandlerNode.class);
    public void processArtifact(Map<String, String> inParams, SvcLogicContext ctx) throws Exception 
    {
        String responsePrefix = inParams.get("response_prefix");        
        try{
            if(inParams != null && !inParams.isEmpty() && inParams.get("postData") !=null ){
                log.info("Received request for process Artifact with params: " + inParams.toString());                        
                String postData = inParams.get("postData");
                JSONObject input = new JSONObject(postData).getJSONObject("input");
                responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix+".") : "";
                storeUpdateSdcArtifacts(input);
            }
        }
        catch(Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    private boolean storeUpdateSdcArtifacts(JSONObject postDataJson) throws Exception {
        log.info("Starting processing of SDC Artifacs into Handler with Data : " + postDataJson.toString());
        try{
            JSONObject request_information = (JSONObject)postDataJson.get(SdcArtifactHandlerConstants.REQUEST_INFORMATION);
            JSONObject document_information =(JSONObject)postDataJson.get(SdcArtifactHandlerConstants.DOCUMENT_PARAMETERS);
            String artifact_name = document_information.getString(SdcArtifactHandlerConstants.ARTIFACT_NAME);
            if(artifact_name !=null){
                updateStoreArtifacts(request_information, document_information );
                if(artifact_name.toLowerCase().startsWith(SdcArtifactHandlerConstants.REFERENCE))
                    return storeReferenceData(request_information, document_information );
                else if (artifact_name.toLowerCase().startsWith(SdcArtifactHandlerConstants.PD))
                    return createDataForPD(request_information, document_information );

            }
            else
                throw new Exception("Missing Artifact Name for Request : "  + request_information.getString(SdcArtifactHandlerConstants.REQUETS_ID));
        }
        catch(Exception e){
            e.printStackTrace();
            throw new Exception("Error while processing Request ID : " + ((JSONObject)postDataJson.get(SdcArtifactHandlerConstants.REQUEST_INFORMATION)).getString(SdcArtifactHandlerConstants.REQUETS_ID) + e.getMessage());
        }
        return false;        

    }
    private boolean createDataForPD(JSONObject request_information, JSONObject document_information) throws Exception {

        String fn = "ArtifactHandlerNode.createReferenceDataForPD";
        String artifact_name = document_information.getString(SdcArtifactHandlerConstants.ARTIFACT_NAME);
        log.info(fn + "Received PD File Name: " + artifact_name + " and suffix lenght " + SdcArtifactHandlerConstants.PD.length());
        try {

            String suffix = artifact_name.substring(SdcArtifactHandlerConstants.PD.length());
            createArtifactRecords(request_information, document_information, suffix);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error while createing PD data records " + e.getMessage());
        }        
        return true;
    }

    private void createArtifactRecords(JSONObject request_information, JSONObject document_information, String suffix) throws Exception {

        log.info("Creating Tosca Records and storing into SDC Artifacs");
        String [] docs = {"Tosca", "Yang"};    
        ArtifactHandlerProviderUtil ahpUtil = new ArtifactHandlerProviderUtil();
        String PDFileContents = document_information.getString(SdcArtifactHandlerConstants.ARTIFACT_CONTENTS);

        //Tosca generation
        OutputStream toscaStream = new ByteArrayOutputStream();
        String toscaContents = null;
        ArtifactProcessorImpl toscaGenerator = new ArtifactProcessorImpl();
        toscaGenerator.generateArtifact(PDFileContents,toscaStream);
        if(toscaStream != null)
            toscaContents = toscaStream.toString();
        log.info("Generated Tosca File : " + toscaContents);

        //Yang generation
        //String yangContents = "Dummay Yang, Yang contents will be available after IST Integration";

        String yangContents = "YANG generation is in Progress";
        String yangName = null;

        for(String doc : docs){
            document_information.put(SdcArtifactHandlerConstants.ARTIFACT_TYPE, doc.concat("Type"));
            document_information.put(SdcArtifactHandlerConstants.ARTIFACT_DESRIPTION, doc.concat("Model"));
            if(doc.equals("Tosca"))
                document_information.put(SdcArtifactHandlerConstants.ARTIFACT_CONTENTS, ahpUtil.escapeSql(toscaContents));
            else if (doc.equals("Yang"))
                document_information.put(SdcArtifactHandlerConstants.ARTIFACT_CONTENTS, ahpUtil.escapeSql(yangContents));
            document_information.put(SdcArtifactHandlerConstants.ARTIFACT_NAME, doc.concat(suffix));
            yangName = doc.concat(suffix);
            updateStoreArtifacts(request_information, document_information);
        }    

        String artifactId = getArtifactID(yangName);
        OutputStream yangStream = new ByteArrayOutputStream();
        YANGGenerator yangGenerator = YANGGeneratorFactory.getYANGGenerator();
        yangGenerator.generateYANG(artifactId , toscaContents, yangStream);
        if(yangStream != null)
            yangContents = yangStream.toString();

        if(yangContents !=null ){
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

    private boolean updateStoreArtifacts(JSONObject request_information, JSONObject document_information ) throws Exception {
        log.info("UpdateStoreArtifactsStarted storing of SDC Artifacs ");

        SvcLogicContext context = new SvcLogicContext();
        DBService dbservice = DBService.initialise();
        ArtifactHandlerProviderUtil ahpUtil = new ArtifactHandlerProviderUtil();
        int intversion = 0;
        context.setAttribute("artifact_name",document_information.getString(SdcArtifactHandlerConstants.ARTIFACT_NAME));
        String internal_version = dbservice.getInternalVersionNumber(context, document_information.getString(SdcArtifactHandlerConstants.ARTIFACT_NAME), null);
        log.info("Internal Version number received from Database : " + internal_version);
        if(internal_version != null){
            intversion = Integer.parseInt(internal_version);
            intversion++ ;
        }        
        context.setAttribute(SdcArtifactHandlerConstants.SERVICE_UUID, document_information.getString(SdcArtifactHandlerConstants.SERVICE_UUID));
        context.setAttribute(SdcArtifactHandlerConstants.DISTRIBUTION_ID, document_information.getString(SdcArtifactHandlerConstants.DISTRIBUTION_ID));
        context.setAttribute(SdcArtifactHandlerConstants.SERVICE_NAME, document_information.getString(SdcArtifactHandlerConstants.SERVICE_NAME));
        context.setAttribute(SdcArtifactHandlerConstants.SERVICE_DESCRIPTION, document_information.getString(SdcArtifactHandlerConstants.SERVICE_DESCRIPTION));
        context.setAttribute(SdcArtifactHandlerConstants.RESOURCE_UUID, document_information.getString(SdcArtifactHandlerConstants.RESOURCE_UUID));
        context.setAttribute(SdcArtifactHandlerConstants.RESOURCE_INSTANCE_NAME,document_information.getString(SdcArtifactHandlerConstants.RESOURCE_INSTANCE_NAME));
        context.setAttribute(SdcArtifactHandlerConstants.RESOURCE_VERSOIN, document_information.getString(SdcArtifactHandlerConstants.RESOURCE_VERSOIN));
        context.setAttribute(SdcArtifactHandlerConstants.RESOURCE_TYPE, document_information.getString(SdcArtifactHandlerConstants.RESOURCE_TYPE));
        context.setAttribute(SdcArtifactHandlerConstants.ARTIFACT_UUID, document_information.getString(SdcArtifactHandlerConstants.ARTIFACT_UUID));
        context.setAttribute(SdcArtifactHandlerConstants.ARTIFACT_TYPE,document_information.getString(SdcArtifactHandlerConstants.ARTIFACT_TYPE));
        context.setAttribute(SdcArtifactHandlerConstants.ARTIFACT_VERSOIN,document_information.getString(SdcArtifactHandlerConstants.ARTIFACT_VERSOIN));
        context.setAttribute(SdcArtifactHandlerConstants.ARTIFACT_DESRIPTION,document_information.getString(SdcArtifactHandlerConstants.ARTIFACT_DESRIPTION));
        context.setAttribute(SdcArtifactHandlerConstants.ARTIFACT_CONTENTS,ahpUtil.escapeSql(document_information.getString(SdcArtifactHandlerConstants.ARTIFACT_CONTENTS)));
        context.setAttribute(SdcArtifactHandlerConstants.ARTIFACT_NAME,document_information.getString(SdcArtifactHandlerConstants.ARTIFACT_NAME));
        dbservice.saveArtifacts(context, intversion);
        return true;            

    }

    public boolean storeReferenceData(JSONObject request_information, JSONObject document_information) throws Exception {    
        log.info("Started storing of SDC Artifacs into Handler" );
        try{
            boolean updateRequired = false;
            boolean pdFile = false;
            String suffix = null;
            String categorySuffix = null;
            DBService dbservice = DBService.initialise();
            ArtifactHandlerProviderUtil ahpUtil = new ArtifactHandlerProviderUtil();
            String contentString =  ahpUtil.escapeSql(document_information.getString(SdcArtifactHandlerConstants.ARTIFACT_CONTENTS))    ;
            String artifactName=ahpUtil.escapeSql(document_information.getString(SdcArtifactHandlerConstants.ARTIFACT_NAME));
            String capabilityArtifactName=StringUtils.replace(artifactName, SdcArtifactHandlerConstants.ARTIFACT_NAME_REFERENCE, SdcArtifactHandlerConstants.ARTIFACT_NAME_CAPABILITY);
            JSONObject capabilities = new JSONObject();
            JSONArray vnfActionList = new JSONArray();
            JSONArray vfModuleActionList = new JSONArray();
            JSONArray vnfcActionList = new JSONArray();
            JSONArray vmActionList = new JSONArray();
            String vnfType=null;
            JSONObject contentObject = new JSONObject(contentString);
            JSONArray contentArray= contentObject.getJSONArray("reference_data");
            for(int a=0; a<contentArray.length() ; a++){
                
                JSONObject content = (JSONObject) contentArray.get(a);
                log.info("contentString =" + content.toString());
                JSONObject scope = content.getJSONObject("scope");
                log.info("scope :" + scope);
                SvcLogicContext context = new SvcLogicContext();
                vnfType=scope.getString(SdcArtifactHandlerConstants.VNF_TYPE);
                context.setAttribute(SdcArtifactHandlerConstants.VNF_TYPE, scope.getString(SdcArtifactHandlerConstants.VNF_TYPE));
                context.setAttribute(SdcArtifactHandlerConstants.ACTION, content.getString(SdcArtifactHandlerConstants.ACTION));
                String actionLevel=content.getString(SdcArtifactHandlerConstants.ACTION_LEVEL);
                context.setAttribute(SdcArtifactHandlerConstants.ACTION_LEVEL, content.getString(SdcArtifactHandlerConstants.ACTION_LEVEL));
                if ((null != actionLevel) && actionLevel.equalsIgnoreCase(SdcArtifactHandlerConstants.ACTION_LEVEL_VNFC)) {
                    vnfcActionList.put(content.getString(SdcArtifactHandlerConstants.ACTION));
                }
                if (null != actionLevel && actionLevel.equalsIgnoreCase(SdcArtifactHandlerConstants.ACTION_LEVEL_VF_MODULE)) {
                    vfModuleActionList.put(content.getString(SdcArtifactHandlerConstants.ACTION));
                }
                if (null != actionLevel && actionLevel.equalsIgnoreCase(SdcArtifactHandlerConstants.ACTION_LEVEL_VNF)) {
                    vnfActionList.put(content.getString(SdcArtifactHandlerConstants.ACTION));
                }
                if (null != actionLevel && actionLevel.equalsIgnoreCase(SdcArtifactHandlerConstants.ACTION_LEVEL_VM)) {
                    vmActionList.put(content.getString(SdcArtifactHandlerConstants.ACTION));
                }
                if(scope.has(SdcArtifactHandlerConstants.VNFC_TYPE) && !scope.isNull(SdcArtifactHandlerConstants.VNFC_TYPE) )
                    context.setAttribute(SdcArtifactHandlerConstants.VNFC_TYPE, scope.getString(SdcArtifactHandlerConstants.VNFC_TYPE));
                else
                    context.setAttribute(SdcArtifactHandlerConstants.VNFC_TYPE,null);
                if (content.has(SdcArtifactHandlerConstants.DEVICE_PROTOCOL))
                    context.setAttribute(SdcArtifactHandlerConstants.DEVICE_PROTOCOL, content.getString(SdcArtifactHandlerConstants.DEVICE_PROTOCOL));
                if (content.has(SdcArtifactHandlerConstants.USER_NAME))
                    context.setAttribute(SdcArtifactHandlerConstants.USER_NAME, content.getString(SdcArtifactHandlerConstants.USER_NAME));
                if (content.has(SdcArtifactHandlerConstants.PORT_NUMBER))
                    context.setAttribute(SdcArtifactHandlerConstants.PORT_NUMBER, content.getString(SdcArtifactHandlerConstants.PORT_NUMBER));
                context.setAttribute(SdcArtifactHandlerConstants.ARTIFACT_TYPE, "");
                if(content.has("artifact-list") && content.get("artifact-list") instanceof JSONArray){                
                    JSONArray artifactLists = (JSONArray)content.get("artifact-list"); 
                    for(int i=0;i<artifactLists.length();i++){
                        JSONObject artifact=(JSONObject)artifactLists.get(i);
                        log.info("artifact is " + artifact);
                        context.setAttribute(SdcArtifactHandlerConstants.ARTIFACT_NAME, artifact.getString(SdcArtifactHandlerConstants.ARTIFACT_NAME));
                        context.setAttribute(SdcArtifactHandlerConstants.FILE_CATEGORY, artifact.getString(SdcArtifactHandlerConstants.ARTIFACT_TYPE));

                        if(artifact.getString(SdcArtifactHandlerConstants.ARTIFACT_NAME) !=null &&
                                artifact.getString(SdcArtifactHandlerConstants.ARTIFACT_NAME).toLowerCase().startsWith(SdcArtifactHandlerConstants.PD))
                        {
                            suffix = artifact.getString(SdcArtifactHandlerConstants.ARTIFACT_NAME).substring(SdcArtifactHandlerConstants.PD.length());
                            categorySuffix = artifact.getString(SdcArtifactHandlerConstants.ARTIFACT_TYPE).substring(SdcArtifactHandlerConstants.PD.length());
                            pdFile = true;
                        }

                        dbservice.processSdcReferences(context, dbservice.isArtifactUpdateRequired(context, SdcArtifactHandlerConstants.DB_SDC_REFERENCE));

                        cleanArtifactInstanceData(context);
                    }

                    if(pdFile)
                    {
                        context.setAttribute(SdcArtifactHandlerConstants.ARTIFACT_NAME, "Tosca".concat(suffix));
                        context.setAttribute(SdcArtifactHandlerConstants.FILE_CATEGORY, SdcArtifactHandlerConstants.TOSCA_MODEL);
                        dbservice.processSdcReferences(context, dbservice.isArtifactUpdateRequired(context, SdcArtifactHandlerConstants.DB_SDC_REFERENCE));
                        context.setAttribute(SdcArtifactHandlerConstants.ARTIFACT_NAME, "Yang".concat(suffix));
                        context.setAttribute(SdcArtifactHandlerConstants.FILE_CATEGORY, SdcArtifactHandlerConstants.PARAMETER_YANG);
                        dbservice.processSdcReferences(context, dbservice.isArtifactUpdateRequired(context, SdcArtifactHandlerConstants.DB_SDC_REFERENCE));
                    }
                }
                if (content.getString(SdcArtifactHandlerConstants.ACTION).equals("Configure")) {
                    if(content.has(SdcArtifactHandlerConstants.DOWNLOAD_DG_REFERENCE) && content.getString(SdcArtifactHandlerConstants.DOWNLOAD_DG_REFERENCE).length() > 0){
                        context.setAttribute(SdcArtifactHandlerConstants.DOWNLOAD_DG_REFERENCE, content.getString(SdcArtifactHandlerConstants.DOWNLOAD_DG_REFERENCE));
                        dbservice.processDownloadDgReference(context, dbservice.isArtifactUpdateRequired(context, SdcArtifactHandlerConstants.DB_DOWNLOAD_DG_REFERENCE));
                    }
    
                    dbservice.processConfigActionDg(context, dbservice.isArtifactUpdateRequired(context, SdcArtifactHandlerConstants.DB_CONFIG_ACTION_DG));
                    dbservice.processDeviceInterfaceProtocol(context, dbservice.isArtifactUpdateRequired(context, SdcArtifactHandlerConstants.DB_DEVICE_INTERFACE_PROTOCOL));
                    dbservice.processDeviceAuthentication(context, dbservice.isArtifactUpdateRequired(context, SdcArtifactHandlerConstants.DB_DEVICE_AUTHENTICATION));
                    
                }
                
                
                populateProtocolReference(dbservice, content);
                
                context.setAttribute(SdcArtifactHandlerConstants.VNFC_TYPE, null);
                
                if( content.has(SdcArtifactHandlerConstants.VM)  && content.get(SdcArtifactHandlerConstants.VM) instanceof JSONArray){
                    JSONArray vmList = (JSONArray)content.get(SdcArtifactHandlerConstants.VM);
                    for(int i=0;i<vmList.length();i++){
                        JSONObject vmInstance=(JSONObject)vmList.get(i);    
                        context.setAttribute(SdcArtifactHandlerConstants.VM_INSTANCE, String.valueOf(vmInstance.getInt(SdcArtifactHandlerConstants.VM_INSTANCE)));
                        log.info("VALUE = " + context.getAttribute(SdcArtifactHandlerConstants.VM_INSTANCE));
                        if(vmInstance.get(SdcArtifactHandlerConstants.VNFC) instanceof JSONArray){
                            JSONArray vnfcInstanceList = (JSONArray)vmInstance.get(SdcArtifactHandlerConstants.VNFC);
                            for(int k=0;k<vnfcInstanceList.length();k++){
                                JSONObject vnfcInstance = (JSONObject)vnfcInstanceList.get(k);
                                context.setAttribute(SdcArtifactHandlerConstants.VNFC_INSTANCE, String.valueOf(vnfcInstance.getInt(SdcArtifactHandlerConstants.VNFC_INSTANCE)));
                                context.setAttribute(SdcArtifactHandlerConstants.VNFC_TYPE, vnfcInstance.getString(SdcArtifactHandlerConstants.VNFC_TYPE));
                                context.setAttribute(SdcArtifactHandlerConstants.VNFC_FUNCTION_CODE, vnfcInstance.getString(SdcArtifactHandlerConstants.VNFC_FUNCTION_CODE));
                                if(vnfcInstance.has(SdcArtifactHandlerConstants.IPADDRESS_V4_OAM_VIP))
                                    context.setAttribute(SdcArtifactHandlerConstants.IPADDRESS_V4_OAM_VIP, vnfcInstance.getString(SdcArtifactHandlerConstants.IPADDRESS_V4_OAM_VIP));
                                if(vnfcInstance.has(SdcArtifactHandlerConstants.GROUP_NOTATION_TYPE))
                                    context.setAttribute(SdcArtifactHandlerConstants.GROUP_NOTATION_TYPE, vnfcInstance.getString(SdcArtifactHandlerConstants.GROUP_NOTATION_TYPE));
                                if(vnfcInstance.has(SdcArtifactHandlerConstants.GROUP_NOTATION_VALUE))
                                    context.setAttribute(SdcArtifactHandlerConstants.GROUP_NOTATION_VALUE, vnfcInstance.getString(SdcArtifactHandlerConstants.GROUP_NOTATION_VALUE));
                                dbservice.processVnfcReference(context, dbservice.isArtifactUpdateRequired(context, SdcArtifactHandlerConstants.DB_VNFC_REFERENCE));
                                cleanVnfcInstance(context);
                            }
                            context.setAttribute(SdcArtifactHandlerConstants.VM_INSTANCE,null);
                        }
                    }        
                } 
                
                                
            }
            capabilities.put("vnf",vnfActionList );     
            capabilities.put("vf-module", vfModuleActionList);
            capabilities.put("vnfc", vnfcActionList);
            capabilities.put("vm", vmActionList);
            processAndStoreCapablitiesArtifact(dbservice, document_information, capabilities,capabilityArtifactName,vnfType );
            
        }
            catch(Exception e)
            {
                e.printStackTrace();
                throw new Exception("Error While Storing :  " + e.getMessage());            
            }
        
            return true;            
        }

        
        


        private void cleanArtifactInstanceData(SvcLogicContext context)
        {
            context.setAttribute(SdcArtifactHandlerConstants.ARTIFACT_NAME, null);
            context.setAttribute(SdcArtifactHandlerConstants.FILE_CATEGORY, null);
        }

        private void cleanVnfcInstance(SvcLogicContext context) {

            context.setAttribute(SdcArtifactHandlerConstants.VNFC_INSTANCE, null);
            context.setAttribute(SdcArtifactHandlerConstants.VNFC_TYPE, null);
            context.setAttribute(SdcArtifactHandlerConstants.VNFC_FUNCTION_CODE, null);
            context.setAttribute(SdcArtifactHandlerConstants.IPADDRESS_V4_OAM_VIP, null);
            context.setAttribute(SdcArtifactHandlerConstants.GROUP_NOTATION_TYPE, null);
            context.setAttribute(SdcArtifactHandlerConstants.GROUP_NOTATION_VALUE, null);

        }

        private void processAndStoreCapablitiesArtifact (DBService dbservice , JSONObject document_information, 
        JSONObject capabilities, String capabilityArtifactName, String vnfType) throws Exception {
            log.info("Begin-->processAndStoreCapablitiesArtifact ");

            try {
                
                JSONObject newCapabilitiesObject=new JSONObject();
                newCapabilitiesObject.put("capabilities", capabilities);
                SvcLogicContext context = new SvcLogicContext();
                context.setAttribute(SdcArtifactHandlerConstants.ARTIFACT_NAME,capabilityArtifactName);
                context.setAttribute(SdcArtifactHandlerConstants.FILE_CATEGORY, SdcArtifactHandlerConstants.CAPABILITY);
                context.setAttribute(SdcArtifactHandlerConstants.ACTION, null);
                context.setAttribute(SdcArtifactHandlerConstants.VNFC_TYPE, null);
                context.setAttribute(SdcArtifactHandlerConstants.ARTIFACT_TYPE, null);
                context.setAttribute(SdcArtifactHandlerConstants.VNF_TYPE,vnfType);
                context.setAttribute(SdcArtifactHandlerConstants.ARTIFACT_CONTENTS,newCapabilitiesObject.toString());
                dbservice.processSdcReferences(context, dbservice.isArtifactUpdateRequired(context, SdcArtifactHandlerConstants.DB_SDC_REFERENCE));
                int intversion = 0;
                
                String internal_version = dbservice.getInternalVersionNumber(context, context.getAttribute(SdcArtifactHandlerConstants.ARTIFACT_NAME), null);
                log.info("Internal Version number received from Database : " + internal_version);
                if(internal_version != null){
                    intversion = Integer.parseInt(internal_version);
                    intversion++ ;
                }        
                context.setAttribute(SdcArtifactHandlerConstants.SERVICE_UUID, document_information.getString(SdcArtifactHandlerConstants.SERVICE_UUID));
                context.setAttribute(SdcArtifactHandlerConstants.DISTRIBUTION_ID, document_information.getString(SdcArtifactHandlerConstants.DISTRIBUTION_ID));
                context.setAttribute(SdcArtifactHandlerConstants.SERVICE_NAME, document_information.getString(SdcArtifactHandlerConstants.SERVICE_NAME));
                context.setAttribute(SdcArtifactHandlerConstants.SERVICE_DESCRIPTION, document_information.getString(SdcArtifactHandlerConstants.SERVICE_DESCRIPTION));
                context.setAttribute(SdcArtifactHandlerConstants.RESOURCE_UUID, document_information.getString(SdcArtifactHandlerConstants.RESOURCE_UUID));
                context.setAttribute(SdcArtifactHandlerConstants.RESOURCE_INSTANCE_NAME,document_information.getString(SdcArtifactHandlerConstants.RESOURCE_INSTANCE_NAME));
                context.setAttribute(SdcArtifactHandlerConstants.RESOURCE_VERSOIN, document_information.getString(SdcArtifactHandlerConstants.RESOURCE_VERSOIN));
                context.setAttribute(SdcArtifactHandlerConstants.RESOURCE_TYPE, document_information.getString(SdcArtifactHandlerConstants.RESOURCE_TYPE));
                context.setAttribute(SdcArtifactHandlerConstants.ARTIFACT_UUID, document_information.getString(SdcArtifactHandlerConstants.ARTIFACT_UUID));
                context.setAttribute(SdcArtifactHandlerConstants.ARTIFACT_VERSOIN,document_information.getString(SdcArtifactHandlerConstants.ARTIFACT_VERSOIN));
                context.setAttribute(SdcArtifactHandlerConstants.ARTIFACT_DESRIPTION,document_information.getString(SdcArtifactHandlerConstants.ARTIFACT_DESRIPTION));
                
                
                dbservice.saveArtifacts(context, intversion);
                return ;            
            }
            catch (Exception e) {
                log.error("Error saving capabilities artifact to DB: "+ e.toString());
                throw e;
            }
            finally {
                log.info("End-->processAndStoreCapablitiesArtifact ");
            }
        
            }
        

            private void populateProtocolReference(DBService dbservice, JSONObject content)  throws Exception{
                log.info("Begin-->populateProtocolReference ");
                try {
                    SvcLogicContext context = new SvcLogicContext();
                    JSONObject scope = content.getJSONObject("scope");
                    String vnfType=null,protocol=null,action=null,actionLevel=null,template=null;
                    if(scope.has(SdcArtifactHandlerConstants.VNF_TYPE) && !scope.isNull(SdcArtifactHandlerConstants.VNF_TYPE))
                        vnfType=scope.getString(SdcArtifactHandlerConstants.VNF_TYPE);
                    if (content.has(SdcArtifactHandlerConstants.DEVICE_PROTOCOL))
                        protocol=content.getString(SdcArtifactHandlerConstants.DEVICE_PROTOCOL);
                    if (content.has(SdcArtifactHandlerConstants.ACTION))
                        action= content.getString(SdcArtifactHandlerConstants.ACTION);
                    if (content.has(SdcArtifactHandlerConstants.ACTION_LEVEL))
                        actionLevel=content.getString(SdcArtifactHandlerConstants.ACTION_LEVEL);
                    if (content.has(SdcArtifactHandlerConstants.TEMPLATE) && !content.isNull(SdcArtifactHandlerConstants.TEMPLATE))
                        template=content.getString(SdcArtifactHandlerConstants.TEMPLATE);
                    dbservice.insertProtocolReference(context, vnfType,protocol,action,actionLevel,template);
                }
                catch (Exception e) {
                    log.error("Error inserting record into protocolReference: "+e.toString());
                    throw e;
                }
                finally {
                    log.info("End-->populateProtocolReference ");
                }
            }

    }
