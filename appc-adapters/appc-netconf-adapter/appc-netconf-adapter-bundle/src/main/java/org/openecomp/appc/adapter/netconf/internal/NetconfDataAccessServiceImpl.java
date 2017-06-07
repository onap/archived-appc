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

package org.openecomp.appc.adapter.netconf.internal;

import javax.sql.rowset.CachedRowSet;

import org.openecomp.appc.adapter.netconf.ConnectionDetails;
import org.openecomp.appc.adapter.netconf.NetconfConnectionDetails;
import org.openecomp.appc.adapter.netconf.NetconfDataAccessService;
import org.openecomp.appc.adapter.netconf.exception.DataAccessException;
import org.openecomp.appc.adapter.netconf.util.Constants;
import org.openecomp.appc.exceptions.APPCException;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.openecomp.sdnc.sli.resource.dblib.DbLibService;

import java.sql.SQLException;
import java.util.ArrayList;


public class NetconfDataAccessServiceImpl implements NetconfDataAccessService {

    private static EELFLogger logger = EELFManager.getInstance().getLogger(NetconfDataAccessServiceImpl.class);

    public void setSchema(String schema) {
        this.schema = schema;
    }

    private String schema;

    public void setDbLibService(DbLibService service) {dbLibService = service;}

    private DbLibService dbLibService;

    @Override
    public String retrieveConfigFileName(String xmlID) throws DataAccessException {
        String fileContent = "";

        String queryString = "select " + Constants.FILE_CONTENT_TABLE_FIELD_NAME + " " +
                "from " + Constants.CONFIGFILES_TABLE_NAME + " " +
                "where " + Constants.FILE_NAME_TABLE_FIELD_NAME + " = ?";

        ArrayList<String> argList = new ArrayList<>();
        argList.add(xmlID);

        try {

            final CachedRowSet data = dbLibService.getData(queryString, argList, schema);
            if (data.first()) {
                fileContent = data.getString(Constants.FILE_CONTENT_TABLE_FIELD_NAME);
            }

        } catch (Throwable e) {
            logger.error("Error Accessing Database " + e);
            throw new DataAccessException(e);
        }

        return fileContent;
    }

    @Override
    public boolean retrieveConnectionDetails(String vnfType, ConnectionDetails connectionDetails) throws
                    DataAccessException {
        boolean recordFound = false;

        String queryString = "select " + Constants.USER_NAME_TABLE_FIELD_NAME + "," + Constants.PASSWORD_TABLE_FIELD_NAME + "," + Constants.PORT_NUMBER_TABLE_FIELD_NAME + " " +
                "from " + Constants.DEVICE_AUTHENTICATION_TABLE_NAME + " " +
                "where " + Constants.VNF_TYPE_TABLE_FIELD_NAME + " = ?";

        ArrayList<String> argList = new ArrayList<>();
        argList.add(vnfType);

        try {

            final CachedRowSet data = dbLibService.getData(queryString, argList, schema);
            if (data.first()) {
                connectionDetails.setUsername(data.getString(Constants.USER_NAME_TABLE_FIELD_NAME));
                connectionDetails.setPassword(data.getString(Constants.PASSWORD_TABLE_FIELD_NAME));
                connectionDetails.setPort(data.getInt(Constants.PORT_NUMBER_TABLE_FIELD_NAME));
                recordFound = true;
            }

        } catch (SQLException e) {
            logger.error("Error Accessing Database " + e);
            throw new DataAccessException(e);
        }

        return recordFound;
    }

    @Override
    public boolean retrieveNetconfConnectionDetails(String vnfType, NetconfConnectionDetails connectionDetails) throws
                    DataAccessException
    {
        ConnectionDetails connDetails = new ConnectionDetails();
        if(this.retrieveConnectionDetails(vnfType, connDetails))
        {
            connectionDetails.setHost(connDetails.getHost());
            connectionDetails.setPort(connDetails.getPort());
            connectionDetails.setUsername(connDetails.getUsername());
            connectionDetails.setPassword(connDetails.getPassword());
        }
        return true;
    }

    @Override
    public boolean logDeviceInteraction(String instanceId, String requestId, String creationDate, String logText) throws
                    DataAccessException {

        String queryString = "INSERT INTO "+ Constants.DEVICE_INTERFACE_LOG_TABLE_NAME+"("+
                Constants.SERVICE_INSTANCE_ID_FIELD_NAME+","+
                Constants.REQUEST_ID_FIELD_NAME+","+
                Constants.CREATION_DATE_FIELD_NAME+","+
                Constants.LOG_FIELD_NAME+") ";
        queryString += "values(?,?,?,?)";

        ArrayList<String> argList = new ArrayList<>();
        argList.add(instanceId);
        argList.add(requestId);
        argList.add(creationDate);
        argList.add(logText);

        try {

            dbLibService.writeData(queryString, argList, schema);

        } catch (SQLException e) {
            logger.error("Logging Device interaction failed - "+ queryString);
            throw new DataAccessException(e);
        }

        return true;
    }

}
