/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 Ericsson
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

package org.onap.appc.messageadapter.impl;

import static org.junit.Assert.assertTrue;

import javax.ws.rs.core.Response.Status;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.appc.domainmodel.lcm.CommonHeader;
import org.onap.appc.domainmodel.lcm.ResponseContext;
import org.onap.appc.domainmodel.lcm.VNFOperation;
import org.onap.appc.srvcomm.messaging.MessagingConnector;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.core.JsonProcessingException;

public class MessageAdapterImplTest {

    private final MessagingConnector connector = Mockito.mock(MessagingConnector.class);
    private static final EELFLogger logger = EELFManager.getInstance().getLogger(MessageAdapterImpl.class);
    private final MessageAdapterImpl impl = new MessageAdapterImpl();
    
    @Before
    public void setUp() {
        impl.init(connector);
    }

    @Test
    public void testSuccess() throws JsonProcessingException {

        ResponseContext context = new ResponseContext();
        context.setPayload("payload");
        org.onap.appc.domainmodel.lcm.Status status = new org.onap.appc.domainmodel.lcm.Status();
        status.setCode(200);
        status.setMessage("success");
        context.setStatus(status);
        CommonHeader commonHeader = new CommonHeader();
        commonHeader.setRequestId("test123");
        commonHeader.setSubRequestId("test456");
        context.setCommonHeader(commonHeader);
        Mockito.when(connector.publishMessage(Mockito.any(), Mockito.any(),Mockito.any())).thenReturn(true);
        assertTrue(impl.post(VNFOperation.Start, "test", context));
    }

}
