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

package org.openecomp.appc.client.impl.protocol;

import java.util.HashMap;
import java.util.Map;

public class ProtocolFactory {

    private static ProtocolFactory instance;
    private Map<ProtocolType,Protocol> protocols;

    /**
     * Singleton factory
     */
    private ProtocolFactory(){

        protocols = new HashMap<ProtocolType, Protocol>();
    }

    /**
     * get factory instance
     * @return factory instance
     */
    public static synchronized ProtocolFactory getInstance(){

        if (instance == null) {
            instance = new ProtocolFactory();
        }
        return instance;
    }

    /**
     * returns instantiated protocol object
     * @param type of protocol object
     * @return protocol object
     */
    public Protocol getProtocolObject(ProtocolType type) throws ProtocolException {

        Protocol protocol = protocols.get(type);
        synchronized (this) {
            if (protocol == null) {
                switch (type) {
                    case SYNC:
                        throw new ProtocolException("Protocol SYNC is not implemented");
                    case ASYNC:
                        protocol = new AsyncProtocolImpl();
                        protocols.put(type, protocol);
                        break;
                    default:
                        throw new ProtocolException("Protocol type not found");
                }
            }
        }
        return protocol;
    }
}
