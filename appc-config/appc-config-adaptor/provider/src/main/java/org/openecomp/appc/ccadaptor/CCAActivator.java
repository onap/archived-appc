/*-
 * ============LICENSE_START=======================================================
 * ONAP : APP-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property.    All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.appc.ccadaptor;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.onap.ccsdk.sli.core.sli.ConfigurationException;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class CCAActivator implements BundleActivator
{

    private static final String CCA_PROP_FILE_VAR = "SDNC_CCA_PROPERTIES";
    private static final String SDNC_CONFIG_DIR_VAR = "SDNC_CONFIG_DIR";

    @SuppressWarnings("rawtypes")
    private ServiceRegistration registration = null;

    //private static final Logger log = LoggerFactory.getLogger(CCAActivator.class);
    private static final EELFLogger log = EELFManager.getInstance().getLogger(CCAActivator.class);

    @Override
    public void start(BundleContext ctx) throws Exception
    {
        // Read properties
        Properties props = new Properties();

        // Read properties from appc-config-adaptor.properties
        String propFileName = System.getenv(CCA_PROP_FILE_VAR);
        if (propFileName == null)
        {
            String propDir = System.getenv(SDNC_CONFIG_DIR_VAR);
            if (propDir == null)
                throw new ConfigurationException(
                    "Cannot find config file - " + CCA_PROP_FILE_VAR + " and " + SDNC_CONFIG_DIR_VAR + " unset");

            propFileName = propDir + "/appc-config-adaptor.properties";
            log.warn("Environment variable " + CCA_PROP_FILE_VAR + " unset - defaulting to " + propFileName);
        }

        File propFile = new File(propFileName);
        if (!propFile.exists())
            throw new ConfigurationException("Missing configuration properties file: " + propFile);

        try(InputStream in = new FileInputStream(propFile)) {
            props.load(in);
            log.info("Loaded properties: ");
        }
        catch (Exception e)
        {
            throw new ConfigurationException("Could not load properties file " + propFileName, e);
        }

        // Advertise adaptor
        ConfigComponentAdaptor adaptor = new ConfigComponentAdaptor(props);
        if (registration == null)
        {
            log.info("Registering service " + ConfigComponentAdaptor.class.getName());
            registration = ctx.registerService(ConfigComponentAdaptor.class.getName(), adaptor, null);
        }

    }

    @Override
    public void stop(BundleContext ctx) throws Exception
    {
        if (registration != null)
        {
            registration.unregister();
            registration = null;
        }
    }
}
