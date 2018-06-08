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

package org.onap.appc.provider.lcm.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.*;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.action.identifiers.ActionIdentifiers;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.common.header.CommonHeader;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.status.Status;
import org.onap.appc.domainmodel.lcm.ResponseContext;
import org.onap.appc.executor.objects.LCMCommandStatus;
import org.onap.appc.requesthandler.objects.RequestHandlerOutput;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({UpgradeService.class, RequestExecutor.class})
public class UpgradeServiceTest {
    private final String PAYLOAD_STRING = "{\"A\":\"A-value\",\"B\":{\"C\":\"B.C-value\",\"D\":\"B.D-value\"}}";
    private UpgradePreCheckInput mockUpgradePreInput = mock(UpgradePreCheckInput.class);
    private UpgradePostCheckInput mockUpgradePostInput = mock(UpgradePostCheckInput.class);
    private UpgradeSoftwareInput mockUpgradeSoftInput = mock(UpgradeSoftwareInput.class);
    private UpgradeBackupInput mockUpgradeBackupInput = mock(UpgradeBackupInput.class);
    private UpgradeBackoutInput mockUpgradeBackoutInput = mock(UpgradeBackoutInput.class);
    private CommonHeader mockCommonHeader = mock(CommonHeader.class);
    private ActionIdentifiers mockAI = mock(ActionIdentifiers.class);
    private Payload mockPayload = mock(Payload.class);

    private UpgradeService upgradePreAction;
    private UpgradeService upgradePostAction;
    private UpgradeService upgradeSoftAction;
    private UpgradeService upgradeBackupAction;
    private UpgradeService upgradeBackoutAction;
   
    @Before
    public void setUp() throws Exception {
    	upgradePreAction = spy(new UpgradeService("upgradePre"));
    	upgradePostAction = spy(new UpgradeService("upgradePost"));
    	upgradeSoftAction = spy(new UpgradeService("upgradeSoft"));
    	upgradeBackupAction = spy(new UpgradeService("upgradeBackup"));
    	upgradeBackoutAction = spy(new UpgradeService("upgradeBackout"));
    }

