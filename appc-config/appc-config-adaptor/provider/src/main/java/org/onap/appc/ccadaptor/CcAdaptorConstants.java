/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 Ericsson
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

package org.onap.appc.ccadaptor;

public class CcAdaptorConstants {

    public static final String CCA_PROP_FILE_VAR = "SDNC_CCA_PROPERTIES";
    public static final String APPC_CONFIG_DIR_VAR = "APPC_CONFIG_DIR";

    private CcAdaptorConstants() {}

    public static String getEnvironmentVariable(String env) {
        return System.getenv(env);
    }
}
