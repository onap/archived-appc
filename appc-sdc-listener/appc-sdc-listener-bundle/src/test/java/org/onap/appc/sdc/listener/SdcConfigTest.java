/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.sdc.listener;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Properties;

import org.junit.Test;

public class SdcConfigTest {

    @Test
    public void testGetMsgBusProperties() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("appc.sdc.provider.url",
                "http://localhost:8181/restconf/operations/SdcMessage:configuration-document-request");
        properties.setProperty("appc.ClosedLoop.poolMembers","localhost:3905");
        SdcConfig sdc = new SdcConfig(properties);
        List<String> uebAddresses = sdc.getMsgBusProperties();
        assertFalse(uebAddresses.isEmpty());
    }
}
