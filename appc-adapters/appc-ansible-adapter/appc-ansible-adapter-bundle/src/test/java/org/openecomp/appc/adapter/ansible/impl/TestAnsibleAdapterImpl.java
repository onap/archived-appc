/*-
 * ============LICENSE_START=======================================================
 * APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Amdocs
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */

package org.openecomp.appc.adapter.ansible.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openecomp.appc.Constants;
import org.openecomp.appc.configuration.ConfigurationFactory;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.sdnc.sli.SvcLogicContext;
import org.openecomp.sdnc.sli.SvcLogicException;
import org.slf4j.MDC;

import org.openecomp.appc.adapter.ansible.AnsibleAdapter;
import org.openecomp.appc.adapter.ansible.impl.AnsibleAdapterImpl;

public class TestAnsibleAdapterImpl {


    private AnsibleAdapterImpl adapter;
    private String TestId;
    private boolean testMode = true;
    
    @SuppressWarnings("nls")
    @BeforeClass
    public static void once() throws NoSuchFieldException, SecurityException, NoSuchMethodException {

    }

    @Before
    public void setup() throws IllegalArgumentException, IllegalAccessException {
	testMode = true;
	adapter = new AnsibleAdapterImpl(testMode);
    }
    
    @Test
    public void testA() throws IOException, IllegalStateException, IllegalArgumentException,
       APPCException {

            Map<String, String> params = new HashMap<>();
	    params.put("AgentUrl", "https://192.168.1.1");
            params.put("User", "test");
	    params.put("Password", "test");
	    params.put("PlaybookName", "test_playbook.yaml");
	    
            SvcLogicContext svcContext = new SvcLogicContext();          
            try{
		String Pending = "100";
		adapter.reqExec(params, svcContext);
		String status=svcContext.getAttribute("org.openecomp.appc.adapter.ansible.result.code");
		TestId=svcContext.getAttribute("org.openecomp.appc.adapter.ansible.result.Id");
		System.out.println("Comparing " + Pending + " and " + status);
		assertEquals(Pending,status);
	    }
	    catch(SvcLogicException e){
	        String message  =svcContext.getAttribute("org.openecomp.appc.adapter.ansible.result.message");	
		String status=svcContext.getAttribute("org.openecomp.appc.adapter.ansible.result.code");
		fail(e.getMessage() + " Code = " + status);
	    }
	    catch(Exception e){
		fail(e.getMessage() + " Unknown exception encountered " );
            }
		
    }

    @Test
    public void testB() throws IOException, IllegalStateException, IllegalArgumentException,
         APPCException {

            Map<String, String> params = new HashMap<>();

	    params.put("AgentUrl", "https://192.168.1.1");
	    params.put("User", "test");
	    params.put("Password", "test");
	    params.put("Id", "100");

            for (String ukey: params.keySet()){
                  System.out.println(String.format("Ansible Parameter %s = %s", ukey, params.get(ukey)));
	    }

            SvcLogicContext svcContext = new SvcLogicContext();          

            try{
		adapter.reqExecResult(params, svcContext);
		String status=svcContext.getAttribute("org.openecomp.appc.adapter.ansible.result.code");
		assertEquals("400",status);
	    }
	    catch(SvcLogicException e){
	        String message  = svcContext.getAttribute("org.openecomp.appc.adapter.ansible.result.message");	
		String status=svcContext.getAttribute("org.openecomp.appc.adapter.ansible.result.code");
		fail(e.getMessage()  + " Code = " + status);
	    }
	    catch(Exception e){
		fail(e.getMessage() + " Unknown exception encountered " );
            }
	    
    }

}
