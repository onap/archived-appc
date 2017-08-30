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

package org.openecomp.appc.client.lcm.api;

import org.openecomp.appc.client.lcm.exceptions.AppcClientException;

import java.util.Properties;

public interface AppcLifeCycleManagerServiceFactory {

    /**
     * Creates a new stateful LCM API
     * @param context application context parameters
     * @param properties configures the behaviour of the LCM
     * @return a new stateful LCM API
     * @throws AppcClientException in case of problem in instantiation
     */
    LifeCycleManagerStateful createLifeCycleManagerStateful(ApplicationContext context, Properties properties) throws AppcClientException;

    /**
     * performs a shutdown of LCM API.
     * in case of graceful, will try and execute the remaining requests, otherwise, will force the shutdown right away
     * @param isForceShutdown - boolean. If true to perform force shutdown, other to perform graceful shutdown.
     */
    void shutdownLifeCycleManager(boolean isForceShutdown);

}
