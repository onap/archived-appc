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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.artifact.handler.dbservices;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants;
import org.onap.ccsdk.sli.adaptors.resource.sql.SqlResource;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource.QueryStatus;

public class DBService {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(DBService.class);
    private static final String FAILURE_PARAM = "FAILURE";
    private static final String RECEIVED_AS = "Internal Version received as1 : ";
    private static final String SET_DOWNLOAD_CONFIG_QUERY_STR = " set DOWNLOAD_CONFIG_DG = $";
    private static final String WHERE_VNF_TYPE_QUERY_STR = " where VNF_TYPE = $";
    private static final String ACTION_QUERY_STR = " , ACTION = $";
    private static final String VNF_TYPE_QUERY_STR = " , VNF_TYPE = $";
    private static final String INSERT_INTO_QUERY_STR = "insert into ";
    private static final String AND_ACTION_QUERY_STR = " and ACTION = $";
    private static final String AND_FILE_CAT_QUERY_STR = " and FILE_CATEGORY = $";
    private static final String AND_VNF_TYPE_QUERY_STR = " and VNF_TYPE = $";
    private static final String UPDATE_QUERY_STR = "update ";
    private static final String AND_VNFC_TYPE_QUERY_STR = " and VNFC_TYPE = $";

    private SvcLogicResource serviceLogic;
    private static DBService dgGeneralDBService = null;

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

    public static DBService initialise() {
        if (dgGeneralDBService == null) {
            dgGeneralDBService = new DBService();
        }
        return dgGeneralDBService;
    }

    public String getInternalVersionNumber(SvcLogicContext ctx, String artifactName, String prefix)
        throws SvcLogicException {
        QueryStatus status;
        String artifactInternalVersion = null;
        if (serviceLogic != null && ctx != null) {
            String key = "select max(internal_version) as maximum from ASDC_ARTIFACTS  WHERE ARTIFACT_NAME = '"
                + artifactName + "'";
            log.info("Getting internal Versoin :" + key);
            status = serviceLogic.query("SQL", false, null, key, prefix, null, ctx);
            if (status.toString().equals(FAILURE_PARAM)) {
                throw new SvcLogicException("Error - getting internal Artifact Number");
            }
            artifactInternalVersion = ctx.getAttribute("maximum");
            log.info("Internal Version received as : " + artifactInternalVersion);
            log.info(RECEIVED_AS + ctx.getAttribute("max(internal_version)"));
            log.info(RECEIVED_AS + ctx.getAttribute("max"));
            log.info(RECEIVED_AS + ctx.getAttribute("internal_version"));
            log.info(RECEIVED_AS + ctx.getAttributeKeySet().toString());
        }
        return artifactInternalVersion;
    }

    public String getArtifactID(SvcLogicContext ctx, String artifactName) throws SvcLogicException {
        QueryStatus status;
        String artifactID = null;
        if (serviceLogic != null && ctx != null) {
            String key = "select max(ASDC_ARTIFACTS_ID) as id from ASDC_ARTIFACTS  WHERE ARTIFACT_NAME = '"
                + artifactName + "'";
            log.info("Getting Artifact ID String :" + key);
            status = serviceLogic.query("SQL", false, null, key, null, null, ctx);
            if (status.toString().equals(FAILURE_PARAM)) {
                throw new SvcLogicException("Error - getting  Artifact ID from database");
            }
            artifactID = ctx.getAttribute("id");
            log.info("SDC_ARTIFACTS_ID received as : " + ctx.getAttribute("id"));
        }
        return artifactID;
    }

    public QueryStatus saveArtifacts(SvcLogicContext ctx, int intversion) throws SvcLogicException {
        QueryStatus status = null;
        if (serviceLogic != null && ctx != null) {
            String key = "INSERT INTO ASDC_ARTIFACTS " + "SET SERVICE_UUID    =  $service-uuid , "
                + " DISTRIBUTION_ID    =  $distribution-id ," + " SERVICE_NAME    =  $service-name ,"
                + " SERVICE_DESCRIPTION    =  $service-description ," + " RESOURCE_UUID    = $resource-uuid ,"
                + " RESOURCE_INSTANCE_NAME    = $resource-instance-name ," + " RESOURCE_NAME    = $resource-name ,"
                + " RESOURCE_VERSION    = $resource-version ," + " RESOURCE_TYPE    = $resource-type ,"
                + " ARTIFACT_UUID    = $artifact-uuid ," + " ARTIFACT_TYPE    = $artifact-type ,"
                + " ARTIFACT_VERSION    = $artifact-version ,"
                + " ARTIFACT_DESCRIPTION    = $artifact-description ," + " INTERNAL_VERSION    = " + intversion
                + "," + " ARTIFACT_NAME       =  $artifact-name ," + " ARTIFACT_CONTENT    =  $artifact-contents ";

            status = serviceLogic.save("SQL", false, false, key, null, null, ctx);
            if (status.toString().equals(FAILURE_PARAM)) {
                throw new SvcLogicException("Error While processing storing Artifact: "
                    + ctx.getAttribute(SdcArtifactHandlerConstants.ARTIFACT_NAME));
            }
        }
        return status;
    }

