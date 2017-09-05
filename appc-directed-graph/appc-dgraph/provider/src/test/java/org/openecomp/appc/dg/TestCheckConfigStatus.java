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

/* TODO:  move the dgtest to opensource
import com.att.sdnctl.dgtestlibrary.AbstractDGTestCase;
import com.att.sdnctl.dgtestlibrary.DGTestCase;
import com.att.sdnctl.dgtestlibrary.GraphKey;
*/
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class TestCheckConfigStatus /* extends AbstractDGTestCase */ {
	private static final EELFLogger logger = EELFManager.getInstance().getLogger(TestCheckConfigStatus.class);
	public static String CheckConfigStatusXML = "src/main/resources/xml/APPC_CheckConfigStatus.xml";



/*
	@Test
	public void testCheckConfigStatusSuccess() {
		try {

			String propertyfileName = "APPC/CheckConfigStatus/CheckConfigStatus_Success.properties";
		
			// Register Call graphs
			String injectGraphXmls[] = new String[] { CheckConfigStatusXML };

			Map<String, Object> serviceReferences = new HashMap<String, Object>();
	
			GraphKey  graphKey = new GraphKey("APPC", null, "CheckConfigStatus", null);
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
	public void testCheckConfigStatusFailure() {
		try {

			String propertyfileName = "APPC/CheckConfigStatus/CheckConfigStatus_Failure.properties";
		
			// Register Call graphs
			String injectGraphXmls[] = new String[] { CheckConfigStatusXML };

			Map<String, Object> serviceReferences = new HashMap<String, Object>();
			
		
			GraphKey  graphKey = new GraphKey("APPC", null, "CheckConfigStatus", null);
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
	public void testParseErrorLogFail() {
		try {

			String propertyfileName = "APPC/CheckConfigStatus/ParseErrorLogFail.properties";
		
			// Register Call graphs
			String injectGraphXmls[] = new String[] { CheckConfigStatusXML };

			Map<String, Object> serviceReferences = new HashMap<String, Object>();
			serviceReferences.put("org.openecomp.appc.ccadaptor.ConfigComponentAdaptor", new MockConfigureNodeExecutor());
		
			GraphKey  graphKey = new GraphKey("APPC", null, "CheckConfigStatus", null);
			DGTestCase tc = new DGTestCase(graphKey);

			
			tc.setInjectGraphXmls(injectGraphXmls);
			tc.setServiceReferences(serviceReferences);
			tc.setPropertyfileName(propertyfileName);
			


			SvcLogicContext ctx = new SvcLogicContext();
			processTestCase(tc, ctx);

			//System.out.println("error-message "+  ctx.getAttribute("error-message"));


		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testParseErrorLogSuccess() { 
		try {

			String propertyfileName = "APPC/CheckConfigStatus/ParseErrorLogSuccess.properties";
		
			// Register Call graphs
			String injectGraphXmls[] = new String[] { CheckConfigStatusXML };

			Map<String, Object> serviceReferences = new HashMap<String, Object>();
			serviceReferences.put("org.openecomp.appc.ccadaptor.ConfigComponentAdaptor", new MockConfigureNodeExecutor());
		
			GraphKey  graphKey = new GraphKey("APPC", null, "CheckConfigStatus", null);
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
	public void testMaxRetries() { 
		try {

			String propertyfileName = "APPC/CheckConfigStatus/MaxRetries.properties";
		
			// Register Call graphs
			String injectGraphXmls[] = new String[] { CheckConfigStatusXML };

			Map<String, Object> serviceReferences = new HashMap<String, Object>();
			serviceReferences.put("org.openecomp.appc.ccadaptor.ConfigComponentAdaptor", new MockConfigureNodeExecutor());
		
			GraphKey  graphKey = new GraphKey("APPC", null, "CheckConfigStatus", null);
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
