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

package org.onap.appc.encryptiontool.wrapper;
import java.util.Iterator;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang.StringUtils;
import org.onap.appc.encryptiontool.sftp.SftpConnect;

public class WrapperEncryptionTool {

    private static final Logger log = LoggerFactory.getLogger(WrapperEncryptionTool.class);
    public static void main(String[] args) {
      try{
        String vnf_type = args[0];
        String protocol = args[1];
        String user = args[2];
        String password = args[3];
        String action = args[4];
        String port = args[5];
        String url = args[6];
        if (StringUtils.isNotBlank(user)) {
            log.info("ERROR-USER can not be null");
        return;
        }
        if (StringUtils.isNotBlank(password)) {
            log.info("ERROR-PASSWORD can not be null");
            return;
        }
        if (StringUtils.isNotBlank(protocol) || StringUtils.isNotBlank(vnf_type) || StringUtils.isNotBlank(action)) {
            log.info("ERROR-PROTOCOL ,Action and VNF-TYPE both can not be null");
            return;
        }
        EncryptionTool et = EncryptionTool.getInstance();
        String enPass = et.encrypt(password);
        boolean flag = false;
            if ((protocol != null && !protocol.isEmpty())) {
                 log.info("trying to do update properties");
                flag = updateProperties(user, vnf_type, enPass, action, port, url, protocol);
            }
            if (flag) {
                    log.info("APPC-trying to do sftp");
                    SftpConnect sftp = new SftpConnect();
                    sftp.performSftp();
        }
       }
        catch (Exception e) {
            log.info("Caught exception", e);
            log.info("APPC-MESSAGE:" + e.getMessage());
        }
         finally{
    System.exit(0);
        }
    }
    public static boolean updateProperties(String user, String vnf_type, String password, String action, String port,
            String url, String protocol) {
        try {
            log.info("Received Inputs protocol:%s User:%s vnfType:%s action:%surl:%s port:%s ", protocol, user,
                    vnf_type, action, url, port);
            String property = protocol;
            if (!StringUtils.isNotBlank(vnf_type)) {
                if (!StringUtils.isNotBlank(protocol)) {
                    if (!StringUtils.isNotBlank(action)) {
                        property = vnf_type + "." + protocol + "." + action;
                    }
                } else {
                    property = vnf_type;
                }
            } else {
                if (!StringUtils.isNotBlank(protocol)) {
                    property = protocol;
                }
            }
            PropertiesConfiguration conf = new PropertiesConfiguration(
                    System.getenv("APPC_CONFIG_DIR")+"/appc_southbound.properties");
            if (conf.subset(property) != null) {
                Iterator<String> it = conf.subset(property).getKeys();
                if (it.hasNext()) {
                    while (it.hasNext()) {
                        String key = it.next();
                        log.info("key---value pairs");
                        log.info(property + "." + key + "------" + conf.getProperty(property + "." + key));
                        if ((property + "." + key).contains("user")) {
                            if (user != null && !user.isEmpty())
                            conf.setProperty(property + "." + key, user);
                        }
                        if ((property + "." + key).contains("password")) {
                            if (password != null && !password.isEmpty())
                                conf.setProperty(property + "." + key, password);
                        }
                        if ((property + "." + key).contains("port")) {
                            if (port != null && !port.isEmpty())
                                conf.setProperty(property + "." + key, port);
                        }
                        if ((property + "." + key).contains("url")) {
                            if (url != null && !url.isEmpty())
                                conf.setProperty(property + "." + key, url);
                        }
                    }
                } else {
                    if (conf.containsKey(property + "." + "user")) {
                       if (user != null && !user.isEmpty())
                        conf.setProperty(property + "." + "user", user);
                    } else {
                        conf.addProperty(property + "." + "user", user);
                    }
                    if (conf.containsKey(property + "." + "password")) {
                        if (password != null && !password.isEmpty())
                            conf.setProperty(property + "." + "password", password);
                    } else {
                        conf.addProperty(property + "." + "password", password);
                    }
                    if (conf.containsKey(property + "." + "port")) {
                        if (port != null && !port.isEmpty())
                            conf.setProperty(property + "." + "port", port);
                    } else {
                        if (port != null && !port.isEmpty())
                            conf.addProperty(property + "." + "port", port);
                    }
                    if (conf.containsKey(property + "." + "url")) {
                        if (url != null && !url.isEmpty())
                            conf.setProperty(property + "." + "url", url);
                    } else {
                        conf.addProperty(property + "." + "url", url);
                    }
                }
            }
            conf.save();
            return true;
        } catch (Exception e) {
            log.debug("Caught Exception", e);
            log.info("Caught exception", e);
            log.info("APPC-MESSAGE:" + e.getMessage());
        return false;
        }
         finally{
           }
    }
}