    public QueryStatus logData(SvcLogicContext ctx, String prefix) throws SvcLogicException {
        QueryStatus status = null;
        if (serviceLogic != null && ctx != null) {
            String key = "INSERT INTO CONFIG_TRANSACTION_LOG " + " SET request_id = $request-id , "
                + " message_type = $log-message-type , " + " message = $log-message ;";
            status = serviceLogic.save("SQL", false, false, key, null, prefix, ctx);
            if (status.toString().equals(FAILURE_PARAM)) {
                throw new SvcLogicException("Error while loging data");
            }

        }
        return status;
    }

    public void processConfigureActionDg(SvcLogicContext context, boolean isUpdate) {
        log.info("Update Parameter for SDC Reference " + isUpdate);
        //TODO implement this method
    }

    public void processSdcReferences(SvcLogicContext context, boolean isUpdate) throws SvcLogicException {
        processSdcReferences(context, isUpdate, null);
    }

    public void processSdcReferences(SvcLogicContext context, boolean isUpdate, String modelId) throws SvcLogicException {
        String key;
        QueryStatus status;
         if (isUpdate && context.getAttribute(SdcArtifactHandlerConstants.FILE_CATEGORY)
            .equals(SdcArtifactHandlerConstants.CAPABILITY)) {
            log.info("Updating capability artifact in ASDC_REFERENCE");
            key = UPDATE_QUERY_STR + SdcArtifactHandlerConstants.DB_SDC_REFERENCE + "  set ARTIFACT_NAME = $"
                + SdcArtifactHandlerConstants.ARTIFACT_NAME + " where " + "FILE_CATEGORY = $"
                + SdcArtifactHandlerConstants.FILE_CATEGORY + AND_VNF_TYPE_QUERY_STR
                + SdcArtifactHandlerConstants.VNF_TYPE;
        } else if (isUpdate) {
            key = UPDATE_QUERY_STR + SdcArtifactHandlerConstants.DB_SDC_REFERENCE + "  set ARTIFACT_NAME = $"
                + SdcArtifactHandlerConstants.ARTIFACT_NAME + " where VNFC_TYPE = $"
                + SdcArtifactHandlerConstants.VNFC_TYPE + AND_FILE_CAT_QUERY_STR
                + SdcArtifactHandlerConstants.FILE_CATEGORY + AND_ACTION_QUERY_STR + SdcArtifactHandlerConstants.ACTION
                + AND_VNF_TYPE_QUERY_STR + SdcArtifactHandlerConstants.VNF_TYPE;
            if (StringUtils.isNotBlank(modelId)) {
                key += createQueryListForTemplateIds(modelId);
            }
        } else {
            if (context.getAttribute(SdcArtifactHandlerConstants.FILE_CATEGORY)
                .equals(SdcArtifactHandlerConstants.CAPABILITY)) {
                log.info("Inserting new record for capability artifact in ASDC_REFERENCE");
                key = INSERT_INTO_QUERY_STR + SdcArtifactHandlerConstants.DB_SDC_REFERENCE + " set VNFC_TYPE = null "
                    + " , FILE_CATEGORY = $" + SdcArtifactHandlerConstants.FILE_CATEGORY + VNF_TYPE_QUERY_STR
                    + SdcArtifactHandlerConstants.VNF_TYPE + " , ACTION = null " + " , ARTIFACT_TYPE = null "
                    + " , ARTIFACT_NAME = $" + SdcArtifactHandlerConstants.ARTIFACT_NAME;
            } else {
                key = INSERT_INTO_QUERY_STR + SdcArtifactHandlerConstants.DB_SDC_REFERENCE + " set VNFC_TYPE = $"
                    + SdcArtifactHandlerConstants.VNFC_TYPE + " , FILE_CATEGORY = $"
                    + SdcArtifactHandlerConstants.FILE_CATEGORY + VNF_TYPE_QUERY_STR
                    + SdcArtifactHandlerConstants.VNF_TYPE + ACTION_QUERY_STR + SdcArtifactHandlerConstants.ACTION
                    + " , ARTIFACT_TYPE = $" + SdcArtifactHandlerConstants.ARTIFACT_TYPE + " , ARTIFACT_NAME = $"
                    + SdcArtifactHandlerConstants.ARTIFACT_NAME;
            }
        }
        if (serviceLogic != null) {
            log.info("Insert Key: " + key);
            status = serviceLogic.save("SQL", false, false, key, null, null, context);
            if (status.toString().equals(FAILURE_PARAM)) {
                throw new SvcLogicException("Error While processing sdc_reference table ");
            }
        }
    }

