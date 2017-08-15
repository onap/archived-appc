/*-
 * ============LICENSE_START=======================================================
 * ONAP : APP-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property.  All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

package org.openecomp.appc.encryptiontool;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;
import org.openecomp.appc.encryptiontool.wrapper.EncryptionToolDGWrapper;
import org.openecomp.appc.encryptiontool.wrapper.WrapperEncryptionTool;
import org.openecomp.sdnc.sli.SvcLogicContext;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class TestEncryptionTool {

    //@Test
    public void testEncryptionTool() throws Exception{

        String [] input = new String[] {"testVnf_Type","testUser","testPassword11", "testAction1", "8080", "http://localhost:8080/restconf/healthcheck"};
        WrapperEncryptionTool.main(input);

    }
    //@Test
    public void testgetPropertyDG() throws Exception{
        EncryptionToolDGWrapper et = new EncryptionToolDGWrapper();        
        SvcLogicContext ctx = new SvcLogicContext();        
        Map<String, String> inParams = new HashMap<String, String>();
        
        inParams.put("prefix", "test");
        inParams.put("propertyName", "testVnf_Type.testAction1.url");
        
        et.getProperty(inParams, ctx);
        
        System.out.println("propertyValue :" + ctx.getAttribute("test.propertyName"));
        
        System.out.println("All  propertyValue :" + ctx.getAttributeKeySet());

    }
    
}
