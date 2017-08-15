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

package org.openecomp.appc.encryptiontool.wrapper;

import java.util.Map;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.openecomp.sdnc.sli.SvcLogicContext;
import org.openecomp.sdnc.sli.SvcLogicException;
import org.openecomp.sdnc.sli.SvcLogicJavaPlugin;

public class EncryptionToolDGWrapper implements SvcLogicJavaPlugin {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(EncryptionToolDGWrapper.class);
    
    
    public void runEncryption(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException 
    {
        String responsePrefix = inParams.get("prefix");
        String userName = inParams.get("userName");
        String password = inParams.get("password");
        String vnf_type = inParams.get("vnf_type");
        
        try{
            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix+".") : ""  ;
            if(StringUtils.isBlank(userName) || StringUtils.isBlank(password) || StringUtils.isBlank(vnf_type)){
                throw new Exception("username or Password is missing");
            }
            
            String [] input = new String[] {vnf_type,userName,password};
            WrapperEncryptionTool.main(input);
            
        }
        catch (Exception e) 
        {            
            throw new SvcLogicException(e.getMessage());
        }
        
    }
    public void getProperty(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException 
    {
        String responsePrefix = inParams.get("prefix");
        String propertyName = inParams.get("propertyName");        
        
    
        try{
            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix+".") : ""  ;
            PropertiesConfiguration conf = new PropertiesConfiguration(Constants.APPC_CONFIG_DIR + "/appc_southbound.properties");    
            conf.setBasePath(null);
            EncryptionTool et = EncryptionTool.getInstance();        
    
            ctx.setAttribute(responsePrefix + "propertyName", et.decrypt(conf.getProperty(propertyName).toString()));            
        }
        catch (Exception e) {
            ctx.setAttribute(responsePrefix + "status", "failure");
            ctx.setAttribute(responsePrefix + "error-message", e.getMessage());
            e.printStackTrace();
            throw new SvcLogicException(e.getMessage());
        }        
    }
}
