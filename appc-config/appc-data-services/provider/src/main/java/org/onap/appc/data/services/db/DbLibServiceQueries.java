/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
 *
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.data.services.db;

import java.sql.SQLException;
import java.util.ArrayList;

import javax.sql.rowset.CachedRowSet;

import org.onap.ccsdk.sli.core.dblib.DBResourceManager;
import org.onap.ccsdk.sli.core.dblib.DbLibService;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource.QueryStatus;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class DbLibServiceQueries {
    
    private static final String DBLIB_SERVICE = "org.onap.ccsdk.sli.core.dblib.DbLibService";
    private static final EELFLogger log = EELFManager.getInstance().getLogger(DbLibServiceQueries.class);
    
    DbLibService dbLibService;
    
    public DbLibServiceQueries() {
        this.dbLibService = getDbLibService();
        if(this.dbLibService == null) {
            throw new NullPointerException("DbLibService reference not found");
        }
    }
    
    public DbLibServiceQueries(DbLibService dbLibService) {
        this.dbLibService = dbLibService;
        if(this.dbLibService == null) {
            throw new NullPointerException("Provided DbLibService is null");
        }
    }
    
    public DbLibServiceQueries(DbLibService dbLibService, boolean allowNull) {
        this.dbLibService = dbLibService;
        if(this.dbLibService == null && !allowNull) {
            throw new NullPointerException("Provided DbLibService is null");
        }
    }
    
    public QueryStatus query(String query, SvcLogicContext ctx) {
        ArrayList<String> arguments = new ArrayList<>();
        query = CtxParameterizedResolver.resolveCtxVars(query, ctx, arguments);
        return performQuery(query, ctx, null, arguments);
    }
    
    public QueryStatus query(String query, String prefix, SvcLogicContext ctx) {
        ArrayList<String> arguments = new ArrayList<>();
        query = CtxParameterizedResolver.resolveCtxVars(query, ctx, arguments);
        return performQuery(query, ctx, prefix, arguments);
    }
    
    public QueryStatus query(String query, SvcLogicContext ctx, ArrayList<String> arguments) {
        return performQuery(query, ctx, null, valueOfArrayList(arguments));
    }
    public QueryStatus query(String query, SvcLogicContext ctx, String prefix, ArrayList<String> arguments) {
        return performQuery(query, ctx, prefix, valueOfArrayList(arguments));
    }
    
    private QueryStatus performQuery(String query, SvcLogicContext ctx, String prefix, ArrayList<String> arguments) {
        
        CachedRowSet result = null;
        try {
            result = dbLibService.getData(query, arguments, null);
            if (!result.next()) {
                log.debug("No data found");
                return QueryStatus.NOT_FOUND;
            } else {
                CtxParameterizedResolver.saveCachedRowSetToCtx(result, ctx, prefix, dbLibService);
            }
        } catch (SQLException e) {
            log.error("Exception in query()",e);
            return QueryStatus.FAILURE;
        }
        return QueryStatus.SUCCESS;
        
    }
    public QueryStatus save(String query, SvcLogicContext ctx) {
        ArrayList<String> arguments = new ArrayList<>();
        query = CtxParameterizedResolver.resolveCtxVars(query, ctx, arguments);
        return performSave(query, arguments);
    }
    
    public QueryStatus save(String query, SvcLogicContext ctx, ArrayList<String> arguments) {
        return performSave(query, valueOfArrayList(arguments));
    }
    
    private QueryStatus performSave(String query, ArrayList<String> arguments) {
        boolean success = false;
        try {
            success = dbLibService.writeData(query, arguments, null);
        } catch (SQLException e) {
            log.error("Exception in save()",e);
            success = false;
        }
        if(success) {
            return QueryStatus.SUCCESS;
        }
        return QueryStatus.FAILURE;
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
            log.debug("Getting bundle Context");
            serviceRef = bundleContext.getServiceReference(DBLIB_SERVICE);
        }

        if (serviceRef == null) {
            log.warn("Could not find service reference for DBLib service");
                    
        } else {
            dbLibService = (DbLibService)bundleContext.getService(serviceRef);
            if (dbLibService == null) {
                log.warn("DBLIB_SERVICE is null");
            }
        }
        if (dbLibService == null) {
            try {
                dbLibService = new DBResourceManager(System.getProperties());
            } catch (Exception e) {
                log.error("Caught exception trying to create db service", e);
            }

            if (dbLibService == null) {
                log.warn("Could not create new DBResourceManager");
            }
        }
        return dbLibService;
    }
    
    //By using String.valueOf on the array list items, we can store any null values as
    //Strings with the value "null". This mirrors the way queries worked prior to the
    //prepared statements.
    private ArrayList<String> valueOfArrayList(ArrayList<String> original) {
        ArrayList<String> valueOfList = new ArrayList<>();
        for(String s : original) {
            valueOfList.add(String.valueOf(s));
        }
        return valueOfList;
    }

}
