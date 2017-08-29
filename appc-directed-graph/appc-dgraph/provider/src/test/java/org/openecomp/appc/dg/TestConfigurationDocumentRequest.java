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

/* move to opensource
import com.att.sdnctl.dgtestlibrary.AbstractDGTestCase;
import com.att.sdnctl.dgtestlibrary.DGTestCase;
import com.att.sdnctl.dgtestlibrary.GraphKey;
*/
import org.openecomp.sdnc.sli.SvcLogicContext;

public class TestConfigurationDocumentRequest /* extends AbstractDGTestCase */ {

/*
	@Test
	public void testSDCMessage() {
		try {
			String propertyfileName = "SDC-MESSAGE_method_configuration-document-request_TC1.properties";

			String configurationDocumentRequestXML = "src/main/resources/xml/SDC-MESSAGE_method_configuration-document-request_3.0.0.xml";


			// Register Call graphs
			String injectGraphXmls[] = new String[] { configurationDocumentRequestXML};

			Map<String, Object> serviceReferences = new HashMap<String, Object>();
			serviceReferences.put("org.openecomp.sdnc.config.generator.convert.ConvertNode",new org.openecomp.sdnc.config.generator.convert.ConvertNode());
			serviceReferences.put("org.openecomp.sdnc.config.generator.merge.MergeNode", new org.openecomp.sdnc.config.generator.merge.MergeNode());

			GraphKey  graphKey = new GraphKey("SDC-MESSAGE", null, "configuration-document-request", null);
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
