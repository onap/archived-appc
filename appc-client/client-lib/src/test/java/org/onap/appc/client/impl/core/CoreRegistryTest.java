/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright 2018 AT&T
 * =================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.client.impl.core;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.onap.appc.client.impl.core.CoreRegistry.EmptyRegistryCallback;

public class CoreRegistryTest {
    
    private boolean erCalledBack;
    private CoreRegistry<String> registry;
    
    @Before
    public void beforeTest() throws Exception {
        erCalledBack = false;
        registry = new CoreRegistry<>(
                new EmptyRegistryCallback() {

                    @Override
                    public void emptyCallback() {
                        erCalledBack = true;
                    }
                });
    }

    @Test
    public void testRegister() {
        registry.register("1", "a");
        assertEquals("a", registry.get("1"));
        registry.register("1", "b");
        assertEquals("b", registry.get("1"));
    }

    @Test
    public void testUnregister() {
        registry.register("3", "c");
        registry.unregister("3");
        assertFalse(registry.isExist("3"));
    }

    @Test
    public void testEmptyCallbackNotCalledOnNewRegistry() {
        assertFalse(erCalledBack);
    }

    @Test
    public void testEmptyCallbackAndIsEmpty() {
        registry.register("3", "c");
        registry.unregister("3");
        assertTrue(registry.isEmpty());
        assertTrue(erCalledBack);
    }
}
