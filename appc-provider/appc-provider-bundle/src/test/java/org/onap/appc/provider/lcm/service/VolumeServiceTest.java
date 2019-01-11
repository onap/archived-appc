/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications (C) 2019 Ericsson
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

package org.onap.appc.provider.lcm.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.Action;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.AttachVolumeInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.AttachVolumeOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.DetachVolumeInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.DetachVolumeOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.Payload;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.ZULU;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.action.identifiers.ActionIdentifiers;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.common.header.CommonHeader;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.status.Status;
import org.onap.appc.domainmodel.lcm.ResponseContext;
import org.onap.appc.executor.objects.LCMCommandStatus;
import org.onap.appc.requesthandler.objects.RequestHandlerOutput;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RequestExecutor.class)
public class VolumeServiceTest {
    private final String PAYLOAD_STRING = "{\"A\":\"A-value\",\"B\":{\"C\":\"B.C-value\",\"D\":\"B.D-value\"}}";
    private AttachVolumeInput mockAttachInput = mock(AttachVolumeInput.class);
    private DetachVolumeInput mockDetachInput = mock(DetachVolumeInput.class);
    private CommonHeader mockCommonHeader = mock(CommonHeader.class);
    private ActionIdentifiers mockAI = mock(ActionIdentifiers.class);
    private Payload mockPayload = mock(Payload.class);

    private VolumeService volumeServiceForAttachAction;
    private VolumeService volumeServiceForDetachAction;

    @Before
    public void setUp() throws Exception {
        volumeServiceForAttachAction = spy(new VolumeService(true));
        volumeServiceForDetachAction = spy(new VolumeService(false));
    }

    @Test
    public void testConstructor() throws Exception {
        Action expectedAction = Action.AttachVolume;
        Assert.assertEquals("Should have proper ACTION", expectedAction,
            (Action) org.powermock.reflect.Whitebox.getInternalState(volumeServiceForAttachAction, "expectedAction"));
        Assert.assertEquals("Should have attach-volume RPC name", "attach-volume",
            (org.powermock.reflect.Whitebox.getInternalState(volumeServiceForAttachAction, "rpcName")).toString());

        expectedAction = Action.DetachVolume;
        Assert.assertEquals("Should have proper ACTION", expectedAction,
            (Action) org.powermock.reflect.Whitebox.getInternalState(volumeServiceForDetachAction, "expectedAction"));
        Assert.assertEquals("Should have detach-volume RPC name", "detach-volume",
            (org.powermock.reflect.Whitebox.getInternalState(volumeServiceForDetachAction, "rpcName")).toString());
    }

    @Test
    public void testValidateForAttachAction() throws Exception {
        // test commonHeader error
        volumeServiceForAttachAction.validate(mockCommonHeader, Action.AttachVolume, mockAI, mockPayload);
        Status status = (Status) Whitebox.getInternalState(volumeServiceForAttachAction, "status");
        Assert.assertEquals("should return missing parameter",
            Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()), status.getCode());
        Mockito.verify(volumeServiceForDetachAction, times(0)).buildStatusForParamName(any(), any());
        Mockito.verify(volumeServiceForDetachAction, times(0)).buildStatusForErrorMsg(any(), any());

        ZULU mockTimeStamp = mock(ZULU.class);
        Mockito.doReturn(mockTimeStamp).when(mockCommonHeader).getTimestamp();
        Mockito.doReturn("api ver").when(mockCommonHeader).getApiVer();
        Mockito.doReturn("orignator Id").when(mockCommonHeader).getOriginatorId();
        Mockito.doReturn("request Id").when(mockCommonHeader).getRequestId();

        // test Invalid action
        volumeServiceForAttachAction.validate(mockCommonHeader, Action.DetachVolume, mockAI, mockPayload);
        status = (Status) Whitebox.getInternalState(volumeServiceForAttachAction, "status");
        Assert.assertEquals("Should return invalid parameter for action",
            Integer.valueOf(LCMCommandStatus.INVALID_INPUT_PARAMETER.getResponseCode()), status.getCode());

