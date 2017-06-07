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

package org.openecomp.appc.adapter.netconf;

import org.openecomp.appc.adapter.netconf.exception.DataAccessException;
import org.openecomp.sdnc.sli.resource.dblib.DbLibService;


@SuppressWarnings("JavaDoc")
public interface NetconfDataAccessService {

    /**
     *
     * @param schema
     */
    void setSchema(String schema);

    /**
     *
     * @param dbLibService
     */
    void setDbLibService(DbLibService dbLibService);

    /**
     *
     * @param xmlID
     * @return
     * @throws DataAccessException
     */
    String retrieveConfigFileName(String xmlID) throws DataAccessException;

    /**
     *
     * @param vnfType
     * @param connectionDetails
     * @return
     * @throws DataAccessException
     */
    boolean retrieveConnectionDetails(String vnfType, ConnectionDetails connectionDetails) throws DataAccessException;

    /**
     *
     * @param vnfType
     * @param connectionDetails
     * @return
     * @throws DataAccessException
     */
    boolean retrieveNetconfConnectionDetails(String vnfType, NetconfConnectionDetails connectionDetails) throws
                    DataAccessException;

    /**
     *
     * @param instanceId
     * @param requestId
     * @param creationDate
     * @param logText
     * @return
     * @throws DataAccessException
     */
    boolean logDeviceInteraction(String instanceId, String requestId, String creationDate, String logText) throws
                    DataAccessException;

}
