/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.seqgen.objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

public class RequestInfoBuilderTest {
    private RequestInfoBuilder builder;

    @Before
    public void setUp() {
        builder = new RequestInfoBuilder();
    }

    @Test
    public void testAction() {
        String action = builder.action("Action").build().getAction();
        assertNotNull(action);
        assertEquals(action, "Action");
    }


    @Test
    public void testActionLevel() {
        String actionLevel = builder.actionLevel("ActionLevel").build().getActionLevel();
        assertNotNull(actionLevel);
        assertEquals(actionLevel, "ActionLevel");
    }


    @Test
    public void testPayload() {
        String payload = builder.payload("Payload").build().getPayload();
        assertNotNull(payload);
        assertEquals(payload, "Payload");
    }

    @Test
    public void testActionIdentifier() {
        assertNotNull(builder.actionIdentifier());
        assertNotNull(builder.build().getActionIdentifier());
    }

    @Test
    public void testVnfId() {
        assertNotNull(builder.actionIdentifier());
        String vnfId = builder.vnfId("VnfId").build().getActionIdentifier().getVnfId();
        assertNotNull(vnfId);
        assertEquals(vnfId, "VnfId");
    }

    @Test
    public void testVnfcName() {
        assertNotNull(builder.actionIdentifier());
        String vnfcName = builder.vnfcName("VnfcName").build().getActionIdentifier().getVnfcName();
        assertNotNull(vnfcName);
        assertEquals(vnfcName, "VnfcName");
    }

    @Test
    public void testVServerId() {
        assertNotNull(builder.actionIdentifier());
        String vServerId = builder.vServerId("VServerId").build().getActionIdentifier().getvServerId();
        assertNotNull(vServerId);
        assertEquals(vServerId, "VServerId");
    }

    @Test
    public void testBuild() {
        RequestInfo info = builder.actionIdentifier()
                                  .vnfId("VnfId")
                                  .vnfcName("VnfcName")
                                  .vServerId("VServerId")
                                  .action("Action")
                                  .actionLevel("ActionLevel")
                                  .payload("Payload")
                                  .build();
        assertNotNull(info);
        ActionIdentifier id = info.getActionIdentifier();
        assertNotNull(id);
        String str;
        str = id.getVnfId();
        assertNotNull(str);
        assertEquals(str, "VnfId");
        str = id.getVnfcName();
        assertNotNull(str);
        assertEquals(str, "VnfcName");
        str = id.getvServerId();
        assertNotNull(str);
        assertEquals(str, "VServerId");
        str = info.getAction();
        assertNotNull(str);
        assertEquals(str, "Action");
        str = info.getActionLevel();
        assertNotNull(str);
        assertEquals(str, "ActionLevel");
        str = info.getPayload();
        assertNotNull(str);
        assertEquals(str, "Payload");
    }

}
