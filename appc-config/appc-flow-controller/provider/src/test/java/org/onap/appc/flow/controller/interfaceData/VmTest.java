/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 IBM.
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

package org.onap.appc.flow.controller.interfaceData;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class VmTest {
    private Vm vm;

    @Before
    public void setUp() {
        vm = new Vm();
    }

    @Test
    public void testVserverId() {
        vm.setVserverId("VserverId");
        assertEquals("VserverId", vm.getVserverId());
    }

    @Test
    public void testVnfc() {
        Vnfcslist Vnfc = new Vnfcslist();
        vm.setVnfc(Vnfc);
        assertEquals(Vnfc, vm.getVnfc());
    }

    @Test
    public void testVmId() {
        vm.setVmId("VmId");
        assertEquals("VmId", vm.getVmId());
    }
}
