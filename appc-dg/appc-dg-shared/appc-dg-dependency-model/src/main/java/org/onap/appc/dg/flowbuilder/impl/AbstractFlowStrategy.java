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

package org.onap.appc.dg.flowbuilder.impl;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.onap.appc.dg.flowbuilder.FlowStrategy;
import org.onap.appc.dg.flowbuilder.exception.InvalidDependencyModelException;
import org.onap.appc.dg.flowbuilder.helper.Graph;
import org.onap.appc.dg.objects.*;
import org.onap.appc.domainmodel.Vnfc;


public abstract class AbstractFlowStrategy implements FlowStrategy {

    private final EELFLogger logger = EELFManager.getInstance().getLogger(AbstractFlowStrategy.class);

    Graph<Vnfc> graph;

    public VnfcFlowModel buildFlowModel(VnfcDependencyModel dependencyModel, InventoryModel inventoryModel) throws InvalidDependencyModelException {
        if(logger.isTraceEnabled()){
            logger.trace("Entering into buildFlowModel with dependency model = " + dependencyModel
                    + "inventory model = " +inventoryModel);
        }

        if(dependencyModel == null
                || dependencyModel.getDependencies() ==null
                || dependencyModel.getDependencies().size() ==0){
            logger.debug("Dependency model not available, building flow model with sequence");
            throw new InvalidDependencyModelException("Dependency model either null or does not contain any dependency");
        }

        VnfcFlowModel flowModel = buildFlowModel(dependencyModel);
        if(logger.isDebugEnabled()){
            logger.debug("Flow Model without instance data: \n" + flowModel);
        }

        logger.info("Populating flow model with A&AI data");
        populateFlowModel(flowModel,inventoryModel);
        if(logger.isDebugEnabled()){
            logger.debug("Flow Model with instance data: \n" + flowModel);
        }

        return flowModel;
    }

    private void populateFlowModel(VnfcFlowModel flowModel, InventoryModel inventoryModel) {
        Iterator<List<Vnfc>> flowIterator;

        for(Vnfc vnfcFromInventory:inventoryModel.getVnf().getVnfcs()){
            flowIterator = flowModel.getModelIterator();
            String vnfcType = vnfcFromInventory.getVnfcType();
            while (flowIterator.hasNext()){
                for(Vnfc vnfcFromFlowModel:flowIterator.next() ){
                    if(vnfcType.equalsIgnoreCase(vnfcFromFlowModel.getVnfcType())){
                        vnfcFromFlowModel.setVnfcName(vnfcFromInventory.getVnfcName());
                        vnfcFromFlowModel.setVserverList(vnfcFromInventory.getVserverList());
                    }
                }
            }

        }

    }

    @SuppressWarnings("unchecked")
    private VnfcFlowModel buildFlowModel(VnfcDependencyModel dependencyModel) throws InvalidDependencyModelException {
        Set<Node<Vnfc>> dependencies = dependencyModel.getDependencies();
        graph = new Graph(dependencies.size());

        for(Node<Vnfc> node:dependencies){
            graph.addVertex(node.getChild());
        }

        for(Node node:dependencies){
            Vnfc child = (Vnfc)node.getChild();
            List<Vnfc> parents = node.getParents();
            for(Vnfc parent:parents){
                graph.addEdge(child,parent);
            }
        }
        List<List<Vnfc>> dependencyList = orderDependencies();

        VnfcFlowModel.VnfcFlowModelBuilder builder = new VnfcFlowModel.VnfcFlowModelBuilder();
        int count=0;
        int flowModelSize = 0;
        for(List<Vnfc> vnfcList:dependencyList){
            builder.addMetadata(count,vnfcList);
            flowModelSize += vnfcList.size();
            count++;
        }
        if(flowModelSize != dependencies.size()){
            throw new InvalidDependencyModelException("Cyclic dependency detected between the VNFC's");
        }

        return builder.build();
    }

    protected abstract List<List<Vnfc>> orderDependencies() throws InvalidDependencyModelException;
}
