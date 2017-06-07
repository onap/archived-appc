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

package org.openecomp.appc.messageadapter;

import org.openecomp.appc.domainmodel.lcm.ResponseContext;
import org.openecomp.appc.domainmodel.lcm.VNFOperation;

public interface MessageAdapter {
    /**
     * Initialize dmaapProducer client to post messages using configuration properties
     */
    void init();

    /**
     * Posts message to DMaaP. As DMaaP accepts only json messages this method first convert dmaapMessage to json format and post it to DMaaP.
     * @param asyncResponse response data that based on it a message will be send to DMaaP (the format of the message that will be sent to DMaaP based on the action and its YANG domainmodel).
     * @return True if message is postes successfully else False
     */
    boolean post(VNFOperation operation, String rpcName, ResponseContext asyncResponse);

}
