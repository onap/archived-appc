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

package org.onap.appc.provider.lcm.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.Action;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.ZULU;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.common.header.CommonHeader;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.status.Status;
import org.onap.appc.executor.objects.LCMCommandStatus;

import static org.mockito.Mockito.mock;

public class ValidationServiceTest {
    private final Integer expectedErrorCode = Integer.valueOf(
        LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode());
    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void getInstance() throws Exception {
        Assert.assertEquals("Should always return the same instance",
            ValidationService.getInstance(), ValidationService.getInstance());
    }

    @Test
    public void validateInput() throws Exception {
        Status status = ValidationService.getInstance().validateInput(null, null, null);
        Assert.assertEquals("Should return error status", expectedErrorCode, status.getCode());
        Assert.assertTrue("Should include common-header in the message",
            status.getMessage().contains("common-header"));
        Assert.assertTrue("Should include action in the message",
            status.getMessage().contains("action"));

        CommonHeader mockCommonHeader = mock(CommonHeader.class);
        status = ValidationService.getInstance().validateInput(mockCommonHeader, Action.Query, "query");
        Assert.assertEquals("Should return error status", expectedErrorCode, status.getCode());
        Assert.assertFalse("Should not include action in the message",
            status.getMessage().contains("action"));

        Mockito.when(mockCommonHeader.getApiVer()).thenReturn("testing API version");
        status = ValidationService.getInstance().validateInput(mockCommonHeader, Action.Query, "query");
        Assert.assertEquals("Should return error status", expectedErrorCode, status.getCode());
        Assert.assertFalse("Should not include api-ver in the message",
            status.getMessage().contains("api-ver"));

        Mockito.when(mockCommonHeader.getOriginatorId()).thenReturn("testing originator id");
        status = ValidationService.getInstance().validateInput(mockCommonHeader, Action.Query, "query");
        Assert.assertEquals("Should return error status", expectedErrorCode, status.getCode());
        Assert.assertFalse("Should not include originator-id in the message",
            status.getMessage().contains("originator-id"));

        Mockito.when(mockCommonHeader.getRequestId()).thenReturn("testing request id");
        status = ValidationService.getInstance().validateInput(mockCommonHeader, Action.Query, "query");
        Assert.assertEquals("Should return error status", expectedErrorCode, status.getCode());
        Assert.assertFalse("Should not include request-id in the message",
            status.getMessage().contains("request-id"));

        Mockito.when(mockCommonHeader.getTimestamp()).thenReturn(mock(ZULU.class));
        status = ValidationService.getInstance().validateInput(mockCommonHeader, Action.Query, "query");
        Assert.assertTrue("Should return success", status == null);
    }
}
