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

package org.onap.appc.encryptiontool.wrapper;

import java.util.Map;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class EncryptionToolDGWrapper implements SvcLogicJavaPlugin {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(EncryptionToolDGWrapper.class);

    public void runEncryption(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {
        String userName = inParams.get("userName");
        String password = inParams.get("password");
        String vnfType = inParams.get("vnf_type");

        try {
            if (StringUtils.isBlank(userName) || StringUtils.isBlank(password) || StringUtils.isBlank(vnfType)) {
                throw new SvcLogicException("username or Password is missing");
            }

            String[] input = new String[] { vnfType, userName, password };
            WrapperEncryptionTool.main(input);

        } catch (Exception e) {
            throw new SvcLogicException(e.getMessage());
        }

    }

    public void getProperty(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {
        String responsePrefix = inParams.get("prefix");
        String vnf_Type = ctx.getAttribute("vnf-type");
        String action = ctx.getAttribute("input.action");
        String protocol = ctx.getAttribute("APPC.protocol.PROTOCOL");
        try {
            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix + ".") : "";
            PropertiesConfiguration conf = new PropertiesConfiguration(
                    Constants.APPC_CONFIG_DIR + "/appc_southbound.properties");
            conf.setBasePath(null);
            EncryptionTool et = EncryptionTool.getInstance();
                        log.info("responsePrefix:"+responsePrefix);
                        log.debug("key:"+vnf_Type+"."+protocol+"."+action);
                if(StringUtils.isNotBlank(vnf_Type) && StringUtils.isNotBlank(protocol) && StringUtils.isNotBlank(action))
             {
            String user = (String)conf.getProperty(vnf_Type + "." + protocol + "." + action + "." + "user");
            String password = (String)conf.getProperty(vnf_Type + "." + protocol + "." + action + "." + "password");
            String port = (String)conf.getProperty(vnf_Type + "." + protocol + "." + action + "." + "port");
            String url = (String)conf.getProperty(vnf_Type + "." + protocol + "." + action + "." + "url");
                if (StringUtils.isBlank(user) || StringUtils.isBlank(password)) {
            throw new SvcLogicException("Error-while fetching user or password");
         }
            if ( (user.startsWith("[") && user.endsWith("]")) || (password.startsWith("[") && password.endsWith("]"))|| (port.startsWith("[") && port.endsWith("]"))||(url.startsWith("[") && url.endsWith("]")) )
            {
                throw new SvcLogicException("Duplicate entries found for  key "+vnf_Type + "." + protocol + "." + action +"in properties File");
            }
            if (StringUtils.isNotBlank(user))
                ctx.setAttribute(responsePrefix + "user", user);
            if (StringUtils.isNotBlank(password))
                ctx.setAttribute(responsePrefix +  "password", et.decrypt(password));
            if (StringUtils.isNotBlank(url))
                ctx.setAttribute(responsePrefix +  "url", url);
            if (StringUtils.isNotBlank(port))
                ctx.setAttribute(responsePrefix + "port", port);
            log.debug(ctx.getAttribute(responsePrefix + "user"));
                        log.debug(ctx.getAttribute(responsePrefix + "password"));
                        log.debug(ctx.getAttribute(responsePrefix + "url"));
                        log.debug(ctx.getAttribute(responsePrefix + "port"));
            }
                else
                {
                    throw new SvcLogicException("Error-as any of properties such as vnf-type,protocol,action are missing in ctx");
                }
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + "status", "failure");
            ctx.setAttribute(responsePrefix + "error-message", e.getMessage());
            log.info("Caught exception", e);
            throw new SvcLogicException(e.getMessage());
        }
    }
}
