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

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
/* need to move to opensource
import com.att.sdnctl.dgtestlibrary.AbstractDGTestCase;
import com.att.sdnctl.dgtestlibrary.DGMockUtils;
import com.att.sdnctl.dgtestlibrary.DGTestCase;
import com.att.sdnctl.dgtestlibrary.GraphKey;
*/
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

public class TestDownloadCliConfig /* extends AbstractDGTestCase */ {

/*
	private static final EELFLogger logger = EELFManager.getInstance().getLogger(TestDownloadIsbcConfig.class);
	public static String DownloadCliConfigXML = "src/main/resources/xml/APPC_DownloadCliConfig.xml";

	@Test
	public void testDownloadCliConfigSuccess() {
		try {
			String propertyfileName = "APPC/DownloadCliConfig/DownloadCliConfig_Success.properties";

			// Register Call graphs
			String injectGraphXmls[] = new String[] { DownloadCliConfigXML };

			Map<String, Object> serviceReferences = new HashMap<String, Object>();

			GraphKey graphKey = new GraphKey("APPC", null, "DownloadCliConfig", null);
			DGTestCase tc = new DGTestCase(graphKey);
			tc.setInjectGraphXmls(injectGraphXmls);
			tc.setServiceReferences(serviceReferences);
			tc.setPropertyfileName(propertyfileName);
			
			Map<String, Object> resourceReferences = new HashMap<String, Object>();
			tc.setResourceReferences(resourceReferences);

			SvcLogicContext ctx = new SvcLogicContext();
			processTestCase(tc, ctx);
			
			//DGMockUtils.printContext(ctx);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testDownloadCliConfigDBFailure() {
		try {

			String propertyfileName = "APPC/DownloadCliConfig/DownloadCliConfig_DB_Failure.properties";
			// Register Call graphs
			String injectGraphXmls[] = new String[] { DownloadCliConfigXML };
			Map<String, Object> serviceReferences = new HashMap<String, Object>();

			GraphKey graphKey = new GraphKey("APPC", null, "DownloadCliConfig", null);
			DGTestCase tc = new DGTestCase(graphKey);
			tc.setInjectGraphXmls(injectGraphXmls);
			tc.setServiceReferences(serviceReferences);
			tc.setPropertyfileName(propertyfileName);
			
			Map<String, Object> resourceReferences = new HashMap<String, Object>();
			tc.setResourceReferences(resourceReferences);

			SvcLogicContext ctx = new SvcLogicContext();
			processTestCase(tc, ctx);
			
			//DGMockUtils.printContext(ctx);


		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testDownloadCliConfigAdaptorFailure() {
		try {

			String propertyfileName = "APPC/DownloadCliConfig/DownloadCliConfig_Adaptor_Failure.properties";

			// Register Call graphs
			String injectGraphXmls[] = new String[] { DownloadCliConfigXML };

			Map<String, Object> serviceReferences = new HashMap<String, Object>();

			GraphKey graphKey = new GraphKey("APPC", null, "DownloadCliConfig", null);
			DGTestCase tc = new DGTestCase(graphKey);

			tc.setInjectGraphXmls(injectGraphXmls);
			tc.setServiceReferences(serviceReferences);
			tc.setPropertyfileName(propertyfileName);
			
			Map<String, Object> resourceReferences = new HashMap<String, Object>();
			tc.setResourceReferences(resourceReferences);

			SvcLogicContext ctx = new SvcLogicContext();
			processTestCase(tc, ctx);
			
			//DGMockUtils.printContext(ctx);


		} catch (Exception e) {
			e.printStackTrace();
		}
	}
*/
}
