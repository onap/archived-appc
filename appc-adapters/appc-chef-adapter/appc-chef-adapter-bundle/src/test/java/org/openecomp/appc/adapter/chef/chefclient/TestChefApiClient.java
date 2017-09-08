package org.openecomp.appc.adapter.chef.chefclient;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.openecomp.appc.adapter.chef.chefapi.*;

import static org.junit.Assert.*;


public class TestChefApiClient {

    private ChefApiClient client;
    private Properties props;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

    @Before
    public void setup() throws IllegalArgumentException, IllegalAccessException {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
        props = new Properties();
        InputStream propStr = getClass().getResourceAsStream("/test.properties");
        if (propStr == null) {
            fail("src/test/resources/test.properties missing");
        }

        try {
            props.load(propStr);
            propStr.close();
        } catch (Exception e) {
            e.printStackTrace();
            fail("Could not initialize properties");
        }
        client = new ChefApiClient(
                props.getProperty("org.openecomp.appc.adapter.chef.chefclient.userId"),
                System.getProperty("user.dir") +
                        props.getProperty("org.openecomp.appc.adapter.chef.chefclient.pemPath"),
                props.getProperty("org.openecomp.appc.adapter.chef.chefclient.endPoint"),
                props.getProperty("org.openecomp.appc.adapter.chef.chefclient.organizations"));
    }

    @Test
    public void testGet(){
        Get get = client.get(props.getProperty("org.openecomp.appc.adapter.chef.chefclient.path"));
        ApiMethod method = get.createRequest();
        String[] response = method.test.split("\n");

        thenStringShouldMatch("GET", response);
    }

    @Test
    public void testPut(){
        Put put = client.put(props.getProperty("org.openecomp.appc.adapter.chef.chefclient.path"));
        ApiMethod method = put.createRequest();
        String[] response = method.test.split("\n");

        thenStringShouldMatch("PUT", response);
    }

    @Test
    public void testPost() {
        Post post = client.post(props.getProperty("org.openecomp.appc.adapter.chef.chefclient.path"));
        ApiMethod method = post.createRequest();
        String[] response = method.test.split("\n");

        thenStringShouldMatch("POST", response);
    }

    @Test
    public void testDelete(){
        Delete delete = client.delete(props.getProperty("org.openecomp.appc.adapter.chef.chefclient.path"));
        ApiMethod method = delete.createRequest();
        String[] response = method.test.split("\n");

        thenStringShouldMatch("DELETE", response);
    }

    private String timestamp(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String timeStamp = sdf.format(new Date());
        timeStamp = timeStamp.replace(" ", "T");
        timeStamp = timeStamp + "Z";
        return timeStamp;
    }

    private void thenStringShouldMatch(String method, String[] response){
        assertEquals("sb Method:" + method, response[0]);
        assertEquals("Hashed Path:+JEk1y2gXwqZRweNjXYtx4ojxW8=", response[1]);
        assertEquals("X-Ops-Content-Hash:2jmj7l5rSw0yVb/vlWAYkK/YBwk=", response[2]);
        assertEquals("X-Ops-Timestamp:" + timestamp(), response[3]);
        String timestamp = timestamp().substring(0, timestamp().length() - 3);
        System.out.print(timestamp);
        String regEx = "X-Ops-Timestamp:" +
                 timestamp +
                "...";
        System.out.print(regEx);
        System.out.print(response[3]);
        assertTrue(outContent.toString(), Pattern.matches(regEx, response[3]));
        assertEquals("X-Ops-UserId:test", response[4]);
    }
}
