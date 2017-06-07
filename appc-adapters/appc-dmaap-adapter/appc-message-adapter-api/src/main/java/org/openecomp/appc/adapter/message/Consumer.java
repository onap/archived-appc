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

package org.openecomp.appc.adapter.message;

import java.util.List;

public interface Consumer {

    /**
     * Gets a batch of messages from the topic. Defaults to 1000 messages with 15s wait for messages if empty.
     * 
     * @return A list of strings representing the messages pulled from the topic.
     */
    public List<String> fetch();

    /**
     * Gets a batch of messages from the topic.
     * 
     * @param waitMs
     *            The amount of time to wait in milliseconds if the topic is empty for data to be written. Should be no
     *            less than 15000ms to prevent too many requests
     * @param limit
     *            The amount of messages to fetch
     * @return A list of strings representing the messages pulled from the topic.
     */
    public List<String> fetch(int waitMs, int limit);

    /**
     * Updates the api credentials for making authenticated requests
     * 
     * @param apiKey
     *            The public key to authenticate with
     * @param apiSecret
     *            The secret key to authenticate with
     */
    public void updateCredentials(String apiKey, String apiSecret);

    /**
     * Creates a dmaap client using a https connection
     * 
     * @param yes
     *            True if https should be used, false otherwise
     */
    public void useHttps(boolean yes);

    /**
     * Closes the dmaap client https connection
     */
    void close();

}
