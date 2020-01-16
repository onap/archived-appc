/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications (C) 2019 Ericsson
 * Modifications (C) 2019 IBM
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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.design.dbervices;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.json.JSONObject;
import org.onap.appc.design.data.ArtifactInfo;
import org.onap.appc.design.data.DesignInfo;
import org.onap.appc.design.data.DesignResponse;
import org.onap.appc.design.data.StatusInfo;
import org.onap.appc.design.data.UserPermissionInfo;
import org.onap.appc.design.services.util.ArtifactHandlerClient;
import org.onap.appc.design.services.util.DesignServiceConstants;
import org.onap.ccsdk.sli.adaptors.resource.sql.SqlResource;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;

public class DesignDBService {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(DesignDBService.class);
    private static DesignDBService dgGeneralDBService;
    private static ArtifactHandlerFactory artifactHandlerFactory = new ArtifactHandlerFactory();
    private static final String SUCCESS_JSON = "{\"update\" : \"success\" } ";
    private static final String STATUS = "STATUS";
    private static final String INFO_STR = "Info: ";
    private static final String DB_OPERATION_ERROR = "Error during DB operation: ";
    private static final String VNFC_TYPE = "vnfc-type";
    private static final String VNF_TYPE = "vnf-type";
    private static final String QUERY_STR = "Query String: ";
    private static final String USER_ID = "userID";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_PERMISSION = "permission";

    private SvcLogicResource serviceLogic;
    private DbService dbservice;
    public static DesignDBService initialise() {
        if (dgGeneralDBService == null) {
            dgGeneralDBService = new DesignDBService();
        }
        return dgGeneralDBService;
    }

    private DesignDBService() {
        if (serviceLogic == null) {
            serviceLogic = new SqlResource();
        }
    }

    public String execute(String action, String payload, String requestId) throws Exception {

        log.info("Received execute request for action: " + action + " with Payload: " + payload);
        RequestValidator.validate(action, payload);
        String response;
        dbservice = new DbService();
        switch (action) {
            case DesignServiceConstants.GETDESIGNS:
                response = getDesigns(payload, requestId);
                break;
            case DesignServiceConstants.GETAPPCTIMESTAMPUTC:
                response =  getAppcTimestampUTC(requestId);
                break;
            case DesignServiceConstants.ADDINCART:
                response = setInCart(payload, requestId);
                break;
            case DesignServiceConstants.GETARTIFACTREFERENCE:
                response = getArtifactReference(payload, requestId);
                break;
            case DesignServiceConstants.GETARTIFACT:
                response = getArtifact(payload, requestId);
                break;
            case DesignServiceConstants.GETGUIREFERENCE:
                response = getGuiReference(payload, requestId);
                break;
            case DesignServiceConstants.GETSTATUS:
                response = getStatus(payload, requestId);
                break;
            case DesignServiceConstants.SETSTATUS:
                response = setStatus(payload, requestId);
                break;
            case DesignServiceConstants.UPLOADARTIFACT:
                response = uploadArtifact(payload, requestId);
                break;
            case DesignServiceConstants.SETPROTOCOLREFERENCE:
                response = setProtocolReference(payload, requestId);
                break;
            case DesignServiceConstants.UPLOADADMINARTIFACT:
                response = uploadAdminArtifact(payload, requestId);
                break;
            case DesignServiceConstants.CHECKVNF:
                response = checkVNF(payload, requestId);
                break;
            case DesignServiceConstants.RETRIEVEVNFPERMISSIONS:
                response = retrieveVnfPermissions(payload, requestId);
                break;
            case DesignServiceConstants.SAVEVNFPERMISSIONS:
                response = saveUserPermissionInfo(payload, requestId);
                break;
            default:
                throw new DBException("Action " + action + " not found while processing request");

        }
        return response;
    }

