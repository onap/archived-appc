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

package org.openecomp.sdnc.config.generator.merge;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.Ignore;
import org.openecomp.sdnc.config.generator.ConfigGeneratorConstant;
import org.openecomp.sdnc.config.generator.merge.MergeNode;

import org.openecomp.sdnc.sli.SvcLogicContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.HashMap;
import java.util.Map;

public class TestMergeNode {

    //@Test
    public void testMergeJsonDataOnTemplate() throws Exception {
        MergeNode mergeNode = new MergeNode();
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX, "test");

        String jsonData = IOUtils.toString(TestMergeNode.class.getClassLoader().getResourceAsStream("merge/vdbe_data.json"));
        System.out.println("TestMergeNode.testMergeJsonDataOnTemplate()" + jsonData);
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_JSON_DATA, jsonData);

        String templateData = IOUtils.toString(TestMergeNode.class.getClassLoader().getResourceAsStream("merge/vdbe_template.xml"));
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_TEMPLATE_DATA, templateData);

        SvcLogicContext ctx = new SvcLogicContext();
        mergeNode.mergeJsonDataOnTemplate(inParams, ctx);
        assertEquals(ctx.getAttribute("test."+ConfigGeneratorConstant.OUTPUT_PARAM_STATUS), ConfigGeneratorConstant.OUTPUT_STATUS_SUCCESS);

        System.out.println("Result: " + ctx.getAttribute("test."+ConfigGeneratorConstant.OUTPUT_PARAM_MERGED_DATA));

    }
    
    //@Ignore
    public void testMergeComplexJsonDataOnTemplate() throws Exception {
        MergeNode mergeNode = new MergeNode();
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX, "test");

        String jsonData = IOUtils.toString(TestMergeNode.class.getClassLoader().getResourceAsStream("merge/complex/vdbe_data.json"));
        System.out.println("TestMergeNode.testMergeJsonComplexDataOnTemplate()" + jsonData);
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_JSON_DATA, jsonData);

        String templateData = IOUtils.toString(TestMergeNode.class.getClassLoader().getResourceAsStream("merge/complex/vdbe_template.xml"));
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_TEMPLATE_DATA, templateData);

        SvcLogicContext ctx = new SvcLogicContext();
        mergeNode.mergeComplexJsonDataOnTemplate(inParams, ctx);
        assertEquals(ctx.getAttribute("test."+ConfigGeneratorConstant.OUTPUT_PARAM_STATUS), ConfigGeneratorConstant.OUTPUT_STATUS_SUCCESS);

        System.out.println("Result: " + ctx.getAttribute("test."+ConfigGeneratorConstant.OUTPUT_PARAM_MERGED_DATA));

    }

    //@Test
    public void testMergeJsonDataOnTemplateFile() throws Exception {
        MergeNode mergeNode = new MergeNode();
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX, "test");

        String jsonData = IOUtils.toString(TestMergeNode.class.getClassLoader().getResourceAsStream("merge/vdbe_data.json"));
        System.out.println("TestMergeNode.testMergeJsonDataOnTemplate()" + jsonData);
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_JSON_DATA, jsonData);
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_TEMPLATE_FILE, "merge/vdbe_template.xml");

        SvcLogicContext ctx = new SvcLogicContext();
        mergeNode.mergeJsonDataOnTemplate(inParams, ctx);
        assertEquals(ctx.getAttribute("test."+ConfigGeneratorConstant.OUTPUT_PARAM_STATUS), ConfigGeneratorConstant.OUTPUT_STATUS_SUCCESS);
        System.out.println("Result: " + ctx.getAttribute("test."+ConfigGeneratorConstant.OUTPUT_PARAM_MERGED_DATA));

    }
}
