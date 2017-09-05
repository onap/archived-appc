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

/* move to open source
import com.att.sdnctl.dgtestlibrary.AbstractDGTestCase;
import com.att.sdnctl.dgtestlibrary.DGTestCase;
import com.att.sdnctl.dgtestlibrary.GraphKey;
*/
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;


public class TestGetConfigParams /* extends AbstractDGTestCase */ {
	

/*
	
	public static String getConfigParamsXML = "src/main/resources/xml/APPC_GetConfigParams.xml";



	@Test
	public void testGetConfigParamsWithDefaultTemplate() {
		try {

			String propertyfileName = "APPC/GetConfigParams/DefaultTemplate.properties";
			Map<String, Object> serviceReferences = new HashMap<String, Object>();

			
			serviceReferences.put("org.openecomp.sdnc.config.generator.convert.ConvertNode", new org.openecomp.sdnc.config.generator.convert.ConvertNode());

			// Register Call graphs
			String injectGraphXmls[] = new String[] { getConfigParamsXML };


			GraphKey  graphKey = new GraphKey("APPC", null, "GetConfigParams", null);
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
	public void testGetConfigParamsWithTemplateName() {
		try {


			String propertyfileName = "APPC/GetConfigParams/TemplateName.properties";
			Map<String, Object> serviceReferences = new HashMap<String, Object>();

			serviceReferences.put("org.openecomp.sdnc.config.generator.convert.ConvertNode", new org.openecomp.sdnc.config.generator.convert.ConvertNode());

			// Register Call graphs
			String injectGraphXmls[] = new String[] { getConfigParamsXML };


			GraphKey  graphKey = new GraphKey("APPC", null, "GetConfigParams", null);
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
	public void testGetConfigParamsWithCliString() {
		try {

			String propertyfileName = "APPC/GetConfigParams/CliString.properties";
			Map<String, Object> serviceReferences = new HashMap<String, Object>();
			serviceReferences.put("org.openecomp.sdnc.config.generator.convert.ConvertNode", new org.openecomp.sdnc.config.generator.convert.ConvertNode());

			
			// Register Call graphs
			String injectGraphXmls[] = new String[] { getConfigParamsXML };


			GraphKey  graphKey = new GraphKey("APPC", null, "GetConfigParams", null);
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
	public void testGetConfigParamsWithCliJson() {
		try {


			String propertyfileName = "APPC/GetConfigParams/CliJson.properties";
			Map<String, Object> serviceReferences = new HashMap<String, Object>();

			
		
			serviceReferences.put("org.openecomp.sdnc.config.generator.convert.ConvertNode", new org.openecomp.sdnc.config.generator.convert.ConvertNode());

			// Register Call graphs
			String injectGraphXmls[] = new String[] { getConfigParamsXML };


			GraphKey  graphKey = new GraphKey("APPC", null, "GetConfigParams", null);
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
	public void testGetConfigParamsForRestore() {
		try {
			String propertyfileName = "APPC/GetConfigParams/Restore.properties";
			Map<String, Object> serviceReferences = new HashMap<String, Object>();

		
		
			serviceReferences.put("org.openecomp.sdnc.config.generator.convert.ConvertNode", new org.openecomp.sdnc.config.generator.convert.ConvertNode());

			// Register Call graphs
			String injectGraphXmls[] = new String[] { getConfigParamsXML };


			GraphKey  graphKey = new GraphKey("APPC", null, "GetConfigParams", null);
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
	public void testGetConfigParamsCommonConfigFail() {
		try {
			String propertyfileName = "APPC/GetConfigParams/CommonConfigFail.properties";
			Map<String, Object> serviceReferences = new HashMap<String, Object>();

		
			serviceReferences.put("org.openecomp.sdnc.config.generator.convert.ConvertNode", new org.openecomp.sdnc.config.generator.convert.ConvertNode());

			// Register Call graphs
			String injectGraphXmls[] = new String[] { getConfigParamsXML };


			GraphKey  graphKey = new GraphKey("APPC", null, "GetConfigParams", null);
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
	public void testGetConfigParamsJson2DGContextFail() {
		try {
			String propertyfileName = "APPC/GetConfigParams/Json2DGContextFail.properties";
			Map<String, Object> serviceReferences = new HashMap<String, Object>();

			
		
			
		
			// Register Call graphs
			String injectGraphXmls[] = new String[] { getConfigParamsXML };


			GraphKey  graphKey = new GraphKey("APPC", null, "GetConfigParams", null);
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
