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

package org.openecomp.appc.dg.netconf.impl;

import java.util.HashMap;

import org.openecomp.appc.adapter.netconf.ConnectionDetails;
import org.openecomp.appc.adapter.netconf.NetconfConnectionDetails;
import org.openecomp.appc.adapter.netconf.NetconfDataAccessService;
import org.openecomp.appc.adapter.netconf.exception.DataAccessException;
import org.openecomp.sdnc.sli.resource.dblib.DbLibService;

class DAOServiceMock implements NetconfDataAccessService {

    private String configFile;
    private ConnectionDetails connection;
    private HashMap<String, String> backupConf;

    @Override
    public void setSchema(String schema) {
    }

    @Override
    public void setDbLibService(DbLibService dbLibService) {
    }

    void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    public HashMap<String, String> getBackupConf() {
        return backupConf;
    }

    public void setConnection(ConnectionDetails connection) {
        this.connection = connection;
    }

    @Override
    public String retrieveConfigFileName(String xmlID) throws DataAccessException {
        if (!xmlID.equals("wrong")) {
            return configFile;
        } else {
            throw new DataAccessException();
        }
    }

    @Override
    public boolean retrieveConnectionDetails(String vnfType, ConnectionDetails connectionDetails) throws
                    DataAccessException {
        return false;
    }

    @Override
    public boolean retrieveNetconfConnectionDetails(String vnfType, NetconfConnectionDetails connectionDetails) throws
                    DataAccessException {
        if (vnfType.equals("VNF")) {
            connectionDetails.setHost(connection.getHost());
            connectionDetails.setPassword(connection.getPassword());
            connectionDetails.setPort(connection.getPort());
            connectionDetails.setUsername(connection.getUsername());

            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean logDeviceInteraction(String instanceId, String requestId, String creationDate, String logText) throws
                    DataAccessException {
        this.backupConf = new HashMap<>();
        backupConf.put("creationDate", creationDate);
        backupConf.put("logText", logText);
        return true;
    }

}
