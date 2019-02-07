/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 Ericsson. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.listener.demo.impl;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.listener.EventHandler;
import org.onap.appc.listener.demo.model.CommonMessage.CommonHeader;
import org.onap.appc.listener.demo.model.IncomingMessage;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ProviderOperations.class)
public class TestWorkerImpl {

  private WorkerImpl workerImplSpy;
  private IncomingMessage message;
  private EventHandler dmaap;

  @Before
  public void setUp() {
    message = Mockito.mock(IncomingMessage.class);
    dmaap = Mockito.mock(EventHandler.class);
    workerImplSpy = Mockito.spy(new WorkerImpl(message, dmaap));
  }

  @Test
  public void testRun() throws APPCException {
    CommonHeader commonHeader = Mockito.mock(CommonHeader.class);
    when(message.getHeader()).thenReturn(commonHeader);
    when(commonHeader.getRequestID()).thenReturn("requestId");
    PowerMockito.mockStatic(ProviderOperations.class);
    PowerMockito.when(ProviderOperations.topologyDG(anyObject())).thenReturn(true);
    workerImplSpy.run();
    verify(workerImplSpy, times(1)).run();
  }

  @Test
  public void testRunElseCase() throws APPCException {
    CommonHeader commonHeader = Mockito.mock(CommonHeader.class);
    when(message.getHeader()).thenReturn(commonHeader);
    when(commonHeader.getRequestID()).thenReturn("requestId");
    when(message.toJson()).thenReturn(new JSONObject());
    PowerMockito.mockStatic(ProviderOperations.class);
    PowerMockito.when(ProviderOperations.topologyDG(anyObject())).thenReturn(false);
    workerImplSpy.run();
    verify(workerImplSpy, times(1)).run();
  }

  @Test
  public void testRunWithException() throws APPCException {
    CommonHeader commonHeader = Mockito.mock(CommonHeader.class);
    when(message.getHeader()).thenReturn(commonHeader);
    when(commonHeader.getRequestID()).thenReturn("requestId");
    workerImplSpy.run();
    verify(workerImplSpy, times(1)).run();
  }
}
