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
package org.onap.appc.configuration;

import java.util.List;
import java.util.Map;

/**
 * The configuration manager provides the configuration properties for other
 * APPC components.
 */
public interface AppcConfigurationManager {
    String NAME = "org.onap.appc.configuration.AppcConfigurationManager";

    /**
     * Notify the manager that a new <code>AppcConfigurationListener</code>
     * service was registered
     * 
     * @param listener
     */
    public void bindConfigManagerListener(AppcConfigurationListener listener);

    /**
     * close the manager
     */
     public void close();

    /**
     * Set the List of <code>AppcConfigurationListener</code>
     * 
     * @param listeners
     */
    public void setConfigurationListeners(List<AppcConfigurationListener> listeners);

    /**
     * Called by the OSGI Configuration Admin service when the managed
     * configuration has been updated
     * 
     * @param properties
     */
    public void update(Map<String, String> properties);
}
