package org.onap.appc.util;

import static org.junit.Assert.assertEquals;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.junit.Test;

public class StreamHelperTest {

    @Test
    public void should_return_empty_string_when_given_null_input_stream() {
        assertEquals("", StreamHelper.getStringFromInputStream(null));
    }

    @Test
    public void should_return_empty_string_when_given_empty_byte_array() {

        byte[] testByteArray = new byte[0];

        assertEquals("",
            StreamHelper.getStringFromInputStream(new ByteArrayInputStream(testByteArray)));
    }

    @Test
    public void should_return_string_when_given_byte_array() {
        String testString = "test string";
        byte[] testByteArray = testString.getBytes();

        assertEquals("test string",
            StreamHelper.getStringFromInputStream(new ByteArrayInputStream(testByteArray)));
    }

}
