package org.onap.appc.listener.impl;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Properties;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.appc.configuration.Configuration;
import org.onap.appc.configuration.DefaultConfiguration;
import org.onap.appc.listener.Controller;
import org.powermock.reflect.Whitebox;

public class EventListenerManagerImplTest {

    @BeforeClass
    public static void setUp() throws Exception {
        Class<EventListenerManagerImpl> eventListenerManagerImplClass = EventListenerManagerImpl.class;
        Field adapterField = eventListenerManagerImplClass.getDeclaredField("adapter");
        adapterField.setAccessible(true);
    }

    @Test
    public void testInitialize() {
        EventListenerManagerImpl manager = Mockito.spy(new EventListenerManagerImpl());
        
        manager.initialize();
        Mockito.verify(manager).initialize();
    }

    @Test
    public void testClose() {
        EventListenerManagerImpl manager = Mockito.spy(new EventListenerManagerImpl());
        manager.close();
        
        Mockito.verify(manager).close();
    }

    @Test
    public void testUpdateConfig() {
        EventListenerManagerImpl manager = Mockito.spy(new EventListenerManagerImpl());
        
        DefaultConfiguration config = new DefaultConfiguration();
        config.clear();
        
        InputStream input = getClass().getResourceAsStream("/org/onap/appc/empty.properties");
        Properties props = new Properties();
        try {
            props.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        config.setProperties(props);
        
        manager.updateConfig(config );
        assertNotNull(Whitebox.getInternalState(manager,"adapter"));
        Controller currentAdapter = Whitebox.getInternalState(manager,"adapter");
        
        // updateConfig with same properties adapter should not be re-initialized
        manager.updateConfig(config );
        assertNotNull(Whitebox.getInternalState(manager,"adapter"));
        assertEquals(currentAdapter,Whitebox.getInternalState(manager,"adapter"));
        
        
        input = getClass().getResourceAsStream("/org/onap/appc/lcmDisabled.properties");
        Properties newProps = new Properties();
        
        try {
            newProps.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        config.clear();
        config.setProperties(newProps);
        
        // updateConfig with different properties adapter should be re-initialized
        manager.updateConfig(config );
        assertNotEquals(currentAdapter,Whitebox.getInternalState(manager,"adapter"));
    }

}
