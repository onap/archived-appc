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


import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
/* need to move to open source
import com.att.sdnctl.dgtestlibrary.AbstractDGTestCase;
import com.att.sdnctl.dgtestlibrary.DGTestCase;
import com.att.sdnctl.dgtestlibrary.GraphKey;
*/
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

public class TestGetDeviceRunningConfig /* extends AbstractDGTestCase */ {
	private static final EELFLogger logger = EELFManager.getInstance().getLogger(TestGetConfigParams.class);
/*
	public String jsonPath = "src/main/resources/json";
	public String xmlpath = "src/main/resources/xml";

	@Test
	public void testChefRunningConfigSuccess() {
		try {
			logger.info("********************************* testChefRunningConfigSuccess *************************************");
			//DGMockUtils.generateXMLFile(jsonPath, xmlpath);

			String propertyfileName = "APPC/GetDeviceRunningConfig/Chef_Success.properties";

			String getDeviceRunningConfigXML = "src/main/resources/xml/APPC_GetDeviceRunningConfig.xml";
			// Register Call graphs
			String injectGraphXmls[] = new String[] { getDeviceRunningConfigXML };


			GraphKey  graphKey = new GraphKey("APPC", null, "GetDeviceRunningConfig", null);
			DGTestCase tc = new DGTestCase(graphKey);
			tc.setInjectGraphXmls(injectGraphXmls);
			tc.setPropertyfileName(propertyfileName);

			SvcLogicContext ctx = new SvcLogicContext();
			processTestCase(tc, ctx);

			assertContextWithProperty(ctx);


		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testChefRunningConfigFailure() {
		try {

			logger.info("********************************* testChefRunningConfigFailure *************************************");
			String propertyfileName = "APPC/GetDeviceRunningConfig/Chef_Failure.properties";

			String getDeviceRunningConfigXML = "src/main/resources/xml/APPC_GetDeviceRunningConfig.xml";
			// Register Call graphs
			String injectGraphXmls[] = new String[] { getDeviceRunningConfigXML };


			GraphKey  graphKey = new GraphKey("APPC", null, "GetDeviceRunningConfig", null);
			DGTestCase tc = new DGTestCase(graphKey);
			tc.setInjectGraphXmls(injectGraphXmls);
			tc.setPropertyfileName(propertyfileName);


			SvcLogicContext ctx = new SvcLogicContext();
			processTestCase(tc, ctx);

			assertContextWithProperty(ctx);

			//DGMockUtils.printContext(ctx);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testChefRunningConfigOthers() {
		try {
			logger.info("********************************* testChefRunningConfigOthers *************************************");
			String propertyfileName = "APPC/GetDeviceRunningConfig/Chef_Others.properties";

			String getDeviceRunningConfigXML = "src/main/resources/xml/APPC_GetDeviceRunningConfig.xml";
			// Register Call graphs
			String injectGraphXmls[] = new String[] { getDeviceRunningConfigXML };


			GraphKey  graphKey = new GraphKey("APPC", null, "GetDeviceRunningConfig", null);
			DGTestCase tc = new DGTestCase(graphKey);
			tc.setInjectGraphXmls(injectGraphXmls);
			tc.setPropertyfileName(propertyfileName);


			SvcLogicContext ctx = new SvcLogicContext();
			processTestCase(tc, ctx);

			assertContextWithProperty(ctx);

			//DGMockUtils.printContext(ctx);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
*/
}
