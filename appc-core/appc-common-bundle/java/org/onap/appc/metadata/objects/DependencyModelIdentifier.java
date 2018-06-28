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

package org.onap.appc.metadata.objects;

/**
 * Object of identifier for dependency model. Currently uses VNF type and catalog version
 */
public class DependencyModelIdentifier {
    static final String TO_STRING_FORMAT =
            "DependencyModelIdentifier : vnfType = %s , catalogVersion = %s";
    static final int prime = 31;

    private String vnfType;
    private String catalogVersion;

    /**
     * Constructor
     * 
     * @param vnfType String of the VNF type
     * @param catalogVersion String of the catalog version
     */
    public DependencyModelIdentifier(String vnfType, String catalogVersion) {
        this.vnfType = vnfType;
        this.catalogVersion = catalogVersion;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = result * prime + (this.vnfType == null ? 0 : this.vnfType.hashCode());
        result = result * prime
                + (this.catalogVersion == null ? 0 : this.catalogVersion.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof DependencyModelIdentifier)) {
            return false;
        }

        DependencyModelIdentifier modelIdentifier = (DependencyModelIdentifier) obj;
        if (this.vnfType == null) {
            if (modelIdentifier.vnfType != null) {
                return false;
            }
        } else if (!this.vnfType.equals(modelIdentifier.vnfType)) {
            return false;
        }

        if (this.catalogVersion == null) {
            if (modelIdentifier.catalogVersion != null) {
                return false;
            }
        } else if (!this.catalogVersion.equals(modelIdentifier.catalogVersion)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format(TO_STRING_FORMAT, vnfType, catalogVersion);
    }

    public String getVnfType() {
        return vnfType;
    }

    public String getCatalogVersion() {
        return catalogVersion;
    }

}
