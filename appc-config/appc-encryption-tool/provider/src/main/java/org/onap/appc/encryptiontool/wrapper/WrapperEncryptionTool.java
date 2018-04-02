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

import java.util.Iterator;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WrapperEncryptionTool {

    private static final Logger log = LoggerFactory.getLogger(WrapperEncryptionTool.class);
    private static final String USER_PARAM = "user";
    private static final String PASS_PARAM = "password";
    private static final String URL_PARAM = "url";
    private static final String PORT_PARAM = "port";

    private WrapperEncryptionTool() {
    }

    public static void main(String[] args) {
        String vnfType = args[0];
        String protocol = args[1];
        String user = args[2];
        String password = args[3];
        String action = args[4];
        String port = args[5];
        String url = args[6];

        if (StringUtils.isBlank(user)) {
            log.info("ERROR-USER can not be null");
            return;
        }
        if (StringUtils.isBlank(password)) {
            log.info("ERROR-PASSWORD can not be null");
            return;
        }
        if (StringUtils.isBlank(protocol) || StringUtils.isBlank(vnfType) || StringUtils.isBlank(action)) {
            log.info("ERROR-PROTOCOL ,Action and VNF-TYPE both can not be null");
            return;
        }

        EncryptionTool et = EncryptionTool.getInstance();
        String enPass = et.encrypt(password);

        if (StringUtils.isBlank(protocol)) {
            updateProperties(user, vnfType, enPass, action, port, url, protocol);
        }
    }

    public static void updateProperties(String user, String vnfType, String password, String action, String port,
        String url, String protocol) {
        try {
            log.info("Received Inputs protocol:%s User:%s vnfType:%s action:%surl:%s port:%s ", protocol, user,
                vnfType, action, url, port);
            String property = protocol;
            if (StringUtils.isNotBlank(vnfType)) {
                if (StringUtils.isNotBlank(protocol) && StringUtils.isNotBlank(action)) {
                    property = vnfType + "." + protocol + "." + action;
                } else if (StringUtils.isNotBlank(protocol)){
                    property = vnfType;
                }
            } else if (StringUtils.isNotBlank(protocol)){
                property = protocol;
            }

            PropertiesConfiguration conf = new PropertiesConfiguration(
                System.getenv("APPC_CONFIG_DIR") + "/appc_southbound.properties");

            if (conf.subset(property) != null) {

                Iterator<String> it = conf.subset(property).getKeys();
                if (it.hasNext()) {
                    while (it.hasNext()) {
                        String key = it.next();
                        log.info("key---value pairs");
                        log.info(property + "." + key + "------" + conf.getProperty(property + "." + key));
                        resolveProperty(user, password, port, url, property, conf, key);
                    }
                } else {
                    resolvePropertyAction(user, password, port, url, property, conf);
                }
            }
            conf.save();
        } catch (Exception e) {
            log.debug("Caught Exception", e);
            log.info("Caught exception", e);
            log.info("APPC-MESSAGE:" + e.getMessage());

        } finally {
            System.exit(0);
        }
    }

    private static void resolvePropertyAction(String user, String password, String port, String url, String property,
        PropertiesConfiguration conf) {
        if (containsParam(user, property, conf, USER_PARAM)) {
            conf.setProperty(property + "." + USER_PARAM, user);
        } else {
            conf.addProperty(property + "." + USER_PARAM, user);
        }
        if (containsParam(user, property, conf, PASS_PARAM)) {
            conf.setProperty(property + "." + PASS_PARAM, password);
        } else {
            conf.addProperty(property + "." + PASS_PARAM, password);
        }
        if (containsParam(user, property, conf, PORT_PARAM)) {
            conf.setProperty(property + "." + PORT_PARAM, port);
        } else if (port != null && !port.isEmpty()) {
            conf.addProperty(property + "." + PORT_PARAM, port);
        }
        if (containsParam(user, property, conf, URL_PARAM)) {
            conf.setProperty(property + "." + URL_PARAM, url);
        } else {
            conf.addProperty(property + "." + URL_PARAM, url);
        }
    }

    private static void resolveProperty(String user, String password, String port, String url, String property,
        PropertiesConfiguration conf, String key) {
        if (contains(user, property, key, USER_PARAM)) {
            conf.setProperty(property + "." + key, user);
        }
        if (contains(user, property, key, PASS_PARAM)) {
            conf.setProperty(property + "." + key, password);
        }
        if (contains(user, property, key, PORT_PARAM)) {
            conf.setProperty(property + "." + key, port);
        }
        if (contains(user, property, key, URL_PARAM)) {
            conf.setProperty(property + "." + key, url);
        }
    }

    private static boolean containsParam(String var, String property, PropertiesConfiguration conf, String param) {
        return StringUtils.isNotBlank(var) && conf.containsKey(property + "." + param);
    }

    private static boolean contains(String var, String property, String key, String param) {
        return StringUtils.isNotBlank(var) && (property + "." + key).contains(param);
    }
}
