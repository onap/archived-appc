/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 Nokia
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
package org.onap.appc.dg.util.impl;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import org.onap.appc.i18n.Msg;
import org.onap.ccsdk.sli.adaptors.aai.AAIService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

public class AAIServiceFactory {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(AAIServiceFactory.class);

    private FrameworkUtilWrapper frameworkUtilWrapper = new FrameworkUtilWrapper();

    public AAIService getAAIService() {
        BundleContext bctx = frameworkUtilWrapper.getBundle(AAIService.class).getBundleContext();
        // Get AAIadapter reference
        ServiceReference sref = bctx.getServiceReference(AAIService.class.getName());
        if (sref != null) {
            logger.info("AAIService from bundlecontext");
            return (AAIService) bctx.getService(sref);

        } else {
            logger.info("AAIService error from bundlecontext");
            logger.error(EELFResourceManager.format(Msg.AAI_CONNECTION_FAILED, "AAIService"));
        }
        return null;
    }

    static class FrameworkUtilWrapper {

        Bundle getBundle(Class<?> clazz) {
            return FrameworkUtil.getBundle(clazz);
        }
    }
}