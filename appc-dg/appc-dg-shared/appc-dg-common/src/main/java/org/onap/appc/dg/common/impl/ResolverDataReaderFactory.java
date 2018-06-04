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

package org.onap.appc.dg.common.impl;

import org.onap.appc.configuration.Configuration;
import org.onap.appc.configuration.ConfigurationFactory;

public class ResolverDataReaderFactory {

    private static final Configuration configuration = ConfigurationFactory.getConfiguration();

    private ResolverDataReaderFactory() {}

    public static AbstractResolverDataReader createResolverDataReader(String resolverType) {
        if ("VNF".equalsIgnoreCase(resolverType)) {
            return ReferenceHolder.VNF_RESOLVER_DATA_READER;
        } else if ("VNFC".equalsIgnoreCase(resolverType)) {
            return ReferenceHolder.VNFC_RESOLVER_DATA_READER;
        } else {
            return null;
        }
    }

    private static class ReferenceHolder {

        private static final AbstractResolverDataReader VNFC_RESOLVER_DATA_READER = new VNFCResolverDataReader();

        private static final AbstractResolverDataReader VNF_RESOLVER_DATA_READER = new VNFResolverDataReader();

        private ReferenceHolder() {}
    }
}
