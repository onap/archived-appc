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

import com.sun.rowset.CachedRowSetImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import org.onap.appc.dao.util.dbcp.DBConnectionPool;
import org.onap.appc.dao.util.helper.DBHelper;
import org.onap.appc.domainmodel.lcm.Flags;
import org.onap.appc.domainmodel.lcm.RequestStatus;
import org.onap.appc.domainmodel.lcm.TransactionRecord;
import org.onap.appc.domainmodel.lcm.VNFOperation;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.transactionrecorder.objects.TransactionConstants;
import org.onap.ccsdk.sli.core.dblib.DbLibService;

import javax.sql.rowset.CachedRowSet;
import java.sql.*;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Matchers.*;

/**
 * Test class for TransactionRecorder
 */
public class TransactionRecorderImplTest {

    private String dbUrl = "jdbc:h2:mem:test;MODE=MYSQL;DB_CLOSE_DELAY=-1";
    private String username = "sa";
    private String password = "sa";
    private String driver = "org.h2.Driver";

    private TransactionRecorderImpl transactionRecorderImpl;
    private DbLibService dbLibService;

    private DBConnectionPool dbConnectionPool;


    /**
     * Ideally JUnit should grab the SQL to create the transaction table from the same source used in deployments;
     * however, at the time of writing this that was not possible.  Should it become possible in the future please
     * update this JUnit test to use the deployment source.
     * <p>
     * Please ensure this table create script is identical to the source script used in a deployment.
     */
    private String TRANSACTION_CREATE_TABLE = "CREATE TABLE TRANSACTIONS (" +
            "  TRANSACTION_ID VARCHAR(75) NOT NULL PRIMARY KEY," +
            "  ORIGIN_TIMESTAMP DATETIME(3) NOT NULL," +
            "  REQUEST_ID VARCHAR(256) NOT NULL," +
            "  SUBREQUEST_ID VARCHAR(256) DEFAULT NULL," +
            "  ORIGINATOR_ID VARCHAR(256) DEFAULT NULL," +
            "  START_TIME DATETIME(3) NOT NULL," +
            "  END_TIME DATETIME(3) DEFAULT NULL," +
            "  TARGET_ID VARCHAR(256) NOT NULL," +
            "  TARGET_TYPE VARCHAR(256) DEFAULT NULL," +
            "  OPERATION VARCHAR(256) NOT NULL," +
            "  RESULT_CODE INT(11) DEFAULT NULL," +
            "  DESCRIPTION TEXT," +
            "  STATE VARCHAR(50) NOT NULL," +
            "  SERVICE_INSTANCE_ID VARCHAR(256) DEFAULT NULL," +
            "  VNFC_NAME VARCHAR(256) DEFAULT NULL," +
            "  VSERVER_ID VARCHAR(256) DEFAULT NULL," +
            "  VF_MODULE_ID VARCHAR(256) DEFAULT NULL," +
            "  MODE VARCHAR(50) NOT NULL," +
            ")";
    private String TRANSACTION_DROP_TABLE = "DROP TABLE IF EXISTS TRANSACTIONS";

    @Before
    public void setUp() throws Exception {
        transactionRecorderImpl = new TransactionRecorderImpl();
        transactionRecorderImpl.setAppcInstanceId("123");
        dbLibService = Mockito.mock(DbLibService.class);
        transactionRecorderImpl.setDbLibService(dbLibService);
        dbConnectionPool = new DBConnectionPool(dbUrl, username, password, driver);
        executeUpdate(TRANSACTION_CREATE_TABLE);

    }


    @After
    public void shutdown() {
        if (dbConnectionPool != null) {
            executeUpdate(TRANSACTION_DROP_TABLE);
            dbConnectionPool.shutdown();
        }
    }

    private void executeUpdate(String updateSQL) {
        Connection connection = null;
        Statement stmt = null;
        try {
            connection = dbConnectionPool.getConnection();
            stmt = connection.createStatement();
            stmt.executeUpdate(updateSQL);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DBHelper.close(null, stmt, connection);
        }
    }

    /**
     * Verify the transactionRecorderImpl.sore() store the TransactionRecord correctly in the database.
     */
    @Test
    public void testStore() throws Exception {

        TransactionRecord input = prepareTransactionsInput();
        Mockito.when(dbLibService.writeData(anyString(), anyObject(), anyString())).thenAnswer(invocation ->
                testStoreInMemory(invocation.getArguments()));
        transactionRecorderImpl.store(input);

    }