    public boolean isArtifactUpdateRequired(SvcLogicContext context, String db) throws DBException {
        return isArtifactUpdateRequired( context,  db, null);
    }

    public boolean isArtifactUpdateRequired(SvcLogicContext context, String db, String modelId)
        throws DBException {
        try {
            log.info("Checking if Update required for this data");
            log.info("db" + db);
            log.info("ACTION=" + context.getAttribute(SdcArtifactHandlerConstants.ACTION));
            log.info("VNFC_TYPE=" + context.getAttribute(SdcArtifactHandlerConstants.VNFC_TYPE));
            log.info("VNFC_INSTANCE=" + context.getAttribute(SdcArtifactHandlerConstants.VNFC_INSTANCE));
            log.info("VM_INSTANCE=" + context.getAttribute(SdcArtifactHandlerConstants.VM_INSTANCE));
            log.info("VNF_TYPE=" + context.getAttribute(SdcArtifactHandlerConstants.VNF_TYPE));

            //Check for templates
            //if templates are present - there might be multiple records, so validate
            if( db.equals(SdcArtifactHandlerConstants.DB_SDC_REFERENCE) && StringUtils.isNotBlank(modelId)) {
                log.info("ModelId is sent!!");
                  String queryPart = createQueryListForTemplateIds(modelId);
                  log.info("Querypart is = "+queryPart);
                   if (isUpdateRequiredForTemplates(queryPart, context, db)) {
                       log.info("Update is Required!!");
                    return true;
                   } else {
                       log.info("Insert is Required!!");
                       return false;
                   }
            }

            String whereClause;
            QueryStatus status;
            whereClause = WHERE_VNF_TYPE_QUERY_STR + SdcArtifactHandlerConstants.VNF_TYPE;
            whereClause = resolveWhereClause(context, db, whereClause);
            if (validate(db)) {
                    String key = "select COUNT(*) from " + db + whereClause;
                    log.info("SELECT String : " + key);
                    status = serviceLogic.query("SQL", false, null, key, null, null, context);
                    checkForFailure(db, status);
                    String count = context.getAttribute("COUNT(*)");
                    log.info("Number of row Returned : " + count + ": " + status + ":");
                    return tryAddCountAttribute(context, count);
            }
            return false;
        } catch (SvcLogicException e) {
            throw new DBException("An error occurred while checking for artifact update", e);
        }
    }

    private void checkForFailure(String db, QueryStatus status) throws SvcLogicException {
        if (status.toString().equals(FAILURE_PARAM)) {
            throw new SvcLogicException("Error while reading data from " + db);
        }
    }

    private boolean validate(String db) {
        return db != null && serviceLogic != null;
    }

    private boolean keyExists(PropertiesConfiguration conf, String property) {
        if (conf.subset(property) != null) {
            if (conf.containsKey(property)) {
                log.info("Key Exists for property" + property + "in southbound.properties file");
                return true;
            }
        } else {
            log.info("Key Does not exists and need to add the key  for property" + property
                + "in southbound.properties file");
        }
        return false;
    }

    private boolean tryAddCountAttribute(SvcLogicContext context, String count) {
        if (count != null && Integer.parseInt(count) > 0) {
            context.setAttribute(count, null);
            return true;
        } else {
            return false;
        }
    }


