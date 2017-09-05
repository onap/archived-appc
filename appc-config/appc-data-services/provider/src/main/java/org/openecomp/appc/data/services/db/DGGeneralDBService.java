/*-
 * ============LICENSE_START=======================================================
 * ONAP : APP-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property.  All rights reserved.
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

package org.openecomp.appc.data.services.db;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource.QueryStatus;
import org.onap.ccsdk.sli.adaptors.resource.sql.SqlResource;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class DGGeneralDBService {

	private static final EELFLogger log = EELFManager.getInstance().getLogger(DGGeneralDBService.class);
	private SvcLogicResource serviceLogic;
	private static DGGeneralDBService dgGeneralDBService = null;

	public static DGGeneralDBService initialise() {
		if (dgGeneralDBService == null) {
			dgGeneralDBService = new DGGeneralDBService();
		}
		return dgGeneralDBService;
	}

	private DGGeneralDBService() {
		if (serviceLogic == null) {
			serviceLogic = new SqlResource();
		}
	}

	public QueryStatus getDeviceProtocolByVnfType(SvcLogicContext ctx, String prefix) throws SvcLogicException {
		QueryStatus status = null;
		if (serviceLogic != null && ctx != null) {
			String key = "SELECT * FROM DEVICE_INTERFACE_PROTOCOL WHERE vnf_type = $vnf-type ;";
			status = serviceLogic.query("SQL", false, null, key, prefix, null, ctx);
		}
		return status;
	}

	public QueryStatus getDeviceAuthenticationByVnfType(SvcLogicContext ctx, String prefix) throws SvcLogicException {
		QueryStatus status = null;
		if (serviceLogic != null && ctx != null) {
			String key = "SELECT * FROM DEVICE_AUTHENTICATION WHERE vnf_type = $vnf-type ;";
			status = serviceLogic.query("SQL", false, null, key, prefix, null, ctx);

		}
		return status;
	}

	public QueryStatus getConfigFileReferenceByVnfType(SvcLogicContext ctx, String prefix) throws SvcLogicException {
		QueryStatus status = null;
		if (serviceLogic != null && ctx != null) {
			String key = "SELECT * FROM CONFIG_FILE_REFERENCE WHERE vnf_type = $vnf-type ;";
			status = serviceLogic.query("SQL", false, null, key, prefix, null, ctx);
		}
		return status;
	}

	public QueryStatus getConfigFileReferenceByFileTypeNVnfType(SvcLogicContext ctx, String prefix, String fileType)
			throws SvcLogicException {
		QueryStatus status = null;
		if (serviceLogic != null && ctx != null) {
			String key = "SELECT * FROM CONFIG_FILE_REFERENCE  WHERE file_type = '" + fileType
					+ "' and vnf_type = $vnf-type ;";
			status = serviceLogic.query("SQL", false, null, key, prefix, null, ctx);
		}
		return status;
	}

	public QueryStatus getTemplate(SvcLogicContext ctx, String prefix, String fileCategory) throws SvcLogicException {
		QueryStatus status = null;
		if (serviceLogic != null && ctx != null) {
			String key = "SELECT artifact_content file_content , asdc_artifacts_id config_file_id "
					+ " FROM ASDC_ARTIFACTS "
					+ " WHERE asdc_artifacts_id = ( SELECT MAX(a.asdc_artifacts_id) configfileid  "
					+ " FROM ASDC_ARTIFACTS a, ASDC_REFERENCE b " + " WHERE a.artifact_name = b.artifact_name "
					+ " AND file_category =  '" + fileCategory + "'" + " AND action =  $request-action "
					+ " AND vnf_type =  $vnf-type  " + " AND vnfc_type =   $vnfc-type ) ; ";
			status = serviceLogic.query("SQL", false, null, key, prefix, null, ctx);
		}
		return status;
	}

	public QueryStatus getTemplateByVnfTypeNAction(SvcLogicContext ctx, String prefix, String fileCategory)
			throws SvcLogicException {
		QueryStatus status = null;
		if (serviceLogic != null && ctx != null) {
			String key = "SELECT artifact_content file_content , asdc_artifacts_id config_file_id "
					+ " FROM ASDC_ARTIFACTS "
					+ " WHERE asdc_artifacts_id = (SELECT MAX(a.asdc_artifacts_id) configfileid  "
					+ " FROM ASDC_ARTIFACTS a, ASDC_REFERENCE b " + " WHERE a.artifact_name = b.artifact_name "
					+ " AND file_category =  '" + fileCategory + "'" + " AND action =  $request-action "
					+ " AND vnf_type =  $vnf-type ) ; ";

			status = serviceLogic.query("SQL", false, null, key, prefix, null, ctx);
		}
		return status;
	}

	public QueryStatus getTemplateByVnfType(SvcLogicContext ctx, String prefix, String fileCategory)
			throws SvcLogicException {
		QueryStatus status = null;
		if (serviceLogic != null && ctx != null) {
			String key = "SELECT artifact_content file_content , asdc_artifacts_id config_file_id "
					+ " FROM ASDC_ARTIFACTS "
					+ " WHERE asdc_artifacts_id = (SELECT MAX(a.asdc_artifacts_id) configfileid  "
					+ " FROM ASDC_ARTIFACTS a, ASDC_REFERENCE b " + " WHERE a.artifact_name = b.artifact_name "
					+ " AND file_category =  '" + fileCategory + "'" + " AND vnf_type =  $vnf-type ) ; ";

			status = serviceLogic.query("SQL", false, null, key, prefix, null, ctx);
		}
		return status;
	}

	public QueryStatus getTemplateByTemplateName(SvcLogicContext ctx, String prefix, String templateName)
			throws SvcLogicException {
		QueryStatus status = null;
		if (serviceLogic != null && ctx != null) {
			String key = "SELECT artifact_content file_content , asdc_artifacts_id config_file_id "
					+ " FROM ASDC_ARTIFACTS "
					+ " WHERE asdc_artifacts_id = (SELECT MAX(asdc_artifacts_id) configfileid  "
					+ " FROM ASDC_ARTIFACTS  " + " WHERE artifact_name = '" + templateName + "' ) ; ";

			status = serviceLogic.query("SQL", false, null, key, prefix, null, ctx);
		}
		return status;
	}

	public QueryStatus getConfigureActionDGByVnfTypeNAction(SvcLogicContext ctx, String prefix)
			throws SvcLogicException {
		QueryStatus status = null;
		if (serviceLogic != null && ctx != null) {
			String key = "SELECT * " + " FROM CONFIGURE_ACTION_DG "
					+ " where vnf_type = $vnf-type and action = $request-action ; ";

			status = serviceLogic.query("SQL", false, null, key, prefix, null, ctx);
		}
		return status;
	}

	public QueryStatus getConfigureActionDGByVnfType(SvcLogicContext ctx, String prefix) throws SvcLogicException {
		QueryStatus status = null;
		if (serviceLogic != null && ctx != null) {
			String key = "SELECT * " + " FROM CONFIGURE_ACTION_DG "
					+ " where vnf_type = $vnf-type and action IS NULL ; ";

			status = serviceLogic.query("SQL", false, null, key, prefix, null, ctx);
		}
		return status;
	}

	public QueryStatus getMaxConfigFileId(SvcLogicContext ctx, String prefix, String fileCategory)
			throws SvcLogicException {
		QueryStatus status = null;
		if (serviceLogic != null && ctx != null) {
			String key = "SELECT MAX(config_file_id) configfileid " + " FROM CONFIGFILES " + " WHERE file_category = '"
					+ fileCategory + "'" + " AND vnf_id =  $vnf-id  AND vm_name = $vm-name ; ";

			status = serviceLogic.query("SQL", false, null, key, prefix, null, ctx);
		}
		return status;
	}

	public QueryStatus saveConfigFiles(SvcLogicContext ctx, String prefix) throws SvcLogicException {

		QueryStatus status = null;

		if (serviceLogic != null && ctx != null) {
			String key = "INSERT INTO CONFIGFILES " + " SET data_source        = $data-source , "
					+ " service_instance_id =  $service-instance-id ," + " action              =   $request-action ,"
					+ " vnf_type            = 	$vnf-type ," + " vnfc_type           = 	$vnfc-type ,"
					+ " vnf_id              =   $vnf-id , " + " vnf_name            =   $vnf-name ,"
					+ " vm_name            =   $vm-name ," + " file_category 		=  $file-category ,"
					+ " file_content        =  $file-content ; ";

			status = serviceLogic.save("SQL", false, false, key, null, prefix, ctx);

		}
		return status;

	}

	public QueryStatus savePrepareRelationship(SvcLogicContext ctx, String prefix, String fileId, String sdcInd)
			throws SvcLogicException {

		QueryStatus status = null;
		String key = null;

		if (serviceLogic != null && ctx != null) {

			if ("Y".equals(sdcInd))

				key = "INSERT INTO PREPARE_FILE_RELATIONSHIP " + " SET service_instance_id =  $service-instance-id , "
						+ "   request_id         = $request-id , " + "  asdc_artifacts_id        =  " + fileId + " ;";
			else
				key = "INSERT INTO PREPARE_FILE_RELATIONSHIP " + " SET service_instance_id =  $service-instance-id , "
						+ "   request_id         = $request-id , " + "  config_file_id        =  " + fileId + " ;";

			status = serviceLogic.save("SQL", false, false, key, null, prefix, ctx);

			log.info("DGGeneralDBService.savePrepareRelationship()" + ctx.getAttributeKeySet());
		}
		return status;

	}

	public void cleanContextPropertyByPrefix(SvcLogicContext ctx, String prefix) {
		if (ctx != null && ctx.getAttributeKeySet() != null && StringUtils.isNotBlank(prefix)) {

			Set<String> keySet = ctx.getAttributeKeySet();
			for (String key : keySet) {
				if (StringUtils.isNotBlank(key) && key.startsWith(prefix = ".")) {
					ctx.getAttributeKeySet().remove(key);
				}
			}
		}
	}

	public QueryStatus saveUploadConfig(SvcLogicContext ctx, String prefix) throws SvcLogicException {

		QueryStatus status = null;

		if (serviceLogic != null && ctx != null) {
			String key = "INSERT INTO UPLOAD_CONFIG " + " SET request_id = $request-id , "
					+ " action = $request-action , " + " originator_id = $originator-id , " + " vnf_id =  $vnf-id , "
					+ " vnf_name = $vnf-name ,  " + " vm_name =  $vm-name ,  "
					+ " host_ip_address = $vnf-host-ip-address , " + " vnf_type            = 	$vnf-type , "
					+ " vnfc_type           = 	$vnfc-type , " + " config_indicator 		=  'Current' , "
					+ " content        =  $tmp.escaped.devicerunningconfig ; ";

			status = serviceLogic.save("SQL", false, false, key, null, prefix, ctx);

			log.info("DGGeneralDBService.saveUploadConfig()" + ctx.getAttributeKeySet());

		}
		return status;

	}

	/*public QueryStatus getMaxUploadConfigFileId(SvcLogicContext ctx, String prefix) throws SvcLogicException {
		QueryStatus status = null;
		if (serviceLogic != null && ctx != null) {
			String key = "SELECT MAX(upload_config_id) uploadconfigid " + " FROM UPLOAD_CONFIG "
					+ " WHERE vnf_id =  $vnf-id  AND vm_name = $vm-name ; ";

			status = serviceLogic.query("SQL", false, null, key, prefix, null, ctx);
			log.info("DGGeneralDBService.getMaxUploadConfigFileId()" + ctx.getAttributeKeySet());
		}
		return status;
	}*/

	public QueryStatus updateUploadConfig(SvcLogicContext ctx, String prefix, int maxId) throws SvcLogicException {
		QueryStatus status = null;
		if (serviceLogic != null && ctx != null) {
			String key = "UPDATE UPLOAD_CONFIG " + " SET  config_indicator 		=  null "
					+ " WHERE upload_config_id != " + maxId + " AND config_indicator 		=  'Current' "
					+ " AND vnf_id = $vnf-id " + " AND vm_name =  $vm-name ; ";

			status = serviceLogic.save("SQL", false, false, key, null, prefix, ctx);

			log.info("DGGeneralDBService.updateUploadConfig()" + ctx.getAttributeKeySet());

		}
		return status;

	}

	
	public QueryStatus getTemplateByArtifactType(SvcLogicContext ctx, String prefix, String fileCategory, String artifactType)
			throws SvcLogicException {
		QueryStatus status = null;
		if (serviceLogic != null && ctx != null) {
			String key = "SELECT artifact_content file_content , asdc_artifacts_id config_file_id "
					+ " FROM ASDC_ARTIFACTS "
					+ " WHERE asdc_artifacts_id = (SELECT MAX(a.asdc_artifacts_id) configfileid  "
					+ " FROM ASDC_ARTIFACTS a, ASDC_REFERENCE b " + " WHERE a.artifact_name = b.artifact_name "
					+ " AND file_category =  '" + fileCategory + "'" + " AND action =  $request-action "
					+ " AND artifactType =  '" + artifactType + "'"	+ " AND vnf_type =  $vnf-type ) ; ";

			status = serviceLogic.query("SQL", false, null, key, prefix, null, ctx);
		}
		return status;
	}
	
	
	public QueryStatus getConfigFilesByVnfVmNCategory(SvcLogicContext ctx, String prefix, String fileCategory, String vnfId, String vmName)
			throws SvcLogicException {
		QueryStatus status = null;
		if (serviceLogic != null && ctx != null) {
			
			String key = "SELECT  file_content ,  config_file_id "
					+ " FROM CONFIGFILES "
					+ " WHERE config_file_id = ( SELECT MAX(config_file_id) configfileid " + " FROM CONFIGFILES " 
					+ " WHERE file_category = '"	+ fileCategory + "'" 
					+ " AND vnf_id =  '" + vnfId + "'" 
					+ " AND vm_name = '" + vmName + "' ) ; ";
			
			
			status = serviceLogic.query("SQL", false, null, key, prefix, null, ctx);
		}
		return status;
	}
	
	
	public QueryStatus getDownloadConfigTemplateByVnf(SvcLogicContext ctx, String prefix)
			throws SvcLogicException {
		QueryStatus status = null;
		if (serviceLogic != null && ctx != null) {
			String key = "SELECT * FROM DOWNLOAD_CONFIG_TEMPLATE  WHERE vnf_type = $vnf-type ; ";
			status = serviceLogic.query("SQL", false, null, key, prefix, null, ctx);
		}
		return status;
	}
	
	
	
	public QueryStatus saveConfigTransactionLog(SvcLogicContext ctx, String prefix) throws SvcLogicException {

		QueryStatus status = null;

		if (serviceLogic != null && ctx != null) {
				
		
				String key = "INSERT INTO CONFIG_TRANSACTION_LOG " + " SET request_id = $request-id , "
				+ " message_type = $log-message-type , "
				+ " message = $log-message ;";


				status = serviceLogic.save("SQL", false, false, key, null, prefix, ctx);

		

		}
		return status;

	}

	
	public QueryStatus getVnfcReferenceByVnfcTypeNAction(SvcLogicContext ctx, String prefix)
			throws SvcLogicException {
		QueryStatus status = null;
		if (serviceLogic != null && ctx != null) {
			
			String key = "SELECT  * "
					+ " FROM VNFC_REFERENCE "
					+ " WHERE vnf_type =  $vnf-type " 
					+ " AND vnfc_type = $vnfc-type "
					+ " AND action =  $request-action "
					+ " ORDER BY vm_instance, vnfc_instance ; ";
			
			
			status = serviceLogic.query("SQL", false, null, key, prefix, null, ctx);
		}
		return status;
	}

	
	public QueryStatus getVnfcReferenceByVnfTypeNAction(SvcLogicContext ctx, String prefix)
			throws SvcLogicException {
		QueryStatus status = null;
		if (serviceLogic != null && ctx != null) {
			
			String key = "SELECT  * "
					+ " FROM VNFC_REFERENCE "
					+ " WHERE vnf_type =  $vnf-type " 
					+ " AND action =  $request-action   "
					+ " ORDER BY vm_instance, vnfc_instance ; ";
			
			status = serviceLogic.query("SQL", false, null, key, prefix, null, ctx);
		}
		return status;
	}
	
	
	public QueryStatus getUploadConfigInfo(SvcLogicContext ctx, String prefix)
			throws SvcLogicException {
		QueryStatus status = null;
		if (serviceLogic != null && ctx != null) {
			
			String key = "SELECT  * , UNIX_TIMESTAMP(UPLOAD_DATE) UPLOAD_TIMESTAMP "
					+ " FROM UPLOAD_CONFIG "
					+ " WHERE upload_config_id = " + 
					"( SELECT MAX(upload_config_id) uploadconfigid " + " FROM UPLOAD_CONFIG "
					+ " WHERE vnf_id =  $vnf-id  AND vm_name = $vm-name ) ; ";
				
			status = serviceLogic.query("SQL", false, null, key, prefix, null, ctx);
		}
		return status;
	}
	 public String getCapability(SvcLogicContext ctx, String vnf_type) throws SvcLogicException {

         //{"capabilities":{"vnfc":[],"vm":[],"vf-module":[],"vnf":["ConfigureTest","ConfigModify","HealthCheck"]}}
         String fn = "getCapability ";
         QueryStatus status = null;
         SvcLogicContext localContext = new SvcLogicContext();
         localContext.setAttribute("vnf-type", vnf_type);
         if (serviceLogic != null && localContext  != null) {
                 String queryString = "select max(internal_version) as maxInternalVersion, artifact_name as artifactName from ASDC_ARTIFACTS " +
                                  " where artifact_name in (select artifact_name from ASDC_REFERENCE  where vnf_type= $vnf-type "  +
                             " and file_category = 'capability' )" ;

                 log.info(fn + "Query String : " + queryString);
                 status = serviceLogic.query("SQL", false, null, queryString, null, null, localContext);

                 if(status.toString().equals("FAILURE"))
                         throw new SvcLogicException("Error - while getting capabilitiesData ");

                 String queryString1 = "select artifact_content from ASDC_ARTIFACTS  "  +
                                 " where artifact_name = $artifactName  and internal_version = $maxInternalVersion ";

                 log.debug(fn + "Query String : " + queryString1);
                 status = serviceLogic.query("SQL", false, null, queryString1, null, null, localContext);
                 if(status.toString().equals("FAILURE"))
                         throw new SvcLogicException("Error - while getting capabilitiesData ");
         }

         return localContext.getAttribute("artifact-content");
 }

}
