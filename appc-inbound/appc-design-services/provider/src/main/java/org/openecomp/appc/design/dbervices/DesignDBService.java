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

package org.openecomp.appc.design.dbervices;

import java.io.IOException;
import java.security.SecureRandom;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.openecomp.appc.design.data.ArtifactInfo;
import org.openecomp.appc.design.data.DesignInfo;
import org.openecomp.appc.design.data.DesignResponse;
import org.openecomp.appc.design.data.StatusInfo;
import org.openecomp.appc.design.services.util.ArtifactHandlerClient;
import org.openecomp.appc.design.services.util.DesignServiceConstants;
import org.openecomp.sdnc.sli.SvcLogicResource;
import org.openecomp.sdnc.sli.resource.dblib.DBResourceManager;
import org.openecomp.sdnc.sli.resource.sql.SqlResource;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class DesignDBService {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(DesignDBService.class);
    private SvcLogicResource serviceLogic;
    private static DesignDBService dgGeneralDBService = null;
    private static DBResourceManager jdbcDataSource;

    DbService dbservice = null;
    private static Properties props;
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

        log.info("Received execute request for action : " + action + "  with Payload : "+  payload );
        RequestValidator.validate(action, payload);
        String response = null;
        dbservice =  new DbService();
        switch (action) {
        case DesignServiceConstants.GETDESIGNS:
            response =  getDesigns(payload,requestID );
            break;
        case DesignServiceConstants.ADDINCART:
            response=  setInCart(payload, requestID);
            break ;
        case DesignServiceConstants.GETARTIFACTREFERENCE:
            response=  getArtifactReference(payload, requestID);
            break;
        case DesignServiceConstants.GETARTIFACT:
            response=  getArtifact(payload, requestID);
            break;
        case DesignServiceConstants.GETGUIREFERENCE:
            response=  getGuiReference(payload, requestID);
            break;
        case DesignServiceConstants.GETSTATUS:
            response=  getStatus(payload, requestID);
            break;
        case DesignServiceConstants.SETSTATUS:
            response=  setStatus(payload, requestID);
            break;        
        case DesignServiceConstants.UPLOADARTIFACT:
            response=  uploadArtifact(payload, requestID);
            break;        
        case DesignServiceConstants.SETPROTOCOLREFERENCE:
            response=  setProtocolReference(payload, requestID);
            break;    
        default: 
            throw new Exception(" Action " + action + " not found while processing request ");            

        }
        return response;                
    }

    private String setInCart(String payload, String requestID) throws Exception {
        
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode payloadObject = objectMapper.readTree(payload);
        ArrayList<String> argList = new ArrayList<>();                    
        argList.add(payloadObject.get(DesignServiceConstants.INCART).textValue());
        argList.add(payloadObject.get(DesignServiceConstants.VNF_TYPE).textValue());
                
        String queryString = "UPDATE DT_ARTIFACT_TRACKING SET INCART= ? WHERE ASDC_REFERENCE_ID  IN " 
                            + " (SELECT ASDC_REFERENCE_ID FROM ASDC_REFERENCE_ID WHERE VNF_TYPE = ? " ;
                 
        if(payloadObject.get(DesignServiceConstants.VNF_TYPE) != null &&! payloadObject.get(DesignServiceConstants.VNF_TYPE).textValue().isEmpty())    { 
            queryString = queryString + "  AND VNFC_TYPE = ? ) AND USER = ? " ;
            argList.add(payloadObject.get(DesignServiceConstants.VNFC_TYPE).textValue());
        }
        else{
            queryString = queryString + "  ) AND USER = ? " ;
        }
        
        argList.add(payloadObject.get(DesignServiceConstants.USER_ID).textValue());
        
        log.info("Query String :" + queryString);
         boolean data = dbservice.updateDBData(queryString, argList);

    if(!data)
        throw new Exception("Error while updating ProtocolReference");
    
    return "{\"update\" : \"success\" } ";
        
    }
    private String setProtocolReference(String payload, String requestID) throws Exception {
        
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode payloadObject = objectMapper.readTree(payload);
        ArrayList<String> argList = new ArrayList<>();            
        
        argList.add(payloadObject.get(DesignServiceConstants.ACTION).textValue());
        argList.add(payloadObject.get(DesignServiceConstants.ACTION_LEVEL).textValue());
        argList.add(payloadObject.get(DesignServiceConstants.VNF_TYPE).textValue());
        argList.add(payloadObject.get(DesignServiceConstants.PROTOCOL).textValue());

        String queryString = " DELETE FROM PROTOCOL_REFERENCE WHERE ACTION = ? AND ACTION_LEVEL AND VNF_TYPE= ?  AND PROTOCOL = ? " ;
        
        log.info("Delete Query String :" + queryString);
        boolean data = dbservice.updateDBData(queryString, argList);
        
        log.info("Record Deleted");

        if((payloadObject.get(DesignServiceConstants.TEMPLATE) != null && !payloadObject.get(DesignServiceConstants.TEMPLATE).textValue().isEmpty()))
            argList.add(payloadObject.get(DesignServiceConstants.TEMPLATE).textValue());
        else
            argList.add("NO");
        
        String insertString = "INSERT INTO PROTOCOL_REFERENCE VALUES (?,?,?,?,?,SYSDATE()) ";
        
        if(payloadObject.get(DesignServiceConstants.VNFC_TYPE) != null && ! payloadObject.get(DesignServiceConstants.VNFC_TYPE).textValue().isEmpty()){
            queryString = queryString + " AND  VNFC_TYPE =  ? )" ;
        }
        else{
            queryString = queryString + " ) ";
        }
        log.info("Query String :" + queryString);
             data = dbservice.updateDBData(queryString, argList);

        if(!data)
            throw new Exception("Error while updating ProtocolReference");
        
        return "{\"update\" : \"success\" } ";
    }
    private String uploadArtifact(String payload, String requestID) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode payloadObject = objectMapper.readTree(payload);
        log.info("Got upload Aritfact with Payload : " + payloadObject.asText());
        try{
            ArtifactHandlerClient ac = new ArtifactHandlerClient(); 
            String requestString = ac.createArtifactData(payload, requestID);
            ac.execute(requestString, "POST");
            int sdc_artifact_id = getSDCArtifactIDbyRequestID(requestID);
            int sdc_reference_id = getSDCReferenceID(payload);
            createArtifactTrackingRecord(payload, requestID,sdc_artifact_id, sdc_reference_id );
            String status = getDataFromActionStatus(payload, "STATUS");
            if(status == null || status.isEmpty())
                setActionStatus(payload, "Not Tested");
            linkstatusRelationShip(sdc_artifact_id,sdc_reference_id, payload);

        }
        catch(Exception e){
            e.printStackTrace();
            throw e;
        }
         return "{\"update\" : \"success\" } ";

    }

    private void linkstatusRelationShip(int sdc_artifact_id, int sdc_reference_id, String payload) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode payloadObject = objectMapper.readTree(payload);
        ArrayList<String> argList = new ArrayList<>();            
        argList.add(String.valueOf(sdc_artifact_id));
        argList.add(String.valueOf(sdc_reference_id));
        argList.add(payloadObject.get(DesignServiceConstants.VNF_TYPE).textValue());
        argList.add(payloadObject.get(DesignServiceConstants.ACTION).textValue());
        argList.add(payloadObject.get(DesignServiceConstants.USER_ID).textValue());

        String queryString = "INSERT INTO DT_STATUS_RELATIONSHIP (DT_ARTIFACT_TRACKING_ID,DT_ACTION_STATUS_ID) VALUES " +  
                 "(( SELECT DT_ARTIFACT_TRACKING_ID FROM DT_ARTIFACT_TRACKING WHERE ASDC_ARTIFACTS_ID = ? AND ASDC_REFERENCE_ID = ? ) , "
                + "( SELECT DT_ACTION_STATUS_ID FROM DT_ACTION_STATUS WHERE  VNF_TYPE = ? AND ACTION = ?  AND USER = ? " ;

        if(payloadObject.get(DesignServiceConstants.VNFC_TYPE) != null && ! payloadObject.get(DesignServiceConstants.VNFC_TYPE).textValue().isEmpty()){
            queryString = queryString + " AND  VNFC_TYPE =  ? ) )" ;
        }
        else{
            queryString = queryString + " ) ) ";
        }
        log.info("Query String :" + queryString);
            boolean data = dbservice.updateDBData(queryString, argList);

        if(!data)
            throw new Exception("Error while updating RealtionShip table");

    }
    private int getSDCReferenceID(String payload) throws Exception {

        String vnfc_type = null;
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode payloadObject = objectMapper.readTree(payload);
        ArrayList<String> argList = new ArrayList<>();            
        argList.add(payloadObject.get(DesignServiceConstants.VNF_TYPE).textValue());
        
        argList.add(payloadObject.get(DesignServiceConstants.ARTIFACT_TYPE).textValue());
        argList.add(payloadObject.get(DesignServiceConstants.ARTIFACT_NAME).textValue());

        String queryString = " SELECT ASDC_REFERENCE_ID FROM ASDC_REFERENCE WHERE VNF_TYPE = ?  "
                + " AND ARTIFACT_TYPE = ?  AND ARTIFACT_NAME = ? " ;
        
        if(payloadObject.get(DesignServiceConstants.ACTION) != null && !payloadObject.get(DesignServiceConstants.ACTION).textValue().isEmpty()){
            argList.add(payloadObject.get(DesignServiceConstants.ACTION).textValue());
            queryString = queryString + " AND ACTION = ? ";
        }
        if(payloadObject.get(DesignServiceConstants.VNFC_TYPE) !=null && !payloadObject.get(DesignServiceConstants.VNFC_TYPE).textValue().isEmpty()){
            argList.add(payloadObject.get(DesignServiceConstants.VNFC_TYPE).textValue());
            queryString = queryString + " AND VNFC_TYPE = ? ";

        }

        log.info("Query String :" + queryString);
        ResultSet data = dbservice.getDBData(queryString, argList);
        int sdc_reference_id = 0;        
        while(data.next()) {            
            sdc_reference_id = data.getInt("ASDC_REFERENCE_ID");                
        }    
        log.info("Got sdc_reference_id = " + sdc_reference_id );
        return sdc_reference_id;

    }

    private String getDataFromActionStatus(String payload, String dataValue) throws Exception {
        String status = null ;
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode payloadObject = objectMapper.readTree(payload);
        ArrayList<String> argList = new ArrayList<>();            
        argList.add(payloadObject.get(DesignServiceConstants.VNF_TYPE).textValue());
        argList.add(payloadObject.get(DesignServiceConstants.ACTION).textValue());
        argList.add(payloadObject.get(DesignServiceConstants.USER_ID).textValue());
        String queryString = " SELECT " + dataValue + " FROM DT_ACTION_STATUS WHERE VNF_TYPE = ? AND ACTION = ? AND USER = ? ";
        if(payloadObject.get(DesignServiceConstants.VNFC_TYPE) !=null && !payloadObject.get(DesignServiceConstants.VNFC_TYPE).textValue().isEmpty()){
            argList.add(payloadObject.get(DesignServiceConstants.VNFC_TYPE).textValue());
            queryString = queryString + " AND VNFC_TYPE = ? ";
        }
        log.info("Query String :" + queryString);
        ResultSet data = dbservice.getDBData(queryString, argList);
        while(data.next()) {            
            status = data.getString("STATUS");                
        }    
        log.info("DT_ACTION_STATUS Status = " + status );
        return status;
    }        

    private boolean  setActionStatus(String payload, String status) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode payloadObject = objectMapper.readTree(payload);
        ArrayList<String> argList = new ArrayList<>();    
        argList.add(payloadObject.get(DesignServiceConstants.ACTION).textValue());
        argList.add(payloadObject.get(DesignServiceConstants.VNF_TYPE).textValue());
        

        String insertQuery = " INSERT INTO DT_ACTION_STATUS (ACTION, VNF_TYPE, VNFC_TYPE, USER, TECHNOLOGY, UPDATED_DATE, STATUS) VALUES (?,?,?,?,?,sysdate() , ?); ";
        if(payloadObject.get(DesignServiceConstants.VNFC_TYPE) !=null && !payloadObject.get(DesignServiceConstants.VNFC_TYPE).textValue().isEmpty()){
            argList.add(payloadObject.get(DesignServiceConstants.VNFC_TYPE).textValue());
        }
        else{
            argList.add(null);
        }
        argList.add(payloadObject.get(DesignServiceConstants.USER_ID).textValue());
        if(payloadObject.get(DesignServiceConstants.TECHNOLOGY) !=null && !payloadObject.get(DesignServiceConstants.TECHNOLOGY).textValue().isEmpty()){
            argList.add(payloadObject.get(DesignServiceConstants.TECHNOLOGY).textValue());
        }
        else{
            argList.add(null);
        }
        argList.add(status);

        boolean updateStatus = dbservice.updateDBData(insertQuery, argList);
        if(!updateStatus)
            throw new Exception("Error while updating Action Status");
        return updateStatus;
    }

    private void createArtifactTrackingRecord(String payload, String requestID, int sdc_artifact_id, int sdc_reference_id) throws Exception {
        String vnfc_type = null;
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode payloadObject = objectMapper.readTree(payload);

        ArrayList<String> argList = new ArrayList<>();            
        argList.add(String.valueOf(sdc_artifact_id));
        argList.add(String.valueOf(sdc_reference_id));
        argList.add(payloadObject.get(DesignServiceConstants.USER_ID).textValue());
        if (payloadObject.get(DesignServiceConstants.TECHNOLOGY) != null &&! payloadObject.get(DesignServiceConstants.TECHNOLOGY).textValue().isEmpty())
            argList.add(payloadObject.get(DesignServiceConstants.TECHNOLOGY).textValue());
        else
            argList.add("");

        if (payloadObject.get(DesignServiceConstants.PROTOCOL) != null &&! payloadObject.get(DesignServiceConstants.PROTOCOL).textValue().isEmpty())
            argList.add(payloadObject.get(DesignServiceConstants.PROTOCOL).textValue());
        else
            argList.add("");


        String queryString = "INSERT INTO DT_ARTIFACT_TRACKING (ASDC_ARTIFACTS_ID, ASDC_REFERENCE_ID, USER, TECHNOLOGY, CREATION_DATE, UPDATED_DATE, ARTIFACT_STATUS, PROTOCOL, IN_CART) VALUES (? , ? , ?, ?, sysdate() , sysdate(), 'Created',  ? ,'N' )" ;

        log.info("Query String :" + queryString);
        boolean data = dbservice.updateDBData(queryString, argList);
        if(!data)
            throw new Exception("Error Updating DT_ARTIFACT_TRACKING ");


    }

    private int getSDCArtifactIDbyRequestID(String requestID) throws Exception {
        log.info("Starting getArtifactIDbyRequestID DB Operation");
        int artifact_id = 0;
        try{
            ArrayList<String> argList = new ArrayList<>();            
            argList.add("TLSUUID" + requestID);                
            String queryString = " SELECT ASDC_ARTIFACTS_ID FROM ASDC_ARTIFACTS where SERVICE_UUID = ? ";                
            log.info("Query String :" + queryString);
            ResultSet data = dbservice.getDBData(queryString, argList);
            while(data.next()){
                artifact_id = data.getInt("ASDC_ARTIFACTS_ID");
            }
        }
        catch(Exception e){
            e.printStackTrace();
            throw e;
        }
        log.info("Got SDC_ARTIFACTS_ID As :" + artifact_id);
        return artifact_id;
    }


    private String getArtifact(String payload, String requestID) throws Exception {
        String fn = "DBService.getStatus ";        
        log.info("Starting getArtifact DB Operation");
        try{
            String vnfc_type = null;
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode payloadObject = objectMapper.readTree(payload);
            ArrayList<String> argList = new ArrayList<>();            
            argList.add(payloadObject.get("artifact-name").textValue());
            argList.add(payloadObject.get("artifact-type").textValue());

            String queryString = "SELECT INTERNAL_VERSION, ARTIFACT_CONTENT FROM ASDC_ARTIFACTS where " + 
                    " ARTIFACT_NAME = ? AND ARTIFACT_TYPE = ?  " ;

            log.info("Query String :" + queryString);
            ResultSet data = dbservice.getDBData(queryString, argList);
            String artifact_content = null;
            int hightestVerion = 0 ;
            while(data.next()) {            
                int version = data.getInt("INTERNAL_VERSION");
                if(hightestVerion < version)
                    artifact_content = data.getString("ARTIFACT_CONTENT");            
            }    
            
            if(artifact_content == null || artifact_content.isEmpty())
                throw new Exception("Sorry !!! I dont have any artifact Named : " + payloadObject.get("artifact-name").textValue());
            DesignResponse designResponse = new DesignResponse();
            designResponse.setUserId(payloadObject.get("userID").textValue());
            List<ArtifactInfo> artifactInfoList = new ArrayList<ArtifactInfo>();    
            ArtifactInfo artifactInfo =  new ArtifactInfo();
            artifactInfo.setArtifact_content(artifact_content);
            artifactInfoList.add(artifactInfo);
            designResponse.setArtifactInfo(artifactInfoList);
            
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = mapper.writeValueAsString(designResponse);
            log.info("Info : " + jsonString);
            return jsonString;
        }
        catch(SQLException e)
        {
            log.error("Error while DB operation : " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        catch(Exception e)
        {
            log.error("Error while DB operation : " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

    }
    private String setStatus(String payload, String requestID) throws Exception {
        String fn = "DBService.getStatus ";        
        log.info("Starting getStatus DB Operation");
        try{
            String vnfc_type = null;
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode payloadObject = objectMapper.readTree(payload);


            ArrayList<String> argList = new ArrayList<>();
            argList.add(payloadObject.get("artifact_status").textValue());
            argList.add(payloadObject.get("action_status").textValue());

            argList.add(payloadObject.get("userID").textValue());
            argList.add(payloadObject.get("vnf-type").textValue());

            String queryString = " UPDATE DT_ARTIFACT_TRACKING DAT, DT_STATUS_RELATIONSHIP DSR  SET DAT.ARTIFACT_STATUS = ? , DAS.DT_ACTION_STATUS = ? "
                    + " where  DAT.USER = DAS.USER and DSR.DT_ARTIFACT_TRACKING_ID = DAT.DT_ARTIFACT_TRACKING_ID "
                    + " and DSR.DT_ACTION_STATUS_ID = DAS.DT_ACTION_STATUS_ID and DAT.USER = ? "
                    + " and  DAS.VNF_TYPE = ? " ;

            if(payloadObject.get("vnfc-type") !=null && !payloadObject.get("vnfc-type").textValue().isEmpty()){
                argList.add(payloadObject.get("vnfc-type").textValue());
                queryString = queryString    + " and DAS.VNFC_TYPE = ? ";                        
            }

            log.info("Query String :" + queryString);

            DesignResponse designResponse = new DesignResponse();
            designResponse.setUserId(payloadObject.get("userID").textValue());
            List<StatusInfo> statusInfoList = new ArrayList<StatusInfo>();
            boolean update = dbservice.updateDBData(queryString, argList);
            if(!update)
                throw new Exception("Sorry .....Something went wrong while updating the Status");

            ObjectMapper mapper = new ObjectMapper();
            String jsonString = mapper.writeValueAsString(designResponse);
            log.info("Info : " + jsonString);
            return jsonString;
        }
        catch(SQLException e)
        {
            log.error("Error while DB operation : " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        catch(Exception e)
        {
            log.error("Error while DB operation : " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    private String getStatus(String payload, String requestID) throws Exception {
        String fn = "DBService.getStatus ";        
        log.info("Starting getStatus DB Operation");
        try{
            String vnfc_type = null;
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode payloadObject = objectMapper.readTree(payload);
            String UserID = payloadObject.get("userID").textValue();    
            String vnf_type = payloadObject.get("vnf-type").textValue();
            if(payloadObject.get("vnfc-type") != null )
                vnfc_type = payloadObject.get("vnfc-type").textValue();
            ArrayList<String> argList = new ArrayList<>();

            argList.add(UserID);
            argList.add(vnf_type);

            String queryString = "SELECT DAS.VNF_TYPE, DAS.VNFC_TYPE,  DAS.STATUS, DAS.ACTION, DAT.ARTIFACT_STATUS "
                    + "from  DT_ACTION_STATUS DAS , DT_ARTIFACT_TRACKING DAT, DT_STATUS_RELATIONSHIP DSR " + 
                    " where  DAT.USER = DAS.USER and DSR.DT_ARTIFACT_TRACKING_ID = DAT.DT_ARTIFACT_TRACKING_ID "
                    + " and DSR.DT_ACTION_STATUS_ID = DAS.DT_ACTION_STATUS_ID and DAT.USER = ? "
                    + " and  DAS.VNF_TYPE = ? " ;

            if(vnfc_type !=null && ! vnfc_type.isEmpty()){
                argList.add(vnfc_type);
                queryString = queryString    + " and DAS.VNFC_TYPE = ? ";                        
            }

            log.info("Query String :" + queryString);

            DesignResponse designResponse = new DesignResponse();
            designResponse.setUserId(UserID);
            List<StatusInfo> statusInfoList = new ArrayList<StatusInfo>();
            ResultSet data = dbservice.getDBData(queryString, argList);
            while(data.next()) {            
                StatusInfo statusInfo = new StatusInfo();
                statusInfo.setAction(data.getString("ACTION"));
                statusInfo.setAction_status(data.getString("STATUS"));
                statusInfo.setArtifact_status(data.getString("ARTIFACT_STATUS"));
                statusInfo.setVnf_type(data.getString("VNF_TYPE"));
                statusInfo.setVnfc_type(data.getString("VNFC_TYPE"));            
                statusInfoList.add(statusInfo);
            }

            if(statusInfoList.size() < 1)
                throw new Exception("OOPS !!!! No VNF information available for VNF-TYPE : " + vnf_type + " for User : "  + UserID);
            designResponse.setStatusInfoList(statusInfoList);
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = mapper.writeValueAsString(designResponse);
            log.info("Info : " + jsonString);
            return jsonString;
        }
        catch(SQLException e)
        {
            log.error("Error while DB operation : " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        catch(Exception e)
        {
            log.error("Error while DB operation : " + e.getMessage());
            e.printStackTrace();
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
    private String getAddInCart(String payload, String requestID) {
        // TODO Auto-generated method stub
        return null;
    }

    //    private String getDesigns(String payload, String requestID) throws SQLException, JsonProcessingException, IOException, SvcLogicException {
    //        
    //        String fn = "DBService.getDesigns ";        
    //        QueryStatus status = null;
    //        ObjectMapper objectMapper = new ObjectMapper();
    //        JsonNode jnode = objectMapper.readTree(payload);
    //        String UserId = jnode.get("userID").textValue();
    //        SvcLogicContext localContext = new SvcLogicContext();
    //        localContext.setAttribute("requestID", requestID);
    //        localContext.setAttribute("userID", UserId);
    //        if (serviceLogic != null && localContext != null) {    
    //            String queryString = "SELECT AR.VNF_TYPE, AR.VNFC_TYPE,  DAT.PROTOCOL, DAT.IN_CART from  " + 
    //                    DesignServiceConstants.DB_DT_ARTIFACT_TRACKING  + " DAT , " +  DesignServiceConstants.DB_SDC_REFERENCE  +
    //                    " AR where DAT.SDC_REFERENCE_ID= AR.SDC_REFERENCE_ID  and DAT.USER = $userID" ;
    //                    
    //            log.info(fn + "Query String : " + queryString);
    //            try {
    //                status = serviceLogic.query("SQL", true, null, queryString, null, null, localContext);
    //            } catch (SvcLogicException e1) {
    //                // TODO Auto-generated catch block
    //                e1.printStackTrace();
    //            }        
    //
    //            if(status.toString().equals("FAILURE"))
    //                throw new SvcLogicException("Error - while getting FlowReferenceData ");
    //        
    //            Properties props = localContext.toProperties();
    //            log.info("SvcLogicContext contains the following : " + props.toString());
    //            for (Enumeration e = props.propertyNames(); e.hasMoreElements() ; ) {
    //                String propName = (String) e.nextElement();
    //                log.info(propName+" = "+props.getProperty(propName));
    //                
    //            }
    //        }
    //        return requestID;
    //        
    //    }

    private String getDesigns(String payload, String requestID) throws Exception {

        String fn = "DBService.getDesigns ";        
        log.info("Starting getDesgins DB Operation");


        try{
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode payloadObject = objectMapper.readTree(payload);
            String UserID = payloadObject.get("userID").textValue();        
            ArrayList<String> argList = new ArrayList<>();
            argList.add(UserID);

            String queryString = "SELECT AR.VNF_TYPE, AR.VNFC_TYPE,  DAT.PROTOCOL, DAT.IN_CART, AR.ACTION, AR.ARTIFACT_NAME, AR.ARTIFACT_TYPE from  " + 
                    DesignServiceConstants.DB_DT_ARTIFACT_TRACKING  + " DAT , " +  DesignServiceConstants.DB_SDC_REFERENCE  +
                    " AR where DAT.ASDC_REFERENCE_ID= AR.ASDC_REFERENCE_ID  and DAT.USER = ? ";

            DesignResponse designResponse = new DesignResponse();
            designResponse.setUserId(UserID);
            List<DesignInfo> designInfoList = new ArrayList<DesignInfo>();
            ResultSet data = dbservice.getDBData(queryString, argList);
            while(data.next()) {            
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
            if(designInfoList.size() < 1)
                throw new Exception(" Welcome to CDT, Looks like you dont have Design Yet... Lets create some....");
            designResponse.setDesignInfoList(designInfoList);
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = mapper.writeValueAsString(designResponse);
            log.info("Info : " + jsonString);
            return jsonString;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw e;
        }
    }

}


