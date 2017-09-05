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

package org.openecomp.appc.dg;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.openecomp.appc.dg.mock.instance.MockConfigureNodeExecutor;
import org.openecomp.appc.dg.mock.instance.MockSvcLogicJavaPlugin;
import org.openecomp.sdnc.config.generator.convert.ConvertNode;
import org.openecomp.sdnc.config.generator.merge.MergeNode;
/*  move to open source
import com.att.sdnctl.dgtestlibrary.AbstractDGTestCase;
import com.att.sdnctl.dgtestlibrary.DGTestCase;
import com.att.sdnctl.dgtestlibrary.GraphKey;
*/
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

public class TestCommonConfig /* extends AbstractDGTestCase */ {

/*

	public String jsonPath = "src/main/resources/json";
	public String xmlpath = "src/main/resources/xml";

	@Test
	public void testCommonConfig() {
		try {
			//DGXMLGenerator generator = new DGXMLGenerator();
			//generator.generateXMLFromJSON(jsonPath, xmlpath, null);

			String propertyfileName = "APPC/CommonConfiguration/APPC_method_CommonConfiguration_TC2.properties";

			String commonConfigureXML = "src/main/resources/xml/APPC_CommonConfiguration.xml";
			String callGraph1XML = "src/main/resources/xml/APPC_GetConfigParams.xml";
			String callGraph2XML = "src/main/resources/xml/APPC_Configure.xml";
			String callGraph3XML = "src/main/resources/xml/APPC_SetStatus.xml";
			String callGraph4XML = "src/main/resources/xml/APPC_DownloadRestconfConfig.xml";
			String callGraph5XML = "src/main/resources/xml/APPC_GenerateConfig.xml";
			String callGraph6XML = "src/main/resources/xml/APPC_DownloadXmlConfig.xml";

			// Register Call graphs
			String injectGraphXmls[] = new String[] { commonConfigureXML,
					callGraph1XML,
					callGraph2XML,
					callGraph3XML,
					callGraph4XML,
					callGraph5XML,
					callGraph6XML };
		

			Map<String, Object> serviceReferences = new HashMap<String, Object>();
			serviceReferences.put("org.openecomp.sdnc.config.generator.convert.ConvertNode",new org.openecomp.sdnc.config.generator.convert.ConvertNode());
			serviceReferences.put("org.openecomp.sdnc.config.generator.merge.MergeNode", new org.openecomp.sdnc.config.generator.merge.MergeNode());
			//serviceReferences.put("com.att.appc.config.generator.node.ConfigResourceNode", new MockConfigResourceNode());
			serviceReferences.put("org.openecomp.appc.ccadaptor.ConfigComponentAdaptor", new MockConfigureNodeExecutor());

			GraphKey  graphKey = new GraphKey("APPC", null, "CommonConfiguration", null);
			DGTestCase tc = new DGTestCase(graphKey);
			tc.setInjectGraphXmls(injectGraphXmls);
			tc.setServiceReferences(serviceReferences);
			tc.setPropertyfileName(propertyfileName);

			SvcLogicContext ctx = new SvcLogicContext();
			processTestCase(tc, ctx);
			

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	
	

	@Test
	public void testGetConfigParamsFailure() {
		try {
			//DGXMLGenerator generator = new DGXMLGenerator();
			//generator.generateXMLFromJSON(jsonPath, xmlpath, null);

			String propertyfileName = "APPC/CommonConfiguration/GetConfigParamsFail.properties";

			String commonConfigureXML = "src/main/resources/xml/APPC_CommonConfiguration.xml";
			

			// Register Call graphs
			String injectGraphXmls[] = new String[] { commonConfigureXML };

			Map<String, Object> serviceReferences = new HashMap<String, Object>();
			
			

			GraphKey  graphKey = new GraphKey("APPC", null, "CommonConfiguration", null);
			DGTestCase tc = new DGTestCase(graphKey);
			tc.setInjectGraphXmls(injectGraphXmls);
			tc.setServiceReferences(serviceReferences);
			tc.setPropertyfileName(propertyfileName);

			
			
			SvcLogicContext ctx = new SvcLogicContext();
			processTestCase(tc, ctx);


		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	
	@Test
	public void testInvalidRequestAction() {
		try {
			//DGXMLGenerator generator = new DGXMLGenerator();
			//generator.generateXMLFromJSON(jsonPath, xmlpath, null);

			String propertyfileName = "APPC/CommonConfiguration/InvalidRequestAction.properties";

			String commonConfigureXML = "src/main/resources/xml/APPC_CommonConfiguration.xml";
			

			// Register Call graphs
			String injectGraphXmls[] = new String[] { commonConfigureXML };

			Map<String, Object> serviceReferences = new HashMap<String, Object>();
			
			

			GraphKey  graphKey = new GraphKey("APPC", null, "CommonConfiguration", null);
			DGTestCase tc = new DGTestCase(graphKey);
			tc.setInjectGraphXmls(injectGraphXmls);
			tc.setServiceReferences(serviceReferences);
			tc.setPropertyfileName(propertyfileName);

			SvcLogicContext ctx = new SvcLogicContext();
			processTestCase(tc, ctx);


		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	@Test
	public void testCommonConfigISBCTemplateSuccess() {
		try {
			//DGXMLGenerator generator = new DGXMLGenerator();
			//generator.generateXMLFromJSON(jsonPath, xmlpath, null);

			String propertyfileName = "APPC/CommonConfiguration/ISBCTemplateSuccess.properties";

			String commonConfigureXML = "src/main/resources/xml/APPC_CommonConfiguration.xml";
			String callGraph1XML = "src/main/resources/xml/APPC_GetConfigParams.xml";
			String callGraph2XML = "src/main/resources/xml/APPC_Configure.xml";
			String callGraph3XML = "src/main/resources/xml/APPC_SetStatus.xml";
			String callGraph4XML = "src/main/resources/xml/APPC_GenerateTemplateConfig.xml";
			String callGraph5XML = "src/main/resources/xml/APPC_CheckConfigStatus.xml";
			String callGraph6XML = "src/main/resources/xml/APPC_DownloadIsbcConfig.xml";
			String callGraph7XML = "src/main/resources/xml/APPC_UpdateAaiInfo.xml";
			String callGraph8XML = "src/main/resources/xml/APPC_GetVfModuleInfo.xml";
			String callGraph9XML = "src/main/resources/xml/APPC_SaveRunningConfig.xml";
			String callGraph10XML = "src/main/resources/xml/APPC_GetDeviceRunningConfig.xml";

			// Register Call graphs
			String injectGraphXmls[] = new String[] { commonConfigureXML,
					callGraph1XML,
					callGraph2XML,
					callGraph3XML,
					callGraph4XML,
					callGraph5XML,
					callGraph6XML,
					callGraph7XML,
					callGraph8XML,
					callGraph9XML,
					callGraph10XML };

			Map<String, Object> serviceReferences = new HashMap<String, Object>();
			serviceReferences.put("org.openecomp.sdnc.config.generator.convert.ConvertNode",new org.openecomp.sdnc.config.generator.convert.ConvertNode());
			serviceReferences.put("org.openecomp.sdnc.config.generator.merge.MergeNode", new org.openecomp.sdnc.config.generator.merge.MergeNode());
		
			

			GraphKey  graphKey = new GraphKey("APPC", null, "CommonConfiguration", null);
			DGTestCase tc = new DGTestCase(graphKey);
			tc.setInjectGraphXmls(injectGraphXmls);
			tc.setServiceReferences(serviceReferences);
			tc.setPropertyfileName(propertyfileName);

			Map<String, Object> resourceReferences = new HashMap<String, Object>();
			tc.setResourceReferences(resourceReferences);
			
			SvcLogicContext ctx = new SvcLogicContext();
			processTestCase(tc, ctx);


		} catch (Exception e) {
			e.printStackTrace();
		}

	}
*/
}





