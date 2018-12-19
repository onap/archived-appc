/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.oam.util;

import com.att.eelf.configuration.EELFLogger;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.AppcState;
import org.onap.appc.statemachine.impl.readers.AppcOamStates;
import org.osgi.framework.Bundle;

import java.util.Map;

/*
 * Utility class provides general state helps
 */
public class StateHelper {
    /** logger inherited from AppcOam */
    private final EELFLogger logger;
    private ConfigurationHelper configurationHelper;
    /** APP-C OAM current state in AppcOamStates value */
    private volatile AppcOamStates appcOamCurrentState;

    /**
     * Constructor
     *
     * @param eelfLogger of the logger
     */
    public StateHelper(EELFLogger eelfLogger, ConfigurationHelper cHelper) {
        logger = eelfLogger;
        configurationHelper = cHelper;
        appcOamCurrentState = AppcOamStates.Unknown;
    }

    /**
     * Set the passed in state to the class <b>appOamCurrentState</b>.
     *
     * @param appcOamStates of the new state
     */
    public void setState(AppcOamStates appcOamStates) {
        appcOamCurrentState = appcOamStates;
    }

    /**
     * Get the state
     * @return the class <b>appOamCurrentState</b>
     */
    public AppcOamStates getState() {
        return appcOamCurrentState;
    }

    /**
     * Validate if the passed in state is the same as the class <b>appOamCurrentState</b>.
     *
     * @param appcOamStates of the to be compared state
     * @return true if they are the same, otherwise false
     */
    boolean isSameState(AppcOamStates appcOamStates) {
        return appcOamCurrentState == appcOamStates;
    }

    /**
     * Get APP-C OAM current state
     *
     * <p>When appcOamCurrentState is null or unknown, reset it with APPC LCM bundle state.
     *
     * @return AppcOamStates of the current APP-C OAM state
     */
    public AppcOamStates getCurrentOamState() {
        if (appcOamCurrentState == null || appcOamCurrentState.equals(AppcOamStates.Unknown)) {
            appcOamCurrentState = getBundlesState();
        }
        return appcOamCurrentState;
    }

    /**
     * Use getCurrentOamState to get current OAM AppcOamStates and then convert to AppcState of Yang.
     *
     * @return AppcState of current OAM state
     */
    public AppcState getCurrentOamYangState() {
        try {
            AppcOamStates appcOamStates = getCurrentOamState();
            return AppcState.valueOf(appcOamStates.name());
        } catch (Exception ex) {
            logger.error(String.format("Unable to determine the current APP-C OAM state due to %s.", ex.getMessage()));
        }
        return AppcState.Unknown;
    }

    /**
     * Get APPC state from the state of the set of APPC LCM bundles.
     * <p>The state of each bundle will be checked and the lowest state will be uses as the returning AppcOamStates.
     * <p>The bundle state order are defined in OSGI bundle (@see org.osgi.framework.Bundle) class
     * as the int value assigned to each state as the following: <br>
     *   -  UNINSTALLED (1) <br>
     *   -  INSTALLED   (2) <br>
     *   -  RESOLVED    (4) <br>
     *   -  STARTING    (8) <br>
     *   -  STOPPING    (16) <br>
     *   -  ACTIVE      (32) <br>
     *
     * @return AppcOamStates
     */
    public AppcOamStates getBundlesState() {
        BundleHelper bundleHelper = getBundleHelper(logger, configurationHelper);
        Map<String, Bundle> lcmBundleMap = bundleHelper.getAppcLcmBundles();
        if (lcmBundleMap == null || lcmBundleMap.isEmpty()) {
            return AppcOamStates.Unknown;
        }

        // As we are picking up the lowest bundle state as general APP-C state, we will start with ACTIVE
        int currentState = Bundle.ACTIVE;
        for (Bundle bundle : lcmBundleMap.values()) {
            int bundleState = bundle.getState();
            logger.trace(String.format("getBundlesState: [%s] has state (%d)", bundle.getSymbolicName(), bundleState));
            if (bundleState < currentState) {
                currentState = bundleState;
            }
        }
        return AppcOamStates.getOamStateFromBundleState(currentState);
    }

    protected BundleHelper getBundleHelper(EELFLogger logger, ConfigurationHelper configurationHelper) {
        return new BundleHelper(logger, configurationHelper, this);
    }
}
