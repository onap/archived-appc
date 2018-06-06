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

package org.onap.appc.dao.util.exception;

import java.sql.SQLException;

/**
 * This class is exception class for DBConnectionPool{@link org.onap.appc.dao.util.dbcp.DBConnectionPool}
 */
public class DBConnectionPoolException extends SQLException {
    public DBConnectionPoolException() {
        super();
    }

    public DBConnectionPoolException(String message) {
        super(message);
    }

    public DBConnectionPoolException(String message, Throwable cause) {
        super(message, cause);
    }

    public DBConnectionPoolException(Throwable cause) {
        super(cause);
    }
}
