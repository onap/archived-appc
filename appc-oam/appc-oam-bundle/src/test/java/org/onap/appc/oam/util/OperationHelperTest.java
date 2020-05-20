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

package org.onap.appc.oam.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.MaintenanceModeInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.StartInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.StopInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.common.header.CommonHeader;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.common.header.common.header.Flags;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.exceptions.InvalidInputException;
import org.onap.appc.exceptions.InvalidStateException;
import org.onap.appc.lifecyclemanager.LifecycleManager;
import org.onap.appc.lifecyclemanager.objects.LifecycleException;
import org.onap.appc.lifecyclemanager.objects.NoTransitionDefinedException;
import org.onap.appc.oam.AppcOam;
import org.onap.appc.statemachine.impl.readers.AppcOamMetaDataReader;
import org.onap.appc.statemachine.impl.readers.AppcOamStates;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({FrameworkUtil.class})
public class OperationHelperTest {
    private OperationHelper operationHelper;
    private LifecycleManager lifecycleManager = mock(LifecycleManager.class);
    private CommonHeader mockCommonHeader = mock(CommonHeader.class);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        operationHelper = new OperationHelper();
        Whitebox.setInternalState(operationHelper, "lifecycleMgr", lifecycleManager);
    }

    @Test
    public void testIsInputValidWithMissingInput() throws Exception {
        expectedException.expect(InvalidInputException.class);
        expectedException.expectMessage(operationHelper.MISSING_COMMON_HEADER_MESSAGE);

        operationHelper.isInputValid(null);
        expectedException = ExpectedException.none();
    }

    @Test
    public void testIsInputValidWithMissingCommonHeader() throws Exception {
        StartInput mockInput = mock(StartInput.class);
        expectedException.expect(InvalidInputException.class);
        expectedException.expectMessage(operationHelper.MISSING_COMMON_HEADER_MESSAGE);

        operationHelper.isInputValid(mockInput);
        expectedException = ExpectedException.none();
    }

    @Test
    public void testIsInputValidWithMissingOid() throws Exception {
        StartInput mockInput = mock(StartInput.class);
        Mockito.doReturn(mockCommonHeader).when(mockInput).getCommonHeader();
        Mockito.doReturn(null).when(mockCommonHeader).getOriginatorId();
        expectedException.expect(InvalidInputException.class);
        expectedException.expectMessage(operationHelper.MISSING_FIELD_MESSAGE);

        operationHelper.isInputValid(mockInput);
        expectedException = ExpectedException.none();
    }

    @Test
    public void testIsInputValidWithMissingRid() throws Exception {
        StartInput mockInput = mock(StartInput.class);
        Mockito.doReturn(mockCommonHeader).when(mockInput).getCommonHeader();
        Mockito.doReturn("originalId").when(mockCommonHeader).getOriginatorId();
        Mockito.doReturn(null).when(mockCommonHeader).getRequestId();
        expectedException.expect(InvalidInputException.class);
        expectedException.expectMessage(operationHelper.MISSING_FIELD_MESSAGE);

        operationHelper.isInputValid(mockInput);
        expectedException = ExpectedException.none();
    }

    @Test
    public void testIsInputValidWithMmodeFlags() throws Exception {
        MaintenanceModeInput mockInput = mock(MaintenanceModeInput.class);
        Mockito.doReturn(mockCommonHeader).when(mockInput).getCommonHeader();
        Mockito.doReturn("originalId").when(mockCommonHeader).getOriginatorId();
        Mockito.doReturn("requestId").when(mockCommonHeader).getRequestId();
        Mockito.doReturn(mock(Flags.class)).when(mockCommonHeader).getFlags();
        expectedException.expect(InvalidInputException.class);
        expectedException.expectMessage(operationHelper.NOT_SUPPORT_FLAG);

        operationHelper.isInputValid(mockInput);
        expectedException = ExpectedException.none();
    }

    @Test
    public void testIsInputValidPass() throws Exception {
        StartInput mockInput = mock(StartInput.class);
        Mockito.doReturn(mockCommonHeader).when(mockInput).getCommonHeader();
        Mockito.doReturn("originalId").when(mockCommonHeader).getOriginatorId();
        Mockito.doReturn("requestId").when(mockCommonHeader).getRequestId();

        //with Flags
        Mockito.doReturn(mock(Flags.class)).when(mockCommonHeader).getFlags();
        operationHelper.isInputValid(mockInput);

        //without Flags
        Mockito.doReturn(null).when(mockCommonHeader).getFlags();
        operationHelper.isInputValid(mockInput);

        // MaintenanceMode without Flags
        MaintenanceModeInput mockInput1 = mock(MaintenanceModeInput.class);
        Mockito.doReturn(mockCommonHeader).when(mockInput1).getCommonHeader();
        operationHelper.isInputValid(mockInput1);
        assertNotNull(mockInput);
    }

    @Test
    public void testGetCommonHeader() throws Exception {
        CommonHeader commonHeader = mock(CommonHeader.class);
        // for StartInput
        StartInput startInput = mock(StartInput.class);
        Mockito.doReturn(commonHeader).when(startInput).getCommonHeader();
        Assert.assertEquals("Should return startInput commonHeader", commonHeader,
                operationHelper.getCommonHeader(startInput));

        // for StopInput
        StopInput stopInput = mock(StopInput.class);
        Mockito.doReturn(commonHeader).when(stopInput).getCommonHeader();
        Assert.assertEquals("Should return stopInput commonHeader", commonHeader,
                operationHelper.getCommonHeader(stopInput));

        // for MaintenanceModeInput
        MaintenanceModeInput mmInput = mock(MaintenanceModeInput.class);
        Mockito.doReturn(commonHeader).when(mmInput).getCommonHeader();
        Assert.assertEquals("Should return MaintenanceModeInput commonHeader", commonHeader,
                operationHelper.getCommonHeader(mmInput));

        // unsupported type
        Assert.assertTrue("should return null",
                operationHelper.getCommonHeader(new Object()) == null);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetService() throws Exception {
        Class<OperationHelper> operationHelperClass = OperationHelper.class;
        String className = operationHelperClass.getName();
        String exceptionMsg = String.format(operationHelper.NO_SERVICE_REF_FORMAT, className);

        mockStatic(FrameworkUtil.class);
        Bundle bundle = mock(Bundle.class);
        PowerMockito.when(FrameworkUtil.getBundle(operationHelperClass)).thenReturn(bundle);

        // No bundle context
        Mockito.when(bundle.getBundleContext()).thenReturn(null);
        expectedException.expect(APPCException.class);
        expectedException.expectMessage(exceptionMsg);
        operationHelper.getService(operationHelperClass);

        // No service reference
        BundleContext bundleContext = mock(BundleContext.class);
        Mockito.when(bundle.getBundleContext()).thenReturn(bundleContext);
        Mockito.when(bundleContext.getServiceReference(className)).thenReturn(null);
        expectedException.expect(APPCException.class);
        expectedException.expectMessage(exceptionMsg);
        operationHelper.getService(operationHelperClass);

        // Success path
        ServiceReference svcRef = mock(ServiceReference.class);
        Mockito.when(bundleContext.getServiceReference(className)).thenReturn(svcRef);
        expectedException = ExpectedException.none();
        Assert.assertTrue("should not be null", operationHelper.getService(operationHelperClass) != null);
    }

    @Test
    public void testGetNextState() throws Exception {
        AppcOamMetaDataReader.AppcOperation operation = AppcOamMetaDataReader.AppcOperation.Start;
        AppcOamStates currentState = AppcOamStates.Stopped;
        String exceptionMsg = String.format(AppcOam.INVALID_STATE_MESSAGE_FORMAT, operation, "APPC", currentState);

        // got LifecycleException
        Mockito.doThrow(LifecycleException.class).when(lifecycleManager)
                .getNextState("APPC", operation.name(), currentState.name());
        expectedException.expect(InvalidStateException.class);
        expectedException.expectMessage(exceptionMsg);
        operationHelper.getNextState(operation, currentState);

        // got NoTransitionDefinedException
        Mockito.doThrow(NoTransitionDefinedException.class).when(lifecycleManager)
                .getNextState("APPC", operation.name(), currentState.name());
        expectedException.expect(InvalidStateException.class);
        expectedException.expectMessage(exceptionMsg);
        operationHelper.getNextState(operation, currentState);

        // Success path
        expectedException = ExpectedException.none();
        Mockito.doReturn("starting").when(lifecycleManager)
                .getNextState("APPC", operation.name(), currentState.name());
        Assert.assertEquals("Should return proper Starting state", AppcOamStates.Starting,
                operationHelper.getNextState(operation, currentState));
    }
}