    private String resolveWhereClause(SvcLogicContext context, String db, String whereClause) {
        if (db != null) {
            if (hasValidAttributes(context, db)) {
                return whereClause + AND_FILE_CAT_QUERY_STR + SdcArtifactHandlerConstants.FILE_CATEGORY;
            } else if (db.equals(SdcArtifactHandlerConstants.DB_SDC_REFERENCE)) {
                return whereClause + AND_VNFC_TYPE_QUERY_STR + SdcArtifactHandlerConstants.VNFC_TYPE
                    + AND_FILE_CAT_QUERY_STR + SdcArtifactHandlerConstants.FILE_CATEGORY + AND_ACTION_QUERY_STR
                    + SdcArtifactHandlerConstants.ACTION;
            } else if (db.equals(SdcArtifactHandlerConstants.DB_DOWNLOAD_DG_REFERENCE)) {
                return " where PROTOCOL = $" + SdcArtifactHandlerConstants.DEVICE_PROTOCOL;
            } else if (db.equals(SdcArtifactHandlerConstants.DB_DEVICE_AUTHENTICATION)) {
                log.info(" DB validation for Device authentication " + whereClause + " AND  PROTOCOL = $"
                             + SdcArtifactHandlerConstants.DEVICE_PROTOCOL + " AND ACTION = $"
                             + SdcArtifactHandlerConstants.ACTION);
                return whereClause + " AND  PROTOCOL = $" + SdcArtifactHandlerConstants.DEVICE_PROTOCOL
                             + " AND ACTION = $" + SdcArtifactHandlerConstants.ACTION;
            } else if (db.equals(SdcArtifactHandlerConstants.DB_CONFIG_ACTION_DG)) {
                return whereClause + AND_ACTION_QUERY_STR + SdcArtifactHandlerConstants.ACTION;
            } else if (db.equals(SdcArtifactHandlerConstants.DB_VNFC_REFERENCE)) {
                return whereClause + AND_ACTION_QUERY_STR + SdcArtifactHandlerConstants.ACTION
                    + AND_VNFC_TYPE_QUERY_STR + SdcArtifactHandlerConstants.VNFC_TYPE + " and VNFC_INSTANCE = $"
                    + SdcArtifactHandlerConstants.VNFC_INSTANCE + " and VM_INSTANCE = $"
                    + SdcArtifactHandlerConstants.VM_INSTANCE;
            }
        }
        return whereClause;
    }

    private boolean hasValidAttributes(SvcLogicContext context, String db) {
        return db.equals(SdcArtifactHandlerConstants.DB_SDC_REFERENCE)
            && context.getAttribute(SdcArtifactHandlerConstants.FILE_CATEGORY)
            .equals(SdcArtifactHandlerConstants.CAPABILITY)
            && context.getAttribute(SdcArtifactHandlerConstants.ACTION) == null;
    }

    public void processDeviceInterfaceProtocol(SvcLogicContext context, boolean isUpdate) throws SvcLogicException {
        log.info("Starting DB operation for Device Interface Protocol " + isUpdate);
        String key;
        QueryStatus status;
        if (isUpdate) {
            key = UPDATE_QUERY_STR + SdcArtifactHandlerConstants.DB_DEVICE_INTERFACE_PROTOCOL + " set PROTOCOL = $"
                + SdcArtifactHandlerConstants.DEVICE_PROTOCOL + " , DG_RPC = 'getDeviceRunningConfig' "
                + " , MODULE = 'APPC' " + WHERE_VNF_TYPE_QUERY_STR + SdcArtifactHandlerConstants.VNF_TYPE;
        } else {
            key =
                INSERT_INTO_QUERY_STR + SdcArtifactHandlerConstants.DB_DEVICE_INTERFACE_PROTOCOL + " set  VNF_TYPE = $"
                    + SdcArtifactHandlerConstants.VNF_TYPE + " , PROTOCOL = $"
                    + SdcArtifactHandlerConstants.DEVICE_PROTOCOL + " , DG_RPC = 'getDeviceRunningConfig' "
                    + " , MODULE = 'APPC' ";
        }

        if (serviceLogic != null && context != null) {

            status = serviceLogic.save("SQL", false, false, key, null, null, context);
            if (status.toString().equals(FAILURE_PARAM)) {
                throw new SvcLogicException("Error While processing DEVICE_INTERFACE_PROTOCOL table ");
            }
        }
    }

