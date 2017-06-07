/*-
 * ============LICENSE_START=======================================================
 * APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Amdocs
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */

package org.openecomp.appc.licmgr.impl;

import javax.sql.rowset.CachedRowSet;

import org.openecomp.appc.licmgr.Constants;
import org.openecomp.appc.licmgr.LicenseDataAccessService;
import org.openecomp.appc.licmgr.exception.DataAccessException;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.openecomp.sdnc.sli.resource.dblib.DbLibService;

import static org.openecomp.appc.licmgr.Constants.ASDC_ARTIFACTS_FIELDS;

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
        if (null == fields || 0 == fields.length) fields = new String[]{ASDC_ARTIFACTS_FIELDS.ARTIFACT_CONTENT.name()};

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
                String msg = "Missing license model for VNF_TYPE: " + vnfType + " and VNF_VERSION: " + vnfVersion + " in table " + Constants.ASDC_ARTIFACTS_TABLE_NAME;
                logger.info(msg);
            }
        } catch (SQLException e) {
            logger.error("Error Accessing Database " + e);
            throw new DataAccessException(e);
        }

        return result;
    }

    private String buildQueryStatement() {
        return "select * " + "from " + Constants.ASDC_ARTIFACTS_TABLE_NAME + " " +
            "where " + ASDC_ARTIFACTS_FIELDS.RESOURCE_NAME.name() + " = ?" +
             " AND " + ASDC_ARTIFACTS_FIELDS.RESOURCE_VERSION.name() + " = ?" +
             " AND " + ASDC_ARTIFACTS_FIELDS.ARTIFACT_TYPE.name() + " = ?";
    }

    /**
     * Implementation of storeArtifactPayload()
     * @see LicenseDataAccessService
     */
    @Override
    public void storeArtifactPayload(Map<String, String> parameters) throws RuntimeException {

        if(parameters == null || parameters.isEmpty()) {
            throw new RuntimeException("No parameters for insert are provided");
        }

        String insertStr = "INSERT INTO " + Constants.ASDC_ARTIFACTS_TABLE_NAME + "(";
        String valuesStr = "VALUES(";
        String insertStatementStr;

        ArrayList<String> params = new ArrayList<>();
        boolean firstTime = true;
        for(Map.Entry<String, String> entry : parameters.entrySet()) {
            if(!firstTime) {
                insertStr += ",";
                valuesStr += ",";
            }
            else {
                firstTime = false;
            }
            insertStr += entry.getKey();
            valuesStr += "?";

            params.add(entry.getValue());
        }

        insertStr += ")";
        valuesStr += ")";
        insertStatementStr = insertStr + " " + valuesStr;

        executeStoreArtifactPayload(insertStatementStr, params);
    }

    /**
     * Exexutes insert statement for artifact payload
     * @param insertStatementStr
     * @param params
     * @throws RuntimeException
     */
    private void executeStoreArtifactPayload(String insertStatementStr, ArrayList<String> params) throws RuntimeException {

        try {
            logger.info("used schema=" + this.schema);
            logger.info("insert statement=" + insertStatementStr);

            dbLibService.writeData(insertStatementStr, params, this.schema);

            logger.info("finished to execute insert");

        } catch (SQLException e) {
            logger.error("Storing Artifact payload failed - " + insertStatementStr);
            throw new RuntimeException("Storing Artifact payload failed - " + insertStatementStr);
        }
    }

}