    @Test
    public void testGetInProgressRequests() throws SQLException, APPCException {
        TransactionRecord record1 = prepareTransactionsInput();
        insertRecord(record1);
        TransactionRecord input = prepareTransactionsInput();
        input.setStartTime(Instant.now());
        Mockito.when(dbLibService.getData(anyString(), anyObject(), anyString())).thenAnswer(invocation ->
                inMemoryExecutionWithResultSet(invocation.getArguments()));
        Assert.assertEquals(1, transactionRecorderImpl.getInProgressRequests(input,0).size());

    }

    @Test
    public void testGetInProgressRequestsWithinTimeInterval() throws SQLException, APPCException {
        TransactionRecord record1 = prepareTransactionsInput();
        record1.setStartTime(Instant.now().minus(4,ChronoUnit.HOURS));
        insertRecord(record1);
        TransactionRecord input = prepareTransactionsInput();
        input.setStartTime(Instant.now());
        Mockito.when(dbLibService.getData(anyString(), anyObject(), anyString())).thenAnswer(invocation ->
                inMemoryExecutionWithResultSet(invocation.getArguments()));
        List<TransactionRecord> aList= transactionRecorderImpl.getInProgressRequests(input,12);
        Assert.assertEquals(1, transactionRecorderImpl.getInProgressRequests(input,12).size());

    }

    @Test
    public void testIsTransactionDuplicate() throws SQLException, APPCException {
        TransactionRecord input = prepareTransactionsInput();
        Mockito.when(dbLibService.getData(anyString(), anyObject(), anyString())).thenAnswer(invocation ->
                inMemoryExecutionWithResultSet(invocation.getArguments()));
        Assert.assertFalse(transactionRecorderImpl.isTransactionDuplicate(input));

    }

    @Test
    public void testGetInProgressRequestsCount() throws SQLException, APPCException {
        TransactionRecord input = prepareTransactionsInput();
        Mockito.when(dbLibService.getData(anyString(), anyObject(), anyString())).thenAnswer(invocation ->
                inMemoryExecutionWithResultSet(invocation.getArguments()));
        Assert.assertEquals(0, transactionRecorderImpl.getInProgressRequestsCount().intValue());
    }

    @Test
    public void testUpdate() throws APPCException, SQLException {
        TransactionRecord input = prepareTransactionsInput();
        insertRecord(input);
        Map<TransactionConstants.TRANSACTION_ATTRIBUTES, String> updateColumns = new HashMap<>();
        updateColumns.put(TransactionConstants.TRANSACTION_ATTRIBUTES.TARGET_TYPE, "Firewall");
        Mockito.when(dbLibService.writeData(anyString(), anyObject(), anyString())).thenAnswer(invocation ->
                testUpdateInMemory(invocation.getArguments()));
        transactionRecorderImpl.update(input.getTransactionId(), updateColumns);
    }

    @Test
    public void testMarkTransactionsAborted() throws SQLException {
        TransactionRecord input = prepareTransactionsInput();
        insertRecord(input);
        Mockito.when(dbLibService.writeData(anyString(), anyObject(), anyString())).thenAnswer(invocation ->
                testMarkAbortedInMemory(invocation.getArguments()));
        transactionRecorderImpl.markTransactionsAborted("123~");
    }

    private ResultSet inMemoryExecutionWithResultSet(Object[] obj) throws Exception {
        String query = (String) obj[0];
        ArrayList<String> args = (ArrayList<String>) obj[1];
        Connection con = dbConnectionPool.getConnection();
        PreparedStatement ps = con.prepareStatement(query);
        for (int i = 1; i <= args.size(); i++) {
            ps.setString(i, args.get(i - 1));
        }
        CachedRowSet rowSet = new CachedRowSetImpl();
        rowSet.populate(ps.executeQuery());
        return rowSet;
    }

    private boolean testMarkAbortedInMemory(Object[] obj) throws Exception {
        String query = (String) obj[0];
        ArrayList<String> args = (ArrayList<String>) obj[1];
        Connection con = dbConnectionPool.getConnection();
        PreparedStatement ps = con.prepareStatement(query);
        for (int i = 1; i <= args.size(); i++) {
            ps.setString(i, args.get(i - 1));
        }
        ps.execute();
        return isTransactionAborted();
    }

