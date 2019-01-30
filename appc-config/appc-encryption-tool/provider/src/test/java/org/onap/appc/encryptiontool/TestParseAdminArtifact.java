/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018-2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.appc.encryptiontool;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.onap.appc.encryptiontool.fqdn.ParseAdminArtifcat;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

import com.fasterxml.jackson.core.JsonProcessingException;

public class TestParseAdminArtifact {
    SvcLogicContext ctx = null;
    ParseAdminArtifcat parseAdminArtifact = null;

    @Before
    public void setup() throws IOException {
        parseAdminArtifact = new MockParseAdminArtifcat();
    }

    @Test
    public void testRetrieveFqdn() throws Exception {
        ctx = new SvcLogicContext();
        ctx.setAttribute("tenantAai", "tenantuuid1");
        ctx.setAttribute("cloudOwneraai", "aic3.0");
        ctx.setAttribute("cloudRegionAai", "san4a");
        try {
            String fqdnMatchPayload = parseAdminArtifact.retrieveFqdn(ctx);
            assertEquals("fqdn-value1 url:port", fqdnMatchPayload);

        } catch (SvcLogicException | RuntimeException | IOException e) {
            e.printStackTrace();
            throw new Exception("Failed to retrieve fqdn:"+ e.getMessage());
        }

    }

}
