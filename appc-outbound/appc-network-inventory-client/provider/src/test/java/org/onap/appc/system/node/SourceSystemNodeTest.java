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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.system.node;


import java.util.HashMap;
import java.util.Map;
import org.jline.utils.Log;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.powermock.api.mockito.PowerMockito;
import org.mockito.Mockito;
import org.onap.appc.aai.utils.AaiClientConstant;
import org.onap.appc.instar.interfaceImpl.InstarRestClientImpl;
import org.onap.appc.instar.interfaceImpl.InterfaceIpAddressImpl;
import org.onap.appc.system.interfaces.RuleHandlerInterface;
import org.onap.appc.instar.utils.InstarClientConstant;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;


public class SourceSystemNodeTest {


        //ONAP Migration

        @Rule
        public ExpectedException expectedEx = ExpectedException.none();

        @Test
        public void testSourceSystemNode() throws Exception {
            SvcLogicContext ctx = new SvcLogicContext();
            String key_content = "{\"name\":\"LOCAL_ACCESS_IP_ADDR\",\"description\":\"this is the node0 tacplus server IP address\",\"type\":"
                    + "\"ipv4_address\",\"required\":true,\"source\":\"INSTAR\",\"rule-type\":\"interface-ip-address\",\"default\":null,\"request-keys\":"
                    + "null,\"response-keys\":[{\"unique-key-name\":\"addressfqdn\",\"unique-key-value\":\"00000000000000\",\"field-key-name\":\"ipaddress-v4\"}]}";
            Map<String, String> inParams = new HashMap<String, String>();
            inParams.put(InstarClientConstant.INPUT_PARAM_RESPONSE_PRIFIX, "");
            inParams.put(InstarClientConstant.INSTAR_KEYS, "LOCAL_ACCESS_IP_ADDR");
            inParams.put("operationName", InstarClientConstant.OPERATION_GET_IPADDRESS_BY_VNF_NAME);
            ctx.setAttribute("INSTAR.LOCAL_ACCESS_IP_ADDR", key_content);
            ctx.setAttribute(InstarClientConstant.INPUT_PARAM_RESPONSE_PRIFIX, "");
            ctx.setAttribute(InstarClientConstant.VNF_NAME, "basx0003v");
            SourceSystemNode icn  = PowerMockito.spy(new SourceSystemNode());
            RuleHandlerInterface mockRuleHandler = Mockito.mock(InterfaceIpAddressImpl.class);
            Mockito.doReturn(mockRuleHandler).when(icn).createHandler(Mockito.any(), Mockito.any());
            Mockito.doNothing().when(mockRuleHandler).processRule();
            icn.getInstarInfo(inParams, ctx);
            Log.info(ctx.getStatus());
            Assert.assertEquals(InstarClientConstant.OUTPUT_STATUS_SUCCESS, ctx.getAttribute("" + InstarClientConstant.OUTPUT_PARAM_STATUS));
        }

        @Test
        public void testSourceSystemNodeFailure() throws Exception {
            SvcLogicContext ctx = new SvcLogicContext();
            String key_content = "{\"name\":\"LOCAL_ACCESS_IP_ADDR\",\"description\":\"this is the node0 tacplus server IP address\",\"type\":"
                    + "\"ipv4_address\",\"required\":true,\"source\":\"INSTAR\",\"rule-type\":\"NON_INTERFACE_IP_ADDRESS\",\"default\":null,\"request-keys\":"
                    + "null,\"response-keys\":[{\"unique-key-name\":\"addressfqdn\",\"unique-key-value\":\"00000000000000\",\"field-key-name\":\"ipaddress-v4\"}]}";
            Log.info(key_content);
            Map<String, String> inParams = new HashMap<String, String>();
            inParams.put(InstarClientConstant.INPUT_PARAM_RESPONSE_PRIFIX, "TEST");
            inParams.put(InstarClientConstant.INSTAR_KEYS, "LOCAL_ACCESS_IP_ADDR");
            inParams.put("operationName", InstarClientConstant.OPERATION_GET_IPADDRESS_BY_VNF_NAME);
            ctx.setAttribute("INSTAR.LOCAL_ACCESS_IP_ADDR", key_content);
            ctx.setAttribute(InstarClientConstant.INPUT_PARAM_RESPONSE_PRIFIX, "TEST");
            ctx.setAttribute(InstarClientConstant.VNF_NAME, "basx0003v");
            SourceSystemNode icn  = new SourceSystemNode();
            expectedEx.expect(SvcLogicException.class);
            expectedEx.expectMessage("No Rule Defined to process :");
            icn.getInstarInfo(inParams, ctx);
        }

