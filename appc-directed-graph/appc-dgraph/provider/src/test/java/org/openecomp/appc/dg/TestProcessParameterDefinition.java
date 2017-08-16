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

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
/* move to open source
import com.att.sdnctl.dgtestlibrary.AbstractDGTestCase;
import com.att.sdnctl.dgtestlibrary.DGTestCase;
import com.att.sdnctl.dgtestlibrary.GraphKey;
*/
import org.openecomp.sdnc.sli.SvcLogicContext;


public class TestProcessParameterDefinition /* extends AbstractDGTestCase */ {
	
	private static final EELFLogger logger = EELFManager.getInstance().getLogger(TestProcessParameterDefinition.class);
	public static String getProcessPDXML = "src/main/resources/xml/APPC_ProcessParameterDefinition.xml";

/*

	
	@Test
	public void testProcessPD() {
		try {


			String propertyfileName = "APPC/ProcessParameterDefinition/ProcessPD.properties";
			Map<String, Object> serviceReferences = new HashMap<String, Object>();

			
		
			serviceReferences.put("org.openecomp.sdnc.config.params.parser.PropertyDefinitionNode", new org.openecomp.sdnc.config.params.parser.PropertyDefinitionNode());

			
			
			// Register Call graphs
			String injectGraphXmls[] = new String[] { getProcessPDXML };


			GraphKey  graphKey = new GraphKey("APPC", null, "ProcessParameterDefinition", null);
			DGTestCase tc = new DGTestCase(graphKey);
			tc.setInjectGraphXmls(injectGraphXmls);
			tc.setServiceReferences(serviceReferences);
			tc.setPropertyfileName(propertyfileName);
			

			SvcLogicContext ctx = new SvcLogicContext();
			processTestCase(tc, ctx);
			
			//System.out.println(ctx.getAttribute("tmp.allParams.configuration-parameters"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	@Test
	public void testGetPDFail() {
		try {


			String propertyfileName = "APPC/ProcessParameterDefinition/GetPDFail.properties";
			Map<String, Object> serviceReferences = new HashMap<String, Object>();

			
		
			serviceReferences.put("org.openecomp.sdnc.config.params.parser.PropertyDefinitionNode", new org.openecomp.sdnc.config.params.parser.PropertyDefinitionNode());

			
			
			// Register Call graphs
			String injectGraphXmls[] = new String[] { getProcessPDXML };


			GraphKey  graphKey = new GraphKey("APPC", null, "ProcessParameterDefinition", null);
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
	public void testNoInstarParams() {
			try {


				String propertyfileName = "APPC/ProcessParameterDefinition/NoInstarParams.properties";
				Map<String, Object> serviceReferences = new HashMap<String, Object>();

				
			
				serviceReferences.put("org.openecomp.sdnc.config.params.parser.PropertyDefinitionNode", new org.openecomp.sdnc.config.params.parser.PropertyDefinitionNode());

				
				
				// Register Call graphs
				String injectGraphXmls[] = new String[] { getProcessPDXML };


				GraphKey  graphKey = new GraphKey("APPC", null, "ProcessParameterDefinition", null);
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
	public void testGetInstarParamsFail() {
			try {


				String propertyfileName = "APPC/ProcessParameterDefinition/GetInstarParamsFail.properties";
				Map<String, Object> serviceReferences = new HashMap<String, Object>();

				
			
								
				
				// Register Call graphs
				String injectGraphXmls[] = new String[] { getProcessPDXML };


				GraphKey  graphKey = new GraphKey("APPC", null, "ProcessParameterDefinition", null);
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
	public void testMergeParamsFail() {
			try {


				String propertyfileName = "APPC/ProcessParameterDefinition/MergeParamsFail.properties";
				Map<String, Object> serviceReferences = new HashMap<String, Object>();

				
			
								
				
				// Register Call graphs
				String injectGraphXmls[] = new String[] { getProcessPDXML };


				GraphKey  graphKey = new GraphKey("APPC", null, "ProcessParameterDefinition", null);
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
	public void testSaveMdSal() {
		try {


			String propertyfileName = "APPC/ProcessParameterDefinition/SaveMdsalFail.properties";
			Map<String, Object> serviceReferences = new HashMap<String, Object>();

			
		
			serviceReferences.put("org.openecomp.sdnc.config.params.parser.PropertyDefinitionNode", new org.openecomp.sdnc.config.params.parser.PropertyDefinitionNode());

			
			
			// Register Call graphs
			String injectGraphXmls[] = new String[] { getProcessPDXML };


			GraphKey  graphKey = new GraphKey("APPC", null, "ProcessParameterDefinition", null);
			DGTestCase tc = new DGTestCase(graphKey);
			tc.setInjectGraphXmls(injectGraphXmls);
			tc.setServiceReferences(serviceReferences);
			tc.setPropertyfileName(propertyfileName);
			

			SvcLogicContext ctx = new SvcLogicContext();
			processTestCase(tc, ctx);
			
			//System.out.println(ctx.getAttribute("tmp.allParams.configuration-parameters"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
*/	
}



	
