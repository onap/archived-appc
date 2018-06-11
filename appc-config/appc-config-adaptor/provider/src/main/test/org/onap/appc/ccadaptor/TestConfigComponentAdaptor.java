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
package org.onap.appc.ccadaptor;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

public class TestConfigComponentAdaptor {
    
    
    @Test
    public void testGetCliRunningConfig(){
        Properties props = null;
        ConfigComponentAdaptor cca = new ConfigComponentAdaptor(props);
        String Get_config_template = ("get_config_template");
        String key = "GetCliRunningConfig";
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("Host_ip_address", "test");
        parameters.put("User_name", "test");
        parameters.put("Password", "password");
        parameters.put("Port_number", "22");
        parameters.put("Get_config_template", Get_config_template);

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("service-data.vnf-config-parameters-list.vnf-config-parameters[0].update-configuration[0].block-key-name", "test");
        cca.configure(key, parameters, ctx);
        
    }
    
    @Test
    public void testDownloadCliConfig(){
        Properties props = null;
        ConfigComponentAdaptor cca = new ConfigComponentAdaptor(props);
        String Get_config_template = ("get_config_template");
        String key = "DownloadCliConfig";
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("Host_ip_address", "test");
        parameters.put("User_name", "test");
        parameters.put("Password", "password");
        parameters.put("Port_number", "22");
        parameters.put("Get_config_template", Get_config_template);

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("service-data.vnf-config-parameters-list.vnf-config-parameters[0].update-configuration[0].block-key-name", "test");
        cca.configure(key, parameters, ctx);
        
    }
    
    @Test
    public void testXmlDownload(){
        Properties props = null;
        ConfigComponentAdaptor cca = new ConfigComponentAdaptor(props);
        String Get_config_template = ("get_config_template");
        String key = "xml-download";
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("Host_ip_address", "test");
        parameters.put("User_name", "test");
        parameters.put("Password", "password");
        parameters.put("Port_number", "22");
        parameters.put("Get_config_template", Get_config_template);

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("service-data.vnf-config-parameters-list.vnf-config-parameters[0].update-configuration[0].block-key-name", "test");
        cca.configure(key, parameters, ctx);
    }
    
    @Test
    public void testXmlGetrunningconfig(){
        Properties props = null;
        ConfigComponentAdaptor cca = new ConfigComponentAdaptor(props);
        String Get_config_template = ("get_config_template");
        String key = "xml-getrunningconfig";
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("Host_ip_address", "test");
        parameters.put("User_name", "test");
        parameters.put("Password", "password");
        parameters.put("Port_number", "22");
        parameters.put("Get_config_template", Get_config_template);

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("service-data.vnf-config-parameters-list.vnf-config-parameters[0].update-configuration[0].block-key-name", "test");
        cca.configure(key, parameters, ctx);
        
    }
    
    @Test
    public void testEscapeSql(){
        Properties props = null;
        ConfigComponentAdaptor cca = new ConfigComponentAdaptor(props);
        String Get_config_template = ("get_config_template");
        String key = "escapeSql";
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("Host_ip_address", "test");
        parameters.put("User_name", "test");
        parameters.put("Password", "password");
        parameters.put("Port_number", "22");
        parameters.put("Get_config_template", Get_config_template);

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("service-data.vnf-config-parameters-list.vnf-config-parameters[0].update-configuration[0].block-key-name", "test");
        cca.configure(key, parameters, ctx);
    }
    
    @Test
    public void testAll(){
        Properties props = null;
        ConfigComponentAdaptor cca = new ConfigComponentAdaptor(props);
        String Get_config_template = ("test");
        String Download_config_template = ("test");
        String key = "GetCliRunningConfig";
        Map<String, String> parameters = new HashMap<String,String>();
        parameters.put("Host_ip_address", "test");
        parameters.put("User_name", "test");
        parameters.put("Password", "password");
        parameters.put("Port_number", "22");
        parameters.put("Protocol", "netconf");
        parameters.put("Contents", "Contents");
        parameters.put("Get_config_template", Get_config_template);
        parameters.put("Download_config_template", Download_config_template);
        parameters.put("Config_contents", "config\nsystem\nservice-interface serv1\nipv4\ngateway-ip-address 192.168.30.44");

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("service-data.vnf-config-parameters-list.vnf-config-parameters[0].update-configuration[0].block-key-name", "test");
        cca.configure(key, parameters, ctx);
        
        
    }
    

}