    public void processDeviceAuthentication(SvcLogicContext context, boolean isUpdate)
        throws DBException {
        try {
            String fn = "DBService.processDeviceAuthentication";
            log.info(fn + "Starting DB operation for Device Authentication " + isUpdate);
            String port = context.getAttribute(SdcArtifactHandlerConstants.PORT_NUMBER);
            String user = context.getAttribute(SdcArtifactHandlerConstants.USER_NAME);
            String protocol = context.getAttribute(SdcArtifactHandlerConstants.DEVICE_PROTOCOL);
            String action = context.getAttribute(SdcArtifactHandlerConstants.ACTION);
            String vnftype = context.getAttribute(SdcArtifactHandlerConstants.VNF_TYPE);

            if (StringUtils.isBlank(port)) {
                port = "0";
            }
            if (StringUtils.isBlank(user)) {
                user = "";
            }
            if (isInvalidInput(SdcArtifactHandlerConstants.DEVICE_PROTOCOL, SdcArtifactHandlerConstants.ACTION,
                      	SdcArtifactHandlerConstants.VNF_TYPE)) {
                throw new SvcLogicException(
                    "Error While processing reference File as few or all of parameters VNF_TYPE,PROTOCOL,ACTION are missing ");
            }

            log.info("Starting DB operation for Device authentication " + isUpdate);
            log.info("credentials"+user + "user" + "port" + port +"protocol"+protocol+"action"+action+"vnftype"+vnftype);
            String key;
            QueryStatus status;
            if (isUpdate) {
                key = UPDATE_QUERY_STR + SdcArtifactHandlerConstants.DB_DEVICE_AUTHENTICATION + " set USER_NAME = $"
                        + SdcArtifactHandlerConstants.USER_NAME + " , PORT_NUMBER = $" + SdcArtifactHandlerConstants.PORT_NUMBER + "";
                if (context.getAttributeKeySet().contains(SdcArtifactHandlerConstants.URL)) {
                    String url = context.getAttribute(SdcArtifactHandlerConstants.URL);
                    if (StringUtils.isBlank(url)) {
                        url = "" ;
                    }
                    key = key + ", URL = '" + url + "' ";
                }
                key = key + WHERE_VNF_TYPE_QUERY_STR + SdcArtifactHandlerConstants.VNF_TYPE + "  AND PROTOCOL = $"
                        + SdcArtifactHandlerConstants.DEVICE_PROTOCOL + " AND  ACTION = $"
                        + SdcArtifactHandlerConstants.ACTION;
            } else {
                key = "insert into DEVICE_AUTHENTICATION set VNF_TYPE = $" + SdcArtifactHandlerConstants.VNF_TYPE + " , PROTOCOL = $" + SdcArtifactHandlerConstants.DEVICE_PROTOCOL + " , " + "ACTION = $" + SdcArtifactHandlerConstants.ACTION + " , USER_NAME = $" + SdcArtifactHandlerConstants.USER_NAME + " , PORT_NUMBER = $" + SdcArtifactHandlerConstants.PORT_NUMBER + "";
                if (context.getAttributeKeySet().contains(SdcArtifactHandlerConstants.URL)) {
                    String url = context.getAttribute(SdcArtifactHandlerConstants.URL);
                    if (StringUtils.isBlank(url)) {
                        url = "";
                    }
                    key = key + ", URL = '" + url + "' ";
                }
            }

            log.info("Query forDevice authentication  " + key);
            if (serviceLogic != null && context != null) {

                status = serviceLogic.save("SQL", false, false, key, null, null, context);
                if (status.toString().equals(FAILURE_PARAM)) {
                    throw new SvcLogicException("Error While processing DEVICE_AUTHENTICATION table ");
                }
            }

        } catch (SvcLogicException e) {

            throw new DBException("An error occurred when processing device authentication", e);
        }
    }

    private boolean isInvalidInput(String protocol, String action, String vnfType) {
        return isInvalid(vnfType) && isInvalid(action) && isInvalid(protocol);
    }

    private boolean isInvalid(String str) {
        return (str == null) || ("".equals(str));
    }

    public void processVnfcReference(SvcLogicContext context, boolean isUpdate) throws SvcLogicException {
        String fn = "DBService.processVnfcReference";
        log.info(fn + "Starting DB operation for Vnfc Reference " + isUpdate);
        String key;

        int vmInstance = -1;
        if (context.getAttribute(SdcArtifactHandlerConstants.VM_INSTANCE) != null) {
            vmInstance = Integer.parseInt(context.getAttribute(SdcArtifactHandlerConstants.VM_INSTANCE));
        }

        int vnfcInstance = -1;
        if (context.getAttribute(SdcArtifactHandlerConstants.VNFC_INSTANCE) != null) {
            vnfcInstance = Integer.parseInt(context.getAttribute(SdcArtifactHandlerConstants.VNFC_INSTANCE));
        }

        QueryStatus status;
        if (isUpdate) {
            key = UPDATE_QUERY_STR + SdcArtifactHandlerConstants.DB_VNFC_REFERENCE + " set VM_INSTANCE = " + vmInstance
                + " , VNFC_INSTANCE = " + vnfcInstance + " , VNFC_TYPE = $" + SdcArtifactHandlerConstants.VNFC_TYPE
                + " , VNFC_FUNCTION_CODE = $" + SdcArtifactHandlerConstants.VNFC_FUNCTION_CODE
                + " , GROUP_NOTATION_TYPE = $" + SdcArtifactHandlerConstants.GROUP_NOTATION_TYPE
                + " , GROUP_NOTATION_VALUE = $" + SdcArtifactHandlerConstants.GROUP_NOTATION_VALUE
                + " , IPADDRESS_V4_OAM_VIP = $" + SdcArtifactHandlerConstants.IPADDRESS_V4_OAM_VIP
                + WHERE_VNF_TYPE_QUERY_STR + SdcArtifactHandlerConstants.VNF_TYPE + AND_ACTION_QUERY_STR
                + SdcArtifactHandlerConstants.ACTION + AND_VNFC_TYPE_QUERY_STR + SdcArtifactHandlerConstants.VNFC_TYPE
                + " and VNFC_INSTANCE = $" + SdcArtifactHandlerConstants.VNFC_INSTANCE + " and VM_INSTANCE = $"
                + SdcArtifactHandlerConstants.VM_INSTANCE;
        } else {
            key = INSERT_INTO_QUERY_STR + SdcArtifactHandlerConstants.DB_VNFC_REFERENCE + " set  VNF_TYPE = $"
                + SdcArtifactHandlerConstants.VNF_TYPE + ACTION_QUERY_STR + SdcArtifactHandlerConstants.ACTION
                + " , VM_INSTANCE = $" + SdcArtifactHandlerConstants.VM_INSTANCE + " , VNFC_INSTANCE = $"
                + SdcArtifactHandlerConstants.VNFC_INSTANCE + " , VNFC_TYPE = $"
                + SdcArtifactHandlerConstants.VNFC_TYPE + " , VNFC_FUNCTION_CODE = $"
                + SdcArtifactHandlerConstants.VNFC_FUNCTION_CODE + " , TEMPLATE_ID = $"
                + SdcArtifactHandlerConstants.TEMPLATE_ID + " , GROUP_NOTATION_TYPE = $"
                + SdcArtifactHandlerConstants.GROUP_NOTATION_TYPE + " , IPADDRESS_V4_OAM_VIP = $"
                + SdcArtifactHandlerConstants.IPADDRESS_V4_OAM_VIP + " , GROUP_NOTATION_VALUE = $"
                + SdcArtifactHandlerConstants.GROUP_NOTATION_VALUE;
        }

        if (serviceLogic != null) {
            status = serviceLogic.save("SQL", false, false, key, null, null, context);
            if (status.toString().equals(FAILURE_PARAM)) {
                throw new SvcLogicException("Error While processing VNFC_REFERENCE table ");
            }
        }
    }

