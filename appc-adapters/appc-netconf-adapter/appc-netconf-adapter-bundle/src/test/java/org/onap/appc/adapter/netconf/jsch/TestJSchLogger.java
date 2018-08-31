package org.onap.appc.adapter.netconf.jsch;

import org.junit.Assert;
import org.junit.Test;
import org.onap.appc.adapter.netconf.internal.NetconfAdapter2;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class TestJSchLogger {


    @Test
    public void testIsEnabled() throws IOException {
        JSchLogger jSchLogger = new JSchLogger();

        boolean response = jSchLogger.isEnabled(2);

        Assert.assertEquals(true, response);
    }

    @Test
    public void testLog() throws IOException {
        JSchLogger jSchLogger = new JSchLogger();

        jSchLogger.log(0, "test-debug");
        jSchLogger.log(1, "test-info");
        jSchLogger.log(2, "test-warn");
        jSchLogger.log(3, "test-error");
        jSchLogger.log(4, "test-fatal");
        jSchLogger.log(5, "test-other");

    }
}
