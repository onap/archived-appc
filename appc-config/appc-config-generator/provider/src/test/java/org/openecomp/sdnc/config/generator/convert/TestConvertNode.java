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

package org.openecomp.sdnc.config.generator.convert;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.openecomp.sdnc.config.generator.ConfigGeneratorConstant;
import org.openecomp.sdnc.config.generator.convert.ConvertNode;
import org.openecomp.sdnc.config.generator.merge.TestMergeNode;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.openecomp.sdnc.sli.SvcLogicContext;
import org.openecomp.sdnc.sli.SvcLogicException;

public class TestConvertNode {
    private static final  EELFLogger log = EELFManager.getInstance().getLogger(TestConvertNode.class);

//    @Test
    public void testPayloadParametersConfig() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_IS_ESCAPED, "N");
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_BLOCK_KEYS, "configuration-parameters,configuration.configuration-json,configuration.configuration-string");
        convertJson2Context("convert/payload_parameters_config.json", inParams, ctx);
        log.info("testPayloadParametersConfig Result: " + ctx.getAttribute("block_configuration-parameters"));

    }

//@Test
    public void testPayloadCliConfig() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_IS_ESCAPED, "N");
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_BLOCK_KEYS, "configuration-parameters,configuration.configuration-json,configuration.configuration-string");
        convertJson2Context("convert/payload_cli_config.json", inParams, ctx);

        log.info("testPayloadCliConfig Result: " + ctx.getAttribute("block_configuration-parameters"));
        log.info("testPayloadCliConfig Result: " + ctx.getAttribute("block_configuration.configuration-string"));
    }

        //@Test   
    public void testPayloadXMLConfig() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_IS_ESCAPED, "N");
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_BLOCK_KEYS, "configuration-parameters,configuration.configuration-json,configuration.configuration-string");
        convertJson2Context("convert/payload_xml_config.json", inParams, ctx);

        log.info("testPayloadXMLConfig Result: " + ctx.getAttribute("block_configuration-parameters"));
        log.info("testPayloadXMLConfig Result: " + ctx.getAttribute("block_configuration.configuration-string"));
    }
         //@Test
    public void testPayloadJsonConfig() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_IS_ESCAPED, "N");
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_BLOCK_KEYS, "configuration-parameters,configuration.configuration-json,configuration.configuration-string");
        convertJson2Context("convert/payload_json_config.json", inParams, ctx);
        log.info("testPayloadJsonConfig Result: " + ctx.getAttribute("block_configuration-parameters"));
        log.info("testPayloadJsonConfig Result: " + ctx.getAttribute("block_configuration.configuration-json"));
    }
        //@Test
    private void convertJson2Context(String jsonFile, Map<String, String> inParams, SvcLogicContext ctx) throws IOException, SvcLogicException {
        ConvertNode convertNode = new ConvertNode();
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX, "test");

        String jsonData = IOUtils.toString(TestMergeNode.class.getClassLoader().getResourceAsStream(jsonFile));
        log.info("TestConvertNode.testConvertJson2DGContext()" + jsonData);
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_JSON_DATA, jsonData);
        convertNode.convertJson2DGContext(inParams, ctx);
        assertEquals(ctx.getAttribute("test."+ConfigGeneratorConstant.OUTPUT_PARAM_STATUS), ConfigGeneratorConstant.OUTPUT_STATUS_SUCCESS);
    }

//    @Test
    public void testEscapeData() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> inParams = new HashMap<String, String>();
        //String unescapeData = IOUtils.toString(TestMergeNode.class.getClassLoader().getResourceAsStream("convert/escape/config_msc.txt"));
        String unescapeData = IOUtils.toString(TestMergeNode.class.getClassLoader().getResourceAsStream("convert/escape/config_ssc.txt"));
        //String unescapeData = IOUtils.toString(TestMergeNode.class.getClassLoader().getResourceAsStream("convert/escape/config_vdbe.xml"));
        log.info("TestConvertNode.testEscapeData() unescapeData :"+unescapeData);
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX, "test");
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_UNESCAPE_DATA, unescapeData);
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_DATA_TYPE, ConfigGeneratorConstant.DATA_TYPE_SQL);
        ConvertNode convertNode = new ConvertNode();
        convertNode.escapeData(inParams, ctx);
        log.info("testEscapeData Result: " + ctx.getAttribute("test."+ConfigGeneratorConstant.OUTPUT_PARAM_ESCAPE_DATA));
    }



//    @Test
    public void testConvertContextToJson() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.uploadConfigInfo.UPLOAD-CONFIG-ID", "200");
        ctx.setAttribute("tmp.uploadConfigInfo.VNF-ID", "00000");
        ctx.setAttribute("tmp.uploadConfigInfo.test[0]", "test0");
        ctx.setAttribute("tmp.uploadConfigInfo.test[1]", "test1");
        ctx.setAttribute("tmp.uploadConfigInfo.test[2]", "test2");
    
        
        ConvertNode convertNode = new ConvertNode();
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX, "test");
        inParams.put("contextKey", "tmp.uploadConfigInfo");

            
        convertNode.convertContextToJson(inParams, ctx);
        
        log.info("JSON CONTENT " + ctx.getAttribute("test.jsonContent"));
        assertEquals(ctx.getAttribute("test."+ConfigGeneratorConstant.OUTPUT_PARAM_STATUS), ConfigGeneratorConstant.OUTPUT_STATUS_SUCCESS);
        
        
            
    }

    
    

}
