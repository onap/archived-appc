package org.openecomp.appc.simulator.client.main;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.openecomp.appc.client.lcm.exceptions.AppcClientException;
import org.openecomp.appc.simulator.client.impl.JsonRequestHandler;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@RunWith(PowerMockRunner.class)
@PrepareForTest({JsonRequestHandler.class,ClientRunner.class})

public class TestClientRunner {

    JsonRequestHandler jsonRequestHandler;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @Before
    public void init() throws AppcClientException{
        System.setOut(new PrintStream(outContent));
        jsonRequestHandler= Mockito.mock(JsonRequestHandler.class);

    }

    @After
    public void cleanUpStreams() {
        System.setOut(null);
    }

    @Test
    public void testMain() throws java.io.IOException,java.lang.Exception{
        String  []arguments=new String[]{"src/test/resources/data","JSON"};
        PowerMockito.whenNew(JsonRequestHandler.class).withArguments(Mockito.anyObject()).thenReturn(jsonRequestHandler);
        Mockito.doNothing().when(jsonRequestHandler).proceedFile(Matchers.anyObject(), Matchers.anyObject());

        ClientRunner.main(arguments);
        String expectedOutput=outContent.toString();
        Assert.assertEquals(expectedOutput,outContent.toString());
    }

    @Test
    public void testGetPrperties(){
        String folder="src/test/resources/data";
        Properties properties=new Properties();
        properties=getProperties(folder);
        Assert.assertNotNull(properties);
    }

    @Test
    public void testGetJsonFIles() throws FileNotFoundException{
        String folder="src/test/resources/data";
        List<File> sources = getJsonFiles(folder);
        Assert.assertNotNull(sources);
    }

    private static Properties getProperties(String folder) {
        Properties prop = new Properties();

        InputStream conf = null;
        try {
            conf = new FileInputStream(folder + "client-simulator.properties");
        } catch (FileNotFoundException e) {

        }
        if (conf != null) {
            try {
                prop.load(conf);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                prop.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("client-simulator.properties"));
            } catch (Exception e) {
                throw new RuntimeException("### ERROR ### - Could not load properties to test");
            }
        }
        return prop;
    }

    private static List<File> getJsonFiles(String folder) throws FileNotFoundException {
        Path dir = Paths.get(folder);
        FileFilter fileFilter = new WildcardFileFilter("*.json");
        return new ArrayList<File>(Arrays.asList(dir.toFile().listFiles(fileFilter)));
    }

}
