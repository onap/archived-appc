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

package org.onap.appc.requesthandler.impl;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.json.JSONObject;
import org.onap.appc.domainmodel.lcm.ActionIdentifiers;
import org.onap.appc.domainmodel.lcm.RequestStatus;
import org.onap.appc.domainmodel.lcm.ResponseContext;
import org.onap.appc.domainmodel.lcm.RuntimeContext;
import org.onap.appc.domainmodel.lcm.Status;
import org.onap.appc.domainmodel.lcm.VNFOperation;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.executor.objects.LCMCommandStatus;
import org.onap.appc.executor.objects.Params;
import org.onap.appc.requesthandler.exceptions.MultipleRecordsRetrievedException;
import org.onap.appc.util.JsonUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class handles the LCM-Requests that don't need communication with the VNF.
 * The ideal flow is to validate the request, query locally and return data.
 */
public class LocalRequestHandlerImpl extends AbstractRequestHandlerImpl {
    private final EELFLogger logger = EELFManager.getInstance().getLogger(LocalRequestHandlerImpl.class);

    @Override
    protected void handleRequest(RuntimeContext runtimeContext) {
        final VNFOperation action = runtimeContext.getRequestContext().getAction();

        switch (action) {
            case ActionStatus:
                processActionStatus(runtimeContext);
                break;
            default:
                logger.error(String.format("Action: %s isn't mapped to a Local Request.", action));

        }
    }

    /**
     * Process request of ActionStatus.
     * The <code>status</code> in the <code>ResponseContext</code> will be set according to
     * the response from <code>getStatusOfRequest(String,String,String,String</code>
     * <p>
     * The MULTIPLE_REQUESTS_FOUND exception is caught when <code>getStatusOfRequest(String,String,String,String</code>
     * throws MultipleRecordsRetrievedException. Otherwise, all the responses returned from here will be SUCCESS.
     *
     * @param runtimeContext an RuntimeContext Object
     */
    private void processActionStatus(RuntimeContext runtimeContext) {
        // Get the input payload and Action Identifier to know the request to query
        Map<String, String> payloadAttributeMap = getPayloadAttribute(runtimeContext.getRequestContext().getPayload());
        String requestId = payloadAttributeMap.get("request-id");
        String vnfId = getVnfId(runtimeContext.getRequestContext().getActionIdentifiers());
        String originatorId = payloadAttributeMap.get("originator-id");
        String subRequestId = payloadAttributeMap.get("sub-request-id");

        ResponseContext context = runtimeContext.getResponseContext();
        RequestStatus requestStatus;
        String jsonPayload = null;
        Status status;

        // Use Transaction Recorder to query DB for this Request's status
        try {
            requestStatus = getStatusOfRequest(requestId, subRequestId, originatorId, vnfId);
            jsonPayload = createPayload(requestStatus.getExternalActionStatusName(), requestStatus.name());
            status = buildStatus(LCMCommandStatus.SUCCESS, null, null);
        } catch (MultipleRecordsRetrievedException ex) {
            status = buildStatus(LCMCommandStatus.MULTIPLE_REQUESTS_FOUND, "parameters",
                getSearchCriteria(requestId, subRequestId, originatorId, vnfId));
            logger.debug("RequestStatus is set to MULTIPLE_REQUESTS_FOUND due to MultipleRecordsRetrievedException",
                ex.getMessage());
        } catch (APPCException ex) {
            jsonPayload = createPayload(RequestStatus.UNKNOWN.getExternalActionStatusName(),
                RequestStatus.UNKNOWN.name());
            status = buildStatus(LCMCommandStatus.SUCCESS, null, null);
            logger.debug("RequestStatus is set to UNKNOWN due to APPCException:", ex.getMessage());
        }

        // Create ResponseContext with payload containing ActionStatus with status and status-reason
        context.setStatus(status);
        if (jsonPayload != null) {
            context.setPayload(jsonPayload);
        }
    }

    private String getSearchCriteria(String requestId, String subrequestId, String originatorid, String vnfId) {
        StringBuilder suffix = new StringBuilder();
        suffix.append(String.format("request-id=%s", requestId));
        suffix.append(String.format(" AND vnf-id=%s", vnfId));
        if (subrequestId != null) {
            suffix.append(String.format(" AND sub-request-id=%s", subrequestId));
        }
        if (originatorid != null) {
            suffix.append(String.format(" AND originator-id=%s", originatorid));
        }

        return suffix.toString();
    }

    private Map<String, String> getPayloadAttribute(String payload) {
        Map<String, String> map;
        try {
            map = JsonUtil.convertJsonStringToFlatMap(payload);
        } catch (IOException e) {
            logger.error(String.format("Error encountered when converting JSON payload '%s' to map", payload), e);
            throw new IllegalArgumentException("Search criteria cannot be determined from Payload");
        }

        if (map == null || !map.containsKey("request-id")) {
            throw new IllegalArgumentException("request-id is absent in the Payload");
        }

        return map;
    }

    private String getVnfId(ActionIdentifiers identifiers) {
        if (identifiers == null || identifiers.getVnfId() == null) {
            throw new IllegalArgumentException("vnf-id is absent in Action Identifiers");
        }
        return identifiers.getVnfId();
    }

    /**
     * Build a Status.
     *
     * @param lcmCommandStatus for the Status code and message format
     * @param key              String for the LCMcommandStatus format
     * @param message          String for the Status message vaiable
     * @return the newly build Status
     */
    private Status buildStatus(LCMCommandStatus lcmCommandStatus, String key, String message) {
        Status status = new Status();
        status.setCode(lcmCommandStatus.getResponseCode());

        if (key != null) {
            Params params = new Params().addParam(key, message);
            status.setMessage(lcmCommandStatus.getFormattedMessage(params));
        } else {
            status.setMessage(lcmCommandStatus.getResponseMessage());
        }

        return status;
    }

    private String createPayload(String status, String statusReason) {
        Map<String, String> payload = new HashMap<>();
        payload.put("status", status);
        payload.put("status-reason", statusReason);
        return (new JSONObject(payload)).toString();
    }

    private RequestStatus getStatusOfRequest(String requestId, String subRequestId, String originatorId, String vnfId)
        throws MultipleRecordsRetrievedException, APPCException {
        RequestStatus requestStatus = RequestStatus.UNKNOWN;

        List<RequestStatus> records = transactionRecorder.getRecords(requestId, subRequestId, originatorId, vnfId);
        if (records != null) {
            final int size = records.size();
            if (size > 1) {
                throw new MultipleRecordsRetrievedException(
                    String.format("MULTIPLE REQUESTS FOUND USING SEARCH CRITERIA: %s", getSearchCriteria(requestId,
                        subRequestId, originatorId, vnfId)));
            }

            if (size == 0) {
                requestStatus = RequestStatus.NOT_FOUND;
            } else {
                requestStatus = records.get(0);
            }
        }

        return requestStatus;
    }

    @Override
    public void onRequestExecutionStart(String vnf_id, boolean readOnlyActivity, boolean forceFlag) {
        //Do nothing
    }
}