    public void processDownloadDgReference(SvcLogicContext context, boolean isUpdate)
        throws SvcLogicException {
        String fn = "DBService.processDownloadDgReference";
        log.info(fn + "Starting DB operation for Download DG Reference " + isUpdate);
        String key;
        QueryStatus status = null;

        if (isUpdate) {
            key =
                UPDATE_QUERY_STR + SdcArtifactHandlerConstants.DB_DOWNLOAD_DG_REFERENCE + SET_DOWNLOAD_CONFIG_QUERY_STR
                    + SdcArtifactHandlerConstants.DOWNLOAD_DG_REFERENCE + " where PROTOCOL = $"
                    + SdcArtifactHandlerConstants.DEVICE_PROTOCOL;
        } else {
            key = INSERT_INTO_QUERY_STR + SdcArtifactHandlerConstants.DB_DOWNLOAD_DG_REFERENCE
                + SET_DOWNLOAD_CONFIG_QUERY_STR
                + SdcArtifactHandlerConstants.DOWNLOAD_DG_REFERENCE + " , PROTOCOL = $"
                + SdcArtifactHandlerConstants.DEVICE_PROTOCOL;
        }

        if (serviceLogic != null && context != null) {
            status = serviceLogic.save("SQL", false, false, key, null, null, context);
        }
        if ((status == null) || status.toString().equals(FAILURE_PARAM)) {
            throw new SvcLogicException("Error While processing DOWNLOAD_DG_REFERENCE table ");
        }
    }

    public void processConfigActionDg(SvcLogicContext context, boolean isUpdate) throws SvcLogicException {
        String fn = "DBService.processConfigActionDg";
        log.info(fn + "Starting DB operation for Config DG Action " + isUpdate);
        String key;
        QueryStatus status = null;

        if (context.getAttribute(SdcArtifactHandlerConstants.DOWNLOAD_DG_REFERENCE) != null
            && context.getAttribute(SdcArtifactHandlerConstants.DOWNLOAD_DG_REFERENCE).length() > 0) {
            if (isUpdate) {
                key = UPDATE_QUERY_STR + SdcArtifactHandlerConstants.DB_CONFIG_ACTION_DG + SET_DOWNLOAD_CONFIG_QUERY_STR
                    + SdcArtifactHandlerConstants.DOWNLOAD_DG_REFERENCE + " where ACTION = $"
                    + SdcArtifactHandlerConstants.ACTION + AND_VNF_TYPE_QUERY_STR
                    + SdcArtifactHandlerConstants.VNF_TYPE;
            } else {
                key = INSERT_INTO_QUERY_STR + SdcArtifactHandlerConstants.DB_CONFIG_ACTION_DG
                    + SET_DOWNLOAD_CONFIG_QUERY_STR
                    + SdcArtifactHandlerConstants.DOWNLOAD_DG_REFERENCE + ACTION_QUERY_STR
                    + SdcArtifactHandlerConstants.ACTION + VNF_TYPE_QUERY_STR + SdcArtifactHandlerConstants.VNF_TYPE;
            }

            if (serviceLogic != null) {
                status = serviceLogic.save("SQL", false, false, key, null, null, context);
            }
            if ((status == null) || status.toString().equals(FAILURE_PARAM)) {
                throw new SvcLogicException("Error While processing Configure DG Action table ");
            }
        } else {
            log.info("No Update required for Config DG Action");
        }

    }

