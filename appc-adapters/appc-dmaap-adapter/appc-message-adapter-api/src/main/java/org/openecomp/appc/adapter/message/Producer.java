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

public interface Producer {

    public boolean post(String partition, String data);

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
