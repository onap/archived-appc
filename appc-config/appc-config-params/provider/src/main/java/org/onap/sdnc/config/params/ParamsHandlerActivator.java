/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications Copyright (c) 2019 IBM
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

package org.onap.sdnc.config.params;

import java.util.LinkedList;
import java.util.List;

import org.onap.sdnc.config.params.parser.PropertyDefinitionNode;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class ParamsHandlerActivator implements BundleActivator {
    
    private static final String STR_PROPERTY_DEFINITION_FAILED = "Failed while getting PropertyDefinitionNode";

    private List<ServiceRegistration> registrations = new LinkedList<>();


    private static final EELFLogger log =
            EELFManager.getInstance().getLogger(ParamsHandlerActivator.class);

    @Override
    public void start(BundleContext ctx) throws Exception {

        try {
            PropertyDefinitionNode propertyDefinitionNode = new PropertyDefinitionNode();
            log.info("Registering service " + propertyDefinitionNode.getClass().getName());
            registrations.add(ctx.registerService(propertyDefinitionNode.getClass().getName(),
                    propertyDefinitionNode, null));
            log.info("Registering service sccessful for  "
                    + propertyDefinitionNode.getClass().getName());
        } catch (Exception e) {
            log.error(STR_PROPERTY_DEFINITION_FAILED, e);
            
        }

    }

    @Override
    public void stop(BundleContext arg0) throws Exception {
        for (ServiceRegistration registration : registrations) {
            registration.unregister();
            registration = null;
        }

    }

}
