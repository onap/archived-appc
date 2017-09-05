/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
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
 * 
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdnc.config.generator.reader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.openecomp.sdnc.config.generator.ConfigGeneratorConstant;
import org.openecomp.sdnc.config.generator.merge.TestMergeNode;
import org.openecomp.sdnc.config.generator.reader.ReaderNode;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

public class TestReaderNode {
    @Test(expected = Exception.class)
    public void testGetFileData() throws SvcLogicException, IOException {
        ReaderNode r = new ReaderNode();
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX, "test");
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_FILE_NAME, IOUtils
                .toString(TestMergeNode.class.getClassLoader().getResourceAsStream("convert/payload_cli_config.json")));
        SvcLogicContext ctx = new SvcLogicContext();
        r.getFileData(inParams, ctx);
    }
}
