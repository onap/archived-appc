/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 IBM.
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

package org.onap.appc.seqgen.objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class CapabilityModelTest {

    private CapabilityModel capabilityModel;

    private List<String> vnfCapabilities = new ArrayList<>();
    private List<String> vfModuleCapabilities = new ArrayList<>();
    private Map<String, List<String>> vmCapabilities = new HashMap<>();
    private List<String> vnfcCapabilities = new ArrayList<>();

    @Before
    public void setUp() {
        capabilityModel = new CapabilityModel(vnfCapabilities, vfModuleCapabilities, vmCapabilities, vnfcCapabilities);
    }

    @Test
    public void testVnfCapabilities() {
        assertEquals(vnfCapabilities, capabilityModel.getVnfCapabilities());
    }

    @Test
    public void testVfModuleCapabilities() {
        assertEquals(vfModuleCapabilities, capabilityModel.getVfModuleCapabilities());
    }
    
    @Test
    public void testToString() {
        assertTrue(capabilityModel.toString() instanceof String);
    }

}
