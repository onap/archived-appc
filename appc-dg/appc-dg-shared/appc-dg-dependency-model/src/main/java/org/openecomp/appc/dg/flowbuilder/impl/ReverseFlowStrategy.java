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

package org.openecomp.appc.dg.flowbuilder.impl;

import java.util.*;

import org.openecomp.appc.dg.flowbuilder.exception.InvalidDependencyModel;
import org.openecomp.appc.domainmodel.Vnfc;


public class ReverseFlowStrategy extends AbstractFlowStrategy {

    @Override
    protected List<List<Vnfc>> orderDependencies() {
        ArrayList<List<Vnfc>> arrayList = new ArrayList<>();

        Queue<Vnfc> queue1 = new LinkedList();
        Set<Vnfc> queue2 = new LinkedHashSet<>();

        Set<Vnfc> uniqueElementSet = new HashSet<>();
        Set<Vnfc> duplicateElementSet = new HashSet<>();

        // identifying independent nodes in queue1
        for(int colIndex=0;colIndex<graph.getSize();colIndex++){
            Integer sum = 0;
            for(int rowIndex=0;rowIndex<graph.getSize();rowIndex++){
                sum+= graph.getDependencyMatrix()[rowIndex][colIndex];
            }
            if(sum==0){
                Vnfc vnfc = graph.getVertexList().get(colIndex);
                queue1.add(vnfc);
            }
        }
        if(queue1.isEmpty()){
            throw new InvalidDependencyModel("There seems to be no leaf node for Vnfc dependencies");
        }
        arrayList.add((List<Vnfc>)queue1);
        queue1 = new LinkedList<>(queue1);

        boolean flag = true;

        while(flag){
            // iterating over queue1 and for each node in it finding all dependent nodes and putting them on queue2
            while(!queue1.isEmpty()){
                Vnfc listItem = queue1.remove();
                Integer rowIndex = graph.getIndex(listItem);
                for(Integer index =0;index<graph.getSize();index++){
                    Integer value = graph.getDependencyMatrix()[rowIndex][index];
                    if(value ==1){
                        Vnfc vnfc = graph.getVertexList().get(index);
                        queue2.add(vnfc);
                    }
                }
            }
            for(Vnfc vnfc:queue2){
                if(!uniqueElementSet.add(vnfc)){
                    duplicateElementSet.add(vnfc);
                }
            }
            if(queue2.isEmpty()){
                flag= false; // empty queue2 indicates that all leaf nodes have been identified, i.e. stop the iteration
            }
            else{
                arrayList.add(new ArrayList<Vnfc>(queue2));
                if(arrayList.size()>graph.getSize()){
                    // dependency list cannot be larger than total number of nodes
                    // if it happens indicates cycle in the dependency
                    throw new InvalidDependencyModel("Cycle detected in the VNFC dependencies");
                }
                queue1.addAll(queue2);
                queue2 = new LinkedHashSet<>();
            }
        }
        // If any node depends on multiple nodes present in different execution sequence,
        // its execution should happen on the higher order, removing its presence on lower execution sequence
        if(!duplicateElementSet.isEmpty()){
            for(Vnfc vnfc:duplicateElementSet){
                boolean firstOccurrence= true;
                for(int i=0;i<arrayList.size();i++){
                    List<Vnfc> list = arrayList.get(i);
                    if(list.contains(vnfc)){
                        if(firstOccurrence){
                            firstOccurrence =false;
                            list.remove(vnfc);
                        }
                        else{
                            continue;
                        }
                    }

                }
            }
        }
        return  arrayList;
    }
}
