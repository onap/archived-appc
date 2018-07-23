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