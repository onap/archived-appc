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

package org.onap.appc.workflow.impl;

import org.junit.Assert;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.onap.appc.transactionrecorder.TransactionRecorder;
import org.onap.appc.workflow.activator.TransactionAbortedMarker;

import java.util.concurrent.ScheduledExecutorService;

public class MockTransactionAbortedMarker extends TransactionAbortedMarker{

    public MockTransactionAbortedMarker(ScheduledExecutorService executor){
        super(executor);
    }

    @Override
    public TransactionRecorder lookupTransactionRecorder(){
        TransactionRecorder transactionRecorder = Mockito.mock(TransactionRecorder.class);
        Mockito.doNothing().when(transactionRecorder).markTransactionsAborted(Matchers.anyString());
        Mockito.doAnswer((InvocationOnMock invocationOnMock) -> {
            Assert.assertNotNull(invocationOnMock.getArguments()[0]);
            return null;
        }).when(transactionRecorder).setAppcInstanceId(Matchers.anyString());
        return transactionRecorder;
    }
}
