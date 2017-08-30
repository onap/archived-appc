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

package org.openecomp.appc.client.impl.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** client lib Registry
 */
class CoreRegistry<T>{
    private Map<String, T> registry =
            new ConcurrentHashMap<String, T>();

    final private EmptyRegistryCallback emptyRegistryCallback;


    CoreRegistry(EmptyRegistryCallback emptyRegistryCallback){
        this.emptyRegistryCallback = emptyRegistryCallback;
    }

    void register(String key, T obj) {
        registry.put(key, obj);
    }

    <T> T unregister(String key) {
        T item = (T) registry.remove(key);
        if(registry.isEmpty()) {
            emptyRegistryCallback.emptyCallback();
        }
        return item;
    }

    <T> T get(String key){
        return (T) registry.get(key);
    }

    synchronized boolean isExist(String key) {
        return registry.containsKey(key);
    }

    boolean isEmpty(){
        return registry.isEmpty();
    }

    public interface EmptyRegistryCallback{
        void emptyCallback();
    }
}
