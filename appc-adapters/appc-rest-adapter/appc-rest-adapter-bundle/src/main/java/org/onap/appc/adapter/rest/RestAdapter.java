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

package org.onap.appc.adapter.rest;

import java.util.Map;

import org.onap.appc.exceptions.APPCException;
import com.att.cdp.zones.model.Server;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;

/**
 * This interface defines the operations that the provider adapter exposes.
 * <p>
 * This interface defines static constant property values that can be used to configure the adapter. These constants are
 * prefixed with the name PROPERTY_ to indicate that they are configuration properties. These properties are read from
 * the configuration file for the adapter and are used to define the providers, identity service URLs, and other
 * information needed by the adapter to interface with an IaaS provider.
 * </p>
 */
public interface RestAdapter extends SvcLogicJavaPlugin {

     /**
     * The type of provider to be accessed to locate and operate on a virtual machine instance. This is used to load the
     * correct provider support through the CDP IaaS abstraction layer and can be OpenStackProvider, BareMetalProvider,
     * or any other supported provider type.
     */
    static final String PROPERTY_PROVIDER_TYPE = "org.onap.appc.provider.type";

    /**
     * The adapter maintains a cache of providers organized by the name of the provider, not its type. This is
     * equivalent to the system or installation name. All regions within the same installation are assumed to be the
     * same type.
     */
    static final String PROPERTY_PROVIDER_NAME = "org.onap.appc.provider.name";

    /**
     * The fully-qualified URL of the instance to be manipulated as it is known to the provider.
     */
    static final String PROPERTY_INSTANCE_URL = "org.onap.appc.instance.url";

    /**
     * The fully-qualified URL of the instance to be manipulated as it is known to the provider.
     */
    static final String PROPERTY_IDENTITY_URL = "org.onap.appc.identity.url";

    /**
     * Returns the symbolic name of the adapter
     * 
     * @return The adapter name
     */
    String getAdapterName();


    void commonGet(Map<String, String> params, SvcLogicContext ctx) ;
    
    void commonPost(Map<String, String> params, SvcLogicContext ctx) ;
    
    void commonPut(Map<String, String> params, SvcLogicContext ctx) ;
    
    void commonDelete(Map<String, String> params, SvcLogicContext ctx) ;

}
