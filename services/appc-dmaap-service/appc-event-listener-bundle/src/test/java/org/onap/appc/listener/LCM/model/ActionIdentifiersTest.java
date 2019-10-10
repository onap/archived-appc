/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2018 Nokia Solutions and Networks
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
package org.onap.appc.listener.LCM.model;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class ActionIdentifiersTest {

    private ActionIdentifiers actionIdentifiers;


    @Before
    public void setup(){
        actionIdentifiers = new ActionIdentifiers();
    }

    @Test
    public void should_set_properties(){

        actionIdentifiers.setServiceInstanceId("test-instance-id");
        actionIdentifiers.setVnfID("test-vnf-id");
        actionIdentifiers.setVnfcName("test-name");
        actionIdentifiers.setVserverId("test-vserver-id");


        assertEquals("test-instance-id", actionIdentifiers.getServiceInstanceId());
        assertEquals("test-vnf-id", actionIdentifiers.getVnfID());
        assertEquals("test-name", actionIdentifiers.getVnfcName());
        assertEquals("test-vserver-id", actionIdentifiers.getVserverId());
    }

    @Test
    public void should_initialize_parameters_from_constructor(){

        actionIdentifiers.setServiceInstanceId("test-instance-id");
        actionIdentifiers.setVnfID("test-vnf-id");
        actionIdentifiers.setVnfcName("test-name");
        actionIdentifiers.setVserverId("test-vserver-id");

        ActionIdentifiers testObject = new ActionIdentifiers(actionIdentifiers);

        assertEquals("test-instance-id", testObject.getServiceInstanceId());
        assertEquals("test-vnf-id",  testObject.getVnfID());
        assertEquals("test-name",  testObject.getVnfcName());
        assertEquals("test-vserver-id",  testObject.getVserverId());
    }
}
