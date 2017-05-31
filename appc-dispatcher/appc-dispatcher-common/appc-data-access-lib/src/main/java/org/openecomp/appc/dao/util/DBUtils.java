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

import java.sql.*;

import org.openecomp.appc.configuration.Configuration;
import org.openecomp.appc.configuration.ConfigurationFactory;

@Deprecated
public class DBUtils {
	private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	private static final Configuration configuration = ConfigurationFactory.getConfiguration();
	static {
		try {
			String driver = JDBC_DRIVER;
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static Connection getConnection(String schema) throws SQLException {
		DriverManager.registerDriver(new com.mysql.jdbc.Driver());
		String dbURL = configuration.getProperty(String.format("org.openecomp.appc.db.url.%s", schema), "");
		String userName = configuration.getProperty(String.format("org.openecomp.appc.db.user.%s", schema), "");
		String password = configuration.getProperty(String.format("org.openecomp.appc.db.pass.%s", schema), "");
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

		}
		return clearFlag;

	}
}
