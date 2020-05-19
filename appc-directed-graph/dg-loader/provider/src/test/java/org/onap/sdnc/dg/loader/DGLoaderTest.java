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

package org.onap.sdnc.dg.loader;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

public class DGLoaderTest {

    @Test
    public void testXMLGenerator() throws Exception {
        DGXMLGenerator application = new DGXMLGenerator();
        String jsonPath = null;
        String xmlPath = null;
        String propertyPath = "somePath";
        // Generate, GenerateLoad, GenerateLoadActivate
        String[] args = new String[] {"src/test/resources/json", "src/test/resources/xml"};
        // logger.info("DGXML Conversion Started with arguments :"+ args[0] +":"+ args[1]);
        if (args.length >= 2) {
            jsonPath = args[0];
            xmlPath = args[1];
        }

        application.generateXMLFromJSON(jsonPath, xmlPath, propertyPath);
        File dir = new File("src/test/resources/xml");
        String extensions[] = new String[] {"xml", "XML"};
        List<File> files = (List<File>) FileUtils.listFiles(dir, extensions, true);
        assertNotNull(files.get(0));
        assertNotNull(files.get(0).getName());
    }

    @Test
    public void testXMLGeneratorMain() throws Exception {
        String[] args = new String[] {"src/test/resources/json", "src/test/resources/xml"};
        DGXMLGenerator.main(args);
        File dir = new File("src/test/resources/xml");
        String extensions[] = new String[] {"xml", "XML"};
        List<File> files = (List<File>) FileUtils.listFiles(dir, extensions, true);
        assertNotNull(files.get(0));
        assertNotNull(files.get(0).getName());
    }

    @Test
    public void testDGLoader() throws Exception {
        String propertyPath = "src/test/resources/dummy.properties";
        String xmlPath = "src/test/resources/xml/Appc_UniTest.xml";
        DGXMLLoader dgXMLLoad = new MockDGXMLLoader();
        dgXMLLoad.loadDGXMLFile(xmlPath);
        assertNotNull(propertyPath);
    }

    @Test
    public void testDGLoaderWithDir() throws Exception {
        String propertyPath = "src/test/resources/dummy.properties";
        String xmlPath = "src/test/resources/xml";
        DGXMLLoader dgXMLLoad = new MockDGXMLLoader();
        Whitebox.invokeMethod(dgXMLLoad, "loadDGXMLDir", xmlPath);
        assertNotNull(propertyPath);
    }

    @Test
    public void testDGLoaderWithDirThrowsException() throws Exception {
        String propertyPath = "src/test/resources/dummy.properties";
        String xmlPath = "src/test/resources/xml/xml";
        DGXMLLoader dgXMLLoader = new MockDGXMLLoader();
        Whitebox.invokeMethod(dgXMLLoader, "loadDGXMLDir", xmlPath);
        assertNotNull(xmlPath);
    }

    @Test
    public void testDGActivate() throws Exception {
        String propertyPath = "src/test/resources/dummy.properties";
        String activateFilePath = "src/test/resources/dg_activate_test";
        DGXMLActivator dgXMLActivator = new MockDGXMLActivator();
        dgXMLActivator.activateDg(activateFilePath);
        assertNotNull(dgXMLActivator);

    }

    @Test
    public void testDGActivateThrowsException() throws Exception {
        String propertyPath = "src/test/resources/dummy.properties";
        String activateFilePath = "src/test/resources/someFile";
        DGXMLActivator dgXMLActivator = new MockDGXMLActivator();
        dgXMLActivator.activateDg(activateFilePath);
        assertNotNull(activateFilePath);

    }

    @Test
    public void testDGLoaderActivator() throws Exception {
        String xmlPath = "src/test/resources/xml";
        DGLoaderActivator dgLoaderActivator = new DGLoaderActivator();
        dgLoaderActivator.start(null);
        dgLoaderActivator.stop(null);
        assertTrue(true);
    }

    @Test(expected = Exception.class)
    public void testDGActivateConstructorThrowsException() throws Exception {
        String somePath = "";
        DGXMLActivator dgXMLActivator = new DGXMLActivator(somePath);
    }

    @Test(expected = Exception.class)
    public void testDGXMLLoadConstructorThrowsException() throws Exception {
        String somePath = "";
        DGXMLLoader dgXMLLoader = new DGXMLLoader(somePath);
    }
}
