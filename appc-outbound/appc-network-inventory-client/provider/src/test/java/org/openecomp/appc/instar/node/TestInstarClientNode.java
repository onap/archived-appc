/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.appc.instar.node;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.HttpMethod;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.openecomp.appc.instar.dme2client.SecureRestClientTrustManager;
import org.openecomp.appc.instar.utils.InstarClientConstant;
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
}
