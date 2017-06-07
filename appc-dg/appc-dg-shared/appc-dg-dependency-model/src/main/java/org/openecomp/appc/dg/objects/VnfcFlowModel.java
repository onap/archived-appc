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

package org.openecomp.appc.dg.objects;

import java.util.*;

import org.openecomp.appc.domainmodel.Vnfc;


public class VnfcFlowModel {
    private Map<Integer,List<Vnfc>> flowModelMap;

    private VnfcFlowModel(VnfcFlowModelBuilder builder){
        this.flowModelMap = builder.map;

    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Flow Model : ");
        Iterator<List<Vnfc>> iterator = getModelIterator();
        while(iterator.hasNext()){
            for(Vnfc vnfc:iterator.next()){
                stringBuilder.append(vnfc.toString()).append(", \n");
            }
        }

        return stringBuilder.toString();
    }

    public Iterator<List<Vnfc>> getModelIterator(){
        return flowModelMap.values().iterator();
    }

    public static class VnfcFlowModelBuilder{

        Map<Integer,List<Vnfc>> map;

        public VnfcFlowModelBuilder(){
            map = new HashMap<>();
        }

        public VnfcFlowModelBuilder addMetadata(Integer index,Vnfc vnfc){
            List<Vnfc> vnfcList = this.map.get(index);
            if(vnfcList == null){
                vnfcList = new LinkedList<>();
                map.put(index,vnfcList);
            }
            vnfcList.add(vnfc);
            return this;
        }

        public VnfcFlowModelBuilder addMetadata(Integer index,List<Vnfc> vnfcs){
            List<Vnfc> vnfcList = this.map.get(index);
            if(vnfcList == null){
                vnfcList = new LinkedList<>();
                map.put(index,vnfcList);
            }
            vnfcList.addAll(vnfcs);
            return this;
        }

        public VnfcFlowModel build(){
            return new VnfcFlowModel(this);
        }

    }


}
