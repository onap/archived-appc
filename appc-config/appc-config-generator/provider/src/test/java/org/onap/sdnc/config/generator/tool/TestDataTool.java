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
 * Modifications Copyright (C) 2018 Ericsson
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

package org.onap.sdnc.config.generator.tool;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.onap.sdnc.config.generator.ConfigGeneratorConstant;
import org.onap.sdnc.config.generator.pattern.TestPatternNode;


public class TestDataTool {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Mock
    private LogParserTool logParserTool = new LogParserTool();

    @Test
    public void testCheckData() throws IOException {
        String data = IOUtils.toString(
            TestPatternNode.class.getClassLoader()
                .getResourceAsStream("convert/payload_cli_config.json"),
            ConfigGeneratorConstant.STRING_ENCODING);
        assertEquals(ConfigGeneratorConstant.DATA_TYPE_JSON,
                CheckDataTool.checkData(data));
    }

    @Test
    public void testIsJSON() throws IOException {
        String data = IOUtils.toString(
            TestPatternNode.class.getClassLoader()
                .getResourceAsStream("convert/payload_cli_config.json"),
            ConfigGeneratorConstant.STRING_ENCODING);
        assertEquals(true, CheckDataTool.isJSON(data));
    }

    @Test
    public void testIsXML() throws IOException {
        String data = IOUtils.toString(
            TestPatternNode.class.getClassLoader().getResourceAsStream("pattern/xml_data.xml"),
            ConfigGeneratorConstant.STRING_ENCODING);
        assertEquals(true, CheckDataTool.isXML(data));
    }

    @Test
    public void testNode() {
        CustomJsonNodeFactory c = new CustomJsonNodeFactory();
        String text = "test";
        assertEquals(CustomTextNode.class, c.textNode(text).getClass());;
    }

    @Test
    public void testCustomText() {
        CustomTextNode c = new CustomTextNode("test");
        assertEquals("test",c.toString());
    }

    @Test
    public void testEscapeUtils() {
        String s = "test\\";
        String st = "test\"test";
        String str = "test\'" + "test";
        String strng = "test\0";
        assertEquals("test\\\\", EscapeUtils.escapeString(s));
        assertEquals("test\"test", EscapeUtils.escapeSql(st));
        assertEquals("test\\'test", EscapeUtils.escapeString(str));
        assertEquals("test\\0", EscapeUtils.escapeString(strng));
        assertEquals(null, EscapeUtils.escapeString(null));
    }

    @Test
    public void testgetData() throws Exception {
        List<String> argList = null;
        String schema = "sdnctl";
        String tableName = "dual";
        String getselectData = "123";
        String getDataClasue = "123='123'";
        expectedEx.expect(NullPointerException.class);
        DbServiceUtil.getData(tableName, argList, schema, getselectData, getDataClasue);
    }

    @Test
    public void testupdateDB() throws Exception {
        String setClause = null;
        String tableName = "dual";
        List<String> inputArgs = null;
        String whereClause = "123='123'";
        expectedEx.expect(NullPointerException.class);
        DbServiceUtil.updateDB(tableName, inputArgs, whereClause, setClause);
    }

    // @Test(expected = Exception.class)
    public void testinitDbLibService() throws Exception {
        DbServiceUtil.initDbLibService();
    }

    @Test
    public void testJSONTool() throws Exception {
        String data = IOUtils.toString(
            TestPatternNode.class.getClassLoader()
                .getResourceAsStream("convert/payload_cli_config.json"),
            ConfigGeneratorConstant.STRING_ENCODING);
        JSONTool.convertToProperties(data);
        List<String> blockKeys = new ArrayList<String>();
        blockKeys.add("vnf-type");
        blockKeys.add("request-parameters");
        assertEquals(HashMap.class, JSONTool.convertToProperties(data, blockKeys).getClass());
    }

    @Test
    public void testLogParserTool() throws Exception {
        String data = IOUtils.toString(
            TestPatternNode.class.getClassLoader().getResourceAsStream("pattern/errorlog.txt"),
            ConfigGeneratorConstant.STRING_ENCODING);
        LogParserTool lpt = new LogParserTool();
        assertEquals("Did not find the string 'Starting orchestration of file backed up to /var/opt"+
                "/MetaSwitch/orch/orch_conf.json' in the log file with timestamp within the last 5 minutes", 
                lpt.parseErrorLog(data));
    }

    @Test
    public void testMergeTool() throws Exception {
        String template = "test";
        Map<String, String> dataMap = new HashMap<String, String>();
        assertEquals("test", MergeTool.mergeMap2TemplateData(template, dataMap));
    }

    @Test
    public void testcheckDateTime() throws Exception {
        String line = "2017-08-20T17:40:23.100361+00:00 Error parsing orchestration file:";
        assertEquals("Did not find the string 'Starting orchestration of file backed up to /var/opt"+
                "/MetaSwitch/orch/orch_conf.json' in the log file with timestamp within the last 5 minutes",
                logParserTool.parseErrorLog(line));
    }

    @Test
    public void testCheckDataForInvalidXml()
    {
        String data="<xml><configuration</configuration>";
        assertEquals(ConfigGeneratorConstant.DATA_TYPE_TEXT,CheckDataTool.checkData(data));
    }

    @Test
    public void testCheckDataForValidXml()
    {
        String data="<xml><configuration></configuration></xml>";
        assertEquals(ConfigGeneratorConstant.DATA_TYPE_XML,CheckDataTool.checkData(data));
    }
}
