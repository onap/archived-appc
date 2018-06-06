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

package org.onap.appc.dao.util.api;

import java.sql.Connection;

/**
 * @deprecated As of release 1802, replaced by {@link #(org.onap.appc.dao.util.api.DBConnectionPoolService)}
 * <p>
 * This interface has been deprecated due to a connection pool has
 * been introduced into this bundle.
 * refer to {@link DBConnectionPoolService}
 */
@Deprecated
public interface JdbcConnectionFactory {
    /**
     * Open a jdbc connection
     *
     * @return {@link Connection}
     */
    Connection openDbConnection();

    /**
     * Close a jdbc connection
     *
     * @param {@link Connection}
     */
    void closeDbConnection(Connection connection);
}
