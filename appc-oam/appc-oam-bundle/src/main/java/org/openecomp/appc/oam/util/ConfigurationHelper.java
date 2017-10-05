/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.appc.oam.util;

import com.att.eelf.configuration.EELFLogger;
import org.apache.commons.lang3.ArrayUtils;
import org.openecomp.appc.Constants;
import org.openecomp.appc.configuration.Configuration;
import org.openecomp.appc.configuration.ConfigurationFactory;

import java.util.concurrent.TimeUnit;

/**
 * Utility class provides general configuration helps
 */
public class ConfigurationHelper {
    final static String PROP_KEY_APPC_NAME = Constants.PROPERTY_APPLICATION_NAME;
    final static String PROP_KEY_METRIC_STATE = "metric.enabled";
    private final String OAM_OPERATION_TIMEOUT_SECOND = "appc.OAM.api.timeout";
    /** Default operation timeout set to 1 minute */
    private final int DEFAULT_OAM_OPERATION_TIMEOUT = 60;

    private final EELFLogger logger;
    private Configuration configuration = ConfigurationFactory.getConfiguration();

    public ConfigurationHelper(EELFLogger eelfLogger) {
        logger = eelfLogger;
    }

    public String getAppcName() {
        return configuration.getProperty(PROP_KEY_APPC_NAME);
    }

    public boolean isMetricEnabled() {
        return configuration.getBooleanProperty(PROP_KEY_METRIC_STATE, false);
    }

    public Configuration getConfig() {
        return configuration;
    }

    /**
     * Read property value of a specified property key
     *
     * @param propertyKey string of the property key
     * @return String[] of the property values associated with the propertyKey
     */
    String[] readProperty(String propertyKey) {
        String propertyValue = configuration.getProperty(propertyKey);
        if (propertyValue == null) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Property[%s] has value (%s).", propertyKey, propertyValue));
        }

        if (propertyValue.contains(",")) {
            return propertyValue.split("\\s*,\\s*");
        }
        return new String[]{propertyValue};
    }





    /**
     * This method returns timeout in milliseconds.  The source is chosen in the following order:
     * The overrideTimeoutSeconds argument
     * or {@link #OAM_OPERATION_TIMEOUT_SECOND} found in the configuration file
     * or the {@link #DEFAULT_OAM_OPERATION_TIMEOUT}
     * @param overrideTimeoutSeconds  or null to us the other sources
     * @return timeout in milliseconds
     */
    public long getOAMOperationTimeoutValue(Integer overrideTimeoutSeconds) {
        return TimeUnit.SECONDS.toMillis(
                overrideTimeoutSeconds == null ?
            getConfig().getIntegerProperty(OAM_OPERATION_TIMEOUT_SECOND, DEFAULT_OAM_OPERATION_TIMEOUT)
            :
            overrideTimeoutSeconds
        );
    }
}
