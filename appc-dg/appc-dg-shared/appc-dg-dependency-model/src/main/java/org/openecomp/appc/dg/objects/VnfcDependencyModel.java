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

import java.util.Set;

import org.openecomp.appc.domainmodel.Vnfc;


public class VnfcDependencyModel {
    private Set<Node<Vnfc>> dependencies;

    public VnfcDependencyModel(Set<Node<Vnfc>> dependencies){
        this.dependencies = dependencies;
    }

    public Set<Node<Vnfc>> getDependencies() {
        return dependencies;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("dependencies = ");
        for(Node node:dependencies){
            stringBuilder.append(node.toString()).append(", ");
        }
        return super.toString();
    }
}
