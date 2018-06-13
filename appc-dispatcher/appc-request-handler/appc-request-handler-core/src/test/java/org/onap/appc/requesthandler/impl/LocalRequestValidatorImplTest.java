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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.requesthandler.impl;


import static org.junit.Assert.fail;
import static org.powermock.api.mockito.PowerMockito.spy;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFLogger.Level;
import com.att.eelf.configuration.EELFManager;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.appc.domainmodel.lcm.ActionIdentifiers;
import org.onap.appc.domainmodel.lcm.RuntimeContext;
import org.onap.appc.domainmodel.lcm.VNFOperation;
import org.onap.appc.exceptions.InvalidInputException;
import org.onap.appc.requesthandler.LCMStateManager;
import org.onap.appc.requesthandler.exceptions.DuplicateRequestException;
import org.onap.appc.requesthandler.exceptions.LCMOperationsDisabledException;
import org.onap.appc.requesthandler.exceptions.RequestExpiredException;
import org.onap.appc.transactionrecorder.TransactionRecorder;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

/**
 * Test class for LocalRequestValidatorImpl
 */

@RunWith(PowerMockRunner.class)
public class LocalRequestValidatorImplTest implements LocalRequestHanlderTestHelper {

    @Mock
    private LCMStateManager lcmStateManager;

    @Mock
    private TransactionRecorder transactionRecorder;

    LocalRequestValidatorImpl requestValidator;

    @Before
    public void setUp() throws Exception {
        requestValidator = spy(new LocalRequestValidatorImpl());
        requestValidator.setLcmStateManager(lcmStateManager);
        requestValidator.setTransactionRecorder(transactionRecorder);

        Mockito.when(lcmStateManager.isLCMOperationEnabled()).thenReturn(true);
        Mockito.when(transactionRecorder.isTransactionDuplicate(Matchers.anyObject())).thenReturn(false);
        final EELFLogger logger = EELFManager.getInstance().getLogger(LocalRequestValidatorImpl.class);
        logger.setLevel(Level.TRACE);
        Whitebox.setInternalState(requestValidator, "logger", logger);
    }

    @Test(expected = LCMOperationsDisabledException.class)
    public void validateRequestLCMDisabled() throws Exception {
        Mockito.when(lcmStateManager.isLCMOperationEnabled()).thenReturn(false);
        requestValidator.validateRequest(createRequestValidatorInput());
    }

    @Test(expected = DuplicateRequestException.class)
    public void validateRequestDuplicateReqFailure() throws Exception {
        Mockito.when(transactionRecorder.isTransactionDuplicate(Matchers.anyObject())).thenReturn(true);
        requestValidator.validateRequest(createRequestValidatorInput());
    }

    @Test
    public void validateRequestSuccess() throws Exception {
        requestValidator.validateRequest(createRequestValidatorInput());
    }

    @Test(expected = InvalidInputException.class)
    public void validateRequestPayloadFail() throws Exception {
        String incorrectPayload = "{\"RequestId\":\"requestToCheck\"}";
        RuntimeContext context = createRequestValidatorInput();
        context.getRequestContext().setPayload(incorrectPayload);
        requestValidator.validateRequest(context);
    }

    @Test(expected = InvalidInputException.class)
    public void validateRequestActionIdentifiersNullVnfIdFail() throws Exception {
        RuntimeContext context = createRequestValidatorInput();
        ActionIdentifiers ai = context.getRequestContext().getActionIdentifiers();
        ai.setVnfId(null);
        requestValidator.validateRequest(context);
    }

    @Test
    public void validateRequestUnsupportedAction() throws Exception {
        RuntimeContext context = createRequestValidatorInput();
        context.getRequestContext().setAction(VNFOperation.AttachVolume);
        requestValidator.validateRequest(context);
    }
    
    @Test(expected = InvalidInputException.class)
    public void validateRequestInvalidInputInvalidTime() throws Exception {
        RuntimeContext context = createRequestValidatorInput();
        context.getResponseContext().getCommonHeader().setTimestamp(new Date(System.currentTimeMillis() + 999999));
        final EELFLogger logger = EELFManager.getInstance().getLogger(AbstractRequestValidatorImpl.class);
        logger.setLevel(Level.TRACE);
        Whitebox.setInternalState(requestValidator, "logger", logger);
        requestValidator.validateRequest(context);
        fail("Exception not thrown");
    }

    @Test(expected = RequestExpiredException.class)
    public void validateRequestRequestExpiredException() throws Exception {
        RuntimeContext context = createRequestValidatorInput();
        context.getResponseContext().getCommonHeader().setTimestamp(new Date(System.currentTimeMillis() - 999999));
        final EELFLogger logger = EELFManager.getInstance().getLogger(AbstractRequestValidatorImpl.class);
        logger.setLevel(Level.TRACE);
        Whitebox.setInternalState(requestValidator, "logger", logger);
        requestValidator.validateRequest(context);
        fail("Exception not thrown");
    }
    
    private RuntimeContext createRequestValidatorInput() {
        return createRequestHandlerRuntimeContext("VSCP", "{\"request-id\":\"request-id\"}");
    }
}
