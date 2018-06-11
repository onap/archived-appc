/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
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

package org.onap.sdnc.dg.loader;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DGXMLGenerator {

    private static final Logger logger = LoggerFactory.getLogger(DGXMLGenerator.class);

    private static final String STRING_ENCODING = "utf-8";
    private static final String JS_INTERFACE_DG_CONVERTOR = "dgconverter";
    private static final String JS_METHOD_GET_NODE_TO_XML = "getNodeToXml";
    private static final String GENERATOR_TEMPLATE_FILE = "js/dg_xml2json.js";

    public void generateXMLFromJSON(String jsonPath, String xmlpath, String propertyPath) {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("JavaScript");
            if (!(engine instanceof Invocable)) {
                logger.error("Invoking methods is not supported.");
                return;
            }
            Invocable inv = (Invocable) engine;
            String js = IOUtils.toString(DGXMLGenerator.class.getClassLoader()
                .getResourceAsStream(GENERATOR_TEMPLATE_FILE), STRING_ENCODING);
            engine.eval(js);

            Object dgconverter = engine.get(JS_INTERFACE_DG_CONVERTOR);

            List<File> files = new ArrayList<>();
            if (dgconverter != null) {
                File jsonPathFile = new File(jsonPath);
                if (jsonPathFile.isDirectory()) {
                    String[] extensions = new String[]{"json", "JSON"};
                    files = (List<File>) FileUtils.listFiles(jsonPathFile, extensions, true);
                } else if (jsonPathFile.isFile()) {
                    files.add(jsonPathFile);
                } else {
                    throw new InvalidParameterException("Failed to get the nature of the JSON path :" + jsonPath);
                }

                logger.info("JSON Files identified " + files.size());

                if (!files.isEmpty()) {
                    boolean isXmlPathDeleted = FileUtils.deleteQuietly(new File(xmlpath));
                    logger.info("Cleaning old DG XML under : " + xmlpath + ", delete status :"
                        + isXmlPathDeleted);

                    generateXmls(xmlpath, inv, dgconverter, files);

                } else {
                    logger.info("No JSON Files to generate XML");
                }
            } else {
                logger.error("Couldn't get Java Script Engine..");
            }
        } catch (Exception e) {
            logger.error("Failed to generate generateXMLFromJSON", e);
        }
    }

    private void generateXmls(String xmlpath, Invocable inv, Object dgconverter, List<File> files)
        throws IOException, ScriptException, NoSuchMethodException {
        for (File file : files) {
            String dgJson = FileUtils.readFileToString(file, STRING_ENCODING);
            logger.info("Generating XML from  :" + file.getName());
            String xmlFileName =
                xmlpath + "/" + file.getName().replace(".json", ".xml");

            Object dgXMl =
                inv.invokeMethod(dgconverter, JS_METHOD_GET_NODE_TO_XML, dgJson);
            tryWriteXml(xmlFileName, dgXMl);
        }
    }

    private void tryWriteXml(String xmlFileName, Object dgXMl) throws IOException {
        if (dgXMl != null) {
            File xmlFile = new File(xmlFileName);
            FileUtils.writeStringToFile(xmlFile, dgXMl.toString(), STRING_ENCODING);
            logger.info("Generated XML File under  :" + xmlFile.getCanonicalPath());
        }
    }

    public static void main(String[] args) {
        try {
            DGXMLGenerator application = new DGXMLGenerator();
            String jsonPath;
            String xmlPath;
            String propertyPath = null;
            // Generate, GenerateLoad, GenerateLoadActivate
            if (args != null && args.length >= 2) {
                // e.g "src/main/resources/json" "src/test/resources/xml"
                logger.info("DGXML Conversion Started with arguments :" + args[0] + ":" + args[1]);
                jsonPath = args[0];
                xmlPath = args[1];
            } else {
                throw new InvalidParameterException("Required inputs are missing <jsonPath> <xmlPath>");
            }

            application.generateXMLFromJSON(jsonPath, xmlPath, propertyPath);
            logger.info("DGXML Conversion Completed...");
        } catch (Exception e) {
            logger.error("Failed in DG XML Generation", e);
        }
    }
}