    public String getModelDataInformationbyArtifactName(String artifactName) throws SvcLogicException {
        String fn = "DBService.getVnfData";
        SvcLogicContext con = new SvcLogicContext();
        String key;
        QueryStatus status;
        key =
            "select VNF_TYPE, VNFC_TYPE, ACTION, FILE_CATEGORY, ARTIFACT_TYPE from ASDC_REFERENCE where  ARTIFACT_NAME = "
                + artifactName;

        if (serviceLogic != null) {
            log.info(fn + "select Key: " + key);
            status = serviceLogic.query("SQL", false, null, key, null, null, con);
            if (status.toString().equals(FAILURE_PARAM)) {
                throw new SvcLogicException("Error While processing is ArtifactUpdateRequiredforPD table ");
            }

        }
        log.info(fn + "Vnf_received :" + con.getAttribute("VNF_TYPE"));

        return con.getAttribute("VNF_TYPE");
    }

    public void updateYangContents(SvcLogicContext context, String artifactId, String yangContents)
        throws SvcLogicException {
        String fn = "DBService.updateYangContents";
        log.info(fn + "Starting DB operation for  updateYangContents");
        String key;
        QueryStatus status = null;

        key = "update ASDC_ARTIFACTS " + " set ARTIFACT_CONTENT = '" + yangContents + "'"
            + " where ASDC_ARTIFACTS_ID = " + artifactId;

        if (serviceLogic != null && context != null) {
            status = serviceLogic.save("SQL", false, false, key, null, null, context);
        }
        if ((status == null) || status.toString().equals(FAILURE_PARAM)) {
            throw new SvcLogicException("Error While processing Configure DG Action table ");
        }

    }


    public void insertProtocolReference(SvcLogicContext context, String vnfType, String protocol, String action,
        String actionLevel, String template) throws SvcLogicException {
        String fn = "DBService.insertProtocolReference";
        log.info(fn + "Starting DB operation for  insertProtocolReference");
        String key;
        QueryStatus status = null;

        key = "insert into PROTOCOL_REFERENCE (ACTION, VNF_TYPE, PROTOCOL, UPDATED_DATE, TEMPLATE, ACTION_LEVEL)"
            + " values  (" + "'" + action + "', '" + vnfType + "', '" + protocol + "', now(),'" + template + "', '"
            + actionLevel + "')";

        if (serviceLogic != null && context != null) {
            status = serviceLogic.save("SQL", false, false, key, null, null, context);
        }
        if ((status == null) || status.toString().equals(FAILURE_PARAM)) {
            throw new SvcLogicException("Error While processing insertProtocolReference ");
        }

    }

    public boolean isProtocolReferenceUpdateRequired(SvcLogicContext context, String vnfType, String protocol,
        String action, String actionLevel, String template) throws SvcLogicException {
        SvcLogicContext localContext = new SvcLogicContext();
        String fn = "DBService.isProtocolReferenceUpdateRequired";
        log.info(fn + "Starting DB operation for  isProtocolReferenceUpdateRequired");

        String key = "select COUNT(*) from PROTOCOL_REFERENCE where ACTION='" + action + "' and ACTION_LEVEL='" + actionLevel
            + "' and VNF_TYPE='" + vnfType + "'";
        serviceLogic.query("SQL", false, null, key, null, null, localContext);

        String countStr = localContext.getAttribute("COUNT(*)");
        int count = Integer.parseInt(countStr);
        return count > 0;
    }

    public void updateProtocolReference(SvcLogicContext context, String vnfType, String protocol, String action,
        String actionLevel, String template) throws SvcLogicException {

        String fn = "DBService.isProtocolReferenceUpdateRequired";
        log.info(fn + "Starting DB operation for  isProtocolReferenceUpdateRequired");
        String key;
        QueryStatus status;

        key = "update PROTOCOL_REFERENCE set UPDATED_DATE=now(), template='" + template + "', protocol ='" + protocol
            + "' where ACTION='" + action + "' and ACTION_LEVEL='" + actionLevel + "' and VNF_TYPE='" + vnfType
            + "'";
        status = serviceLogic.save("SQL", false, false, key, null, null, context);
        if (status == QueryStatus.FAILURE) {
            log.info("updateProtocolReference:: Error updating protocol reference");
            throw new SvcLogicException("Error - updating PROTOCOL_REFERENCE_TABLE in updateProtocolReference");
        }
    }

