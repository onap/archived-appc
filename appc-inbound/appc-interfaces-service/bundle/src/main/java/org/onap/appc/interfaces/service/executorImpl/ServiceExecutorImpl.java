/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2019 Ericsson
 * ================================================================================
 * Modifications Copyright (C) 2019 IBM
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
 * 
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.interfaces.service.executorImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onap.appc.aai.client.aai.AaiService;
import org.onap.appc.interfaces.service.data.Request;
import org.onap.appc.interfaces.service.data.ScopeOverlap;
import org.onap.ccsdk.sli.adaptors.aai.AAIClient;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;

public class ServiceExecutorImpl {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(ServiceExecutorImpl.class);
    private AAIClient aaiClient;

    public String isRequestOverLap(String requestData) throws Exception {
        String response = "\"requestOverlap\"  : ";
        log.info("Response from ServiceExecutorImpl");
        ObjectMapper mapper = getObjectMapper();
        ScopeOverlap scopeOverlap = mapper.readValue(requestData, ScopeOverlap.class);
        // return response + String.valueOf(checkForOverLap(scopeOverlap));
        boolean isOverlap = checkForOverLap(scopeOverlap);
        scopeOverlap.setOverlap(String.valueOf(isOverlap));
        if (scopeOverlap.getOverlap() != null && scopeOverlap.getOverlap().equalsIgnoreCase("false")){
            log.info(response + "false");
            return response + "false";
        }
        else{
            log.info(response + "true");
            return response + "true";
        }
    }

    private boolean checkForOverLap(ScopeOverlap scopeOverlap) throws Exception {
        log.info("Checking for isScopeOverlap");
        if (scopeOverlap.getInProgressRequest() == null) {
            return Boolean.FALSE;
        }else if ( scopeOverlap.getInProgressRequest().isEmpty()){
            return Boolean.FALSE;
        }
        if (scopeOverlap.getCurrentRequest().getActionIdentifiers().getVnfId() != null) {
            return Boolean.TRUE;
        } else if (!Strings.isNullOrEmpty(scopeOverlap.getVnfId())
                && scopeOverlap.getInProgressRequest().size() > 0) {
            log.info("Checking overlap for similar vnfid :" + isVnfIdOverlap(scopeOverlap));
            return isVnfIdOverlap(scopeOverlap);
        } else if (scopeOverlap.getCurrentRequest().getActionIdentifiers().getVfModuleId() != null) {
            return Boolean.TRUE;
        } else if (scopeOverlap.getCurrentRequest().getActionIdentifiers().getvServerId() != null) {
            return isVserverOrVnfcIdOverLap(scopeOverlap);
        } else if (scopeOverlap.getCurrentRequest().getActionIdentifiers().getVnfcName() != null) {
                return isVserverOrVnfcIdOverLap(scopeOverlap);
        } else {
            throw new Exception(" Action Identifier doesn't have VnfId, VfModuleId, VServerId, VnfcName ");
        }
    }

    private boolean isVnfcNameOverLap(ScopeOverlap scopeOverlap) throws Exception {

        AaiService aaiService = getAaiService(aaiClient);
        SvcLogicContext ctx = getSvcLogicContext();
        Map<String, String> params = new HashMap<>();
        List<String> inProgressVServerIds = new ArrayList<>();
        String currentVnfcVserverId = new String();
        String currentRequestVnfcName = scopeOverlap.getCurrentRequest().getActionIdentifiers().getVnfcName();
        String currentRequestVServerId = scopeOverlap.getCurrentRequest().getActionIdentifiers().getvServerId();
        List<Request> inProgressRequests = scopeOverlap.getInProgressRequest();
        params.put("vnfId", scopeOverlap.getVnfId());
        try {
            aaiService.getGenericVnfInfo(params, ctx);
            int vmCount = Integer.parseInt(ctx.getAttribute("vm-count"));
                for(Request inprogressRequest:inProgressRequests){
                    if(inprogressRequest.getActionIdentifiers().getVnfcName() != null){
                        for (int i = 0; i < vmCount; i++) {
                            if (ctx.getAttribute("vm[" + i + "].vnfc-name") != null && ctx.getAttribute("vm[" + i + "].vnfc-name")
                                    .equals(inprogressRequest.getActionIdentifiers().getVnfcName())) {
                                String newInProgressVserverId = ctx.getAttribute("vm[" + i + "].vserver-id");
                                inProgressVServerIds.add(newInProgressVserverId);
                                log.debug("Received vserver-id from AAI: " + newInProgressVserverId);
                            }
                        }
                    }
            }
            for(Request inProgVserverIds:inProgressRequests)
                if(inProgVserverIds.getActionIdentifiers().getvServerId() != null)
                    inProgressVServerIds.add(inProgVserverIds.getActionIdentifiers().getvServerId());
            if(currentRequestVnfcName != null){
                for (int i = 0; i < vmCount; i++) {
                    if (ctx.getAttribute("vm[" + i + "].vnfc-name") != null && ctx.getAttribute("vm[" + i + "].vnfc-name")
                            .equals(currentRequestVnfcName)) {
                        currentVnfcVserverId = ctx.getAttribute("vm[" + i + "].vserver-id");
                        log.debug("Received vserver-id from AAI: " + currentVnfcVserverId);
                        return inProgressVServerIds.contains(currentVnfcVserverId);
                    }
                }
            }
            for (Request request : inProgressRequests) {
                if(!Strings.isNullOrEmpty(currentRequestVServerId)  && currentRequestVServerId.equalsIgnoreCase(request.getActionIdentifiers().getvServerId()))
                    return Boolean.TRUE;
            }
            if(currentRequestVServerId != null)    {
                return  inProgressVServerIds.contains(currentRequestVServerId);
            }
            return Boolean.FALSE;
        } catch (Exception e) {
            log.debug(e.getMessage());
            throw e;
        }
    }

    private boolean isVserverOrVnfcIdOverLap(ScopeOverlap scopeOverlap) throws Exception {
        List<Request> inProgressRequests = scopeOverlap.getInProgressRequest();
        for (Request request : inProgressRequests) {
            if(request.getActionIdentifiers().getVnfId() != null)
            return Boolean.TRUE ;
            }
        for (Request request : inProgressRequests) {
            if(request.getActionIdentifiers().getVfModuleId() != null)
                return Boolean.TRUE ;
        }
        String currentVserverID = scopeOverlap.getCurrentRequest().getActionIdentifiers().getvServerId();
        for (Request request : inProgressRequests) {
            if(currentVserverID != null && currentVserverID.equalsIgnoreCase(request.getActionIdentifiers().getvServerId()))
                return Boolean.TRUE ;
        }
        return isVnfcNameOverLap(scopeOverlap);
    }

    private boolean isVnfIdOverlap(ScopeOverlap scopeOverlap) throws Exception {
        List<Request> inProgressRequests = scopeOverlap.getInProgressRequest();
        log.info("inProgressRequests list" + inProgressRequests.toString());
        for (Request request : inProgressRequests) {
            log.info("request list" + request.getTargetId());
            if (!Strings.isNullOrEmpty(scopeOverlap.getVnfId())
                    && !Strings.isNullOrEmpty(request.getTargetId())
                    && (request.getTargetId()
                            .equals(scopeOverlap.getVnfId())))
                return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    protected ObjectMapper getObjectMapper() {
        return new ObjectMapper();
    }

    protected AaiService getAaiService(AAIClient aaiClient) {
        return new AaiService(aaiClient);
    }

    protected SvcLogicContext getSvcLogicContext() {
        return new SvcLogicContext();
    }
}
