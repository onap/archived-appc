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

package org.onap.appc.simulator.client.main;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.stubbing.answers.DoesNothing;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.appc.client.lcm.exceptions.AppcClientException;
import org.onap.appc.simulator.client.RequestHandler;
import org.onap.appc.simulator.client.impl.JsonRequestHandler;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@RunWith(MockitoJUnitRunner.class)

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

/* JsinRequestHandler is a constructor
 * will figure out how to do test without PowerMock later
    @Test
    public void testMain() throws java.io.IOException,java.lang.Exception{
        String  []arguments=new String[]{"src/test/resources/data","JSON"};
        
        PowerMockito.whenNew(JsonRequestHandler.class).withArguments(Mockito.anyObject()).thenReturn(jsonRequestHandler);
        Mockito.doNothing().when(jsonRequestHandler).proceedFile(Matchers.anyObject(), Matchers.anyObject());

        ClientRunner.main(arguments);
        String expectedOutput=outContent.toString();
        Assert.assertEquals(expectedOutput,outContent.toString());
    }
*/
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
