/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * =============================================================================
 * Modifications Copyright (C) 2018 IBM.
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
package org.onap.appc.flow.executor.node;

import java.util.HashMap;

import org.junit.Test;
import org.onap.appc.flow.controller.node.JsonParsingNode;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

public class TestParsingNode {

    @Test
    public void testRestServiceNode() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        HashMap<String, String> inParams = new HashMap<String, String>();
        JsonParsingNode rsn = new JsonParsingNode();
        inParams.put("data", "{\"identifier\": \"scope represented\",\"state\": \"healthy\",\"test\": \"passed\", \"time\": \"01-01-1000:0000\"}");
        inParams.put("responsePrefix", "APPC.healthcheck");
        rsn.parse(inParams, ctx);
        for (Object key : ctx.getAttributeKeySet()) {
            String parmName = (String) key;
            String parmValue = ctx.getAttribute(parmName);
        }
        
    }
    
    @Test(expected=SvcLogicException.class)
    public void testRestServiceNodeForNonJsonData() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        HashMap<String, String> inParams = new HashMap<String, String>();
        JsonParsingNode rsn = new JsonParsingNode();
        inParams.put("data", "2");
        inParams.put("responsePrefix", "APPC.healthcheck");
        rsn.parse(inParams, ctx);
                
    }
}
