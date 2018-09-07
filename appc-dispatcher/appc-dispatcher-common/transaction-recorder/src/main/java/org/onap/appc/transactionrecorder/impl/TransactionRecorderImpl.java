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

package org.onap.appc.transactionrecorder.impl;

import org.apache.commons.lang.StringUtils;
import org.onap.appc.domainmodel.lcm.Flags;
import org.onap.appc.domainmodel.lcm.RequestStatus;
import org.onap.appc.domainmodel.lcm.VNFOperation;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.transactionrecorder.TransactionRecorder;
import org.onap.appc.domainmodel.lcm.TransactionRecord;
import org.onap.appc.transactionrecorder.objects.TransactionConstants;
import org.onap.ccsdk.sli.core.dblib.DbLibService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.time.temporal.ChronoUnit;

import static org.onap.appc.transactionrecorder.objects.TransactionConstants.TRANSACTION_ATTRIBUTES.*;
import static org.onap.appc.transactionrecorder.objects.TransactionConstants.*;


public class TransactionRecorderImpl implements TransactionRecorder {

    private final String SCHEMA = "sdnctl";

    private String appcInstanceId;

    private DbLibService dbLibService;

    public void setDbLibService(DbLibService dbLibService) {
        this.dbLibService = dbLibService;
    }

    private static final Logger logger = LoggerFactory.getLogger(TransactionRecorderImpl.class);

