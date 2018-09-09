/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications Copyright (C) 2018 IBM
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

package org.onap.sdnc.config.audit.node;
 
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.onap.sdnc.config.audit.node.CompareNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.assertEquals;

import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

public class TestCompareNodeJson {
    private static final Logger log = LoggerFactory.getLogger(TestCompareNodeJson.class);
    private CompareNode cmp;
	private SvcLogicContext ctx;
    private HashMap<String, String> testMap;
	
    @Before
    public void setUp()
    {
        cmp = new CompareNode();
		ctx = new SvcLogicContext();
        testMap = new HashMap<String, String>();
        
    }
    
    @Test
    public void TestCompareJsonForSamePayload() throws SvcLogicException {
        String controlJson = "{\n\"input\": {\n   \"appc-request-header\": {\n       \"svc-request-id\": \"000000000\", \n \"svc-action\": \"prepare\"   \n }, \n\"request-information\": {\n \"request-id\": \"000000000\", \n\"request-action\": \"VsbgServiceActivateRequest\", \n\"request-sub-action\": \"PREPARE\",  \n \"source\": \"Version2\" \n} \n} \n}";
        String testJson = "{\n\"input\": {\n  \"appc-request-header\": {\n \"svc-request-id\": \"000000000\", \n \"svc-action\": \"prepare\"   \n }, \n\"request-information\": {\n \"request-id\": \"000000000\", \n\"request-action\": \"VsbgServiceActivateRequest\", \n\"request-sub-action\": \"PREPARE\",  \n \"source\": \"Version2\" \n} \n} \n}";
        testMap.put("compareDataType", "RestConf");
        testMap.put("sourceData", controlJson);
        testMap.put("targetData", testJson);
        cmp.compare(testMap, ctx);
        assert (ctx.getAttribute("STATUS").equals("SUCCESS"));
    }
    
    @Test
    public void TestCompareCliForNoPayload() throws SvcLogicException {
        cmp.compare(testMap, ctx);
        assertEquals ("FAILURE",ctx.getAttribute("STATUS"));
    }
    
    @Test
    public void TestCompareCliForNullCompareType() throws SvcLogicException {
        testMap.put("compareDataType", "RestConf");
        cmp.compare(testMap, ctx);
        assertEquals ("FAILURE",ctx.getAttribute("STATUS"));
    }

    @Test
    public void TestCompareJsonFordifferentPayload() throws SvcLogicException {
        String controlJson = "{\n\"input\": {\n   \"appc-request-header\": {\n       \"svc-request-id\": \"000000000\", \n \"svc-action\": \"prepare\"   \n }, \n\"request-information\": {\n \"request-id\": \"000000000\", \n\"request-action\": \"VsbgServiceActivateRequest\", \n\"request-sub-action\": \"PREPARE\",  \n \"source\": \"Version2\" \n} \n} \n}";
        String testJson = "{\n\"input\": {\n  \"appc-request-header\": { \n \"svc-action\": \"prepare\"   \n }, \n\"request-information\": {\n \"request-id\": \"0000000000\", \n\"request-action\": \"VsbgServiceActivateRequest\", \n\"request-sub-action\": \"PREPARE\",  \n \"source\": \"Version2\" \n} \n} \n}";
        testMap.put("compareDataType", "RestConf");
        testMap.put("sourceData", controlJson);
        testMap.put("targetData", testJson);
        cmp.compare(testMap, ctx);
        assert (ctx.getAttribute("STATUS").equals("FAILURE"));
    }
    
    @Test
    public void TestCompareJsonFordifferentPayloadWithXMLDataType() throws SvcLogicException {
        String controlJson = "{\n\"input\": {\n   \"appc-request-header\": {\n       \"svc-request-id\": \"000000000\", \n \"svc-action\": \"prepare\"   \n }, \n\"request-information\": {\n \"request-id\": \"000000000\", \n\"request-action\": \"VsbgServiceActivateRequest\", \n\"request-sub-action\": \"PREPARE\",  \n \"source\": \"Version2\" \n} \n} \n}";
        String testJson = "{\n\"input\": {\n  \"appc-request-header\": { \n \"svc-action\": \"prepare\"   \n }, \n\"request-information\": {\n \"request-id\": \"0000000000\", \n\"request-action\": \"VsbgServiceActivateRequest\", \n\"request-sub-action\": \"PREPARE\",  \n \"source\": \"Version2\" \n} \n} \n}";
        testMap.put("compareDataType", "XML");
        testMap.put("sourceData", controlJson);
        testMap.put("targetData", testJson);
        cmp.compare(testMap, ctx);
        assertEquals ("FAILURE",ctx.getAttribute("STATUS"));
    }
}
