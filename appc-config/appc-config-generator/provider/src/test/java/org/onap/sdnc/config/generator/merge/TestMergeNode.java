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

package org.onap.sdnc.config.generator.merge;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.sdnc.config.generator.ConfigGeneratorConstant;

public class TestMergeNode {

    @Test(expected = Exception.class)
    public void testMergeJsonDataOnTemplate() throws Exception {
        MergeNode mergeNode = new MergeNode();
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX, "test");
        String jsonData = IOUtils.toString(
                TestMergeNode.class.getClassLoader().getResourceAsStream("merge/vdbe_data.json"));
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_JSON_DATA, jsonData);
        String templateData = IOUtils.toString(TestMergeNode.class.getClassLoader()
                .getResourceAsStream("merge/vdbe_template.xml"));
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_TEMPLATE_DATA, templateData);
        SvcLogicContext ctx = new SvcLogicContext();
        mergeNode.mergeJsonDataOnTemplate(inParams, ctx);
        assertEquals(ctx.getAttribute("test." + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS),
                ConfigGeneratorConstant.OUTPUT_STATUS_SUCCESS);

    }

    @Test(expected = Exception.class)
    public void testMergeComplexJsonDataOnTemplate() throws Exception {
        MergeNode mergeNode = new MergeNode();
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX, "test");

        String jsonData = IOUtils.toString(TestMergeNode.class.getClassLoader()
                .getResourceAsStream("merge/complex/vdbe_data.json"));
        System.out.println("TestMergeNode.testMergeJsonComplexDataOnTemplate()" + jsonData);
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_JSON_DATA, jsonData);

        String templateData = IOUtils.toString(TestMergeNode.class.getClassLoader()
                .getResourceAsStream("merge/complex/vdbe_template.xml"));
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_TEMPLATE_DATA, templateData);

        SvcLogicContext ctx = new SvcLogicContext();
        mergeNode.mergeComplexJsonDataOnTemplate(inParams, ctx);
        assertEquals(ctx.getAttribute("test." + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS),
                ConfigGeneratorConstant.OUTPUT_STATUS_SUCCESS);

    }

    @Test(expected = Exception.class)
    public void testMergeJsonDataOnTemplateFile() throws Exception {
        MergeNode mergeNode = new MergeNode();
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX, "test");

        String jsonData = IOUtils.toString(
                TestMergeNode.class.getClassLoader().getResourceAsStream("merge/vdbe_data.json"));
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_JSON_DATA, jsonData);
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_TEMPLATE_FILE, "merge/vdbe_template.xml");

        SvcLogicContext ctx = new SvcLogicContext();
        mergeNode.mergeJsonDataOnTemplate(inParams, ctx);
        assertEquals(ctx.getAttribute("test." + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS),
                ConfigGeneratorConstant.OUTPUT_STATUS_SUCCESS);
    }

    @Test
    public void testMmergeDataOnTemplate() throws SvcLogicException {
        MergeNode mergeNode = new MergeNode();
        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> inParams = new HashMap<String, String>();
        mergeNode.mergeDataOnTemplate(inParams, ctx);
    }
    
    @Test
    public void testMmergeDataOnTemplateWithTemplateData() throws SvcLogicException, IOException {
        MergeNode mergeNode = new MergeNode();
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX, "test");
        String jsonData = "{name1:value1,name2:value2}";
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_JSON_DATA, jsonData);
        String templateData = "testTemplateData";
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_TEMPLATE_DATA, templateData);
        SvcLogicContext ctx = new SvcLogicContext();
        mergeNode.mergeJsonDataOnTemplate(inParams, ctx);
        assertEquals(ConfigGeneratorConstant.OUTPUT_STATUS_SUCCESS,ctx.getAttribute("test." + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS));

    }

    @Test
    public void mergeYamlDataOnTemplate() throws SvcLogicException {
        MergeNode mergeNode = new MergeNode();
        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> inParams = new HashMap<String, String>();
        mergeNode.mergeYamlDataOnTemplate(inParams, ctx);
    }
}
