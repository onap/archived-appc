package org.onap.appc.configuration.impl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.appc.configuration.AppcConfigurationListener;
import org.onap.appc.configuration.Configuration;

public class AppcConfigurationManagerImplTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testSetConfigurationListeners() {
        Map<String, String> props = new HashMap<String, String>();
        props.put("testProperty", "true");
        AppcConfigurationManagerImpl manager = new AppcConfigurationManagerImpl(props);
        
        assertNotNull(manager);
        
        SampleListener listener1 = Mockito.spy(new SampleListener("TestListener1",1000));
        SampleListener listener2 = Mockito.spy(new SampleListener("TestListener2",5));
        
        List<AppcConfigurationListener> listeners = new ArrayList<AppcConfigurationListener>();
        
        listeners.add(listener1);
        listeners.add(listener2);
        
        manager.setConfigurationListeners(listeners);

        Mockito.verify(listener1).updateConfig(Mockito.any(Configuration.class));
        Mockito.verify(listener2).updateConfig(Mockito.any(Configuration.class));
    }

    @Test
    public void testBindConfigManagerListener() {
        Map<String, String> props = new HashMap<String, String>();
        props.put("testProperty", "true");
        AppcConfigurationManagerImpl manager = new AppcConfigurationManagerImpl(props);
        
        assertNotNull(manager);
        
        SampleListener listener1 = Mockito.spy(new SampleListener("TestListener1",1000));
        SampleListener listener2 = Mockito.spy(new SampleListener("TestListener2",5));
        SampleListener listener3 = Mockito.spy(new SampleListener("TestListener3",500));
        
        List<AppcConfigurationListener> listeners = new ArrayList<AppcConfigurationListener>();
        
        listeners.add(listener1);
        listeners.add(listener2);
        listeners.add(listener3);
        
        manager.setConfigurationListeners(listeners);

        Mockito.verify(listener1).updateConfig(Mockito.any(Configuration.class));
        Mockito.verify(listener2).updateConfig(Mockito.any(Configuration.class));
        
        manager.bindConfigManagerListener(listener3);
        Mockito.verify(listener2).updateConfig(Mockito.any(Configuration.class));        
    }

    private class SampleListener implements AppcConfigurationListener{
        String name = null;
        Configuration myConfig = null;
        int waitTime = 1;
        
        SampleListener(String name, int delay){
            this.name = name;
            waitTime = delay;
        }
        @Override
        public void updateConfig(Configuration config) {
            System.out.println("updateConfig called on " + name);
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            myConfig = config;
            System.out.println("updateConfig finished on " + name);
        }
        
    }
}
