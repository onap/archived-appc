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

package org.onap.appc.transactionrecorder;


import org.onap.appc.domainmodel.lcm.RequestStatus;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.domainmodel.lcm.TransactionRecord;
import org.onap.appc.transactionrecorder.objects.TransactionConstants;

import java.util.List;
import java.util.Map;

/**
 * Interface to persist and query LCM requests
 */
public interface TransactionRecorder {
    /**
     * Stores transaction record to appc database by calling APPC Dao layer.
     * @param record Transaction record data.
     */
    void store(TransactionRecord record) throws APPCException;

    /**
     * This method is called when a particular row in transactions needs to be updated
     * @param key This is TransactionId which uniquely identifies the record.
     * @param updateColumns Map containing names of updated columns and their values.
     * @throws APPCException
     */
    void update(String key, Map<TransactionConstants.TRANSACTION_ATTRIBUTES, String> updateColumns) throws APPCException;

    /**
     * Marks all records in Transactions table in non-terminal state as ABORTED. This method is to be called during
     * APPC startup.
     *
     * @param appcInstanceId
     */
    void markTransactionsAborted(String appcInstanceId);

    /**
     * Fetch list of Transactions which are in non-terminal state i.e. ACCEPTED or RECEIVED for particular TargetId.
     * @param record Transactions object from which TargetId and StartTime is extracted to fetch list of in progress
     *               requests which APPC received before the current request.
     * @param interval Number of hours - Time window to consider requests as being in progress
     * @return List of Transactions in non terminal state.
     * @throws APPCException
     */
    List<TransactionRecord> getInProgressRequests(TransactionRecord record, int interval) throws APPCException;

    /**
     * Checks whether the incoming request is duplicate.
     * @param record Transaction object from which RequestId, SubRequestId, OriginatorId is extracted to check duplicate request.
     * @return
     * @throws APPCException
     */
    Boolean isTransactionDuplicate(TransactionRecord record) throws APPCException;

    /**
     * Retrieves {@link RequestStatus} from transaction table based on the passed parameters.
     * @param requestId: RequestId of the request to search (Required)
     * @param subrequestId: Sub-requestId (Optional)
     * @param originatorId: Originator Id who sent the request(Optional)
     * @param vnfId: VNFId to search (Required)
     * @return list of RequestStatus'es
     */
    List<RequestStatus> getRecords(String requestId, String subrequestId, String originatorId, String vnfId)
            throws APPCException;

    /**
     * Count of all requests which are currently in non-terminal state.
     * @return Count of all request in state RECEIVED and ACCEPTED.
     */
    Integer getInProgressRequestsCount() throws APPCException;

    /**
     *
     * @param appcInstanceId
     */
    void setAppcInstanceId(String appcInstanceId);
}
