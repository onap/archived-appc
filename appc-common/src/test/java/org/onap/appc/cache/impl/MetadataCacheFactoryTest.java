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

import static org.mockito.Mockito.mock;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.appc.cache.CacheStrategies;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

@RunWith(PowerMockRunner.class)
@PrepareForTest({MetadataCacheFactory.class, MetadataCacheImpl.class})
public class MetadataCacheFactoryTest {
    @Test
    public void testConstructor() throws Exception {
        Whitebox.invokeConstructor(MetadataCacheFactory.class);
    }

    @Test
    public void testGetInstance() throws Exception {
        Assert.assertTrue("Should not return null", MetadataCacheFactory.getInstance() != null);
        Assert.assertEquals("Should always return the same object",
                MetadataCacheFactory.getInstance(), MetadataCacheFactory.getInstance());
    }

    @Test
    public void testGetMetadataCacheWithNoArgument() throws Exception {
        MetadataCacheImpl mockImpl = mock(MetadataCacheImpl.class);
        PowerMockito.whenNew(MetadataCacheImpl.class).withNoArguments().thenReturn(mockImpl);
        Assert.assertEquals(mockImpl, MetadataCacheFactory.getInstance().getMetadataCache());
    }

    @Test
    public void testGetMetadataCacheWithArgument() throws Exception {
        CacheStrategies cacheStrategies = CacheStrategies.LRU;
        MetadataCacheImpl mockImpl = mock(MetadataCacheImpl.class);
        PowerMockito.whenNew(MetadataCacheImpl.class).withArguments(cacheStrategies)
                .thenReturn(mockImpl);
        Assert.assertEquals(mockImpl,
                MetadataCacheFactory.getInstance().getMetadataCache(cacheStrategies));
    }

}
