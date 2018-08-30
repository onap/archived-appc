package org.onap.appc.adapter.netconf.internal;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class TestNetconfAdapter2 {

    private static final String EOM = "]]>]]>";

    @Test (expected = IOException.class)
    public void testReceiveMessage() throws IOException {
        PipedOutputStream pos = new PipedOutputStream();
        PipedInputStream is = new PipedInputStream(pos);

        PipedInputStream pis = new PipedInputStream();
        PipedOutputStream os = new PipedOutputStream(pis);

        NetconfAdapter2 netconfAdapter = new NetconfAdapter2(is, os);

        String request = "Hello, netconf!";
        pos.write(request.getBytes());
        pos.write(EOM.getBytes());
        String response = netconfAdapter.receiveMessage();
        Assert.assertNotNull(response);
        Assert.assertEquals(request, response.trim());
    }

    @Test (expected = IOException.class)
    public void testSendMessage() throws IOException {
        PipedOutputStream pos = new PipedOutputStream();
        PipedInputStream is = new PipedInputStream(pos);

        PipedInputStream pis = new PipedInputStream();
        PipedOutputStream os = new PipedOutputStream(pis);

        NetconfAdapter2 netconfAdapter = new NetconfAdapter2(is, os);

        String request = "Hello, netconf!";
        netconfAdapter.sendMessage(request);
        byte[] bytes = new byte[request.length()+EOM.length()+2];
        int count = pis.read(bytes);
        String response = new String(bytes, 0, count);
        Assert.assertNotNull(response);
        Assert.assertTrue(response.endsWith(EOM));
        response = response.substring(0, response.length() - EOM.length()).trim();
        Assert.assertEquals(request, response);
    }

    @Test (expected = IOException.class)
    public void testSendReceive() throws IOException {
        PipedOutputStream os = new PipedOutputStream();
        PipedInputStream is = new PipedInputStream(os);

        NetconfAdapter2 netconfAdapter = new NetconfAdapter2(is, os);

        String request = "Hello, netconf!";
        netconfAdapter.sendMessage(request);
        String response = netconfAdapter.receiveMessage();
        Assert.assertNotNull(response);
        Assert.assertEquals(request, response.trim());
    }

    @Test
    public void testDefaultSendReceive() throws IOException {

        NetconfAdapter2 netconfAdapter = new NetconfAdapter2();

        String request = "Hello, netconf!";
        netconfAdapter.sendMessage(request);

        InputStream in = netconfAdapter.getIn();
        OutputStream out = netconfAdapter.getOut();

        Assert.assertNotNull(in);
        Assert.assertNotNull(out);
    }
}