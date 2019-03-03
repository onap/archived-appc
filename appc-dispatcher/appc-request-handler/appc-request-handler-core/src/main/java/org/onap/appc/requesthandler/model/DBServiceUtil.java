/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.requesthandler.model;

import java.io.File;
import java.net.URL;
import java.util.Properties;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicService;
import org.onap.ccsdk.sli.core.dblib.DBResourceManager;
import org.onap.ccsdk.sli.core.dblib.DbLibService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class DBServiceUtil {
    /*private static final Logger log = LoggerFactory.getLogger(DBServiceUtil.class);*/
    private static final EELFLogger log = EELFManager.getInstance().getLogger(DBServiceUtil.class);
    /*private static final String DBLIB_SERVICE = "org.onap.ccsdk.sli.core.dblib.DBResourceManager";*/
    private static final String DBLIB_SERVICE = "org.onap.ccsdk.sli.core.dblib.DbLibService";
    private DbLibService dbLibSvc = null;

    public DBServiceUtil() throws SQLException {
        log.info("Initializing DbService service");
        try {
            dbLibSvc = initDbLibService();
            if (dbLibSvc == null) {
                log.error("Got Exception While getting DB Connection");
                throw new SQLException("Got Exception While getting DB Connection");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new SQLException("An error occurred when instantiating DB service", e);
        }
    }

    public ResultSet getData(String query, List<String> paramList) throws Exception {
        ResultSet resultSet;
        StringBuilder sqlBuilder = new StringBuilder(query);
        log.info("Query :" + sqlBuilder.toString());
        try {
            resultSet = dbLibSvc.getData(sqlBuilder.toString(), new ArrayList(paramList), null);
        } catch (Exception e) {
            log.error("query " + sqlBuilder + " :: " + e.getMessage());
            throw new Exception("An error occurred when reading DB data", e);
        }
        return resultSet;
    }

    public static DbLibService initDbLibService() {
        DbLibService dbLibService = null;
        BundleContext bundleContext = null;
        ServiceReference serviceRef = null;
        Bundle bundle = FrameworkUtil.getBundle(SvcLogicService.class);
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
            dbLibService = (DbLibService) bundleContext.getService(serviceRef);
            if (dbLibService == null) {
                log.warn("DBLIB_SERVICE is null");
            }
        }
        if (dbLibService == null) {
            try {
                
                log.info("Using System Props");
                Properties props = new Properties();
                File file = new File("/opt/app/bvc/properties/dblib.properties");
                URL propURL = file.toURI().toURL();
                props.load(propURL.openStream());
                dbLibService = new DBResourceManager(props);
            
            } catch (Exception e) {
                log.error("Caught exception trying to create db service", e);
            }
            if (dbLibService == null) {
                log.warn("Could not create new DBResourceManager");
            }
        }
        return dbLibService;
    }

}
