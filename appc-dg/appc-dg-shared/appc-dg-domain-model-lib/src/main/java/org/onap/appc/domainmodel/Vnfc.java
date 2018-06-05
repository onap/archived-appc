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

import java.util.LinkedList;
import java.util.List;


public class Vnfc {

    private String vnfcType;
    private String resilienceType;
    private boolean mandatory;
    private String vnfcName;
    private List<Vserver> vserverList;

    public Vnfc(){
        this.vserverList = new LinkedList<>();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder().append("Vnfc : vnfcType = ").append(vnfcType).append(", vnfcName = ").append(vnfcName).append(", resilienceType = " ).append(resilienceType).append(", mandatory = ").append(mandatory);
        for(Vserver vserver:vserverList){
            stringBuilder.append(vserver.toString()).append(", \n");
        }
        return stringBuilder.toString();
    }

    @Override
    public int hashCode(){
        final int prime = 31;
        int result = 1;
        result = result * prime + (this.vnfcType == null ? 0 :this.vnfcType.hashCode());
        result = result * prime + (this.resilienceType == null ? 0 :this.resilienceType.hashCode());
        result = result * prime + (this.vnfcName == null ? 0 :this.vnfcName.hashCode());
        result = result * prime + (Boolean.valueOf(this.mandatory).hashCode());
        return result;
    }
    @Override
    public boolean equals(Object object){
        if(object == null){
            return false;
        }
        if(!(object instanceof Vnfc)){
            return false;
        }
        Vnfc vnfc = (Vnfc)object;

        if(this.vnfcType == null){
            if(vnfc.vnfcType !=null)
                return false;
        }
        else if(!this.vnfcType.equals(vnfc.vnfcType))
            return false;

        if(this.resilienceType == null){
            if(vnfc.resilienceType !=null)
                return false;
        }
        else if(!this.resilienceType.equals(vnfc.resilienceType))
            return false;

        if(this.vnfcName == null){
            if(vnfc.vnfcName !=null)
                return false;
        }
        else if(!this.vnfcName.equals(vnfc.vnfcName))
            return false;
        if (this.mandatory != vnfc.mandatory)
            return false;
        return true;
    }

    public String getVnfcType() {
        return vnfcType;
    }

    public void setVnfcType(String vnfcType) {
        this.vnfcType = vnfcType;
    }

    public String getResilienceType() {
        return resilienceType;
    }

    public void setResilienceType(String resilienceType) {
        this.resilienceType = resilienceType;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public String getVnfcName() {
        return vnfcName;
    }

    public void setVnfcName(String vnfcName) {
        this.vnfcName = vnfcName;
    }

    public List<Vserver> getVserverList() {
        return vserverList;
    }

    public void setVserverList(List<Vserver> vserverList) {
        this.vserverList = vserverList;
    }

    public void addVservers(List<Vserver> vserverList){
        this.vserverList.addAll(vserverList);
    }

    public void addVserver(Vserver vm) {
        this.vserverList.add(vm);
    }


}
