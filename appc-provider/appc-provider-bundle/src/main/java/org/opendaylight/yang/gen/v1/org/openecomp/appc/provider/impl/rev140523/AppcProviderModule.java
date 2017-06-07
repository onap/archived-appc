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

package org.opendaylight.yang.gen.v1.org.openecomp.appc.provider.impl.rev140523;

import org.openecomp.appc.provider.AppcProvider;

/**
 * This was generated code. It was generated into the source tree because it has to be manually modified.
 * 
 */
public class AppcProviderModule extends
                org.opendaylight.yang.gen.v1.org.openecomp.appc.provider.impl.rev140523.AbstractAppcProviderModule {

    /**
     * @param identifier
     * @param dependencyResolver
     */
    @SuppressWarnings("javadoc")
    public AppcProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier,
                              org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    /**
     * @param identifier
     * @param dependencyResolver
     * @param oldModule
     * @param oldInstance
     */
    @SuppressWarnings("javadoc")
    public AppcProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier,
                              org.opendaylight.controller.config.api.DependencyResolver dependencyResolver,
                              org.opendaylight.yang.gen.v1.org.openecomp.appc.provider.impl.rev140523.AppcProviderModule oldModule,
                              java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    /**
     * @see org.opendaylight.yang.gen.v1.org.openecomp.appc.provider.impl.rev140523.AbstractAppcProviderModule#customValidation()
     */
    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    /**
     * This method is manually updated to actually invoke the provider implementation
     * 
     * @see org.opendaylight.yang.gen.v1.org.openecomp.appc.provider.impl.rev140523.AbstractAppcProviderModule#createInstance()
     */
    @Override
    public java.lang.AutoCloseable createInstance() {

        final AppcProvider provider =
            new AppcProvider(getDataBrokerDependency(), getNotificationServiceDependency(), getRpcRegistryDependency());
        return new AutoCloseable() {

            @Override
            public void close() throws Exception {
                provider.close();
            }
        };

    }

}
