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

package org.onap.appc.listener;

import org.onap.appc.configuration.Configuration;
import org.onap.appc.configuration.ConfigurationFactory;
import org.onap.appc.listener.impl.ControllerImpl;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * This activator is used to initialize and terminate the dmaap listener controller and pool(s)
 * <p>
 * The DMaaP listener is responsible for listening to a topic on the Universal Event Bus and reading in messages that
 * conform to the DCAE message format for APPC. These messages will then be parsed and passed along to the APPC Provider
 * to take action on. The listener will also send messages out on DMaaP during critical phases. The messages sent out
 * will have a status of:
 * <ul>
 * <li><i>PENDING</i> - The listener has read the message off of DMaaP and has put it in the queue to be processed</li>
 * <li><i>ACTIVE</i> - The listener has begun actually processing the request and is waiting on the appc provider to
 * complete the request</li>
 * <li><i>SUCCESS</i> or <i>FAILURE</i> - The listener has gotten a response back from the appc provider. If it is a
 * FAILURE, a message should also be included</li>
 * </ul>
 * </p>
 * <p>
 * Activation of the bundle will provision 1 controller that in turn will provision 1 (or in the future more) listener
 * to interact with DMaaP. Each listener will have a queue of messages read off of DMaaP and a thread pool of workers to
 * process them. This worker is responsible for contacting appc provider to perform the action
 * </p>
 * <p>
 * When the bundle is deactivated, the stopNow() method is called and the thread pool is emptied and all remaining jobs
 * are orphaned. Alternatively stop() could be called which would allow all remaining jobs in the queue to complete at
 * the cost of longer run time.
 * </p>
 * 
 * @since Aug 30, 2015
 * @version $Id$
 */
public class AppcEventListenerActivator implements BundleActivator {

    /**
     * The bundle registration
     */
    private ServiceRegistration registration = null;

    /**
     * The configuration object
     */
    private Configuration configuration;

    /**
     * The reference to the actual implementation object that implements the services
     */
    private Controller adapter;

    /**
     * The logger to be used
     */
    private final EELFLogger LOG = EELFManager.getInstance().getLogger(AppcEventListenerActivator.class);

    /**
     * Called when this bundle is started so the Framework can perform the bundle-specific activities necessary to start
     * this bundle. This method can be used to register services or to allocate any resources that this bundle needs.
     * <p>
     * This method must complete and return to its caller in a timely manner.
     * </p>
     *
     * @param ctx
     *            The execution context of the bundle being started.
     * @throws java.lang.Exception
     *             If this method throws an exception, this bundle is marked as stopped and the Framework will remove
     *             this bundle's listeners, unregister all services registered by this bundle, and release all services
     *             used by this bundle.
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext ctx) throws Exception {
        LOG.info("Starting Bundle " + getName());

        Properties props = getProperties();

        Set<ListenerProperties> listeners = new HashSet<>();

        // Configure event listener for the demo use case
        ListenerProperties demoProps = new ListenerProperties("appc.demo", props);
        // Only add the listener if properties are set
        if (!demoProps.getProperties().isEmpty()) {
            demoProps.setListenerClass(org.onap.appc.listener.demo.impl.ListenerImpl.class);
            listeners.add(demoProps);
        }


        ListenerProperties clLCMProps = new ListenerProperties("appc.LCM", props);
        // Only add the listener if properties are set
        if (!clLCMProps.getProperties().isEmpty()) {
            clLCMProps.setListenerClass(org.onap.appc.listener.LCM.impl.ListenerImpl.class);
            listeners.add(clLCMProps);
        }


        // Configure the OAM properties
        String oamPropKeyPrefix = "appc.OAM";
        ListenerProperties oamProps  = new ListenerProperties(oamPropKeyPrefix, props);
        // Only add the listener if properties are set and enabled is true
        if (!oamProps.getProperties().isEmpty() && isAppcOamPropsListenerEnabled(oamProps)) {
            oamProps.setListenerClass(org.onap.appc.listener.LCM.impl.ListenerImpl.class);
            listeners.add(oamProps);
        } else {
            LOG.warn(String.format("The listener %s is disabled and will not be run", oamPropKeyPrefix));
        }

        adapter = new ControllerImpl(listeners);
        if (ctx != null && registration == null) {
            LOG.info("Registering service DMaaP Controller");
            registration = ctx.registerService(Controller.class, adapter, null);
        }
        adapter.start();

        LOG.info("DMaaP Listener started successfully");
    }

    /**
     * Called when this bundle is stopped so the Framework can perform the bundle-specific activities necessary to stop
     * the bundle. In general, this method should undo the work that the BundleActivator.start method started. There
     * should be no active threads that were started by this bundle when this bundle returns. A stopped bundle must not
     * call any Framework objects.
     * <p>
     * This method must complete and return to its caller in a timely manner.
     * </p>
     *
     * @param ctx
     *            The execution context of the bundle being stopped.
     * @throws java.lang.Exception
     *             If this method throws an exception, the bundle is still marked as stopped, and the Framework will
     *             remove the bundle's listeners, unregister all services registered by the bundle, and release all
     *             services used by the bundle. *
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext ctx) throws Exception {
        boolean stopNow = true;
        LOG.info("Stopping DMaaP Listener. StopNow=" + stopNow);
        adapter.stop(stopNow);
        if (registration != null) {
            registration.unregister();
            registration = null;
        }
        LOG.info("DMaaP Listener stopped successfully");
    }

    public String getName() {
        return "DMaaP Listener";
    }

    /**
     * Check if AppcOam props disable listener or not
     *
     * @param listenerProperties of ListenerProperties objext
     * @return false only if AppcOam disabled key is defined and equal to true. Otherwise, return true.
     */
    private boolean isAppcOamPropsListenerEnabled(ListenerProperties listenerProperties) {
        final Properties appcOamProperties = listenerProperties.getProperties();

        boolean result;
        if (appcOamProperties == null) {
            result = true;
        } else {
            result = !Boolean.parseBoolean(appcOamProperties.getProperty(
                    ListenerProperties.KEYS.DISABLED.getPropertySuffix()));
        }

        return result;
    }
    
    /**
     * Get properties from configuration
     */
    Properties getProperties() {
        configuration = ConfigurationFactory.getConfiguration();
        return configuration.getProperties();
    }
    
}
