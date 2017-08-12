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

/**
 * Utility class provides general configuration helps
 */
public class ConfigurationHelper {
    final static String PROP_KEY_APPC_NAME = Constants.PROPERTY_APPLICATION_NAME;
    final static String PROP_KEY_METRIC_STATE = "metric.enabled";

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
     * Read property value of a specified proeprty key
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
}
