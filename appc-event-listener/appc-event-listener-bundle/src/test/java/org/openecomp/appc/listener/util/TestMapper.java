/*-
 * ============LICENSE_START=======================================================
 * APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Amdocs
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
 * ============LICENSE_END=========================================================
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */

package org.openecomp.appc.listener.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.openecomp.appc.listener.util.Mapper;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class TestMapper {

    private String dummyJson = "{\"a\":\"%s\"}";
    private DummyObj dummyObj = new DummyObj();

    @JsonSerialize
    public static class DummyObj implements Serializable {
        @JsonProperty("a")
        public String a;

        public DummyObj() {
        }
    }

    @Before
    public void setup() {
    }

    @Test
    public void testGetMapper() {
        assertNotNull(Mapper.getMapper());
    }

    @Test
    public void testToJsonObject() {
        JSONObject out;
        out = Mapper.toJsonObject(".");
        assertNull(out);

        String value = "b";
        out = Mapper.toJsonObject(String.format(dummyJson, value));
        assertNotNull(out);
        assertEquals(value, out.get("a"));
    }

    @Test
    public void testConstructor() {
        // Only here for code coverage
        Mapper m = new Mapper();
        assertNotNull(m);
    }

    @Test
    public void testMap() {
        List<String> in = new ArrayList<String>();
        in.add("");
        in.add(null);

        List<DummyObj> out = Mapper.mapList(in, DummyObj.class);
        assertNotNull(out);
        assertTrue(out.isEmpty());

        in.add(String.format(dummyJson, "1"));
        in.add("{\"invalid\":\"yes\"}");
        in.add(String.format(dummyJson, "2"));

        out = Mapper.mapList(in, DummyObj.class);
        assertNotNull(out);
        assertEquals(2, out.size());
        assertEquals("1", out.get(0).a);
        assertEquals("2", out.get(1).a);
    }

}
