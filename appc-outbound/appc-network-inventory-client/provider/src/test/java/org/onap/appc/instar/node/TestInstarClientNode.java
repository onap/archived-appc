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

package org.onap.appc.instar.node;

import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.HttpMethod;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.onap.appc.aai.utils.AaiClientConstant;
import org.onap.appc.instar.dme2client.SecureRestClientTrustManager;
import org.onap.appc.instar.utils.InstarClientConstant;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;


public class TestInstarClientNode {

    //ONAP Migration

    @Test(expected=Exception.class)
    public void testInstarClientNode() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        String key_conetent = IOUtils.toString(TestInstarClientNode.class.getClassLoader().getResourceAsStream("templates/sampleKeyContents"), Charset.defaultCharset());
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(InstarClientConstant.INPUT_PARAM_RESPONSE_PRIFIX, "TEST");
        inParams.put(InstarClientConstant.INSTAR_KEYS, "LOCAL_ACCESS_IP_ADDR");
        inParams.put("operationName", InstarClientConstant.OPERATION_GET_IPADDRESS_BY_VNF_NAME);
        ctx.setAttribute("INSTAR.LOCAL_ACCESS_IP_ADDR", key_conetent);
        ctx.setAttribute(InstarClientConstant.INPUT_PARAM_RESPONSE_PRIFIX, "TEST");
        ctx.setAttribute(InstarClientConstant.VNF_NAME, "basx0003v");
        InstarClientNode icn  = new InstarClientNode();
        icn.getInstarInfo(inParams, ctx);
        String address = (new JSONObject(ctx.getAttribute("TEST." + InstarClientConstant.INSTAR_KEY_VALUES))).getString("LOCAL_ACCESS_IP_ADDR");
    }
    @Test(expected=Exception.class)
    public void testInstarData() throws Exception {
        InstarClientNode inNode = new InstarClientNode();
         SvcLogicContext ctx  = new SvcLogicContext ();
         Map<String, String> inParams = new HashMap<String, String>();

         inParams.put(InstarClientConstant.VNF_NAME, "basx0003v");
         inParams.put("operationName", InstarClientConstant.OPERATION_GET_IPADDRESS_BY_VNF_NAME);
         inNode.getInstarData(inParams, ctx);
        }

    @Test
    public void  TestGetAaiInfo() throws Exception {
        InstarClientNode aaiNode=new InstarClientNode();
        Map<String, String> inParams=new HashMap<String, String> ();
        SvcLogicContext ctx=new SvcLogicContext();
        String keyVals=new String("[\"LOCAL_CORE_ALT_IP_ADDR\",\"REMOTE_ACCESS_IP_ADDR\",\"PARAMETER3\",\"PARAMETER4\"]");
        inParams.put("aaiKeys",keyVals);
        inParams.put("responsePrefix","test");
        String yamlParameterString=IOUtils.toString(TestInstarClientNode.class.getClassLoader().getResourceAsStream("./YamlParameter.txt"), Charset.defaultCharset());
        ctx.setAttribute(AaiClientConstant.SOURCE_SYSTEM_AAI + "." +  "LOCAL_CORE_ALT_IP_ADDR",yamlParameterString);
        String yamlParameterString2=IOUtils.toString(TestInstarClientNode.class.getClassLoader().getResourceAsStream("./YamlParameter2.txt"), Charset.defaultCharset());
        ctx.setAttribute(AaiClientConstant.SOURCE_SYSTEM_AAI + "." +  "REMOTE_ACCESS_IP_ADDR",yamlParameterString2);
        String yamlParameterString3=IOUtils.toString(TestInstarClientNode.class.getClassLoader().getResourceAsStream("./YamlParameter3.txt"), Charset.defaultCharset());
        ctx.setAttribute(AaiClientConstant.SOURCE_SYSTEM_AAI + "." +  "PARAMETER3",yamlParameterString3);
        String yamlParameterString4=IOUtils.toString(TestInstarClientNode.class.getClassLoader().getResourceAsStream("./YamlParameter4.txt"), Charset.defaultCharset());
        ctx.setAttribute(AaiClientConstant.SOURCE_SYSTEM_AAI + "." +  "PARAMETER4",yamlParameterString4);
        stubAaiVnfInfoData(ctx);
        aaiNode.getAaiInfo(inParams, ctx);
        String [] valToCompare={"\"LOCAL_CORE_ALT_IP_ADDR\":\"testVnf\"",
                                "\"REMOTE_ACCESS_IP_ADDR\":\"testVnfc2,testVnfc3\"",
                                "\"PARAM3\":\"testVnfcIpv4Address1\"",
                                "\"PARAM4\":\"server1,server2,server3\""};
        String value=ctx.getAttribute("test."+AaiClientConstant.AAI_KEY_VALUES);
        boolean pass=false;
        for (int i=0;i<valToCompare.length;i++) {
            if (!StringUtils.contains(value,valToCompare[i] )) {
                //System.out.println(value+"....... "+valToCompare[i].toString());
                pass=false;
                break;
            }
            else {
                pass=true;
            }
        }
        assertTrue(pass);
    }

    public void stubAaiVnfInfoData(SvcLogicContext context) {

        context.setAttribute("tmp.vnfInfo.vm-count","3");
        context.setAttribute("tmp.vnfInfo.vnf.vnf-name","testVnf");
        context.setAttribute("tmp.vnfInfo.vnf.vnf-oam-ipv4-address","test-ipv4-address");
        context.setAttribute("tmp.vnfInfo.vm[0].vserver-name","server1");
        context.setAttribute("tmp.vnfInfo.vm[0].vserver-id","serverId1");
        context.setAttribute("tmp.vnfInfo.vm[0].vnfc-count","1");
        context.setAttribute("tmp.vnfInfo.vm[0].vnfc-name","testVnfc1");
        context.setAttribute("tmp.vnfInfo.vm[0].vnfc-function-code","msc");
        context.setAttribute("tmp.vnfInfo.vm[0].vnfc-ipaddress-v4-oam-vip","testVnfcIpv4Address1");

        context.setAttribute("tmp.vnfInfo.vm[1].vserver-name","server2");
        context.setAttribute("tmp.vnfInfo.vm[1].vserver-id","serverId2");
        context.setAttribute("tmp.vnfInfo.vm[1].vnfc-count","1");
        context.setAttribute("tmp.vnfInfo.vm[1].vnfc-name","testVnfc2");
        context.setAttribute("tmp.vnfInfo.vm[1].vnfc-function-code","testFnCode");
        context.setAttribute("tmp.vnfInfo.vm[1].vnfc-ipaddress-v4-oam-vip","testVnfcIpv4Address2");

        context.setAttribute("tmp.vnfInfo.vm[2].vserver-name","server3");
        context.setAttribute("tmp.vnfInfo.vm[2].vserver-id","serverId3");
        context.setAttribute("tmp.vnfInfo.vm[2].vnfc-count","1");
        context.setAttribute("tmp.vnfInfo.vm[2].vnfc-name","testVnfc3");
        context.setAttribute("tmp.vnfInfo.vm[2].vnfc-function-code","testFnCode");
        context.setAttribute("tmp.vnfInfo.vm[2].vnfc-ipaddress-v4-oam-vip","testVnfcIpv4Address3");

    }
}
