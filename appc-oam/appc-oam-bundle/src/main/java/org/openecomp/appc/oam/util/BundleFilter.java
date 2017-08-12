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

import org.osgi.framework.Bundle;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;



/**
 *
 * Utility Class that splits a given bundleSet into two sets: bundleToStopSet and
 * bundleToNotStopSet
 *
 * The bundleToStopSet is defined as: all bundles which match at least one of
 * the stopRegexes but exceptRegexes none of the
 *
 * The bundleToNotStopSet is defined as all bundles which are not a member of
 * the bundleToStopSet
 *
 */
class BundleFilter {

    private final Map<String, Bundle> bundleToStopSet;
    private final Map<String, Bundle> bundleToNotStopSet;


    /**
     * BundleFilter a bundle filter
     * @param stopRegexes  - An array of regular expression used to pick out which bundles are candidates for stopping
     * @param exceptRegexes - An array of regular expression used to override which bundles are candidates for stopping
     * @param bundles - An array of the bundle to be split into {@link #getBundlesToStop()} {@link #getBundlesToNotStop()}
     */
    BundleFilter(String[] stopRegexes, String[] exceptRegexes, Bundle[] bundles) {

        Pattern[] stopPatterns = toPattern(stopRegexes);
        Pattern[] exceptPatterns = toPattern(exceptRegexes);

        Map<String, Bundle> bundleToStop = new HashMap<>();
        Map<String, Bundle>  bundleToNotStop = new HashMap<>();

        for (Bundle bundle : bundles) {
            String symbolicName = bundle.getSymbolicName();
            if (isMatch(symbolicName,stopPatterns) && !isMatch(symbolicName,exceptPatterns)) {
                bundleToStop.put(symbolicName, bundle);
            } else {
                bundleToNotStop.put(symbolicName, bundle);
            }
        }

        this.bundleToStopSet = Collections.unmodifiableMap(bundleToStop);
        this.bundleToNotStopSet = Collections.unmodifiableMap(bundleToNotStop);
    }

    /**
     * Determines if the value matches any of the regular expressions.
     *
     * @param value
     *            - the value that is to be matched
     * @param patterns
     *            - the array of {@link Pattern} to match the value against
     * @return boolean true if there is a match
     */
    private boolean isMatch(String value,Pattern[] patterns) {
        for (Pattern pattern : patterns) {
            if (pattern.matcher(value).matches()) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method converts an Array of regular expression in String form into a
     * Array of {@link Pattern}
     *
     * @param regex
     *            - A string array of regular expressions
     * @return Pattern Array of compiled regular expressions
     */
    private Pattern[] toPattern(String[] regex) {
        Pattern[] pattern = new Pattern[regex.length];
        for (int i = 0; i < regex.length; i++ ) {
            pattern[i] = Pattern.compile(regex[i]);
        }
        return pattern;
    }


    /**@return Map of bundles that are to be stopped  */
    Map<String, Bundle> getBundlesToStop(){
        return bundleToStopSet;
    }

    /**
     *
     * @return Map of bundles that are not to be stopped
     */
    Map<String, Bundle> getBundlesToNotStop() {
        return bundleToNotStopSet;
    }
}
