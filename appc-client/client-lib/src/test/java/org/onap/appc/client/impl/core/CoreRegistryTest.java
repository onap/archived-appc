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
