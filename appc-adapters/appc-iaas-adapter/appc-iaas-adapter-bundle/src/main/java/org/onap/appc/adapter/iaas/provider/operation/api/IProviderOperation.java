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

package org.onap.appc.adapter.iaas.provider.operation.api;

import org.onap.appc.adapter.iaas.impl.ProviderCache;
import org.onap.appc.exceptions.APPCException;
import com.att.cdp.zones.model.ModelObject;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import java.util.Map;

/**
 * @since September 26, 2016
 */
public interface IProviderOperation {

    /**
     * perform specific provider operation
     * 
     * @param params
     * @param context
     * @return Object represents Stack, Server Or Image
     */
    ModelObject doOperation(Map<String, String> params, SvcLogicContext context) throws APPCException;

    /**
     * sets a cache of providers that are predefined.
     * 
     * @param providerCache
     */
    void setProviderCache(Map<String /* provider name */, ProviderCache> providerCache);

    /**
     * should be initialized by user
     * 
     * @param defaultDomain
     */
    void setDefaultDomain(String defaultDomain);

    /**
     * should be initialized by user
     * 
     * @param defaultUser
     */
    void setDefaultUser(String defaultUser);

    /**
     * should be initialized by user
     * 
     * @param defaultPassword
     */
    void setDefaultPassword(String defaultPassword);
}
