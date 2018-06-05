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

package org.onap.appc.client.impl.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * client lib Registry
 */
class CoreRegistry<T> {

    private final EmptyRegistryCallback emptyRegistryCallback;
    private Map<String, T> registry = new ConcurrentHashMap<>();

    CoreRegistry(EmptyRegistryCallback emptyRegistryCallback) {
        this.emptyRegistryCallback = emptyRegistryCallback;
    }

    void register(String key, T obj) {
        registry.put(key, obj);
    }

    T unregister(String key) {
        T item = registry.remove(key);
        if (registry.isEmpty()) {
            emptyRegistryCallback.emptyCallback();
        }
        return item;
    }

    T get(String key) {
        return registry.get(key);
    }

    synchronized boolean isExist(String key) {
        return registry.containsKey(key);
    }

    boolean isEmpty() {
        return registry.isEmpty();
    }

    @FunctionalInterface
    public interface EmptyRegistryCallback {

        void emptyCallback();
    }
}
