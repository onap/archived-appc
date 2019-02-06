/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 Ericsson
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
 *
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.dao.util.helper;

import org.junit.Test;
import org.mockito.Mockito;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBHelperTest {

    @Test
    public void testClose() throws SQLException {
        ResultSet resultSet = Mockito.mock(ResultSet.class);
        Statement statement = Mockito.mock(Statement.class);
        Connection connection = Mockito.mock(Connection.class);
        DBHelper.close(resultSet, statement, connection);
        Mockito.verify(connection).close();
    }

    @Test
    public void testCloseResultSet() throws SQLException {
        ResultSet resultSet = Mockito.mock(ResultSet.class);
        Mockito.doThrow(new SQLException()).when(resultSet).close();
        DBHelper.closeResultSet(resultSet);
        Mockito.verify(resultSet, Mockito.times(1)).close();
    }

    @Test
    public void testCloseResultSetNull() throws SQLException {
        ResultSet resultSet = Mockito.mock(ResultSet.class);
        Mockito.doThrow(new SQLException()).when(resultSet).close();
        DBHelper.closeResultSet(null);
        Mockito.verify(resultSet, Mockito.times(0)).close();
    }

    @Test
    public void testCloseStatement() throws SQLException {
        Statement statement = Mockito.mock(Statement.class);
        Mockito.doThrow(new SQLException()).when(statement).close();
        DBHelper.closeStatement(statement);
        Mockito.verify(statement, Mockito.times(1)).close();
    }

    @Test
    public void testCloseStatementNull() throws SQLException {
        Statement statement = Mockito.mock(Statement.class);
        Mockito.doThrow(new SQLException()).when(statement).close();
        DBHelper.closeStatement(null);
        Mockito.verify(statement, Mockito.times(0)).close();
    }

    @Test
    public void testCloseConnection() throws SQLException {
        Connection connection = Mockito.mock(Connection.class);
        Mockito.doThrow(new SQLException()).when(connection).close();
        DBHelper.closeConnection(connection);
        Mockito.verify(connection, Mockito.times(1)).close();
    }

    @Test
    public void testCloseConnectionNull() throws SQLException {
        Connection connection = Mockito.mock(Connection.class);
        Mockito.doThrow(new SQLException()).when(connection).close();
        DBHelper.closeConnection(null);
        Mockito.verify(connection, Mockito.times(0)).close();
    }
}
