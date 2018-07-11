package org.onap.appc.requesthandler.Constants;

/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 IBM Intellectual Property. All rights reserved.
 * =============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR ConstantsDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ============LICENSE_END=========================================================
 */
    
import org.junit.Assert;
import org.junit.Test;

public class ConstantsTest {

    @Test
    public void TestConstantsstants() {

        Assert.assertEquals(900000, Constants.DEFAULT_IDLE_TIMEOUT);
        Assert.assertEquals(30, Constants.DEFAULT_TTL);
        Assert.assertEquals("SUCCESS", Constants.SUCCESS_MSG);
        Assert.assertEquals("FAILURE", Constants.FAILURE_MSG);
        Assert.assertEquals("true", Constants.DEFAULT_LOGGING_FLAG);
        Assert.assertEquals("org.onap.appc.workflow.default.ttl", Constants.DEFAULT_TTL_KEY);

    }
}