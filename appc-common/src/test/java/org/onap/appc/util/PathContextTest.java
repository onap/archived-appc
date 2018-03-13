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
