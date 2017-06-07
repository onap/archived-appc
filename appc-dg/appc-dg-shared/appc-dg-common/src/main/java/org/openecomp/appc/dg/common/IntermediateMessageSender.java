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

import org.openecomp.sdnc.sli.SvcLogicContext;
import org.openecomp.sdnc.sli.SvcLogicJavaPlugin;

import java.util.Map;

/**
 * This interface provides api for sending intermediate messages from DG to initiator
 */
public interface IntermediateMessageSender extends SvcLogicJavaPlugin{

    /**
     * DG plugin which sends intermediate messages generated from DG to the initiator
     * @param params expects 1. code, (mandatory)
     *                       2. message, (mandatory)
     *                       3. payload,
     *                       4. prefix
     * @param context expects 1. input.common-header.timestamp,
     *                2. input.common-header.api-ver,
     *                3. input.common-header.originator-id,
     *                4. input.common-header.request-id, (mandatory)
     *                5. input.common-header.sub-request-id,
     *                6. input.action
     */
    void sendMessage(Map<String, String> params, SvcLogicContext context);
}
