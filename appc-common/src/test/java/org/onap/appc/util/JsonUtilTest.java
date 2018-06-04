/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
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

package org.onap.appc.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import java.io.FileNotFoundException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;


public class JsonUtilTest {

    @Test(expected = NullPointerException.class)
    public void should_throw_exception_when_json_string_is_null() throws IOException {

        JsonUtil.convertJsonStringToFlatMap(null);
    }

    @Test(expected = JsonParseException.class)
    public void should_throw_exception_when_invalid_json_string() throws IOException {

        JsonUtil.convertJsonStringToFlatMap("{key: value}");
    }

    @Test
    public void should_convert_json_string_to_flat_map() throws IOException {

        String jsonString = "{\"A\":\"A-value\",\"B\":{\"C\":\"B.C-value\",\"D\":\"B.D-value\"}}";
        Map<String, String> flatMap = JsonUtil.convertJsonStringToFlatMap(jsonString);

        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("A", "A-value");
        expectedMap.put("B.C", "B.C-value");
        expectedMap.put("B.D", "B.D-value");

        assertEquals(expectedMap, flatMap);
        assertNotNull(flatMap);
    }

    @Test
    public void should_convert_json_string_to_flat_map_with_nested_json() throws IOException {

        String jsonString = "{\"A\":\"A-value\",\"B\":\"{\\\"C\\\":\\\"C-value\\\",\\\"D\\\":\\\"D-value\\\"}\"}";
        Map<String, String> flatMap = JsonUtil.convertJsonStringToFlatMap(jsonString);

        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("A", "A-value");
        expectedMap.put("B", "{\"C\":\"C-value\",\"D\":\"D-value\"}");

        assertEquals(expectedMap, flatMap);
        assertNotNull(flatMap);
    }

    @Test(expected = FileNotFoundException.class)
    public void should_throw_exception_when_not_found_json_file() throws IOException {
        JsonUtil.readInputJson("not-existing.json", DummyClass.class);
    }


    @Test(expected = JsonParseException.class)
    public void should_throw_exception_when_invalid_json_file() throws IOException {
        JsonUtil.readInputJson("/invalid.json", DummyClass.class);
    }

    @Test
    public void should_parse_valid_json_file () throws IOException {
        DummyClass dummyClass = JsonUtil.readInputJson("/valid.json", DummyClass.class);

        assertEquals("dummy name", dummyClass.getName());
        assertEquals(99, dummyClass.getValue());
    }

    private static class DummyClass {

        private String name;
        private int value;

        public DummyClass(@JsonProperty("name") String name, @JsonProperty("value") int someValue) {
            this.name = name;
            this.value = someValue;
        }

        public String getName() {
            return name;
        }

        public int getValue() {
            return value;
        }
    }
}


