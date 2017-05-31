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

package org.openecomp.appc.executor.objects;

import java.lang.Object;
import java.util.HashMap;
import java.util.Map;


public class Params {
    public static final String paramDgNameSpace = "dg.status.message.param.";
    public static final String errorDgMessageParamName = "errorDgMessage";
    private Map<String, java.lang.Object> params;

    public Params() {
    }

    public Map<String, java.lang.Object> getParams() {
        return params;
    }

    public void setParams(Map<String, java.lang.Object> params) {
        this.params = params;
    }

    public Params addParam(String paramName, java.lang.Object paramValue) {
        params = params == null ? new HashMap<String, Object>() : params;
        params.put(paramName, paramValue);
        return this;
    }

    @Override
    public String toString() {
        return "Params{" +
                "params=" + params +
                '}';
    }
}
