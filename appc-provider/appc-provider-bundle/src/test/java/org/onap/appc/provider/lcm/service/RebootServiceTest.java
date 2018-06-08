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
import org.mockito.Mock;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.Action;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.Payload;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.RebootInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.RebootOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.ZULU;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.action.identifiers.ActionIdentifiers;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.status.Status;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static org.mockito.Mockito.times;

@RunWith(PowerMockRunner.class)
public class RebootServiceTest {
    private final String vserverId = "vserverId";

    @Mock
    private RebootService rebootService;
    @Mock
    private RebootInput rebootInput;
    @Mock
    private ActionIdentifiers actionIdentifiers;
    @Mock
    private org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.common.header.CommonHeader commonHeader;

    private Payload payload;

    @Before
    public void setUp() throws Exception {
        rebootService = new RebootService();
        payload = new Payload("{\"reboot-type\":\"hard\"}");

        PowerMockito.doReturn(actionIdentifiers).when(rebootInput).getActionIdentifiers();
        PowerMockito.doReturn(payload).when(rebootInput).getPayload();
        PowerMockito.doReturn(commonHeader).when(rebootInput).getCommonHeader();
        PowerMockito.doReturn(new ZULU("2017-09-05T16:55:55.807Z")).when(commonHeader).getTimestamp();
        PowerMockito.doReturn("2.00").when(commonHeader).getApiVer();
        PowerMockito.doReturn("demo-lcm-stop-id#1").when(commonHeader).getRequestId();
        PowerMockito.doReturn("demo-lcm-stop-id#2").when(commonHeader).getSubRequestId();
        PowerMockito.doReturn("originator-id").when(commonHeader).getOriginatorId();
        PowerMockito.doReturn(Action.Reboot).when(rebootInput).getAction();
    }

    @Test
    public void testConstructor() throws Exception {
        Action expectedAction = Action.Reboot;
        Assert.assertEquals("Should have proper ACTION", expectedAction,
            (Action) org.powermock.reflect.Whitebox.getInternalState(rebootService, "expectedAction"));
        Assert.assertEquals("Should have reboot RPC name", expectedAction.name().toLowerCase(),
            (org.powermock.reflect.Whitebox.getInternalState(rebootService, "rpcName")).toString());
    }

    @Test
    public void testProcessAccepted() throws Exception {
        PowerMockito.doReturn(vserverId).when(actionIdentifiers).getVserverId();
        RebootOutputBuilder process = rebootService.process(rebootInput);
        PowerMockito.verifyPrivate(rebootService, times(1)).invoke("validate", rebootInput);
        Assert.assertNotNull(process.getCommonHeader());
        Assert.assertNotNull(process.getStatus());
    }

    @Test
    public void testProcessError() throws Exception {
        RebootOutputBuilder process = rebootService.process(rebootInput);
        PowerMockito.verifyPrivate(rebootService, times(1))
            .invoke("validate", rebootInput);
        Assert.assertNotNull(process.getStatus());
    }

    @Test
    public void testValidateSuccess() throws Exception {
        PowerMockito.doReturn(vserverId).when(actionIdentifiers).getVserverId();
        Status validate = Whitebox.invokeMethod(rebootService, "validate", rebootInput);
        Assert.assertNull(validate);
    }

    @Test
    public void testValidateMissingVserverId() throws Exception {
        PowerMockito.doReturn("").when(actionIdentifiers).getVserverId();
        Whitebox.invokeMethod(rebootService, "validate", rebootInput);
        Status status = Whitebox.getInternalState(rebootService, "status");
        Assert.assertNotNull(status);
    }

    @Test
    public void testValidateWrongAction() throws Exception {
        PowerMockito.doReturn(Action.Audit).when(rebootInput).getAction();
        PowerMockito.doReturn("").when(actionIdentifiers).getVserverId();
        Whitebox.invokeMethod(rebootService, "validate", rebootInput);
        Status status = Whitebox.getInternalState(rebootService, "status");
        Assert.assertNotNull(status);
    }

    @Test
    public void testValidateMissingActionIdentifier() throws Exception {
        PowerMockito.doReturn(actionIdentifiers).when(rebootInput).getActionIdentifiers();
        Whitebox.invokeMethod(rebootService, "validate", rebootInput);
        Status status = Whitebox.getInternalState(rebootService, "status");
        Assert.assertNotNull(status);
    }

    @Test
    public void testValidateMissingRebootType() throws Exception {
        Payload payload = new Payload("{}");
        PowerMockito.doReturn(payload).when(rebootInput).getPayload();
        PowerMockito.doReturn(vserverId).when(actionIdentifiers).getVserverId();
        Whitebox.invokeMethod(rebootService, "validate", rebootInput);
        Status status = Whitebox.getInternalState(rebootService, "status");
        Assert.assertNotNull(status);
    }

    @Test
    public void testValidateWrongRebootType() throws Exception {
        Payload payload = new Payload("{\"reboot-type\":\"1\"}");
        PowerMockito.doReturn(payload).when(rebootInput).getPayload();
        PowerMockito.doReturn(vserverId).when(actionIdentifiers).getVserverId();
        Whitebox.invokeMethod(rebootService, "validate", rebootInput);
        Status status = Whitebox.getInternalState(rebootService, "status");
        Assert.assertNotNull(status);
    }
}
