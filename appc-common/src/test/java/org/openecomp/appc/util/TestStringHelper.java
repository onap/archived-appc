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



package org.openecomp.appc.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.junit.Test;
import org.openecomp.appc.util.StringHelper;


public class TestStringHelper {

    @Test
    public void testAsListWithNullList() {
        String value = StringHelper.asList((String[]) null);
        assertNotNull(value);
        assertEquals("[]", value);
    }

    @Test
    public void testAsListWithEmptyList() {
        String value = StringHelper.asList(new String[] {});
        assertNotNull(value);
        assertEquals("[]", value);
    }

    @Test
    public void testAsListWithSingleValue() {
        String value = StringHelper.asList("one");
        assertNotNull(value);
        assertEquals("[one]", value);
    }

    @Test
    public void testAsListWithTwoValues() {
        String value = StringHelper.asList("one", "two");
        assertNotNull(value);
        assertEquals("[one,two]", value);
    }

    @Test
    public void testAsListWithFiveValues() {
        String value = StringHelper.asList("one", "two", "three", "four", "five");
        assertNotNull(value);
        assertEquals("[one,two,three,four,five]", value);
    }

    @Test
    public void testPropertiesToString() {
        String key1 = "key1";
        String val1 = "val1";
        String key2 = "key2";
        String val2 = "val2";

        assertEquals(null, StringHelper.propertiesToString(null));

        Properties props = new Properties();

        String result = StringHelper.propertiesToString(props);
        assertNotNull(result);
        assertEquals("[ ]", result);

        props.setProperty(key1, val1);
        result = StringHelper.propertiesToString(props);
        assertNotNull(result);
        assertTrue(result.contains(key1));
        assertTrue(result.contains(val1));
        assertTrue(result.lastIndexOf(",") < result.length() - 3); // No trailing comma

        props.setProperty(key2, val2);
        result = StringHelper.propertiesToString(props);
        assertNotNull(result);
        assertTrue(result.contains(key1));
        assertTrue(result.contains(val1));
        assertTrue(result.contains(key2));
        assertTrue(result.contains(val2));
        assertTrue(result.lastIndexOf(",") < result.length() - 3); // No trailing comma
    }
}
