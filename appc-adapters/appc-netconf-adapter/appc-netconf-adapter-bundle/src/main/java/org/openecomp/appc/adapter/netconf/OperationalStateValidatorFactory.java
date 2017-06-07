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

package org.openecomp.appc.adapter.netconf;

import org.apache.commons.lang3.NotImplementedException;
import org.openecomp.appc.configuration.Configuration;
import org.openecomp.appc.configuration.ConfigurationFactory;

public class OperationalStateValidatorFactory {
    protected static final Configuration configuration = ConfigurationFactory.getConfiguration();

    public static OperationalStateValidator getOperationalStateValidator(String vnfType) {
        VnfType vnfTypeEnum = null;
        try {
            vnfTypeEnum = VnfType.getVnfType(vnfType);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Illegal value in vnfType. vnfType="+vnfType,e);
        }
        return getOperationalStateValidator(vnfTypeEnum);
    }

    public static OperationalStateValidator getOperationalStateValidator(VnfType vnfType) {
        switch (vnfType) {
            case VNF:
            case VNF_MOCK:
                return new VNFOperationalStateValidatorImpl();
            case MOCK:
                return new MockOperationalStateValidatorImpl();
            default:
                throw new NotImplementedException("missing implementaion for the given vnfType:" + vnfType.name());
        }
    }
}
