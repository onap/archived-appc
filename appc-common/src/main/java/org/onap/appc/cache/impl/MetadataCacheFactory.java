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

import org.onap.appc.cache.CacheStrategies;
import org.onap.appc.cache.MetadataCache;

/**
 * Metadata Cache Factory
 */
public class MetadataCacheFactory {

    private static class ReferenceHolder {
        private ReferenceHolder() {
            throw new IllegalAccessError("ReferenceHolder");
        }

        private static final MetadataCacheFactory FACTORY = new MetadataCacheFactory();
    }

    private MetadataCacheFactory() {
        // do nothing
    }

    public static MetadataCacheFactory getInstance(){
        return ReferenceHolder.FACTORY;
    }

    public MetadataCache getMetadataCache(){
        return new MetadataCacheImpl();
    }

    /**
     * Get MetadataCache
     * @param cacheStrategy the CacheStrategies to be used to build MetadataCacheImpl
     * @return a new instance of MetadataCacheImpl
     */
    public MetadataCache getMetadataCache(CacheStrategies cacheStrategy) {
        return new MetadataCacheImpl(cacheStrategy);
    }



}