        @Test
        public void testInstarData() throws Exception {
             SourceSystemNode icn  = Mockito.spy(new SourceSystemNode());
             SvcLogicContext ctx  = new SvcLogicContext ();
             Map<String, String> inParams = new HashMap<String, String>();
             InstarRestClientImpl mockRestClient = Mockito.mock(InstarRestClientImpl.class);
             Mockito.doReturn(mockRestClient).when(icn).createRestClientInterface(Mockito.any());
             Mockito.doReturn("Test-data").when(mockRestClient).sendRequest(Mockito.anyString());
             inParams.put(InstarClientConstant.VNF_NAME, "basx0003v");
             inParams.put("operationName", InstarClientConstant.OPERATION_GET_IPADDRESS_BY_VNF_NAME);
             icn.getInstarData(inParams, ctx);
             Assert.assertEquals(InstarClientConstant.OUTPUT_STATUS_SUCCESS, ctx.getAttribute("" + InstarClientConstant.OUTPUT_PARAM_STATUS));
             Assert.assertEquals("Test-data", ctx.getAttribute(InstarClientConstant.INSTAR_KEY_VALUES));
        }

        @Test
        public void testInstarDataFailure() throws Exception {
             SourceSystemNode icn  = new SourceSystemNode();
             SvcLogicContext ctx  = new SvcLogicContext ();
             Map<String, String> inParams = new HashMap<String, String>();
             inParams.put(InstarClientConstant.VNF_NAME, "basx0003v");
             inParams.put("operationName", InstarClientConstant.OPERATION_GET_IPADDRESS_BY_VNF_NAME);
             expectedEx.expect(SvcLogicException.class);
             //expectedEx.expectMessage("Cannot find Property file -SDNC_CONFIG_DIR");
             icn.getInstarData(inParams, ctx);
        }

        @Test
        public void  TestGetAaiInfo() throws Exception {
            SourceSystemNode aaiNode=new SourceSystemNode();
            Map<String, String> inParams=new HashMap<String, String> ();
            SvcLogicContext ctx=new SvcLogicContext();
            String keyVals = new String("[\"vnf_name\"]");
            inParams.put("aaiKeys",keyVals);
            inParams.put("responsePrefix","test");
            String parameterString="{\"name\":\"vnf_name\",\"description\":null,\"type\":null,\"required\":false,\"source\":\"A&AI\","
                    + "\"rule-type\":\"vnf-name\",\"default\":null,\"request-keys\":null,\"response-keys\":[{\"unique-key-name\":\"parent-name\","
                    + "\"unique-key-value\":\"vnf\",\"field-key-name\":\"vnf-name\",\"filter-by-field\":null,\"filter-by-value\":null}]}";
            ctx.setAttribute(AaiClientConstant.SOURCE_SYSTEM_AAI + "." +  "vnf_name",parameterString);
            aaiNode.getAaiInfo(inParams, ctx);
            Assert.assertEquals(InstarClientConstant.OUTPUT_STATUS_SUCCESS, ctx.getAttribute("test." + InstarClientConstant.OUTPUT_PARAM_STATUS));
        }

        @Test
        public void  TestGetAaiInfoFailure() throws Exception {
            SourceSystemNode aaiNode=new SourceSystemNode();
            Map<String, String> inParams= Mockito.spy(new HashMap<String, String> ());
            inParams.put(InstarClientConstant.INPUT_PARAM_RESPONSE_PRIFIX, "");
            Mockito.doThrow(new RuntimeException("Test-exception")).when(inParams).get(AaiClientConstant.AAI_KEYS);
            SvcLogicContext ctx=new SvcLogicContext();
            expectedEx.expect(SvcLogicException.class);
            expectedEx.expectMessage("Test-exception");
            aaiNode.getAaiInfo(inParams, ctx);
        }

}

