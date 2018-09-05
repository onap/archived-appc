package org.onap.appc.listener;

import org.onap.appc.configuration.AppcConfigurationListener;

/**
 * Interface for Event listener manager extends
 * <code>AppcConfigurationListener</code> to listen for APPC configuration
 * changes
 * 
 *
 */
public interface EventListenerManager extends AppcConfigurationListener {

    /**
     * Perform initialization
     */
    public void initialize();

    /**
     * Perform shutdown
     */
    public void close();
}
