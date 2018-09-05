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

/**
 * Interface class to listen for changes in APPC configuration managed by
 * <code>AppcConfigurationManager</code>.
 * 
 * <p>
 * Classes implementing this interface must register with OSGI as a
 * <code>AppcConfigurationListener</code> Service to be notified when the
 * configuration has been changed.
 * </p>
 */
public interface AppcConfigurationListener {

    /**
     * Called by <code>AppcConfigurationManager</code> when the APPC
     * configuration is updated.
     *
     * @param config
     *            the updated configuration
     */
    void updateConfig(Configuration config);
}