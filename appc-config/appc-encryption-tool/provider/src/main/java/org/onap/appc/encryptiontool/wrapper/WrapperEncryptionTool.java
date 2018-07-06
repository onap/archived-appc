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

import java.util.ArrayList;
import javax.sql.rowset.CachedRowSet;
import org.apache.commons.lang.StringUtils;
import org.onap.ccsdk.sli.core.dblib.DBResourceManager;
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
        updateProperties(user, vnfType, enPass, action, port, url, protocol);
    }

    public static void updateProperties(String user, String vnfType, String enPass, String action, String port,
            String url, String protocol) {
        DBResourceManager dbResourceManager = null;
        ArrayList<String> getList = new ArrayList<>();
        getList.add(vnfType);
        getList.add(protocol);
        getList.add(action);
        String whereClause = " VNF_TYPE = ? AND  PROTOCOL = ?  AND ACTION = ? ";
        String setClause = " USER_NAME = ?, PASSWORD = ?, PORT_NUMBER = ?,URL = ? ";
        String insertClause = " USER_NAME,PASSWORD,PORT_NUMBER,URL,VNF_TYPE,PROTOCOL,ACTION";
        String insertsetClause = " ?,?,?,?,?,?,?";
        try {
            dbResourceManager = DbServiceUtil.initDbLibService();
            CachedRowSet data = DbServiceUtil.getData(Constants.DEVICE_AUTHENTICATION, getList, Constants.SCHEMA_SDNCTL,
                    "*", whereClause);
            int rowCount = 0;
            if (data.first()) {
                rowCount++;
                   log.info(rowCount + "rowcount");
            }
            getList.clear();
            getList.add(user);
            getList.add(enPass);
            getList.add(port);
            getList.add(url);
            getList.add(vnfType);
            getList.add(protocol);
            getList.add(action);
            if (rowCount == 1) {
                DbServiceUtil.updateDB(Constants.DEVICE_AUTHENTICATION, getList, whereClause, setClause);
                log.info("APPC-MESSAGE: Password Updated Successfully");
            } else {
                DbServiceUtil.insertDB(Constants.DEVICE_AUTHENTICATION, getList, insertClause, insertsetClause);
                log.info("APPC-MESSAGE: password  Inserted Successfully");
            }
        } catch (Exception e) {
            log.debug("Caught Exception", e);
            log.info("Caught exception", e);
            log.info("APPC-MESSAGE:" + e.getMessage());
        } finally {
            //When dbResourceManager is not created then no need to cleanup
            if (dbResourceManager != null) {
                dbResourceManager.cleanUp();
            }
        }
    }

}
