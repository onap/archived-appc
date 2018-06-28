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
package org.onap.appc.util;

import static org.junit.Assert.*;

import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class PathContextTest {

    private PathContext pathContext;

    @Before
    public void setup() {
        pathContext = new PathContext();
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_exception_when_pushed_null_token() {
        pathContext.pushToken(null);
    }

    @Test(expected = IllegalStateException.class)
    public void should_throw_exception_when_popped_token_from_empty_path() {
        pathContext.popToken();
    }

    @Test
    public void should_delimit_tokens_with_dot() {
        pathContext.pushToken("test");
        pathContext.pushToken("token");

        assertEquals("test.token", pathContext.getPath());
    }

    @Test
    public void should_pop_tokens() {
        pathContext.pushToken("test");
        pathContext.pushToken("token");
        pathContext.pushToken("token2");

        pathContext.popToken();

        assertEquals("test.token", pathContext.getPath());
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_exception_when_pushed_null_modifier() {
        pathContext.pushModifier(null);
    }

    @Test(expected = IllegalStateException.class)
    public void should_throw_exception_when_popped_modifier_from_empty_path() {
        pathContext.popModifier();
    }

    @Test
    public void should_not_delimit_modifiers() {
        pathContext.pushModifier("test");
        pathContext.pushModifier("modifier");

        assertEquals("testmodifier", pathContext.getPath());
    }

    @Test
    public void should_pop_modifiers() {
        pathContext.pushModifier("test");
        pathContext.pushModifier("modifier");
        pathContext.pushModifier("modifier2");

        pathContext.popModifier();

        assertEquals("testmodifier", pathContext.getPath());
    }

    @Test
    public void should_pop_modifiers_and_tokens() {
        pathContext.pushModifier("test");
        pathContext.pushModifier("modifier");
        pathContext.pushToken("token");

        //TODO popToken() and popModifier() actually work the same.
        //TODO Is there sense to keep same method under different names then?

        pathContext.popToken();
        assertEquals("testmodifier", pathContext.getPath());

        pathContext.popModifier();
        assertEquals("test", pathContext.getPath());
    }

    @Test
    public void should_add_entries(){
        pathContext.entry("name", "value");

        Map<String, String> entries = pathContext.entries();
        assertEquals("value", entries.get("name"));
    }

}
