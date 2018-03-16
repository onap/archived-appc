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
* ============LICENSE_END=========================================================
*/
package org.onap.appc.domainmodel.lcm;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestRequestStatus {

    private RequestStatus requestStatus = RequestStatus.ACCEPTED;

    @Test
    public void testName() {
        assertEquals("ACCEPTED", requestStatus.name());
    }

    @Test
    public void testEquals() {
        assertTrue(requestStatus.equals(RequestStatus.ACCEPTED));
        assertFalse(requestStatus.equals(null));
    }

    @Test
    public void testGetDescription() {
        assertEquals("Request has been accepted and is in progress.", requestStatus.getDescription());
    }

    @Test
    public void testGetExternalActionStatusName() {
        assertEquals(ExternalActionStatus.IN_PROGRESS.name(), requestStatus.getExternalActionStatusName());
    }

    @Test
    public void testGetExternalActionStatus() {
        assertEquals(ExternalActionStatus.IN_PROGRESS, requestStatus.getExternalActionStatus());
    }

    @Test
    public void testIsTerminal() {
        assertEquals(false, requestStatus.isTerminal());
    }

}
