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
package org.onap.appc.yang.type;

import static org.junit.Assert.assertEquals;
import java.util.Map;
import org.junit.Test;

public class TestYangTypes {
    private Map<String, String> testTypeMap = YangTypes.getYangTypeMap();

    @Test
    public void testGetYangTypeMap_Size() {
        assertEquals(48, testTypeMap.size());
    }
    @Test(expected = java.lang.UnsupportedOperationException.class)
    public void testGetYangTypeMap_UnModifiableMap() {
        testTypeMap.remove("timeticks");
        assertEquals(47, testTypeMap.size());
    }
    @Test
    public void testGetYangTypeMap_ValidKey() {
        assertEquals("uint64", testTypeMap.get("uint64"));
    }
    @Test
    public void testGetYangTypeMap_In_ValidKey() {
        assertEquals(null, testTypeMap.get("uint128"));
    }

}
