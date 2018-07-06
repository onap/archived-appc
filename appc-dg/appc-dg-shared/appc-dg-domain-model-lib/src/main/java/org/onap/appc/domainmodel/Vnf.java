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

package org.onap.appc.domainmodel;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Vnf {

    private String vnfId;
    private String vnfType;
    private String vnfVersion;
    private String identityUrl;
    private  List <Vserver> vservers;

    public Vnf(){
        vservers = new LinkedList<>();
    }

    public String getVnfId() {
        return vnfId;
    }

    public void setVnfId(String vnfId) {
        this.vnfId = vnfId;
    }

    public String getVnfType() {
        return vnfType;
    }

    public void setVnfType(String vnfType) {
        this.vnfType = vnfType;
    }

    public String getVnfVersion() {
        return vnfVersion;
    }

    public void setVnfVersion(String vnfVersion) {
        this.vnfVersion = vnfVersion;
    }

    public List<Vserver> getVservers() {
        return vservers;
    }

    public void setVservers(List<Vserver> vservers) {
        this.vservers = vservers;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder().append("Vnf : vnfId = ").append(vnfId ).append(" , vnfType = ").append( vnfType);
        for(Vserver vserver:vservers){
            stringBuilder.append(vserver.toString()).append(",");
        }
        return stringBuilder.toString();
    }

    public void addVserver(Vserver vserver) {
        this.vservers.add(vserver);
    }

    public List<Vnfc> getVnfcs(){
        Set<Vnfc> vnfcs = new HashSet<>();
        for(Vserver vserver:vservers){
            if(vserver.getVnfc() != null)
                vnfcs.add(vserver.getVnfc());
        }
        return new LinkedList<>(vnfcs);
    }

    public String getIdentityUrl() {
		return identityUrl;
    }

    public void setIdentityUrl(String identityUrl) {
		this.identityUrl = identityUrl;
    }
}
