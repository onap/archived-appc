/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
 * Modifications Copyright (C) 2018 IBM
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

package org.onap.sdnc.config.audit.node;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.onap.sdnc.config.audit.node.CompareNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import static org.junit.Assert.assertEquals;

public class TestCompareNodeCli {
    private static final Logger log = LoggerFactory.getLogger(TestCompareNodeCli.class);
    private CompareNode cmp;
	SvcLogicContext ctx;
    HashMap<String, String> testMap;
        
    
    @Before
    public void setUp()
    {
        cmp = new CompareNode();
		ctx = new SvcLogicContext();
		testMap = new HashMap<String, String>();
    }
    
    @Test
    public void TestCompareCliForSamePayload() throws SvcLogicException {
        testMap.put("compareDataType", "Cli");
        testMap.put("sourceData", "This is a Text Configuration of Device");
        testMap.put("targetData", "This is a Text Configuration of Device");
        cmp.compare(testMap, ctx);
        assert (ctx.getAttribute("STATUS").equals("SUCCESS"));
    }
    
    @Test
    public void TestCompareCliForNoPayload() throws SvcLogicException {
        testMap.put("compareDataType", "Cli");
        cmp.compare(testMap, ctx);
        assertEquals ("FAILURE",ctx.getAttribute("STATUS"));
    }

    @Test
    public void TestCompareCliFordifferentPayload() throws SvcLogicException {
        testMap.put("compareDataType", "Cli");
        testMap.put("sourceData", "This is a Text Negative test Configuration of Device");
        testMap.put("targetData", "This is a Text Configuration of Device");
        cmp.compare(testMap, ctx);
        assert (ctx.getAttribute("STATUS").equals("FAILURE"));
    }

    @Test
    public void TestCompareForMissingInput() throws SvcLogicException {
        testMap.put("sourceData", "This is a Text Negative test Configuration of Device");
        testMap.put("targetData.configuration-data", "This is a Text Configuration of Device");
        cmp.compare(testMap, ctx);
        assert (ctx.getAttribute("STATUS").equals("FAILURE"));
    }

}