    private String saveUserPermissionInfo(String payload, String requestId) throws Exception {
        try {
            if (requestId == null || requestId.isEmpty()) {
                throw new DBException("requestId in saveUserPermissionInfo request is null or Blank");
            }
            log.info("Received Save User Permission from " + requestId + " with payload " + payload);
            Date startTime = new Date();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String dbDate = dateFormat.format(startTime);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode payloadObject = objectMapper.readTree(payload);
            String vnf_type = payloadObject.get(DesignServiceConstants.VNF_TYPE).textValue();
            String modifier = payloadObject.get(DesignServiceConstants.CREATORUSERID).textValue();
            JsonNode users = payloadObject.get("users");
            if (users == null || !users.isArray()) {
                throw new DBException("Users list is not provided in the input payload");
            }
            for (JsonNode node : users) {
                String userId = node.get(DesignServiceConstants.USER_ID).textValue();
                String permission = node.get(DesignServiceConstants.PERMISSION).textValue();
                ArrayList<String> argList = new ArrayList<>();
                argList.add(vnf_type);
                argList.add(userId);
                log.info("Checking User - " + userId + " current permissions in db for this vnf type");
                String queryString = "SELECT PERMISSION FROM DT_USER_PERMISSIONS WHERE VNF_TYPE = ? AND USER_ID = ?";
                log.info(QUERY_STR + queryString);
                String user_permission = null;
                int rowCount = 0;
                try (ResultSet data = dbservice.getDBData(queryString, argList)) {
                    while (data.next()) {
                        rowCount++;
                        user_permission = data.getString("PERMISSION");
                        if (Strings.isNullOrEmpty(permission)) {
                            log.info("Received request to delete db record for User - " + userId);
                            ArrayList<String> delArgList = new ArrayList<>();
                            delArgList.add(vnf_type);
                            delArgList.add(userId);
                            String deleteQuery = "DELETE FROM DT_USER_PERMISSIONS WHERE VNF_TYPE = ? AND USER_ID = ?";
                            log.info(QUERY_STR + deleteQuery);
                            log.info("Arguments List: " + delArgList);
                            boolean status = dbservice.updateDBData(deleteQuery, delArgList);
                            if (!status) {
                                throw new DBException("Error while deleting record from DT_USER_PERMISSIONS");
                            } else {
                                log.info("Record deleted");
                            }
                        } else if (user_permission.matches(permission)) {
                            log.info("User " + userId + " permission record found in db for same vnf_type " + vnf_type
                                    + ". No update needed.");
                        } else {
                            log.info("User's permission record will be updated. New permission: " + permission
                                    + " for user " + userId + " as requested by " + requestId
                                    + " will be saved to database.");
                            ArrayList<String> updateArgList = new ArrayList<>();
                            updateArgList.add(permission);
                            updateArgList.add(modifier);
                            updateArgList.add(dbDate);
                            updateArgList.add(vnf_type);
                            updateArgList.add(userId);

                            String updateQuery =
                                    "UPDATE DT_USER_PERMISSIONS SET PERMISSION = ?, MODIFIER = ?, DATE_MODIFIED = ?"
                                    + " WHERE VNF_TYPE = ? AND USER_ID = ?";
                            log.info(QUERY_STR + updateQuery);
                            log.info("Arguments List: " + updateArgList);
                            boolean updateStatus = dbservice.updateDBData(updateQuery, updateArgList);
                            if (!updateStatus) {
                                throw new DBException("Error while updating User Permissions");
                            }
                        }
                    }
                }
                if (rowCount == 0 && !(Strings.isNullOrEmpty(permission))) {
                    log.info("User not found in database for this vnf_type. The new permission " + permission
                            + " for user " + userId + " and vnf_type " + vnf_type + " as requested by " + requestId
                            + " will be saved to database.");
                    ArrayList<String> insertArgList = new ArrayList<>();
                    insertArgList.add(vnf_type);
                    insertArgList.add(userId);
                    insertArgList.add(permission);
                    insertArgList.add(modifier);
                    String insertQuery =
                            "INSERT INTO DT_USER_PERMISSIONS (VNF_TYPE, USER_ID, PERMISSION, DATE_MODIFIED, MODIFIER)"
                            + " VALUES (?, ?, ?, sysdate(), ?)";
                    log.info(QUERY_STR + insertQuery);
                    log.info("Arguments List: " + insertArgList);
                    boolean updateStatus = dbservice.updateDBData(insertQuery, insertArgList);
                    if (!updateStatus) {
                        throw new DBException("Error while inserting record for User Permissions");
                    }
                }
            }
        } catch (Exception e) {
            log.error("An error occurred in saveUserPermissionInfo " + e.getMessage(), e);
            throw e;
        }
        return SUCCESS_JSON;
    }

    private String checkVNF(String payload, String requestId) throws Exception {
        try {
            log.info("Got into Check VNF Request with payload: " + payload);
            if (payload == null || payload.isEmpty()) {
                throw new DBException("Payload in CheckVNF request is null or Blank");
            }
            if (requestId == null || requestId.isEmpty()) {
                throw new DBException("requestId in CheckVNF request is null or Blank");
            }
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode payloadObject = objectMapper.readTree(payload);
            String vnfType = payloadObject.get("vnf-type").textValue();

            log.info("Check VNF Request with VNF TYPE: " + vnfType);

            ArrayList<String> argList = new ArrayList<>();
            argList.add(vnfType);

            String queryString =
                    "SELECT DT_ACTION_STATUS_ID, USER FROM sdnctl.DT_ACTION_STATUS WHERE VNF_TYPE = ?"
                    + " ORDER BY DT_ACTION_STATUS_ID DESC LIMIT 1;";

            log.info(QUERY_STR + queryString);
            try (ResultSet data = dbservice.getDBData(queryString, argList)) {

                int rowCount = 0;
                String user = null;
                String dtActionStatusId = null;

                while (data.next()) {
                    rowCount++;
                    user = data.getString("USER");
                    dtActionStatusId = data.getString("DT_ACTION_STATUS_ID");
                }

                log.debug("DT_ACTION_STATUS_ID " + dtActionStatusId + " user " + user);

                JSONObject jObject = new JSONObject();


                if (rowCount == 0) {
                    log.debug("vnf-type not present in APPC DB, row Count: " + rowCount);
                    jObject.put("result", "No");
                } else {
                    log.debug("vnf-type present in APPC DB, row Count: " + rowCount);
                    jObject.put("result", "Yes");
                    jObject.put("user", user);
                }

                log.info("Check VNF result: " + jObject.toString());
                return jObject.toString();

            }
        } catch (Exception e) {
            log.error("An error occurred in checkVNF " + e.getMessage(), e);
            throw e;
        }

    }

