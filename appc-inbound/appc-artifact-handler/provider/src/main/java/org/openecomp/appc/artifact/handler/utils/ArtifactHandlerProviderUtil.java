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

package org.openecomp.appc.artifact.handler.utils;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.artifacthandler.rev170321.UploadartifactInput;
import org.openecomp.appc.artifact.handler.node.ArtifactHandlerNode;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class ArtifactHandlerProviderUtil {

	public UploadartifactInput templateData ; 
	SvcLogicContext context = null;
	private static final EELFLogger log = EELFManager.getInstance().getLogger(ArtifactHandlerProviderUtil.class);
	public static void loadProperties() {
		// TODO Auto-generated method stub
		
	}
	public enum DistributionStatusEnum {
		DOWNLOAD_OK,
		DOWNLOAD_ERROR,
		ALREADY_DOWNLOADED,
		DEPLOY_OK,
		DEPLOY_ERROR,
		ALREADY_DEPLOYED;
	}

	public ArtifactHandlerProviderUtil(){};
	
	public ArtifactHandlerProviderUtil(UploadartifactInput input) {
		this.templateData = input;
		log.info("templateData " + this.templateData);
	}
	
	public void processTemplate(String requestInfo) throws Exception {		
		if(context == null)
			context = new SvcLogicContext();
			
		ArtifactHandlerNode node  = new ArtifactHandlerNode();
		try {
			
			HashMap<String, String>  processdata = new HashMap<String, String>();
			processdata.put("postData", requestInfo);
			log.info("Post data = " + requestInfo);
			node.processArtifact(processdata, context);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("Error: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
		
	}
	public String createDummyRequestData() throws JSONException, IOException{


		JSONObject info = new JSONObject(this.templateData);
		log.info("INFO = " + info);
		String artifact_name  = templateData.getDocumentParameters().getArtifactName();
		String artifact_version =  templateData.getDocumentParameters().getArtifactVersion();
		
		JSONObject json = new JSONObject();
	    JSONObject requestInfo = new JSONObject();	   
	    String random = getRandom();
	    
	    requestInfo.put(SdcArtifactHandlerConstants.REQUETS_ID, "TLRID-" + random);
	    requestInfo.put(SdcArtifactHandlerConstants.REQUEST_ACTION, "StoreSdcDocumentRequest");
	    requestInfo.put(SdcArtifactHandlerConstants.SOURCE, "TemplateLoader");
	
	    JSONObject docParams = new JSONObject();
	    docParams.put(SdcArtifactHandlerConstants.SERVICE_UUID, "TLSUUID" + templateData.getRequestInformation().getRequestId());
	    docParams.put(SdcArtifactHandlerConstants.DISTRIBUTION_ID, "TLDID" + random);
	    docParams.put(SdcArtifactHandlerConstants.SERVICE_NAME, "TLServiceName");
	    docParams.put(SdcArtifactHandlerConstants.SERVICE_DESCRIPTION, "Template Loader Test");
	    docParams.put(SdcArtifactHandlerConstants.SERVICE_ARTIFACTS, "[]");
	    docParams.put(SdcArtifactHandlerConstants.RESOURCE_UUID, "TLRUID" + random);
	    docParams.put(SdcArtifactHandlerConstants.RESOURCE_INSTANCE_NAME, "TLRIName");
	    docParams.put(SdcArtifactHandlerConstants.REOURCE_NAME, "TLResourceName");
	    docParams.put(SdcArtifactHandlerConstants.RESOURCE_VERSOIN, "TLResourceVersion");
	    docParams.put(SdcArtifactHandlerConstants.RESOURCE_TYPE, "TLResourceType");
	    docParams.put(SdcArtifactHandlerConstants.ARTIFACT_UUID, "TLAUUID" +  random);
	    docParams.put(SdcArtifactHandlerConstants.ARTIFACT_NAME, templateData.getDocumentParameters().getArtifactName());
	    docParams.put(SdcArtifactHandlerConstants.ARTIFACT_TYPE, "APPC-CONFIG");
	    docParams.put(SdcArtifactHandlerConstants.ARTIFACT_VERSOIN, templateData.getDocumentParameters().getArtifactVersion());
	    docParams.put(SdcArtifactHandlerConstants.ARTIFACT_DESRIPTION, "SdcTestDescription");
	//   String data = IOUtils.toString(TestartifactHandlerNode.class.getClassLoader().getResourceAsStream("template_msrp_msc_a_template.json"), "utf-8");
	//    String data = IOUtils.toString(TemplateProcessor.class.getClassLoader().getResourceAsStream("referenceData.json"), "utf-8");

	   // this.templateData = this.templateData.substring(this.templateData.indexOf("}") + 1);
	    docParams.put("artifact-contents", templateData.getDocumentParameters().getArtifactContents());
	
	    json.put(SdcArtifactHandlerConstants.REQUEST_INFORMATION, requestInfo);
	    json.put(SdcArtifactHandlerConstants.DOCUMENT_PARAMETERS, docParams);
	    System.out.println("Final data ="  + this.templateData);
	    return String.format("{\"input\": %s}", json.toString());
	}
	
	private String getRandom() {
		SecureRandom random = new SecureRandom();
	    int num = random.nextInt(100000);
	    String formatted = String.format("%05d", num); 
	    return formatted;
	}
	
	public String escapeSql(String str) {
		if (str == null) {
			return null;
		}
		String searchList[] = new String[]{"'","\\"};
		String replacementList[] = new String[]{ "''","\\\\"};
		return StringUtils.replaceEach(str,searchList, replacementList);
	}
	public String createRequestData() throws JSONException, IOException{


		JSONObject info = new JSONObject(this.templateData);
		log.info("INFO = " + info);
				
		JSONObject json = new JSONObject();
	    JSONObject requestInfo = new JSONObject();	   
	    String random = getRandom();
	    
	    requestInfo.put(SdcArtifactHandlerConstants.REQUETS_ID, templateData.getRequestInformation().getRequestId());
	    requestInfo.put(SdcArtifactHandlerConstants.REQUEST_ACTION, "StoreSdcDocumentRequest");
	    requestInfo.put(SdcArtifactHandlerConstants.SOURCE, templateData.getRequestInformation().getSource());
	    
	    String serviceDescription =  serviceDescriptionData(templateData.getDocumentParameters().getServiceDescription());
	    
	    JSONObject docParams = new JSONObject();
	    docParams.put(SdcArtifactHandlerConstants.SERVICE_UUID, templateData.getDocumentParameters().getResourceUuid());
	    docParams.put(SdcArtifactHandlerConstants.DISTRIBUTION_ID, templateData.getDocumentParameters().getDistributionId());
	    docParams.put(SdcArtifactHandlerConstants.SERVICE_NAME, templateData.getDocumentParameters().getServiceName());
	    docParams.put(SdcArtifactHandlerConstants.SERVICE_DESCRIPTION,serviceDescription);
	    docParams.put(SdcArtifactHandlerConstants.SERVICE_ARTIFACTS, templateData.getDocumentParameters().getServiceArtifacts());
	    docParams.put(SdcArtifactHandlerConstants.RESOURCE_UUID, templateData.getDocumentParameters().getResourceUuid());
	    docParams.put(SdcArtifactHandlerConstants.RESOURCE_INSTANCE_NAME, templateData.getDocumentParameters().getResourceInstanceName());
	    docParams.put(SdcArtifactHandlerConstants.REOURCE_NAME, templateData.getDocumentParameters().getResourceName());
	    docParams.put(SdcArtifactHandlerConstants.RESOURCE_VERSOIN, templateData.getDocumentParameters().getResourceVersion());
	    docParams.put(SdcArtifactHandlerConstants.RESOURCE_TYPE, templateData.getDocumentParameters().getResourceType());
	    docParams.put(SdcArtifactHandlerConstants.ARTIFACT_UUID, templateData.getDocumentParameters().getArtifactUuid());
	    docParams.put(SdcArtifactHandlerConstants.ARTIFACT_NAME, templateData.getDocumentParameters().getArtifactName());
	    docParams.put(SdcArtifactHandlerConstants.ARTIFACT_TYPE, templateData.getDocumentParameters().getArtifactType());
	    docParams.put(SdcArtifactHandlerConstants.ARTIFACT_VERSOIN, templateData.getDocumentParameters().getArtifactVersion());
	    docParams.put(SdcArtifactHandlerConstants.ARTIFACT_DESRIPTION, templateData.getDocumentParameters().getArtifactDescription());

	    docParams.put("artifact-contents", templateData.getDocumentParameters().getArtifactContents());
	
	    json.put(SdcArtifactHandlerConstants.REQUEST_INFORMATION, requestInfo);
	    json.put(SdcArtifactHandlerConstants.DOCUMENT_PARAMETERS, docParams);
	    System.out.println("Final data ="  + this.templateData);
	    return String.format("{\"input\": %s}", json.toString());
	}
	
	private String serviceDescriptionData(String serviceDescription){
		if(!StringUtils.isBlank(serviceDescription)&&serviceDescription.length()>255){
			serviceDescription = serviceDescription.substring(0, 255);
		}
		return serviceDescription;
	}	
}