        // test empty ActionIdentifier
        volumeServiceForAttachAction.validate(mockCommonHeader, Action.AttachVolume, mockAI, mockPayload);
        status = (Status) Whitebox.getInternalState(volumeServiceForAttachAction, "status");
        Assert.assertEquals("should return missing parameter",
            Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()), status.getCode());

        // test empty VSERVER_ID
        Mockito.doReturn("").when(mockAI).getVserverId();
        volumeServiceForAttachAction.validate(mockCommonHeader, Action.AttachVolume, mockAI, mockPayload);
        status = (Status) Whitebox.getInternalState(volumeServiceForAttachAction, "status");
        Assert.assertEquals("should return invalid parameter",
            Integer.valueOf(LCMCommandStatus.INVALID_INPUT_PARAMETER.getResponseCode()), status.getCode());

        Mockito.doReturn("vserverId").when(mockAI).getVserverId();

        // test null payload
        volumeServiceForAttachAction.validate(mockCommonHeader, Action.AttachVolume, mockAI, null);
        Mockito.verify(volumeServiceForAttachAction, times(1)).validateExcludedActIds(any(), any());
        status = (Status) Whitebox.getInternalState(volumeServiceForAttachAction, "status");
        Assert.assertEquals("should return missing parameter",
            Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()), status.getCode());

        // test empty payload
        Mockito.doReturn("").when(mockPayload).getValue();
        volumeServiceForAttachAction.validate(mockCommonHeader, Action.AttachVolume, mockAI, mockPayload);
        status = (Status) Whitebox.getInternalState(volumeServiceForAttachAction, "status");
        Assert.assertEquals("should return invalid parameter",
            Integer.valueOf(LCMCommandStatus.INVALID_INPUT_PARAMETER.getResponseCode()), status.getCode());

        // test space payload
        Mockito.doReturn(" ").when(mockPayload).getValue();
        volumeServiceForAttachAction.validate(mockCommonHeader, Action.AttachVolume, mockAI, mockPayload);
        status = (Status) Whitebox.getInternalState(volumeServiceForAttachAction, "status");
        Assert.assertEquals("should return invalid parameter",
            Integer.valueOf(LCMCommandStatus.INVALID_INPUT_PARAMETER.getResponseCode()), status.getCode());
    }

    @Test
    public void testValidateForDetachAction() throws Exception {
        // test commonHeader error
        volumeServiceForDetachAction.validate(mockCommonHeader, Action.DetachVolume, mockAI, mockPayload);
        Status status = (Status) Whitebox.getInternalState(volumeServiceForDetachAction, "status");
        Assert.assertEquals("should return missing parameter",
            Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()), status.getCode());
        Mockito.verify(volumeServiceForDetachAction, times(0)).buildStatusForParamName(any(), any());
        Mockito.verify(volumeServiceForDetachAction, times(0)).buildStatusForErrorMsg(any(), any());

        ZULU mockTimeStamp = mock(ZULU.class);
        Mockito.doReturn(mockTimeStamp).when(mockCommonHeader).getTimestamp();
        Mockito.doReturn("api ver").when(mockCommonHeader).getApiVer();
        Mockito.doReturn("orignator Id").when(mockCommonHeader).getOriginatorId();
        Mockito.doReturn("request Id").when(mockCommonHeader).getRequestId();

        // test Invalid action
        volumeServiceForDetachAction.validate(mockCommonHeader, Action.AttachVolume, mockAI, mockPayload);
        status = (Status) Whitebox.getInternalState(volumeServiceForDetachAction, "status");
        Assert.assertEquals("Should return invalid parameter for action",
            Integer.valueOf(LCMCommandStatus.INVALID_INPUT_PARAMETER.getResponseCode()), status.getCode());

        // test empty ActionIdentifier
        volumeServiceForDetachAction.validate(mockCommonHeader, Action.DetachVolume, mockAI, mockPayload);
        status = (Status) Whitebox.getInternalState(volumeServiceForDetachAction, "status");
        Assert.assertEquals("should return missing parameter",
            Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()), status.getCode());

        // test empty VSERVER_ID
        Mockito.doReturn("").when(mockAI).getVserverId();
        volumeServiceForDetachAction.validate(mockCommonHeader, Action.DetachVolume, mockAI, mockPayload);
        status = (Status) Whitebox.getInternalState(volumeServiceForDetachAction, "status");
        Assert.assertEquals("should return invalid parameter",
            Integer.valueOf(LCMCommandStatus.INVALID_INPUT_PARAMETER.getResponseCode()), status.getCode());

        Mockito.doReturn("vserverId").when(mockAI).getVserverId();

        // test null payload
        volumeServiceForDetachAction.validate(mockCommonHeader, Action.DetachVolume, mockAI, null);
        Mockito.verify(volumeServiceForDetachAction, times(1)).validateExcludedActIds(any(), any());
        status = (Status) Whitebox.getInternalState(volumeServiceForDetachAction, "status");
        Assert.assertEquals("should return missing parameter",
            Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()), status.getCode());

        // test empty payload
        Mockito.doReturn("").when(mockPayload).getValue();
        volumeServiceForDetachAction.validate(mockCommonHeader, Action.DetachVolume, mockAI, mockPayload);
        status = (Status) Whitebox.getInternalState(volumeServiceForDetachAction, "status");
        Assert.assertEquals("should return invalid parameter",
            Integer.valueOf(LCMCommandStatus.INVALID_INPUT_PARAMETER.getResponseCode()), status.getCode());

        // test space payload
        Mockito.doReturn(" ").when(mockPayload).getValue();
        volumeServiceForDetachAction.validate(mockCommonHeader, Action.DetachVolume, mockAI, mockPayload);
        status = (Status) Whitebox.getInternalState(volumeServiceForDetachAction, "status");
        Assert.assertEquals("should return invalid parameter",
            Integer.valueOf(LCMCommandStatus.INVALID_INPUT_PARAMETER.getResponseCode()), status.getCode());
    }
}
