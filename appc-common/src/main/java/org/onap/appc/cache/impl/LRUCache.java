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

package org.onap.appc.cache.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import org.onap.appc.cache.CacheStrategy;

/**
 * LRU cache implements CacheStategy<K, V>
 * @param <K> Key
 * @param <V> Value
 */
public class LRUCache<K,V> implements CacheStrategy<K,V> {

    private Map<K,V> map;

    LRUCache(final Integer capacity){
        map = new LinkedHashMap<K,V>(capacity, 0.75F, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest){
                return size() > capacity;
            }
        };
    }

    @Override
    public V getObject(K key) {
        return map.get(key);
    }

    @Override
    public void putObject(K key, V value) {
        map.put(key,value);
    }
}
