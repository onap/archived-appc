/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
 * Modfication Copyright (C) 2018 IBM.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.sdnc.config.generator.writer;

import static org.junit.Assert.assertEquals;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.sdnc.config.generator.ConfigGeneratorConstant;

public class TestFileWriterNode {

    @Test
    public void writeFile() throws Exception {
        FileWriterNode FileWriterNode = new FileWriterNode();
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_FILE_NAME,
                "src/test/resources/writer/testcvaas.json");
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_REQUEST_DATA,
                "{'name':'Name','role':'admin'}");
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX, "test");
        SvcLogicContext ctx = new SvcLogicContext();
        FileWriterNode.writeFile(inParams, ctx);
        assertEquals(ctx.getAttribute("test." + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS),
                ConfigGeneratorConstant.OUTPUT_STATUS_SUCCESS);
    }
    
    @Test(expected=Exception.class)
    public void testWriteFileForEmptyParams() throws Exception {
        FileWriterNode FileWriterNode = new FileWriterNode();
        Map<String, String> inParams = new HashMap<String, String>();
        SvcLogicContext ctx = new SvcLogicContext();
        FileWriterNode.writeFile(inParams, ctx);
      }
}
