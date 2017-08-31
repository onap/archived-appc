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

package org.openecomp.sdnc.config.generator.transform;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.openecomp.sdnc.config.generator.ConfigGeneratorConstant;
import org.openecomp.sdnc.config.generator.merge.TestMergeNode;
import org.openecomp.sdnc.config.generator.transform.XSLTTransformerNode;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.openecomp.sdnc.sli.SvcLogicContext;

public class TestXSLTTransformerNode {
    
    private static final EELFLogger log = EELFManager.getInstance().getLogger(TestXSLTTransformerNode.class);

    @Test
    public void transformData() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        String templateData = IOUtils.toString(
                TestMergeNode.class.getClassLoader().getResourceAsStream("transform/template.xsl"),
                Charset.defaultCharset());
        String requestData = IOUtils.toString(
                TestMergeNode.class.getClassLoader().getResourceAsStream("transform/request.xml"),
                Charset.defaultCharset());
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_TEMPLATE_DATA, templateData);
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_REQUEST_DATA, requestData);
        XSLTTransformerNode transformerNode = new XSLTTransformerNode();
        transformerNode.transformData(inParams, ctx);
        log.info("transformData Result: " + ctx.getAttribute(ConfigGeneratorConstant.OUTPUT_PARAM_TRANSFORMED_DATA));

    }
}
