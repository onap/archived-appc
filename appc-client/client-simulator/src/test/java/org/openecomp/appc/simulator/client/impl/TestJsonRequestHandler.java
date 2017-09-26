package org.openecomp.appc.simulator.client.impl;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.openecomp.appc.client.lcm.api.LifeCycleManagerStateful;
import org.openecomp.appc.client.lcm.exceptions.AppcClientException;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LifeCycleManagerStateful.class})

public class TestJsonRequestHandler {

    JsonResponseHandler jsonResponseHandler=new JsonResponseHandler();
    @Before
    public void init(){
        jsonResponseHandler= Mockito.mock(JsonResponseHandler.class);
    }


    @Test
    public void testProceedFiles() throws AppcClientException,java.io.IOException{
    String folder="src/test/resources/data";
    List<File> sources = getJsonFiles(folder);
    File source=sources.get(0);
    File log = new File(folder + "/output.txt");
    JsonRequestHandler requestHandler = new JsonRequestHandler();
    Mockito.doNothing().when(jsonResponseHandler).onResponse(Matchers.anyBoolean());
    requestHandler.proceedFile(source,log);

    Assert.assertNotNull(log);

    }

    private static List<File> getJsonFiles(String folder) throws FileNotFoundException {
        Path dir = Paths.get(folder);
        FileFilter fileFilter = new WildcardFileFilter("*.json");
        return new ArrayList<File>(Arrays.asList(dir.toFile().listFiles(fileFilter)));
    }


}