/*-
 * ============LICENSE_START=======================================================
 * openECOMP : APP-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 						reserved.
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
 */

package org.openecomp.appc.provider;

import org.openecomp.appc.util.StringHelper;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import org.openecomp.sdnc.sli.SvcLogicException;
import org.openecomp.sdnc.sli.provider.SvcLogicService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import static com.att.eelf.configuration.Configuration.*;

import java.util.Properties;

public class AppcProviderClient {

    //private static final Logger LOG = LoggerFactory.getLogger(AppcProviderClient.class);
	private static EELFLogger LOG = EELFManager.getInstance().getApplicationLogger();
    private static EELFLogger metricsLogger = EELFManager.getInstance().getMetricsLogger();

    private SvcLogicService svcLogic = null;

    public AppcProviderClient() {
        BundleContext bctx = FrameworkUtil.getBundle(SvcLogicService.class).getBundleContext();

        // Get SvcLogicService reference
        ServiceReference sref = bctx.getServiceReference(SvcLogicService.NAME);
        if (sref != null) {
            svcLogic = (SvcLogicService) bctx.getService(sref);

        } else {
            LOG.warn("Cannot find service reference for " + SvcLogicService.NAME);

        }
    }

    public boolean hasGraph(String module, String rpc, String version, String mode) throws SvcLogicException {
        LOG.debug(String.format("Checking for graph. %s %s %s %s", module, rpc, version, mode));
        return (svcLogic.hasGraph(module, rpc, version, mode));
    }

    public Properties execute(String module, String rpc, String version, String mode, Properties parms)
        throws SvcLogicException {

        LOG.debug("Parameters passed to SLI: " + StringHelper.propertiesToString(parms));
        metricsLogger.info("Parameters passed to SLI: " + StringHelper.propertiesToString(parms));

        Properties respProps = svcLogic.execute(module, rpc, version, mode, parms);

        LOG.debug("Parameters returned by SLI: " + StringHelper.propertiesToString(respProps));
        metricsLogger.info("Parameters returned by SLI: " + StringHelper.propertiesToString(respProps));

        // No impact on flow. Not sure why it is here
        // if ("failure".equalsIgnoreCase(respProps.getProperty("SvcLogic.status"))) { return (respProps); }

        return respProps;
    }
}
