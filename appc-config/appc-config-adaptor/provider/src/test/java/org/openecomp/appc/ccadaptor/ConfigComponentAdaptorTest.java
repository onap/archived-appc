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

package org.openecomp.appc.ccadaptor;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdnc.sli.SvcLogicContext;
import org.powermock.reflect.Whitebox;

public class ConfigComponentAdaptorTest {
    
    
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
        parameters.put("Config_contents", "test");

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("service-data.vnf-config-parameters-list.vnf-config-parameters[0].update-configuration[0].block-key-name", "test");
        cca.configure(key, parameters, ctx);        
    }
    
    @Test(expected=Exception.class)
    public void testAll1(){
        Properties props = null;
        ConfigComponentAdaptor cca = new ConfigComponentAdaptor(props);
        String key = "get";
        Map<String, String> parameters = new HashMap<String,String>();
        parameters.put("Host_ip_address", "test");
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("service-data.vnf-config-parameters-list.vnf-config-parameters[0].update-configuration[0].block-key-name", "test");
        cca.configure(key, parameters, ctx);        
    }
    
    @Test(expected=Exception.class)
    public void testAll2(){
        Properties props = null;
        ConfigComponentAdaptor cca = new ConfigComponentAdaptor(props);
        String key = "cli";
        Map<String, String> parameters = new HashMap<String,String>();
        parameters.put("Host_ip_address", "test");
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("service-data.vnf-config-parameters-list.vnf-config-parameters[0].update-configuration[0].block-key-name", "test");
        cca.configure(key, parameters, ctx);        
    }
    
    @Test
    public void testGetStringBetweenQuotes() throws Exception{
        Properties props = null;
        ConfigComponentAdaptor cca = new ConfigComponentAdaptor(props);
        String result =Whitebox.invokeMethod(cca, "getStringBetweenQuotes","\"testvalue\"");
        Assert.assertEquals("testvalue", result);
    }
    
    @Test
    public void testBuildXmlRequest() throws Exception{
        Properties props = null;
        ConfigComponentAdaptor cca = new ConfigComponentAdaptor(props);
        Map<String, String> param = new HashMap<String,String>();
        Whitebox.invokeMethod(cca, "buildXmlRequest",param,"template");    
    }
    
    @Test
    public void testTrimResponse() throws Exception{
        Properties props = null;
        ConfigComponentAdaptor cca = new ConfigComponentAdaptor(props);
        String result =Whitebox.invokeMethod(cca, "trimResponse","testData");
        Assert.assertEquals("", result);
    }
    
    @Test
    public void testBuildNetworkData2() throws Exception{
        Properties props = null;
        ConfigComponentAdaptor cca = new ConfigComponentAdaptor(props);
        SvcLogicContext ctx = new SvcLogicContext();
        String result =Whitebox.invokeMethod(cca, "buildNetworkData2",ctx,"template","operation");
        Assert.assertEquals("template", result);
    }
    
    //@Test
    public void testGetLastFewLinesOfFile() throws Exception{
        Properties props = null;
        ConfigComponentAdaptor cca = new ConfigComponentAdaptor(props);
        Whitebox.invokeMethod(cca, "readFile","test");    
    }
    
    @Test
    public void testConnect() throws Exception{
        Properties props = null;
        ConfigComponentAdaptor cca = new ConfigComponentAdaptor(props);
        SvcLogicContext ctx = new SvcLogicContext();
        cca.activate("key", ctx);

    }
    
    @Test(expected=Exception.class)
    public void testActivate() throws Exception{
        Properties props = null;
        ConfigComponentAdaptor cca = new ConfigComponentAdaptor(props);
        SvcLogicContext ctx = new SvcLogicContext();
        String result =Whitebox.invokeMethod(cca, "activate",ctx,true);
        Assert.assertEquals("template", result);
    }
    
    @Test(expected=Exception.class)
    public void testAudit() throws Exception{
        Properties props = null;
        ConfigComponentAdaptor cca = new ConfigComponentAdaptor(props);
        SvcLogicContext ctx = new SvcLogicContext();
        String result =Whitebox.invokeMethod(cca, "audit",ctx,"test");
        Assert.assertEquals("template", result);
    }
    
    @Test(expected=Exception.class)
    public void testPrepare() throws Exception{
        Properties props = null;
        ConfigComponentAdaptor cca = new ConfigComponentAdaptor(props);
        SvcLogicContext ctx = new SvcLogicContext();
        String result =Whitebox.invokeMethod(cca, "prepare",ctx,"test","test");
        Assert.assertEquals("template", result);
    }
}
