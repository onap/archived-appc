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


import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.appc.dg.mock.instance.MockConfigureNodeExecutor;
import org.openecomp.appc.dg.mock.instance.MockSvcLogicJavaPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.powermock.api.mockito.PowerMockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
/* need to move to open source
import com.att.sdnctl.dgtestlibrary.AbstractDGTestCase;
import com.att.sdnctl.dgtestlibrary.DGTestCase;
import com.att.sdnctl.dgtestlibrary.GraphKey;
import org.apache.sling.testing.mock.osgi.MockOsgi;
*/
import org.openecomp.sdnc.sli.SvcLogicContext;
import org.openecomp.sdnc.sli.provider.ExecuteNodeExecutor;
import org.openecomp.sdnc.sli.provider.SvcLogicAdaptorFactory;


public class TestGenerateConfig /* extends AbstractDGTestCase */ {
	private static final EELFLogger logger = EELFManager.getInstance().getLogger(TestGenerateConfig.class);
	public static String generateConfigXML = "src/main/resources/xml/APPC_GenerateConfig.xml";

/*

	@Test
	public void testGenerateConfigWithParameters() {
		try {

			String propertyfileName = "APPC/GenerateConfig/GenerateConfigWithParams.properties";
			//String generateConfigXML = "src/main/resources/xml/APPC_GenerateConfig.xml";

			// Register Call graphs
			String injectGraphXmls[] = new String[] { generateConfigXML };

			Map<String, Object> serviceReferences = new HashMap<String, Object>();
	
			serviceReferences.put("org.openecomp.sdnc.config.generator.merge.MergeNode", new org.openecomp.sdnc.config.generator.merge.MergeNode());
			//serviceReferences.put("com.att.appc.config.generator.node.ConfigResourceNode", new MockConfigResourceNode());
			
			serviceReferences.put("org.openecomp.appc.data.services.node.ConfigResourceNode", new MockSvcLogicJavaPlugin());

		
			GraphKey  graphKey = new GraphKey("APPC", null, "GenerateConfig", null);
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
	public void testGenerateConfigWithNoParameters() {
		try {

			String propertyfileName = "APPC/GenerateConfig/GenerateConfigNoParams.properties";
			//String generateConfigXML = "src/main/resources/xml/APPC_GenerateConfig.xml";

			// Register Call graphs
			String injectGraphXmls[] = new String[] { generateConfigXML };

			Map<String, Object> serviceReferences = new HashMap<String, Object>();
	
			serviceReferences.put("org.openecomp.sdnc.config.generator.merge.MergeNode", new org.openecomp.sdnc.config.generator.merge.MergeNode());
			//serviceReferences.put("com.att.appc.config.generator.node.ConfigResourceNode", new MockConfigResourceNode());
			
			serviceReferences.put("org.openecomp.appc.data.services.node.ConfigResourceNode", new MockSvcLogicJavaPlugin());


			GraphKey  graphKey = new GraphKey("APPC", null, "GenerateConfig", null);
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
