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
import java.sql.SQLException;

/**
 * @deprecated As of release 1802, replaced by {@link #(AppcDatabaseConnectionPool)}
 * <p>
 * This class provides the ability to create dbconnection by using DBUtils which
 * has been depreacted.
 */
@Deprecated
public class AppcJdbcConnectionFactory implements JdbcConnectionFactory {

    private String schema;

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public Connection openDbConnection() {
        try {
            return DBUtils.getConnection(schema);
        } catch (SQLException e) {
            throw new JdbcRuntimeException(Messages.EXP_APPC_JDBC_CONNECT.format(schema), e);
        }
    }

    public void closeDbConnection(Connection connection) {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new JdbcRuntimeException(Messages.EXP_APPC_JDBC_DISCONNECT.format(schema), e);
        }
    }
}
