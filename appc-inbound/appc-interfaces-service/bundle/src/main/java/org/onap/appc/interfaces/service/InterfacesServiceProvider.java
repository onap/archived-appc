/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications Copyright (C) 2019 Ericsson
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

package org.onap.appc.interfaces.service;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.org.onap.appc.interfaces.service.rev170818.InterfacesServiceService;
import org.onap.appc.interfaces.service.InterfacesServiceProviderImpl;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class InterfacesServiceProvider{

    private static final EELFLogger log = EELFManager.getInstance().getLogger(InterfacesServiceProvider.class);

    private final DataBroker dataBroker;
    private final RpcProviderRegistry rpcProviderRegistry;
    private RpcRegistration <InterfacesServiceService> serviceRegistration;

    public InterfacesServiceProvider(final DataBroker dataBroker, RpcProviderRegistry rpcProviderRegistry) {
        this.dataBroker = dataBroker;
        this.rpcProviderRegistry = rpcProviderRegistry;
    }
    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        // initialize data broker
        this.serviceRegistration = this.rpcProviderRegistry.addRpcImplementation(InterfacesServiceService.class,
                new InterfacesServiceProviderImpl());
        log.info("DataCollectorProvider Session Initiated");
    }
    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        if(this.serviceRegistration != null){
            this.serviceRegistration.close();
        }
        log.info("DataCollectorProvider Closed");
    }
}
