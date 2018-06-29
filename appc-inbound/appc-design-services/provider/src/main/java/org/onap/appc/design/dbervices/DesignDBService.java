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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.design.dbervices;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;
import org.onap.appc.design.data.ArtifactInfo;
import org.onap.appc.design.data.DesignInfo;
import org.onap.appc.design.data.DesignResponse;
import org.onap.appc.design.data.StatusInfo;
import org.onap.appc.design.services.util.ArtifactHandlerClient;
import org.onap.appc.design.services.util.DesignServiceConstants;
import org.onap.ccsdk.sli.adaptors.resource.sql.SqlResource;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;

public class DesignDBService {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(DesignDBService.class);
    private static DesignDBService dgGeneralDBService;

    private static final String SUCCESS_JSON = "{\"update\" : \"success\" } ";
    private static final String STATUS = "STATUS";
    private static final String INFO_STR = "Info : ";
    private static final String DB_OPERATION_ERROR = "Error while DB operation : ";
    private static final String VNFC_TYPE = "vnfc-type";
    private static final String QUERY_STR = "Query String :";
    private static final String USER_ID = "userID";

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

    public String execute(String action, String payload, String requestID) throws Exception {

        log.info("Received execute request for action : " + action + "  with Payload : " + payload);
        RequestValidator.validate(action, payload);
        String response;
        dbservice = new DbService();
        switch (action) {
            case DesignServiceConstants.GETDESIGNS:
                response = getDesigns(payload, requestID);
                break;
            case DesignServiceConstants.GETAPPCTIMESTAMPUTC:
                response =  getAppcTimestampUTC( requestID );
                break;
            case DesignServiceConstants.ADDINCART:
                response = setInCart(payload, requestID);
                break;
            case DesignServiceConstants.GETARTIFACTREFERENCE:
                response = getArtifactReference(payload, requestID);
                break;
            case DesignServiceConstants.GETARTIFACT:
                response = getArtifact(payload, requestID);
                break;
            case DesignServiceConstants.GETGUIREFERENCE:
                response = getGuiReference(payload, requestID);
                break;
            case DesignServiceConstants.GETSTATUS:
                response = getStatus(payload, requestID);
                break;
            case DesignServiceConstants.SETSTATUS:
                response = setStatus(payload, requestID);
                break;
            case DesignServiceConstants.UPLOADARTIFACT:
                response = uploadArtifact(payload, requestID);
                break;
            case DesignServiceConstants.SETPROTOCOLREFERENCE:
                response = setProtocolReference(payload, requestID);
                break;
            default:
                throw new DBException(" Action " + action + " not found while processing request ");

        }
        return response;
    }

    private String getAppcTimestampUTC( String requestID) throws Exception
    {
      log.info("Starting getAppcTimestampUTC: requestID:"+ requestID );
      try{
        java.util.TimeZone gmtTZ= java.util.TimeZone.getTimeZone("GMT");
        java.util.GregorianCalendar theCalendar=
          new java.util.GregorianCalendar( gmtTZ );
        int Year= theCalendar.get( Calendar.YEAR );
        int Month= 1 + theCalendar.get( Calendar.MONTH );
        int Day= theCalendar.get( Calendar.DAY_OF_MONTH );
        int Hour= theCalendar.get( Calendar.HOUR_OF_DAY );
        int Minute= theCalendar.get( Calendar.MINUTE );
        int Second= theCalendar.get( Calendar.SECOND );
        int Millisec= theCalendar.get( Calendar.MILLISECOND );
        java.lang.StringBuffer stbT= new java.lang.StringBuffer();
        java.text.DecimalFormat nfmt2= new java.text.DecimalFormat( "00" );
        stbT.append( String.valueOf(Year) );
        stbT.append( "-"+ nfmt2.format(Month) );
        stbT.append( "-"+ nfmt2.format(Day) );
        stbT.append( "T"+ nfmt2.format(Hour) );
        stbT.append( ":"+ nfmt2.format(Minute) );
        stbT.append( ":"+ nfmt2.format(Second) );
        stbT.append( "."+ nfmt2.format(Millisec) );
        stbT.append( "Z" );
        String timeStr= stbT.toString();
        // String outString = "{ \"tsvalue\":\""+ timeStr +"\" }";
        String outString = timeStr;
          log.info("TimestampUTC:[" + outString +"]");
        return outString;
      }
      catch(Exception e)
      {
        log.error("Error while getAppcTimestampUTC : " + e.getMessage());
        e.printStackTrace();
        throw e;
      }
    }

