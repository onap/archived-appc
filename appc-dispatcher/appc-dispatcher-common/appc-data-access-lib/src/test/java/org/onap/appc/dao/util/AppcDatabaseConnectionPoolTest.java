/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications Copyright (C) 2019 Ericsson
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

package org.onap.appc.dao.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import java.sql.Connection;
import java.sql.SQLException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.onap.appc.configuration.Configuration;
import org.onap.appc.configuration.ConfigurationFactory;
import org.onap.appc.dao.util.dbcp.DBConnectionPool;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ConfigurationFactory.class})
@PowerMockIgnore("javax.management.*")
public class AppcDatabaseConnectionPoolTest {
    private String dbUrl = "jdbc:h2:mem:~/test;MODE=MYSQL;DB_CLOSE_DELAY=-1";
    private String username = "sa";
    private String password = "sa";
    private String driver = "org.h2.Driver";

    private Configuration configuration;

    private DBConnectionPool dbConnectionPool;
    private AppcDatabaseConnectionPool appcDatabaseConnectionPool;

    @Before
    public void setUp() throws Exception {
        mockStatic(ConfigurationFactory.class);
        when(ConfigurationFactory.getConfiguration()).thenReturn(configuration);
        appcDatabaseConnectionPool = spy(new AppcDatabaseConnectionPool(dbUrl, username, password, driver));
        dbConnectionPool = mock(DBConnectionPool.class);
        Whitebox.setInternalState(appcDatabaseConnectionPool, "dbConnectionPool", dbConnectionPool);
    }

    @Test
    public void testDBURL() {
        String dbString;

        dbString = PropertyPattern.DBURL.getPattern();
        dbString = String.format(dbString, "test");
        Assert.assertEquals("org.onap.appc.db.url.test", dbString);
    }

    @Test
    public void testUSERNAME() {
        String dbString;

        dbString = PropertyPattern.USERNAME.getPattern();
        dbString = String.format(dbString, "test");
        Assert.assertEquals("org.onap.appc.db.user.test", dbString);
    }

    @Test
    public void testPASSWORD() {
        String dbString;

        dbString = PropertyPattern.PASSWORD.getPattern();
        dbString = String.format(dbString, "test");
        Assert.assertEquals("org.onap.appc.db.pass.test", dbString);
    }

    @Test
    public void testDRIVER() {
        String dbString;

        dbString = PropertyPattern.DRIVER.getPattern();
        Assert.assertEquals("org.onap.appc.db.jdbc.driver", dbString);
    }

    @Test
    public void testArgumentConstructor() {
        AppcDatabaseConnectionPool appcDatabaseConnectionPool = new AppcDatabaseConnectionPool(dbUrl, username,
            password, driver);
        Object dbConnectionPool = Whitebox.getInternalState(appcDatabaseConnectionPool, "dbConnectionPool");
        Assert.assertNotNull(dbConnectionPool);
    }

    @Test
    public void testGetConnection() throws SQLException {
        appcDatabaseConnectionPool.getConnection();
        Mockito.verify(dbConnectionPool, times(1)).getConnection();
    }

    @Test
    public void testDestroy() throws SQLException {
        appcDatabaseConnectionPool.destroy();
        Mockito.verify(dbConnectionPool, times(1)).shutdown();
    }

    @Test
    public void testGetDataSourceStatus() {
        appcDatabaseConnectionPool.getDataSourceStatus();
        Mockito.verify(dbConnectionPool, times(1)).getDataSourceStatus();
    }

    @Test
    public void testInit() throws SQLException {
        Configuration mockConfiguration = Mockito.mock(Configuration.class);
        when(ConfigurationFactory.getConfiguration()).thenReturn(mockConfiguration);
        when(mockConfiguration.getProperty(Mockito.anyString(), Mockito.anyString())).thenReturn("");
        DBConnectionPool mockDbConnectionPool = Mockito.mock(DBConnectionPool.class);
        Connection mockConnection = Mockito.mock(Connection.class);
        when(mockDbConnectionPool.getConnection()).thenReturn(mockConnection);
        when(appcDatabaseConnectionPool.getDBConnectionPool("", "", "", "")).thenReturn(mockDbConnectionPool);
        appcDatabaseConnectionPool.init();
        Mockito.verify(mockConnection).close();
    }

    @Test
    public void testSetDbName() {
        AppcDatabaseConnectionPool pool = new AppcDatabaseConnectionPool();
        pool.setDbName("TEST");
        assertEquals("TEST", Whitebox.getInternalState(pool, "dbName"));
    }
}
