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

package org.openecomp.appc.transactionrecorder.impl;

import org.openecomp.appc.dao.util.DBUtils;
import org.openecomp.appc.transactionrecorder.TransactionRecorder;
import org.openecomp.appc.transactionrecorder.objects.TransactionRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;



public class TransactionRecorderImpl implements TransactionRecorder {

    private static String APPCCTL_SCHEMA = "appcctl";

    private static final Logger logger = LoggerFactory.getLogger(TransactionRecorderImpl.class);

    /**
     * Stores transaction record to appc database by calling APPC Dao layer.
     * @param record Transaction record data.
     */
    @Override
    public void store(TransactionRecord record) {
        Connection connection = null;
        PreparedStatement stmt = null;
        String queryString = "INSERT INTO transactions VALUES (?,?,?,?,?,?,?,?,?,?)";
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Transaction Data started Inserting Successfully into DB");
            }
            connection = DBUtils.getConnection(APPCCTL_SCHEMA);
            stmt = connection.prepareStatement(queryString);
            stmt.setTimestamp(1, new java.sql.Timestamp(record.getTimeStamp().toEpochMilli()));
            stmt.setString(2, record.getRequestID());
            stmt.setTimestamp(3, new java.sql.Timestamp(record.getStartTime().toEpochMilli()));
            stmt.setTimestamp(4, new java.sql.Timestamp(record.getEndTime().toEpochMilli()));
            stmt.setString(5, record.getTargetID());
            stmt.setString(6, record.getTargetType());
            stmt.setString(7, record.getSubComponent());
            stmt.setString(8, record.getOperation());
            stmt.setString(9, record.getResultCode());
            stmt.setString(10, record.getDescription());
            stmt.execute();
            if (logger.isDebugEnabled()) {
                logger.debug("Transaction Data Inserted Successfully into DB");
            }
        } catch (SQLException e) {
            logger.error("Error Accessing Database " + e);
            throw new RuntimeException(e);
        } finally {
            DBUtils.clearResources(null, stmt, connection);
        }
    }
}
