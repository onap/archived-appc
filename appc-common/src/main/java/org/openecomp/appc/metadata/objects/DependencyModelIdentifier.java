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

package org.openecomp.appc.metadata.objects;


public class DependencyModelIdentifier {
    private String vnfType;
    private String catalogVersion;

    public DependencyModelIdentifier(String vnfType, String catalogVersion){
        this.vnfType = vnfType;
        this.catalogVersion = catalogVersion;
    }

    public int hashCode(){
        final int prime = 31;
        int result = 1;
        result = result * prime + (this.vnfType == null ? 0 :this.vnfType.hashCode());
        result = result * prime + (this.catalogVersion == null ? 0 :this.catalogVersion.hashCode());
        return result;
    }

    public boolean equals(Object obj){
        if(obj ==null)
            return false;
        if(!(obj instanceof DependencyModelIdentifier))
            return false;

        DependencyModelIdentifier modelIdentifier = (DependencyModelIdentifier)obj;
        if(this.vnfType == null){
            if(modelIdentifier.vnfType !=null)
                return false;
        }
        else if(!this.vnfType.equals(modelIdentifier.vnfType))
            return false;

        if(this.catalogVersion == null){
            if(modelIdentifier.catalogVersion !=null)
                return false;
        }
        else if(!this.catalogVersion.equals(modelIdentifier.catalogVersion))
            return false;

        return true;
    }

    @Override
    public String toString() {
        return "DependencyModelIdentifier : vnfType = "+vnfType + " , catalogVersion = " +catalogVersion;
    }

    public String getVnfType() {
        return vnfType;
    }

    public String getCatalogVersion() {
        return catalogVersion;
    }

}
