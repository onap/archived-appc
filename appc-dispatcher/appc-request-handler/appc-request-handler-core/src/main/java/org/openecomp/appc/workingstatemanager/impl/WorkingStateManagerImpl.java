/*-
 * ============LICENSE_START=======================================================
 * APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Amdocs
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */

package org.openecomp.appc.workingstatemanager.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.appc.configuration.ConfigurationFactory;
import org.openecomp.appc.executor.objects.Params;
import org.openecomp.appc.message.RequestHandlerMessages;
import org.openecomp.appc.util.MessageFormatter;
import org.openecomp.appc.workingstatemanager.objects.VNFWorkingState;
import org.openecomp.appc.workingstatemanager.objects.VnfWorkingStateDto;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.apache.commons.lang3.StringUtils;


public class WorkingStateManagerImpl extends JdbcWorkingStateManager {

    private static final String SQL_RETRIEVE_VNF_STATE_MANAGEMENT = "SELECT VNF_ID,STATE,OWNER_ID,UPDATED,VER FROM VNF_STATE_MANAGEMENT WHERE VNF_ID=?";
    private static final String SQL_INSERT_VNF_STATE_MANAGEMENT = "INSERT IGNORE INTO VNF_STATE_MANAGEMENT (VNF_ID,STATE,OWNER_ID,UPDATED,VER) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_UPDATE_VNF_STATE_MANAGEMENT = "UPDATE VNF_STATE_MANAGEMENT SET OWNER_ID=?, UPDATED=?, STATE=?, VER=? WHERE VNF_ID=? AND VER=?";
    private static final String SQL_CURRENT_TIMESTAMP = "SELECT CURRENT_TIMESTAMP()";
    private static int maxAttempts = ConfigurationFactory.getConfiguration().getIntegerProperty("org.openecomp.appc.workingstatemanager.maxAttempts",20);

    private static Map<String,VNFWorkingState> workingStateMap = new ConcurrentHashMap<>();
    private static final EELFLogger logger = EELFManager.getInstance().getLogger(WorkingStateManagerImpl.class);


    /**
     * Return true if vnf state exists in working state map and state is STABLE else return false. If vnf does not exists in working state map throws vnf not found  exception.
     * @param vnfId vnf Id to be verified for stable state
     * @return True if vnf Exists and state is STABLE else False.
     */
    @Override
    public boolean isVNFStable(String vnfId){
        if (logger.isTraceEnabled()) {
            logger.trace("Entering to isVNFStable with vnfId = "+ vnfId);
        }
        Connection connection = null;
        boolean vnfStable = false;
        try {
            connection = openDbConnection();
            VnfWorkingStateDto vnfWorkingStateDto = retrieveVnfWorkingState(connection, vnfId);
            vnfStable = isVNFStable(vnfWorkingStateDto);
        } catch (SQLException e) {
            String errMsg = StringUtils.isEmpty(e.getMessage())? e.toString() :e.getMessage();
            throw new RuntimeException(errMsg);
        } finally {
            if(connection != null) {
                closeDbConnection(connection);
        }
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Exiting from isVNFStable for vnfId = "+ vnfId+" with Result = "+vnfStable);
        }
        return vnfStable;
    }

    /**
     * Updates working state for given vnf Id. Returns true if update was allowed and succeeded. Update will success only if the existing vnf state is 'STABLE' or
     * if the registered ownerId is equal to the given ownerId or if the forceFlag is true.
     * Note on case of simultaneously updates the latest updates will be failed, and another attempts will be done after refetching the updated data from persistent store.
     * @param vnfId vnf Id to be updated
     * @param workingState new working state
     * @param ownerId
     * @param forceFlag - force to update also on case given onwerId is different then the registered one
     */
    @Override
    public boolean setWorkingState(String vnfId, VNFWorkingState workingState, String ownerId, boolean forceFlag){
        boolean updated = false;
        if (logger.isTraceEnabled()) {
            logger.trace("Entering to setWorkingState with vnfId = "+ ObjectUtils.toString(vnfId)+ ", VNFWorkingState = " +  workingState.name() + ", ownerId = "+ownerId+", forceFlag = "+forceFlag);
        }
        Connection connection = null;
        try {
            connection = openDbConnection();
            updated = setWorkingStateIfStableOrSameOwnerIdOrForce(connection, vnfId, workingState, ownerId, forceFlag, maxAttempts);
        } catch (SQLException e) {
            String errMsg = StringUtils.isEmpty(e.getMessage())? e.toString() :e.getMessage();
            throw new RuntimeException(errMsg);
        } finally {
            if(connection != null) {
                closeDbConnection(connection);
            }
        }

        logger.trace("setWorkingState exit with output updated = "+updated);
        return updated;
    }

