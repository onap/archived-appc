/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications (C) 2019 Ericsson
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

package org.onap.appc.dao.util.dbcp;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.onap.appc.dao.util.exception.DBConnectionPoolException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;


public class DBConnectionPoolTest {
    private final String connectURI = "jdbc:h2:mem:~/test;MODE=MYSQL;DB_CLOSE_DELAY=-1";
    private final String username = "sa";
    private final String password = "sa";
    private final String driverClass = "org.h2.Driver";

    private DBConnectionPool dbcp;
    private DBConnectionPool dbcp2;
    private Connection connection;

    @Before
    public void setUp() throws Exception {
        dbcp = new DBConnectionPool(connectURI, username, password, driverClass);
        dbcp2 = new DBConnectionPool(connectURI, username, password, driverClass);
    }

    @Test
    public void testGetConnection() {
        try {
            connection = dbcp.getConnection();
        } catch (DBConnectionPoolException e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertNotNull(connection);
    }

    @Test
    public void testGetDataSourceStatus() {
        Map<String, Integer> dataSourceStatus = dbcp.getDataSourceStatus();
        Assert.assertNotNull(dataSourceStatus);
    }

    @Test(expected = DBConnectionPoolException.class)
    public void testShutdown() throws DBConnectionPoolException {
        dbcp2.shutdown();
        connection = dbcp2.getConnection();
        Assert.assertNull(connection);
    }

    @After
    public void clean() {
        if (dbcp != null) {
            dbcp.shutdown();
        }
        if (dbcp2 != null) {
            dbcp2.shutdown();
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