    private boolean isTransactionAborted() throws Exception {
        String query = "SELECT COUNT(*) FROM  TRANSACTIONS WHERE STATE = ?";
        Connection con = dbConnectionPool.getConnection();
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, RequestStatus.ABORTED.toString());
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            int value = rs.getInt(1);
            if (value == 1) {
                System.out.println("Non terminal Transactions are aborted");
                return true;
            }
        }
        throw new Exception("Transactions are not aborted");
    }

    private boolean testUpdateInMemory(Object[] obj) throws Exception {
        String query = (String) obj[0];
        ArrayList<String> args = (ArrayList<String>) obj[1];
        Connection con = dbConnectionPool.getConnection();
        PreparedStatement ps = con.prepareStatement(query);
        for (int i = 1; i <= args.size(); i++) {
            ps.setString(i, args.get(i - 1));
        }
        ps.execute();
        String updatedValue = checkIfValueIsUpdated(args.get(1));
        System.out.println("updated Value is " + updatedValue);
        if (updatedValue.equals("Firewall")) {
            return true;
        }
        throw new Exception("Not Updated");
    }

    private boolean testStoreInMemory(Object[] obj) throws Exception {
        String query = (String) obj[0];
        ArrayList<String> args = (ArrayList<String>) obj[1];
        Connection con = dbConnectionPool.getConnection();
        PreparedStatement ps = con.prepareStatement(query);
        for (int i = 1; i <= args.size(); i++) {
            ps.setString(i, args.get(i - 1));
        }
        ps.execute();
        if (checkIfRowIsPresent(args.get(0))) {
            return true;
        }
        throw new Exception("Failed to update");
    }

    private TransactionRecord prepareTransactionsInput() {
        TransactionRecord input = new TransactionRecord();
        input.setTransactionId(UUID.randomUUID().toString());
        input.setOriginTimestamp(Instant.parse("2017-09-11T00:00:01.00Z"));
        input.setRequestId("REQUEST_ID");
        input.setSubRequestId("SUB_REQUEST_ID");
        input.setOriginatorId("ORIGINATOR_ID");
        input.setStartTime(Instant.parse("2017-09-11T00:00:02.00Z"));
        input.setTargetId("TARGET_ID");
        input.setTargetType("TARGET_TYPE");
        input.setServiceInstanceId("SERVICE_INSTANCE_ID");
        input.setOperation(VNFOperation.ActionStatus);
        input.setResultCode(200);
        input.setRequestState(RequestStatus.ACCEPTED);
        input.setDescription("DESCRIPTION");
        input.setMode(Flags.Mode.EXCLUSIVE);
        return input;
    }

    private void insertRecord(TransactionRecord input) throws SQLException {
        final String STORE_DATE_QUERY = TransactionConstants.INSERT_INTO + TransactionConstants.TRANSACTIONS +
                " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        Connection con = dbConnectionPool.getConnection();
        PreparedStatement ps = con.prepareStatement(STORE_DATE_QUERY);
        ArrayList<String> args = prepareArguments(input);
        args.remove(0);
        args.add(0, "123~" + input.getTransactionId());
        for (int i = 1; i <= 18; i++) {
            ps.setString(i, args.get(i - 1));
        }
        ps.execute();
        if (checkIfRowIsPresent(args.get(0))) {
            System.out.println("RECORD INSERTED " + args.get(0));
        }

    }

    private ArrayList<String> prepareArguments(TransactionRecord input) {
        ArrayList<String> arguments = new ArrayList<>();
        arguments.add(input.getTransactionId());
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

    private boolean checkIfRowIsPresent(String key) {
        Connection con = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            con = dbConnectionPool.getConnection();
            ps = con.prepareStatement("SELECT COUNT(*) FROM  TRANSACTIONS WHERE TRANSACTION_ID = ?");
            ps.setString(1, key);
            rs = ps.executeQuery();
            while (rs.next()) {
                int value = rs.getInt(1);
                System.out.println("KEY checked is " + key + " COUNT RETURNED IS " + value);
                if (value == 1) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBHelper.close(rs, ps, con);
        }
        return false;
    }

    private String checkIfValueIsUpdated(String key) throws Exception {
        Connection con = dbConnectionPool.getConnection();
        PreparedStatement ps = con.prepareStatement("SELECT TARGET_TYPE FROM  TRANSACTIONS WHERE TRANSACTION_ID = ?");
        ps.setString(1, key);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            String value = rs.getString("TARGET_TYPE");
            return value;
        }
        throw new Exception("Value not found");
    }


    /**
     * Verify the transactionRecorderImpl. getRecords () can be fetch with each of the parameter combinations
     * @throws Exception
     *//*
    @Test
    public void test_api_getRecords() throws Exception {


        final int requestId = 0;
        final int subrequestId = 1;
        final int originatorId = 2;
        final int vnfId = 3;
        final int requestStatus = 4;


        String[][] trCreateMatrix = {
            {"request1", "subrequestId1", "originatorId1", "vnfId1", RequestStatus.UNKNOWN.name()},
            {"request1", "subrequestId2", "originatorId1", "vnfId1", RequestStatus.RECEIVED.name()},
            {"request2", "subrequestId1", "originatorId1", "vnfId1", RequestStatus.ACCEPTED.name()},
            {"request2", "subrequestId2", "originatorId1", "vnfId1", RequestStatus.REJECTED.name()},
            {"request1", "subrequestId1", "originatorId1", "vnfId2", RequestStatus.SUCCESSFUL.name()},
            {"request1", "subrequestId2", "originatorId1", "vnfId2", RequestStatus.FAILED.name()},
            {"request2", "subrequestId1", "originatorId1", "vnfId2", RequestStatus.TIMEOUT.name()},
            {"request2", "subrequestId2", "originatorId1", "vnfId2", RequestStatus.ABORTED.name()},
            {"request1", "subrequestId1", "originatorId2", "vnfId1", RequestStatus.UNKNOWN.name()},
            {"request1", "subrequestId2", "originatorId2", "vnfId1", RequestStatus.RECEIVED.name()},
            {"request2", "subrequestId1", "originatorId2", "vnfId1", RequestStatus.ACCEPTED.name()},
            {"request2", "subrequestId2", "originatorId2", "vnfId1", RequestStatus.REJECTED.name()},
            {"request1", "subrequestId1", "originatorId2", "vnfId2", RequestStatus.SUCCESSFUL.name()},
            {"request1", "subrequestId2", "originatorId2", "vnfId2", RequestStatus.FAILED.name()},
            {"request2", "subrequestId1", "originatorId2", "vnfId2", RequestStatus.TIMEOUT.name()},
            {"request2", "subrequestId2", "originatorId2", "vnfId2", RequestStatus.ABORTED.name()},
        };


        TransactionRecord tr = new TransactionRecord();
        tr.setTimeStamp(Instant.parse("2017-09-11T00:00:01.00Z"));
        tr.setStartTime(Instant.parse("2017-09-11T00:00:02.00Z"));
        tr.setEndTime(Instant.parse("2017-09-11T00:00:03.00Z"));
        tr.setTargetType("TARGET_TYPE");
        tr.setSubComponent("SUB_COMPONENT");
        tr.setOperation(VNFOperation.ActionStatus);
        tr.setResultCode("RESULT_CODE");
        tr.setDescription("DESCRIPTION");

        for (int row = 0; row < trCreateMatrix.length; row++) {
            tr.setRequestID(trCreateMatrix[row][requestId]);
            tr.setSubRequestID(trCreateMatrix[row][subrequestId]);
            tr.setOriginatorId(trCreateMatrix[row][originatorId]);
            tr.setTargetID(trCreateMatrix[row][vnfId]);
            tr.setRequestStatus(RequestStatus.valueOf(trCreateMatrix[row][requestStatus]));
            transactionRecorderImpl.store(tr);
        }


        String[][] trSearchMatrix = {
            {"request1", null, null, "vnfId1"},
            {"request2", "subrequestId1", null, "vnfId1"},
            {"request1", null, "originatorId1", "vnfId1"},
            {"request2", "subrequestId2", "originatorId1", "vnfId1"},
        };


        for (int i = 0; i < trSearchMatrix.length; i++) {
            final int row = i;
            List<RequestStatus> actualList = transactionRecorderImpl
                .getRecords(trSearchMatrix[row][requestId], trSearchMatrix[row][subrequestId],
                    trSearchMatrix[row][originatorId], trSearchMatrix[row][vnfId])
                .stream()
                .sorted()
                .collect(Collectors.toList());

            List<RequestStatus> expectedList = Arrays.stream(trCreateMatrix)
                .filter(entry -> entry[requestId].equals(trSearchMatrix[row][requestId]))
                .filter(entry -> trSearchMatrix[row][subrequestId] == null || entry[subrequestId].equals
                    (trSearchMatrix[row][subrequestId]))
                .filter(entry -> trSearchMatrix[row][originatorId] == null || entry[originatorId].equals
                    (trSearchMatrix[row][originatorId]))
                .filter(entry -> entry[vnfId].equals(trSearchMatrix[row][vnfId]))
                .map(entry -> RequestStatus.valueOf(entry[requestStatus]))
                .sorted()
                .collect(Collectors.toList());
            System.out.println(expectedList);
            System.out.println(actualList);
            Assert.assertEquals("Unexpected results: ", expectedList, actualList);

        }


    }*/
}
