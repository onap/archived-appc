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

package org.onap.appc.validationpolicy.executors;

import org.onap.appc.domainmodel.lcm.VNFOperation;
import org.onap.appc.validationpolicy.objects.RuleResult;

import java.util.List;

/**
 * Provides API for executing request validation rules defined by
 * request validation policy
 */
public interface RuleExecutor  {
    /**
     * Executes request validation rules defined in request validation policy
     * @param actionReceived current/new lcm request action
     * @param inProgressRequestActions list of in-progress lcm action for the same vnf
     * @return returns RuleResult (ACCEPT/REJECT) based on the rules
     */
    RuleResult executeRule(String actionReceived, List<VNFOperation> inProgressRequestActions);
}
