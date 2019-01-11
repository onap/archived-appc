
/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications (C) 2019 Ericsson
 * ================================================================================
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
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.*;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.action.identifiers.ActionIdentifiers;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.common.header.CommonHeader;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.status.Status;
import org.onap.appc.executor.objects.LCMCommandStatus;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

@RunWith(MockitoJUnitRunner.class)
public class RebootServiceTest {
    private  String PAYLOAD_STRING = "{\"A\":\"A-value\",\"B\":{\"C\":\"B.C-value\",\"D\":\"B.D-value\"}}";
    private RebootInput mockInput = mock(RebootInput.class);
    private CommonHeader mockCommonHeader = mock(CommonHeader.class);
    private ActionIdentifiers mockAI = mock(ActionIdentifiers.class);
    private Payload mockPayload = mock(Payload.class);

    private RebootService rebootService;
    @Before
    public void setUp() throws Exception {
        rebootService = spy(new RebootService());
    }

    @Test
    public void testValidateMissingParameters() throws Exception {
        rebootService.validate(mockInput);
        Status status = (Status) Whitebox.getInternalState(rebootService, "status");
        //testing missing parameters in the input
        Assert.assertEquals("should return missing parameter",
                Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()), status.getCode());
    }
    @Test
    public void testValidateForInvalidAction() throws Exception {

        Mockito.doReturn(mockCommonHeader).when(mockInput).getCommonHeader();
        Mockito.doReturn(mockPayload).when(mockInput).getPayload();
        Mockito.doReturn(PAYLOAD_STRING).when(mockPayload).getValue();
        ZULU zuluTimeStamp = new ZULU("2017-06-29T21:44:00.35Z");
        Mockito.doReturn(zuluTimeStamp).when(mockCommonHeader).getTimestamp();
        Mockito.doReturn("api ver").when(mockCommonHeader).getApiVer();
        Mockito.doReturn("orignator Id").when(mockCommonHeader).getOriginatorId();
        Mockito.doReturn("request Id").when(mockCommonHeader).getRequestId();

        Mockito.doReturn(Action.AttachVolume).when(mockInput).getAction();
        Mockito.doReturn(mockAI).when(mockInput).getActionIdentifiers();
        Mockito.doReturn("vserverId").when(mockAI).getVserverId();

        // test invalid action
        rebootService.validate(mockInput);
        Status status = (Status) Whitebox.getInternalState(rebootService, "status");
        Assert.assertEquals("should return missing parameter",
                Integer.valueOf(LCMCommandStatus.INVALID_INPUT_PARAMETER.getResponseCode()), status.getCode());
    }

    @Test
    public void testValidateForMissingActionIdentifiers() throws Exception {

        Mockito.doReturn(mockCommonHeader).when(mockInput).getCommonHeader();
        Mockito.doReturn(mockPayload).when(mockInput).getPayload();
        Mockito.doReturn(PAYLOAD_STRING).when(mockPayload).getValue();
        ZULU zuluTimeStamp = new ZULU("2017-06-29T21:44:00.35Z");
        Mockito.doReturn(zuluTimeStamp).when(mockCommonHeader).getTimestamp();
        Mockito.doReturn("api ver").when(mockCommonHeader).getApiVer();
        Mockito.doReturn("orignator Id").when(mockCommonHeader).getOriginatorId();
        Mockito.doReturn("request Id").when(mockCommonHeader).getRequestId();

        Mockito.doReturn(Action.Reboot).when(mockInput).getAction();
        Mockito.doReturn("vserverId").when(mockAI).getVserverId();

        // test invalid action
        rebootService.validate(mockInput);
        Status status = (Status) Whitebox.getInternalState(rebootService, "status");
        Assert.assertEquals("should return missing parameter",
                Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()), status.getCode());
    }

    @Test
    public void testValidateMissingOrIncorrectPayload() throws Exception {
        PAYLOAD_STRING = "{\"A\":\"A-value\",\"B\":{\"C\":\"B.C-value\",\"D\":\"B.D-value\"},\"type\":\"HARD\"}";

        Mockito.doReturn(mockCommonHeader).when(mockInput).getCommonHeader();
        ZULU zuluTimeStamp = new ZULU("2017-06-29T21:44:00.35Z");
        Mockito.doReturn(zuluTimeStamp).when(mockCommonHeader).getTimestamp();
        Mockito.doReturn("api ver").when(mockCommonHeader).getApiVer();
        Mockito.doReturn("orignator Id").when(mockCommonHeader).getOriginatorId();
        Mockito.doReturn("request Id").when(mockCommonHeader).getRequestId();

        Mockito.doReturn(Action.Reboot).when(mockInput).getAction();
        Mockito.doReturn(mockAI).when(mockInput).getActionIdentifiers();
        Mockito.doReturn("vserverId").when(mockAI).getVserverId();
        Mockito.doReturn("vnfId").when(mockAI).getVnfId();

        //testing missing payload
        rebootService.validate(mockInput);
        Status status = (Status) Whitebox.getInternalState(rebootService, "status");
        Assert.assertEquals("should return missing parameter",
                Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()), status.getCode());

        //testing payload with no value or empty
        Mockito.doReturn(mockPayload).when(mockInput).getPayload();
        rebootService.validate(mockInput);
        status = (Status) Whitebox.getInternalState(rebootService, "status");
        Assert.assertEquals("should return missing parameter",
                Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()), status.getCode());

        //Incorrect PayLoad leads to unexpected error during conversion to map
        PAYLOAD_STRING = "{\"A\":\"A-value\"\"B\":{\"C\":\"B.C-value\",\"D\":\"B.D-value\"},\"type\":\"HARD\"}";
        Mockito.doReturn(PAYLOAD_STRING).when(mockPayload).getValue();
        rebootService.validate(mockInput);
        status = (Status) Whitebox.getInternalState(rebootService, "status");
        Assert.assertEquals("should return missing parameter",
                Integer.valueOf(LCMCommandStatus.UNEXPECTED_ERROR.getResponseCode()), status.getCode());
    }

    @Test
    public void testValidateMissingRebootType() throws Exception {

        Mockito.doReturn(mockCommonHeader).when(mockInput).getCommonHeader();
        Mockito.doReturn(mockPayload).when(mockInput).getPayload();
        Mockito.doReturn(PAYLOAD_STRING).when(mockPayload).getValue();
        ZULU zuluTimeStamp = new ZULU("2017-06-29T21:44:00.35Z");
        Mockito.doReturn(zuluTimeStamp).when(mockCommonHeader).getTimestamp();
        Mockito.doReturn("api ver").when(mockCommonHeader).getApiVer();
        Mockito.doReturn("orignator Id").when(mockCommonHeader).getOriginatorId();
        Mockito.doReturn("request Id").when(mockCommonHeader).getRequestId();

        Mockito.doReturn(Action.Reboot).when(mockInput).getAction();
        Mockito.doReturn(mockAI).when(mockInput).getActionIdentifiers();
        Mockito.doReturn("vserverId").when(mockAI).getVserverId();
        Mockito.doReturn("vnfId").when(mockAI).getVnfId();

        // testing missing reboot-type in the payload
        rebootService.validate(mockInput);
        Status status = (Status) Whitebox.getInternalState(rebootService, "status");
        Assert.assertEquals("should return status null",
                null, status);
    }

    @Test
    public void testValidateInvalidRebootType() throws Exception {
        PAYLOAD_STRING = "{\"A\":\"A-value\",\"B\":{\"C\":\"B.C-value\",\"D\":\"B.D-value\"},\"type\":\"test\"}";
        Mockito.doReturn(mockCommonHeader).when(mockInput).getCommonHeader();
        Mockito.doReturn(mockPayload).when(mockInput).getPayload();
        Mockito.doReturn(PAYLOAD_STRING).when(mockPayload).getValue();
        ZULU zuluTimeStamp = new ZULU("2017-06-29T21:44:00.35Z");
        Mockito.doReturn(zuluTimeStamp).when(mockCommonHeader).getTimestamp();
        Mockito.doReturn("api ver").when(mockCommonHeader).getApiVer();
        Mockito.doReturn("orignator Id").when(mockCommonHeader).getOriginatorId();
        Mockito.doReturn("request Id").when(mockCommonHeader).getRequestId();

        Mockito.doReturn(Action.Reboot).when(mockInput).getAction();
        Mockito.doReturn(mockAI).when(mockInput).getActionIdentifiers();
        Mockito.doReturn("vserverId").when(mockAI).getVserverId();
        Mockito.doReturn("vnfId").when(mockAI).getVnfId();

        // test invalid reboot-type ex: pass "test" in this case
        rebootService.validate(mockInput);
        Status status = (Status) Whitebox.getInternalState(rebootService, "status");
        Assert.assertEquals("should return missing parameter",
                Integer.valueOf(LCMCommandStatus.INVALID_INPUT_PARAMETER.getResponseCode()), status.getCode());
    }
}
