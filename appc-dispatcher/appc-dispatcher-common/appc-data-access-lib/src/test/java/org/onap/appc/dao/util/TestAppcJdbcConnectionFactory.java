/*-
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

package org.onap.appc.dao.util;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.sql.Connection;
import java.sql.SQLException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.onap.appc.dao.util.exception.JdbcRuntimeException;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DBUtils.class)
public class TestAppcJdbcConnectionFactory {

    AppcJdbcConnectionFactory appcJdbcConnectionFactory;
    private Connection connection;

    @Before
    public void setUp() throws SQLException {
        appcJdbcConnectionFactory = new AppcJdbcConnectionFactory();
        connection = Mockito.mock(Connection.class);
        PowerMockito.mockStatic(DBUtils.class);
        PowerMockito.when(DBUtils.getConnection("sdnctl")).thenReturn(connection);
    }

    @Test
    public void testOpenDbConnection() {
        appcJdbcConnectionFactory.setSchema("sdnctl");
        assertNotNull(appcJdbcConnectionFactory.openDbConnection());
    }

    @Test(expected = JdbcRuntimeException.class)
    public void testOpenDbConnectionException() throws SQLException {
        PowerMockito.when(DBUtils.getConnection("sdnctl")).thenThrow(new SQLException());
        appcJdbcConnectionFactory.setSchema("sdnctl");
        assertNotNull(appcJdbcConnectionFactory.openDbConnection());
    }

    @Test
    public void testCloseDbConnection() throws SQLException {
        appcJdbcConnectionFactory.closeDbConnection(connection);
        verify(connection, times(1)).close();
    }

    @Test(expected = JdbcRuntimeException.class)
    public void testCloseDbConnectionException() throws SQLException {
        doThrow(new SQLException()).when(connection).close();
        appcJdbcConnectionFactory.closeDbConnection(connection);
    }
}
