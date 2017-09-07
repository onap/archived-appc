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

package org.openecomp.appc.design.dbervices;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.UUID;

import org.onap.ccsdk.sli.core.sli.provider.SvcLogicService;
import org.onap.ccsdk.sli.core.dblib.DBResourceManager;
import org.onap.ccsdk.sli.core.dblib.DbLibService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbService {

    private static final Logger Log = LoggerFactory.getLogger(DbService.class);
    private static final String DBLIB_SERVICE = "org.onap.ccsdk.sli.core.dblib.DBResourceManager";
    DbLibService dblibSvc = null;
    String errorMsg = null;

    public DbService() throws Exception {
        DbLibService dblibSvc = null;
        Log.info("Initializing DbService service");
        try
        {
            dblibSvc = getDbLibService();
            if (dblibSvc == null) {
                Log.error("Got Exception While getting DB Connection");
                throw new Exception("Got Exception While getting DB Connection");
            }
            this.dblibSvc = dblibSvc;
        }
        catch (Exception e) {
            Log.error(e.getMessage());
            throw e;
        }
    }

    private static DbLibService getDbLibService() {
        
        DbLibService dblibSvc = null;
        BundleContext bctx = null;
        ServiceReference sref = null;

        Bundle bundle =  FrameworkUtil.getBundle(SvcLogicService.class);

        if (bundle != null) {
            bctx = bundle.getBundleContext();
        }

        if (bctx != null) {
            Log.debug("Getting bundle Context");
            sref = bctx.getServiceReference(DBLIB_SERVICE);
        }

        if (sref == null) {
            Log.warn("Could not find service reference for DBLib service");
                    
        } else {
            dblibSvc = (DbLibService) bctx.getService(sref);
            if (dblibSvc == null) {
                Log.warn("DBLIB_SERVICE is null");
            }
        }
        if (dblibSvc == null) {
            try {
                dblibSvc = new DBResourceManager(System.getProperties());
            } catch (Exception e) {
                Log.error("Caught exception trying to create db service", e);
            }

            if (dblibSvc == null) {
                Log.warn("Could not create new DBResourceManager");
            }
        }
        return (dblibSvc);
    }

    public ResultSet getDBData(String query) throws Exception {
        ResultSet resultSet;
        StringBuilder sqlBuilder = new StringBuilder(query);
        Log.info("Query: " + sqlBuilder.toString());
        try {
            resultSet = dblibSvc.getData(sqlBuilder.toString(), null, null);
        } catch (Exception e) {
            Log.error("SQL query "+sqlBuilder+" :: " + e.getMessage());
            throw e;
        }
        return resultSet;
    }

    public ResultSet getDBData(String query, ArrayList<String> paramList) throws Exception {
        ResultSet resultSet;
        StringBuilder sqlBuilder = new StringBuilder(query);
        Log.info("Query :" + sqlBuilder.toString());
        try {
            resultSet = dblibSvc.getData(sqlBuilder.toString(), paramList, null);
        } catch (Exception expObj) {
            Log.error("query "+sqlBuilder+" :: " + expObj.getMessage());
            throw expObj;
        }
        return resultSet;
    }
    
    public boolean updateDBData(String query, ArrayList<String> paramList) throws Exception {
        boolean update;
        StringBuilder sqlBuilder = new StringBuilder(query);
        Log.info("Query :" + sqlBuilder.toString());
        try {
            update = dblibSvc.writeData(sqlBuilder.toString(), paramList, null);
        } catch (Exception expObj) {
            Log.error("query "+sqlBuilder+" :: " + expObj.getMessage());
            throw expObj;
        }
        return update;
    }
}
