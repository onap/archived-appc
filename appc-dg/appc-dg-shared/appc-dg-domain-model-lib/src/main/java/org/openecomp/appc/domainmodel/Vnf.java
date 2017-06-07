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

package org.openecomp.appc.domainmodel;

import java.util.LinkedList;
import java.util.List;

public class Vnf {
    private String vnfId;
    private String vnfType;
    private String vnfVersion;

    private List<Vnfc> vnfcs;

    public Vnf(String vnfId,String vnfType,String vnfVersion){
        this.vnfId = vnfId;
        this.vnfType = vnfType;
        this.vnfVersion = vnfVersion;
        this.vnfcs = new LinkedList<>();
    }

    public String getVnfVersion() {
        return vnfVersion;
    }

    public String getVnfId() {
        return vnfId;
    }

    public String getVnfType() {
        return vnfType;
    }

    public void addVnfc(Vnfc vnfc){
        this.vnfcs.add(vnfc);
    }

    public List<Vnfc> getVnfcs() {
        return vnfcs;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Vnf : vnfId = " + vnfId +" , vnfType = " + vnfType);
        for(Vnfc vnfc:vnfcs){
            stringBuilder.append(vnfc.toString()).append(",");
        }
        return stringBuilder.toString();
    }
}
