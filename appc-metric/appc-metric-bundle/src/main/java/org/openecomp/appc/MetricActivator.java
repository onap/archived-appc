/*-
 * ============LICENSE_START=======================================================
 * APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Amdocs
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
 * ============LICENSE_END=========================================================
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */

package org.openecomp.appc;

import org.openecomp.appc.metricservice.MetricService;
import org.openecomp.appc.metricservice.impl.MetricServiceImpl;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;


public class MetricActivator implements BundleActivator {

    private ServiceRegistration registration = null;

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(MetricActivator.class);

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        logger.debug("Starting Metric service " );
        MetricService impl = new MetricServiceImpl();
        String regName = MetricService.class.getName();
        logger.debug("Registering Metric service " + regName);
        registration = bundleContext.registerService(regName, impl, null);
        logger.debug("Registered Metric service " + regName);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {

    }
}
