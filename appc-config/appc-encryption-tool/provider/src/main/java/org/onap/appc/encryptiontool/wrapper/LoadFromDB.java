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
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import javax.sql.rowset.CachedRowSet;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.onap.ccsdk.sli.core.dblib.DBResourceManager;
import org.slf4j.*;

public class LoadFromDB {

    private static final Logger log = LoggerFactory
            .getLogger(WrapperEncryptionTool.class);
    public static void main(String[] args) {
        int rowCount =0;
        ArrayList argList=null;

        String getselectData = " DA.VNF_TYPE, PR.PROTOCOL, PR.ACTION ,DA.USER_NAME,DA.PASSWORD,DA.PORT_NUMBER ";

        String clause = "  DA.VNF_TYPE=PR.VNF_TYPE group by PR.ACTION ";
        String tableName ="DEVICE_AUTHENTICATION DA , PROTOCOL_REFERENCE PR";
        DBResourceManager dbResourceManager = null;
        try {

            dbResourceManager = DbServiceUtil.initDbLibService();
            CachedRowSet data = DbServiceUtil.getData(tableName, argList, Constants.SCHEMA_SDNCTL, getselectData,clause );

            Map <String,String> mp = new HashMap<String,String>();
            while (data.next()) {

              mp.put(data.getString(1)+"."+data.getString(2)+"."+data.getString(3)+"."+"user",data.getString(4));
              mp.put(data.getString(1)+"."+data.getString(2)+"."+data.getString(3)+"."+"password",data.getString(5));
              mp.put(data.getString(1)+"."+data.getString(2)+"."+data.getString(3)+"."+"port",data.getString(6));
              mp.put(data.getString(1)+"."+data.getString(2)+"."+data.getString(3)+"."+"url","");
              rowCount++;
            }


            log.info("Size of Map data:"+mp.size());
                File file  = new File(Constants.APPC_CONFIG_DIR );
                file.mkdir();
                file  = new File(Constants.APPC_CONFIG_DIR + "/appc_southbound.properties");
                if(file.exists())
                {
                     log.info("APPC-MESSAGE:" + " File already Exists");
                }
                else
                {
                    file.createNewFile();
                    log.info("APPC-MESSAGE:" + " New  File is created");
                }
            if (rowCount == 0)
                log.info("APPC-MESSAGE: ERROR - No record Found ");
            else {


                log.info("Size of Map file:"+mp.size());
                PropertiesConfiguration conf = new PropertiesConfiguration(
                        Constants.APPC_CONFIG_DIR + "/appc_southbound.properties");


                for (Map.Entry<String, String> key : mp.entrySet()) {
                           log.debug(key.getKey() + ":" + key.getValue());
                         if(key.getValue()==null)
                    {
                        key.setValue("");
                    }
                    conf.setProperty(key.getKey(), key.getValue());
              }


                conf.save();
                log.info("APPC-MESSAGE:" + "properties updated successfully");

            }
        } catch (Exception e) {
            log.info("Caught exception", e);
            log.info("APPC-MESSAGE:" + e.getMessage());
        } finally {
            if (dbResourceManager != null) {
                dbResourceManager.cleanUp();
                 System.exit(0);
            }
        }
    }

}
