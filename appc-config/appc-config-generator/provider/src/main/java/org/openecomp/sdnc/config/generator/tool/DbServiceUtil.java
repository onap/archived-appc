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

package org.openecomp.sdnc.config.generator.tool;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

import javax.sql.rowset.CachedRowSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.onap.ccsdk.sli.core.dblib.DBResourceManager;
import org.onap.ccsdk.sli.core.dblib.DbLibService;

public class DbServiceUtil
{    
    private static final Logger log = LoggerFactory
            .getLogger(DbServiceUtil.class);
    
    private static Properties props;
    private static DBResourceManager jdbcDataSource = null;
    

    public static boolean updateDB(String tableName, ArrayList inputArgs, String scema, String whereClause, String setCluase) throws SQLException  
    {
            String updatePasswordString = "update " + tableName + " set " +  setCluase +  " where " + whereClause ;
            boolean result = jdbcDataSource.writeData(updatePasswordString, inputArgs,Constants.SCHEMA_SDNCTL);
            return result;
    }
    
    
     public static CachedRowSet getData(String tableName, ArrayList argList, String schema, String getselectData, String getDataClasue ) throws SQLException
     {         
         String selectQuery = "select " + getselectData + "from " + tableName + " where " + getDataClasue ;         
         CachedRowSet data = jdbcDataSource.getData(selectQuery, argList, schema);         
         return data;         
     }


    public static DBResourceManager initDbLibService() throws Exception 
    {
        props = new Properties();
        File file = new File("/opt/app/bvc/properties/dblib.properties");        
        URL propURL = file.toURI().toURL();    
        props.load(propURL.openStream());        
        jdbcDataSource = DBResourceManager.create(props);
        return jdbcDataSource;
        
    }



}
