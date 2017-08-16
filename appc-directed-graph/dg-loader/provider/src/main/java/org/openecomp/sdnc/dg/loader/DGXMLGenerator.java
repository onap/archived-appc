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

package org.openecomp.sdnc.dg.loader;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DGXMLGenerator {
	private final static Logger logger = LoggerFactory.getLogger(DGXMLGenerator.class);

	public static String STRING_ENCODING = "utf-8";
	public static String JS_INTERFACE_DG_CONVERTOR = "dgconverter";
	public static String JS_METHOD_GET_NODE_TO_XML = "getNodeToXml";
	public static String GENERATOR_TEMPLATE_FILE = "js/dg_xml2json.js";

	public void generateXMLFromJSON(String jsonPath, String xmlpath, String propertyPath) throws Exception {
		try{
			ScriptEngineManager manager = new ScriptEngineManager();
			ScriptEngine engine = manager.getEngineByName("JavaScript");
			if (!(engine instanceof Invocable)) {
				logger.error("Invoking methods is not supported.");
				return;
			}
			Invocable inv = (Invocable) engine;
			//		engine.eval(new FileReader(DGXMLGenerator.class.getClassLoader().getResource(GENERATOR_TEMPLATE_FILE).getPath()));
			String js = IOUtils.toString(DGXMLGenerator.class.getClassLoader().getResourceAsStream(GENERATOR_TEMPLATE_FILE),STRING_ENCODING);
			engine.eval(js);

			Object dgconverter = engine.get(JS_INTERFACE_DG_CONVERTOR);

			List<File> files = new ArrayList<File>();
			if(dgconverter != null){
				File jsonPathFile = new File(jsonPath);
				if(jsonPathFile.isDirectory()){
					String[] extensions = new String[] { "json", "JSON" };
					files = (List<File>) FileUtils.listFiles(jsonPathFile, extensions, true);
				}else if(jsonPathFile.isFile()){
					files.add(jsonPathFile);
				}else{
					throw new Exception("Failed to get the nature of the JSON path :"+ jsonPath);
				}

				logger.info("JSON Files identified "+ files.size());

				if(files.size() > 0){
					boolean isXmlPathDeleted = FileUtils.deleteQuietly(new File(xmlpath));
					logger.info("Cleaning old DG XML under : " + xmlpath + ", delete status :" + isXmlPathDeleted);

					for (File file : files) {
						String dgJson = FileUtils.readFileToString(file,STRING_ENCODING);
						logger.info("Generating XML from  :" + file.getName());
						String xmlFileName = xmlpath +"/"+file.getName().replace(".json", ".xml");

						Object dgXMl = inv.invokeMethod(dgconverter, JS_METHOD_GET_NODE_TO_XML, dgJson);
						// Write the XML File
						if(dgXMl != null){
							File xmlFile = new File(xmlFileName);
							FileUtils.writeStringToFile(xmlFile, dgXMl.toString(), STRING_ENCODING);
							logger.info("Generated XML File under  :" + xmlFile.getCanonicalPath());
						}
					}

				}else{
					logger.info("No JSON Files to generate XML");
				}
			}else{
				logger.error("Couldn't get Java Script Engine..");
			}
		}catch (Exception e) {
			logger.error("Failed to generate generateXMLFromJSON :"+e.getMessage());
		}
	}


	public static void main(String[] args) {
		try {
			DGXMLGenerator application = new DGXMLGenerator();
			String jsonPath = null;
			String xmlPath = null;
			String propertyPath = null;
			// Generate, GenerateLoad, GenerateLoadActivate
			//args = new String[]{"src/main/resources/json","src/test/resources/xml"};
			logger.info("DGXML Conversion Started with arguments :"+ args[0] +":"+ args[1]);
			if(args != null && args.length >= 2){
				jsonPath = args[0];
				xmlPath = args[1];
			}else{
				throw new Exception("Sufficient inputs are missing <jsonPath> <xmlPath>");
			}

			application.generateXMLFromJSON(jsonPath, xmlPath, propertyPath);
			logger.info("DGXML Conversion Completed...");
		} catch (Exception e) {
			logger.error("Failed in DG XML Generation :"+e.getMessage());
			e.printStackTrace();
		}

	}

}
