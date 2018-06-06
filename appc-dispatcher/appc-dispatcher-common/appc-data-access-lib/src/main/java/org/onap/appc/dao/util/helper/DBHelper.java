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

package org.onap.appc.dao.util.helper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This class provides the basic utility methods of database connection
 * <p>
 * Since currently this class only contains stateless methods, the package
 * is exported by maven-bundle-plugin. If you have to add some stateful methods
 * in this class, one suggested solution is that use blueprint to create a singleton
 * which is exported as service
 */
public class DBHelper {
    /**
     * Closes a database resultSet,statement and connection in that order. Data access objects should call this method
     * in a finally block.
     *
     * @param resultSet  or null
     * @param statement  or null
     * @param connection or null
     */
    public static void close(ResultSet resultSet, Statement statement, Connection connection) {
        try {
            closeResultSet(resultSet);
        } finally {
            try {
                closeStatement(statement);
            } finally {
                closeConnection(connection);
            }
        }
    }

    /**
     * Closes a database result set. Data access objects should call this method
     * when a result set is no longer needed.
     *
     * @param rs A ResultSet Object
     */
    public static void closeResultSet(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException se) {
            // Ignore this exception and allow execution to continue.
            // so that connection can try to be close.
        }
    }

    /**
     * Closes a database query statement. Data access objects should call this
     * method when a statement is no longer needed.
     *
     * @param stmt A Statement Object
     */
    public static void closeStatement(Statement stmt) {
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException se) {
            // Ignore this exception and allow execution to continue.
            // so that connection can try to be close.
        }
    }

    /**
     * Closes a database connection. Data access objects should call this method
     * when a database connection is no longer needed.
     *
     * @param connection A Connection Object     *
     */
    public static void closeConnection(Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException se) {
            // Ignore this exception and allow execution to continue.
        }
    }
}
