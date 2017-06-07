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

package org.openecomp.appc.dg.common.impl;

import org.openecomp.appc.configuration.Configuration;
import org.openecomp.appc.configuration.ConfigurationFactory;

public class ResolverFactory {
    private static final Configuration configuration = ConfigurationFactory.getConfiguration();
    private static class ReferenceHolder{
        private static final AbstractResolver VNFC_RESOLVER = new VNFCResolver(configuration.getIntegerProperty("org.openecomp.appc.workflow.resolver.refresh_interval", 300));
        private static final AbstractResolver VNF_RESOLVER = new VNFResolver(configuration.getIntegerProperty("org.openecomp.appc.workflow.resolver.refresh_interval", 300));
    }

    public static AbstractResolver createResolver(String resolverType){
        if(resolverType.equalsIgnoreCase("VNF")){
            return ReferenceHolder.VNF_RESOLVER;
        }else if(resolverType.equalsIgnoreCase("VNFC")){
            return ReferenceHolder.VNFC_RESOLVER;
        }else {
            return null;
        }
    }
}
