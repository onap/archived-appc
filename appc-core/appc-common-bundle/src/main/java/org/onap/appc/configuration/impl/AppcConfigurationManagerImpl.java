/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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
 * 
 * ============LICENSE_END=========================================================
 */
package org.onap.appc.configuration.impl;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import org.onap.appc.configuration.AppcConfigurationManager;
import org.onap.appc.configuration.AppcConfigurationListener;
import org.onap.appc.configuration.Configuration;
import org.onap.appc.configuration.DefaultConfiguration;
import org.onap.appc.i18n.Msg;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

/**
 * Provides the implementation of <code>AppcConfigurationManager</code>
 * interface. 
 */
public final class AppcConfigurationManagerImpl implements AppcConfigurationManager {
    /**
     * The EELF Logger we will use
     */
    private static final EELFLogger logger = EELFManager.getInstance().getLogger(AppcConfigurationManagerImpl.class);
    
    /**
     * The default properties resource to be loaded
     */
    private static final String DEFAULT_PROPERTIES = "org/onap/appc/default.properties";

    /**
     * This is a constant array of special property names that will be copied
     * from the configuration back to the System properties object if they are
     * defined in the configuration AND they do not already exist in the System
     * properties object. These are intended as a convenience for setting the
     * AFT properties for the Discovery client where it may be difficult or
     * impossible to set VM arguments for the container.
     */
    private static final String[]                                                  specialProperties      = {
            "AFT_LATITUDE", "AFT_LONGITUDE", "AFT_ENVIRONMENT", "SCLD_PLATFORM" };
    /**
     * The Configuration object <code>Configuration</code> interface the system
     * configuration settings.
     */
    private DefaultConfiguration config = null;

    /**
     * the configuration provided by Configuration Admin 
     */
    private volatile Map<String, String> props = new HashMap<String, String>();

    private static final AtomicReference<SettableFuture<AppcConfigurationManager>> INSTANCE_FUTURE        = new AtomicReference<>();

    /**
     * The List of <code>AppcConfigurationListener</code> Services registered
     * with OSGI. Provided by the Blueprint reference-list. This list
     * dynamically changes as services register and unregister with OSGI
     * allowing the configuration manager to always have a current list of all
     * listeners without having to manage the list.
     */
    private List<AppcConfigurationListener> configurationListeners = null;

    /**
     * Construct the manager with an initial set of properties.
     * @param properties
     */
    public AppcConfigurationManagerImpl(Map<String, String> properties) {
        update(properties);

        INSTANCE_FUTURE.compareAndSet(null, SettableFuture.create());
        INSTANCE_FUTURE.get().set(this);
    }

    public static ListenableFuture<AppcConfigurationManager> instanceFuture() {
        INSTANCE_FUTURE.compareAndSet(null, SettableFuture.create());
        return INSTANCE_FUTURE.get();
    }

    @Override
    public void close() {
        SettableFuture<AppcConfigurationManager> future = INSTANCE_FUTURE.getAndSet(null);
        if (future != null) {
            future.setException(new RuntimeException("AppcConfiguration has been closed"));
        }
    }

