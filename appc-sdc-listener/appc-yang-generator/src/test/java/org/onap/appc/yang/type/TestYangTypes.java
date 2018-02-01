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
