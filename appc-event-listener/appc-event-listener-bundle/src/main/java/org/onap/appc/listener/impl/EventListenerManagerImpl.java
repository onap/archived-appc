package org.onap.appc.listener.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;

import org.onap.appc.configuration.AppcConfigurationManager;
import org.onap.appc.configuration.Configuration;
import org.onap.appc.listener.Controller;
import org.onap.appc.listener.EventListenerManager;
import org.onap.appc.listener.ListenerProperties;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class EventListenerManagerImpl implements EventListenerManager {

    AppcConfigurationManager configManager = null;
    
    /**
     * The logger to be used
     */
    private final EELFLogger LOG = EELFManager.getInstance().getLogger(EventListenerManagerImpl.class);
    
    /**
     * The current listener configuration
     */
    private Map<String,ListenerProperties> listenersConfig = null;
    
    /**
     * The reference to the actual implementation object that implements the services
     */
    private Controller adapter;
    
    public void initialize() {
        LOG.info("Starting EventListenerManager waiting for configuration");
    }
    
    public void close() {
        if(adapter != null) {
            //shut down current listeners
            boolean stopNow = true;
            LOG.info("Stopping DMaaP Listener. StopNow=" + stopNow);
            adapter.stop(stopNow);
            LOG.info("DMaaP Listener stopped successfully");
        }
    }

    @Override
    public void updateConfig(Configuration config) {
        LOG.info("Updating Configuration");
        if(config == null) {
            LOG.info("Empty configuration recieved");
        }else{
            Boolean updateListeners=false;
            Properties props = config.getProperties();
            
            Map<String,ListenerProperties> listeners = new HashMap<String,ListenerProperties>();

            // Configure event listener for the demo use case
            ListenerProperties demoProps = new ListenerProperties("appc.demo", props);
            // Only add the listener if properties are set
            if (!demoProps.getProperties().isEmpty()) {
                demoProps.setListenerClass(org.onap.appc.listener.demo.impl.ListenerImpl.class);
                listeners.put("appc.demo",demoProps);
            }


            ListenerProperties clLCMProps = new ListenerProperties("appc.LCM", props);
            // Only add the listener if properties are set
            if (!clLCMProps.getProperties().isEmpty()) {
                clLCMProps.setListenerClass(org.onap.appc.listener.LCM.impl.ListenerImpl.class);
                listeners.put("appc.LCM",clLCMProps);
            }


            // Configure the OAM properties
            String oamPropKeyPrefix = "appc.OAM";
            ListenerProperties oamProps  = new ListenerProperties(oamPropKeyPrefix, props);
            // Only add the listener if properties are set and enabled is true
            if (!oamProps.getProperties().isEmpty()) {
                oamProps.setListenerClass(org.onap.appc.listener.LCM.impl.ListenerImpl.class);
                listeners.put("appc.OAM",oamProps);
            }

            if(listenersConfig == null || !listenersConfig.equals(listeners)) {
                //Listener configuration has changed
                LOG.info("Starting new configuration for Event Listerners");
                if(adapter != null) {
                    //shut down current listeners
                    boolean stopNow = true;
                    LOG.info("Stopping DMaaP Listener. StopNow=" + stopNow);
                    adapter.stop(stopNow);
                    LOG.info("DMaaP Listener stopped successfully");
                }
                
                //start the listeners with the new configuration
                listenersConfig = listeners;
                adapter = new ControllerImpl(listenersConfig);
                adapter.start();
                LOG.info("New Event Listeners started successfully");
            }else {
                LOG.info("No Event Listener modifications detected");
            }
        }
    }
}