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

import static org.mockito.Mockito.spy;
import org.junit.Assert;
import org.junit.Test;
import org.onap.appc.cache.CacheStrategies;
import org.powermock.reflect.Whitebox;

public class MetadataCacheImplTest {
    @Test
    public void testConstructor() throws Exception {
        // test without parameter
        MetadataCacheImpl impl = new MetadataCacheImpl<>();
        Assert.assertTrue("Should have initialized strategy",
                Whitebox.getInternalState(impl, "strategy") != null);

        // test with parameter
        impl = new MetadataCacheImpl<>(CacheStrategies.LRU);
        Assert.assertTrue("Should have initialized strategy",
                Whitebox.getInternalState(impl, "strategy") != null);

        impl = new MetadataCacheImpl<>(null);
        Assert.assertTrue("Should not initialized strategy",
                Whitebox.getInternalState(impl, "strategy") == null);
    }

    @Test
    public void testGetAndPutObject() throws Exception {
        MetadataCacheImpl impl = spy(new MetadataCacheImpl<>());

        String key = "testing key";
        Assert.assertTrue(impl.getObject(key) == null);

        String value = "testing value";
        impl.putObject(key, value);
        Assert.assertEquals(value, impl.getObject(key));
    }
}
