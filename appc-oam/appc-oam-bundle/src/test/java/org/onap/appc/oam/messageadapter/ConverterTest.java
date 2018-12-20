/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 Ericsson
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

package org.onap.appc.oam.messageadapter;

import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.appc.oam.AppcOam.RPC;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.common.header.CommonHeader;
import com.fasterxml.jackson.core.JsonProcessingException;



public class ConverterTest {

    private OAMContext oamContext = Mockito.spy(new OAMContext());

    @Before
    public void setup() {
        org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.status.StatusBuilder statusBuilder = 
                new org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.status.StatusBuilder();
        statusBuilder.setCode(1);
        statusBuilder.setMessage("MESSAGE");
        oamContext.setStatus(statusBuilder.build());
        org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.common.header.CommonHeaderBuilder commonHeaderBuilder =
                new org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.common.header.CommonHeaderBuilder();
        commonHeaderBuilder.setRequestId("REQUEST_ID");
        CommonHeader commonHeader = commonHeaderBuilder.build();
        oamContext.setCommonHeader(commonHeader);
    }

    @Test
    public void testStop() throws JsonProcessingException {
        oamContext.setRpcName(RPC.stop);
        assertTrue(Converter.convAsyncResponseToUebOutgoingMessageJsonString(oamContext).contains("\"rpc-name\":\"stop\""));
    }

    @Test
    public void testStart() throws JsonProcessingException {
        oamContext.setRpcName(RPC.start);
        assertTrue(Converter.convAsyncResponseToUebOutgoingMessageJsonString(oamContext).contains("\"rpc-name\":\"start\""));
    }

    @Test
    public void testRestart() throws JsonProcessingException {
        oamContext.setRpcName(RPC.restart);
        assertTrue(Converter.convAsyncResponseToUebOutgoingMessageJsonString(oamContext).contains("\"rpc-name\":\"restart\""));
    }

    @Test
    public void testMmode() throws JsonProcessingException {
        oamContext.setRpcName(RPC.maintenance_mode);
        assertTrue(Converter.convAsyncResponseToUebOutgoingMessageJsonString(oamContext).contains("\"rpc-name\":\"maintenance_mode\""));
    }

}
