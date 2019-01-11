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
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.*;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.action.identifiers.ActionIdentifiers;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.common.header.CommonHeader;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.status.Status;
import org.onap.appc.executor.objects.LCMCommandStatus;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;


public class UpgradeServiceTest {
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

    @Test
    public void testValidateForPreCheckAction() throws Exception {
        // test commonHeader error
        upgradePreAction.validate(mockCommonHeader, Action.UpgradePreCheck, mockAI, mockPayload);
        Status status = (Status) Whitebox.getInternalState(upgradePreAction, "status");
        Assert.assertEquals("should return missing parameter",
            Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()), status.getCode());

        ZULU mockTimeStamp = mock(ZULU.class);
        Mockito.doReturn(mockTimeStamp).when(mockCommonHeader).getTimestamp();
        Mockito.doReturn("api ver").when(mockCommonHeader).getApiVer();
        Mockito.doReturn("orignator Id").when(mockCommonHeader).getOriginatorId();
        Mockito.doReturn("request Id").when(mockCommonHeader).getRequestId();

        // test Invalid action
        Mockito.doReturn(null).when(upgradePreAction).validateVnfId(Mockito.any(CommonHeader.class), 
                Mockito.any(Action.class), Mockito.any(ActionIdentifiers.class));
        upgradePreAction.validate(mockCommonHeader, Action.UpgradePreCheck, mockAI, mockPayload);
        status = (Status) Whitebox.getInternalState(upgradePreAction, "status");
        Assert.assertEquals("Should return invalid parameter for action",
            Integer.valueOf(302), status.getCode());

        // test empty ActionIdentifier
        upgradePreAction.validate(mockCommonHeader, Action.UpgradePreCheck, mockAI, mockPayload);
        status = (Status) Whitebox.getInternalState(upgradePreAction, "status");
        Assert.assertEquals("should return missing parameter",
            Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()), status.getCode());

        // test empty VSERVER_ID
        Mockito.doReturn("").when(mockAI).getVnfId();
        upgradePreAction.validate(mockCommonHeader, Action.UpgradePreCheck, mockAI, mockPayload);
        status = (Status) Whitebox.getInternalState(upgradePreAction, "status");
        Assert.assertEquals("should return invalid parameter",
            Integer.valueOf(302), status.getCode());

        Mockito.doReturn("vnfId").when(mockAI).getVnfId();

        // test null payload
        upgradePreAction.validate(mockCommonHeader, Action.UpgradePreCheck, mockAI, null);
        status = (Status) Whitebox.getInternalState(upgradePreAction, "status");
        Assert.assertEquals("should return missing parameter",
            Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()), status.getCode());

        // test empty payload
        Mockito.doReturn("").when(mockPayload).getValue();
        upgradePreAction.validate(mockCommonHeader, Action.UpgradePreCheck, mockAI, mockPayload);
        status = (Status) Whitebox.getInternalState(upgradePreAction, "status");
        Assert.assertEquals("should return invalid parameter",
            Integer.valueOf(301), status.getCode());

        // test space payload
        Mockito.doReturn(null).when(upgradePreAction).validateMustHaveParamValue(Mockito.anyString(),
                Mockito.anyString());
        Mockito.doReturn(" ").when(mockPayload).getValue();
        upgradePreAction.validate(mockCommonHeader, Action.UpgradePreCheck, mockAI, mockPayload);
        status = (Status) Whitebox.getInternalState(upgradePreAction, "status");
        Assert.assertEquals("should return invalid parameter",
                Integer.valueOf(LCMCommandStatus.UNEXPECTED_ERROR.getResponseCode()), status.getCode());
    }
}
