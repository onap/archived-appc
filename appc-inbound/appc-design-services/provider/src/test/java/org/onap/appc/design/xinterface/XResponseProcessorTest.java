/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 Ericsson
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
 *
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.design.xinterface;

import java.io.IOException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.appc.design.services.util.DesignServiceConstants;

public class XResponseProcessorTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void testParseNull() throws Exception {
        XResponseProcessor xResponseProcessor = new XResponseProcessor();
        expectedEx.expect(IOException.class);
        expectedEx.expectMessage("Cannot find Property file");
        xResponseProcessor.parseResponse("{}", "{}");
    }

    @Test
    public void testParseV4() throws Exception {
        XResponseProcessor xResponseProcessor = new XResponseProcessor();
        expectedEx.expect(IOException.class);
        expectedEx.expectMessage("Cannot find Property file");
        xResponseProcessor.parseResponse("{\"" + DesignServiceConstants.INSTAR_V4_ADDRESS + "\":\"TEST\", \"" + DesignServiceConstants.INSTAR_V4_MASK + "\":\"TEST\"}", "{}");
    }

    @Test
    public void testParseV6() throws Exception {
        XResponseProcessor xResponseProcessor = new XResponseProcessor();
        expectedEx.expect(IOException.class);
        expectedEx.expectMessage("Cannot find Property file");
        xResponseProcessor.parseResponse("{\"" + DesignServiceConstants.INSTAR_V6_ADDRESS + "\":\"TEST\", \"" + DesignServiceConstants.INSTAR_V6_MASK + "\":\"TEST\"}", "{}");
    }
}
