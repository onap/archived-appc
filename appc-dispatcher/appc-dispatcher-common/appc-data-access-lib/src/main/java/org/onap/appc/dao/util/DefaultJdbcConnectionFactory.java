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

package org.onap.appc.dao.util;

import org.onap.appc.dao.util.api.JdbcConnectionFactory;
import org.onap.appc.dao.util.exception.JdbcRuntimeException;
import org.onap.appc.dao.util.message.Messages;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @deprecated As of release 1802, replaced by {@link #(org.onap.appc.dao.util.dbcp.DBConnectionPool)}
 * <p>
 * This class provides the ability to access mysql database which has been deprecated because
 * {@link #openDbConnection()} for each database request is not a good practice especially
 * on appc performance.
 * <p>
 * If you would like to use appcctl (mysql database), bundle:appc-data-access-lib has created
 * a database connection pool bean and exported as a service by using blueprint.
 * If you would like to create a new database connection pool, refer to the way mentioned above.
 * {@link org.onap.appc.dao.util.api.DBConnectionPoolService} has an example of how to use
 * the connection pool.
 */
@Deprecated
public abstract class DefaultJdbcConnectionFactory implements JdbcConnectionFactory {

    private static boolean driverRegistered = false;

    private String jdbcURL;
    private String jdbcUserName;
    private String jdbcPassword;

    public void setJdbcURL(String jdbcURL) {
        this.jdbcURL = jdbcURL;
    }

    public void setJdbcUserName(String jdbcUserName) {
        this.jdbcUserName = jdbcUserName;
    }

    public void setJdbcPassword(String jdbcPassword) {
        this.jdbcPassword = jdbcPassword;
    }


    protected abstract void registedDriver() throws SQLException;

    @Override
    public Connection openDbConnection() {
        try {
            if (!driverRegistered) {
                registedDriver();
                driverRegistered = true;
            }
            return DriverManager.getConnection(jdbcURL, jdbcUserName, jdbcPassword);
        } catch (SQLException e) {
            throw new JdbcRuntimeException(Messages.EXP_JDBC_CONNECT.format(jdbcURL), e);
        }
    }

    @Override
    public void closeDbConnection(Connection connection) {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new JdbcRuntimeException(Messages.EXP_JDBC_DISCONNECT.format(jdbcURL), e);
        }
    }
}
