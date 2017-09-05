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
import org.openecomp.appc.dg.mock.instance.MockSvcLogicJavaPlugin;

/* need to move to open source
import com.att.sdnctl.dgtestlibrary.AbstractDGTestCase;
import com.att.sdnctl.dgtestlibrary.DGTestCase;
import com.att.sdnctl.dgtestlibrary.GraphKey;
*/
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class TestDownloadIsbcConfig /* extends AbstractDGTestCase */{
	private static final EELFLogger logger = EELFManager.getInstance().getLogger(TestDownloadIsbcConfig.class);
	public static String DownloadIsbcConfigXML = "src/main/resources/xml/APPC_DownloadIsbcConfig.xml";

/*
	@Test
	public void testDownloadIsbcConfigSuccess() {
		try {

			String propertyfileName = "APPC/DownloadIsbcConfig/DownloadIsbcConfig_Success.properties";
		
			// Register Call graphs
			String injectGraphXmls[] = new String[] { DownloadIsbcConfigXML };

			Map<String, Object> serviceReferences = new HashMap<String, Object>();
		
			
		
		
			GraphKey  graphKey = new GraphKey("APPC", null, "DownloadIsbcConfig", null);
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
	public void testConfigFileFailure() {
		try {

			String propertyfileName = "APPC/DownloadIsbcConfig/ConfigFile_Failure.properties";
		
			// Register Call graphs
			String injectGraphXmls[] = new String[] { DownloadIsbcConfigXML };

			Map<String, Object> serviceReferences = new HashMap<String, Object>();
		
		
		
			GraphKey  graphKey = new GraphKey("APPC", null, "DownloadIsbcConfig", null);
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
	public void testPutFailure() {
		try {

			String propertyfileName = "APPC/DownloadIsbcConfig/Put_Failure.properties";
		
			// Register Call graphs
			String injectGraphXmls[] = new String[] { DownloadIsbcConfigXML };

			Map<String, Object> serviceReferences = new HashMap<String, Object>();
		
			
		
		
			GraphKey  graphKey = new GraphKey("APPC", null, "DownloadIsbcConfig", null);
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
