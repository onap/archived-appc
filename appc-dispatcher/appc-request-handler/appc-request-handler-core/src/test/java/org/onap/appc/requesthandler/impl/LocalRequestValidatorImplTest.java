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

package org.onap.appc.requesthandler.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.appc.domainmodel.lcm.ActionLevel;
import org.onap.appc.domainmodel.lcm.RuntimeContext;
import org.onap.appc.domainmodel.lcm.VNFOperation;
import org.onap.appc.exceptions.InvalidInputException;
import org.onap.appc.requesthandler.LCMStateManager;
import org.onap.appc.requesthandler.exceptions.DuplicateRequestException;
import org.onap.appc.requesthandler.exceptions.LCMOperationsDisabledException;

import static org.powermock.api.mockito.PowerMockito.spy;

/**
 * Test class for LocalRequestValidatorImpl
 */
@RunWith(MockitoJUnitRunner.class)
public class LocalRequestValidatorImplTest implements LocalRequestHanlderTestHelper {

    @Mock
    private LCMStateManager lcmStateManager;

    LocalRequestValidatorImpl requestValidator;

    @Before
    public void setUp() throws Exception {
        requestValidator = spy(new LocalRequestValidatorImpl());
        requestValidator.setLcmStateManager(lcmStateManager);

        Mockito.when(lcmStateManager.isLCMOperationEnabled()).thenReturn(true);
    }

    @Test(expected = LCMOperationsDisabledException.class)
    public void validateRequestLCMDisabled() throws Exception {
        Mockito.when(lcmStateManager.isLCMOperationEnabled()).thenReturn(false);
        requestValidator.validateRequest(createRequestValidatorInput());
    }
    //TODO needs to be fixed
    /*@Test
    public void validateRequestSuccess() throws Exception {
        requestValidator.validateRequest(createRequestValidatorInput());
    }

    @Test(expected = DuplicateRequestException.class)
    public void validateRequestDuplicateReqFailure() throws Exception {
        requestValidator.validateRequest(createRequestValidatorInput());
    }

    @Test(expected = InvalidInputException.class)
    public void validateRequestPayloadFail() throws Exception {
        String incorrectPayload = "{\"RequestId\":\"requestToCheck\"}";
        RuntimeContext context = createRequestValidatorInput();
        context.getRequestContext().setPayload(incorrectPayload);
        requestValidator.validateRequest(context);
    }*/

    private RuntimeContext createRequestValidatorInput() {
        return createRequestHandlerRuntimeContext("VSCP", "{\"request-id\":\"request-id\"}");
    }
}
