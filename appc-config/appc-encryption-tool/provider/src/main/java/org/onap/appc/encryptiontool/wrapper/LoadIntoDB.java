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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.onap.ccsdk.sli.core.dblib.DBResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadIntoDB {

    private static final Logger log = LoggerFactory.getLogger(LoadIntoDB.class);

    public static void main(String[] args) {

        Map<String, String> finalMap = fetchProperties();
        insertProperties(finalMap);

    }

    private static Map<String, String> fetchProperties() {
        Map<String, String> finalMap = new HashMap<String, String>();
        try {
            PropertiesConfiguration conf = new PropertiesConfiguration(
                    Constants.APPC_CONFIG_DIR + "/appc_southbound.properties");
            Iterator<String> confKeys = conf.getKeys();
            List<String> listofAllKeys = new ArrayList<String>();
            confKeys.forEachRemaining(new Consumer<String>() {
                public void accept(String confKeys) {
                    if (confKeys.contains(".user")) {
                        String key = confKeys.split(".user")[0];
                        if (StringUtils.isNotBlank(key.split("\\.")[0]) || StringUtils.isNotBlank(key.split("\\.")[1])
                                || StringUtils.isNotBlank(key.split("\\.")[2])) {

                            if (StringUtils.isNotBlank(conf.getString(key + ".user"))
                                    || StringUtils.isNotBlank(conf.getString(key + ".port"))
                                    || StringUtils.isNotBlank(conf.getString(key + ".url"))
                                    || StringUtils.isNotBlank(conf.getString(key + ".password")))
                                listofAllKeys.add(key);
                        }
                    }
                }
            });
            List<String> listWithoutDuplicates = listofAllKeys.stream().distinct().collect(Collectors.toList());
            listWithoutDuplicates.forEach(new Consumer<String>() {
                public void accept(String key) {
                    String user = StringUtils.isNotBlank(conf.getString(key + ".user")) ? conf.getString(key + ".user")
                            : " ";
                    String password = StringUtils.isNotBlank(conf.getString(key + ".password"))
                            ? conf.getString(key + ".password")
                            : " ";
                    String port = StringUtils.isNotBlank(conf.getString(key + ".port")) ? conf.getString(key + ".port")
                            : "0";
                    String url = StringUtils.isNotBlank(conf.getString(key + ".url")) ? conf.getString(key + ".url")
                            : " ";
                    finalMap.put(key, user + "," + password + "," + port + "," + url);
                }
            });

        } catch (Exception e) {
            log.info("APPC-MESSAGE:" + e.getMessage());
        }
        return finalMap;
    }

    private static boolean insertProperties(Map<String, String> map) {

        DBResourceManager dbResourceManager = null;
        try {
            dbResourceManager = DbServiceUtil.initDbLibService();
            boolean delete = DbServiceUtil.deleteData(Constants.DEVICE_AUTHENTICATION, null);
            StringBuilder sql = new StringBuilder(
                    "insert into DEVICE_AUTHENTICATION(VNF_TYPE, PROTOCOL,ACTION,USER_NAME,PASSWORD,PORT_NUMBER,URL) VALUES");
            StringBuilder placeholders = new StringBuilder();
            if (delete) {
                map.forEach((key, value) -> {
                    try {
                        placeholders.append("(").append("'" + key.split("\\.")[0] + "'").append(",")
                                .append("'" + key.split("\\.")[1] + "'").append(",")
                                .append("'" + key.split("\\.")[2] + "'").append(",")
                                .append("'" + value.split(",")[0] + "'").append(",")
                                .append("'" + value.split(",")[1] + "'").append(",").append(value.split(",")[2])
                                .append(",").append("'" + value.split(",")[3] + "'").append(")").append(",")
                                .append("\n");

                    } catch (Exception e) {
                        log.info(" " + e.getStackTrace());
                        log.info("APPC-MESSAGE:" + e.getMessage());
                    }
                });
                String values = placeholders.substring(0, placeholders.length() - 2);
                sql = sql.append(values);
                boolean status = dbResourceManager.writeData(sql.toString(), null, Constants.SCHEMA_SDNCTL);
                log.info("APPC-MESSAGE:" + "Inserted Successfully into " + Constants.DEVICE_AUTHENTICATION + "status"
                        + status);
            }
        } catch (Exception e) {
            log.info(" " + e.getStackTrace());
            log.info("APPC-MESSAGE:" + e.getMessage());
            dbResourceManager.cleanUp();
        } finally {
            dbResourceManager.cleanUp();
            System.exit(0);
        }
        return true;
    }
}
