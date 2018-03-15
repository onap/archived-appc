/*
* ============LICENSE_START=======================================================
* ONAP : APPC
* ================================================================================
* Copyright 2018 TechMahindra
*=================================================================================
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.onap.appc.lifecyclemanager.objects;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestNoTransitionDefinedException {
    @Test
    public void testConstructorWithMessage() throws Exception {
        String message = "testing message";
        NoTransitionDefinedException noTransitionDefinedException = new NoTransitionDefinedException(message,"currentState" , "event");
        assertTrue(noTransitionDefinedException.getCause() == null);
        assertEquals(message, noTransitionDefinedException.getLocalizedMessage());
        assertEquals(message, noTransitionDefinedException.getMessage());
    }
}
