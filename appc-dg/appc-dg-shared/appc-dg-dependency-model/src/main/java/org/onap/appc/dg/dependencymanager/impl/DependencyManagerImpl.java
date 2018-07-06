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

package org.onap.appc.dg.dependencymanager.impl;

import org.onap.appc.dg.dependencymanager.DependencyManager;
import org.onap.appc.dg.dependencymanager.DependencyType;
import org.onap.appc.dg.dependencymanager.exception.DependencyModelNotFound;
import org.onap.appc.dg.flowbuilder.exception.InvalidDependencyModelException;
import org.onap.appc.dg.objects.DependencyTypes;
import org.onap.appc.dg.objects.VnfcDependencyModel;

import org.onap.appc.cache.MetadataCache;
import org.onap.appc.cache.impl.MetadataCacheFactory;
import org.onap.appc.metadata.objects.DependencyModelIdentifier;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class DependencyManagerImpl implements DependencyManager {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(DependencyManagerImpl.class);

    MetadataCache<DependencyModelIdentifier,VnfcDependencyModel> cache;

    DependencyManagerImpl(){
        cache = MetadataCacheFactory.getInstance().getMetadataCache();
    }

    public VnfcDependencyModel getVnfcDependencyModel(DependencyModelIdentifier modelIdentifier,DependencyTypes dependencyType) throws InvalidDependencyModelException, DependencyModelNotFound {
        if (logger.isTraceEnabled()) {
            logger.trace("Entering to getVnfcDependencyModel with DependencyModelIdentifier = "+ modelIdentifier
                    + " , DependencyTypes = " + dependencyType);
        }
        VnfcDependencyModel dependencyModel = cache.getObject(modelIdentifier);
        if(dependencyModel == null){
            logger.debug("Dependency model not found in cache, creating strategy for reading it");
            DependencyType strategy = getStrategy(dependencyType);
            // Throw exception if strategy could not be created because it is required
            // to retrieve the vnfc dependency model later
            if (strategy == null) {
                throw new NullPointerException("Strategy is null. Failed to retrieve Vnfc Dependency Model");
            }
            dependencyModel = strategy.getVnfcDependencyModel(modelIdentifier);
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Returning getVnfcDependencyModel with dependency model = "+ dependencyModel);
        }
        return dependencyModel;
    }

    private DependencyType getStrategy(DependencyTypes dependencyType) {
        switch (dependencyType){
            case RESOURCE:
                return new ResourceDependency();
        }
        return null;
    }
}
