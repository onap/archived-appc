/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
 * Modification Copyright (C) 2018 IBM.
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

package org.onap.sdnc.config.generator.pattern;

import static org.junit.Assert.assertEquals;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.sdnc.config.generator.ConfigGeneratorConstant;

public class TestPatternNode {

    @Test
    public void parseErrorLog() throws Exception {
        PatternNode patternNode = new PatternNode();
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX, "test");
        String logData = IOUtils.toString(
                TestPatternNode.class.getClassLoader().getResourceAsStream("pattern/errorlog.txt"),
                ConfigGeneratorConstant.STRING_ENCODING);
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_LOG_DATA, logData);
        SvcLogicContext ctx = new SvcLogicContext();
        patternNode.parseErrorLog(inParams, ctx);
        assertEquals(ctx.getAttribute("test." + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS),
                ConfigGeneratorConstant.OUTPUT_STATUS_SUCCESS);

    }

    // @Test(expected=Exception.class)
    public void checkXMLData() throws Exception {
        PatternNode patternNode = new PatternNode();
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX, "test");

        String xmlData = IOUtils.toString(
                TestPatternNode.class.getClassLoader().getResourceAsStream("pattern/xml_data.xml"),
                ConfigGeneratorConstant.STRING_ENCODING);
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_CHECK_DATA, xmlData);
        SvcLogicContext ctx = new SvcLogicContext();
        patternNode.checkDataType(inParams, ctx);
        assertEquals(ctx.getAttribute("test." + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS),
                ConfigGeneratorConstant.OUTPUT_STATUS_SUCCESS);
    }

    // @Test
    public void checkJsonData() throws Exception {
        PatternNode patternNode = new PatternNode();
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX, "test");
        String xmlData =
                IOUtils.toString(TestPatternNode.class.getClassLoader().getResourceAsStream(
                        "pattern/json_data.json"), ConfigGeneratorConstant.STRING_ENCODING);
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_CHECK_DATA, xmlData);
        SvcLogicContext ctx = new SvcLogicContext();
        patternNode.checkDataType(inParams, ctx);
        assertEquals(ctx.getAttribute("test." + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS),
                ConfigGeneratorConstant.OUTPUT_STATUS_SUCCESS);
    }

    @Test(expected = Exception.class)
    public void checStringData() throws Exception {
        PatternNode patternNode = new PatternNode();
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX, "test");
        String stringData = IOUtils.toString(
                TestPatternNode.class.getClassLoader().getResourceAsStream("pattern/text_data.txt"),
                ConfigGeneratorConstant.STRING_ENCODING);
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_CHECK_DATA, stringData);
        SvcLogicContext ctx = new SvcLogicContext();
        patternNode.checkDataType(inParams, ctx);
        assertEquals(ctx.getAttribute("test." + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS),
                ConfigGeneratorConstant.OUTPUT_STATUS_SUCCESS);
    }
    
    @Test
    public void testCheckDataType() throws Exception {
        PatternNode patternNode = new PatternNode();
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX, "test");
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_CHECK_DATA, "testData");
        SvcLogicContext ctx = new SvcLogicContext();
        patternNode.checkDataType(inParams, ctx);
        assertEquals(ctx.getAttribute("test." + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS),
                ConfigGeneratorConstant.OUTPUT_STATUS_SUCCESS);
    }
}
