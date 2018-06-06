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

package org.onap.appc.validationpolicy.rules;

import org.onap.appc.domainmodel.lcm.VNFOperation;
import org.onap.appc.validationpolicy.objects.RuleResult;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of Reject Rule
 */
public class RejectRule extends Rule {

    RejectRule(Set<VNFOperation> inclusions, Set<VNFOperation> exclusions){
        super(inclusions, exclusions);
    }

    @Override
    public RuleResult executeRule(String action, List<VNFOperation> inProgressActionList) {
        if(inclusions!= null && !inclusions.isEmpty()){
            /* new action (action-received) should be rejected,
               if any of the in-progress request actions
               are mentioned in the in-progress-inclusion list*/
            if(logger.isDebugEnabled()){
                logger.debug("Executing reject rule for action-received = " + action+ " and in-progress-actions-inclusions =" + inProgressActionList.toString());
            }
            Set<String> inProgressActionSet = inProgressActionList.stream().map(VNFOperation::name).collect(Collectors.toSet());
            inProgressActionSet.retainAll(inclusions.stream().map(VNFOperation::name).collect(Collectors.toList()));
            if(!inProgressActionSet.isEmpty()){
                return RuleResult.REJECT;
            }
            return RuleResult.ACCEPT;
        }
        else if(exclusions != null && !exclusions.isEmpty()){
            /* new action (action-received) should be accepted,
               if all of the in-progress request actions
               are mentioned in the in-progress-exclusion list*/
            if(logger.isDebugEnabled()){
                logger.debug("Executing reject rule for action-received = " + action + " and in-progress-actions-inclusions = " + inProgressActionList.toString());
            }
            Set<String> inProgressActionSet = inProgressActionList.stream().map(VNFOperation::name).collect(Collectors.toSet());
            inProgressActionSet.removeAll(exclusions.stream().map(VNFOperation::name).collect(Collectors.toList()));
            if(inProgressActionSet.isEmpty()){
                return RuleResult.ACCEPT;
            }
            return RuleResult.REJECT;
        }
        return RuleResult.REJECT;
    }
}