    public String getDownLoadDGReference(SvcLogicContext context) throws DBException {
        try {

            String fn = "DBService.setDownLoadDGReference";
            String downloadConfigDg;
            log.info(fn + "Setting Download DG Reference from DB");
            String key;
            QueryStatus status;
            String protocol = context.getAttribute(SdcArtifactHandlerConstants.DEVICE_PROTOCOL);
            if (StringUtils.isBlank(protocol)) {
                log.info(fn + " :: Protocol is Blank!! Returning without querying DB");
                throw new ConfigurationException(fn + ":: Protocol is Blank!! Returning without querying DB");
            }
            key = "select download_config_dg from " + SdcArtifactHandlerConstants.DB_DOWNLOAD_DG_REFERENCE
                + " where protocol = '" + protocol + "'";
            SvcLogicContext localContext = new SvcLogicContext();
            status = serviceLogic.query("SQL", false, null, key, null, null, localContext);
            if (status == QueryStatus.FAILURE) {
                log.info(fn + ":: Error retrieving download_config_dg");
                throw new SvcLogicException("Error retrieving download_config_dg");
            }
            if (status == QueryStatus.NOT_FOUND) {
                log.info(fn + ":: NOT_FOUND! No data found for download_config_dg!!");
                throw new SvcLogicException(fn + ":: NOT_FOUND! No data found for download_config_dg!");
            }
            downloadConfigDg = localContext.getAttribute("download-config-dg");
            log.info(fn + "download_config_dg::" + downloadConfigDg);
            return downloadConfigDg;
        } catch (SvcLogicException | ConfigurationException e) {
            throw new DBException("An error occurred when getting DG reference", e);
        }
    }

    public void cleanUpVnfcReferencesForVnf(SvcLogicContext context) throws SvcLogicException {
        try {
            String key1 = "delete from " + SdcArtifactHandlerConstants.DB_VNFC_REFERENCE + " where action = $"
                + SdcArtifactHandlerConstants.ACTION + " and vnf_type = $" + SdcArtifactHandlerConstants.VNF_TYPE;
            log.debug("Action : " + context.getAttribute(SdcArtifactHandlerConstants.ACTION));
            log.debug("vnfType: " + context.getAttribute(SdcArtifactHandlerConstants.VNF_TYPE));
            QueryStatus status;
            log.info("cleanUpVnfcReferencesForVnf()::Query:" + key1);
            if (serviceLogic != null) {
                status = serviceLogic.save("SQL", false, false, key1, null, null, context);
                if (status.toString().equals(FAILURE_PARAM)) {
                    log.debug("Error deleting from VNFC_REFERENCE table");
                    throw new SvcLogicException("Error While processing VNFC_REFERENCE table ");
                }
            }
        } catch (Exception e) {
            log.debug("Error deleting from VNFC_REFERENCE table  : "
                + context.getAttribute(SdcArtifactHandlerConstants.ACTION) + " and "
                + context.getAttribute(SdcArtifactHandlerConstants.VNF_TYPE), e);
        }
    }


    public boolean isUpdateRequiredForTemplates(String queryPart, SvcLogicContext context, String db) throws DBException {
        try {
            log.info("Checking if Update required for this data");
            log.info("db" + db);
            log.info("ACTION=" + context.getAttribute(SdcArtifactHandlerConstants.ACTION));
            log.info("VNF_TYPE=" + context.getAttribute(SdcArtifactHandlerConstants.VNF_TYPE));
            log.info("");
            String whereClause;
            QueryStatus status;
            whereClause = WHERE_VNF_TYPE_QUERY_STR + SdcArtifactHandlerConstants.VNF_TYPE ;
            whereClause = resolveWhereClause(context, db, whereClause);
            whereClause += queryPart;
            if (validate(db)) {
                if (!db.equals(SdcArtifactHandlerConstants.DB_DEVICE_AUTHENTICATION)) {
                    String key = "select COUNT(*) from " + db + whereClause;
                    log.info("SELECT String : " + key);
                    status = serviceLogic.query("SQL", false, null, key, null, null, context);
                    checkForFailure(db, status);
                    String count = context.getAttribute("COUNT(*)");
                    log.info("Number of row Returned : " + count + ": " + status + ":");
                    return tryAddCountAttribute(context, count);
                }
            }
            log.info("Problems validating DB and/or Context ");
            return false;

        } catch (SvcLogicException e) {
            throw new DBException("An error occurred while checking for artifact update", e);
        }
    }

    public String createQueryListForTemplateIds(String modelId) {
        String queryPart = " AND ARTIFACT_NAME like '%_" + modelId+".%'";
        return queryPart;
    }
}
