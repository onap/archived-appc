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

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.onap.appc.configuration.Configuration;
import org.onap.appc.configuration.ConfigurationFactory;
import java.sql.*;

/**
 * @deprecated As of release 1802, replaced by {@link #(org.onap.appc.dao.util.dbcp.DBConnectionPool)}
 * <p>
 * This class provides the ability to access mysql database which has been @Deprecated because
 * {@link #getConnection(String)} for each database request is not a good practice especially
 * on appc performance.
 * <p>
 * If you would like to use appcctl (mysql database), bundle:appc-data-access-lib has created
 * a database connection pool bean and exported as a service by using blueprint.
 * If you would like to create a new database connection pool, refer to the way mentioned above.
 * {@link org.onap.appc.dao.util.api.DBConnectionPoolService} has an example of how to use
 * the connection pool.
 */
@Deprecated
public class DBUtils {
    private static final EELFLogger logger = EELFManager.getInstance().getLogger(DBUtils.class);
    private static final String JDBC_DRIVER = "org.mariadb.jdbc.Driver";
    private static final Configuration configuration = ConfigurationFactory.getConfiguration();

    static {
        try {
            String driver = JDBC_DRIVER;
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage());
        }
    }

    public static Connection getConnection(String schema) throws SQLException {
        DriverManager.registerDriver(new org.mariadb.jdbc.Driver());
        String dbURL = configuration.getProperty(String.format("org.onap.appc.db.url.%s", schema), "");
        String userName = configuration.getProperty(String.format("org.onap.appc.db.user.%s", schema), "");
        String password = configuration.getProperty(String.format("org.onap.appc.db.pass.%s", schema), "");
        return DriverManager.getConnection(dbURL, userName, password);
    }

    public static boolean clearResources(ResultSet resultSet, PreparedStatement ptmt, Connection connection) {
        boolean clearFlag = false;
        try {
            if (resultSet != null)
                resultSet.close();
            if (ptmt != null)
                ptmt.close();
            if (connection != null)
                connection.close();
            clearFlag = true;
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return clearFlag;

    }
}
