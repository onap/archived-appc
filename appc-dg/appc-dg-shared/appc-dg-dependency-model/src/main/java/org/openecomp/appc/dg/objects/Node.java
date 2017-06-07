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

import java.util.LinkedList;
import java.util.List;


public class Node<T> {
    T child;
    List<T> parents;

    @Override
    public int hashCode(){
        return child.hashCode();
    }

    @Override
    public boolean equals(Object object){
        if(object == null){
            return false;
        }
        if(!(object instanceof Node)){
            return false;
        }
        Node node = (Node)object;
        return this.child.equals(node.getChild());
    }

    public Node(T child){
        this.child = child;
        this.parents = new LinkedList<>();
    }

    public T getChild() {
        return child;
    }

    public List<T> getParents() {
        return parents;
    }

    public void addParent(T parent){
        this.parents.add(parent);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Node : child = " + child + " , parents = ");
        for(T parent:parents){
            stringBuilder.append(parent).append(",");
        }
        return stringBuilder.toString();
    }
}
