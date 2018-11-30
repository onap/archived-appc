/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
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

package org.onap.sdnc.config.generator.convert;

import static org.junit.Assert.assertEquals; 
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.sdnc.config.generator.ConfigGeneratorConstant;
import org.onap.sdnc.config.generator.merge.TestMergeNode;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class TestConvertNode {
    private static final EELFLogger log =
            EELFManager.getInstance().getLogger(TestConvertNode.class);

    private ConvertNode convertNode = new ConvertNode();

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void testConvertJson2DGContextFailure() throws SvcLogicException, IOException {
        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_IS_ESCAPED, "Y");
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX, "test");
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage("A JSONObject text must begin with '{'");
        convertJson2Context("convert/payload_parameters_config.json", inParams, ctx);
    }

    @Test
    public void testConvertJson2DGContextSuccess() throws SvcLogicException, IOException {
        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_IS_ESCAPED, "N");
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX, "");
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_BLOCK_KEYS,
                "configuration-parameters,configuration.configuration-json,configuration.configuration-string");
        convertJson2Context("convert/payload_cli_config.json", inParams, ctx);
        assertEquals(ConfigGeneratorConstant.OUTPUT_STATUS_SUCCESS,
                ctx.getAttribute("test." + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS));
        log.info("testPayloadCliConfig Result: "
                + ctx.getAttribute("block_configuration-parameters"));
        log.info("testPayloadCliConfig Result: "
                + ctx.getAttribute("block_configuration.configuration-string"));
    }

    @Test
    public void testEscapeDataForJsonDataType() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_UNESCAPE_DATA,"{}");
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_DATA_TYPE, ConfigGeneratorConstant.DATA_TYPE_JSON);
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX, "test");
        convertNode.escapeData(inParams, ctx);
        assertEquals(ConfigGeneratorConstant.OUTPUT_STATUS_SUCCESS,
                ctx.getAttribute("test." + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS));
    }

    @Test
    public void testEscapeDataForXMLDataType() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_UNESCAPE_DATA,"<>");
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_DATA_TYPE, ConfigGeneratorConstant.DATA_TYPE_XML);
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX, "");
        convertNode.escapeData(inParams, ctx);
        assertEquals(ConfigGeneratorConstant.OUTPUT_STATUS_SUCCESS,
                ctx.getAttribute(ConfigGeneratorConstant.OUTPUT_PARAM_STATUS));
    }

    @Test
    public void testEscapeDataForSQLDataType() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_UNESCAPE_DATA,"SHOW TABLES;");
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_DATA_TYPE, ConfigGeneratorConstant.DATA_TYPE_SQL);
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX, "");
        convertNode.escapeData(inParams, ctx);
        assertEquals(ConfigGeneratorConstant.OUTPUT_STATUS_SUCCESS,
            ctx.getAttribute(ConfigGeneratorConstant.OUTPUT_PARAM_STATUS));
    }

    @Test
    public void testEscapeDataForUnsupportedDataType() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_UNESCAPE_DATA,"lorum ipsem");
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_DATA_TYPE, ConfigGeneratorConstant.DATA_TYPE_TEXT);
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX, "");
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage("Datatype (dataType) param  value (TEXT)is not supported  for escapeData conversion.");
        convertNode.escapeData(inParams, ctx);
    }

    @Test
    public void testUnescapeDataForJsonDataType() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_ESCAPE_DATA,"{}");
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_DATA_TYPE, ConfigGeneratorConstant.DATA_TYPE_JSON);
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX, "test");
        convertNode.unEscapeData(inParams, ctx);
        assertEquals(ConfigGeneratorConstant.OUTPUT_STATUS_SUCCESS,
                ctx.getAttribute("test." + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS));
    }

    @Test
    public void testUnescapeDataForXMLDataType() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_ESCAPE_DATA,"<>");
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_DATA_TYPE, ConfigGeneratorConstant.DATA_TYPE_XML);
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX, "");
        convertNode.unEscapeData(inParams, ctx);
        assertEquals(ConfigGeneratorConstant.OUTPUT_PARAM_STATUS,
                ctx.getAttribute(ConfigGeneratorConstant.OUTPUT_STATUS_SUCCESS));
    }

    @Test
    public void testUnescapeDataForUnsupportedDataType() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_ESCAPE_DATA,"lorum ipsem");
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_DATA_TYPE, ConfigGeneratorConstant.DATA_TYPE_TEXT);
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX, "");
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage("Datatype (dataType) param  value (TEXT)is not supported  for unEscapeData conversion.");
        convertNode.unEscapeData(inParams, ctx);
    }

    @Test
    public void testUnescapeDataForNoDataSent() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_DATA_TYPE, ConfigGeneratorConstant.DATA_TYPE_TEXT);
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX, "");
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage("Escape (escapeData) param is missing for escapeData conversion.");
        convertNode.unEscapeData(inParams, ctx);
    }

    @Test
    public void testUnescapeDataForNoDataTypeSent() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_ESCAPE_DATA,"lorum ipsem");
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX, "");
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage("Datatype (dataType)param is missing for escapeData conversion.");
        convertNode.unEscapeData(inParams, ctx);
    }

    @Test
    public void testEscapeDataForNoDataSent() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_DATA_TYPE, ConfigGeneratorConstant.DATA_TYPE_TEXT);
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX, "");
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage("Unescape (unEscapeData) param is missing for escapeData conversion.");
        convertNode.escapeData(inParams, ctx);
    }

    @Test
    public void testEscapeDataForNoDataTypeSent() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_UNESCAPE_DATA,"lorum ipsem");
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX, "");
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage("Datatype (dataType)param is missing for escapeData conversion.");
        convertNode.escapeData(inParams, ctx);
    }

    @Test
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
        assertEquals(ConfigGeneratorConstant.OUTPUT_STATUS_SUCCESS, ctx.getAttribute("test." + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS));
    }

    @Test
    public void testConvertContextToJsonFailure() throws Exception {
        SvcLogicContext ctx = Mockito.spy(new SvcLogicContext());
        Mockito.doThrow(new NullPointerException("Mock Exception")).when(ctx).getAttributeKeySet();
        ctx.setAttribute("tmp.uploadConfigInfo.UPLOAD-CONFIG-ID", "200");
        ctx.setAttribute("tmp.uploadConfigInfo.VNF-ID", "00000");
        ctx.setAttribute("tmp.uploadConfigInfo.test[0]", "test0");
        ctx.setAttribute("tmp.uploadConfigInfo.test[1]", "test1");
        ctx.setAttribute("tmp.uploadConfigInfo.test[2]", null);
        ConvertNode convertNode = new ConvertNode();
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX, "");
        inParams.put("contextKey", "tmp.uploadConfigInfo");
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage("Mock Exception");
        convertNode.convertContextToJson(inParams, ctx);
    }

    private void convertJson2Context(String jsonFile, Map<String, String> inParams,
            SvcLogicContext ctx) throws IOException, SvcLogicException {
        ConvertNode convertNode = new ConvertNode();
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX, "test");
        String jsonData = IOUtils
                .toString(TestMergeNode.class.getClassLoader().getResourceAsStream(jsonFile),
                        StandardCharsets.UTF_8);
        log.info("TestConvertNode.testConvertJson2DGContext()" + jsonData);
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_JSON_DATA, jsonData);
        convertNode.convertJson2DGContext(inParams, ctx);
        assertEquals(ConfigGeneratorConstant.OUTPUT_STATUS_SUCCESS, ctx.getAttribute("test." + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS));
    }
}
