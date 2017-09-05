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

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Properties;

import javax.sql.rowset.CachedRowSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.onap.ccsdk.sli.core.dblib.DBResourceManager;

public class WrapperEncryptionTool {

    private static final Logger log = LoggerFactory
            .getLogger(WrapperEncryptionTool.class);

    public static void main(String[] args)
    {
        int rowCount = 0;
        String vnf_type=args[0];
        String user = args[1];
        String password = args[2];
        String action = args[3];
        String port = args[4];
        String url = args[5];

        if("".equals(vnf_type))
        {
            System.out.println("ERROR-VNF_TYPE can not be null");
            return;
        }
        if("".equals(user))
        {
            System.out.println("ERROR-USER can not be null");
            return;
        }
        if("".equals(password))
        {
            System.out.println("ERROR-PASSWORD can not be null");
            return;
        }

        EncryptionTool encryptionTool = EncryptionTool.getInstance();
        String enPass = encryptionTool.encrypt(password);

        if(action != null && !action.isEmpty()){
            updateProperties(user,vnf_type , enPass, action, port, url);
            return ;
        }

        ArrayList<String> argList = new ArrayList<>();
        argList.add(vnf_type);
        argList.add(user);
        String clause = " vnf_type = ? and user_name = ? ";
        String setClause = " password = ? ";
        String getselectData = " * ";
        DBResourceManager dbResourceManager = null;
        try
        {
            dbResourceManager = DbServiceUtil.initDbLibService();
            CachedRowSet data = DbServiceUtil.getData(Constants.DEVICE_AUTHENTICATION, 
                argList, Constants.SCHEMA_SDNCTL, getselectData,clause );
            while(data.next())
            {
                rowCount ++;
            }
            if(rowCount == 0)
                log.info("APPC-MESSAGE: ERROR - No record Found for VNF_TYPE: " + vnf_type + ", User " + user );
            else
            {
                argList.clear();
                argList.add(enPass);
                argList.add(vnf_type);
                argList.add(user);
                DbServiceUtil.updateDB(Constants.DEVICE_AUTHENTICATION, argList, 
                    Constants.SCHEMA_SDNCTL, clause, setClause);
                log.info("APPC-MESSAGE: Password Updated Successfully");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            log.info("APPC-MESSAGE:" + e.getMessage());
        }
        finally
        {
            dbResourceManager.cleanUp();
            System.exit(0);
        }
    }

    private static void updateProperties(String user, String vnf_type, String password, 
            String action, String port, String url) {

        log.info("Received Inputs User:" + user + " vnf_type:"  + vnf_type + " action:" + action );

        String property =  vnf_type + "." + action + ".";

        try {
            PropertiesConfiguration conf = new PropertiesConfiguration(Constants.APPC_CONFIG_DIR  + "/appc_southbound.properties");
            conf.setProperty(property + "user",  user);
            if(port != null && !port.isEmpty() )
                conf.setProperty(property + "port",  port);
            if(password != null && !password.isEmpty() )
                conf.setProperty(property + "password",  password);
            if(url != null && !url.isEmpty() )
            conf.setProperty(property + "url",  url);

            conf.save();

        }
        catch (Exception e ) {
            e.printStackTrace();
            log.info("APPC-MESSAGE:" + e.getMessage());
        }

    }
}
