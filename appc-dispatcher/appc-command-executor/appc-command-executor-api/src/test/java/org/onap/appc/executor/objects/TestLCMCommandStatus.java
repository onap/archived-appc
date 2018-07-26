/*
* ============LICENSE_START=======================================================
* ONAP : APPC
* ================================================================================
* Copyright 2018 TechMahindra
*=================================================================================
* Modifications Copyright 2018 IBM.
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
package org.onap.appc.executor.objects;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class TestLCMCommandStatus {

    private LCMCommandStatus accepted = LCMCommandStatus.ACCEPTED;

    @Test
    public void testName() {
        assertEquals("ACCEPTED", accepted.name());
    }

    @Test
    public void testEquals() {
        assertTrue(accepted.equals(LCMCommandStatus.ACCEPTED));
        assertFalse(accepted.equals(null));
    }

    @Test
    public void testGetResponseMessage() {
        assertEquals("ACCEPTED - request accepted", accepted.getResponseMessage());
    }

    @Test
    public void testGetResponseCode() {
        assertEquals(100, accepted.getResponseCode());
    }

    @Test
    public void testToString_ReturnNonEmptyString() {
        assertNotEquals(accepted.toString(), "");
        assertNotEquals(accepted.toString(), null);

    }

    @Test
    public void testTostring() {
        assertTrue(accepted.toString().contains(accepted.name()));
    }
    
    @Test
    public void testGetFormattedMessageWithCode() {
        Params params= new Params();
        Map<String, java.lang.Object> map = new HashMap<String, java.lang.Object>();
        map.put("testKey1", "testValue1");
        map.put("testKey2", "testValue2");
        params.setParams(map);
        String response=accepted.getFormattedMessageWithCode(params);
        String expected="100-ACCEPTED - request accepted";
        assertEquals(expected, response);
    }
    
}