    private String setInCart(String payload, String requestID) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode payloadObject = objectMapper.readTree(payload);
        ArrayList<String> argList = new ArrayList<>();
        argList.add(payloadObject.get(DesignServiceConstants.INCART).textValue());
        argList.add(payloadObject.get(DesignServiceConstants.VNF_TYPE).textValue());

        String queryString = "UPDATE DT_ARTIFACT_TRACKING SET INCART= ? WHERE ASDC_REFERENCE_ID  IN "
            + " (SELECT ASDC_REFERENCE_ID FROM ASDC_REFERENCE_ID WHERE VNF_TYPE = ? ";

        if (payloadObject.get(DesignServiceConstants.VNF_TYPE) != null && !payloadObject
            .get(DesignServiceConstants.VNF_TYPE).textValue().isEmpty()) {
            queryString = queryString + "  AND VNFC_TYPE = ? ) AND USER = ? ";
            argList.add(payloadObject.get(DesignServiceConstants.VNFC_TYPE).textValue());
        } else {
            queryString = queryString + "  ) AND USER = ? ";
        }
        argList.add(payloadObject.get(DesignServiceConstants.USER_ID).textValue());
        log.info(QUERY_STR + queryString);
        boolean data = dbservice.updateDBData(queryString, argList);

        if (!data) {
            throw new DBException("Error while updating ProtocolReference");
        }
        return SUCCESS_JSON;
    }

    private String setProtocolReference(String payload, String requestID) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode payloadObject = objectMapper.readTree(payload);
        ArrayList<String> argList = new ArrayList<>();

        argList.add(payloadObject.get(DesignServiceConstants.ACTION).textValue());
        argList.add(payloadObject.get(DesignServiceConstants.ACTION_LEVEL).textValue());
        argList.add(payloadObject.get(DesignServiceConstants.VNF_TYPE).textValue());
        argList.add(payloadObject.get(DesignServiceConstants.PROTOCOL).textValue());

        String queryString = " DELETE FROM PROTOCOL_REFERENCE WHERE ACTION = ? AND ACTION_LEVEL AND VNF_TYPE= ?  AND PROTOCOL = ? ";

        log.info("Delete Query String :" + queryString);
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

            queryString = queryString + " AND  VNFC_TYPE =  ? )";
        } else {
            queryString = queryString + " ) ";
        }
        log.info(QUERY_STR + queryString);
        data = dbservice.updateDBData(queryString, argList);

        if (!data) {
            throw new DBException("Error while updating ProtocolReference");
        }
        return SUCCESS_JSON;
    }

    private String uploadArtifact(String payload, String requestID) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        JsonNode payloadObject = objectMapper.readTree(payload);
        log.info("Got upload Aritfact with Payload : " + payloadObject.asText());
        try {
            ArtifactHandlerClient ac = new ArtifactHandlerClient();
            String requestString = ac.createArtifactData(payload, requestID);
            ac.execute(requestString, "POST");
            int sdcArtifactId = getSDCArtifactIDbyRequestID(requestID);
            int sdcReferenceId = getSDCReferenceID(payload);
            createArtifactTrackingRecord(payload, requestID, sdcArtifactId, sdcReferenceId);
            String status = getDataFromActionStatus(payload, STATUS);
            if (status == null || status.isEmpty()) {
              log.info("Action Status is: "+ status);
              setActionStatus(payload, "Not Tested");
            }
            linkstatusRelationShip(sdcArtifactId, sdcReferenceId, payload);

        } catch (Exception e) {
            log.error("An error occured in uploadArtifact", e);
            throw e;
        }
        return SUCCESS_JSON;

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
            "INSERT INTO DT_STATUS_RELATIONSHIP (DT_ARTIFACT_TRACKING_ID,DT_ACTION_STATUS_ID) VALUES " +
                "(( SELECT DT_ARTIFACT_TRACKING_ID FROM DT_ARTIFACT_TRACKING WHERE ASDC_ARTIFACTS_ID = ? AND ASDC_REFERENCE_ID = ? ) , "
                + "( SELECT DT_ACTION_STATUS_ID FROM DT_ACTION_STATUS WHERE  VNF_TYPE = ? AND ACTION = ?  AND USER = ? ";

        if (payloadObject.get(DesignServiceConstants.VNFC_TYPE) != null && !payloadObject
            .get(DesignServiceConstants.VNFC_TYPE).textValue().isEmpty()) {
            queryString = queryString + " AND  VNFC_TYPE =  ? GROUP BY VNF_TYPE HAVING COUNT(VNF_TYPE)>=1 ) )";
        } else {
            queryString = queryString + " GROUP BY VNF_TYPE HAVING COUNT(VNF_TYPE)>=1 ) ) ";
        }
        log.info(QUERY_STR + queryString);
        boolean data = dbservice.updateDBData(queryString, argList);

        if (!data) {
            throw new DBException("Error while updating RelationShip table");
        }

    }

    private int getSDCReferenceID(String payload) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode payloadObject = objectMapper.readTree(payload);
        ArrayList<String> argList = new ArrayList<>();
        argList.add(payloadObject.get(DesignServiceConstants.VNF_TYPE).textValue());

        argList.add(payloadObject.get(DesignServiceConstants.ARTIFACT_TYPE).textValue());
        argList.add(payloadObject.get(DesignServiceConstants.ARTIFACT_NAME).textValue());

        String queryString = " SELECT ASDC_REFERENCE_ID FROM ASDC_REFERENCE WHERE VNF_TYPE = ?  "
            + " AND ARTIFACT_TYPE = ?  AND ARTIFACT_NAME = ? ";

        if (payloadObject.get(DesignServiceConstants.ACTION) != null && !payloadObject
            .get(DesignServiceConstants.ACTION).textValue().isEmpty()) {
            argList.add(payloadObject.get(DesignServiceConstants.ACTION).textValue());
            queryString = queryString + " AND ACTION = ? ";
        }
        if (payloadObject.get(DesignServiceConstants.VNFC_TYPE) != null && !payloadObject
            .get(DesignServiceConstants.VNFC_TYPE).textValue().isEmpty()) {
            argList.add(payloadObject.get(DesignServiceConstants.VNFC_TYPE).textValue());
            queryString = queryString + " AND VNFC_TYPE = ? ";

        }

        log.info(QUERY_STR + queryString);
        ResultSet data = dbservice.getDBData(queryString, argList);
        int sdcReferenceId = 0;
        while (data.next()) {
            sdcReferenceId = data.getInt("ASDC_REFERENCE_ID");
        }
        log.info("Got sdcReferenceId= " + sdcReferenceId);
        return sdcReferenceId;
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
            " SELECT " + dataValue + " FROM DT_ACTION_STATUS WHERE VNF_TYPE = ? AND ACTION = ? AND USER = ? ";
        if (payloadObject.get(DesignServiceConstants.VNFC_TYPE) != null && !payloadObject
            .get(DesignServiceConstants.VNFC_TYPE).textValue().isEmpty()) {
            argList.add(payloadObject.get(DesignServiceConstants.VNFC_TYPE).textValue());
            queryString = queryString + " AND VNFC_TYPE = ? ";
        }
        log.info(QUERY_STR + queryString);
        ResultSet data = dbservice.getDBData(queryString, argList);
        while (data.next()) {
            status = data.getString(STATUS);
        }
        log.info("DT_ACTION_STATUS Status = " + status);
        return status;
    }

    private void setActionStatus(String payload, String status) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode payloadObject = objectMapper.readTree(payload);
        ArrayList<String> argList = new ArrayList<>();
        argList.add(payloadObject.get(DesignServiceConstants.ACTION).textValue());
        argList.add(payloadObject.get(DesignServiceConstants.VNF_TYPE).textValue());

        String insertQuery = " INSERT INTO DT_ACTION_STATUS (ACTION, VNF_TYPE, VNFC_TYPE, USER, TECHNOLOGY, UPDATED_DATE, STATUS) VALUES (?,?,?,?,?,sysdate() , ?); ";
        if (payloadObject.get(DesignServiceConstants.VNFC_TYPE) != null && !payloadObject
            .get(DesignServiceConstants.VNFC_TYPE).textValue().isEmpty()) {
            argList.add(payloadObject.get(DesignServiceConstants.VNFC_TYPE).textValue());
            log.info("Vnfc-Type: " + payloadObject.get(DesignServiceConstants.VNFC_TYPE).textValue());
        } else {
            argList.add(null);
        }
        argList.add(payloadObject.get(DesignServiceConstants.USER_ID).textValue());
        if (payloadObject.get(DesignServiceConstants.TECHNOLOGY) != null && !payloadObject
            .get(DesignServiceConstants.TECHNOLOGY).textValue().isEmpty()) {
            argList.add(payloadObject.get(DesignServiceConstants.TECHNOLOGY).textValue());
        } else {
            argList.add(null);
        }
        argList.add(status);

        log.info("QueryString: " + insertQuery);
        log.info("Arguments List: " + argList);
        boolean updateStatus = dbservice.updateDBData(insertQuery, argList);
        if (!updateStatus)
            throw new DBException("Error while updating Action Status");
    }

    private void createArtifactTrackingRecord(String payload, String requestID, int sdcArtifactId, int sdcReferenceId)
        throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode payloadObject = objectMapper.readTree(payload);

        ArrayList<String> argList = new ArrayList<>();
        argList.add(String.valueOf(sdcArtifactId));
        argList.add(String.valueOf(sdcReferenceId));
        argList.add(payloadObject.get(DesignServiceConstants.USER_ID).textValue());
        if (payloadObject.get(DesignServiceConstants.TECHNOLOGY) != null && !payloadObject
            .get(DesignServiceConstants.TECHNOLOGY).textValue().isEmpty()) {
            argList.add(payloadObject.get(DesignServiceConstants.TECHNOLOGY).textValue());
        } else {
            argList.add("");
        }

        if (payloadObject.get(DesignServiceConstants.PROTOCOL) != null && !payloadObject
            .get(DesignServiceConstants.PROTOCOL).textValue().isEmpty()) {
            argList.add(payloadObject.get(DesignServiceConstants.PROTOCOL).textValue());
        } else {
            argList.add("");
        }

        String queryString = "INSERT INTO DT_ARTIFACT_TRACKING (ASDC_ARTIFACTS_ID, ASDC_REFERENCE_ID, USER, TECHNOLOGY, CREATION_DATE, UPDATED_DATE, ARTIFACT_STATUS, PROTOCOL, IN_CART) VALUES (? , ? , ?, ?, sysdate() , sysdate(), 'Created',  ? ,'N' )";

        log.info(QUERY_STR + queryString);
        boolean data = dbservice.updateDBData(queryString, argList);
        if (!data) {
            throw new DBException("Error Updating DT_ARTIFACT_TRACKING ");
        }
    }

    private int getSDCArtifactIDbyRequestID(String requestID) throws Exception {
        log.info("Starting getArtifactIDbyRequestID DB Operation");
        int artifactId = 0;
        try {
            ArrayList<String> argList = new ArrayList<>();
            argList.add("TLSUUID" + requestID);
            String queryString = " SELECT ASDC_ARTIFACTS_ID FROM ASDC_ARTIFACTS where SERVICE_UUID = ? ";
            log.info(QUERY_STR + queryString);
            ResultSet data = dbservice.getDBData(queryString, argList);
            while (data.next()) {
                artifactId = data.getInt("ASDC_ARTIFACTS_ID");
            }
        } catch (Exception e) {
            log.error("An error occurred in getSDCArtifactIDbyRequestID", e);
            throw e;
        }
        log.info("Got SDC_ARTIFACTS_ID As :" + artifactId);
        return artifactId;
    }


    private String getArtifact(String payload, String requestID) throws Exception {
        log.info("Starting getArtifact DB Operation");
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode payloadObject = objectMapper.readTree(payload);
            ArrayList<String> argList = new ArrayList<>();
            argList.add(payloadObject.get("artifact-name").textValue());
            argList.add(payloadObject.get("artifact-type").textValue());

            String queryString = "SELECT INTERNAL_VERSION, ARTIFACT_CONTENT FROM ASDC_ARTIFACTS where " +
                " ARTIFACT_NAME = ? AND ARTIFACT_TYPE = ?  ";

            log.info(QUERY_STR + queryString);
            ResultSet data = dbservice.getDBData(queryString, argList);
            String artifactContent = null;
            int hightestVerion = -1;
            while (data.next()) {
                int version = data.getInt("INTERNAL_VERSION");
                if (hightestVerion < version) {
                    artifactContent = data.getString("ARTIFACT_CONTENT");
                }
            }
            if (artifactContent == null || artifactContent.isEmpty()) {
                throw new DBException(
                    "Sorry !!! I dont have any artifact Named : " + payloadObject.get("artifact-name").textValue());
            }
            DesignResponse designResponse = new DesignResponse();
            List<ArtifactInfo> artifactInfoList = new ArrayList<>();
            ArtifactInfo artifactInfo = new ArtifactInfo();
            artifactInfo.setArtifact_content(artifactContent);
            artifactInfoList.add(artifactInfo);
            designResponse.setArtifactInfo(artifactInfoList);
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = mapper.writeValueAsString(designResponse);
            log.info(INFO_STR + jsonString);
            return jsonString;
        } catch (Exception e) {
            log.error(DB_OPERATION_ERROR, e);
            throw e;
        }
    }

    private String setStatus(String payload, String requestID) throws Exception {

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
                queryString = queryString + " and DAS.VNFC_TYPE = ? ";
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

    private String getStatus(String payload, String requestID) throws Exception {
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

            String queryString = "SELECT DAS.VNF_TYPE, DAS.VNFC_TYPE,  DAS.STATUS, DAS.ACTION, DAT.ARTIFACT_STATUS "
                + "from  DT_ACTION_STATUS DAS , DT_ARTIFACT_TRACKING DAT, DT_STATUS_RELATIONSHIP DSR " +
                " where  DAT.USER = DAS.USER and DSR.DT_ARTIFACT_TRACKING_ID = DAT.DT_ARTIFACT_TRACKING_ID "
                + " and DSR.DT_ACTION_STATUS_ID = DAS.DT_ACTION_STATUS_ID and DAT.USER = ? "
                + " and  DAS.VNF_TYPE = ? ";

            if (vnfcType != null && !vnfcType.isEmpty()) {
                argList.add(vnfcType);
                queryString = queryString + " and DAS.VNFC_TYPE = ? ";
            }

            log.info(QUERY_STR + queryString);

            DesignResponse designResponse = new DesignResponse();
            designResponse.setUserId(userID);
            List<StatusInfo> statusInfoList = new ArrayList<>();
            ResultSet data = dbservice.getDBData(queryString, argList);
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
                    "OOPS !!!! No VNF information available for VNF-TYPE : " + vnfType + " for User : " + userID);
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
            log.error("Exception : ", e);
            throw e;
        }
    }

    private String getGuiReference(String payload, String requestID) {
        // TODO Auto-generated method stub
        return null;
    }

    private String getArtifactReference(String payload, String requestID) {
        // TODO Auto-generated method stub
        return null;
    }

    private String getDesigns(String payload, String requestID) throws Exception {

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

            if (filterKey != null) {
                queryString =
                    "SELECT AR.VNF_TYPE, AR.VNFC_TYPE,  DAT.PROTOCOL, DAT.IN_CART, AR.ACTION, AR.ARTIFACT_NAME, AR.ARTIFACT_TYPE from  "
                        +
                        DesignServiceConstants.DB_DT_ARTIFACT_TRACKING + " DAT , "
                        + DesignServiceConstants.DB_SDC_REFERENCE +
                        " AR where DAT.ASDC_REFERENCE_ID= AR.ASDC_REFERENCE_ID  and DAT.USER = ? and AR.ARTIFACT_NAME like '%"
                        + filterKey + "%' GROUP BY AR.VNF_TYPE,AR.ARTIFACT_NAME";
            } else {
                queryString =
                    "SELECT AR.VNF_TYPE, AR.VNFC_TYPE,  DAT.PROTOCOL, DAT.IN_CART, AR.ACTION, AR.ARTIFACT_NAME, AR.ARTIFACT_TYPE from  "
                        +
                        DesignServiceConstants.DB_DT_ARTIFACT_TRACKING + " DAT , "
                        + DesignServiceConstants.DB_SDC_REFERENCE +
                        " AR where DAT.ASDC_REFERENCE_ID= AR.ASDC_REFERENCE_ID  and DAT.USER = ? GROUP BY AR.VNF_TYPE,AR.ARTIFACT_NAME";
            }
            DesignResponse designResponse = new DesignResponse();
            designResponse.setUserId(userID);
            List<DesignInfo> designInfoList = new ArrayList<>();
            ResultSet data = dbservice.getDBData(queryString, argList);
            while (data.next()) {
                DesignInfo designInfo = new DesignInfo();
                designInfo.setInCart(data.getString("IN_CART"));
                designInfo.setProtocol(data.getString("PROTOCOL"));
                designInfo.setVnf_type(data.getString("VNF_TYPE"));
                designInfo.setVnfc_type(data.getString("VNFC_TYPE"));
                designInfo.setAction(data.getString("ACTION"));
                designInfo.setArtifact_type(data.getString("ARTIFACT_TYPE"));
                designInfo.setArtifact_name(data.getString("ARTIFACT_NAME"));
                designInfoList.add(designInfo);
            }
            if (designInfoList.isEmpty()) {
                throw new DBException(
                    " Welcome to CDT, Looks like you dont have Design Yet... Lets create some....");
            }
            designResponse.setDesignInfoList(designInfoList);
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = mapper.writeValueAsString(designResponse);
            log.info(INFO_STR + jsonString);
            return jsonString;
        } catch (Exception e) {
            log.error("Error while Starting getDesgins DB operation : ", e);
            throw e;
        }
    }
}


