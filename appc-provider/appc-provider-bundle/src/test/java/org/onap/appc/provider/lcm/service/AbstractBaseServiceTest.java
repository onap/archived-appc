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
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.Action;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.ZULU;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.action.identifiers.ActionIdentifiers;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.common.header.CommonHeader;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.status.Status;
import org.onap.appc.executor.objects.LCMCommandStatus;
import org.onap.appc.executor.objects.Params;
import org.onap.appc.requesthandler.objects.RequestHandlerOutput;
import org.powermock.reflect.Whitebox;

import java.util.EnumSet;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class AbstractBaseServiceTest {
    private Action expectedAction = Action.Query;
    private String rpcName = expectedAction.name().toLowerCase();

    private CommonHeader commonHeader = mock(CommonHeader.class);
    private ActionIdentifiers mockAI = mock(ActionIdentifiers.class);
    private testAbc testAbstractBaseService;

    class testAbc extends AbstractBaseService {
        public testAbc() {
            super(AbstractBaseServiceTest.this.expectedAction);
        }
    }

    @Before
    public void setUp() throws Exception {
        testAbstractBaseService = spy(new testAbc());
    }

    @Test
    public void testConstructor() throws Exception {
        Assert.assertEquals("Should have proper ACTION", expectedAction,
            (Action) Whitebox.getInternalState(testAbstractBaseService, "expectedAction"));
        Assert.assertEquals("Should have action-status RPC name", rpcName,
            (Whitebox.getInternalState(testAbstractBaseService, "rpcName")).toString());
    }

    @Test
    public void testValidateInput() throws Exception {
        // test commonHeader error
        Status status = testAbstractBaseService.validateInput(commonHeader, Action.Query, null);
        Assert.assertEquals("should return missing parameter",
            Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()), status.getCode());

        ZULU mockTimeStamp = mock(ZULU.class);
        Mockito.doReturn(mockTimeStamp).when(commonHeader).getTimestamp();
        Mockito.doReturn("api ver").when(commonHeader).getApiVer();
        Mockito.doReturn("originator Id").when(commonHeader).getOriginatorId();
        Mockito.doReturn("request Id").when(commonHeader).getRequestId();

        // test invalid action
        status = testAbstractBaseService.validateInput(commonHeader, Action.AttachVolume, null);
        Assert.assertEquals("Should return invalid parameter for action",
            Integer.valueOf(LCMCommandStatus.INVALID_INPUT_PARAMETER.getResponseCode()), status.getCode());

        // test null actionIdentifier
        status = testAbstractBaseService.validateInput(commonHeader, Action.Query, null);
        Assert.assertEquals("should return missing parameter",
            Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()), status.getCode());

        // test validation passed
        status = testAbstractBaseService.validateInput(commonHeader, Action.Query, mockAI);
        Assert.assertTrue("Should have null status", status == null);
    }

    @Test
    public void testValidateVnfId() throws Exception {
        // Skip test input validation, as it is all done in testValidateInput

        ZULU mockTimeStamp = mock(ZULU.class);
        Mockito.doReturn(mockTimeStamp).when(commonHeader).getTimestamp();
        Mockito.doReturn("api ver").when(commonHeader).getApiVer();
        Mockito.doReturn("originator Id").when(commonHeader).getOriginatorId();
        Mockito.doReturn("request Id").when(commonHeader).getRequestId();

        // test null VNF ID
        Status status = testAbstractBaseService.validateVnfId(commonHeader, Action.Query, mockAI);
        Assert.assertEquals("should return invalid parameter",
            Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()), status.getCode());

        // test empty VNF_ID
        Mockito.doReturn("").when(mockAI).getVnfId();
        status = testAbstractBaseService.validateVnfId(commonHeader, Action.Query, mockAI);
        Assert.assertEquals("should return invalid parameter",
            Integer.valueOf(LCMCommandStatus.INVALID_INPUT_PARAMETER.getResponseCode()), status.getCode());

        // test calling validateExcludeActId
        Mockito.doReturn("vnfId").when(mockAI).getVnfId();
        status = testAbstractBaseService.validateVnfId(commonHeader, Action.Query, mockAI);
        Assert.assertTrue("Should have null status", status == null);
    }

    @Test
    public void testValidateVserverId() throws Exception {
        // Skip test input validation, as it is all done in testValidateInput

        ZULU mockTimeStamp = mock(ZULU.class);
        Mockito.doReturn(mockTimeStamp).when(commonHeader).getTimestamp();
        Mockito.doReturn("api ver").when(commonHeader).getApiVer();
        Mockito.doReturn("originator Id").when(commonHeader).getOriginatorId();
        Mockito.doReturn("request Id").when(commonHeader).getRequestId();

        // test null VNF ID
        Status status = testAbstractBaseService.validateVserverId(commonHeader, Action.Query, mockAI);
        Assert.assertEquals("should return invalid parameter",
            Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()), status.getCode());

        // test empty VNF_ID
        Mockito.doReturn("").when(mockAI).getVserverId();
        status = testAbstractBaseService.validateVserverId(commonHeader, Action.Query, mockAI);
        Assert.assertEquals("should return invalid parameter",
            Integer.valueOf(LCMCommandStatus.INVALID_INPUT_PARAMETER.getResponseCode()), status.getCode());

        // test calling validateExcludeActId
        Mockito.doReturn("vserverId").when(mockAI).getVserverId();
        status = testAbstractBaseService.validateVserverId(commonHeader, Action.Query, mockAI);
        Assert.assertTrue("Should have null status", status == null);
    }

    @Test
    public void testValidateExcludedActIds() throws Exception {
        EnumSet<AbstractBaseService.ACTID_KEYS> exclutionKeys = EnumSet.of(AbstractBaseService.ACTID_KEYS.VNF_ID);
        Status status = testAbstractBaseService.validateExcludedActIds(mockAI, exclutionKeys);
        Assert.assertTrue("Should have not error", status  == null);

        Integer expectedErrorCode = Integer.valueOf(LCMCommandStatus.INVALID_INPUT_PARAMETER.getResponseCode());
        Mockito.doReturn("vnfc name").when(mockAI).getVnfcName();
        status = testAbstractBaseService.validateExcludedActIds(mockAI, exclutionKeys);
        Assert.assertEquals("Should have error for vnfc name", expectedErrorCode, status.getCode());

        Mockito.doReturn(null).when(mockAI).getVnfcName();
        Mockito.doReturn("vserver Id").when(mockAI).getVserverId();
        status = testAbstractBaseService.validateExcludedActIds(mockAI, exclutionKeys);
        Assert.assertEquals("Should have error for vserver Id", expectedErrorCode, status.getCode());

        Mockito.doReturn(null).when(mockAI).getVserverId();
        Mockito.doReturn("vf module Id").when(mockAI).getVfModuleId();
        status = testAbstractBaseService.validateExcludedActIds(mockAI, exclutionKeys);
        Assert.assertEquals("Should have error for vf module Id", expectedErrorCode, status.getCode());

        Mockito.doReturn(null).when(mockAI).getServiceInstanceId();
        Mockito.doReturn("service instance Id").when(mockAI).getServiceInstanceId();
        status = testAbstractBaseService.validateExcludedActIds(mockAI, exclutionKeys);
        Assert.assertEquals("Should have error for service instance Id", expectedErrorCode, status.getCode());

        Mockito.doReturn("vnfc name").when(mockAI).getVnfcName();
        Mockito.doReturn("vserver Id").when(mockAI).getVserverId();
        Mockito.doReturn("vf module Id").when(mockAI).getVfModuleId();
        Mockito.doReturn("vnf Id").when(mockAI).getVnfId();
        status = testAbstractBaseService.validateExcludedActIds(mockAI, exclutionKeys);
        Assert.assertEquals("Should have error code", expectedErrorCode, status.getCode());
        Assert.assertEquals("Should have error message",
            LCMCommandStatus.INVALID_INPUT_PARAMETER.getFormattedMessage(getMsgParams(exclutionKeys)),
            status.getMessage());
    }

    @Test
    public void testExecuteAction() throws Exception {
        RequestHandlerOutput output = testAbstractBaseService.executeAction(null);
        Assert.assertTrue("Should return null RequestHandlerOutput", output == null);
        Status status = Whitebox.getInternalState(testAbstractBaseService, "status");
        Assert.assertEquals("Should have error code",
            Integer.valueOf(LCMCommandStatus.UNEXPECTED_ERROR.getResponseCode()), status.getCode());
    }

    private Params getMsgParams(EnumSet<AbstractBaseService.ACTID_KEYS> exclutionKeys) {
        StringBuilder msgBuilder = new StringBuilder();
        for (QueryService.ACTID_KEYS aKey : AbstractBaseService.ACTID_KEYS.values()) {
            if (exclutionKeys.contains(aKey)) {
                continue;
            }
            msgBuilder.append(aKey.getKeyName()).append(testAbstractBaseService.DELIMITER_COMMA);
        }
        String msg = msgBuilder.toString();
        return new Params().addParam("errorMsg", msg.substring(0, msg.length() -1));
    }
}
