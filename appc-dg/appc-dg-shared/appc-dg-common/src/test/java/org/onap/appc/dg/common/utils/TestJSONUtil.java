/*
* ============LICENSE_START=======================================================
* ONAP : APPC
* ================================================================================
* Copyright 2018 AT&T
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
package org.onap.appc.dg.common.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import java.util.HashMap;
import java.util.Map;
import java.io.FileReader;
import java.io.FileNotFoundException;

import java.io.UncheckedIOException;


public class TestJSONUtil {

    @Before
    public void setUp() {
    }


    @Test
    public void testFromJsonReader() {
        
        try {
            TestJSONUtilVnf jOut = JSONUtil.fromJson(new FileReader("src/test/resources/data/input.json"), TestJSONUtilVnf.class);
            assertEquals("I1", jOut.getVnfId());
            assertEquals("T1", jOut.getVnfType());

        } catch (UncheckedIOException uioe)
        {
        }
        catch (FileNotFoundException fnfe)
        {
        }
        
    }
    

    @Test
    public void testFromJsonException() {
        TestJSONUtilVnf jOut = null;
        try {
            jOut = JSONUtil.fromJson("{\"vnfId\":\"I2\",\"vnfType\"\"T2\"}", TestJSONUtilVnf.class);

        } catch (UncheckedIOException uioe)
        {
            assertEquals(jOut, null);
        }
        
    }
    
    
    @Test
    public void testFromToJsonStr() {
        TestJSONUtilVnf jRef = new TestJSONUtilVnf("I1", "T1");
        
        try {
            assertEquals(JSONUtil.toJson(jRef), "{\"vnfId\":\"I1\",\"vnfType\":\"T1\"}");
            jRef.setVnfId("I2");
            jRef.setVnfType("T2");
            assertEquals(JSONUtil.toJson(jRef), "{\"vnfId\":\"I2\",\"vnfType\":\"T2\"}");
            String refJson = JSONUtil.toJson(jRef);
            
            TestJSONUtilVnf jOut = JSONUtil.fromJson(refJson, TestJSONUtilVnf.class);
            assertEquals(jRef.getVnfId(), jOut.getVnfId());
            assertEquals(jRef.getVnfType(), jOut.getVnfType());

        } catch (UncheckedIOException uioe)
        {
        }
        
    }

    @Test
    public void testExttractValues() {
        TestJSONUtilVnf jRef = new TestJSONUtilVnf("I2", "T2");
        
        try {

            String refJson = JSONUtil.toJson(jRef);
            
        
            Map<String, String> map = JSONUtil.extractPlainValues(refJson, "vnfId", "vnfType");
            
            HashMap<String, String> hashMap = 
                 (map instanceof HashMap) 
                  ? (HashMap) map 
                  : new HashMap<String, String>(map);
            assertEquals(hashMap.get("vnfId"), "I2");
            assertEquals(hashMap.get("vnfType"), "T2");
            
            
        } catch (UncheckedIOException uioe)
        {        }
        
    }


}
