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
import java.util.List;

interface Consumer {

        /**
         * Gets a batch of messages from the topic. Defaults to 1000 messages with 15s wait for messages if empty.
         *
         * @return A list of strings representing the messages pulled from the topic.
         * @throws IOException
         */
        List<String> fetch() throws IOException;

        /**
         * Gets a batch of messages from the topic.
         *
         * @param limit The amount of messages to fetch
         * @return A list of strings representing the messages pulled from the topic.
         * @throws IOException
         */
        List<String> fetch(int limit) throws IOException;

        /**
        * Send dummy fetch request to register client to be able to fetch messages
        * @throws IOException
        */
        void registerForRead() throws IOException;

        void close();
}