    private String uploadAdminArtifact(String payload, String requestId) throws Exception {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
            JsonNode payloadObject = objectMapper.readTree(payload);
            log.info("Got upload Admin Artifact with requestId: " + requestId + " & Payload: "
                    + payloadObject.asText());

            if (Strings.isNullOrEmpty(requestId)) {
                throw new DBException("Request-id is missing in the uploadAdminArtifact payload.");
            }

            ArtifactHandlerClient ac = new ArtifactHandlerClient();
            String requestString = ac.createArtifactData(payload, requestId);
            ac.execute(requestString, "POST");



            int sdcArtifactId = getSDCArtifactIDbyRequestID(requestId);
            if (sdcArtifactId == 0) {
                throw new DBException("Error occurred while validating/Saving the artifact to SDC_ARTIFACTS"
                        + " or getting SDC_ARTIFACTS_ID.");
            }

            JsonNode json = payloadObject.get(DesignServiceConstants.USER_ID);
            if (json == null) {
                throw new DBException("User Id is null");
            } else if (json.asText().trim().isEmpty()) {
                log.info("UserId in Admin Artifact is blank, User Id: " + json.asText());
                throw new DBException("User Id is blank");
            }

            int sdcReferenceId = 0;
            createArtifactTrackingRecord(payload, requestId, sdcArtifactId, sdcReferenceId);

        } catch (Exception e) {
            log.error("An error occurred in uploadAdminArtifact: " + e.getMessage(), e);
            throw e;
        }
        return SUCCESS_JSON;
    }

    private String getAppcTimestampUTC(String requestId) throws Exception {
        log.info("Starting getAppcTimestampUTC: requestId: " + requestId);
        java.util.TimeZone gmtTZ = java.util.TimeZone.getTimeZone("GMT");
        java.text.SimpleDateFormat formatter =
                new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        formatter.setTimeZone(gmtTZ);
        java.util.Date dateVal = new java.util.Date();
        log.info("getAppcTimestampUTC: current local Date: [" + dateVal + "]");
        String timeStr = formatter.format(dateVal);
        log.info("getAppcTimestampUTC: returning: [" + timeStr + "]");
        return timeStr;
    }

    private String setInCart(String payload, String requestId) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode payloadObject = objectMapper.readTree(payload);
        ArrayList<String> argList = new ArrayList<>();
        argList.add(payloadObject.get(DesignServiceConstants.INCART).textValue());
        argList.add(payloadObject.get(DesignServiceConstants.VNF_TYPE).textValue());

        String queryString =
                "UPDATE DT_ARTIFACT_TRACKING SET INCART = ? WHERE ASDC_REFERENCE_ID IN"
                + " (SELECT ASDC_REFERENCE_ID FROM ASDC_REFERENCE_ID WHERE VNF_TYPE = ?";

        if (payloadObject.get(DesignServiceConstants.VNFC_TYPE) != null
                && !payloadObject.get(DesignServiceConstants.VNFC_TYPE).textValue().isEmpty()) {
            queryString += " AND VNFC_TYPE = ?";
            argList.add(payloadObject.get(DesignServiceConstants.VNFC_TYPE).textValue());
        }
        queryString += ") AND USER = ?";
        argList.add(payloadObject.get(DesignServiceConstants.USER_ID).textValue());
        log.info(QUERY_STR + queryString);
        boolean data = dbservice.updateDBData(queryString, argList);

        if (!data) {
            throw new DBException("Error while updating ProtocolReference");
        }
        return SUCCESS_JSON;
    }

    private String setProtocolReference(String payload, String requestId) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode payloadObject = objectMapper.readTree(payload);
        ArrayList<String> argList = new ArrayList<>();

        argList.add(payloadObject.get(DesignServiceConstants.ACTION).textValue());
        argList.add(payloadObject.get(DesignServiceConstants.ACTION_LEVEL).textValue());
        argList.add(payloadObject.get(DesignServiceConstants.VNF_TYPE).textValue());
        argList.add(payloadObject.get(DesignServiceConstants.PROTOCOL).textValue());

        String queryString = "DELETE FROM PROTOCOL_REFERENCE WHERE ACTION = ? AND ACTION_LEVEL AND VNF_TYPE = ? AND PROTOCOL = ?";

        log.info("Delete Query String: " + queryString);
        boolean data;

        log.info("Record Deleted");

        if (payloadObject.get(DesignServiceConstants.TEMPLATE) != null &&
            !payloadObject.get(DesignServiceConstants.TEMPLATE).textValue().isEmpty()) {

            argList.add(payloadObject.get(DesignServiceConstants.TEMPLATE).textValue());
        } else {
            argList.add("NO");
        }

        if (payloadObject.get(DesignServiceConstants.VNFC_TYPE) != null &&
            !payloadObject.get(DesignServiceConstants.VNFC_TYPE).textValue().isEmpty()) {

            queryString += " AND VNFC_TYPE = ?";
        }
        queryString += ")";
        log.info(QUERY_STR + queryString);
        data = dbservice.updateDBData(queryString, argList);

        if (!data) {
            throw new DBException("Error while updating ProtocolReference");
        }
        return SUCCESS_JSON;
    }

    private String uploadArtifact(String payload, String requestId) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        JsonNode payloadObject = objectMapper.readTree(payload);
        log.info("Got upload Artifact with Payload: " + payloadObject.asText());
        try {
            ArtifactHandlerClient ac = artifactHandlerFactory.ahi();
            String requestString = ac.createArtifactData(payload, requestId);
            ac.execute(requestString, "POST");
            int sdcArtifactId = getSDCArtifactIDbyRequestID(requestId);
            int sdcReferenceId = getSDCReferenceID(payload);
            createArtifactTrackingRecord(payload, requestId, sdcArtifactId, sdcReferenceId);
            String status = getDataFromActionStatus(payload, STATUS);
            if (status == null || status.isEmpty()) {
                log.info("Action Status is: " + status);
                setActionStatus(payload, "Not Tested");
            }
            linkstatusRelationShip(sdcArtifactId, sdcReferenceId, payload);
            savePermissionInfo(payload, requestId);
        } catch (Exception e) {
            log.error("An error occurred in uploadArtifact", e);
            throw e;
        }
        return SUCCESS_JSON;

    }
    private void savePermissionInfo(String payload, String requestId) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode payloadObject = objectMapper.readTree(payload);
        ArrayList<String> argList = new ArrayList<>();
        argList.add(payloadObject.get(DesignServiceConstants.VNF_TYPE).textValue());
        argList.add(payloadObject.get(DesignServiceConstants.USER_ID).textValue());
        log.info("Entered savePermissionInfo from uploadArtifact with payload " + payload);
        String queryString = "SELECT PERMISSION FROM DT_USER_PERMISSIONS WHERE VNF_TYPE = ? AND USER_ID = ?";
        log.info(QUERY_STR + queryString);
        try (ResultSet data = dbservice.getDBData(queryString, argList)) {
            String user_permission = null;
            int rowCount = 0;
            while (data.next()) {
                rowCount++;
                user_permission = data.getString("PERMISSION");
                log.info("User exists in database with permission = " + user_permission);
            }
            if (rowCount == 0) {
                log.info("No record found in database");
                log.info("Inserting one record in database");
                String permission = "owner";
                ArrayList<String> insertArgList = new ArrayList<>();
                insertArgList.add(payloadObject.get(DesignServiceConstants.VNF_TYPE).textValue());
                insertArgList.add(payloadObject.get(DesignServiceConstants.USER_ID).textValue());
                insertArgList.add(permission);
                insertArgList.add(payloadObject.get(DesignServiceConstants.USER_ID).textValue());
                String insertQuery =
                        "INSERT INTO DT_USER_PERMISSIONS (VNF_TYPE, USER_ID, PERMISSION, DATE_MODIFIED, MODIFIER)"
                        + " VALUES (?, ?, ?, sysdate(), ?)";
                log.info(QUERY_STR + insertQuery);
                log.info("Arguments List: " + insertArgList);
                boolean updateStatus = dbservice.updateDBData(insertQuery, insertArgList);
                if (!updateStatus) {
                    throw new DBException("Error while inserting record to DT_USER_PERMISSIONS");
                }
            }
        }
    }

    private void linkstatusRelationShip(int sdcArtifactId, int sdcReferenceId, String payload) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode payloadObject = objectMapper.readTree(payload);
        ArrayList<String> argList = new ArrayList<>();
        argList.add(String.valueOf(sdcArtifactId));
        argList.add(String.valueOf(sdcReferenceId));
        argList.add(payloadObject.get(DesignServiceConstants.VNF_TYPE).textValue());
        argList.add(payloadObject.get(DesignServiceConstants.ACTION).textValue());
        argList.add(payloadObject.get(DesignServiceConstants.USER_ID).textValue());

        String queryString =
                "INSERT INTO DT_STATUS_RELATIONSHIP (DT_ARTIFACT_TRACKING_ID, DT_ACTION_STATUS_ID) VALUES"
                + " ((SELECT DT_ARTIFACT_TRACKING_ID FROM DT_ARTIFACT_TRACKING"
                    + " WHERE ASDC_ARTIFACTS_ID = ? AND ASDC_REFERENCE_ID = ?),"
                + " (SELECT DT_ACTION_STATUS_ID FROM DT_ACTION_STATUS"
                    + " WHERE VNF_TYPE = ? AND ACTION = ? AND USER = ?";

        if (payloadObject.get(DesignServiceConstants.VNFC_TYPE) != null
                && !payloadObject.get(DesignServiceConstants.VNFC_TYPE).textValue().isEmpty()) {
            queryString += " AND VNFC_TYPE = ?";
        }
        queryString += " GROUP BY VNF_TYPE HAVING COUNT(VNF_TYPE)>=1 ))";
        log.info(QUERY_STR + queryString);
        boolean data = dbservice.updateDBData(queryString, argList);

        if (!data) {
            throw new DBException("Error while updating Relationship table");
        }

    }

    private int getSDCReferenceID(String payload) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode payloadObject = objectMapper.readTree(payload);
        ArrayList<String> argList = new ArrayList<>();
        argList.add(payloadObject.get(DesignServiceConstants.VNF_TYPE).textValue());

        argList.add(payloadObject.get(DesignServiceConstants.ARTIFACT_TYPE).textValue());
        argList.add(payloadObject.get(DesignServiceConstants.ARTIFACT_NAME).textValue());

        String queryString =
                "SELECT ASDC_REFERENCE_ID FROM ASDC_REFERENCE WHERE VNF_TYPE = ?"
                + " AND ARTIFACT_TYPE = ? AND ARTIFACT_NAME = ?";

        if (payloadObject.get(DesignServiceConstants.ACTION) != null
                && !payloadObject.get(DesignServiceConstants.ACTION).textValue().isEmpty()) {
            argList.add(payloadObject.get(DesignServiceConstants.ACTION).textValue());
            queryString += " AND ACTION = ?";
        }
        if (payloadObject.get(DesignServiceConstants.VNFC_TYPE) != null
                && !payloadObject.get(DesignServiceConstants.VNFC_TYPE).textValue().isEmpty()) {
            argList.add(payloadObject.get(DesignServiceConstants.VNFC_TYPE).textValue());
            queryString += " AND VNFC_TYPE = ?";
        }

        log.info(QUERY_STR + queryString);
        try (ResultSet data = dbservice.getDBData(queryString, argList)) {
            int sdcReferenceId = 0;
            while (data.next()) {
                sdcReferenceId = data.getInt("ASDC_REFERENCE_ID");
            }
            log.info("Got sdcReferenceId = " + sdcReferenceId);
            return sdcReferenceId;
        }
    }

    private String getDataFromActionStatus(String payload, String dataValue) throws Exception {
        String status = null;
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode payloadObject = objectMapper.readTree(payload);
        ArrayList<String> argList = new ArrayList<>();
        argList.add(payloadObject.get(DesignServiceConstants.VNF_TYPE).textValue());
        argList.add(payloadObject.get(DesignServiceConstants.ACTION).textValue());
        argList.add(payloadObject.get(DesignServiceConstants.USER_ID).textValue());
        String queryString =
                "SELECT " + dataValue + " FROM DT_ACTION_STATUS WHERE VNF_TYPE = ? AND ACTION = ? AND USER = ?";
        if (payloadObject.get(DesignServiceConstants.VNFC_TYPE) != null
                && !payloadObject.get(DesignServiceConstants.VNFC_TYPE).textValue().isEmpty()) {
            argList.add(payloadObject.get(DesignServiceConstants.VNFC_TYPE).textValue());
            queryString += " AND VNFC_TYPE = ?";
        }
        log.info(QUERY_STR + queryString);
        try (ResultSet data = dbservice.getDBData(queryString, argList)) {
            while (data.next()) {
                status = data.getString(STATUS);
            }
            log.info("DT_ACTION_STATUS Status = " + status);
            return status;
        }
    }

    private void setActionStatus(String payload, String status) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode payloadObject = objectMapper.readTree(payload);
        ArrayList<String> argList = new ArrayList<>();
        argList.add(payloadObject.get(DesignServiceConstants.ACTION).textValue());
        argList.add(payloadObject.get(DesignServiceConstants.VNF_TYPE).textValue());

        String insertQuery = " INSERT INTO DT_ACTION_STATUS (ACTION, VNF_TYPE, VNFC_TYPE, USER, TECHNOLOGY, UPDATED_DATE, STATUS) VALUES (?,?,?,?,?,sysdate() , ?); ";
        if (payloadObject.get(DesignServiceConstants.VNFC_TYPE) != null
                && !payloadObject.get(DesignServiceConstants.VNFC_TYPE).textValue().isEmpty()) {
            argList.add(payloadObject.get(DesignServiceConstants.VNFC_TYPE).textValue());
            log.info("Vnfc-Type: " + payloadObject.get(DesignServiceConstants.VNFC_TYPE).textValue());
        } else {
            argList.add(null);
        }
        argList.add(payloadObject.get(DesignServiceConstants.USER_ID).textValue());
        if (payloadObject.get(DesignServiceConstants.TECHNOLOGY) != null
                && !payloadObject.get(DesignServiceConstants.TECHNOLOGY).textValue().isEmpty()) {
            argList.add(payloadObject.get(DesignServiceConstants.TECHNOLOGY).textValue());
        } else {
            argList.add(null);
        }
        argList.add(status);

        log.info("QueryString: " + insertQuery);
        log.info("Arguments List: " + argList);
        boolean updateStatus = dbservice.updateDBData(insertQuery, argList);
        if (!updateStatus) {
            throw new DBException("Error while updating Action Status");
        }
    }

    private void createArtifactTrackingRecord(String payload, String requestId, int sdcArtifactId, int sdcReferenceId)
            throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode payloadObject = objectMapper.readTree(payload);

        ArrayList<String> argList = new ArrayList<>();
        argList.add(String.valueOf(sdcArtifactId));
        argList.add(String.valueOf(sdcReferenceId));
        argList.add(payloadObject.get(DesignServiceConstants.USER_ID).textValue());
        if (payloadObject.get(DesignServiceConstants.TECHNOLOGY) != null
                && !payloadObject.get(DesignServiceConstants.TECHNOLOGY).textValue().isEmpty()) {
            argList.add(payloadObject.get(DesignServiceConstants.TECHNOLOGY).textValue());
        } else {
            argList.add("");
        }

        if (payloadObject.get(DesignServiceConstants.PROTOCOL) != null
                && !payloadObject.get(DesignServiceConstants.PROTOCOL).textValue().isEmpty()) {
            argList.add(payloadObject.get(DesignServiceConstants.PROTOCOL).textValue());
        } else {
            argList.add("");
        }

        String queryString =
                "INSERT INTO DT_ARTIFACT_TRACKING (ASDC_ARTIFACTS_ID, ASDC_REFERENCE_ID, USER, TECHNOLOGY,"
                + " CREATION_DATE, UPDATED_DATE, ARTIFACT_STATUS, PROTOCOL, IN_CART)"
                + " VALUES (?, ?, ?, ?, sysdate(), sysdate(), 'Created', ?, 'N')";

        log.info(QUERY_STR + queryString);
        boolean data = dbservice.updateDBData(queryString, argList);
        if (!data) {
            throw new DBException("Error Updating DT_ARTIFACT_TRACKING");
        }
    }

    private int getSDCArtifactIDbyRequestID(String requestId) throws Exception {
        log.info("Starting getArtifactIDbyRequestID DB Operation");
        int artifactId = 0;
        try {
            ArrayList<String> argList = new ArrayList<>();
            argList.add("TLSUUID" + requestId);
            String queryString = "SELECT ASDC_ARTIFACTS_ID FROM ASDC_ARTIFACTS where SERVICE_UUID = ?";
            log.info(QUERY_STR + queryString + " & UUID: " + "TLSUUID" + requestId);
            try (ResultSet data = dbservice.getDBData(queryString, argList)) {
                while (data.next()) {
                    artifactId = data.getInt("ASDC_ARTIFACTS_ID");
                }
            }
        } catch (Exception e) {
            log.error("An error occurred in getSDCArtifactIDbyRequestID", e);
            throw e;
        }
        log.info("Got SDC_ARTIFACTS_ID As: " + artifactId);
        return artifactId;
    }


    private String getArtifact(String payload, String requestId) throws Exception {
        log.info("Starting getArtifact DB Operation");
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode payloadObject = objectMapper.readTree(payload);
            String artifactName = payloadObject.get("artifact-name").textValue();
            ArrayList<String> argList = new ArrayList<>();
            argList.add(artifactName);
            argList.add(payloadObject.get("artifact-type").textValue());

            String queryString =
                    "SELECT INTERNAL_VERSION, ARTIFACT_CONTENT FROM ASDC_ARTIFACTS"
                    + " where ARTIFACT_NAME = ? AND ARTIFACT_TYPE = ?";

            log.info(QUERY_STR + queryString);
            String artifactContent = null;
            try (ResultSet data = dbservice.getDBData(queryString, argList)) {

                int rowCount = 0;
                int highestVersion = -1;

                while (data.next()) {
                    rowCount++;

                    int version = data.getInt("INTERNAL_VERSION");
                    if (highestVersion < version) {
                        artifactContent = data.getString("ARTIFACT_CONTENT");
                        highestVersion = version;
                    }
                }

                log.debug("No of rows: " + rowCount + " highest Internal Version " + highestVersion);

                if (rowCount == 0) {
                    throw new DBException(
                            "Sorry!!! APPC DB doesn't have any artifact Named: " + artifactName);
                }

                if (artifactContent == null || artifactContent.isEmpty()) {
                    throw new DBException("Sorry!!! Artifact Content is stored blank in APPC DB for " + artifactName
                        + " and Internal version " + highestVersion);
                }
            }

            DesignResponse designResponse = new DesignResponse();
            List<ArtifactInfo> artifactInfoList = new ArrayList<>();
            ArtifactInfo artifactInfo = new ArtifactInfo();
            artifactInfo.setArtifact_content(artifactContent);
            artifactInfoList.add(artifactInfo);
            designResponse.setArtifactInfo(artifactInfoList);
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = mapper.writeValueAsString(designResponse);
            log.debug("End of getArtifact: " + INFO_STR + jsonString);
            return jsonString;
        } catch (Exception e) {
            log.error(DB_OPERATION_ERROR, e);
            throw e;
        }
    }

    private String setStatus(String payload, String requestId) throws Exception {

        log.info("Starting getStatus DB Operation");
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode payloadObject = objectMapper.readTree(payload);

            ArrayList<String> argList = new ArrayList<>();
            argList.add(payloadObject.get("artifact_status").textValue());
            argList.add(payloadObject.get("action_status").textValue());

            argList.add(payloadObject.get(USER_ID).textValue());
            argList.add(payloadObject.get("vnf-type").textValue());

            String queryString =
                " UPDATE DT_ARTIFACT_TRACKING DAT, DT_STATUS_RELATIONSHIP DSR  SET DAT.ARTIFACT_STATUS = ? , DAS.DT_ACTION_STATUS = ? "
                    + " where  DAT.USER = DAS.USER and DSR.DT_ARTIFACT_TRACKING_ID = DAT.DT_ARTIFACT_TRACKING_ID "
                    + " and DSR.DT_ACTION_STATUS_ID = DAS.DT_ACTION_STATUS_ID and DAT.USER = ? "
                    + " and  DAS.VNF_TYPE = ? ";

            if (payloadObject.get(VNFC_TYPE) != null && !payloadObject.get(VNFC_TYPE).textValue().isEmpty()) {
                argList.add(payloadObject.get(VNFC_TYPE).textValue());
                queryString += " and DAS.VNFC_TYPE = ?";
            }

            log.info(QUERY_STR + queryString);

            DesignResponse designResponse = new DesignResponse();
            designResponse.setUserId(payloadObject.get(USER_ID).textValue());
            boolean update = dbservice.updateDBData(queryString, argList);
            if (!update) {
                throw new DBException("Sorry .....Something went wrong while updating the Status");
            }

            ObjectMapper mapper = new ObjectMapper();
            String jsonString = mapper.writeValueAsString(designResponse);
            log.info(INFO_STR + jsonString);
            return jsonString;
        } catch (Exception e) {
            log.error(DB_OPERATION_ERROR, e);
            throw e;
        }
    }

    private String getStatus(String payload, String requestId) throws Exception {
        log.info("Starting getStatus DB Operation");
        try {
            String vnfcType = null;
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode payloadObject = objectMapper.readTree(payload);
            String userID = payloadObject.get(USER_ID).textValue();
            String vnfType = payloadObject.get("vnf-type").textValue();
            if (payloadObject.get(VNFC_TYPE) != null) {
                vnfcType = payloadObject.get(VNFC_TYPE).textValue();
            }
            ArrayList<String> argList = new ArrayList<>();

            argList.add(userID);
            argList.add(vnfType);

            String queryString =
                    "SELECT DAS.VNF_TYPE, DAS.VNFC_TYPE, DAS.STATUS, DAS.ACTION, DAT.ARTIFACT_STATUS"
                    + "  from  DT_ACTION_STATUS DAS, DT_ARTIFACT_TRACKING DAT, DT_STATUS_RELATIONSHIP DSR"
                    + "  where  DAT.USER = DAS.USER and DSR.DT_ARTIFACT_TRACKING_ID = DAT.DT_ARTIFACT_TRACKING_ID"
                    + " and DSR.DT_ACTION_STATUS_ID = DAS.DT_ACTION_STATUS_ID and DAT.USER = ?"
                    + " and DAS.VNF_TYPE = ?";

            if (vnfcType != null && !vnfcType.isEmpty()) {
                argList.add(vnfcType);
                queryString = queryString + " and DAS.VNFC_TYPE = ?";
            }

            log.info(QUERY_STR + queryString);

            DesignResponse designResponse = new DesignResponse();
            designResponse.setUserId(userID);
            List<StatusInfo> statusInfoList = new ArrayList<>();
            try (ResultSet data = dbservice.getDBData(queryString, argList)) {
                while (data.next()) {
                    StatusInfo statusInfo = new StatusInfo();
                    statusInfo.setAction(data.getString("ACTION"));
                    statusInfo.setAction_status(data.getString(STATUS));
                    statusInfo.setArtifact_status(data.getString("ARTIFACT_STATUS"));
                    statusInfo.setVnf_type(data.getString("VNF_TYPE"));
                    statusInfo.setVnfc_type(data.getString("VNFC_TYPE"));
                    statusInfoList.add(statusInfo);
                }

                if (statusInfoList.isEmpty()) {
                    throw new DBException(
                            "OOPS!!!! No VNF information available for VNF-TYPE: " + vnfType + " for User: " + userID);
                }
            }
            designResponse.setStatusInfoList(statusInfoList);
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = mapper.writeValueAsString(designResponse);
            log.info(INFO_STR + jsonString);
            return jsonString;
        } catch (SQLException e) {
            log.error(DB_OPERATION_ERROR, e);
            throw e;
        } catch (Exception e) {
            log.error(DB_OPERATION_ERROR + e.getMessage());
            log.error("Exception:", e);
            throw e;
        }
    }

    private String getGuiReference(String payload, String requestId) {
        // TODO Auto-generated method stub
        return null;
    }

    private String getArtifactReference(String payload, String requestId) {
        // TODO Auto-generated method stub
        return null;
    }

    private String getDesigns(String payload, String requestId) throws Exception {

        String queryString;
        log.info("Starting getDesigns DB Operation");

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode payloadObject = objectMapper.readTree(payload);
            String userID = payloadObject.get(USER_ID).textValue();
            String filterKey = null;
            if (payloadObject.hasNonNull("filter")) {
                filterKey = payloadObject.get("filter").textValue();
            }
            ArrayList<String> argList = new ArrayList<>();
            argList.add(userID);
            argList.add(userID);
            queryString =
                    "SELECT DISTINCT AR.VNF_TYPE, AR.VNFC_TYPE, DAT.PROTOCOL, DAT.IN_CART, AR.ACTION, AR.ARTIFACT_NAME,"
                    + " AR.ARTIFACT_TYPE, DUP.PERMISSION, DAS.USER  FROM  "
                    + DesignServiceConstants.DB_DT_ARTIFACT_TRACKING + " DAT, "
                    + DesignServiceConstants.DB_SDC_REFERENCE + " AR, " + DesignServiceConstants.DB_DT_USER_PERMISSIONS
                    + " DUP, " + DesignServiceConstants.DB_DT_ACTION_STATUS + " DAS "
                    + " WHERE  AR.VNF_TYPE = DUP.VNF_TYPE AND DAS.VNF_TYPE = DUP.VNF_TYPE"
                    + " AND DAT.ASDC_REFERENCE_ID = AR.ASDC_REFERENCE_ID AND DUP.USER_ID = ? AND AR.VNF_TYPE IN"
                    + " (SELECT DUP.VNF_TYPE FROM DT_USER_PERMISSIONS DUP"
                    + " WHERE DUP.PERMISSION IN('owner','contributor') AND DUP.USER_ID = ? GROUP BY VNF_TYPE)";

            if (filterKey != null) {
                queryString += " AND AR.ARTIFACT_NAME like '%" + filterKey + "%'";
            }
            queryString += " GROUP BY AR.VNF_TYPE, AR.ARTIFACT_NAME";

            log.info("QUERY FOR getDesigns: " + queryString);
            DesignResponse designResponse = new DesignResponse();
            designResponse.setUserId(userID);
            List<DesignInfo> designInfoList = new ArrayList<>();
            try (ResultSet data = dbservice.getDBData(queryString, argList)) {
                while (data.next()) {
                    DesignInfo designInfo = new DesignInfo();
                    designInfo.setInCart(data.getString("IN_CART"));
                    designInfo.setProtocol(data.getString("PROTOCOL"));
                    designInfo.setVnf_type(data.getString("VNF_TYPE"));
                    designInfo.setVnfc_type(data.getString("VNFC_TYPE"));
                    designInfo.setAction(data.getString("ACTION"));
                    designInfo.setArtifact_type(data.getString("ARTIFACT_TYPE"));
                    designInfo.setArtifact_name(data.getString("ARTIFACT_NAME"));
                    designInfo.setPermission(data.getString("PERMISSION"));
                    designInfo.setCreatorUserId(data.getString("USER"));
                    designInfoList.add(designInfo);
                }
                if (designInfoList.isEmpty()) {
                    throw new DBException(
                            "Welcome to CDT, Looks like you don't have Design Yet... Let's create some....");
                }
            }
            designResponse.setDesignInfoList(designInfoList);
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = mapper.writeValueAsString(designResponse);
            log.info(INFO_STR + jsonString);
            return jsonString;
        } catch (Exception e) {
            log.error("Error while Starting getDesigns DB operation:", e);
            throw e;
        }
    }

    private String retrieveVnfPermissions(String payload, String requestId) throws Exception {
        log.info("Starting retrieveVnfPermissions DB Operation");

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode payloadObject = objectMapper.readTree(payload);
            ArrayList<String> argList = new ArrayList<>();
            String vnfType = payloadObject.get(VNF_TYPE).textValue();
            argList.add(vnfType);

            String queryString = "SELECT USER_ID, PERMISSION FROM DT_USER_PERMISSIONS WHERE VNF_TYPE = ?";
            log.info(QUERY_STR + queryString);
            List<UserPermissionInfo> userPermList = new ArrayList<>();
            try (ResultSet data = dbservice.getDBData(queryString, argList)) {

                int rowCount = 0;

                while (data.next()) {
                    rowCount++;
                    UserPermissionInfo userPermInfo = new UserPermissionInfo();
                    userPermInfo.setUserID(data.getString(COLUMN_USER_ID));
                    userPermInfo.setPermission(data.getString(COLUMN_PERMISSION));
                    userPermList.add(userPermInfo);
                }
                log.info("Number of rows=" + rowCount + ", for vnf-type=" + vnfType);

                if (userPermList.isEmpty()) {
                    throw new DBException("No user permissions information available for VNF-TYPE: " + vnfType);
                }
            }
            DesignResponse designResponse = new DesignResponse();
            designResponse.setUsers(userPermList);
            designResponse.setVnfType(vnfType);
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = mapper.writeValueAsString(designResponse);
            log.info("End of retrieveVnfPermissions: " + INFO_STR + jsonString);
            return jsonString;

        } catch (Exception e) {
            log.error("Error while Starting retrieveVnfPermissions DB operation:", e);
            throw e;
        }
    }

    public static class ArtifactHandlerFactory {
        public ArtifactHandlerClient ahi() throws Exception {
            return new ArtifactHandlerClient();
        }
    }

}