    /**
     * Stores transaction record to appc database by calling APPC Dao layer.
     *
     * @param record Transaction record data.
     */
    @Override
    public void store(TransactionRecord record) throws APPCException {
        if (logger.isTraceEnabled()) {
            logger.trace("Transaction data insertion into DB");
        }
        final String STORE_DATE_QUERY = TransactionConstants.INSERT_INTO + TransactionConstants.TRANSACTIONS +
            "(" + TRANSACTION_ID.getColumnName() + TransactionConstants.COMMA +
            ORIGIN_TIMESTAMP.getColumnName() + TransactionConstants.COMMA +
            REQUEST_ID.getColumnName() + TransactionConstants.COMMA +
            SUBREQUEST_ID.getColumnName() + TransactionConstants.COMMA +
            ORIGINATOR_ID.getColumnName() + TransactionConstants.COMMA +
            START_TIME.getColumnName() + TransactionConstants.COMMA +
            END_TIME.getColumnName() + TransactionConstants.COMMA +
            TARGET_ID.getColumnName() + TransactionConstants.COMMA +
            TARGET_TYPE.getColumnName() + TransactionConstants.COMMA +
            OPERATION.getColumnName() + TransactionConstants.COMMA +
            RESULT_CODE.getColumnName() + TransactionConstants.COMMA +
            DESCRIPTION.getColumnName() + TransactionConstants.COMMA +
            STATE.getColumnName() + TransactionConstants.COMMA +
            SERVICE_INSTANCE_ID + TransactionConstants.COMMA +
            VNFC_NAME + TransactionConstants.COMMA +
            VSERVER_ID + TransactionConstants.COMMA +
            VF_MODULE_ID + TransactionConstants.COMMA +
            MODE + ") " +
            "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            dbLibService.writeData(STORE_DATE_QUERY, prepareArguments(record), SCHEMA);
        } catch (SQLException e) {
            logger.error("Error on storing record " + record.toString(), e);
            throw new APPCException(ERROR_ACCESSING_DATABASE, e);
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Transaction Data Inserted Successfully into DB");
        }
    }

    @Override
    public void update(String key, Map<TransactionConstants.TRANSACTION_ATTRIBUTES, String> updateColumns) throws
        APPCException {
        ArrayList<String> values = new ArrayList<>();

        StringBuilder queryBuilder = new StringBuilder("UPDATE TRANSACTIONS SET ");
        for (Map.Entry<TransactionConstants.TRANSACTION_ATTRIBUTES, String> entry : updateColumns.entrySet()) {
            queryBuilder.append(entry.getKey().getColumnName() + " = ? ,");
            values.add(entry.getValue());
        }
        queryBuilder.deleteCharAt(queryBuilder.lastIndexOf(","));
        queryBuilder.append(WHERE + TRANSACTION_ID.getColumnName() + " = ?");
        values.add(appcInstanceId + "~" + key);

        String query = queryBuilder.toString();
        try {
            dbLibService.writeData(query, values, SCHEMA);
        } catch (SQLException e) {
            logger.error("Error in updating records " + e);
            throw new APPCException(ERROR_ACCESSING_DATABASE, e);
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Transaction data updated successfully");
        }

    }

    @Override
    public void markTransactionsAborted(String appcInstanceId) {
        if (logger.isTraceEnabled()) {
            logger.trace("marking in progress transactions to aborted");
        }
        final String updateQuery =
            "UPDATE " + TransactionConstants.TRANSACTIONS +
                " SET " + STATE.getColumnName() + " = '" + RequestStatus.ABORTED.name() + "',"
                        + END_TIME.getColumnName() + " = ? " +
                WHERE + TRANSACTION_ID.getColumnName() + " LIKE '" + appcInstanceId + "%'  AND "
                + STATE.getColumnName() + " in (?,?)";

        if (logger.isDebugEnabled()) {
            logger.debug("Update query " + updateQuery + " appc-instance-id " + appcInstanceId);
        }

        ArrayList<String> arguments = new ArrayList<>();
        arguments.add(dateToStringConverterMillis(Instant.now()));
        arguments.add(RequestStatus.ACCEPTED.name());
        arguments.add(RequestStatus.RECEIVED.name());
        try {
            dbLibService.writeData(updateQuery, arguments, SCHEMA);
        } catch (SQLException e) {
            String message = "In progress transactions couldn't be marked aborted on server start up";
            logger.error(message);
            throw new RuntimeException(message);
        }
        if (logger.isTraceEnabled()) {
            logger.trace("In progress transactions marked aborted");
        }
    }

    @Override
    public List<TransactionRecord> getInProgressRequests(TransactionRecord record, int interval) throws APPCException {

        String IN_PROGRESS_REQUESTS_QUERY = "SELECT * FROM " +
            TransactionConstants.TRANSACTIONS + WHERE +
            TARGET_ID + " = ? AND " +
            STATE.getColumnName() + " IN (?,?) AND " +
            START_TIME.getColumnName() + " < ?";

        ArrayList<String> inProgressQueryParams = new ArrayList<>();
        Instant window = record.getStartTime().minus(interval, ChronoUnit.HOURS);
        inProgressQueryParams.add(record.getTargetId());
        inProgressQueryParams.add(RequestStatus.RECEIVED.name());
        inProgressQueryParams.add(RequestStatus.ACCEPTED.name());
        inProgressQueryParams.add(dateToStringConverterMillis(record.getStartTime()));
        if (interval > 0) {
            IN_PROGRESS_REQUESTS_QUERY += " AND " + START_TIME.getColumnName() + " > ? ";
            inProgressQueryParams.add(dateToStringConverterMillis(window));
        }

        try (CachedRowSet rowSet = dbLibService.getData(IN_PROGRESS_REQUESTS_QUERY, inProgressQueryParams, SCHEMA)) {
            List<TransactionRecord> inProgressRecords = new ArrayList<>();
            TransactionRecord transaction;
            while (rowSet.next()) {
                transaction = new TransactionRecord();
                transaction.setTransactionId(rowSet.getString(TRANSACTION_ID.getColumnName()));
                transaction.setRequestId(rowSet.getString(REQUEST_ID.getColumnName()));
                transaction.setSubRequestId(rowSet.getString(SUBREQUEST_ID.getColumnName()));
                transaction.setOriginatorId(rowSet.getString(ORIGINATOR_ID.getColumnName()));
                transaction.setStartTime(stringToDateConverterMillis(rowSet.getString(START_TIME.getColumnName())));
                transaction.setTargetId(rowSet.getString(TARGET_ID.getColumnName()));
                transaction.setTargetType(rowSet.getString(TARGET_TYPE.getColumnName()));
                transaction.setOperation(VNFOperation.valueOf(rowSet.getString(OPERATION.getColumnName())));
                transaction.setRequestState(RequestStatus.valueOf(rowSet.getString(STATE.getColumnName())));
                transaction.setVnfcName(rowSet.getString(VNFC_NAME.getColumnName()));
                transaction.setVserverId(rowSet.getString(VSERVER_ID.getColumnName()));
                transaction.setVfModuleId(rowSet.getString(VF_MODULE_ID.getColumnName()));
                transaction.setServiceInstanceId(rowSet.getString(SERVICE_INSTANCE_ID.getColumnName()));
                transaction.setMode(Flags.Mode.valueOf(rowSet.getString(MODE.getColumnName())));
                inProgressRecords.add(transaction);
            }
            if (logger.isTraceEnabled()) {
                logger.trace("In progress transaction records fetched from database successfully.");
            }
            return inProgressRecords;
        } catch (ParseException e) {
            logger.error("Error parsing start date during fetching in progress records ", e);
            throw new APPCException(ERROR_ACCESSING_DATABASE, e);
        } catch (SQLException e) {
            logger.error("Error fetching in progress records for Transaction ID = " + appcInstanceId + "~" + record
                .getTransactionId(), e);
            throw new APPCException(ERROR_ACCESSING_DATABASE, e);
        }
    }

    @Override
    public Boolean isTransactionDuplicate(TransactionRecord record) throws APPCException {

        StringBuilder duplicateRequestCheckQuery = new StringBuilder("SELECT " +
            TRANSACTION_ID.getColumnName() + " FROM " +
            TransactionConstants.TRANSACTIONS + WHERE +
            TRANSACTION_ID.getColumnName() + " <> ? AND " +
            REQUEST_ID.getColumnName() + " = ? AND " +
            STATE.getColumnName() + " IN(?,?) ");

        ArrayList<String> duplicateCheckParams = new ArrayList<>();
        duplicateCheckParams.add(appcInstanceId + "~" + record.getTransactionId());
        duplicateCheckParams.add(record.getRequestId());
        duplicateCheckParams.add(RequestStatus.RECEIVED.name());
        duplicateCheckParams.add(RequestStatus.ACCEPTED.name());

        if (!StringUtils.isBlank(record.getSubRequestId())) {
            duplicateRequestCheckQuery.append(AND + SUBREQUEST_ID.getColumnName() + " = ? ");
            duplicateCheckParams.add(record.getSubRequestId());
        } else {
            duplicateRequestCheckQuery.append(AND + SUBREQUEST_ID.getColumnName() + IS_NULL);
        }
        if (!StringUtils.isBlank(record.getOriginatorId())) {
            duplicateRequestCheckQuery.append(AND + ORIGINATOR_ID.getColumnName() + " = ? ");
            duplicateCheckParams.add(record.getOriginatorId());
        } else {
            duplicateRequestCheckQuery.append(AND + ORIGINATOR_ID.getColumnName() + IS_NULL);
        }
        if (logger.isDebugEnabled()) {
            logger.debug(duplicateRequestCheckQuery.toString());
        }
        try (CachedRowSet rowSet = dbLibService.getData(duplicateRequestCheckQuery.toString(), duplicateCheckParams,
            SCHEMA)) {
            if (rowSet.first()) {
                String transactionId = rowSet.getString(TRANSACTION_ID.getColumnName());
                if (logger.isErrorEnabled()) {
                    logger.error("Duplicate request found. Transaction ID " + transactionId + " is currently in " +
                        "progress.");
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            logger.error("Error checking duplicate records for Transaction ID = " + appcInstanceId + "~" + record
                .getTransactionId(), e);
            throw new APPCException(ERROR_ACCESSING_DATABASE, e);
        }
    }

    @Override
    public Integer getInProgressRequestsCount() throws APPCException {
        final String inProgressRequestCountQuery = "SELECT COUNT(*) as VALUE FROM "
            + TransactionConstants.TRANSACTIONS
            + WHERE + STATE.getColumnName() + " IN (?,?) ";

        ArrayList<String> checkInProgressParams = new ArrayList<>();
        checkInProgressParams.add(RequestStatus.RECEIVED.name());
        checkInProgressParams.add(RequestStatus.ACCEPTED.name());
        try(CachedRowSet rowSet=dbLibService.getData(inProgressRequestCountQuery,checkInProgressParams,SCHEMA)){
            if (rowSet.first()) {
                int count = rowSet.getInt("VALUE");
                logger.info("In progress request count fetched from database successfully.");
                return count;
            }
        }
        catch (SQLException e) {
            logger.error("Error checking in progress request count in the transaction table", e);
            throw new APPCException(ERROR_ACCESSING_DATABASE, e);
        }
        logger.error("Error checking in progress request count in the transaction table");
        throw new APPCException(ERROR_ACCESSING_DATABASE);
    }

    @Override
    public void setAppcInstanceId(String appcInstanceId) {
        this.appcInstanceId = appcInstanceId;
    }


    @Override
    public List<RequestStatus> getRecords(String requestId, String subrequestId, String originatorId, String vnfId)
        throws APPCException {
        StringBuilder queryString = (new StringBuilder(1024))
            .append("SELECT " + TRANSACTION_ATTRIBUTES.STATE.getColumnName())
            .append(" FROM " + TRANSACTIONS)
            .append(" WHERE " + TRANSACTION_ATTRIBUTES.REQUEST_ID.getColumnName() + "  = ? AND " +
                TRANSACTION_ATTRIBUTES.TARGET_ID.getColumnName() + " = ?");

        ArrayList<String> argList = new ArrayList<>();
        argList.add(requestId);
        argList.add(vnfId);

        if (subrequestId != null) {
            queryString.append(" AND " + TRANSACTION_ATTRIBUTES.SUBREQUEST_ID.getColumnName() + " = ?");
            argList.add(subrequestId);
        }
        if (originatorId != null) {
            queryString.append(" AND " + TRANSACTION_ATTRIBUTES.ORIGINATOR_ID.getColumnName() + " = ?");
            argList.add(originatorId);
        }

        List<RequestStatus> requestStatusList = new ArrayList<>();
        try {
            CachedRowSet resultSet = dbLibService.getData(queryString.toString(), argList, SCHEMA);
            while (resultSet.next()) {
                String name = resultSet.getString(TRANSACTION_ATTRIBUTES.STATE.getColumnName());
                RequestStatus requestStatus = null;
                try {
                    requestStatus = RequestStatus.valueOf(name);
                } catch (IllegalArgumentException e) {
                    logger.error(String.format("Invalid request status (%s) using (%s) :", name, RequestStatus
                        .UNKNOWN), e);
                    requestStatus = RequestStatus.UNKNOWN;
                }
                requestStatusList.add(requestStatus);
                logger.debug(String.format("Request Status obtained (%s).", requestStatus));
            }
        } catch (SQLException e) {
            logger.error("Error Accessing Database ", e);
            throw new APPCException(String.format("Error retrieving record for requestID %s and vnfId %s " +
                "from the transactions table", requestId, vnfId), e);
        }

        return requestStatusList;
    }

    private ArrayList<String> prepareArguments(TransactionRecord input) {
        ArrayList<String> arguments = new ArrayList<>();
        arguments.add(appcInstanceId + "~" + input.getTransactionId());
        arguments.add(dateToStringConverterMillis(input.getOriginTimestamp()));
        arguments.add(input.getRequestId());
        arguments.add(input.getSubRequestId());
        arguments.add(input.getOriginatorId());
        arguments.add(dateToStringConverterMillis(input.getStartTime()));
        arguments.add(dateToStringConverterMillis(input.getEndTime()));
        arguments.add(input.getTargetId());
        arguments.add(input.getTargetType());
        arguments.add(input.getOperation().name());
        arguments.add(String.valueOf(input.getResultCode()));
        arguments.add(input.getDescription());
        arguments.add(input.getRequestState());
        arguments.add(input.getServiceInstanceId());
        arguments.add(input.getVnfcName());
        arguments.add(input.getVserverId());
        arguments.add(input.getVfModuleId());
        arguments.add(input.getMode());

        return arguments;
    }

    private static String dateToStringConverterMillis(Instant date) {
        if (date == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(ZoneOffset.UTC);
        return formatter.format(date);
    }

    private static Instant stringToDateConverterMillis(String dateString) throws ParseException {
        SimpleDateFormat customDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return customDate.parse(dateString).toInstant();
    }
}
