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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Properties;

interface MessagingService {

    /**
     * initialize consumer/publisher
     * @param props
     */
    void init(Properties props) throws IOException, GeneralSecurityException, NoSuchFieldException, IllegalAccessException;

    /**
     * sends a string as is
     * @param partition
     * @param body
     */
    void send(String partition, String body) throws IOException;

    /**
     * retrieve messages from bus - timeout extracted from props or see impl
     * @return
     */
    List<String> fetch() throws IOException;

    /**
     * retrieve messages from bus - timeout extracted from props or see impl
     * @param limit
     * @return
     */
    List<String> fetch(int limit) throws IOException;

    void close();
}
