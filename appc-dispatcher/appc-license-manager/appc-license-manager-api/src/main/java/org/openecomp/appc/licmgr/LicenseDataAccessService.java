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

package org.openecomp.appc.licmgr;

import java.util.Map;

import org.openecomp.appc.licmgr.exception.DataAccessException;
import org.openecomp.sdnc.sli.resource.dblib.DbLibService;


@SuppressWarnings("JavaDoc")
public interface LicenseDataAccessService {

    /**
     *
     * @param schema
     */
    void setSchema(String schema);

    /**
     *
     * @param vnfType resource name
     * @param vnfVersion resource version
     * @return map contains requested fields as Strings
     * @throws RuntimeException
     */
    Map<String,String> retrieveLicenseModelData(String vnfType, String vnfVersion, String... fields) throws DataAccessException;

    /**
     *
     * @param parameters parameters to store in artifacts table
     * @throws RuntimeException
     */
    void storeArtifactPayload(Map<String, String> parameters) throws RuntimeException;
}
