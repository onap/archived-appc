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

package org.openecomp.appc.artifact.handler.dbservices;

import java.sql.SQLException;
import java.util.HashMap;

import org.openecomp.appc.artifact.handler.utils.AsdcArtifactHandlerConstants;
import org.openecomp.sdnc.sli.SvcLogicContext;
import org.openecomp.sdnc.sli.SvcLogicException;
import org.openecomp.sdnc.sli.SvcLogicResource;
import org.openecomp.sdnc.sli.SvcLogicResource.QueryStatus;
import org.openecomp.sdnc.sli.resource.sql.SqlResource;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class DBService {

	private static final EELFLogger log = EELFManager.getInstance().getLogger(DBService.class);
	private SvcLogicResource serviceLogic;
	private static DBService dgGeneralDBService = null;
	public static DBService initialise() {
		if (dgGeneralDBService == null) {
			dgGeneralDBService = new DBService();
		}
		return dgGeneralDBService;
	}
	private DBService() {
		if (serviceLogic == null) {
			serviceLogic = new SqlResource();
		}
	}

	public String getInternalVersionNumber(SvcLogicContext ctx, String artifactName, String prefix) throws SvcLogicException {
		String fn = "DBService.getInternalVersionNumber";
		QueryStatus status = null;	
		String  artifactInternalVersion = null;
		if (serviceLogic != null && ctx != null) {	
			String key = "select max(internal_version) as maximum from ASDC_ARTIFACTS  WHERE ARTIFACT_NAME = '" + artifactName + "'";	
			log.info("Getting internal Versoin :" + key);
			status = serviceLogic.query("SQL", false, null, key, prefix, null, ctx);
			if(status.toString().equals("FAILURE"))
				throw new SvcLogicException("Error - getting internal Artifact Number");
			artifactInternalVersion = ctx.getAttribute("maximum");
			log.info("Internal Version received as : " + artifactInternalVersion);
			log.info("Internal Version received as1 : " + ctx.getAttribute("max(internal_version)"));
			log.info("Internal Version received as1 : " + ctx.getAttribute("max"));
			log.info("Internal Version received as1 : " + ctx.getAttribute("internal_version"));
			log.info("Internal Version received as1 : " + ctx.getAttributeKeySet().toString());
		}
		return artifactInternalVersion;
	}
	public String getArtifactID(SvcLogicContext ctx, String artifactName) throws SvcLogicException {
		String fn = "DBService.getArtifactID";
		QueryStatus status = null;	
		String  artifactID = null;
		if (serviceLogic != null && ctx != null) {	
			String key = "select max(ASDC_ARTIFACTS_ID) as id from ASDC_ARTIFACTS  WHERE ARTIFACT_NAME = '" + artifactName + "'";	
			log.info("Getting Artifact ID String :" + key);
			status = serviceLogic.query("SQL", false, null, key, null, null, ctx);
			if(status.toString().equals("FAILURE"))
				throw new SvcLogicException("Error - getting  Artifact ID from database");
			artifactID = ctx.getAttribute("id");
			log.info("ASDC_ARTIFACTS_ID received as : " + ctx.getAttribute("id"));
		}
		return artifactID;
	}
	public QueryStatus saveArtifacts(SvcLogicContext ctx, int intversion) throws SvcLogicException {
		String fn = "DBService.saveArtifacts";
		QueryStatus status = null;
		if (serviceLogic != null && ctx != null) {
			String key = "INSERT INTO ASDC_ARTIFACTS " +
					"SET SERVICE_UUID	=  $service-uuid , " + 
					" DISTRIBUTION_ID	=  $distribution-id ," + 
					" SERVICE_NAME	=  $service-name ," + 
					" SERVICE_DESCRIPTION	=  $service-description ," + 
					" RESOURCE_UUID	= $resource-uuid ," + 
					" RESOURCE_INSTANCE_NAME	= $resource-instance-name ," + 
					" RESOURCE_NAME	= $resource-name ," + 
					" RESOURCE_VERSION	= $resource-version ," + 
					" RESOURCE_TYPE	= $resource-type ," + 
					" ARTIFACT_UUID	= $artifact-uuid ," + 
					" ARTIFACT_TYPE	= $artifact-type ," + 
					" ARTIFACT_VERSION	= $artifact-version ," + 
					" ARTIFACT_DESCRIPTION	= $artifact-description ," + 
					" INTERNAL_VERSION	= " + intversion + "," + 
					" ARTIFACT_NAME       =  $artifact-name ," + 
					" ARTIFACT_CONTENT    =  $artifact-contents "  ;

			status = serviceLogic.save("SQL", false, false, key, null, null, ctx);
			if(status.toString().equals("FAILURE"))
				throw new SvcLogicException("Error While processing storing Artifact: " +ctx.getAttribute(AsdcArtifactHandlerConstants.ARTIFACT_NAME));
		}
		return status;

	}
	public QueryStatus logData(SvcLogicContext ctx, String prefix) throws SvcLogicException {
		String fn = "DBService.saveReferenceData";
		QueryStatus status = null;
		if (serviceLogic != null && ctx != null) {
			String key = "INSERT INTO CONFIG_TRANSACTION_LOG " + " SET request_id = $request-id , "
					+ " message_type = $log-message-type , "
					+ " message = $log-message ;";
			status = serviceLogic.save("SQL", false, false, key, null, prefix, ctx);
			if(status.toString().equals("FAILURE"))
				throw new SvcLogicException("Error while loging data");

		}
		return status;
	}

	public void processConfigureActionDg(SvcLogicContext context, boolean isUpdate) {
		String fn = "DBService.processConfigureActionDg";
		log.info("Update Parameter for ASDC Reference " + isUpdate );
		String key = "";
		QueryStatus status = null;
		if(isUpdate);

	}

	public void processAsdcReferences(SvcLogicContext context, boolean isUpdate) throws SvcLogicException {
		String fn = "DBService.processAsdcReferences";
		String key = "";
		QueryStatus status = null;
		
		if (isUpdate && AsdcArtifactHandlerConstants.FILE_CATEGORY.equals(AsdcArtifactHandlerConstants.CAPABILITY)) {
			key = "update " + AsdcArtifactHandlerConstants.DB_ASDC_REFERENCE + "  set ARTIFACT_NAME = $" + AsdcArtifactHandlerConstants.ARTIFACT_NAME + 
			" where VNFC_TYPE = $" + AsdcArtifactHandlerConstants.VNFC_TYPE + 
			" and FILE_CATEGORY = $" + AsdcArtifactHandlerConstants.FILE_CATEGORY +
			" and ACTION = null";
		}
		else if(isUpdate)
			key = "update " + AsdcArtifactHandlerConstants.DB_ASDC_REFERENCE + "  set ARTIFACT_NAME = $" + AsdcArtifactHandlerConstants.ARTIFACT_NAME + 
			" where VNFC_TYPE = $" + AsdcArtifactHandlerConstants.VNFC_TYPE + 
			" and FILE_CATEGORY = $" + AsdcArtifactHandlerConstants.FILE_CATEGORY + 
			" and ACTION = $" + AsdcArtifactHandlerConstants.ACTION ;

		else { 
			if (AsdcArtifactHandlerConstants.FILE_CATEGORY.equals(AsdcArtifactHandlerConstants.CAPABILITY)) {
				key = "insert into " + AsdcArtifactHandlerConstants.DB_ASDC_REFERENCE +  
				" set VNFC_TYPE = null "  +
				" , FILE_CATEGORY = $" + AsdcArtifactHandlerConstants.FILE_CATEGORY +
				" , VNF_TYPE = $" + AsdcArtifactHandlerConstants.VNF_TYPE +
				" , ACTION = null "  +
				" , ARTIFACT_TYPE = null " +
				" , ARTIFACT_NAME = $" + AsdcArtifactHandlerConstants.ARTIFACT_NAME ;
			}
			else {
				key = "insert into " + AsdcArtifactHandlerConstants.DB_ASDC_REFERENCE +  
				" set VNFC_TYPE = $" + AsdcArtifactHandlerConstants.VNFC_TYPE +
				" , FILE_CATEGORY = $" + AsdcArtifactHandlerConstants.FILE_CATEGORY +
				" , VNF_TYPE = $" + AsdcArtifactHandlerConstants.VNF_TYPE +
				" , ACTION = $" + AsdcArtifactHandlerConstants.ACTION +
				" , ARTIFACT_TYPE = $" + AsdcArtifactHandlerConstants.ARTIFACT_TYPE +
				" , ARTIFACT_NAME = $" + AsdcArtifactHandlerConstants.ARTIFACT_NAME ;
			}
		}
		if (serviceLogic != null && context != null) {	
			log.info("Insert Key: " + key);
			status = serviceLogic.save("SQL", false, false, key, null, null, context);
			if(status.toString().equals("FAILURE"))
				throw new SvcLogicException("Error While processing asdc_reference table ");
		}
	}

	public boolean isArtifactUpdateRequired(SvcLogicContext context, String db) throws SvcLogicException, SQLException  {
		String fn = "DBService.isArtifactUpdateRequired";
		log.info("Checking if Update required for this data" );

		log.info("db" + db);
		log.info("ACTION=" + context.getAttribute(AsdcArtifactHandlerConstants.ACTION));
		log.info("VNFC_TYPE=" + context.getAttribute(AsdcArtifactHandlerConstants.VNFC_TYPE));
		log.info("VNFC_INSTANCE=" + context.getAttribute(AsdcArtifactHandlerConstants.VNFC_INSTANCE));
		log.info("VM_INSTANCE=" + context.getAttribute(AsdcArtifactHandlerConstants.VM_INSTANCE));
		log.info("VNF_TYPE=" + context.getAttribute(AsdcArtifactHandlerConstants.VNF_TYPE));
		String whereClause =  "";

		QueryStatus status = null;	
	/*	if(context.getAttribute(AsdcArtifactHandlerConstants.ARTIFACT_NAME) !=null &&
				context.getAttribute(AsdcArtifactHandlerConstants.ARTIFACT_NAME).toLowerCase().startsWith(AsdcArtifactHandlerConstants.PD))
			whereClause = " where artifact_name = $" + AsdcArtifactHandlerConstants.ARTIFACT_NAME 
			+ " and vnf_type = 'DummyVnf' ";
		else*/
			whereClause = " where VNF_TYPE = $" +  AsdcArtifactHandlerConstants.VNF_TYPE;
		
		if (db !=null && db.equals(AsdcArtifactHandlerConstants.DB_ASDC_REFERENCE) && 
			context.getAttribute(AsdcArtifactHandlerConstants.FILE_CATEGORY).equals(AsdcArtifactHandlerConstants.CAPABILITY) &&
			context.getAttribute(AsdcArtifactHandlerConstants.ACTION) ==null) {
				whereClause = whereClause + " and FILE_CATEGORY = $" + AsdcArtifactHandlerConstants.FILE_CATEGORY ;
		}
			
		else if(db !=null && db.equals(AsdcArtifactHandlerConstants.DB_ASDC_REFERENCE)) {			
			whereClause = whereClause + " and VNFC_TYPE = $" + AsdcArtifactHandlerConstants.VNFC_TYPE
					+ " and FILE_CATEGORY = $" + AsdcArtifactHandlerConstants.FILE_CATEGORY  
					+ " and ACTION = $" + AsdcArtifactHandlerConstants.ACTION;
		}

		else if(db.equals(AsdcArtifactHandlerConstants.DB_DOWNLOAD_DG_REFERENCE)) {
			whereClause = " where PROTOCOL = $" + AsdcArtifactHandlerConstants.DEVICE_PROTOCOL;
		}
		else if(db.equals(AsdcArtifactHandlerConstants.DB_CONFIG_ACTION_DG)) {
			whereClause = whereClause + " and ACTION = $" + AsdcArtifactHandlerConstants.ACTION;
		}
		else if(db.equals(AsdcArtifactHandlerConstants.DB_VNFC_REFERENCE)){
			int vm_instance = -1  ;
			if(context.getAttribute(AsdcArtifactHandlerConstants.VM_INSTANCE) !=null)
				vm_instance = Integer.parseInt(context.getAttribute(AsdcArtifactHandlerConstants.VM_INSTANCE));
			int vnfc_instance = -1  ;
			if(context.getAttribute(AsdcArtifactHandlerConstants.VNFC_INSTANCE) !=null)
				vnfc_instance = Integer.parseInt(context.getAttribute(AsdcArtifactHandlerConstants.VNFC_INSTANCE));
			whereClause = whereClause + " and ACTION = $" + AsdcArtifactHandlerConstants.ACTION 
					+ " and VNFC_TYPE = $" + AsdcArtifactHandlerConstants.VNFC_TYPE 
					+ " and VNFC_INSTANCE = $" + AsdcArtifactHandlerConstants.VNFC_INSTANCE 
					+ " and VM_INSTANCE = $" + AsdcArtifactHandlerConstants.VM_INSTANCE ;

		}
		if (serviceLogic != null && context != null) {	
			String key = "select COUNT(*) from " + db + whereClause ;		
			log.info("SELECT String : " + key);
			status = serviceLogic.query("SQL", false, null, key, null, null, context);
			if(status.toString().equals("FAILURE")){
				throw new SvcLogicException("Error while reading data from " + db );
			}
			String count = context.getAttribute("COUNT(*)");
			log.info("Number of row Returned : " + count + ": " +  status + ":");
			if(count !=null && Integer.parseInt(count) > 0){				
				context.setAttribute(count, null);
				return true;
			}
			else
				return false;
		}
		return false;
	}

	public void processDeviceInterfaceProtocol(SvcLogicContext context, boolean isUpdate) throws SvcLogicException {
		String fn = "DBService.processDeviceInterfaceProtocol";
		log.info("Starting DB operation for Device Interface Protocol " + isUpdate );
		String key = "";
		QueryStatus status = null;
		if(isUpdate)
			key = "update " + AsdcArtifactHandlerConstants.DB_DEVICE_INTERFACE_PROTOCOL + 
			" set PROTOCOL = $" + AsdcArtifactHandlerConstants.DEVICE_PROTOCOL + 
			" , DG_RPC = 'getDeviceRunningConfig' " +  
			" , MODULE = 'APPC' " + 
			" where VNF_TYPE = $" + AsdcArtifactHandlerConstants.VNF_TYPE ;
		else
			key = "insert into " + AsdcArtifactHandlerConstants.DB_DEVICE_INTERFACE_PROTOCOL+ 
			" set  VNF_TYPE = $" + AsdcArtifactHandlerConstants.VNF_TYPE + 
			" , PROTOCOL = $" + AsdcArtifactHandlerConstants.DEVICE_PROTOCOL + 
			" , DG_RPC = 'getDeviceRunningConfig' " +  
			" , MODULE = 'APPC' " ; 

		if (serviceLogic != null && context != null) {	

			status = serviceLogic.save("SQL", false, false, key, null, null, context);
			if(status.toString().equals("FAILURE"))
				throw new SvcLogicException("Error While processing DEVICE_INTERFACE_PROTOCOL table ");
		}

	}

	public void processDeviceAuthentication(SvcLogicContext context, boolean isUpdate) throws SvcLogicException {
		String fn = "DBService.processDeviceAuthentication";
		log.info(fn + "Starting DB operation for Device Authentication " + isUpdate );
		String key = "";
		QueryStatus status = null;
		if(isUpdate)
			key = "update " + AsdcArtifactHandlerConstants.DB_DEVICE_AUTHENTICATION + 
			" set USER_NAME = $" + AsdcArtifactHandlerConstants.USER_NAME + 
			" , PASSWORD = 'dummy' " +
			" , PORT_NUMBER = $" + AsdcArtifactHandlerConstants.PORT_NUMBER +
			" where VNF_TYPE = $" + AsdcArtifactHandlerConstants.VNF_TYPE ;
		else
			key = "insert into " + AsdcArtifactHandlerConstants.DB_DEVICE_AUTHENTICATION+ 
			" set  VNF_TYPE = $" + AsdcArtifactHandlerConstants.VNF_TYPE + 
			" , USER_NAME = $" + AsdcArtifactHandlerConstants.USER_NAME + 
			" , PASSWORD = 'dummy' " +
			" , PORT_NUMBER = $" + AsdcArtifactHandlerConstants.PORT_NUMBER;

		if (serviceLogic != null && context != null) {	
			status = serviceLogic.save("SQL", false, false, key, null, null, context);
			if(status.toString().equals("FAILURE"))
				throw new SvcLogicException("Error While processing DEVICE_AUTHENTICATION table ");
		}	
	}

	public void processVnfcReference(SvcLogicContext context, boolean isUpdate) throws SvcLogicException {
		String fn = "DBService.processVnfcReference";
		log.info(fn + "Starting DB operation for Vnfc Reference " + isUpdate );
		String key = "";
		int vm_instance = -1  ;
		if(context.getAttribute(AsdcArtifactHandlerConstants.VM_INSTANCE) !=null)
			vm_instance = Integer.parseInt(context.getAttribute(AsdcArtifactHandlerConstants.VM_INSTANCE));
		int vnfc_instance = -1  ;
		if(context.getAttribute(AsdcArtifactHandlerConstants.VNFC_INSTANCE) !=null)
			vnfc_instance = Integer.parseInt(context.getAttribute(AsdcArtifactHandlerConstants.VNFC_INSTANCE));
		QueryStatus status = null;
		if(isUpdate)
			key = "update " + AsdcArtifactHandlerConstants.DB_VNFC_REFERENCE + 
			" set VM_INSTANCE = " + vm_instance   + 
			" , VNFC_INSTANCE = " + vnfc_instance + 
			" , VNFC_TYPE = $" + AsdcArtifactHandlerConstants.VNFC_TYPE +
			" , VNFC_FUNCTION_CODE = $" + AsdcArtifactHandlerConstants.VNFC_FUNCTION_CODE +
			" , GROUP_NOTATION_TYPE = $" + AsdcArtifactHandlerConstants.GROUP_NOTATION_TYPE +
			" , GROUP_NOTATION_VALUE = $" + AsdcArtifactHandlerConstants.GROUP_NOTATION_VALUE  + 	
			" , IPADDRESS_V4_OAM_VIP = $" + AsdcArtifactHandlerConstants.IPADDRESS_V4_OAM_VIP  + 
			" where VNF_TYPE = $" + AsdcArtifactHandlerConstants.VNF_TYPE +
			" and ACTION = $" + AsdcArtifactHandlerConstants.ACTION +
			" and VNFC_TYPE = $" + AsdcArtifactHandlerConstants.VNFC_TYPE  + 
			" and VNFC_INSTANCE = $" + AsdcArtifactHandlerConstants.VNFC_INSTANCE 
			+ " and VM_INSTANCE = $" + AsdcArtifactHandlerConstants.VM_INSTANCE ;
		else
			key = "insert into " + AsdcArtifactHandlerConstants.DB_VNFC_REFERENCE+ 
			" set  VNF_TYPE = $" + AsdcArtifactHandlerConstants.VNF_TYPE + 
			" , ACTION = $" + AsdcArtifactHandlerConstants.ACTION + 
			" , VM_INSTANCE = $" + AsdcArtifactHandlerConstants.VM_INSTANCE + 
			" , VNFC_INSTANCE = $" + AsdcArtifactHandlerConstants.VNFC_INSTANCE + 
			" , VNFC_TYPE = $" + AsdcArtifactHandlerConstants.VNFC_TYPE +
			" , VNFC_FUNCTION_CODE = $" + AsdcArtifactHandlerConstants.VNFC_FUNCTION_CODE +
			" , GROUP_NOTATION_TYPE = $" + AsdcArtifactHandlerConstants.GROUP_NOTATION_TYPE +
			" , IPADDRESS_V4_OAM_VIP = $" + AsdcArtifactHandlerConstants.IPADDRESS_V4_OAM_VIP  + 
			" , GROUP_NOTATION_VALUE = $" + AsdcArtifactHandlerConstants.GROUP_NOTATION_VALUE ;	

		if (serviceLogic != null && context != null) {	
			status = serviceLogic.save("SQL", false, false, key, null, null, context);
			if(status.toString().equals("FAILURE"))
				throw new SvcLogicException("Error While processing VNFC_REFERENCE table ");
		}
	}

	public void processDownloadDgReference(SvcLogicContext context, boolean isUpdate) throws SvcLogicException, SQLException {
		String fn = "DBService.processDownloadDgReference";
		log.info(fn + "Starting DB operation for Download DG Reference " + isUpdate );
		String key = "";
		QueryStatus status = null;

		if(isUpdate)
			key = "update " + AsdcArtifactHandlerConstants.DB_DOWNLOAD_DG_REFERENCE + 
			" set DOWNLOAD_CONFIG_DG = $" + AsdcArtifactHandlerConstants.DOWNLOAD_DG_REFERENCE + 
			" where PROTOCOL = $" + AsdcArtifactHandlerConstants.DEVICE_PROTOCOL ;
		else 
			key = "insert into " + AsdcArtifactHandlerConstants.DB_DOWNLOAD_DG_REFERENCE+ 
			" set DOWNLOAD_CONFIG_DG = $" + AsdcArtifactHandlerConstants.DOWNLOAD_DG_REFERENCE + 
			" , PROTOCOL = $" + AsdcArtifactHandlerConstants.DEVICE_PROTOCOL ;

		if (serviceLogic != null && context != null)
			status = serviceLogic.save("SQL", false, false, key, null, null, context);
		if(status.toString().equals("FAILURE"))
			throw new SvcLogicException("Error While processing DOWNLOAD_DG_REFERENCE table ");
	}
	public void processConfigActionDg(SvcLogicContext context, boolean isUpdate) throws SvcLogicException
	{
		String fn = "DBService.processConfigActionDg";
		log.info(fn + "Starting DB operation for Config DG Action " + isUpdate );
		String key = "";
		QueryStatus status = null;

		if(context.getAttribute(AsdcArtifactHandlerConstants.DOWNLOAD_DG_REFERENCE) != null && 
				context.getAttribute(AsdcArtifactHandlerConstants.DOWNLOAD_DG_REFERENCE).length() > 0){
			if(isUpdate)
				key = "update " + AsdcArtifactHandlerConstants.DB_CONFIG_ACTION_DG + 
				" set DOWNLOAD_CONFIG_DG = $" + AsdcArtifactHandlerConstants.DOWNLOAD_DG_REFERENCE + 
				" where ACTION = $" + AsdcArtifactHandlerConstants.ACTION  + 
				" and VNF_TYPE = $" + AsdcArtifactHandlerConstants.VNF_TYPE ;
			else 
				key = "insert into " + AsdcArtifactHandlerConstants.DB_CONFIG_ACTION_DG+ 
				" set DOWNLOAD_CONFIG_DG = $" + AsdcArtifactHandlerConstants.DOWNLOAD_DG_REFERENCE + 
				" , ACTION = $" + AsdcArtifactHandlerConstants.ACTION  + 
				" , VNF_TYPE = $" + AsdcArtifactHandlerConstants.VNF_TYPE ;

			if (serviceLogic != null && context != null)
				status = serviceLogic.save("SQL", false, false, key, null, null, context);
			if(status.toString().equals("FAILURE"))
				throw new SvcLogicException("Error While processing Configure DG Action table ");
		}
		else
			log.info("No Update required for Config DG Action");

	}

	public String  getModelDataInformationbyArtifactName(String artifact_name) throws SvcLogicException 
	{
		String fn = "DBService.getVnfData";
		String key = "";
		SvcLogicContext con = new SvcLogicContext();
		HashMap<String, String> modelData = new HashMap<String, String>();
		QueryStatus status = null;
		key = "select VNF_TYPE, VNFC_TYPE, ACTION, FILE_CATEGORY, ARTIFACT_TYPE from ASDC_REFERENCE where  ARTIFACT_NAME = " + artifact_name ;

		if (serviceLogic != null && con != null) {	
			log.info(fn + "select Key: " + key);
			status = serviceLogic.query("SQL", false, null, key, null, null, con);
			if(status.toString().equals("FAILURE"))
				throw new SvcLogicException("Error While processing is ArtifactUpdateRequiredforPD table ");		

		}

		log.info(fn + "Vnf_received :" + con.getAttribute("VNF_TYPE"));			

		return con.getAttribute("VNF_TYPE");

	}
	public void updateYangContents(SvcLogicContext context, String artifactId, String yangContents) throws SvcLogicException {
		String fn = "DBService.updateYangContents";
		log.info(fn + "Starting DB operation for  updateYangContents");
		String key = "";
		QueryStatus status = null;

			key = "update ASDC_ARTIFACTS " + 
				" set ARTIFACT_CONTENT = '" + yangContents  +  "'" + 
				" where ASDC_ARTIFACTS_ID = " + artifactId ;
				
			if (serviceLogic != null && context != null)
				status = serviceLogic.save("SQL", false, false, key, null, null, context);
			if(status.toString().equals("FAILURE"))
				throw new SvcLogicException("Error While processing Configure DG Action table ");		

	}
	
	
	public void insertProtocolReference(SvcLogicContext context, String vnfType, String protocol, String action, String action_level,
			String template) throws SvcLogicException {
		String fn = "DBService.insertProtocolReference";
		log.info(fn + "Starting DB operation for  insertProtocolReference");
		String key = "";
		QueryStatus status = null;

			key = "insert into PROTOCOL_REFERENCE (ACTION, VNF_TYPE, PROTOCOL, UPDATED_DATE, TEMPLATE, ACTION_LEVEL)" + 
				" values  (" +  
				 "'"+action +"', '"+ vnfType+"', '"+protocol+"', now(),'"+template+"', '"+action_level+"')";
				
			if (serviceLogic != null && context != null)
				status = serviceLogic.save("SQL", false, false, key, null, null, context);
			if(status.toString().equals("FAILURE"))
				throw new SvcLogicException("Error While processing insertProtocolReference ");	
		
	}
	
	


		
		
	}
