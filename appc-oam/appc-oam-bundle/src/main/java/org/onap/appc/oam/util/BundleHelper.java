/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications (C) 2018 Ericsson
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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.oam.util;

import com.att.eelf.configuration.EELFLogger;
import org.apache.commons.lang3.ArrayUtils;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.oam.AppcOam;
import org.onap.appc.oam.processor.BaseCommon;
import org.onap.appc.statemachine.impl.readers.AppcOamStates;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Utility class provides general bundle operational helps.
 */
public class BundleHelper {
    private final static String PROP_BUNDLE_TO_STOP = "appc.OAM.ToStop.properties";
    private final static String PROP_BUNDLES_TO_NOT_STOP = "appc.OAM.ToNotStop.properties";

    private final EELFLogger logger;
    private final StateHelper stateHelper;
    private final ConfigurationHelper configurationHelper;

    /**
     * Constructor
     *
     * @param eelfLogger of the logger
     * @param configurationHelperIn of ConfigurationHelper instance
     * @param stateHelperIn of StateHelper instance
     */
    public BundleHelper(EELFLogger eelfLogger,
                        ConfigurationHelper configurationHelperIn,
                        StateHelper stateHelperIn) {
        logger = eelfLogger;
        configurationHelper = configurationHelperIn;
        stateHelper = stateHelperIn;
    }

    /**
     * Handle bundle operations, such as stop or start bundle.
     *
     * @param rpc enum indicate if the operation is to stop, start or restart
     * @return boolean to indicate if the operation is successful (true) or failed (false)
     * @throws APPCException when error occurs
     */
    public boolean bundleOperations(AppcOam.RPC rpc,
                                    Map<String, Future<?>> threads,
                                    AsyncTaskHelper taskHelper,
                                    BaseCommon baseCommon)
        throws APPCException {
        long mStartTime = System.currentTimeMillis();
        logDebug(String.format("Entering OAM bundleOperations with rpc (%s).", rpc.name()));

        String action = rpc.getAppcOperation().toString();
        if (rpc != AppcOam.RPC.stop && rpc != AppcOam.RPC.start) {
            throw new APPCException("rpc(" + rpc + ") is not supported by bundleOperation.");
        }

        AppcOamStates originalState = stateHelper.getState();

        boolean isBundleOperationComplete = true;

        Map<String, Bundle> appcLcmBundles = getAppcLcmBundles();
        for (Map.Entry<String, Bundle> bundleEntry : appcLcmBundles.entrySet()) {
            String bundleName = bundleEntry.getKey();
            Bundle bundle = bundleEntry.getValue();

            logDebug("OAM launch thread for %s bundle %s", action, bundleName);
            if (rpc == AppcOam.RPC.start) {
                // Abort in the interruption case.
                // such as when a Stop request is receive while APPC is still trying to Start Up.
                if (!stateHelper.isSameState(originalState)) {
                    logger.warn("OAM %s bundle operation aborted since OAM state is no longer %s!",
                        originalState.name());
                    isBundleOperationComplete = false;
                    break;
                }
            }

            threads.put(bundleName,
                taskHelper.submitBaseSubCallable(new BundleTask(rpc, bundle,baseCommon)));
        }

        logDebug(String.format("Leaving OAM bundleOperations with rpc (%s) with complete(%s), elasped (%d) ms.",
            rpc.name(), Boolean.toString(isBundleOperationComplete), getElapseTimeMs(mStartTime)));

        return isBundleOperationComplete;
    }

    private long getElapseTimeMs(long mStartTime) {
        return System.currentTimeMillis() - mStartTime;
    }

    /**
     * Check if all BundleTasks are completed
     * @param bundleNameFutureMap with bundle name and BundleTask Future object
     * @return true if all are done, otherwise, false
     */
    public boolean isAllTaskDone(Map<String, Future<?>> bundleNameFutureMap) {
        boolean anyNotDone = bundleNameFutureMap.values().stream().anyMatch((f) -> !f.isDone());
        return !anyNotDone;
    }

    /**
     * Cancel BundleTasks which are not finished
     * @param bundleNameFutureMap with bundle name and BundleTask Future object
     */
    public void cancelUnfinished(Map<String, Future<?>> bundleNameFutureMap) {
        bundleNameFutureMap.values().stream().filter((f)
            -> !f.isDone()).forEach((f)
            -> f.cancel(true));
    }

