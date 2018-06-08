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
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.status.Status;
import org.onap.appc.domainmodel.lcm.ResponseContext;
import org.onap.appc.executor.objects.LCMCommandStatus;
import org.onap.appc.executor.objects.Params;
import org.onap.appc.requesthandler.objects.RequestHandlerOutput;

import java.text.ParseException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AbstractBaseUtilsTest {

    private AbstractBaseUtilsTest.testAbc testAbstractBaseUtils;

    class testAbc extends AbstractBaseUtils {
        // no content needed
    }

    @Before
    public void setUp() throws Exception {
        testAbstractBaseUtils = spy(new AbstractBaseUtilsTest.testAbc());
    }

    private void validateStatusResult(Params params, LCMCommandStatus lcmCommandStatus, Status status) {
        Assert.assertEquals(String.format("Should return proper code of %s", lcmCommandStatus.toString()),
            Integer.valueOf(lcmCommandStatus.getResponseCode()), status.getCode());
        Assert.assertEquals(String.format("Should return proper message of $s", lcmCommandStatus.toString()),
            lcmCommandStatus.getFormattedMessage(params), status.getMessage());
    }

    @Test
    public void testBuildStatusWithoutParams() throws Exception {
        LCMCommandStatus lcmCommandStatus = LCMCommandStatus.ACCEPTED;
        Status status = testAbstractBaseUtils.buildStatusWithoutParams(lcmCommandStatus);
        validateStatusResult(null, lcmCommandStatus, status);
    }

    @Test
    public void testBuildStatusForErrorMsg() throws Exception {
        String key = "errorMsg";
        String msg = "testing";
        Params params = new Params().addParam(key, msg);
        LCMCommandStatus lcmCommandStatus = LCMCommandStatus.INVALID_INPUT_PARAMETER;
        Status status = testAbstractBaseUtils.buildStatusForErrorMsg(lcmCommandStatus, msg);
        verify(testAbstractBaseUtils, times(1)).buildStatus(lcmCommandStatus, msg, key);
        validateStatusResult(params, lcmCommandStatus, status);
    }


    @Test
    public void testBuildStatusForVnfId() throws Exception {
        String key = "vnfId";
        String msg = "testing";
        Params params = new Params().addParam(key, msg);
        LCMCommandStatus lcmCommandStatus = LCMCommandStatus.VNF_NOT_FOUND;
        Status status = testAbstractBaseUtils.buildStatusForVnfId(lcmCommandStatus, msg);
        verify(testAbstractBaseUtils, times(1)).buildStatus(lcmCommandStatus, msg, key);
        validateStatusResult(params, lcmCommandStatus, status);
    }

    @Test
    public void testBuildStatusForParamName() throws Exception {
        String key = "paramName";
        String msg = "testing";
        Params params = new Params().addParam(key, msg);
        LCMCommandStatus lcmCommandStatus = LCMCommandStatus.MISSING_MANDATORY_PARAMETER;
        Status status = testAbstractBaseUtils.buildStatusForParamName(lcmCommandStatus, msg);
        verify(testAbstractBaseUtils, times(1)).buildStatus(lcmCommandStatus, msg, key);
        validateStatusResult(params, lcmCommandStatus, status);
    }

    @Test
    public void testBuildStatusForId() throws Exception {
        String key = "id";
        String msg = "testing";
        Params params = new Params().addParam(key, msg);
        LCMCommandStatus lcmCommandStatus = LCMCommandStatus.VSERVER_NOT_FOUND;
        Status status = testAbstractBaseUtils.buildStatusForId(lcmCommandStatus, msg);
        verify(testAbstractBaseUtils, times(1)).buildStatus(lcmCommandStatus, msg, key);
        validateStatusResult(params, lcmCommandStatus, status);
    }

    @Test
    public void testBuildStatus() throws Exception {
        String key = "errorMsg";
        String msg = "testing";
        Params params = new Params().addParam(key, msg);
        LCMCommandStatus lcmCommandStatus = LCMCommandStatus.UNEXPECTED_ERROR;
        Status status = testAbstractBaseUtils.buildStatus(lcmCommandStatus, msg, key);
        validateStatusResult(params, lcmCommandStatus, status);

        key = "vnfId";
        params = new Params().addParam(key, msg);
        lcmCommandStatus = LCMCommandStatus.VNF_NOT_FOUND;
        status = testAbstractBaseUtils.buildStatus(lcmCommandStatus, msg, key);
        validateStatusResult(params, lcmCommandStatus, status);

        key = "paramName";
        params = new Params().addParam(key, msg);
        lcmCommandStatus = LCMCommandStatus.MISSING_MANDATORY_PARAMETER;
        status = testAbstractBaseUtils.buildStatus(lcmCommandStatus, msg, key);
        validateStatusResult(params, lcmCommandStatus, status);
    }

    @Test
    public void testBuildStatusWithParseException() throws Exception {
        LCMCommandStatus lcmCommandStatus = LCMCommandStatus.REQUEST_PARSING_FAILED;
        String key = "errorMsg";
        String exceptionMsg = null;

        ParseException parseException = new ParseException(exceptionMsg, 0);
        Params params = new Params().addParam(key, parseException.toString());
        Status status = testAbstractBaseUtils.buildStatusWithParseException(parseException);
        validateStatusResult(params, lcmCommandStatus, status);

        exceptionMsg = "testing message";
        parseException = new ParseException(exceptionMsg, 0);
        params = new Params().addParam(key, exceptionMsg);
        status = testAbstractBaseUtils.buildStatusWithParseException(parseException);
        validateStatusResult(params, lcmCommandStatus, status);
    }

    @Test
    public void testBuildStatusWithDispatcherOutput() throws Exception {
        RequestHandlerOutput mockOutput = mock(RequestHandlerOutput.class);

        ResponseContext mockContext = mock(ResponseContext.class);
        Mockito.doReturn(mockContext).when(mockOutput).getResponseContext();

        org.onap.appc.domainmodel.lcm.Status mockStatus = mock(org.onap.appc.domainmodel.lcm.Status.class);
        Mockito.doReturn(mockStatus).when(mockContext).getStatus();

        Integer resultCode = new Integer(401);
        String resultMsg = "testing result message";
        Mockito.doReturn(resultCode).when(mockStatus).getCode();
        Mockito.doReturn(resultMsg).when(mockStatus).getMessage();

        Status status = testAbstractBaseUtils.buildStatusWithDispatcherOutput(mockOutput);
        Assert.assertEquals("Should return result code", resultCode, status.getCode());
        Assert.assertEquals("Should return result message", resultMsg, status.getMessage());
    }

    @Test
    public void testGetRpcName() throws Exception {
        Assert.assertEquals("Should return action-status",
            "action-status", testAbstractBaseUtils.getRpcName(Action.ActionStatus));
        Assert.assertEquals("Should return query",
            "query", testAbstractBaseUtils.getRpcName(Action.Query));
        Assert.assertEquals("Should return reboot",
            "reboot", testAbstractBaseUtils.getRpcName(Action.Reboot));
        Assert.assertEquals("Should return attach-volume",
            "attach-volume", testAbstractBaseUtils.getRpcName(Action.AttachVolume));
        Assert.assertEquals("Should return detach-volume",
            "detach-volume", testAbstractBaseUtils.getRpcName(Action.DetachVolume));
        Assert.assertEquals("Should return quiesce-traffic",
            "quiesce-traffic", testAbstractBaseUtils.getRpcName(Action.QuiesceTraffic));
        Assert.assertEquals("Should return resume-traffic",
                "resume-traffic", testAbstractBaseUtils.getRpcName(Action.ResumeTraffic));
        Assert.assertEquals("Should return upgrade-pre-check",
                "upgrade-pre-check", testAbstractBaseUtils.getRpcName(Action.UpgradePreCheck));
        Assert.assertEquals("Should return upgrade-post-check",
                "upgrade-post-check", testAbstractBaseUtils.getRpcName(Action.UpgradePostCheck));
        Assert.assertEquals("Should return upgrade-software",
                "upgrade-software", testAbstractBaseUtils.getRpcName(Action.UpgradeSoftware));
        Assert.assertEquals("Should return upgrade-backup",
                "upgrade-backup", testAbstractBaseUtils.getRpcName(Action.UpgradeBackup));
        Assert.assertEquals("Should return upgrade-backout",
                "upgrade-backout", testAbstractBaseUtils.getRpcName(Action.UpgradeBackout));
    }
}
