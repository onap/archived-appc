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

package org.onap.appc.dao.util.dbcp;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.apache.commons.dbcp2.BasicDataSource;
import org.onap.appc.dao.util.api.DBConnectionPoolService;
import org.onap.appc.dao.util.exception.DBConnectionPoolException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class provides ability to create database connection pool.
 */
public class DBConnectionPool implements DBConnectionPoolService {

    private final EELFLogger logger = EELFManager.getInstance().getLogger(DBConnectionPool.class);
    private BasicDataSource dataSource;

    public enum DataSourceStatus {
        ACTIVE_NUMBER("active_number"),
        IDLE_NUMBER("idle_number");

        private String attribute;

        DataSourceStatus(String attribute) {
            this.attribute = attribute;
        }

        public String getAttribute() {
            return attribute;
        }
    }

    public DBConnectionPool(String connectURI, String username, String password, String driverClass) {
        this(connectURI, username, password, driverClass, null, null, null, null, null);
    }

    public DBConnectionPool(String connectURI, String username, String password,
                            String driverClass, Integer initialSize, Integer maxActive,
                            Integer maxIdle, Integer maxWait, Integer minIdle) {
        this.dataSource = getBasicDataSource(connectURI, username, password, driverClass,
            initialSize, maxActive, maxIdle, maxWait, minIdle);
    }

    /**
     * Get a connection from datasource which is thread safe.
     * {@inheritDoc}
     */
    @Override
    public Connection getConnection() throws DBConnectionPoolException {
        if (dataSource == null) {
            throw new DBConnectionPoolException();
        }
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
        } catch (SQLException e) {
            logger.error("Get connection failure", e);
            throw new DBConnectionPoolException(e);
        }

        if(connection == null){
            //
            throw new DBConnectionPoolException("Connection was not created");
        }

        return connection;
    }

    /**
     * Closes and releases all idle connections that are currently stored in the connection pool associated with this
     * data source.
     */
    public void shutdown() {
        if (dataSource != null) {
            try {
                dataSource.close();
            } catch (SQLException e) {
                logger.error("Datasource cannot be closed normally.", e.getMessage());
            }
        }

        dataSource = null;
    }

    /**
     * Get datasource status
     *
     * @return
     */
    public Map<String, Integer> getDataSourceStatus() {
        Map<String, Integer> map = new HashMap<>(2);
        map.put(DataSourceStatus.ACTIVE_NUMBER.getAttribute(), dataSource.getNumActive());
        map.put(DataSourceStatus.IDLE_NUMBER.getAttribute(), dataSource.getNumIdle());

        return map;
    }

    protected BasicDataSource getBasicDataSource(String connectURI, String username, String password,
                                               String driverClass, Integer initialSize, Integer maxtotal,
                                               Integer maxIdle, Integer maxWaitMillis, Integer minIdle) {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(driverClass);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setUrl(connectURI);

        if (initialSize != null) {
            dataSource.setInitialSize(initialSize);
        }
        if (maxtotal != null) {
            dataSource.setMaxTotal(maxtotal);
        }
        if (maxIdle != null) {
            dataSource.setMaxIdle(maxIdle);
        }
        if (maxWaitMillis != null) {
            dataSource.setMaxWaitMillis(maxWaitMillis);
        }
        if (minIdle != null) {
            dataSource.setMinIdle(minIdle);
        }

        return dataSource;
    }
}