    /**
     * Get number of failed BundleTasks
     * @param bundleNameFutureMap with bundle name and BundleTask Future object
     * @return number(long) of the failed BundleTasks
     */
    public long getFailedMetrics(Map<String, Future<?>> bundleNameFutureMap) {
        return bundleNameFutureMap.values().stream().map((f) -> {
            try {
                return f.get();
            } catch (Exception e) {
                // should not get here
                throw new RuntimeException(e);
            }
        }).filter((b) -> ((BundleTask)b).failException != null).count();
    }

    /**
     * Gets the list of Appc-bundles to be stopped/started
     *
     * @return Map of bundle symbolic name and bundle instance
     */
    Map<String, Bundle> getAppcLcmBundles() {
        logDebug("In getAppcLcmBundles");

        String[] bundlesToStop = readPropsFromPropListName(PROP_BUNDLE_TO_STOP);
        String[] regExBundleNotStop = readPropsFromPropListName(PROP_BUNDLES_TO_NOT_STOP);

        BundleFilter bundleList = getBundleFilter(bundlesToStop, regExBundleNotStop, getBundleList());

        logger.info(String.format("(%d) APPC bundles to Stop/Start: %s.", bundleList.getBundlesToStop().size(),
            bundleList.getBundlesToStop().toString()));

        logger.debug(String.format("(%d) APPC bundles that won't be Stopped/Started: %s.",
            bundleList.getBundlesToNotStop().size(), bundleList.getBundlesToNotStop().toString()));

        return bundleList.getBundlesToStop();
    }

    /**
     * Gets a list of all user desired bundles that should be stopped/Started as part of
     * OAM Stop and Start API
     *
     * @param propListKey String of the properties list property name
     * @return properties values of the related
     */
    String[] readPropsFromPropListName(String propListKey) {
        // get properties list by properties list name
        String[] propNames = configurationHelper.readProperty(propListKey);
        // go through each property to get the property values
        String[] propValue = ArrayUtils.EMPTY_STRING_ARRAY;
        if (propNames != null) {
            for (String aPropName : propNames) {
                propValue = ArrayUtils.addAll(propValue, configurationHelper.readProperty(aPropName));
            }
        }
        return propValue;
    }

    /**
     * Get all bundle list of APP-C
     * @return Array of Bundle
     */
    Bundle[] getBundleList() {
        BundleContext myBundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        if (myBundleContext != null) {
            return myBundleContext.getBundles();
        }
        return null;
    }

    /**
     * Genral debug log when debug logging level is enabled.
     * @param message of the log message format
     * @param args of the objects listed in the message format
     */
    private void logDebug(String message, Object... args) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format(message, args));
        }
    }

    protected BundleFilter getBundleFilter(String[] stopRegexes, String[] exceptRegexes, Bundle[] bundles) {
        return new BundleFilter(stopRegexes, exceptRegexes, bundles);
    }

    /**
     * Runnable to execute bundle operations: start or stop
     */
    class BundleTask implements Callable<BundleTask> {
        Exception failException;

        private AppcOam.RPC rpc;
        private Bundle bundle;
        private String bundleName;
        private String actionName;
        private final BaseCommon baseCommon;

        BundleTask(AppcOam.RPC rpcIn, Bundle bundleIn, BaseCommon baseCommon) {
            rpc = rpcIn;
            actionName = rpc.getAppcOperation().toString();
            bundle = bundleIn;
            bundleName = bundle.getSymbolicName();
            this.baseCommon = baseCommon;
        }

        @Override
        public BundleTask call() throws Exception {
            try {
                baseCommon.setInitialLogProperties();

                long bundleOperStartTime = System.currentTimeMillis();
                logDebug(String.format("OAM %s bundle %s ===>", actionName, bundleName));
                switch (rpc) {
                    case start:
                        bundle.start();
                        break;
                    case stop:
                        bundle.stop();
                        break;
                    default:
                        // should do nothing
                }
                logDebug(String.format("OAM %s bundle %s completed <=== elasped %d",
                    actionName, bundleName, getElapseTimeMs(bundleOperStartTime)));
            } catch (BundleException e) {
                logger.error(String.format("Exception encountered when OAM %s bundle %s ",
                    actionName, bundleName), e);
                failException = e;
            }
            finally {
                baseCommon.clearRequestLogProperties();
            }
            return this;
        }
    }
}
