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

package org.onap.appc.dao.util.api;

import org.onap.appc.dao.util.exception.DBConnectionPoolException;

import java.sql.Connection;
import java.util.Map;

/**
 * This class is the interface of DBConnectionPool.
 * <p>
 * Below is an example of how to query an entry from database.
 * Inject AppcDatabaseConnectionPool bean by the blueprint first
 * for example,
 * {@code
 * <reference id="AppcMysqlDBConnectionPoolService" availability="mandatory"
 * activation="eager" interface="org.onap.appc.dao.util.api.DBConnectionPoolService" />
 * }
 * <p>
 * Then, query the data and close ResultSet, Statement, Connection.
 * <blockquote><pre>
 * {@code
 * private AppcDatabaseConnectionPool pool;
 * public void setAppcDatabaseConnectionPool(AppcDatabaseConnectionPool pool){
 *     this.pool = pool;
 * }
 * public queryAppcDatabase(AppcDatabaseConnectionPool pool){
 *      Connection connection = null;
 *      try {
 *          connection = pool.getConnection();
 *      } catch (DBConnectionPoolException e) {
 *          e.printStackTrace();
 *      }
 *      Connection conn = null;
 *      Statement stmt = null;
 *      ResultSet rs = null;
 *      try {
 *            stmt = connection.createStatement();
 *            rs = stmt.executeQuery("select * from appcctl.transactions");
 *            System.out.println("# of entries in db:");
 *            int numcols = rs.getMetaData().getColumnCount();
 *            System.out.println(pool.getDataSourceStatus());
 *      }catch (SQLException e) {
 *          e.printStackTrace();
 *      } finally {
 *          try {
 *               pool.close(rs, stmt, conn);
 *              } catch (DataAccessException e) {
 *                  e.printStackTrace();
 *              }
 *     }
 * }
 * }
 * <p>
 * </pre></blockquote>
 */
public interface DBConnectionPoolService {
    /**
     * Get a jdbc connection
     *
     * @return connection {@link Connection}
     * @throws DBConnectionPoolException - if a {@link Connection} cannot be return.
     */
    Connection getConnection() throws DBConnectionPoolException;

    /**
     * Get Data source status
     *
     * @return map
     */
    Map<String, Integer> getDataSourceStatus();

}
