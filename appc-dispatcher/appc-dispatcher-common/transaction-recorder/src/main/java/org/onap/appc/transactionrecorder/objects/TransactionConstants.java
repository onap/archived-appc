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

package org.onap.appc.transactionrecorder.objects;

public class TransactionConstants {

    public static final String INSERT_INTO = "INSERT INTO ";
    public static final String TRANSACTIONS = "TRANSACTIONS";
    public static final String COMMA= " , ";
    public static final String WHERE = " WHERE ";
    public static final String IS_NULL= " IS NULL ";
    public static final String AND=" AND ";
    public static final String ERROR_ACCESSING_DATABASE = "Error Accessing Database ";

    public enum TRANSACTION_ATTRIBUTES {

        TRANSACTION_ID("TRANSACTION_ID"),
        ORIGIN_TIMESTAMP("ORIGIN_TIMESTAMP"),
        REQUEST_ID("REQUEST_ID"),
        SUBREQUEST_ID("SUBREQUEST_ID"),
        ORIGINATOR_ID("ORIGINATOR_ID"),
        START_TIME("START_TIME"),
        END_TIME("END_TIME"),
        TARGET_ID("TARGET_ID"),
        TARGET_TYPE("TARGET_TYPE"),
        OPERATION("OPERATION"),
        RESULT_CODE("RESULT_CODE"),
        DESCRIPTION("DESCRIPTION"),
        STATE("STATE"),
        SERVICE_INSTANCE_ID("SERVICE_INSTANCE_ID"),
        VNFC_NAME("VNFC_NAME"),
        VSERVER_ID("VSERVER_ID"),
        VF_MODULE_ID("VF_MODULE_ID"),
        MODE("MODE");

        private String columnName;
        TRANSACTION_ATTRIBUTES(String columnName){
            this.columnName=columnName;
        }

        public String getColumnName(){
            return columnName;
        }
    }

}
