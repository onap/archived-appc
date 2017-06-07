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

package org.openecomp.appc.cache.impl;

import org.openecomp.appc.cache.CacheStrategies;
import org.openecomp.appc.cache.CacheStrategy;
import org.openecomp.appc.cache.MetadataCache;

public class MetadataCacheFactory {

    private static class ReferenceHolder{
        private static final MetadataCacheFactory FACTORY = new MetadataCacheFactory();
    }
    private MetadataCacheFactory(){

    }

    public static MetadataCacheFactory getInstance(){
        return ReferenceHolder.FACTORY;
    }

    public MetadataCache getMetadataCache(){
        return new MetadataCacheImpl();
    }
    public MetadataCache getMetadataCache(CacheStrategies cacheStrategy){
        return new MetadataCacheImpl(cacheStrategy);
    }



}
