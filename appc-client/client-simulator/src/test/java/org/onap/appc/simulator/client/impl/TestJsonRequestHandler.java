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

package org.onap.appc.simulator.client.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.appc.client.lcm.api.LifeCycleManagerStateful;
import org.onap.appc.client.lcm.exceptions.AppcClientException;
import org.onap.appc.simulator.client.main.ClientRunner;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RunWith(MockitoJUnitRunner.class)

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
