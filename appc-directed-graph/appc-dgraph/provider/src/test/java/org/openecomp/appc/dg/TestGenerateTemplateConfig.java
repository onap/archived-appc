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

import org.junit.Test;
import org.openecomp.appc.dg.mock.instance.MockConfigureNodeExecutor;
import org.openecomp.appc.dg.mock.instance.MockSvcLogicJavaPlugin;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
/* need to move to opensource
import com.att.sdnctl.dgtestlibrary.AbstractDGTestCase;
import com.att.sdnctl.dgtestlibrary.DGTestCase;
import com.att.sdnctl.dgtestlibrary.GraphKey;
*/
import org.openecomp.sdnc.sli.SvcLogicContext;

public class TestGenerateTemplateConfig /* extends AbstractDGTestCase */ {
	private static final EELFLogger logger = EELFManager.getInstance().getLogger(TestGenerateTemplateConfig.class);
/*
	public static String generateConfigXML = "src/main/resources/xml/APPC_GenerateTemplateConfig.xml";

	@Test
	public void testGenerateTemplateConfigWithParameters() {
		try {

			String propertyfileName = "APPC/GenerateTemplateConfig/GenerateTemplateConfigWithParams.properties";
			Map<String, Object> serviceReferences = new HashMap<String, Object>();

			
			serviceReferences.put("org.openecomp.appc.data.services.node.ConfigResourceNode", new MockSvcLogicJavaPlugin());
			serviceReferences.put("org.openecomp.sdnc.config.generator.merge.MergeNode", new org.openecomp.sdnc.config.generator.merge.MergeNode());

			// Register Call graphs
			String injectGraphXmls[] = new String[] { generateConfigXML };

			GraphKey  graphKey = new GraphKey("APPC", null, "GenerateTemplateConfig", null);
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
	public void testGenerateTemplateConfigWithNoParameters() {
		try {

			String propertyfileName = "APPC/GenerateTemplateConfig/GenerateTemplateConfigNoParams.properties";
			Map<String, Object> serviceReferences = new HashMap<String, Object>();

		
			serviceReferences.put("org.openecomp.appc.data.services.node.ConfigResourceNode", new MockSvcLogicJavaPlugin());
			serviceReferences.put("org.openecomp.sdnc.config.generator.merge.MergeNode", new org.openecomp.sdnc.config.generator.merge.MergeNode());

			// Register Call graphs
			String injectGraphXmls[] = new String[] { generateConfigXML };

			GraphKey  graphKey = new GraphKey("APPC", null, "GenerateTemplateConfig", null);
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
