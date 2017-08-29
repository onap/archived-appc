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

package org.openecomp.appc.licmgr.impl;

import javax.sql.rowset.CachedRowSet;

import org.openecomp.appc.licmgr.Constants;
import org.openecomp.appc.licmgr.LicenseDataAccessService;
import org.openecomp.appc.licmgr.exception.DataAccessException;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.openecomp.sdnc.sli.resource.dblib.DbLibService;

import static org.openecomp.appc.licmgr.Constants.SDC_ARTIFACTS_FIELDS;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


@SuppressWarnings("JavaDoc")
public class LicenseDataAccessServiceImpl implements LicenseDataAccessService {

    private static EELFLogger logger = EELFManager.getInstance().getLogger(LicenseDataAccessServiceImpl.class);

    public void setSchema(String schema) {
        this.schema = schema;
    }

    private String schema;

    public void setDbLibService(DbLibService dbLibService) {
        this.dbLibService = dbLibService;
    }

    private DbLibService dbLibService;


    /**
     * empty constructor
     */
    public LicenseDataAccessServiceImpl(){}

    @Override
    public Map<String,String> retrieveLicenseModelData(String vnfType, String vnfVersion, String... fields) throws
                    DataAccessException {

        Map<String,String> result = new HashMap<>();
        if (null == fields || 0 == fields.length) fields = new String[]{SDC_ARTIFACTS_FIELDS.ARTIFACT_CONTENT.name()};

        String queryString = buildQueryStatement();

        ArrayList<String> argList = new ArrayList<>();
        argList.add(vnfType);
        argList.add(vnfVersion);
        argList.add(Constants.VF_LICENSE);

        try {

            final CachedRowSet data = dbLibService.getData(queryString, argList, Constants.NETCONF_SCHEMA);

            if (data.first()) {
                for (String field : fields) {
                    result.put(field, data.getString(field));
                }
            } else {
                String msg = "Missing license model for VNF_TYPE: " + vnfType + " and VNF_VERSION: " + vnfVersion + " in table " + Constants.SDC_ARTIFACTS;
                logger.info(msg);
            }
        } catch (SQLException e) {
            logger.error("Error Accessing Database " + e);
            throw new DataAccessException(e);
        }

        return result;
    }

    private String buildQueryStatement() {
        return "select * " + "from " + Constants.SDC_ARTIFACTS + " " +
            "where " + SDC_ARTIFACTS_FIELDS.RESOURCE_NAME.name() + " = ?" +
             " AND " + SDC_ARTIFACTS_FIELDS.RESOURCE_VERSION.name() + " = ?" +
             " AND " + SDC_ARTIFACTS_FIELDS.ARTIFACT_TYPE.name() + " = ?";
    }

}
