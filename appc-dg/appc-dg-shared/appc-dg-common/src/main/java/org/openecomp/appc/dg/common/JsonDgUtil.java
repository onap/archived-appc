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

package org.openecomp.appc.dg.common;

import java.util.Map;

import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.sdnc.sli.SvcLogicContext;
import org.openecomp.sdnc.sli.SvcLogicJavaPlugin;


public interface JsonDgUtil extends SvcLogicJavaPlugin {
    void flatAndAddToContext(Map<String, String> params, SvcLogicContext ctx) throws APPCException;

    void generateOutputPayloadFromContext(Map<String, String> params, SvcLogicContext ctx) throws APPCException;

    /**
     * Creates filename and content in Json format.
     * @param params
     * @param ctx
     * @throws APPCException
     */
    void cvaasFileNameAndFileContentToContext(Map<String, String> params, SvcLogicContext ctx) throws APPCException;

    /**
     * Checks if a file is created.
     * @param params
     * @param ctx
     * @throws APPCException
     */
    void checkFileCreated(Map<String, String> params, SvcLogicContext ctx) throws APPCException;
}
