/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * 
 *  ECOMP is a trademark and service mark of AT&T Intellectual Property.
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
import org.openecomp.appc.artifact.handler.utils.AsdcArtifactHandlerConstants;
import org.openecomp.appc.yang.YANGGenerator;
import org.openecomp.appc.yang.impl.YANGGeneratorFactory;
import org.openecomp.sdnc.config.params.transformer.tosca.ArtifactProcessorImpl;
import org.openecomp.sdnc.sli.SvcLogicContext;
import org.openecomp.sdnc.sli.SvcLogicException;
import org.openecomp.sdnc.sli.SvcLogicJavaPlugin;

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
				storeUpdateAsdcArtifacts(input);
			}
		}
		catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}

	private boolean storeUpdateAsdcArtifacts(JSONObject postDataJson) throws Exception {
		log.info("Starting processing of ASDC Artifacs into Handler with Data : " + postDataJson.toString());		
		try{
			JSONObject request_information = (JSONObject)postDataJson.get(AsdcArtifactHandlerConstants.REQUEST_INFORMATION);
			JSONObject document_information =(JSONObject)postDataJson.get(AsdcArtifactHandlerConstants.DOCUMENT_PARAMETERS);
			String artifact_name = document_information.getString(AsdcArtifactHandlerConstants.ARTIFACT_NAME);
			if(artifact_name !=null){
				updateStoreArtifacts(request_information, document_information );
				if(artifact_name.toLowerCase().startsWith(AsdcArtifactHandlerConstants.REFERENCE))	
					return storeReferenceData(request_information, document_information );
				else if (artifact_name.toLowerCase().startsWith(AsdcArtifactHandlerConstants.PD))
					return createDataForPD(request_information, document_information );

			}
			else
				throw new Exception("Missing Artifact Name for Request : "  + request_information.getString(AsdcArtifactHandlerConstants.REQUETS_ID));					
		}
		catch(Exception e){
			e.printStackTrace();
			throw new Exception("Error while processing Request ID : " + ((JSONObject)postDataJson.get(AsdcArtifactHandlerConstants.REQUEST_INFORMATION)).getString(AsdcArtifactHandlerConstants.REQUETS_ID) + e.getMessage());
		}
		return false;		

	}
	private boolean createDataForPD(JSONObject request_information, JSONObject document_information) throws Exception {

		String fn = "ArtifactHandlerNode.createReferenceDataForPD";
		String artifact_name = document_information.getString(AsdcArtifactHandlerConstants.ARTIFACT_NAME);
		log.info(fn + "Received PD File Name: " + artifact_name + " and suffix lenght " + AsdcArtifactHandlerConstants.PD.length());
		try {

			String suffix = artifact_name.substring(AsdcArtifactHandlerConstants.PD.length());
			createArtifactRecords(request_information, document_information, suffix);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Error while createing PD data records " + e.getMessage());
		}		
		return true;
	}

	private void createArtifactRecords(JSONObject request_information, JSONObject document_information, String suffix) throws Exception {

		log.info("Creating Tosca Records and storing into ASDC Artifacs");
		String [] docs = {"Tosca", "Yang"};	
		ArtifactHandlerProviderUtil ahpUtil = new ArtifactHandlerProviderUtil();
		String PDFileContents = document_information.getString(AsdcArtifactHandlerConstants.ARTIFACT_CONTENTS);

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
			document_information.put(AsdcArtifactHandlerConstants.ARTIFACT_TYPE, doc.concat("Type"));
			document_information.put(AsdcArtifactHandlerConstants.ARTIFACT_DESRIPTION, doc.concat("Model"));
			if(doc.equals("Tosca"))
				document_information.put(AsdcArtifactHandlerConstants.ARTIFACT_CONTENTS, ahpUtil.escapeSql(toscaContents));
			else if (doc.equals("Yang"))
				document_information.put(AsdcArtifactHandlerConstants.ARTIFACT_CONTENTS, ahpUtil.escapeSql(yangContents));		
			document_information.put(AsdcArtifactHandlerConstants.ARTIFACT_NAME, doc.concat(suffix));	
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
		log.info("UpdateStoreArtifactsStarted storing of ASDC Artifacs ");

		SvcLogicContext context = new SvcLogicContext();
		DBService dbservice = DBService.initialise();
		ArtifactHandlerProviderUtil ahpUtil = new ArtifactHandlerProviderUtil();
		int intversion = 0;
		context.setAttribute("artifact_name",document_information.getString(AsdcArtifactHandlerConstants.ARTIFACT_NAME)); 
		String internal_version = dbservice.getInternalVersionNumber(context, document_information.getString(AsdcArtifactHandlerConstants.ARTIFACT_NAME), null);
		log.info("Internal Version number received from Database : " + internal_version);
		if(internal_version != null){
			intversion = Integer.parseInt(internal_version);
			intversion++ ;
		}		
		context.setAttribute(AsdcArtifactHandlerConstants.SERVICE_UUID, document_information.getString(AsdcArtifactHandlerConstants.SERVICE_UUID));
		context.setAttribute(AsdcArtifactHandlerConstants.DISTRIBUTION_ID, document_information.getString(AsdcArtifactHandlerConstants.DISTRIBUTION_ID));
		context.setAttribute(AsdcArtifactHandlerConstants.SERVICE_NAME, document_information.getString(AsdcArtifactHandlerConstants.SERVICE_NAME));
		context.setAttribute(AsdcArtifactHandlerConstants.SERVICE_DESCRIPTION, document_information.getString(AsdcArtifactHandlerConstants.SERVICE_DESCRIPTION)); 
		context.setAttribute(AsdcArtifactHandlerConstants.RESOURCE_UUID, document_information.getString(AsdcArtifactHandlerConstants.RESOURCE_UUID));
		context.setAttribute(AsdcArtifactHandlerConstants.RESOURCE_INSTANCE_NAME,document_information.getString(AsdcArtifactHandlerConstants.RESOURCE_INSTANCE_NAME));
		context.setAttribute(AsdcArtifactHandlerConstants.RESOURCE_VERSOIN, document_information.getString(AsdcArtifactHandlerConstants.RESOURCE_VERSOIN));
		context.setAttribute(AsdcArtifactHandlerConstants.RESOURCE_TYPE, document_information.getString(AsdcArtifactHandlerConstants.RESOURCE_TYPE));
		context.setAttribute(AsdcArtifactHandlerConstants.ARTIFACT_UUID, document_information.getString(AsdcArtifactHandlerConstants.ARTIFACT_UUID)); 
		context.setAttribute(AsdcArtifactHandlerConstants.ARTIFACT_TYPE,document_information.getString(AsdcArtifactHandlerConstants.ARTIFACT_TYPE)); 
		context.setAttribute(AsdcArtifactHandlerConstants.ARTIFACT_VERSOIN,document_information.getString(AsdcArtifactHandlerConstants.ARTIFACT_VERSOIN)); 
		context.setAttribute(AsdcArtifactHandlerConstants.ARTIFACT_DESRIPTION,document_information.getString(AsdcArtifactHandlerConstants.ARTIFACT_DESRIPTION));
		context.setAttribute(AsdcArtifactHandlerConstants.ARTIFACT_CONTENTS,ahpUtil.escapeSql(document_information.getString(AsdcArtifactHandlerConstants.ARTIFACT_CONTENTS)));
		context.setAttribute(AsdcArtifactHandlerConstants.ARTIFACT_NAME,document_information.getString(AsdcArtifactHandlerConstants.ARTIFACT_NAME));
		dbservice.saveArtifacts(context, intversion);
		return true;			

	}

	public boolean storeReferenceData(JSONObject request_information, JSONObject document_information) throws Exception {	
		log.info("Started storing of ASDC Artifacs into Handler" );
		try{
			boolean updateRequired = false;
			boolean pdFile = false;
			String suffix = null;
			String categorySuffix = null;
			DBService dbservice = DBService.initialise();
			ArtifactHandlerProviderUtil ahpUtil = new ArtifactHandlerProviderUtil();
			String contentString =  ahpUtil.escapeSql(document_information.getString(AsdcArtifactHandlerConstants.ARTIFACT_CONTENTS))	;
			String artifactName=ahpUtil.escapeSql(document_information.getString(AsdcArtifactHandlerConstants.ARTIFACT_NAME));
			String capabilityArtifactName=StringUtils.replace(artifactName,AsdcArtifactHandlerConstants.ARTIFACT_NAME_REFERENCE,AsdcArtifactHandlerConstants.ARTIFACT_NAME_CAPABILITY);
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
				vnfType=scope.getString(AsdcArtifactHandlerConstants.VNF_TYPE);
				context.setAttribute(AsdcArtifactHandlerConstants.VNF_TYPE, scope.getString(AsdcArtifactHandlerConstants.VNF_TYPE));
				context.setAttribute(AsdcArtifactHandlerConstants.ACTION, content.getString(AsdcArtifactHandlerConstants.ACTION));
				String actionLevel=content.getString(AsdcArtifactHandlerConstants.ACTION_LEVEL);
				context.setAttribute(AsdcArtifactHandlerConstants.ACTION_LEVEL, content.getString(AsdcArtifactHandlerConstants.ACTION_LEVEL));
				if ((null != actionLevel) && actionLevel.equalsIgnoreCase(AsdcArtifactHandlerConstants.ACTION_LEVEL_VNFC)) {
					vnfcActionList.put(content.getString(AsdcArtifactHandlerConstants.ACTION));
				}
				if (null != actionLevel && actionLevel.equalsIgnoreCase(AsdcArtifactHandlerConstants.ACTION_LEVEL_VF_MODULE)) {
					vfModuleActionList.put(content.getString(AsdcArtifactHandlerConstants.ACTION));
				}
				if (null != actionLevel && actionLevel.equalsIgnoreCase(AsdcArtifactHandlerConstants.ACTION_LEVEL_VNF)) {
					vnfActionList.put(content.getString(AsdcArtifactHandlerConstants.ACTION));
				}
				if (null != actionLevel && actionLevel.equalsIgnoreCase(AsdcArtifactHandlerConstants.ACTION_LEVEL_VM)) {
					vmActionList.put(content.getString(AsdcArtifactHandlerConstants.ACTION));
				}
				if(scope.has(AsdcArtifactHandlerConstants.VNFC_TYPE) && !scope.isNull(AsdcArtifactHandlerConstants.VNFC_TYPE) )
					context.setAttribute(AsdcArtifactHandlerConstants.VNFC_TYPE, scope.getString(AsdcArtifactHandlerConstants.VNFC_TYPE));
				else
					context.setAttribute(AsdcArtifactHandlerConstants.VNFC_TYPE,null);
				if (content.has(AsdcArtifactHandlerConstants.DEVICE_PROTOCOL))
					context.setAttribute(AsdcArtifactHandlerConstants.DEVICE_PROTOCOL, content.getString(AsdcArtifactHandlerConstants.DEVICE_PROTOCOL));
				if (content.has(AsdcArtifactHandlerConstants.USER_NAME))
					context.setAttribute(AsdcArtifactHandlerConstants.USER_NAME, content.getString(AsdcArtifactHandlerConstants.USER_NAME));
				if (content.has(AsdcArtifactHandlerConstants.PORT_NUMBER))
					context.setAttribute(AsdcArtifactHandlerConstants.PORT_NUMBER, content.getString(AsdcArtifactHandlerConstants.PORT_NUMBER));
				context.setAttribute(AsdcArtifactHandlerConstants.ARTIFACT_TYPE, "");
				if(content.has("artifact-list") && content.get("artifact-list") instanceof JSONArray){				
					JSONArray artifactLists = (JSONArray)content.get("artifact-list"); 
					for(int i=0;i<artifactLists.length();i++){
						JSONObject artifact=(JSONObject)artifactLists.get(i);
						log.info("artifact is " + artifact);
						context.setAttribute(AsdcArtifactHandlerConstants.ARTIFACT_NAME, artifact.getString(AsdcArtifactHandlerConstants.ARTIFACT_NAME));
						context.setAttribute(AsdcArtifactHandlerConstants.FILE_CATEGORY, artifact.getString(AsdcArtifactHandlerConstants.ARTIFACT_TYPE));					

						if(artifact.getString(AsdcArtifactHandlerConstants.ARTIFACT_NAME) !=null && 
								artifact.getString(AsdcArtifactHandlerConstants.ARTIFACT_NAME).toLowerCase().startsWith(AsdcArtifactHandlerConstants.PD))
						{
							suffix = artifact.getString(AsdcArtifactHandlerConstants.ARTIFACT_NAME).substring(AsdcArtifactHandlerConstants.PD.length());
							categorySuffix = artifact.getString(AsdcArtifactHandlerConstants.ARTIFACT_TYPE).substring(AsdcArtifactHandlerConstants.PD.length());
							pdFile = true;
						}

						dbservice.processAsdcReferences(context, dbservice.isArtifactUpdateRequired(context, AsdcArtifactHandlerConstants.DB_ASDC_REFERENCE));		

						cleanArtifactInstanceData(context);
					}

					if(pdFile)
					{
						context.setAttribute(AsdcArtifactHandlerConstants.ARTIFACT_NAME, "Tosca".concat(suffix));
						context.setAttribute(AsdcArtifactHandlerConstants.FILE_CATEGORY, AsdcArtifactHandlerConstants.TOSCA_MODEL);
						dbservice.processAsdcReferences(context, dbservice.isArtifactUpdateRequired(context, AsdcArtifactHandlerConstants.DB_ASDC_REFERENCE));
						context.setAttribute(AsdcArtifactHandlerConstants.ARTIFACT_NAME, "Yang".concat(suffix));
						context.setAttribute(AsdcArtifactHandlerConstants.FILE_CATEGORY, AsdcArtifactHandlerConstants.PARAMETER_YANG);
						dbservice.processAsdcReferences(context, dbservice.isArtifactUpdateRequired(context, AsdcArtifactHandlerConstants.DB_ASDC_REFERENCE));
					}
				}
				if (content.getString(AsdcArtifactHandlerConstants.ACTION).equals("Configure")) {
					if(content.has(AsdcArtifactHandlerConstants.DOWNLOAD_DG_REFERENCE) && content.getString(AsdcArtifactHandlerConstants.DOWNLOAD_DG_REFERENCE).length() > 0){
						context.setAttribute(AsdcArtifactHandlerConstants.DOWNLOAD_DG_REFERENCE, content.getString(AsdcArtifactHandlerConstants.DOWNLOAD_DG_REFERENCE));
						dbservice.processDownloadDgReference(context, dbservice.isArtifactUpdateRequired(context,AsdcArtifactHandlerConstants.DB_DOWNLOAD_DG_REFERENCE));
					}
	
					dbservice.processConfigActionDg(context, dbservice.isArtifactUpdateRequired(context, AsdcArtifactHandlerConstants.DB_CONFIG_ACTION_DG));
					dbservice.processDeviceInterfaceProtocol(context, dbservice.isArtifactUpdateRequired(context, AsdcArtifactHandlerConstants.DB_DEVICE_INTERFACE_PROTOCOL));
					dbservice.processDeviceAuthentication(context, dbservice.isArtifactUpdateRequired(context, AsdcArtifactHandlerConstants.DB_DEVICE_AUTHENTICATION));
					
				}
				
				
				populateProtocolReference(dbservice, content);
				
				context.setAttribute(AsdcArtifactHandlerConstants.VNFC_TYPE, null);
				
				if( content.has(AsdcArtifactHandlerConstants.VM)  && content.get(AsdcArtifactHandlerConstants.VM) instanceof JSONArray){
					JSONArray vmList = (JSONArray)content.get(AsdcArtifactHandlerConstants.VM);
					for(int i=0;i<vmList.length();i++){
						JSONObject vmInstance=(JSONObject)vmList.get(i);	
						context.setAttribute(AsdcArtifactHandlerConstants.VM_INSTANCE, String.valueOf(vmInstance.getInt(AsdcArtifactHandlerConstants.VM_INSTANCE)));
						log.info("VALUE = " + context.getAttribute(AsdcArtifactHandlerConstants.VM_INSTANCE));
						if(vmInstance.get(AsdcArtifactHandlerConstants.VNFC) instanceof JSONArray){
							JSONArray vnfcInstanceList = (JSONArray)vmInstance.get(AsdcArtifactHandlerConstants.VNFC);
							for(int k=0;k<vnfcInstanceList.length();k++){
								JSONObject vnfcInstance = (JSONObject)vnfcInstanceList.get(k);
								context.setAttribute(AsdcArtifactHandlerConstants.VNFC_INSTANCE, String.valueOf(vnfcInstance.getInt(AsdcArtifactHandlerConstants.VNFC_INSTANCE)));
								context.setAttribute(AsdcArtifactHandlerConstants.VNFC_TYPE, vnfcInstance.getString(AsdcArtifactHandlerConstants.VNFC_TYPE));
								context.setAttribute(AsdcArtifactHandlerConstants.VNFC_FUNCTION_CODE, vnfcInstance.getString(AsdcArtifactHandlerConstants.VNFC_FUNCTION_CODE));
								if(vnfcInstance.has(AsdcArtifactHandlerConstants.IPADDRESS_V4_OAM_VIP))
									context.setAttribute(AsdcArtifactHandlerConstants.IPADDRESS_V4_OAM_VIP, vnfcInstance.getString(AsdcArtifactHandlerConstants.IPADDRESS_V4_OAM_VIP));
								if(vnfcInstance.has(AsdcArtifactHandlerConstants.GROUP_NOTATION_TYPE))
									context.setAttribute(AsdcArtifactHandlerConstants.GROUP_NOTATION_TYPE, vnfcInstance.getString(AsdcArtifactHandlerConstants.GROUP_NOTATION_TYPE));
								if(vnfcInstance.has(AsdcArtifactHandlerConstants.GROUP_NOTATION_VALUE))
									context.setAttribute(AsdcArtifactHandlerConstants.GROUP_NOTATION_VALUE, vnfcInstance.getString(AsdcArtifactHandlerConstants.GROUP_NOTATION_VALUE));
								dbservice.processVnfcReference(context, dbservice.isArtifactUpdateRequired(context, AsdcArtifactHandlerConstants.DB_VNFC_REFERENCE));	
								cleanVnfcInstance(context);
							}
							context.setAttribute(AsdcArtifactHandlerConstants.VM_INSTANCE,null);
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
			context.setAttribute(AsdcArtifactHandlerConstants.ARTIFACT_NAME, null);
			context.setAttribute(AsdcArtifactHandlerConstants.FILE_CATEGORY, null);
		}

		private void cleanVnfcInstance(SvcLogicContext context) {

			context.setAttribute(AsdcArtifactHandlerConstants.VNFC_INSTANCE, null);
			context.setAttribute(AsdcArtifactHandlerConstants.VNFC_TYPE, null);
			context.setAttribute(AsdcArtifactHandlerConstants.VNFC_FUNCTION_CODE, null);
			context.setAttribute(AsdcArtifactHandlerConstants.IPADDRESS_V4_OAM_VIP, null);
			context.setAttribute(AsdcArtifactHandlerConstants.GROUP_NOTATION_TYPE, null);
			context.setAttribute(AsdcArtifactHandlerConstants.GROUP_NOTATION_VALUE, null);

		}

		private void processAndStoreCapablitiesArtifact (DBService dbservice , JSONObject document_information, 
		JSONObject capabilities, String capabilityArtifactName, String vnfType) throws Exception {
			log.info("Begin-->processAndStoreCapablitiesArtifact ");

			try {
				
				JSONObject newCapabilitiesObject=new JSONObject();
				newCapabilitiesObject.put("capabilities", capabilities);
				SvcLogicContext context = new SvcLogicContext();
				context.setAttribute(AsdcArtifactHandlerConstants.ARTIFACT_NAME,capabilityArtifactName);
				context.setAttribute(AsdcArtifactHandlerConstants.FILE_CATEGORY, AsdcArtifactHandlerConstants.CAPABILITY);
				context.setAttribute(AsdcArtifactHandlerConstants.ACTION, null);
				context.setAttribute(AsdcArtifactHandlerConstants.VNFC_TYPE, null);
				context.setAttribute(AsdcArtifactHandlerConstants.ARTIFACT_TYPE, null);
				context.setAttribute(AsdcArtifactHandlerConstants.VNF_TYPE,vnfType);
				context.setAttribute(AsdcArtifactHandlerConstants.ARTIFACT_CONTENTS,newCapabilitiesObject.toString());
				dbservice.processAsdcReferences(context, dbservice.isArtifactUpdateRequired(context, AsdcArtifactHandlerConstants.DB_ASDC_REFERENCE));
				int intversion = 0;
				
				String internal_version = dbservice.getInternalVersionNumber(context, context.getAttribute(AsdcArtifactHandlerConstants.ARTIFACT_NAME), null);
				log.info("Internal Version number received from Database : " + internal_version);
				if(internal_version != null){
					intversion = Integer.parseInt(internal_version);
					intversion++ ;
				}		
				context.setAttribute(AsdcArtifactHandlerConstants.SERVICE_UUID, document_information.getString(AsdcArtifactHandlerConstants.SERVICE_UUID));
				context.setAttribute(AsdcArtifactHandlerConstants.DISTRIBUTION_ID, document_information.getString(AsdcArtifactHandlerConstants.DISTRIBUTION_ID));
				context.setAttribute(AsdcArtifactHandlerConstants.SERVICE_NAME, document_information.getString(AsdcArtifactHandlerConstants.SERVICE_NAME));
				context.setAttribute(AsdcArtifactHandlerConstants.SERVICE_DESCRIPTION, document_information.getString(AsdcArtifactHandlerConstants.SERVICE_DESCRIPTION)); 
				context.setAttribute(AsdcArtifactHandlerConstants.RESOURCE_UUID, document_information.getString(AsdcArtifactHandlerConstants.RESOURCE_UUID));
				context.setAttribute(AsdcArtifactHandlerConstants.RESOURCE_INSTANCE_NAME,document_information.getString(AsdcArtifactHandlerConstants.RESOURCE_INSTANCE_NAME));
				context.setAttribute(AsdcArtifactHandlerConstants.RESOURCE_VERSOIN, document_information.getString(AsdcArtifactHandlerConstants.RESOURCE_VERSOIN));
				context.setAttribute(AsdcArtifactHandlerConstants.RESOURCE_TYPE, document_information.getString(AsdcArtifactHandlerConstants.RESOURCE_TYPE));
				context.setAttribute(AsdcArtifactHandlerConstants.ARTIFACT_UUID, document_information.getString(AsdcArtifactHandlerConstants.ARTIFACT_UUID)); 
				context.setAttribute(AsdcArtifactHandlerConstants.ARTIFACT_VERSOIN,document_information.getString(AsdcArtifactHandlerConstants.ARTIFACT_VERSOIN)); 
				context.setAttribute(AsdcArtifactHandlerConstants.ARTIFACT_DESRIPTION,document_information.getString(AsdcArtifactHandlerConstants.ARTIFACT_DESRIPTION));
				
				
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
					if(scope.has(AsdcArtifactHandlerConstants.VNF_TYPE) && !scope.isNull(AsdcArtifactHandlerConstants.VNF_TYPE))
						vnfType=scope.getString(AsdcArtifactHandlerConstants.VNF_TYPE);
					if (content.has(AsdcArtifactHandlerConstants.DEVICE_PROTOCOL))
						protocol=content.getString(AsdcArtifactHandlerConstants.DEVICE_PROTOCOL);
					if (content.has(AsdcArtifactHandlerConstants.ACTION))
						action= content.getString(AsdcArtifactHandlerConstants.ACTION);
					if (content.has(AsdcArtifactHandlerConstants.ACTION_LEVEL))
						actionLevel=content.getString(AsdcArtifactHandlerConstants.ACTION_LEVEL);
					if (content.has(AsdcArtifactHandlerConstants.TEMPLATE) && !content.isNull(AsdcArtifactHandlerConstants.TEMPLATE))
						template=content.getString(AsdcArtifactHandlerConstants.TEMPLATE);
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