    @Test
    public void testConstructor() throws Exception {
        Action expectedAction = Action.UpgradePreCheck;
        Assert.assertEquals("Should have proper ACTION", expectedAction,
            (Action) org.powermock.reflect.Whitebox.getInternalState(upgradePreAction, "expectedAction"));
        Assert.assertEquals("Should have upgrade-precheck RPC name", "upgrade-pre-check",
            (org.powermock.reflect.Whitebox.getInternalState(upgradePreAction, "rpcName")).toString());

        expectedAction = Action.UpgradePostCheck;
        Assert.assertEquals("Should have proper ACTION", expectedAction,
            (Action) org.powermock.reflect.Whitebox.getInternalState(upgradePostAction, "expectedAction"));
        Assert.assertEquals("Should have upgrade-postcheck RPC name","upgrade-post-check",
            (org.powermock.reflect.Whitebox.getInternalState(upgradePostAction, "rpcName")).toString());

         expectedAction = Action.UpgradeSoftware;
        Assert.assertEquals("Should have proper ACTION", expectedAction,
                (Action) org.powermock.reflect.Whitebox.getInternalState(upgradeSoftAction, "expectedAction"));
        Assert.assertEquals("Should have upgrade-software RPC name", "upgrade-software",
                (org.powermock.reflect.Whitebox.getInternalState(upgradeSoftAction, "rpcName")).toString());

        expectedAction = Action.UpgradeBackup;
        Assert.assertEquals("Should have proper ACTION", expectedAction,
                (Action) org.powermock.reflect.Whitebox.getInternalState(upgradeBackupAction, "expectedAction"));
        Assert.assertEquals("Should have upgrade-backup RPC name","upgrade-backup",
                (org.powermock.reflect.Whitebox.getInternalState(upgradeBackupAction, "rpcName")).toString());

        expectedAction = Action.UpgradeBackout;
        Assert.assertEquals("Should have proper ACTION", expectedAction,
                (Action) org.powermock.reflect.Whitebox.getInternalState(upgradeBackoutAction, "expectedAction"));
        Assert.assertEquals("Should have upgrade-backout RPC name","upgrade-backout",
                (org.powermock.reflect.Whitebox.getInternalState(upgradeBackoutAction, "rpcName")).toString());

    }

//    @Test
//    public void testUpgradePreCheck() throws Exception {
//        // test error occurs in validation
//        UpgradePreCheckOutputBuilder outputBuilder = upgradePreAction.upgradePreCheck(mockUpgradePreInput);
//        //Mockito.verify(upgradePreAction, times(0)).proceedAction(any(), any(), any());
//        Assert.assertTrue("Should not have commonHeader as we did not mock it",
//            outputBuilder.getCommonHeader() == null);
//        Assert.assertEquals("should return missing parameter status",
//            Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()),
//            outputBuilder.getStatus().getCode());
//
//        // make validation pass
//        Mockito.doReturn(mockCommonHeader).when(mockUpgradePreInput).getCommonHeader();
//        Mockito.doReturn(mockPayload).when(mockUpgradePreInput).getPayload();
//        Mockito.doReturn(PAYLOAD_STRING).when(mockPayload).getValue();
//
//        ZULU zuluTimeStamp = new ZULU("2017-06-29T21:44:00.35Z");
//        Mockito.doReturn(zuluTimeStamp).when(mockCommonHeader).getTimestamp();
//        Mockito.doReturn("api ver").when(mockCommonHeader).getApiVer();
//        Mockito.doReturn("orignator Id").when(mockCommonHeader).getOriginatorId();
//        Mockito.doReturn("request Id").when(mockCommonHeader).getRequestId();
//
//        Mockito.doReturn(Action.UpgradePreCheck).when(mockUpgradePreInput).getAction();
//        Mockito.doReturn(mockAI).when(mockUpgradePreInput).getActionIdentifiers();
//        Mockito.doReturn("vnfId").when(mockAI).getVnfId();
//
//        // test proceedAction return with error
//        outputBuilder = upgradePreAction.upgradePreCheck(mockUpgradePreInput);
//        //Mockito.verify(upgradePreAction, times(1)).proceedAction(any(), any(), any());
//        Assert.assertTrue("Should have commonHeader",
//            outputBuilder.getCommonHeader() != null);
//        Assert.assertEquals("should return rejected status",
//            Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()),
//            outputBuilder.getStatus().getCode());
//
//        // test proceedAction return without error
//        RequestExecutor mockExecutor = mock(RequestExecutor.class);
//        whenNew(RequestExecutor.class).withNoArguments().thenReturn(mockExecutor);
//
//        RequestHandlerOutput mockOutput = mock(RequestHandlerOutput.class);
//        Mockito.doReturn(mockOutput).when(mockExecutor).executeRequest(any());
//
//        ResponseContext mockResponseContext = mock(ResponseContext.class);
//        Mockito.doReturn(mockResponseContext).when(mockOutput).getResponseContext();
//
//        org.onap.appc.domainmodel.lcm.Status mockStatus = mock(org.onap.appc.domainmodel.lcm.Status.class);
//        Integer successCode = Integer.valueOf(LCMCommandStatus.SUCCESS.getResponseCode());
//        Mockito.doReturn(successCode).when(mockStatus).getCode();
//        Mockito.doReturn(mockStatus).when(mockResponseContext).getStatus();
//
//        outputBuilder = upgradePreAction.upgradePreCheck(mockUpgradePreInput);
//        Assert.assertTrue("Should have commonHeader",
//            outputBuilder.getCommonHeader() != null);
//        Assert.assertEquals("should return success status",
//            new Integer(302), outputBuilder.getStatus().getCode());
//    }
//
//    @Test
//    public void testUpgradePostCheck() throws Exception {
//        // test error occurs in validation
//        UpgradePostCheckOutputBuilder outputBuilder = upgradePostAction.upgradePostCheck(mockUpgradePostInput);
//       // Mockito.verify(upgradePostAction, times(0)).proceedAction(any(), any(), any());
//        Assert.assertTrue("Should not have commonHeader as we did not mock it",
//            outputBuilder.getCommonHeader() == null);
//        Assert.assertEquals("should return missing parameter status",
//            Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()),
//            outputBuilder.getStatus().getCode());
//
//        // make validation pass
//        Mockito.doReturn(mockCommonHeader).when(mockUpgradePostInput).getCommonHeader();
//        Mockito.doReturn(mockPayload).when(mockUpgradePostInput).getPayload();
//        Mockito.doReturn(PAYLOAD_STRING).when(mockPayload).getValue();
//
//        ZULU zuluTimeStamp = new ZULU("2017-06-29T21:44:00.35Z");
//        Mockito.doReturn(zuluTimeStamp).when(mockCommonHeader).getTimestamp();
//        Mockito.doReturn("api ver").when(mockCommonHeader).getApiVer();
//        Mockito.doReturn("orignator Id").when(mockCommonHeader).getOriginatorId();
//        Mockito.doReturn("request Id").when(mockCommonHeader).getRequestId();
//
//        Mockito.doReturn(Action.UpgradePostCheck).when(mockUpgradePostInput).getAction();
//        Mockito.doReturn(mockAI).when(mockUpgradePostInput).getActionIdentifiers();
//        Mockito.doReturn("vnfId").when(mockAI).getVnfId();
//
//        // test proceedAction return with error
//        outputBuilder = upgradePostAction.upgradePostCheck(mockUpgradePostInput);
//       // Mockito.verify(upgradePostAction, times(1)).proceedAction(any(), any(), any());
//        Assert.assertTrue("Should have commonHeader",
//            outputBuilder.getCommonHeader() != null);
//        Assert.assertEquals("should return rejected status",
//            Integer.valueOf(302),
//            outputBuilder.getStatus().getCode());
//
//        // test proceedAction return without error
//        RequestExecutor mockExecutor = mock(RequestExecutor.class);
//        whenNew(RequestExecutor.class).withNoArguments().thenReturn(mockExecutor);
//
//        RequestHandlerOutput mockOutput = mock(RequestHandlerOutput.class);
//        Mockito.doReturn(mockOutput).when(mockExecutor).executeRequest(any());
//
//        ResponseContext mockResponseContext = mock(ResponseContext.class);
//        Mockito.doReturn(mockResponseContext).when(mockOutput).getResponseContext();
//
//        org.onap.appc.domainmodel.lcm.Status mockStatus = mock(org.onap.appc.domainmodel.lcm.Status.class);
//        Integer successCode = Integer.valueOf(LCMCommandStatus.SUCCESS.getResponseCode());
//        Mockito.doReturn(successCode).when(mockStatus).getCode();
//        Mockito.doReturn(mockStatus).when(mockResponseContext).getStatus();;
//
//        outputBuilder = upgradePostAction.upgradePostCheck(mockUpgradePostInput);
//        Assert.assertTrue("Should have commonHeader",
//            outputBuilder.getCommonHeader() != null);
//        Assert.assertEquals("should return success status",
//                new Integer(302), outputBuilder.getStatus().getCode());
//    }
//    @Test
//    public void testUpgradeSoftware() throws Exception {
//        // test error occurs in validation
//        UpgradeSoftwareOutputBuilder outputBuilder = upgradeSoftAction.upgradeSoftware(mockUpgradeSoftInput);
//        //Mockito.verify(upgradeSoftAction, times(0)).proceedAction(any(), any(), any());
//        Assert.assertTrue("Should not have commonHeader as we did not mock it",
//                outputBuilder.getCommonHeader() == null);
//        Assert.assertEquals("should return missing parameter status",
//                Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()),
//                outputBuilder.getStatus().getCode());
//
//        // make validation pass
//        Mockito.doReturn(mockCommonHeader).when(mockUpgradeSoftInput).getCommonHeader();
//        Mockito.doReturn(mockPayload).when(mockUpgradeSoftInput).getPayload();
//        Mockito.doReturn(PAYLOAD_STRING).when(mockPayload).getValue();
//
//        ZULU zuluTimeStamp = new ZULU("2017-06-29T21:44:00.35Z");
//        Mockito.doReturn(zuluTimeStamp).when(mockCommonHeader).getTimestamp();
//        Mockito.doReturn("api ver").when(mockCommonHeader).getApiVer();
//        Mockito.doReturn("orignator Id").when(mockCommonHeader).getOriginatorId();
//        Mockito.doReturn("request Id").when(mockCommonHeader).getRequestId();
//
//        Mockito.doReturn(Action.UpgradeSoftware).when(mockUpgradeSoftInput).getAction();
//        Mockito.doReturn(mockAI).when(mockUpgradeSoftInput).getActionIdentifiers();
//        Mockito.doReturn("vnfId").when(mockAI).getVnfId();
//
//        // test proceedAction return with error
//        outputBuilder = upgradeSoftAction.upgradeSoftware(mockUpgradeSoftInput);
//        //Mockito.verify(upgradeSoftAction, times(1)).proceedAction(any(), any(), any());
//        Assert.assertTrue("Should have commonHeader",
//                outputBuilder.getCommonHeader() != null);
//        Assert.assertEquals("should return rejected status",
//                Integer.valueOf(302),
//                outputBuilder.getStatus().getCode());
//
//        // test proceedAction return without error
//        RequestExecutor mockExecutor = mock(RequestExecutor.class);
//        whenNew(RequestExecutor.class).withNoArguments().thenReturn(mockExecutor);
//
//        RequestHandlerOutput mockOutput = mock(RequestHandlerOutput.class);
//        Mockito.doReturn(mockOutput).when(mockExecutor).executeRequest(any());
//
//        ResponseContext mockResponseContext = mock(ResponseContext.class);
//        Mockito.doReturn(mockResponseContext).when(mockOutput).getResponseContext();
//
//        org.onap.appc.domainmodel.lcm.Status mockStatus = mock(org.onap.appc.domainmodel.lcm.Status.class);
//        Integer successCode = Integer.valueOf(LCMCommandStatus.SUCCESS.getResponseCode());
//        Mockito.doReturn(successCode).when(mockStatus).getCode();
//        Mockito.doReturn(mockStatus).when(mockResponseContext).getStatus();
//
//        outputBuilder = upgradeSoftAction.upgradeSoftware(mockUpgradeSoftInput);
//        Assert.assertTrue("Should have commonHeader",
//                outputBuilder.getCommonHeader() != null);
//        Assert.assertEquals("should return success status",
//                new Integer(302), outputBuilder.getStatus().getCode());
//    }
//    @Test
//    public void testUpgradeBackup() throws Exception {
//        // test error occurs in validation
//        UpgradeBackupOutputBuilder outputBuilder = upgradeBackupAction.upgradeBackup(mockUpgradeBackupInput);
//        //Mockito.verify(upgradeBackupAction, times(0)).proceedAction(any(), any(), any());
//        Assert.assertTrue("Should not have commonHeader as we did not mock it",
//                outputBuilder.getCommonHeader() == null);
//        Assert.assertEquals("should return missing parameter status",
//                Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()),
//                outputBuilder.getStatus().getCode());
//
//        // make validation pass
//        Mockito.doReturn(mockCommonHeader).when(mockUpgradeBackupInput).getCommonHeader();
//        Mockito.doReturn(mockPayload).when(mockUpgradeBackupInput).getPayload();
//        Mockito.doReturn(PAYLOAD_STRING).when(mockPayload).getValue();
//
//        ZULU zuluTimeStamp = new ZULU("2017-06-29T21:44:00.35Z");
//        Mockito.doReturn(zuluTimeStamp).when(mockCommonHeader).getTimestamp();
//        Mockito.doReturn("api ver").when(mockCommonHeader).getApiVer();
//        Mockito.doReturn("orignator Id").when(mockCommonHeader).getOriginatorId();
//        Mockito.doReturn("request Id").when(mockCommonHeader).getRequestId();
//
//        Mockito.doReturn(Action.UpgradeBackup).when(mockUpgradeBackupInput).getAction();
//        Mockito.doReturn(mockAI).when(mockUpgradeBackupInput).getActionIdentifiers();
//        Mockito.doReturn("vnfId").when(mockAI).getVnfId();
//
//        // test proceedAction return with error
//        outputBuilder = upgradeBackupAction.upgradeBackup(mockUpgradeBackupInput);
//        //Mockito.verify(upgradeBackupAction, times(1)).proceedAction(any(), any(), any());
//        Assert.assertTrue("Should have commonHeader",
//                outputBuilder.getCommonHeader() != null);
//        Assert.assertEquals("should return rejected status",
//                Integer.valueOf(302),
//                outputBuilder.getStatus().getCode());
//
//        // test proceedAction return without error
//        RequestExecutor mockExecutor = mock(RequestExecutor.class);
//        whenNew(RequestExecutor.class).withNoArguments().thenReturn(mockExecutor);
//
//        RequestHandlerOutput mockOutput = mock(RequestHandlerOutput.class);
//        Mockito.doReturn(mockOutput).when(mockExecutor).executeRequest(any());
//
//        ResponseContext mockResponseContext = mock(ResponseContext.class);
//        Mockito.doReturn(mockResponseContext).when(mockOutput).getResponseContext();
//
//        org.onap.appc.domainmodel.lcm.Status mockStatus = mock(org.onap.appc.domainmodel.lcm.Status.class);
//        Integer successCode = Integer.valueOf(LCMCommandStatus.SUCCESS.getResponseCode());
//        Mockito.doReturn(successCode).when(mockStatus).getCode();
//        Mockito.doReturn(mockStatus).when(mockResponseContext).getStatus();
//
//        outputBuilder = upgradeBackupAction.upgradeBackup(mockUpgradeBackupInput);
//        Assert.assertTrue("Should have commonHeader",
//                outputBuilder.getCommonHeader() != null);
//        Assert.assertEquals("should return success status",
//                new Integer(302), outputBuilder.getStatus().getCode());
//    }
//
//    @Test
//    public void testUpgradeBackout() throws Exception {
//        // test error occurs in validation
//        UpgradeBackoutOutputBuilder outputBuilder = upgradeBackoutAction.upgradeBackout(mockUpgradeBackoutInput);
//        //Mockito.verify(upgradeBackoutAction, times(0)).proceedAction(any(), any(), any());
//        Assert.assertTrue("Should not have commonHeader as we did not mock it",
//                outputBuilder.getCommonHeader() == null);
//        Assert.assertEquals("should return missing parameter status",
//                Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()),
//                outputBuilder.getStatus().getCode());
//
//        // make validation pass
//        Mockito.doReturn(mockCommonHeader).when(mockUpgradeBackoutInput).getCommonHeader();
//        Mockito.doReturn(mockPayload).when(mockUpgradeBackoutInput).getPayload();
//        Mockito.doReturn(PAYLOAD_STRING).when(mockPayload).getValue();
//
//        ZULU zuluTimeStamp = new ZULU("2017-06-29T21:44:00.35Z");
//        Mockito.doReturn(zuluTimeStamp).when(mockCommonHeader).getTimestamp();
//        Mockito.doReturn("api ver").when(mockCommonHeader).getApiVer();
//        Mockito.doReturn("orignator Id").when(mockCommonHeader).getOriginatorId();
//        Mockito.doReturn("request Id").when(mockCommonHeader).getRequestId();
//
//        Mockito.doReturn(Action.UpgradeBackup).when(mockUpgradeBackupInput).getAction();
//        Mockito.doReturn(mockAI).when(mockUpgradeBackupInput).getActionIdentifiers();
//        Mockito.doReturn("vnfId").when(mockAI).getVnfId();
//
//        // test proceedAction return with error
//        outputBuilder = upgradeBackoutAction.upgradeBackout(mockUpgradeBackoutInput);
//        //Mockito.verify(upgradeBackoutAction, times(1)).proceedAction(any(), any(), any());
//        Assert.assertTrue("Should have commonHeader",
//                outputBuilder.getCommonHeader() != null);
//        Assert.assertEquals("should return rejected status",
//                Integer.valueOf(302),
//                outputBuilder.getStatus().getCode());
//
//        // test proceedAction return without error
//        RequestExecutor mockExecutor = mock(RequestExecutor.class);
//        whenNew(RequestExecutor.class).withNoArguments().thenReturn(mockExecutor);
//
//        RequestHandlerOutput mockOutput = mock(RequestHandlerOutput.class);
//        Mockito.doReturn(mockOutput).when(mockExecutor).executeRequest(any());
//
//        ResponseContext mockResponseContext = mock(ResponseContext.class);
//        Mockito.doReturn(mockResponseContext).when(mockOutput).getResponseContext();
//
//        org.onap.appc.domainmodel.lcm.Status mockStatus = mock(org.onap.appc.domainmodel.lcm.Status.class);
//        Integer successCode = Integer.valueOf(LCMCommandStatus.SUCCESS.getResponseCode());
//        Mockito.doReturn(successCode).when(mockStatus).getCode();
//        Mockito.doReturn(mockStatus).when(mockResponseContext).getStatus();
//
//        outputBuilder = upgradeBackoutAction.upgradeBackout(mockUpgradeBackoutInput);
//        Assert.assertTrue("Should have commonHeader",
//                outputBuilder.getCommonHeader() != null);
//        Assert.assertEquals("should return success status",
//                new Integer(302), outputBuilder.getStatus().getCode());
//    }
//}

//    @Test
//    public void testValidateForPreCheckAction() throws Exception {
//        // test commonHeader error
//        upgradePreAction.validate(mockCommonHeader, Action.UpgradePreCheck, mockAI, mockPayload);
//        Status status = (Status) Whitebox.getInternalState(upgradePreAction, "status");
//        Assert.assertEquals("should return missing parameter",
//            Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()), status.getCode());
//        //Mockito.verify(upgradePreAction, times(0)).buildStatusForParamName(any(), any());
//       // Mockito.verify(upgradePreAction, times(0)).buildStatusForErrorMsg(any(), any());
//
//        ZULU mockTimeStamp = mock(ZULU.class);
//        Mockito.doReturn(mockTimeStamp).when(mockCommonHeader).getTimestamp();
//        Mockito.doReturn("api ver").when(mockCommonHeader).getApiVer();
//        Mockito.doReturn("orignator Id").when(mockCommonHeader).getOriginatorId();
//        Mockito.doReturn("request Id").when(mockCommonHeader).getRequestId();
//
//        // test Invalid action
//        upgradePreAction.validate(mockCommonHeader, Action.UpgradePreCheck, mockAI, mockPayload);
//        status = (Status) Whitebox.getInternalState(upgradePreAction, "status");
//        Assert.assertEquals("Should return invalid parameter for action",
//            Integer.valueOf(302), status.getCode());
//
//        // test empty ActionIdentifier
//        upgradePreAction.validate(mockCommonHeader, Action.UpgradePreCheck, mockAI, mockPayload);
//        status = (Status) Whitebox.getInternalState(upgradePreAction, "status");
//        Assert.assertEquals("should return missing parameter",
//            Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()), status.getCode());
//
//        // test empty VSERVER_ID
//        Mockito.doReturn("").when(mockAI).getVnfId();
//        upgradePreAction.validate(mockCommonHeader, Action.UpgradePreCheck, mockAI, mockPayload);
//        status = (Status) Whitebox.getInternalState(upgradePreAction, "status");
//        Assert.assertEquals("should return invalid parameter",
//            Integer.valueOf(302), status.getCode());
//
//        Mockito.doReturn("vnfId").when(mockAI).getVnfId();
//
//        // test null payload
//        upgradePreAction.validate(mockCommonHeader, Action.UpgradePreCheck, mockAI, null);
//        //Mockito.verify(upgradePreAction, times(1)).validateExcludedActIds(any(), any());
//        status = (Status) Whitebox.getInternalState(upgradePreAction, "status");
//        Assert.assertEquals("should return missing parameter",
//            Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()), status.getCode());
//
//        // test empty payload
//        Mockito.doReturn("").when(mockPayload).getValue();
//        upgradePreAction.validate(mockCommonHeader, Action.UpgradePreCheck, mockAI, mockPayload);
//        status = (Status) Whitebox.getInternalState(upgradePreAction, "status");
//        Assert.assertEquals("should return invalid parameter",
//            Integer.valueOf(302), status.getCode());
//
//        // test space payload
//        Mockito.doReturn(" ").when(mockPayload).getValue();
//        upgradePreAction.validate(mockCommonHeader, Action.UpgradePreCheck, mockAI, mockPayload);
//        status = (Status) Whitebox.getInternalState(upgradePreAction, "status");
//        Assert.assertEquals("should return invalid parameter",
//            Integer.valueOf(302), status.getCode());
//    }
//
//    @Test
//    public void testValidateForUpgradePostAction() throws Exception {
//        // test commonHeader error
//        upgradePostAction.validate(mockCommonHeader, Action.UpgradePostCheck, mockAI, mockPayload);
//        Status status = (Status) Whitebox.getInternalState(upgradePostAction, "status");
//        Assert.assertEquals("should return missing parameter",
//                Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()), status.getCode());
//       // Mockito.verify(upgradePostAction, times(0)).buildStatusForParamName(any(), any());
//        //Mockito.verify(upgradePostAction, times(0)).buildStatusForErrorMsg(any(), any());
//
//        ZULU mockTimeStamp = mock(ZULU.class);
//        Mockito.doReturn(mockTimeStamp).when(mockCommonHeader).getTimestamp();
//        Mockito.doReturn("api ver").when(mockCommonHeader).getApiVer();
//        Mockito.doReturn("orignator Id").when(mockCommonHeader).getOriginatorId();
//        Mockito.doReturn("request Id").when(mockCommonHeader).getRequestId();
//
//        // test Invalid action
//        upgradePostAction.validate(mockCommonHeader, Action.UpgradePostCheck, mockAI, mockPayload);
//        status = (Status) Whitebox.getInternalState(upgradePostAction, "status");
//        Assert.assertEquals("Should return invalid parameter for action",
//                Integer.valueOf(302), status.getCode());
//
//        // test empty ActionIdentifier
//        upgradePostAction.validate(mockCommonHeader, Action.UpgradePostCheck, mockAI, mockPayload);
//        status = (Status) Whitebox.getInternalState(upgradePostAction, "status");
//        Assert.assertEquals("should return missing parameter",
//                Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()), status.getCode());
//
//        // test empty VSERVER_ID
//        Mockito.doReturn("").when(mockAI).getVnfId();
//        upgradePostAction.validate(mockCommonHeader, Action.UpgradePostCheck, mockAI, mockPayload);
//        status = (Status) Whitebox.getInternalState(upgradePostAction, "status");
//        Assert.assertEquals("should return invalid parameter",
//                Integer.valueOf(302), status.getCode());
//
//        Mockito.doReturn("vnfId").when(mockAI).getVnfId();
//
//        // test null payload
//        upgradePostAction.validate(mockCommonHeader, Action.UpgradePostCheck, mockAI, null);
//        //Mockito.verify(upgradePostAction, times(1)).validateExcludedActIds(any(), any());
//        status = (Status) Whitebox.getInternalState(upgradePostAction, "status");
//        Assert.assertEquals("should return missing parameter",
//                Integer.valueOf(302), status.getCode());
//
//        // test empty payload
//        Mockito.doReturn("").when(mockPayload).getValue();
//        upgradePostAction.validate(mockCommonHeader, Action.UpgradePostCheck, mockAI, mockPayload);
//        status = (Status) Whitebox.getInternalState(upgradePostAction, "status");
//        Assert.assertEquals("should return invalid parameter",
//                Integer.valueOf(302), status.getCode());
//
//        // test space payload
//        Mockito.doReturn(" ").when(mockPayload).getValue();
//        upgradePostAction.validate(mockCommonHeader, Action.UpgradePostCheck, mockAI, mockPayload);
//        status = (Status) Whitebox.getInternalState(upgradePostAction, "status");
//        Assert.assertEquals("should return invalid parameter",
//                Integer.valueOf(302), status.getCode());
//    }
//    @Test
//    public void testValidateForUpgradeBackoutAction() throws Exception {
//        // test commonHeader error
//        upgradeBackoutAction.validate(mockCommonHeader, Action.UpgradeBackout, mockAI, mockPayload);
//        Status status = (Status) Whitebox.getInternalState(upgradeBackoutAction, "status");
//        Assert.assertEquals("should return missing parameter",
//                Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()), status.getCode());
//        //Mockito.verify(upgradeBackoutAction, times(0)).buildStatusForParamName(any(), any());
//       // Mockito.verify(upgradeBackoutAction, times(0)).buildStatusForErrorMsg(any(), any());
//
//        ZULU mockTimeStamp = mock(ZULU.class);
//        Mockito.doReturn(mockTimeStamp).when(mockCommonHeader).getTimestamp();
//        Mockito.doReturn("api ver").when(mockCommonHeader).getApiVer();
//        Mockito.doReturn("orignator Id").when(mockCommonHeader).getOriginatorId();
//        Mockito.doReturn("request Id").when(mockCommonHeader).getRequestId();
//
//        // test Invalid action
//        upgradeBackoutAction.validate(mockCommonHeader, Action.UpgradeBackout, mockAI, mockPayload);
//        status = (Status) Whitebox.getInternalState(upgradeBackoutAction, "status");
//        Assert.assertEquals("Should return invalid parameter for action",
//                Integer.valueOf(302), status.getCode());
//
//        // test empty ActionIdentifier
//        upgradeBackoutAction.validate(mockCommonHeader, Action.UpgradeBackout, mockAI, mockPayload);
//        status = (Status) Whitebox.getInternalState(upgradeBackoutAction, "status");
//        Assert.assertEquals("should return missing parameter",
//                Integer.valueOf(302), status.getCode());
//
//        // test empty VSERVER_ID
//        Mockito.doReturn("").when(mockAI).getVnfId();
//        upgradeBackoutAction.validate(mockCommonHeader, Action.UpgradeBackout, mockAI, mockPayload);
//        status = (Status) Whitebox.getInternalState(upgradeBackoutAction, "status");
//        Assert.assertEquals("should return invalid parameter",
//                Integer.valueOf(302), status.getCode());
//
//        Mockito.doReturn("vnfId").when(mockAI).getVnfId();
//
//        // test null payload
//        upgradeBackoutAction.validate(mockCommonHeader, Action.UpgradeBackout, mockAI, null);
//       // Mockito.verify(upgradeBackoutAction, times(1)).validateExcludedActIds(any(), any());
//        status = (Status) Whitebox.getInternalState(upgradeBackoutAction, "status");
//        Assert.assertEquals("should return missing parameter",
//                Integer.valueOf(302), status.getCode());
//
//        // test empty payload
//        Mockito.doReturn("").when(mockPayload).getValue();
//        upgradeBackoutAction.validate(mockCommonHeader, Action.UpgradeBackout, mockAI, mockPayload);
//        status = (Status) Whitebox.getInternalState(upgradeBackoutAction, "status");
//        Assert.assertEquals("should return invalid parameter",
//                Integer.valueOf(302), status.getCode());
//
//        // test space payload
//        Mockito.doReturn(" ").when(mockPayload).getValue();
//        upgradeBackoutAction.validate(mockCommonHeader, Action.UpgradeBackout, mockAI, mockPayload);
//        status = (Status) Whitebox.getInternalState(upgradeBackoutAction, "status");
//        Assert.assertEquals("should return invalid parameter",
//                Integer.valueOf(302), status.getCode());
//    }
//
//    @Test
//    public void testValidateForUpgradeSoftwareAction() throws Exception {
//        // test commonHeader error
//        upgradeSoftAction.validate(mockCommonHeader, Action.UpgradeSoftware, mockAI, mockPayload);
//        Status status = (Status) Whitebox.getInternalState(upgradeSoftAction, "status");
//        Assert.assertEquals("should return missing parameter",
//                Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()), status.getCode());
//        //Mockito.verify(upgradeSoftAction, times(0)).buildStatusForParamName(any(), any());
//        //Mockito.verify(upgradeSoftAction, times(0)).buildStatusForErrorMsg(any(), any());
//
//        ZULU mockTimeStamp = mock(ZULU.class);
//        Mockito.doReturn(mockTimeStamp).when(mockCommonHeader).getTimestamp();
//        Mockito.doReturn("api ver").when(mockCommonHeader).getApiVer();
//        Mockito.doReturn("orignator Id").when(mockCommonHeader).getOriginatorId();
//        Mockito.doReturn("request Id").when(mockCommonHeader).getRequestId();
//
//        // test Invalid action
//        upgradeSoftAction.validate(mockCommonHeader, Action.UpgradeSoftware, mockAI, mockPayload);
//        status = (Status) Whitebox.getInternalState(upgradeSoftAction, "status");
//        Assert.assertEquals("Should return invalid parameter for action",
//                Integer.valueOf(302), status.getCode());
//
//        // test empty ActionIdentifier
//        upgradeSoftAction.validate(mockCommonHeader, Action.UpgradeSoftware, mockAI, mockPayload);
//        status = (Status) Whitebox.getInternalState(upgradeSoftAction, "status");
//        Assert.assertEquals("should return missing parameter",
//                Integer.valueOf(302), status.getCode());
//
//        // test empty VSERVER_ID
//        Mockito.doReturn("").when(mockAI).getVnfId();
//        upgradeSoftAction.validate(mockCommonHeader, Action.UpgradeSoftware, mockAI, mockPayload);
//        status = (Status) Whitebox.getInternalState(upgradeSoftAction, "status");
//        Assert.assertEquals("should return invalid parameter",
//                Integer.valueOf(302), status.getCode());
//
//        Mockito.doReturn("vnfId").when(mockAI).getVnfId();
//
//        // test null payload
//        upgradeSoftAction.validate(mockCommonHeader, Action.UpgradeSoftware, mockAI, null);
//        //Mockito.verify(upgradeSoftAction, times(1)).validateExcludedActIds(any(), any());
//        status = (Status) Whitebox.getInternalState(upgradeSoftAction, "status");
//        Assert.assertEquals("should return missing parameter",
//                Integer.valueOf(302), status.getCode());
//
//        // test empty payload
//        Mockito.doReturn("").when(mockPayload).getValue();
//        upgradeSoftAction.validate(mockCommonHeader, Action.UpgradeSoftware, mockAI, mockPayload);
//        status = (Status) Whitebox.getInternalState(upgradeSoftAction, "status");
//        Assert.assertEquals("should return invalid parameter",
//                Integer.valueOf(302), status.getCode());
//
//        // test space payload
//        Mockito.doReturn(" ").when(mockPayload).getValue();
//        upgradeSoftAction.validate(mockCommonHeader, Action.UpgradeSoftware, mockAI, mockPayload);
//        status = (Status) Whitebox.getInternalState(upgradeSoftAction, "status");
//        Assert.assertEquals("should return invalid parameter",
//                Integer.valueOf(302), status.getCode());
//    }
//
//    @Test
//    public void testValidateForUpgradeBackupAction() throws Exception {
//        // test commonHeader error
//        upgradeBackupAction.validate(mockCommonHeader, Action.UpgradeBackup, mockAI, mockPayload);
//        Status status = (Status) Whitebox.getInternalState(upgradeBackupAction, "status");
//        Assert.assertEquals("should return missing parameter",
//                Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()), status.getCode());
//        //Mockito.verify(upgradeBackupAction, times(0)).buildStatusForParamName(any(), any());
//        //Mockito.verify(upgradeBackupAction, times(0)).buildStatusForErrorMsg(any(), any());
//
//        ZULU mockTimeStamp = mock(ZULU.class);
//        Mockito.doReturn(mockTimeStamp).when(mockCommonHeader).getTimestamp();
//        Mockito.doReturn("api ver").when(mockCommonHeader).getApiVer();
//        Mockito.doReturn("orignator Id").when(mockCommonHeader).getOriginatorId();
//        Mockito.doReturn("request Id").when(mockCommonHeader).getRequestId();
//
//        // test Invalid action
//        upgradeBackupAction.validate(mockCommonHeader, Action.UpgradeBackup, mockAI, mockPayload);
//        status = (Status) Whitebox.getInternalState(upgradeBackupAction, "status");
//        Assert.assertEquals("Should return invalid parameter for action",
//                Integer.valueOf(302), status.getCode());
//
//        // test empty ActionIdentifier
//        upgradeBackupAction.validate(mockCommonHeader, Action.UpgradeBackup, mockAI, mockPayload);
//        status = (Status) Whitebox.getInternalState(upgradeBackupAction, "status");
//        Assert.assertEquals("should return missing parameter",
//                Integer.valueOf(302), status.getCode());
//
//        // test empty VSERVER_ID
//        Mockito.doReturn("").when(mockAI).getVnfId();
//        upgradeBackupAction.validate(mockCommonHeader, Action.UpgradeBackup, mockAI, mockPayload);
//        status = (Status) Whitebox.getInternalState(upgradeBackupAction, "status");
//        Assert.assertEquals("should return invalid parameter",
//                Integer.valueOf(302), status.getCode());
//
//        Mockito.doReturn("vnfId").when(mockAI).getVnfId();
//
//        // test null payload
//        upgradeBackupAction.validate(mockCommonHeader, Action.UpgradeBackup, mockAI, null);
//        //Mockito.verify(upgradeBackupAction, times(1)).validateExcludedActIds(any(), any());
//        status = (Status) Whitebox.getInternalState(upgradeBackupAction, "status");
//        Assert.assertEquals("should return missing parameter",
//                Integer.valueOf(302), status.getCode());
//
//        // test empty payload
//        Mockito.doReturn("").when(mockPayload).getValue();
//        upgradeBackupAction.validate(mockCommonHeader, Action.UpgradeBackup, mockAI, mockPayload);
//        status = (Status) Whitebox.getInternalState(upgradeBackupAction, "status");
//        Assert.assertEquals("should return invalid parameter",
//                Integer.valueOf(302), status.getCode());
//
//        // test space payload
//        Mockito.doReturn(" ").when(mockPayload).getValue();
//        upgradeBackupAction.validate(mockCommonHeader, Action.UpgradeBackup, mockAI, mockPayload);
//        status = (Status) Whitebox.getInternalState(upgradeBackupAction, "status");
//        Assert.assertEquals("should return invalid parameter",
//                Integer.valueOf(302), status.getCode());
//    }
}
