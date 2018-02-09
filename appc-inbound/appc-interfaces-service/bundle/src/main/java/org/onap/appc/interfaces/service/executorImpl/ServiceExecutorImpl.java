/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.interfaces.service.executorImpl;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.onap.appc.aai.client.aai.AaiService;
import org.onap.appc.interfaces.service.data.Request;
import org.onap.appc.interfaces.service.data.ScopeOverlap;
import org.onap.appc.interfaces.service.executor.ExecutorException;
import org.onap.ccsdk.sli.adaptors.aai.AAIClient;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

public class ServiceExecutorImpl {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(ServiceExecutorImpl.class);
    private static final String VNFC_NAME_STR = "].vnfc-name";
    private static final String FALSE = "false";
    private AAIClient aaiClient;

    public String isRequestOverLap(String requestData) throws Exception {
        String response = "\"requestOverlap\"  : ";
        log.info("Response from ServiceExecutorImpl");
        ScopeOverlap scopeOverlap;
        ObjectMapper mapper = new ObjectMapper();
        scopeOverlap = mapper.readValue(requestData, ScopeOverlap.class);
        boolean isOverlap = checkForOverLap(scopeOverlap);
        scopeOverlap.setOverlap(String.valueOf(isOverlap));
        if (scopeOverlap.getOverlap() != null && scopeOverlap.getOverlap().equalsIgnoreCase(FALSE)) {
            log.info(response + FALSE);
            return response + FALSE;
        } else {
            log.info(response + "true");
            return response + "true";
        }
    }

    private boolean checkForOverLap(ScopeOverlap scopeOverlap) throws Exception {
        log.info("Checking for isScopeOverlap");
        if (inProgressRequestIsAbsent(scopeOverlap)) {
            return false;
        } else if (hasVnfOrVfModuleId(scopeOverlap)) {
            return true;
        } else if (hasServerIdOrVnfName(scopeOverlap)) {
            return isVserverOrVnfcIdOverLap(scopeOverlap);
        } else {
            throw new ExecutorException(" Action Identifier doesn't have VnfId, VfModuleId, VServerId, VnfcName ");
        }
    }

    private boolean hasServerIdOrVnfName(ScopeOverlap scopeOverlap) {
        return scopeOverlap.getCurrentRequest().getActionIdentifiers().getvServerId() != null
            || scopeOverlap.getCurrentRequest().getActionIdentifiers().getVnfcName() != null;
    }

    private boolean hasVnfOrVfModuleId(ScopeOverlap scopeOverlap) {
        return scopeOverlap.getCurrentRequest().getActionIdentifiers().getVnfId() != null
            || scopeOverlap.getCurrentRequest().getActionIdentifiers().getVfModuleId() != null;
    }

    private boolean inProgressRequestIsAbsent(ScopeOverlap scopeOverlap) {
        return scopeOverlap.getInProgressRequest() == null || scopeOverlap.getInProgressRequest().isEmpty();
    }

    private boolean isVnfcNameOverLap(ScopeOverlap scopeOverlap) throws Exception {

        AaiService aaiService = new AaiService(aaiClient);
        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> params = new HashMap<>();
        List<String> inProgressVServerIds = new ArrayList<>();
        String currentVnfcVserverId = "";
        String currentRequestVnfcName = scopeOverlap.getCurrentRequest().getActionIdentifiers().getVnfcName();
        String currentRequestVServerId = scopeOverlap.getCurrentRequest().getActionIdentifiers().getvServerId();
        List<Request> inProgressRequests = scopeOverlap.getInProgressRequest();
        params.put("vnfId", scopeOverlap.getVnfId());
        try {
            aaiService.getGenericVnfInfo(params, ctx);
            int vmCount = Integer.parseInt(ctx.getAttribute("vm-count"));
            fillInProgressVServerIds(ctx, inProgressVServerIds, inProgressRequests, vmCount);
            fillInProgressVServerIds(inProgressVServerIds, inProgressRequests);
            if (currentRequestVnfcName != null) {
                currentVnfcVserverId = updateVnfcVServerId(ctx, currentVnfcVserverId, currentRequestVnfcName, vmCount);
                log.debug("Received vserver-id from AAI: " + currentVnfcVserverId);
                return inProgressVServerIds.contains(currentVnfcVserverId);
            }
            for (Request request : inProgressRequests) {
                if (isValidRequest(currentRequestVServerId, request)) {
                    return true;
                }
            }
            return currentRequestVServerId != null && inProgressVServerIds.contains(currentRequestVServerId);
        } catch (Exception e) {
            log.debug(e.getMessage());
            throw e;
        }
    }

