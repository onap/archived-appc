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

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.onap.appc.configuration.Configuration;
import org.onap.appc.configuration.ConfigurationFactory;
import org.onap.appc.dao.util.api.DBConnectionPoolService;
import org.onap.appc.dao.util.dbcp.DBConnectionPool;
import org.onap.appc.dao.util.exception.DBConnectionPoolException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * This class implements
 *
 * @see org.onap.appc.dao.util.dbcp.DBConnectionPool
 * that provides concrete implemenation of accessing appc database which basic setup
 * data would be got from global configuration.
 * @see org.onap.appc.configuration.Configuration
 * <p>
 * The singleton instance of this class has been instantiated by blueprint.
 * An example is shown in the {@link DBConnectionPoolService}
 */
public class AppcDatabaseConnectionPool implements DBConnectionPoolService {

    private final EELFLogger logger = EELFManager.getInstance().getLogger(AppcDatabaseConnectionPool.class);

    private DBConnectionPool dbConnectionPool;
    private String dbName;

    public AppcDatabaseConnectionPool() {
        // do nothing
    }

    public AppcDatabaseConnectionPool(String dbUrl, String userName, String password, String jdbcDriver) {
        dbConnectionPool = new DBConnectionPool(dbUrl, userName, password, jdbcDriver);
    }

    /**
     * Injected by blueprint
     *
     * @param dbName
     */
    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    /**
     * Bean init method used by blueprint
     */
    public void init() {
        Configuration configuration = ConfigurationFactory.getConfiguration();
        String dbUrl = getConnectionProperty(configuration, PropertyPattern.DBURL);
        String userName = getConnectionProperty(configuration, PropertyPattern.USERNAME);
        String password = getConnectionProperty(configuration, PropertyPattern.PASSWORD);
        String jdbcDriver = getJDBCDriver(configuration);

        dbConnectionPool = getDBConnectionPool(dbUrl, userName, password, jdbcDriver);
        // a simple health check
        Connection connection = null;
        try {
            connection = dbConnectionPool.getConnection();
        } catch (DBConnectionPoolException e) {
            logger.error("DB connection pool is created failed." +
                "Please make sure the provided information is correct.");
        }

        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                logger.error("DB connection cannot be closed:", e.getMessage());
            }
        }
    }

    /**
     * Bean destroy method used by blueprint
     */
    public void destroy() {
        if (dbConnectionPool != null) {
            dbConnectionPool.shutdown();
        }
    }

    /**
     * Get the connection from connection pool.
     *
     * @return Connection. If the provided db information is not correct,
     * the return value might be null.
     */
    @Override
    public Connection getConnection() throws DBConnectionPoolException {
        return dbConnectionPool.getConnection();
    }

    /**
     * Get dbcp status like active_status.
     * <p>
     * More details about status of DBConnectionPool,
     * go check {@link org.onap.appc.dao.util.dbcp.DBConnectionPool#getDataSourceStatus()}
     *
     * @return a map contains some dbcp information.
     */
    @Override
    public Map<String, Integer> getDataSourceStatus() {
        return dbConnectionPool.getDataSourceStatus();
    }

    private String getConnectionProperty(Configuration configuration, PropertyPattern propertyPattern) {
        String property = configuration.getProperty(String.format(propertyPattern.getPattern(), dbName), "");
        return property;
    }

    private String getJDBCDriver(Configuration configuration) {
        return configuration.getProperty(PropertyPattern.DRIVER.getPattern(), "");
    }

    protected DBConnectionPool getDBConnectionPool(String dbUrl, String userName, String password, String jdbcDriver) {
        return new DBConnectionPool(dbUrl, userName, password, jdbcDriver);
    }
}
