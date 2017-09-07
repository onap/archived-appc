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

package org.openecomp.appc.encryptiontool.wrapper;

import java.util.ArrayList;

import javax.sql.rowset.CachedRowSet;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.onap.ccsdk.sli.core.dblib.DBResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WrapperEncryptionTool {

    private static final Logger log = LoggerFactory.getLogger(WrapperEncryptionTool.class);

    public static void main(String[] args) {
        int rowCount = 0;
        String vnfType = args[0];
        String user = args[1];
        String password = args[2];
        String action = args[3];
        String port = args[4];
        String url = args[5];

        if ("".equals(vnfType)) {
            log.info("ERROR-VNF_TYPE can not be null");
            return;
        }
        if ("".equals(user)) {
            log.info("ERROR-USER can not be null");
            return;
        }
        if ("".equals(password)) {
            log.info("ERROR-PASSWORD can not be null");
            return;
        }

        EncryptionTool et = EncryptionTool.getInstance();
        String enPass = et.encrypt(password);

        if (action != null && !action.isEmpty()) {
            updateProperties(user, vnfType, enPass, action, port, url);
            return;
        }

        ArrayList<String> argList = new ArrayList<>();
        argList.add(vnfType);
        argList.add(user);
        String clause = " vnfType = ? and user_name = ? ";
        String setClause = " password = ? ";
        String getselectData = " * ";
        DBResourceManager dbResourceManager = null;
        try (CachedRowSet data = DbServiceUtil.getData(Constants.DEVICE_AUTHENTICATION, argList,
                Constants.SCHEMA_SDNCTL, getselectData, clause);) {
            dbResourceManager = DbServiceUtil.initDbLibService();

            while (data.next()) {
                rowCount++;
            }
            if (rowCount == 0)
                log.info("APPC-MESSAGE: ERROR - No record Found for VNF_TYPE: %, User % ", vnfType, user);
            else {
                argList.clear();
                argList.add(enPass);
                argList.add(vnfType);
                argList.add(user);
                DbServiceUtil.updateDB(Constants.DEVICE_AUTHENTICATION, argList, Constants.SCHEMA_SDNCTL, clause,
                        setClause);
                log.info("APPC-MESSAGE: Password Updated Successfully");
            }
        } catch (Exception e) {
            log.info("Caught exception", e);
            log.info("APPC-MESSAGE:" + e.getMessage());
        } finally {
            if (dbResourceManager != null) {
                dbResourceManager.cleanUp();
            }
        }
    }

    private static void updateProperties(String user, String vnfType, String password, String action, String port,
            String url) {

        log.info("Received Inputs User:%s vnfType:%s action:%s", user, vnfType, action);
        String property = vnfType + "." + action + ".";


        try {
            PropertiesConfiguration conf =
                    new PropertiesConfiguration(Constants.APPC_CONFIG_DIR + "/appc_southbound.properties");
            conf.setProperty(property + "user", user);
            if (port != null && !port.isEmpty())
                conf.setProperty(property + "port", port);
            if (password != null && !password.isEmpty())
                conf.setProperty(property + "password", password);
            if (url != null && !url.isEmpty())
                conf.setProperty(property + "url", url);

            conf.save();

        } catch (Exception e) {
            log.info("Caught Exception", e);
        }
    }
}
