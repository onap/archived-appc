/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * 
 *  ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdnc.config.params.parser;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.Ignore;
import org.openecomp.sdnc.config.params.ParamsHandlerConstant;
import org.openecomp.sdnc.config.params.data.PropertyDefinition;
import org.openecomp.sdnc.config.params.parser.PropertyDefinitionNode;
import org.openecomp.sdnc.config.params.transformer.ArtificatTransformer;

import org.openecomp.sdnc.sli.SvcLogicContext;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TestPropertyDefinitionNode {

@Ignore
	public void testProcessMissingParamKeys() throws Exception {
		PropertyDefinitionNode propertyDefinitionNode = new PropertyDefinitionNode();
		Map<String, String> inParams = new HashMap<String, String>();
		inParams.put(ParamsHandlerConstant.INPUT_PARAM_RESPONSE_PRIFIX, "test");

		String yamlData = IOUtils.toString(TestPropertyDefinitionNode.class.getClassLoader().getResourceAsStream("parser/pd.yaml"), Charset.defaultCharset());
		inParams.put(ParamsHandlerConstant.INPUT_PARAM_PD_CONTENT, yamlData);

		String jsonData = IOUtils.toString(TestPropertyDefinitionNode.class.getClassLoader().getResourceAsStream("parser/request-param.json"), Charset.defaultCharset());
		inParams.put(ParamsHandlerConstant.INPUT_PARAM_JSON_DATA, jsonData);

		SvcLogicContext ctx = new SvcLogicContext();
		propertyDefinitionNode.processMissingParamKeys(inParams, ctx);
		assertEquals(ctx.getAttribute("test."+ParamsHandlerConstant.OUTPUT_PARAM_STATUS), ParamsHandlerConstant.OUTPUT_STATUS_SUCCESS);

	}

	public void testProcessExternalSystemParamKeys() throws Exception {
		PropertyDefinitionNode propertyDefinitionNode = new PropertyDefinitionNode();
		Map<String, String> inParams = new HashMap<String, String>();
		inParams.put(ParamsHandlerConstant.INPUT_PARAM_RESPONSE_PRIFIX, "test");

		String yamlData = IOUtils.toString(TestPropertyDefinitionNode.class.getClassLoader().getResourceAsStream("parser/pd.yaml"), Charset.defaultCharset());
		inParams.put(ParamsHandlerConstant.INPUT_PARAM_PD_CONTENT, yamlData);

		String jsonData = IOUtils.toString(TestPropertyDefinitionNode.class.getClassLoader().getResourceAsStream("parser/request-param.json"), Charset.defaultCharset());
		inParams.put(ParamsHandlerConstant.INPUT_PARAM_JSON_DATA, jsonData);

		inParams.put(ParamsHandlerConstant.INPUT_PARAM_SYSTEM_NAME, "INSTAR");

		SvcLogicContext ctx = new SvcLogicContext();
		propertyDefinitionNode.processExternalSystemParamKeys(inParams, ctx);
		assertEquals(ctx.getAttribute("test."+ParamsHandlerConstant.OUTPUT_PARAM_STATUS), ParamsHandlerConstant.OUTPUT_STATUS_SUCCESS);

		System.out.println("Result: " + ctx.getAttributeKeySet());
		System.out.println("INSTAR.keys : " + ctx.getAttribute("INSTAR.keys"));
		System.out.println("INSTAR.LOCAL_CORE_ALT_IP_ADDR.request-logic : " + ctx.getAttribute("INSTAR.LOCAL_ACCESS_IP_ADDR"));
		System.out.println("INSTAR.LOCAL_CORE_ALT_IP_ADDR.request-logic : " + ctx.getAttribute("INSTAR.LOCAL_CORE_ALT_IP_ADDR"));

	}

	public void mergeJsonData() throws Exception {
		PropertyDefinitionNode propertyDefinitionNode = new PropertyDefinitionNode();
		Map<String, String> inParams = new HashMap<String, String>();
		inParams.put(ParamsHandlerConstant.INPUT_PARAM_RESPONSE_PRIFIX, "test");

		String jsonData = IOUtils.toString(TestPropertyDefinitionNode.class.getClassLoader().getResourceAsStream("parser/request-param.json"), Charset.defaultCharset());
		inParams.put(ParamsHandlerConstant.INPUT_PARAM_JSON_DATA, jsonData);

		String mergeJsonData = IOUtils.toString(TestPropertyDefinitionNode.class.getClassLoader().getResourceAsStream("parser/merge-param.json"), Charset.defaultCharset());
		inParams.put(ParamsHandlerConstant.INPUT_PARAM_MERGE__JSON_DATA, mergeJsonData);

		SvcLogicContext ctx = new SvcLogicContext();
		propertyDefinitionNode.mergeJsonData(inParams, ctx);
		assertEquals(ctx.getAttribute("test."+ParamsHandlerConstant.OUTPUT_PARAM_STATUS), ParamsHandlerConstant.OUTPUT_STATUS_SUCCESS);

		System.out.println("Result: " + ctx.getAttributeKeySet()); 
		System.out.println("Merged Value : " + ctx.getAttribute("test." +ParamsHandlerConstant.OUTPUT_PARAM_CONFIGURATION_PARAMETER) );


	}

//	@Test
	public void testArtificatTransformer() throws Exception {
		ArtificatTransformer transformer = new ArtificatTransformer();
		String yamlData = IOUtils.toString(TestPropertyDefinitionNode.class.getClassLoader().getResourceAsStream("parser/pd.yaml"), Charset.defaultCharset());

		PropertyDefinition propertyDefinition = transformer.convertYAMLToPD(yamlData);

		//		String json = transformer.transformYamlToJson(yamlData);
		//		System.out.println("TestPropertyDefinitionNode.testArtificatTransformer()" + json);
		String yaml = transformer.convertPDToYaml(propertyDefinition);
		System.out.println("TestPropertyDefinitionNode.testArtificatTransformer():\n" + yaml);

	}
	
	


}
