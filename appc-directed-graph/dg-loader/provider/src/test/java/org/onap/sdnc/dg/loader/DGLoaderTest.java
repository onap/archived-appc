/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdnc.dg.loader;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.eclipse.osgi.framework.internal.core.BundleContextImpl;
import org.eclipse.osgi.framework.internal.core.BundleHost;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.ccsdk.sli.core.sli.ConfigurationException;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicStore;
import org.onap.ccsdk.sli.core.sli.SvcLogicStoreFactory;
import org.osgi.framework.BundleContext;
import org.powermock.reflect.Whitebox;
import org.powermock.*;

public class DGLoaderTest {
    
    @Test
    public void testXMLGenerator() throws Exception {
        DGXMLGenerator application = new DGXMLGenerator();
        String jsonPath = null;
        String xmlPath = null;
        String propertyPath = "somePath";
        // Generate, GenerateLoad, GenerateLoadActivate
        String []args = new String[]{"src/test/resources/json","src/test/resources/xml"};
        //logger.info("DGXML Conversion Started with arguments :"+ args[0] +":"+ args[1]);
        if(args != null && args.length >= 2){
            jsonPath = args[0];
            xmlPath = args[1];
        }
    
        application.generateXMLFromJSON(jsonPath, xmlPath, propertyPath);
        File dir=new File("src/test/resources/xml");
        String extensions[]=new String[] {"xml","XML"};
        List<File> files = new ArrayList<File>();
        files=(List<File>) FileUtils.listFiles(dir, extensions, true);
        assertNotNull(files.get(0));
        assertNotNull(files.get(0).getName());
    }
    
    @Test
    public void testXMLGeneratorMain() throws Exception {
        String []args = new String[]{"src/test/resources/json","src/test/resources/xml"};
        DGXMLGenerator.main(args);
        File dir=new File("src/test/resources/xml");
        String extensions[]=new String[] {"xml","XML"};
        List<File> files = new ArrayList<File>();
        files=(List<File>) FileUtils.listFiles(dir, extensions, true);
        assertNotNull(files.get(0));
        assertNotNull(files.get(0).getName());
    }
        
    @Test
    public void testDGLoader() throws Exception {
        String propertyPath = "src/test/resources/dummy.properties";
        String xmlPath = "src/test/resources/xml/Appc_UniTest.xml";
        DGXMLLoad dgXMLLoad = new MockDGXMLLoad();
        dgXMLLoad.loadDGXMLFile(xmlPath);
       }
    
    @Test
    public void testDGLoaderWithDir() throws Exception {
        String propertyPath = "src/test/resources/dummy.properties";
        String xmlPath = "src/test/resources/xml";
        DGXMLLoad dgXMLLoad = new MockDGXMLLoad();
        Whitebox.invokeMethod(dgXMLLoad, "loadDGXMLDir",xmlPath);
    }
    
    @Test
    public void testDGLoaderWithDirThrowsException() throws Exception {
        String propertyPath = "src/test/resources/dummy.properties";
        String xmlPath = "src/test/resources/xml/xml";
        DGXMLLoad dgXMLLoad = new MockDGXMLLoad();
        Whitebox.invokeMethod(dgXMLLoad, "loadDGXMLDir",xmlPath);
    }
    
    @Test
    public void testDGActivate() throws Exception {
        String propertyPath = "src/test/resources/dummy.properties";
        String activateFilePath = "src/test/resources/dg_activate_test";
        DGXMLActivate dgXMLActivate = new MockDGXMLActivate();
        dgXMLActivate.activateDg(activateFilePath);

    }
    
    @Test
    public void testDGActivateThrowsException() throws Exception {
        String propertyPath = "src/test/resources/dummy.properties";
        String activateFilePath = "src/test/resources/someFile";
        DGXMLActivate dgXMLActivate = new MockDGXMLActivate();
        dgXMLActivate.activateDg(activateFilePath);

    }
    
    @Test
    public void testDGLoadNActivate() throws Exception {
        String propertyPath = "src/test/resources/dummy.properties";
        String activateFilePath = "src/test/resources/dg_activate_test";
        String xmlPath = "src/test/resources/xml/Appc_UniTest.xml";
        DGXMLLoadNActivate dgXMLLoadNActivate = new MockDGXMLLoadNActivate();
        dgXMLLoadNActivate.loadDGXMLFile(xmlPath);
        dgXMLLoadNActivate.activateDg(activateFilePath);
    }
    
    @Test
    public void testDGLoadNActivateThrowsException() throws Exception {
        String propertyPath = "src/test/resources/dummy.properties";
        String activateFilePath = "src/test/resources/someFile";
        String xmlPath = "src/test/resources/xml/Appc_UniTest.xml";
        DGXMLLoadNActivate dgXMLLoadNActivate = new MockDGXMLLoadNActivate();
        dgXMLLoadNActivate.loadDGXMLFile(xmlPath);
        dgXMLLoadNActivate.activateDg(activateFilePath);
    }
    
    @Test
    public void testDGLoadNActivateloadDGXMLDir() throws Exception {
        String xmlPath = "src/test/resources/xml";
        DGXMLLoadNActivate dgXMLLoadNActivate = new MockDGXMLLoadNActivate();
        Whitebox.invokeMethod(dgXMLLoadNActivate,"loadDGXMLDir",xmlPath);
     }
    
    
    public void testDGLoadNActivateloadDGXMLDirThrowsException() throws Exception {
        String xmlPath = "src/test/resources/someDir";
        DGXMLLoadNActivate dgXMLLoadNActivate = new MockDGXMLLoadNActivate();
        Whitebox.invokeMethod(dgXMLLoadNActivate,"loadDGXMLDir",xmlPath);
     }
    
    @Test
    public void testDGLoaderActivator() throws Exception {
        String xmlPath = "src/test/resources/xml";
        DGLoaderActivator dgLoaderActivator = new DGLoaderActivator();
        dgLoaderActivator.start(null);
        dgLoaderActivator.stop(null);
        assertTrue(true);
    }

    @Test (expected=Exception.class)
    public void testDGActivateConstructorThrowsException() throws Exception {
        String somePath="";
        DGXMLActivate dgXMLActivate = new DGXMLActivate(somePath);
    }
    
    @Test (expected=Exception.class)
    public void testDGXMLLoadConstructorThrowsException() throws Exception {
        String somePath="";
        DGXMLLoad dgXMLLoad = new DGXMLLoad(somePath);
    }
    
    @Test (expected=Exception.class)
    public void testDGLoadNActivateConstructorThrowsException() throws Exception {
        String somePath="";
        DGXMLLoadNActivate dgXMLLoadNActivate = new DGXMLLoadNActivate(somePath);
    }
    
}
