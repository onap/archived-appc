/*-
 * ============LICENSE_START=======================================================
 * ONAP : APP-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property.  All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.openecomp.appc.flow.executor.node;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.appc.flow.controller.data.Transaction;
import org.openecomp.appc.flow.controller.executorImpl.RestExecutor;
import org.openecomp.appc.flow.controller.interfaceData.ActionIdentifier;
import org.openecomp.appc.flow.controller.interfaceData.InventoryInfo;
import org.openecomp.appc.flow.controller.interfaceData.RequestInfo;
import org.openecomp.appc.flow.controller.interfaceData.Vm;
import org.openecomp.appc.flow.controller.interfaceData.VnfInfo;
import org.openecomp.appc.flow.controller.interfaceData.Vnfcslist;
import org.openecomp.appc.flow.controller.node.FlowControlNode;
import org.openecomp.appc.flow.controller.node.RestServiceNode;
import org.openecomp.appc.flow.controller.utils.FlowControllerConstants;
import org.openecomp.sdnc.sli.SvcLogicContext;

public class TestRestServiceNode {
    
    @Test(expected=Exception.class)
    public void testRestServiceNode() throws Exception {
        
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute(FlowControllerConstants.VNF_TYPE, "vUSP - vDBE-IPX HUB");
        ctx.setAttribute(FlowControllerConstants.REQUEST_ACTION, "healthcheck");
        ctx.setAttribute(FlowControllerConstants.VNFC_TYPE, "TESTVNFC-CF");
        ctx.setAttribute(FlowControllerConstants.REQUEST_ID,"TESTCOMMONFRMWK");
        ctx.setAttribute("host-ip-address","127.0.0.1");
        ctx.setAttribute("port-number","8888");
        ctx.setAttribute("request-action-type","GET");
        ctx.setAttribute("context", "loader/restconf/operations/appc-provider-lcm:health-check");
        
        HashMap<String, String> inParams = new HashMap<String, String>();
        RestServiceNode rsn = new RestServiceNode();
        inParams.put("output-state", "state");
        inParams.put("responsePrefix", "healthcheck");
        rsn.sendRequest(inParams, ctx);
        
        for (Object key : ctx.getAttributeKeySet()) {
            String parmName = (String) key;
            String parmValue = ctx.getAttribute(parmName);
        }
        
        
    }
    
    
    @Test(expected=Exception.class)
    public void testInputParamsRestServiceNode() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vnf-id", "test");
        ctx.setAttribute("tmp.vnfInfo.vm-count", "1");
        ctx.setAttribute("tmp.vnfInfo.vm[0].vnfc-count", "1");
        RestExecutor restExe = new RestExecutor();
        Transaction transaction = new Transaction();
        
        FlowControlNode node = new FlowControlNode();
        HashMap<String,String>flowSeq= restExe.execute(transaction, ctx);
        String flowSequnce=flowSeq.get("restResponse");
        
    }
}
