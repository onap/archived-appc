/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications Copyright (C) 2018 Ericsson
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

package org.onap.sdnc.config.generator.reader;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.sdnc.config.generator.ConfigGeneratorConstant;

public class TestReaderNode {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void testGetFileDataSuccess() throws SvcLogicException, IOException {
        ReaderNode r = new ReaderNode();
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX, "test");
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_FILE_NAME,
                "src/test/resources/convert/payload_cli_config.json");
        SvcLogicContext ctx = new SvcLogicContext();
        r.getFileData(inParams, ctx);
        assertEquals(ConfigGeneratorConstant.OUTPUT_STATUS_SUCCESS,
                ctx.getAttribute("test." + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS));
    }

    @Test
    public void testGetFileDataFailure() throws SvcLogicException, IOException {
        ReaderNode r = new ReaderNode();
        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX, "");
        inParams.put(ConfigGeneratorConstant.INPUT_PARAM_FILE_NAME,
                "non_existent_filename");
        SvcLogicContext ctx = new SvcLogicContext();
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage("File 'non_existent_filename' does not exist");
        r.getFileData(inParams, ctx);
    }
}