    /**
     * Updates the current configuration properties. This method is registered
     * with the OSGI Configuration Admin Service to be call when the managed
     * configuration has been changed.
     */
    @Override
    public void update(Map<String, String> properties) {
        if (properties != null && !props.equals(properties)) {
            this.props = properties;
            if (config == null) {
                config = new DefaultConfiguration();
            }
            DateFormat format = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);
            Date now = new Date();
            logger.info("------------------------------------------------------------------------------");

            logger.info("Updating APPC Configuration");

            /*
             * Clear any existing properties
             */
            config.clear();
            logger.info(Msg.CONFIGURATION_CLEARED);

            /*
             * Load the defaults (if any are present)
             */
            InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(DEFAULT_PROPERTIES);
            if (in != null) {
                logger.info(Msg.LOADING_DEFAULTS, DEFAULT_PROPERTIES);
                try {
                    config.setProperties(in);
                } finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                        logger.error("Cannot close inputStream", e);
                    }
                }
                for (String key : config.getProperties().stringPropertyNames()) {
                    logger.info(Msg.PROPERTY_VALUE, key, config.getProperty(key));
                }
            } else {
                logger.info(Msg.NO_DEFAULTS_FOUND, DEFAULT_PROPERTIES);
            }

            /*
             * Apply Configuration Admin specified properties
             */
            logger.info(Msg.LOADING_APPLICATION_OVERRIDES);
            for (String key : props.keySet()) {
                logger.debug(Msg.PROPERTY_VALUE, key, props.get(key));
                config.setProperty(key, props.get(key));
            }

            /*
             * Merge in the System.properties to pick-up any command line
             * arguments (-Dkeyword=value)
             */
            logger.info(Msg.MERGING_SYSTEM_PROPERTIES);
            config.setProperties(System.getProperties());

            /*
             * As a convenience, copy the "specialProperties" that are not
             * defined in System.properties from the configuration back to the
             * system properties object.
             */
            for (String key : config.getProperties().stringPropertyNames()) {
                for (String specialProperty : specialProperties) {
                    if (key.equals(specialProperty) && !System.getProperties().containsKey(key)) {
                        System.setProperty(key, config.getProperty(key));
                        logger.info(Msg.SETTING_SPECIAL_PROPERTY, key, config.getProperty(key));
                    }
                }
            }

            /*
             * Initialize the resource manager by loading the requested bundles,
             * if any are defined. Resource bundles may be specified as a
             * comma-delimited list of names. These resource names are base
             * names of resource bundles, do not include the language or country
             * code, or the ".properties" extension. The actual loading of the
             * resource bundles is done lazily when requested the first time. If
             * the bundle does not exist, or cannot be loaded, it is ignored.
             */
            String resourcesList = config.getProperty(Configuration.PROPERTY_RESOURCE_BUNDLES,
                    Configuration.DEFAULT_RESOURCE_BUNDLES);
            String[] resources = resourcesList.split(",");
            for (String resource : resources) {
                logger.info(Msg.LOADING_RESOURCE_BUNDLE, resource.trim());
                EELFResourceManager.loadMessageBundle(resource.trim());
            }
            logger.info("Appc configuraion updated, notifying listeners");
            updateListeners();
        }
    }

    /**
     * Updates the registered <code>AppcConfigurationListener</code> Services
     */
    private void updateListeners() {
        if (configurationListeners != null && !configurationListeners.isEmpty()) {
            //notify all listeners asynchronously then wait for completion
            logger.info("Updating " + configurationListeners.size() + " listeners");
            List<CompletableFuture<Void>> cfs = new ArrayList<CompletableFuture<Void>>();
            
            for (AppcConfigurationListener listener : configurationListeners) {
                CompletableFuture<Void> listenerFuture = CompletableFuture.runAsync(() -> updateListener(listener));
                cfs.add(listenerFuture);
            }
            
            //group the futures and wait for completion
            CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(cfs.toArray(new CompletableFuture<?>[cfs.size()]));
            
            logger.info("All listeners notified waiting for processing to complete");
            try {
                combinedFuture.get();
            } catch (Exception e) {
                logger.warn("Exception waiting for AppcConfigurationListerners to update",e);
            }
            
            logger.info("All listeners update complete");
            
        } else {
            logger.info("No AppcConfigurationListeners registered to notify");
            

        }
    }

    /**
     * Updates a <code>AppcConfigurationListener</code> with the current configuration
     * @param listener
     * @return 
     * @return 
     */
    private void updateListener(AppcConfigurationListener listener) {
        logger.debug("updating listener " + listener.toString());
        
        try {
            listener.updateConfig(config);
        } catch (Exception e) {
            logger.warn("Exception attempting to notify listener of configuration change", e);
        }
    }

    /**
     * Set the list of <code>AppcConfigurationListener</code> Services
     */
    @Override
    public void setConfigurationListeners(List<AppcConfigurationListener> listeners) {
        this.configurationListeners = listeners;
        if (config != null) {
            updateListeners();
        }
    }

    /**
     * Notify that a new <code>AppcConfigurationListener</code> Service has been
     * registered. This method is used for the blueprint reference-listener to
     * be called when new listeners register. This allows the manager to send
     * the new listener the current configuration upon successful registration
     * otherwise it would have to wait until a configuration change.
     */
    @Override
    public void bindConfigManagerListener(AppcConfigurationListener listener) {
        logger.info("New AppcConfigurationListener registered, calling updateConfig on " + listener.toString());
        updateListener(listener);
    }
}
