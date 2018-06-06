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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.onap.appc.domainmodel.lcm.VNFOperation;
import org.onap.appc.validationpolicy.executors.RuleExecutor;
import org.onap.appc.validationpolicy.objects.RuleResult;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestRuleExecutor {

    RuleExecutor ruleExecutor;

    @Before
    public void setup() throws IOException, URISyntaxException {
        RequestValidationPolicy requestValidationPolicy = new MockRequestValidationPolicy();
        requestValidationPolicy.initialize();
        ruleExecutor = requestValidationPolicy.getInProgressRuleExecutor();
    }

    @Test
    public void testAcceptRule(){
        RuleResult result;
        result = ruleExecutor.executeRule("Sync", Stream.of("HealthCheck", "Stop","Start")
                .map(s -> VNFOperation.findByString(s)).collect(Collectors.toList()));
        Assert.assertEquals(result,RuleResult.REJECT);

        result = ruleExecutor.executeRule("Sync", Stream.of("Start", "Stop","Restart")
                .map(s -> VNFOperation.findByString(s)).collect(Collectors.toList()));
        Assert.assertEquals(result,RuleResult.ACCEPT);

        result = ruleExecutor.executeRule("Stop", Stream.of("HealthCheck","Test","CheckLock")
                .map(s -> VNFOperation.findByString(s)).collect(Collectors.toList()));
        Assert.assertEquals(result,RuleResult.ACCEPT);

        result = ruleExecutor.executeRule("Stop", Stream.of("HealthCheck","Start","CheckLock")
                .map(s -> VNFOperation.findByString(s)).collect(Collectors.toList()));
        Assert.assertEquals(result,RuleResult.REJECT);
    }

    @Test
    public void testRejectRule(){
        RuleResult result;
        result = ruleExecutor.executeRule("Audit", Stream.of("HealthCheck","Test","CheckLock")
                .map(s -> VNFOperation.findByString(s)).collect(Collectors.toList()));
        Assert.assertEquals(result,RuleResult.ACCEPT);

        result = ruleExecutor.executeRule("Audit", Stream.of("HealthCheck", "Test","Restart")
                .map(s -> VNFOperation.findByString(s)).collect(Collectors.toList()));
        Assert.assertEquals(result,RuleResult.REJECT);

        result = ruleExecutor.executeRule("Start", Stream.of("Restart","Start","Stop")
                .map(s -> VNFOperation.findByString(s)).collect(Collectors.toList()));
        Assert.assertEquals(result,RuleResult.ACCEPT);

        result = ruleExecutor.executeRule("Start", Stream.of("HealthCheck","Test","CheckLock")
                .map(s -> VNFOperation.findByString(s)).collect(Collectors.toList()));
        Assert.assertEquals(result,RuleResult.REJECT);
    }



}
