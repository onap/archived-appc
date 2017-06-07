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

package org.openecomp.appc.metricservice.impl;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.openecomp.appc.metricservice.MetricRegistry;
import org.openecomp.appc.metricservice.MetricService;


public class MetricServiceImpl implements MetricService {
   private Map<String,MetricRegistry> concurrentRegistryMap=new ConcurrentHashMap<>();
    @Override
    public MetricRegistry registry(String name) {
        return concurrentRegistryMap.get(name);
    }

    @Override
    public MetricRegistry createRegistry(String name) {
        if(concurrentRegistryMap.get(name)==null)
            concurrentRegistryMap.put(name,new MetricRegistryImpl(name));
        return concurrentRegistryMap.get(name);
    }

    @Override
    public Map<String,MetricRegistry> getAllRegistry(){
        return Collections.unmodifiableMap(concurrentRegistryMap);

    }

    @Override
    public void dispose() {
//TODO
    }
}
