/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.metricservice;


import java.util.Map;

public interface MetricService {
    MetricRegistry registry(String name);
    MetricRegistry createRegistry(String name);
    void dispose();

    /**
     * This API will be used to get the Map of all the registered Registry for the Metric Service
     * @return Map<String,MetricRegistry> where String will be the name of the Metric Registry
     * and MetricRegistry will be the actual object for that Registry
     */
    Map<String,MetricRegistry> getAllRegistry();
}