    public boolean setWorkingStateIfStableOrSameOwnerIdOrForce(Connection connection, String vnfId, VNFWorkingState workingState, String ownerId, boolean forceFlag, int maxAttempts) throws SQLException {
        return setWorkingStateIfStableOrSameOwnerIdOrForce(connection, vnfId, workingState, ownerId, forceFlag,1,maxAttempts);
    }
    public boolean setWorkingStateIfStableOrSameOwnerIdOrForce(Connection connection, String vnfId, VNFWorkingState workingState, String ownerId, boolean forceFlag,int attempt, int maxAttempts) throws SQLException {
        boolean updated = false;
        VnfWorkingStateDto vnfWorkingStateDto = retrieveVnfWorkingState(connection, vnfId);
        Long currentVersion = vnfWorkingStateDto != null ? vnfWorkingStateDto.getVer() : null;
        if(forceFlag || isVNFStable(vnfWorkingStateDto) || vnfWorkingStateDto.getOwnerId().equals(ownerId)){
            updated = storeWorkingStateIfSameVersion(connection, vnfId, workingState, ownerId, currentVersion);

            Params params = new Params().addParam("vnfId", vnfId).addParam("workingState",workingState.name())
                    .addParam("attempt",attempt).addParam("maxAttempts",maxAttempts).addParam("ownerId",ownerId).addParam("forceFlag",forceFlag);
            String logMessage;
            if(updated) {
                logMessage = MessageFormatter.format(RequestHandlerMessages.VNF_WORKING_STATE_UPDATED, params.getParams());
            }else {
                logMessage = MessageFormatter.format(RequestHandlerMessages.VNF_WORKING_STATE_WAS_NOT_UPDATED, params.getParams());
            }
            logger.debug(logMessage);
            if(!updated && attempt<maxAttempts){
                setWorkingStateIfStableOrSameOwnerIdOrForce(connection, vnfId, workingState, ownerId, forceFlag,++attempt,maxAttempts);
            }

        }
        return updated;
    }


    public boolean storeWorkingStateIfSameVersion(Connection connection, String vnfId, VNFWorkingState workingState, String ownerId, Long currentVersion) throws SQLException {
        boolean stored = false;
        if (currentVersion != null) {
            stored = updateStateIfSameVersion(connection, vnfId, ownerId, workingState.name(), currentVersion);
        } else {
            stored = addVnfWorkingStateIfNotExists(connection, vnfId, ownerId, workingState.name());
        }

        return stored;
    }

    private boolean isVNFStable(VnfWorkingStateDto vnfWorkingStateDto) {
        if( vnfWorkingStateDto == null || vnfWorkingStateDto.getState() ==VNFWorkingState.STABLE){
            return true;
        }
        return false;
    }

    public boolean updateStateIfSameVersion(Connection connection, String vnfId, String ownerId, String state, long currentVer) throws SQLException {
        try(PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_VNF_STATE_MANAGEMENT)) {
            long newVer = (currentVer >= Long.MAX_VALUE) ? 1 : (currentVer + 1);
            statement.setString(1, ownerId);
            statement.setLong(2, getCurrentTime(connection));
            statement.setString(3, state);
            statement.setLong(4, newVer);
            statement.setString(5, vnfId);
            statement.setLong(6, currentVer);
            return (statement.executeUpdate() != 0);
        }
    }

    protected VnfWorkingStateDto retrieveVnfWorkingState(Connection connection, String vnfId) throws SQLException {
        VnfWorkingStateDto res = null;

        try(PreparedStatement statement = connection.prepareStatement(SQL_RETRIEVE_VNF_STATE_MANAGEMENT)) { //VNF_ID,STATE,OWNER_ID,UPDATED,VER
            statement.setString(1, vnfId);
            try(ResultSet resultSet = statement.executeQuery()) {
                if(resultSet.next()) {
                    res = new VnfWorkingStateDto(vnfId);
                    String stateString = resultSet.getString(2);
                    VNFWorkingState vnfWorkingState = VNFWorkingState.valueOf(stateString);
                    res.setState(vnfWorkingState);
                    res.setOwnerId(resultSet.getString(3));
                    res.setUpdated(resultSet.getLong(4));
                    res.setVer(resultSet.getLong(5));
                }
            }
        }
        return res;
    }

    private long getCurrentTime(Connection connection) throws SQLException {
        long res = -1;
        if(connection != null) {
            try(PreparedStatement statement = connection.prepareStatement(SQL_CURRENT_TIMESTAMP)) {
                try(ResultSet resultSet = statement.executeQuery()) {
                    if(resultSet.next()) {
                        res = resultSet.getTimestamp(1).getTime();
                    }
                }
            }
        }
        if(res == -1) {
            res = System.currentTimeMillis();
        }
        return res;
        }

    protected boolean addVnfWorkingStateIfNotExists(Connection connection, String vnfId, String ownerId, String state) throws SQLException {
        boolean added = false;
        try(PreparedStatement statement = connection.prepareStatement(SQL_INSERT_VNF_STATE_MANAGEMENT)) { //VNF_ID,STATE,OWNER_ID,UPDATED,VER
            statement.setString(1, vnfId);
            statement.setString(2, state);
            statement.setString(3, ownerId);
            statement.setLong(4, getCurrentTime(connection));
            statement.setLong(5, 1L);
            int rowCount = statement.executeUpdate();
            added = rowCount != 0 ? true : false;
        }
        return added;
    }
}
