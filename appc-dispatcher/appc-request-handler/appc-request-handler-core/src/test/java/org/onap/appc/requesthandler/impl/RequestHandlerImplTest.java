/*
 * ============LICENSE_START=======================================================
 * Copyright (C) 2018 Ericsson. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at
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

package org.onap.appc.requesthandler.impl;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFLogger.Level;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.onap.appc.domainmodel.lcm.ActionIdentifiers;
import org.onap.appc.domainmodel.lcm.CommonHeader;
import org.onap.appc.domainmodel.lcm.Flags;
import org.onap.appc.domainmodel.lcm.RequestContext;
import org.onap.appc.domainmodel.lcm.RequestStatus;
import org.onap.appc.domainmodel.lcm.ResponseContext;
import org.onap.appc.domainmodel.lcm.RuntimeContext;
import org.onap.appc.domainmodel.lcm.VNFContext;
import org.onap.appc.domainmodel.lcm.VNFOperation;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.executor.CommandExecutor;
import org.onap.appc.executor.objects.CommandExecutorInput;
import org.onap.appc.executor.objects.LCMCommandStatus;
import org.onap.appc.executor.objects.Params;
import org.onap.appc.i18n.Msg;
import org.onap.appc.lockmanager.api.LockException;
import org.onap.appc.lockmanager.api.LockManager;
import org.onap.appc.logging.LoggingConstants;
import org.onap.appc.metricservice.MetricRegistry;
import org.onap.appc.metricservice.impl.MetricRegistryImpl;
import org.onap.appc.metricservice.metric.MetricType;
import org.onap.appc.metricservice.metric.impl.DispatchingFuntionMetricImpl;
import org.onap.appc.transactionrecorder.TransactionRecorder;
import org.osgi.framework.FrameworkUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ FrameworkUtil.class })
public class RequestHandlerImplTest implements LocalRequestHanlderTestHelper {

    private RequestHandlerImpl requestHandler;
    private TransactionRecorder recorder;
    private LockManager lockManager;

    @Before
    public void setUp() throws Exception {
        setupForHandlerImplTests();
        requestHandler = PowerMockito.spy(new RequestHandlerImpl());
        recorder = mock(TransactionRecorder.class);
        requestHandler.setTransactionRecorder(recorder);
        lockManager = mock(LockManager.class);
        requestHandler.setLockManager(lockManager);
        List<RequestStatus> result = Arrays.asList(RequestStatus.ACCEPTED);
        PowerMockito.when(recorder.getRecords(anyString(), anyString(), anyString(), anyString())).thenReturn(result);
        final EELFLogger logger = EELFManager.getInstance().getLogger(RequestHandlerImpl.class);
        logger.setLevel(Level.TRACE);
        Whitebox.setInternalState(requestHandler, "logger", logger);
    }

    @Test
    public void testHandleRequestRuntimeContext() {
        Whitebox.setInternalState(requestHandler, "isMetricEnabled", true);
        MetricRegistry metricRegistry = spy(new MetricRegistryImpl("TEST_METRIC_REGISTRY"));
        DispatchingFuntionMetricImpl metric = spy(
                new DispatchingFuntionMetricImpl("DISPATCH_FUNCTION", MetricType.COUNTER, 0, 0));
        metricRegistry.register(metric);
        doNothing().when(metric).incrementAcceptedRequest();
        Whitebox.setInternalState(RequestHandlerImpl.class, "metricRegistry", metricRegistry);
        RuntimeContext runtimeContext = spy(new RuntimeContext());
        RequestContext requestContext = setupRequestContext();
        runtimeContext.setRequestContext(requestContext);
        CommonHeader ch = getCommonHeader("TEST", new Date(System.currentTimeMillis()));
        requestContext.setCommonHeader(ch);
        CommandExecutor commandExecutor = mock(CommandExecutor.class);
        requestHandler.setCommandExecutor(commandExecutor);
        doNothing().when(requestHandler).fillStatus(Mockito.any(RuntimeContext.class),
                Mockito.any(LCMCommandStatus.class), Mockito.any());
        requestHandler.handleRequest(runtimeContext);
        Mockito.verify(requestHandler).fillStatus(runtimeContext, LCMCommandStatus.ACCEPTED, null);
    }

    @Test
    public void testHandleRequestRuntimeContextNegativeTTL() {
        RuntimeContext runtimeContext = spy(new RuntimeContext());
        RequestContext requestContext = setupRequestContext();
        runtimeContext.setRequestContext(requestContext);
        CommonHeader ch = getCommonHeader("TEST", new Date(System.currentTimeMillis() - 100000));
        requestContext.setCommonHeader(ch);
        CommandExecutor commandExecutor = mock(CommandExecutor.class);
        requestHandler.setCommandExecutor(commandExecutor);
        doNothing().when(requestHandler).fillStatus(Mockito.any(RuntimeContext.class),
                Mockito.any(LCMCommandStatus.class), Mockito.any());
        doNothing().when(requestHandler).storeErrorMessageToLog(Mockito.any(RuntimeContext.class), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString());
        requestHandler.handleRequest(runtimeContext);
        Mockito.verify(requestHandler).fillStatus(runtimeContext, LCMCommandStatus.EXPIRED_REQUEST, null);

    }

    @Test
    public void testHandleRequestRuntimeContextAPPCException() throws APPCException {
        RuntimeContext runtimeContext = spy(new RuntimeContext());
        RequestContext requestContext = setupRequestContext();
        runtimeContext.setRequestContext(requestContext);
        CommonHeader ch = getCommonHeader("TEST", new Date(System.currentTimeMillis()));
        requestContext.setCommonHeader(ch);
        CommandExecutor commandExecutor = mock(CommandExecutor.class);
        requestHandler.setCommandExecutor(commandExecutor);
        doNothing().when(requestHandler).fillStatus(Mockito.any(RuntimeContext.class),
                Mockito.any(LCMCommandStatus.class), Mockito.any());
        doThrow(new APPCException("TEST_APPC_EXCEPTION")).when(commandExecutor)
        .executeCommand(Mockito.any(CommandExecutorInput.class));
        doNothing().when(requestHandler).storeErrorMessageToLog(Mockito.any(RuntimeContext.class), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString());
        requestHandler.handleRequest(runtimeContext);
        Mockito.verify(requestHandler).fillStatus(Mockito.any(RuntimeContext.class),
                Mockito.any(LCMCommandStatus.class), Mockito.any(Params.class));
    }

    @Test
    public void testHandleRequestRuntimeContextCaseLock() throws LockException {
        RuntimeContext runtimeContext = spy(new RuntimeContext());
        RequestContext requestContext = new RequestContext();
        requestContext.setAction(VNFOperation.Lock);
        runtimeContext.setRequestContext(requestContext);
        VNFContext vnfContext = new VNFContext();
        vnfContext.setId("TEST_VNF_CONTEXT_ID");
        runtimeContext.setVnfContext(vnfContext);
        CommonHeader ch = getCommonHeader("TEST", new Date(System.currentTimeMillis()));
        requestContext.setCommonHeader(ch);
        CommandExecutor commandExecutor = mock(CommandExecutor.class);
        requestHandler.setCommandExecutor(commandExecutor);
        doNothing().when(requestHandler).fillStatus(Mockito.any(RuntimeContext.class),
                Mockito.any(LCMCommandStatus.class), Mockito.any());
        doReturn(true).when(lockManager).acquireLock(Mockito.anyString(), Mockito.anyString(), Mockito.anyLong());
        requestHandler.handleRequest(runtimeContext);
        Mockito.verify(requestHandler).fillStatus(runtimeContext, LCMCommandStatus.SUCCESS, null);
    }

    @Test
    public void testHandleRequestRuntimeContextLockError() throws LockException {
        RuntimeContext runtimeContext = spy(new RuntimeContext());
        RequestContext requestContext = new RequestContext();
        requestContext.setAction(VNFOperation.Lock);
        runtimeContext.setRequestContext(requestContext);
        VNFContext vnfContext = new VNFContext();
        vnfContext.setId("TEST_VNF_CONTEXT_ID");
        runtimeContext.setVnfContext(vnfContext);
        CommonHeader ch = getCommonHeader("TEST", new Date(System.currentTimeMillis()));
        requestContext.setCommonHeader(ch);
        CommandExecutor commandExecutor = mock(CommandExecutor.class);
        requestHandler.setCommandExecutor(commandExecutor);
        doNothing().when(requestHandler).fillStatus(Mockito.any(RuntimeContext.class),
                Mockito.any(LCMCommandStatus.class), Mockito.any());
        doThrow(new LockException("TEST_LOCK_EXCEPTION")).when(lockManager).acquireLock(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyLong());
        doNothing().when(requestHandler).storeErrorMessageToLog(Mockito.any(RuntimeContext.class), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString());
        requestHandler.handleRequest(runtimeContext);
        Mockito.verify(requestHandler).storeErrorMessageToLog(runtimeContext, LoggingConstants.TargetNames.APPC,
                LoggingConstants.TargetNames.LOCK_MANAGER,
                EELFResourceManager.format(Msg.VF_SERVER_BUSY, "TEST_VNF_CONTEXT_ID"));
    }

    @Test
    public void testHandleRequestRuntimeContextCaseUnlock() throws LockException {
        RuntimeContext runtimeContext = spy(new RuntimeContext());
        RequestContext requestContext = new RequestContext();
        requestContext.setAction(VNFOperation.Unlock);
        runtimeContext.setRequestContext(requestContext);
        VNFContext vnfContext = new VNFContext();
        vnfContext.setId("TEST_VNF_CONTEXT_ID");
        runtimeContext.setVnfContext(vnfContext);
        CommonHeader ch = getCommonHeader("TEST", new Date(System.currentTimeMillis()));
        requestContext.setCommonHeader(ch);
        CommandExecutor commandExecutor = mock(CommandExecutor.class);
        requestHandler.setCommandExecutor(commandExecutor);
        doNothing().when(requestHandler).fillStatus(Mockito.any(RuntimeContext.class),
                Mockito.any(LCMCommandStatus.class), Mockito.any());
        doReturn(true).when(lockManager).acquireLock(Mockito.anyString(), Mockito.anyString(), Mockito.anyLong());
        requestHandler.handleRequest(runtimeContext);
        Mockito.verify(requestHandler).fillStatus(runtimeContext, LCMCommandStatus.SUCCESS, null);
    }

    @Test
    public void testHandleRequestRuntimeContextUnlockError() throws LockException {
        RuntimeContext runtimeContext = spy(new RuntimeContext());
        RequestContext requestContext = new RequestContext();
        requestContext.setAction(VNFOperation.Unlock);
        runtimeContext.setRequestContext(requestContext);
        VNFContext vnfContext = new VNFContext();
        vnfContext.setId("TEST_VNF_CONTEXT_ID");
        runtimeContext.setVnfContext(vnfContext);
        CommonHeader ch = getCommonHeader("TEST", new Date(System.currentTimeMillis()));
        requestContext.setCommonHeader(ch);
        CommandExecutor commandExecutor = mock(CommandExecutor.class);
        requestHandler.setCommandExecutor(commandExecutor);
        doNothing().when(requestHandler).fillStatus(Mockito.any(RuntimeContext.class),
                Mockito.any(LCMCommandStatus.class), Mockito.any());
        doThrow(new LockException("TEST_LOCK_EXCEPTION")).when(lockManager).releaseLock(Mockito.anyString(),
                Mockito.anyString());
        doNothing().when(requestHandler).storeErrorMessageToLog(Mockito.any(RuntimeContext.class), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString());
        requestHandler.handleRequest(runtimeContext);
        Mockito.verify(requestHandler).fillStatus(Mockito.any(RuntimeContext.class),
                Mockito.any(LCMCommandStatus.class), Mockito.any(Params.class));
    }

    @Test
    public void testHandleRequestRuntimeContextCaseCheckLock() {
        RuntimeContext runtimeContext = spy(new RuntimeContext());
        RequestContext requestContext = new RequestContext();
        ResponseContext responseContext = spy(new ResponseContext());
        runtimeContext.setResponseContext(responseContext);
        requestContext.setAction(VNFOperation.CheckLock);
        runtimeContext.setRequestContext(requestContext);
        VNFContext vnfContext = new VNFContext();
        vnfContext.setId("TEST_VNF_CONTEXT_ID");
        runtimeContext.setVnfContext(vnfContext);
        CommonHeader ch = getCommonHeader("TEST", new Date(System.currentTimeMillis()));
        requestContext.setCommonHeader(ch);
        CommandExecutor commandExecutor = mock(CommandExecutor.class);
        requestHandler.setCommandExecutor(commandExecutor);
        doNothing().when(requestHandler).fillStatus(Mockito.any(RuntimeContext.class),
                Mockito.any(LCMCommandStatus.class), Mockito.any());
        doReturn(true).when(lockManager).isLocked("TEST_VNF_CONTEXT_ID");
        requestHandler.handleRequest(runtimeContext);
        Mockito.verify(responseContext).addKeyValueToAdditionalContext("locked", String.valueOf(true).toUpperCase());
    }
    
    private RequestContext setupRequestContext() {
        RequestContext requestContext = new RequestContext();
        requestContext.setAction(VNFOperation.ActionStatus);
        ActionIdentifiers actionIdentifiers = new ActionIdentifiers();
        actionIdentifiers.setVnfId("TEST_VNF_ID");
        requestContext.setActionIdentifiers(actionIdentifiers);
        return requestContext;
    }
}
