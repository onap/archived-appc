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

package org.onap.appc.design.dbervices;

import static com.google.common.collect.Lists.newArrayList;

import java.sql.ResultSet;
import java.util.List;
import org.onap.ccsdk.sli.core.dblib.DBResourceManager;
import org.onap.ccsdk.sli.core.dblib.DbLibService;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbService {

    private static final Logger Log = LoggerFactory.getLogger(DbService.class);
    private static final String DBLIB_SERVICE = "org.onap.ccsdk.sli.core.dblib.DbLibService";
    private DbLibService dbLibSvc = null;

    public DbService() throws DBException {
        Log.info("Initializing DbService service");
        try
        {
            dbLibSvc = getDbLibService();
            if (dbLibSvc == null) {
                Log.error("Got Exception While getting DB Connection");
                throw new DBException("Got Exception While getting DB Connection");
            }
        }
        catch (Exception e) {
            Log.error(e.getMessage());
            throw new DBException("An error occurred when instantiating DB service", e);
        }
    }

    private static DbLibService getDbLibService() {
        
        DbLibService dbLibService = null;
        BundleContext bundleContext = null;
        ServiceReference serviceRef = null;

        Bundle bundle =  FrameworkUtil.getBundle(SvcLogicService.class);

        if (bundle != null) {
            bundleContext = bundle.getBundleContext();
        }

        if (bundleContext != null) {
            Log.debug("Getting bundle Context");
            serviceRef = bundleContext.getServiceReference(DBLIB_SERVICE);
        }

        if (serviceRef == null) {
            Log.warn("Could not find service reference for DBLib service");
                    
        } else {
            dbLibService = (DbLibService)bundleContext.getService(serviceRef);
            if (dbLibService == null) {
                Log.warn("DBLIB_SERVICE is null");
            }
        }
        if (dbLibService == null) {
            try {
                dbLibService = new DBResourceManager(System.getProperties());
            } catch (Exception e) {
                Log.error("Caught exception trying to create db service", e);
            }

            if (dbLibService == null) {
                Log.warn("Could not create new DBResourceManager");
            }
        }
        return dbLibService;
    }

    public ResultSet getDBData(String query) throws DBException{
        ResultSet resultSet;
        StringBuilder sqlBuilder = new StringBuilder(query);
        Log.info("Query: " + sqlBuilder.toString());
        try {
            resultSet = dbLibSvc.getData(sqlBuilder.toString(), null, null);
        } catch (Exception e) {
            Log.error("SQL query "+sqlBuilder+" :: " + e.getMessage());
            throw new DBException("An error occurred when reading DB data", e);
        }
        return resultSet;
    }

    public ResultSet getDBData(String query, List<String> paramList) throws DBException {
        ResultSet resultSet;
        StringBuilder sqlBuilder = new StringBuilder(query);
        Log.info("Query :" + sqlBuilder.toString());
        try {
            resultSet = dbLibSvc.getData(sqlBuilder.toString(), newArrayList(paramList), null);
        } catch (Exception e) {
            Log.error("query "+sqlBuilder+" :: " + e.getMessage());
            throw new DBException("An error occurred when reading DB data", e);
        }
        return resultSet;
    }
    
    public boolean updateDBData(String query, List<String> paramList) throws DBException{
        boolean update;
        StringBuilder sqlBuilder = new StringBuilder(query);
        Log.info("Query :" + sqlBuilder.toString());
        try {
            update = dbLibSvc.writeData(sqlBuilder.toString(), newArrayList(paramList), null);
        } catch (Exception e) {
            Log.error("query "+sqlBuilder+" :: " + e.getMessage());
            throw new DBException("An error occurred when updating DB data", e);
        }
        return update;
    }
}
