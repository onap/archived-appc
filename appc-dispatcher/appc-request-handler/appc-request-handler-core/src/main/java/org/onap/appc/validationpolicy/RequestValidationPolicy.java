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

package org.onap.appc.validationpolicy;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.onap.appc.domainmodel.lcm.VNFOperation;
import org.onap.appc.validationpolicy.executors.ActionInProgressRuleExecutor;
import org.onap.appc.validationpolicy.executors.RuleExecutor;
import org.onap.appc.validationpolicy.objects.Policy;
import org.onap.appc.validationpolicy.objects.PolicyNames;
import org.onap.appc.validationpolicy.objects.Rule;
import org.onap.appc.validationpolicy.objects.ValidationJSON;
import org.onap.appc.validationpolicy.rules.RuleFactory;
import org.onap.ccsdk.sli.core.dblib.DbLibService;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Reads the request validation policy on start-up and provides
 *  accessors for rule executors
 */
public class RequestValidationPolicy {

    private DbLibService dbLibService;

    private RuleExecutor actionInProgressRuleExecutor;

    private final EELFLogger logger = EELFManager.getInstance().getLogger(RequestValidationPolicy.class);

    public void setDbLibService(DbLibService dbLibService) {
        this.dbLibService = dbLibService;
    }

    public void initialize(){
        try {
            String jsonContent = getPolicyJson();
            if (jsonContent == null) return;

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
            ValidationJSON validationJSON = objectMapper.readValue(jsonContent, ValidationJSON.class);
            List<Policy> policyList = validationJSON.getPolicies();
            policyList.stream()
                    .filter(policy -> PolicyNames.ActionInProgress.name().equals(policy.getPolicyName()))
                    .forEach(policy -> {
                Rule[] ruleDTOs = policy.getRules();
                Map<String, org.onap.appc.validationpolicy.rules.Rule> rules = new HashMap<>();
                for(Rule ruleDTO : ruleDTOs) {
                    String action = ruleDTO.getActionReceived();
                    String validationRule = ruleDTO.getValidationRule();
                    Set<VNFOperation> inclusionSet = null;
                    Set<VNFOperation> exclusionSet = null;
                    if (ruleDTO.getInclusionList() != null && !ruleDTO.getInclusionList().isEmpty()) {
                        inclusionSet = ruleDTO.getInclusionList().stream()
                                .map(VNFOperation::findByString).filter(operation -> operation!=null)
                                .collect(Collectors.toSet());
                    }
                    if (ruleDTO.getExclusionList() != null && !ruleDTO.getExclusionList().isEmpty()) {
                        exclusionSet = ruleDTO.getExclusionList().stream()
                                .map(VNFOperation::findByString).filter(operation -> operation!=null)
                                .collect(Collectors.toSet());
                    }
                    org.onap.appc.validationpolicy.rules.Rule rule = RuleFactory
                            .createRule(validationRule, inclusionSet, exclusionSet);
                    rules.put(action, rule);
                }
                actionInProgressRuleExecutor = new ActionInProgressRuleExecutor(Collections.unmodifiableMap(rules));
            });
        } catch (Exception e) {
            logger.error("Error reading request validation policies",e);
        }
    }

    protected String getPolicyJson() {
        String schema = "sdnctl";
        String query = "SELECT MAX(INTERNAL_VERSION),ARTIFACT_CONTENT " +
                       "FROM ASDC_ARTIFACTS " +
                       "WHERE ARTIFACT_NAME = ? " +
                       "GROUP BY ARTIFACT_NAME";
        ArrayList<String> arguments = new ArrayList<>();
        arguments.add("request_validation_policy");
        String jsonContent =null;
        try{
            CachedRowSet rowSet = dbLibService.getData(query,arguments,schema);
            if(rowSet.next()){
                jsonContent = rowSet.getString("ARTIFACT_CONTENT");
            }
            if(logger.isDebugEnabled()){
                logger.debug("request validation policy = " + jsonContent);
            }
            if(StringUtils.isBlank(jsonContent)){
                logger.warn("request validation policy not found in app-c database");
            }
        }
        catch(SQLException e){
            logger.error("Error accessing database",e);
            throw new RuntimeException(e);
        }
        return jsonContent;
    }

    public RuleExecutor getInProgressRuleExecutor(){
        if(actionInProgressRuleExecutor ==null){
            throw new RuntimeException("Rule executor not available, initialization of RequestValidationPolicy failed");
        }
        return actionInProgressRuleExecutor;
    }
}
