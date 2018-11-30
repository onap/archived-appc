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
 * Modification Copyright (C) 2018 Ericsson
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

package org.onap.sdnc.config.generator.merge;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.sdnc.config.generator.ConfigGeneratorConstant;

public class TestMergeNode {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void testMergeJsonDataOnTemplate() throws Exception {
        MergeNode mergeNode = new MergeNode();
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX, "test");
        String jsonData = IOUtils.toString(
                TestMergeNode.class.getClassLoader().getResourceAsStream("merge/valid.json"),
                StandardCharsets.UTF_8);
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_JSON_DATA, jsonData);
        String templateData = IOUtils.toString(TestMergeNode.class.getClassLoader()
                .getResourceAsStream("merge/valid.xml"), StandardCharsets.UTF_8);
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_TEMPLATE_DATA, templateData);
        SvcLogicContext ctx = new SvcLogicContext();
        mergeNode.mergeJsonDataOnTemplate(inParams, ctx);
        assertEquals(ctx.getAttribute("test." + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS),
                ConfigGeneratorConstant.OUTPUT_STATUS_SUCCESS);
    }

    @Test
    public void testMergeJsonDataOnTemplateForEmptyParamData() throws Exception {
        MergeNode mergeNode = new MergeNode();
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX, "test");
        SvcLogicContext ctx = new SvcLogicContext();
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage("JSON Data is missing");
        mergeNode.mergeJsonDataOnTemplate(inParams, ctx);
    }

    @Test
    public void testMergeJsonDataOnTemplateForEmptyTemplateData() throws Exception {
        MergeNode mergeNode = new MergeNode();
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX, "test");
        String jsonData = IOUtils.toString(
                TestMergeNode.class.getClassLoader().getResourceAsStream("merge/valid.json"),
                StandardCharsets.UTF_8);
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_JSON_DATA, jsonData);
        SvcLogicContext ctx = new SvcLogicContext();
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage("Template data or Template file is missing");
        mergeNode.mergeJsonDataOnTemplate(inParams, ctx);
    }

    @Test
    public void testMergeComplexJsonDataOnTemplate() throws Exception {
        MergeNode mergeNode = new MergeNode();
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX, "test");
        String jsonData = IOUtils.toString(TestMergeNode.class.getClassLoader()
                .getResourceAsStream("merge/valid.json"), StandardCharsets.UTF_8);
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_JSON_DATA, jsonData);
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_TEMPLATE_FILE, "merge/valid.xml");
        SvcLogicContext ctx = new SvcLogicContext();
        mergeNode.mergeComplexJsonDataOnTemplate(inParams, ctx);
        assertEquals(ctx.getAttribute("test." + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS),
                ConfigGeneratorConstant.OUTPUT_STATUS_SUCCESS);
    }

    @Test
    public void testMergeComplexJsonDataWithMissingJson() throws Exception {
        MergeNode mergeNode = new MergeNode();
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX, "");
        String jsonData = IOUtils.toString(TestMergeNode.class.getClassLoader()
                .getResourceAsStream("merge/blank.json"), StandardCharsets.UTF_8);
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_JSON_DATA, jsonData);
        SvcLogicContext ctx = new SvcLogicContext();
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage("JSON Data is missing");
        mergeNode.mergeComplexJsonDataOnTemplate(inParams, ctx);
    }

    @Test
    public void testMergeComplexJsonDataWithMissingTemplateDataAndFile() throws Exception {
        MergeNode mergeNode = new MergeNode();
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX, "test");
        String jsonData = IOUtils.toString(TestMergeNode.class.getClassLoader()
                .getResourceAsStream("merge/valid.json"), StandardCharsets.UTF_8);
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_JSON_DATA, jsonData);
        SvcLogicContext ctx = new SvcLogicContext();
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage("Template data or Template file is missing");
        mergeNode.mergeComplexJsonDataOnTemplate(inParams, ctx);
    }
}