    private void fillInProgressVServerIds(List<String> inProgressVServerIds, List<Request> inProgressRequests) {
        for (Request inProgVserverIds : inProgressRequests) {
            tryAddVServerId(inProgressVServerIds, inProgVserverIds);
        }
    }

    private void fillInProgressVServerIds(SvcLogicContext ctx, List<String> inProgressVServerIds,
        List<Request> inProgressRequests, int vmCount) {
        for (Request inprogressRequest : inProgressRequests) {
            tryAddVServerId(ctx, inProgressVServerIds, vmCount, inprogressRequest);
        }
    }

    private boolean isValidRequest(String currentRequestVServerId, Request request) {
        return !Strings.isNullOrEmpty(currentRequestVServerId)
            && currentRequestVServerId.equalsIgnoreCase(request.getActionIdentifiers().getvServerId());
    }

    private void tryAddVServerId(List<String> inProgressVServerIds, Request inProgVserverIds) {
        if (inProgVserverIds.getActionIdentifiers().getvServerId() != null) {
            inProgressVServerIds.add(inProgVserverIds.getActionIdentifiers().getvServerId());
        }
    }

    private void tryAddVServerId(SvcLogicContext ctx, List<String> inProgressVServerIds, int vmCount,
        Request inprogressRequest) {
        if (inprogressRequest.getActionIdentifiers().getVnfcName() != null) {
            for (int i = 0; i < vmCount; i++) {
                if (ctx.getAttribute("vm[" + i + VNFC_NAME_STR) != null && ctx
                    .getAttribute("vm[" + i + VNFC_NAME_STR)
                    .equals(inprogressRequest.getActionIdentifiers().getVnfcName())) {
                    inProgressVServerIds.add(ctx.getAttribute("vm[" + i + "].vserver-id"));
                }
                log.debug("Received vserver-id from AAI: " + inProgressVServerIds);
            }
        }
    }

    private String updateVnfcVServerId(SvcLogicContext ctx, String currentVnfcVserverId, String currentRequestVnfcName,
        int vmCount) {
        for (int i = 0; i < vmCount; i++) {
            if (ctx.getAttribute("vm[" + i + VNFC_NAME_STR) != null &&
                ctx.getAttribute("vm[" + i + VNFC_NAME_STR).equals(currentRequestVnfcName)) {
                return ctx.getAttribute("vm[" + i + "].vserver-id");
            }
        }
        return currentVnfcVserverId;
    }

    private boolean isVserverOrVnfcIdOverLap(ScopeOverlap scopeOverlap) throws Exception {
        List<Request> inProgressRequests = scopeOverlap.getInProgressRequest();
        for (Request request : inProgressRequests) {
            if (request.getActionIdentifiers().getVnfId() != null) {
                return true;
            }
        }
        for (Request request : inProgressRequests) {
            if (request.getActionIdentifiers().getVfModuleId() != null) {
                return true;
            }
        }
        String currentVserverID = scopeOverlap.getCurrentRequest().getActionIdentifiers().getvServerId();
        for (Request request : inProgressRequests) {
            if (validateVServerId(currentVserverID, request)) {
                return true;
            }
        }
        return isVnfcNameOverLap(scopeOverlap);
    }

    private boolean validateVServerId(String currentVserverID, Request request) {
        return
            currentVserverID != null && currentVserverID.equalsIgnoreCase(request.getActionIdentifiers().getvServerId());
    }
}
