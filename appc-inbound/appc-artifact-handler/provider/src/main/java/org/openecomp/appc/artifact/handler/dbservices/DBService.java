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

package org.openecomp.appc.artifact.handler.dbservices;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.onap.ccsdk.sli.adaptors.resource.sql.SqlResource;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource.QueryStatus;
import org.openecomp.appc.artifact.handler.utils.SdcArtifactHandlerConstants;

import java.sql.SQLException;
import java.util.HashMap;

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

    protected DBService(SqlResource svcLogic) {
        if (serviceLogic == null) {
            serviceLogic = svcLogic;
        }
    }

    public String getInternalVersionNumber(SvcLogicContext ctx, String artifactName, String prefix)
            throws SvcLogicException {
        String fn = "DBService.getInternalVersionNumber";
        QueryStatus status = null;
        String artifactInternalVersion = null;
        if (serviceLogic != null && ctx != null) {
            String key = "select max(internal_version) as maximum from ASDC_ARTIFACTS  WHERE ARTIFACT_NAME = '"
                    + artifactName + "'";
            log.info("Getting internal Versoin :" + key);
            status = serviceLogic.query("SQL", false, null, key, prefix, null, ctx);
            if (status.toString().equals("FAILURE"))
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
        String artifactID = null;
        if (serviceLogic != null && ctx != null) {
            String key = "select max(ASDC_ARTIFACTS_ID) as id from ASDC_ARTIFACTS  WHERE ARTIFACT_NAME = '"
                    + artifactName + "'";
            log.info("Getting Artifact ID String :" + key);
            status = serviceLogic.query("SQL", false, null, key, null, null, ctx);
            if (status.toString().equals("FAILURE"))
                throw new SvcLogicException("Error - getting  Artifact ID from database");
            artifactID = ctx.getAttribute("id");
            log.info("SDC_ARTIFACTS_ID received as : " + ctx.getAttribute("id"));
        }
        return artifactID;
    }

    public QueryStatus saveArtifacts(SvcLogicContext ctx, int intversion) throws SvcLogicException {
        String fn = "DBService.saveArtifacts";
        QueryStatus status = null;
        if (serviceLogic != null && ctx != null) {
            String key = "INSERT INTO ASDC_ARTIFACTS " + "SET SERVICE_UUID    =  $service-uuid , "
                    + " DISTRIBUTION_ID    =  $distribution-id ," + " SERVICE_NAME    =  $service-name ,"
                    + " SERVICE_DESCRIPTION    =  $service-description ," + " RESOURCE_UUID    = $resource-uuid ,"
                    + " RESOURCE_INSTANCE_NAME    = $resource-instance-name ," + " RESOURCE_NAME    = $resource-name ,"
                    + " RESOURCE_VERSION    = $resource-version ," + " RESOURCE_TYPE    = $resource-type ,"
                    + " ARTIFACT_UUID    = $artifact-uuid ," + " ARTIFACT_TYPE    = $artifact-type ,"
                    + " ARTIFACT_VERSION    = $artifact-version ," + " ARTIFACT_DESCRIPTION    = $artifact-description ,"
                    + " INTERNAL_VERSION    = " + intversion + "," + " ARTIFACT_NAME       =  $artifact-name ,"
                    + " ARTIFACT_CONTENT    =  $artifact-contents ";

            status = serviceLogic.save("SQL", false, false, key, null, null, ctx);
            if (status.toString().equals("FAILURE"))
                throw new SvcLogicException("Error While processing storing Artifact: "
                        + ctx.getAttribute(SdcArtifactHandlerConstants.ARTIFACT_NAME));
        }
        return status;

    }

    public QueryStatus logData(SvcLogicContext ctx, String prefix) throws SvcLogicException {
        String fn = "DBService.saveReferenceData";
        QueryStatus status = null;
        if (serviceLogic != null && ctx != null) {
            String key = "INSERT INTO CONFIG_TRANSACTION_LOG " + " SET request_id = $request-id , "
                    + " message_type = $log-message-type , " + " message = $log-message ;";
            status = serviceLogic.save("SQL", false, false, key, null, prefix, ctx);
            if (status.toString().equals("FAILURE"))
                throw new SvcLogicException("Error while loging data");

        }
        return status;
    }

    public void processConfigureActionDg(SvcLogicContext context, boolean isUpdate) {
        String fn = "DBService.processConfigureActionDg";
        log.info("Update Parameter for SDC Reference " + isUpdate);
        String key = "";
        QueryStatus status = null;
        if (isUpdate)
            ;
    }

    public void processSdcReferences(SvcLogicContext context, boolean isUpdate) throws SvcLogicException {
        String fn = "DBService.processSdcReferences";
        String key = "";
        QueryStatus status = null;

        if (isUpdate && SdcArtifactHandlerConstants.FILE_CATEGORY.equals(SdcArtifactHandlerConstants.CAPABILITY)) {
            key = "update " + SdcArtifactHandlerConstants.DB_SDC_REFERENCE + "  set ARTIFACT_NAME = $"
                    + SdcArtifactHandlerConstants.ARTIFACT_NAME + " where VNFC_TYPE = $"
                    + SdcArtifactHandlerConstants.VNFC_TYPE + " and FILE_CATEGORY = $"
                    + SdcArtifactHandlerConstants.FILE_CATEGORY + " and ACTION = null";
        } else if (isUpdate)
            key = "update " + SdcArtifactHandlerConstants.DB_SDC_REFERENCE + "  set ARTIFACT_NAME = $"
                    + SdcArtifactHandlerConstants.ARTIFACT_NAME + " where VNFC_TYPE = $"
                    + SdcArtifactHandlerConstants.VNFC_TYPE + " and FILE_CATEGORY = $"
                    + SdcArtifactHandlerConstants.FILE_CATEGORY + " and ACTION = $"
                    + SdcArtifactHandlerConstants.ACTION;

        else {
            if (SdcArtifactHandlerConstants.FILE_CATEGORY.equals(SdcArtifactHandlerConstants.CAPABILITY)) {
                key = "insert into " + SdcArtifactHandlerConstants.DB_SDC_REFERENCE + " set VNFC_TYPE = null "
                        + " , FILE_CATEGORY = $" + SdcArtifactHandlerConstants.FILE_CATEGORY + " , VNF_TYPE = $"
                        + SdcArtifactHandlerConstants.VNF_TYPE + " , ACTION = null " + " , ARTIFACT_TYPE = null "
                        + " , ARTIFACT_NAME = $" + SdcArtifactHandlerConstants.ARTIFACT_NAME;
            } else {
                key = "insert into " + SdcArtifactHandlerConstants.DB_SDC_REFERENCE + " set VNFC_TYPE = $"
                        + SdcArtifactHandlerConstants.VNFC_TYPE + " , FILE_CATEGORY = $"
                        + SdcArtifactHandlerConstants.FILE_CATEGORY + " , VNF_TYPE = $"
                        + SdcArtifactHandlerConstants.VNF_TYPE + " , ACTION = $" + SdcArtifactHandlerConstants.ACTION
                        + " , ARTIFACT_TYPE = $" + SdcArtifactHandlerConstants.ARTIFACT_TYPE + " , ARTIFACT_NAME = $"
                        + SdcArtifactHandlerConstants.ARTIFACT_NAME;
            }
        }
        if (serviceLogic != null && context != null) {
            log.info("Insert Key: " + key);
            status = serviceLogic.save("SQL", false, false, key, null, null, context);
            if (status.toString().equals("FAILURE"))
                throw new SvcLogicException("Error While processing sdc_reference table ");
        }
    }

    public boolean isArtifactUpdateRequired(SvcLogicContext context, String db) throws SvcLogicException, SQLException {
        String fn = "DBService.isArtifactUpdateRequired";
        log.info("Checking if Update required for this data");

        log.info("db" + db);
        log.info("ACTION=" + context.getAttribute(SdcArtifactHandlerConstants.ACTION));
        log.info("VNFC_TYPE=" + context.getAttribute(SdcArtifactHandlerConstants.VNFC_TYPE));
        log.info("VNFC_INSTANCE=" + context.getAttribute(SdcArtifactHandlerConstants.VNFC_INSTANCE));
        log.info("VM_INSTANCE=" + context.getAttribute(SdcArtifactHandlerConstants.VM_INSTANCE));
        log.info("VNF_TYPE=" + context.getAttribute(SdcArtifactHandlerConstants.VNF_TYPE));
        String whereClause = "";

        QueryStatus status = null;
        whereClause = " where VNF_TYPE = $" + SdcArtifactHandlerConstants.VNF_TYPE;

        if (db != null) {
            if (db.equals(SdcArtifactHandlerConstants.DB_SDC_REFERENCE)
                    && context.getAttribute(SdcArtifactHandlerConstants.FILE_CATEGORY)
                    .equals(SdcArtifactHandlerConstants.CAPABILITY)
                    && context.getAttribute(SdcArtifactHandlerConstants.ACTION) == null) {
                whereClause = whereClause + " and FILE_CATEGORY = $" + SdcArtifactHandlerConstants.FILE_CATEGORY;
            }

            else if (db.equals(SdcArtifactHandlerConstants.DB_SDC_REFERENCE)) {
                whereClause = whereClause + " and VNFC_TYPE = $" + SdcArtifactHandlerConstants.VNFC_TYPE
                        + " and FILE_CATEGORY = $" + SdcArtifactHandlerConstants.FILE_CATEGORY + " and ACTION = $"
                        + SdcArtifactHandlerConstants.ACTION;
            }

            else if (db.equals(SdcArtifactHandlerConstants.DB_DOWNLOAD_DG_REFERENCE)) {
                whereClause = " where PROTOCOL = $" + SdcArtifactHandlerConstants.DEVICE_PROTOCOL;
            } else if (db.equals(SdcArtifactHandlerConstants.DB_CONFIG_ACTION_DG)) {
                whereClause = whereClause + " and ACTION = $" + SdcArtifactHandlerConstants.ACTION;
            } else if (db.equals(SdcArtifactHandlerConstants.DB_VNFC_REFERENCE)) {
                int vm_instance = -1;
                if (context.getAttribute(SdcArtifactHandlerConstants.VM_INSTANCE) != null)
                    vm_instance = Integer.parseInt(context.getAttribute(SdcArtifactHandlerConstants.VM_INSTANCE));
                int vnfc_instance = -1;
                if (context.getAttribute(SdcArtifactHandlerConstants.VNFC_INSTANCE) != null)
                    vnfc_instance = Integer.parseInt(context.getAttribute(SdcArtifactHandlerConstants.VNFC_INSTANCE));
                whereClause = whereClause + " and ACTION = $" + SdcArtifactHandlerConstants.ACTION + " and VNFC_TYPE = $"
                        + SdcArtifactHandlerConstants.VNFC_TYPE + " and VNFC_INSTANCE = $"
                        + SdcArtifactHandlerConstants.VNFC_INSTANCE + " and VM_INSTANCE = $"
                        + SdcArtifactHandlerConstants.VM_INSTANCE;

            }
        }

        if (serviceLogic != null && context != null) {
            String key = "select COUNT(*) from " + db + whereClause;
            log.info("SELECT String : " + key);
            status = serviceLogic.query("SQL", false, null, key, null, null, context);
            if (status.toString().equals("FAILURE")) {
                throw new SvcLogicException("Error while reading data from " + db);
            }
            String count = context.getAttribute("COUNT(*)");
            log.info("Number of row Returned : " + count + ": " + status + ":");
            if (count != null && Integer.parseInt(count) > 0) {
                context.setAttribute(count, null);
                return true;
            } else
                return false;
        }
        return false;
    }

    public void processDeviceInterfaceProtocol(SvcLogicContext context, boolean isUpdate) throws SvcLogicException {
        String fn = "DBService.processDeviceInterfaceProtocol";
        log.info("Starting DB operation for Device Interface Protocol " + isUpdate);
        String key = "";
        QueryStatus status = null;
        if (isUpdate)
            key = "update " + SdcArtifactHandlerConstants.DB_DEVICE_INTERFACE_PROTOCOL + " set PROTOCOL = $"
                    + SdcArtifactHandlerConstants.DEVICE_PROTOCOL + " , DG_RPC = 'getDeviceRunningConfig' "
                    + " , MODULE = 'APPC' " + " where VNF_TYPE = $" + SdcArtifactHandlerConstants.VNF_TYPE;
        else
            key = "insert into " + SdcArtifactHandlerConstants.DB_DEVICE_INTERFACE_PROTOCOL + " set  VNF_TYPE = $"
                    + SdcArtifactHandlerConstants.VNF_TYPE + " , PROTOCOL = $"
                    + SdcArtifactHandlerConstants.DEVICE_PROTOCOL + " , DG_RPC = 'getDeviceRunningConfig' "
                    + " , MODULE = 'APPC' ";

        if (serviceLogic != null && context != null) {

            status = serviceLogic.save("SQL", false, false, key, null, null, context);
            if (status.toString().equals("FAILURE"))
                throw new SvcLogicException("Error While processing DEVICE_INTERFACE_PROTOCOL table ");
        }

    }

    public void processDeviceAuthentication(SvcLogicContext context, boolean isUpdate) throws SvcLogicException {
        String fn = "DBService.processDeviceAuthentication";
        log.info(fn + "Starting DB operation for Device Authentication " + isUpdate);
        String key = "";
        QueryStatus status = null;
        if (isUpdate)
            key = "update " + SdcArtifactHandlerConstants.DB_DEVICE_AUTHENTICATION + " set USER_NAME = $"
                    + SdcArtifactHandlerConstants.USER_NAME +/* " , PASSWORD = 'dummy' " +*/ " , PORT_NUMBER = $"
                    + SdcArtifactHandlerConstants.PORT_NUMBER + " where VNF_TYPE = $"
                    + SdcArtifactHandlerConstants.VNF_TYPE;
        else
            key = "insert into " + SdcArtifactHandlerConstants.DB_DEVICE_AUTHENTICATION + " set  VNF_TYPE = $"
                    + SdcArtifactHandlerConstants.VNF_TYPE + " , USER_NAME = $" + SdcArtifactHandlerConstants.USER_NAME
                    +/* " , PASSWORD = 'dummy' " + */ " , PORT_NUMBER = $" + SdcArtifactHandlerConstants.PORT_NUMBER;

        if (serviceLogic != null && context != null) {
            status = serviceLogic.save("SQL", false, false, key, null, null, context);
            if (status.toString().equals("FAILURE"))
                throw new SvcLogicException("Error While processing DEVICE_AUTHENTICATION table ");
        }
    }

    public void processVnfcReference(SvcLogicContext context, boolean isUpdate) throws SvcLogicException {
        String fn = "DBService.processVnfcReference";
        log.info(fn + "Starting DB operation for Vnfc Reference " + isUpdate);
        String key = "";
        int vm_instance = -1;
        if (context.getAttribute(SdcArtifactHandlerConstants.VM_INSTANCE) != null)
            vm_instance = Integer.parseInt(context.getAttribute(SdcArtifactHandlerConstants.VM_INSTANCE));
        int vnfc_instance = -1;
        if (context.getAttribute(SdcArtifactHandlerConstants.VNFC_INSTANCE) != null)
            vnfc_instance = Integer.parseInt(context.getAttribute(SdcArtifactHandlerConstants.VNFC_INSTANCE));
        QueryStatus status = null;
        if (isUpdate)
            key = "update " + SdcArtifactHandlerConstants.DB_VNFC_REFERENCE + " set VM_INSTANCE = " + vm_instance
                    + " , VNFC_INSTANCE = " + vnfc_instance + " , VNFC_TYPE = $" + SdcArtifactHandlerConstants.VNFC_TYPE
                    + " , VNFC_FUNCTION_CODE = $" + SdcArtifactHandlerConstants.VNFC_FUNCTION_CODE
                    + " , GROUP_NOTATION_TYPE = $" + SdcArtifactHandlerConstants.GROUP_NOTATION_TYPE
                    + " , GROUP_NOTATION_VALUE = $" + SdcArtifactHandlerConstants.GROUP_NOTATION_VALUE
                    + " , IPADDRESS_V4_OAM_VIP = $" + SdcArtifactHandlerConstants.IPADDRESS_V4_OAM_VIP
                    + " where VNF_TYPE = $" + SdcArtifactHandlerConstants.VNF_TYPE + " and ACTION = $"
                    + SdcArtifactHandlerConstants.ACTION + " and VNFC_TYPE = $" + SdcArtifactHandlerConstants.VNFC_TYPE
                    + " and VNFC_INSTANCE = $" + SdcArtifactHandlerConstants.VNFC_INSTANCE + " and VM_INSTANCE = $"
                    + SdcArtifactHandlerConstants.VM_INSTANCE;
        else
            key = "insert into " + SdcArtifactHandlerConstants.DB_VNFC_REFERENCE + " set  VNF_TYPE = $"
                    + SdcArtifactHandlerConstants.VNF_TYPE + " , ACTION = $" + SdcArtifactHandlerConstants.ACTION
                    + " , VM_INSTANCE = $" + SdcArtifactHandlerConstants.VM_INSTANCE + " , VNFC_INSTANCE = $"
                    + SdcArtifactHandlerConstants.VNFC_INSTANCE + " , VNFC_TYPE = $"
                    + SdcArtifactHandlerConstants.VNFC_TYPE + " , VNFC_FUNCTION_CODE = $"
                    + SdcArtifactHandlerConstants.VNFC_FUNCTION_CODE + " , GROUP_NOTATION_TYPE = $"
                    + SdcArtifactHandlerConstants.GROUP_NOTATION_TYPE + " , IPADDRESS_V4_OAM_VIP = $"
                    + SdcArtifactHandlerConstants.IPADDRESS_V4_OAM_VIP + " , GROUP_NOTATION_VALUE = $"
                    + SdcArtifactHandlerConstants.GROUP_NOTATION_VALUE;

        if (serviceLogic != null && context != null) {
            status = serviceLogic.save("SQL", false, false, key, null, null, context);
            if (status.toString().equals("FAILURE"))
                throw new SvcLogicException("Error While processing VNFC_REFERENCE table ");
        }
    }

    public void processDownloadDgReference(SvcLogicContext context, boolean isUpdate)
            throws SvcLogicException, SQLException {
        String fn = "DBService.processDownloadDgReference";
        log.info(fn + "Starting DB operation for Download DG Reference " + isUpdate);
        String key = "";
        QueryStatus status = null;

        if (isUpdate)
            key = "update " + SdcArtifactHandlerConstants.DB_DOWNLOAD_DG_REFERENCE + " set DOWNLOAD_CONFIG_DG = $"
                    + SdcArtifactHandlerConstants.DOWNLOAD_DG_REFERENCE + " where PROTOCOL = $"
                    + SdcArtifactHandlerConstants.DEVICE_PROTOCOL;
        else
            key = "insert into " + SdcArtifactHandlerConstants.DB_DOWNLOAD_DG_REFERENCE + " set DOWNLOAD_CONFIG_DG = $"
                    + SdcArtifactHandlerConstants.DOWNLOAD_DG_REFERENCE + " , PROTOCOL = $"
                    + SdcArtifactHandlerConstants.DEVICE_PROTOCOL;

        if (serviceLogic != null && context != null)
            status = serviceLogic.save("SQL", false, false, key, null, null, context);
        if ((status == null) || status.toString().equals("FAILURE"))
            throw new SvcLogicException("Error While processing DOWNLOAD_DG_REFERENCE table ");
    }

    public void processConfigActionDg(SvcLogicContext context, boolean isUpdate) throws SvcLogicException {
        String fn = "DBService.processConfigActionDg";
        log.info(fn + "Starting DB operation for Config DG Action " + isUpdate);
        String key = "";
        QueryStatus status = null;

        if (context.getAttribute(SdcArtifactHandlerConstants.DOWNLOAD_DG_REFERENCE) != null
                && context.getAttribute(SdcArtifactHandlerConstants.DOWNLOAD_DG_REFERENCE).length() > 0) {
            if (isUpdate)
                key = "update " + SdcArtifactHandlerConstants.DB_CONFIG_ACTION_DG + " set DOWNLOAD_CONFIG_DG = $"
                        + SdcArtifactHandlerConstants.DOWNLOAD_DG_REFERENCE + " where ACTION = $"
                        + SdcArtifactHandlerConstants.ACTION + " and VNF_TYPE = $"
                        + SdcArtifactHandlerConstants.VNF_TYPE;
            else
                key = "insert into " + SdcArtifactHandlerConstants.DB_CONFIG_ACTION_DG + " set DOWNLOAD_CONFIG_DG = $"
                        + SdcArtifactHandlerConstants.DOWNLOAD_DG_REFERENCE + " , ACTION = $"
                        + SdcArtifactHandlerConstants.ACTION + " , VNF_TYPE = $" + SdcArtifactHandlerConstants.VNF_TYPE;

            if (serviceLogic != null && context != null)
                status = serviceLogic.save("SQL", false, false, key, null, null, context);
            if ((status == null) || status.toString().equals("FAILURE"))
                throw new SvcLogicException("Error While processing Configure DG Action table ");
        } else
            log.info("No Update required for Config DG Action");

    }

    public String getModelDataInformationbyArtifactName(String artifact_name) throws SvcLogicException {
        String fn = "DBService.getVnfData";
        String key = "";
        SvcLogicContext con = new SvcLogicContext();
        HashMap<String, String> modelData = new HashMap<String, String>();
        QueryStatus status = null;
        key = "select VNF_TYPE, VNFC_TYPE, ACTION, FILE_CATEGORY, ARTIFACT_TYPE from ASDC_REFERENCE where  ARTIFACT_NAME = "
                + artifact_name;

        if (serviceLogic != null && con != null) {
            log.info(fn + "select Key: " + key);
            status = serviceLogic.query("SQL", false, null, key, null, null, con);
            if (status.toString().equals("FAILURE"))
                throw new SvcLogicException("Error While processing is ArtifactUpdateRequiredforPD table ");

        }

        log.info(fn + "Vnf_received :" + con.getAttribute("VNF_TYPE"));

        return con.getAttribute("VNF_TYPE");

    }

    public void updateYangContents(SvcLogicContext context, String artifactId, String yangContents)
            throws SvcLogicException {
        String fn = "DBService.updateYangContents";
        log.info(fn + "Starting DB operation for  updateYangContents");
        String key = "";
        QueryStatus status = null;

        key = "update ASDC_ARTIFACTS " + " set ARTIFACT_CONTENT = '" + yangContents + "'"
                + " where ASDC_ARTIFACTS_ID = " + artifactId;

        if (serviceLogic != null && context != null)
            status = serviceLogic.save("SQL", false, false, key, null, null, context);
        if ((status == null) || status.toString().equals("FAILURE"))
            throw new SvcLogicException("Error While processing Configure DG Action table ");

    }


    public void insertProtocolReference(SvcLogicContext context, String vnfType, String protocol, String action,
            String action_level, String template) throws SvcLogicException {
        String fn = "DBService.insertProtocolReference";
        log.info(fn + "Starting DB operation for  insertProtocolReference");
        String key = "";
        QueryStatus status = null;

        key = "insert into PROTOCOL_REFERENCE (ACTION, VNF_TYPE, PROTOCOL, UPDATED_DATE, TEMPLATE, ACTION_LEVEL)"
                + " values  (" + "'" + action + "', '" + vnfType + "', '" + protocol + "', now(),'" + template + "', '"
                + action_level + "')";

        if (serviceLogic != null && context != null)
            status = serviceLogic.save("SQL", false, false, key, null, null, context);
        if ((status == null) || status.toString().equals("FAILURE"))
            throw new SvcLogicException("Error While processing insertProtocolReference ");
        
    }
    
    public boolean isProtocolReferenceUpdateRequired(SvcLogicContext context, String vnfType, String protocol,
             String action, String action_level, String template) throws SvcLogicException {
        SvcLogicContext localContext = new SvcLogicContext();
        String fn = "DBService.isProtocolReferenceUpdateRequired";
        log.info(fn + "Starting DB operation for  isProtocolReferenceUpdateRequired");
        String key = "";
        QueryStatus status = null;

        key = "select COUNT(*) from PROTOCOL_REFERENCE where ACTION='" + action + "' and ACTION_LEVEL='" + action_level
                + "' and VNF_TYPE='" + vnfType + "'";
        status = serviceLogic.query("SQL", false, null, key, null, null, localContext);
        String countStr = localContext.getAttribute("COUNT(*)");
        int count = Integer.parseInt(countStr);
        if (count > 0)
            return true;
        else
            return false;
    }

    public void updateProtocolReference(SvcLogicContext context, String vnfType, String protocol, String action,
                String action_level, String template) throws SvcLogicException {

        String fn = "DBService.isProtocolReferenceUpdateRequired";
        log.info(fn + "Starting DB operation for  isProtocolReferenceUpdateRequired");
        String key = "";
        QueryStatus status = null;

        key = "update PROTOCOL_REFERENCE set UPDATED_DATE=now(), template='" + template + "' where ACTION='" + action
                + "' and ACTION_LEVEL='" + action_level + "' and VNF_TYPE='" + vnfType + "'";
        status = serviceLogic.save("SQL", false, false, key, null, null, context);
        if (status == QueryStatus.FAILURE) {
            log.info("updateProtocolReference:: Error updating protocol reference");
            throw new SvcLogicException("Error - updating PROTOCOL_REFERENCE_TABLE in updateProtocolReference");
        }
        return;
    }

}
