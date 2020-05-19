/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 Ericsson
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.onap.appc.cache.MetadataCache;
import org.onap.appc.cache.impl.MetadataCacheFactory;
import org.onap.appc.cache.impl.MetadataCacheImpl;
import org.onap.appc.dg.dependencymanager.DependencyManager;
import org.onap.appc.dg.objects.DependencyTypes;
import org.onap.appc.dg.objects.VnfcDependencyModel;
import org.onap.appc.metadata.objects.DependencyModelIdentifier;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import static org.junit.Assert.assertNotNull;

@RunWith(PowerMockRunner.class)
@PrepareForTest(MetadataCacheFactory.class)
public class DependencyManagerImplTest {

    private MetadataCacheFactory metadataCacheFactory = Mockito.mock(MetadataCacheFactory.class);
    private MetadataCache<DependencyModelIdentifier,VnfcDependencyModel> cache;

    @Before
    public void setup() {
        PowerMockito.mockStatic(MetadataCacheFactory.class);
        PowerMockito.when(MetadataCacheFactory.getInstance()).thenReturn(metadataCacheFactory);
        cache = (MetadataCacheImpl<DependencyModelIdentifier,VnfcDependencyModel>) Mockito.mock(MetadataCacheImpl.class);
        PowerMockito.when(metadataCacheFactory.getMetadataCache()).thenReturn(cache);
    }

    @Test
    public void testDependencyManager() throws Exception {
        DependencyManager dmImpl = DependencyModelFactory.createDependencyManager();
        DependencyModelIdentifier modelIdentifier = new DependencyModelIdentifier("VNF_TYPE", "CATALOG_VERSION");
        DependencyTypes dependencyType = DependencyTypes.findByString("resource");
        Mockito.when(cache.getObject(Mockito.any(DependencyModelIdentifier.class))).thenReturn(new VnfcDependencyModel(null));
        dmImpl.getVnfcDependencyModel(modelIdentifier, dependencyType);
        assertNotNull(dmImpl);
    }

}
