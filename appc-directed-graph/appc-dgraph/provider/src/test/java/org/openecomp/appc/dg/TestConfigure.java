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

/* need to move to open source
import com.att.sdnctl.dgtestlibrary.AbstractDGTestCase;
import com.att.sdnctl.dgtestlibrary.DGTestCase;
import com.att.sdnctl.dgtestlibrary.GraphKey;
*/
import org.openecomp.sdnc.sli.SvcLogicContext;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class TestConfigure /* extends AbstractDGTestCase */ {
	private static final EELFLogger logger = EELFManager.getInstance().getLogger(TestConfigure.class);
	public static String ConfigureXML = "src/main/resources/xml/APPC_Configure.xml";

/*
	@Test
	public void testTemplateConfigureSuccess() {
		try {

			String propertyfileName = "APPC/Configure/Configure_Success.properties";
		
			// Register Call graphs
			String injectGraphXmls[] = new String[] { ConfigureXML };

			Map<String, Object> serviceReferences = new HashMap<String, Object>();
		
	
		
			GraphKey  graphKey = new GraphKey("APPC", null, "Configure", null);
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
	public void testTemplateConfigureFailure() {
		try {

			String propertyfileName = "APPC/Configure/Configure_Failure.properties";
		
			// Register Call graphs
			String injectGraphXmls[] = new String[] { ConfigureXML };

			Map<String, Object> serviceReferences = new HashMap<String, Object>();
		
		
		
			GraphKey  graphKey = new GraphKey("APPC", null, "Configure", null);
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
	public void testGetTemplateFailure() {
		try {

			String propertyfileName = "APPC/Configure/GetTemplate_Failure.properties";
		
			// Register Call graphs
			String injectGraphXmls[] = new String[] { ConfigureXML };

			Map<String, Object> serviceReferences = new HashMap<String, Object>();
		
		
		
			GraphKey  graphKey = new GraphKey("APPC", null, "Configure", null);
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
	public void testGenerateTemplateConfigFailure() {
		try {

			String propertyfileName = "APPC/Configure/GenerateTemplateConfig_Failure.properties";
		
			// Register Call graphs
			String injectGraphXmls[] = new String[] { ConfigureXML };

			Map<String, Object> serviceReferences = new HashMap<String, Object>();
	
		
			GraphKey  graphKey = new GraphKey("APPC", null, "Configure", null);
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
	public void testConfigBlockConfigureSuccess() {
		try {

			String propertyfileName = "APPC/Configure/ConfigBlockConfigure_Success.properties";
		
			// Register Call graphs
			String injectGraphXmls[] = new String[] { ConfigureXML };

			Map<String, Object> serviceReferences = new HashMap<String, Object>();
		
	
			GraphKey  graphKey = new GraphKey("APPC", null, "Configure", null);
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
	public void testConfigBlockConfigureFailure() {
		try {

			String propertyfileName = "APPC/Configure/ConfigBlockConfigure_Failure.properties";
		
			// Register Call graphs
			String injectGraphXmls[] = new String[] { ConfigureXML };

			Map<String, Object> serviceReferences = new HashMap<String, Object>();
		
	
			GraphKey  graphKey = new GraphKey("APPC", null, "Configure", null);
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
	public void testGenerateConfigFailure() {
		try {

			String propertyfileName = "APPC/Configure/GenerateConfig_Failure.properties";
		
			// Register Call graphs
			String injectGraphXmls[] = new String[] { ConfigureXML };

			Map<String, Object> serviceReferences = new HashMap<String, Object>();
		
	
		
			GraphKey  graphKey = new GraphKey("APPC", null, "Configure", null);
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
*/
}
