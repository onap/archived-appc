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

package org.onap.sdnc.dg.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DGXMLActivate {

    private static final Logger logger = LoggerFactory.getLogger(DGXMLActivate.class);
    private DGXMLActivate(){}

    public static void main(String[] args) {
        try {
            String activateFile;
            String propertyPath;

            if (args != null && args.length >= 2) {
                activateFile = args[0];
                propertyPath = args[1];
            } else {
                throw new DGXMLException(
                    "Sufficient inputs for DGXMLActivate are missing <activatefile> <dbPropertyfile>");
            }

            DGXMLActivator activator = new DGXMLActivator(propertyPath);
            activator.activateDg(activateFile);
        } catch (Exception e) {
            logger.error("Arguments missing", e);
        } finally {
            System.exit(1);
        }
    }
}
