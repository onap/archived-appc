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

package org.openecomp.appc.dao.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
            if(!driverRegistered) {
                registedDriver();
                driverRegistered = true;
            }
            return DriverManager.getConnection(jdbcURL, jdbcUserName, jdbcPassword);
        } catch(SQLException e) {
            throw new JdbcRuntimeException(Messages.EXP_JDBC_CONNECT.format(jdbcURL), e);
        }
    }

    @Override
    public void closeDbConnection(Connection connection) {
        try {
            connection.close();
        } catch(SQLException e) {
            throw new JdbcRuntimeException(Messages.EXP_JDBC_DISCONNECT.format(jdbcURL), e);
        }
    }
}
