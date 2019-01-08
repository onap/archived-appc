/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications (C) 2019 Ericsson
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

package org.onap.appc.mdsal.operation;

import org.onap.appc.mdsal.impl.Constants;

/**
 * Creates request url path for config actions based on parameter like module name , container-name and sub modules if any.
 */

public class ConfigOperationRequestFormatter {
    /**
     *  Build a request url path for config actions
     * @param module - yang module name
     * @param containerName - yang container name
     * @param subModules - sub module /container  names as string in varargs ( String ) format
     * @return - resultant path in String format
     */
    public String buildPath(String module, String containerName , String... subModules ) {

        StringBuilder path = new StringBuilder( Constants.CONFIG_PATH + Constants.URL_BACKSLASH + module + ":" + containerName + Constants.URL_BACKSLASH);
        if(subModules.length >0){
            for(String subModule : subModules){
                path.append(subModule);
                path.append("/");
            }
        }
        return  path.toString();
    }
}
