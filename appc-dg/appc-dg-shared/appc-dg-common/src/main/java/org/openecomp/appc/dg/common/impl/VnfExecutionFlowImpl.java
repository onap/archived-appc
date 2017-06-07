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

package org.openecomp.appc.dg.common.impl;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import org.openecomp.sdnc.sli.SvcLogicContext;

import java.util.*;

import org.openecomp.appc.dg.common.VnfExecutionFlow;
import org.openecomp.appc.dg.dependencymanager.DependencyManager;
import org.openecomp.appc.dg.dependencymanager.exception.DependencyModelNotFound;
import org.openecomp.appc.dg.dependencymanager.impl.DependencyModelFactory;
import org.openecomp.appc.dg.flowbuilder.FlowBuilder;
import org.openecomp.appc.dg.flowbuilder.exception.InvalidDependencyModel;
import org.openecomp.appc.dg.flowbuilder.impl.FlowBuilderFactory;
import org.openecomp.appc.dg.objects.*;
import org.openecomp.appc.domainmodel.Vnf;
import org.openecomp.appc.domainmodel.Vnfc;
import org.openecomp.appc.domainmodel.Vserver;
import org.openecomp.appc.i18n.Msg;
import org.openecomp.appc.metadata.objects.DependencyModelIdentifier;

public class VnfExecutionFlowImpl implements VnfExecutionFlow {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(VnfExecutionFlowImpl.class);

    public VnfExecutionFlowImpl(){

    }

    @Override
    public void getVnfExecutionFlowData(Map<String, String> params, SvcLogicContext context) {
        String dependencyType = params.get(Constants.DEPENDENCY_TYPE);
        String flowStrategy = params.get(Constants.FLOW_STRATEGY);
        DependencyModelIdentifier modelIdentifier = readDependencyModelIdentifier(params);
        VnfcDependencyModel dependencyModel = null;
        try {
            validateInput(dependencyType, flowStrategy, params);

            if (logger.isTraceEnabled()) {
                logger.trace("Input received from DG Node : dependencyType = " + dependencyType +
                        " , flowStrategy = " + flowStrategy +
                        ", DependencyModelIdentifier = " + modelIdentifier.toString());
            }

            DependencyManager dependencyManager = DependencyModelFactory.createDependencyManager();


            dependencyModel = dependencyManager.getVnfcDependencyModel(
                    modelIdentifier, DependencyTypes.findByString(dependencyType));
        } catch (DependencyModelNotFound e) {
            String msg = EELFResourceManager.format(Msg.DEPENDENCY_MODEL_NOT_FOUND,params.get(Constants.VNF_TYPE), e.getMessage());
            logger.error(msg);
            context.setAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE,msg);
            context.setAttribute("dependencyModelFound","false");
            return;
        } catch (InvalidDependencyModel e){
            String msg = EELFResourceManager.format(Msg.INVALID_DEPENDENCY_MODEL,params.get(Constants.VNF_TYPE), e.getMessage());
            logger.error(msg);
            context.setAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE,msg);
            throw e;
        }catch (RuntimeException e){
            logger.error(e.getMessage());
            context.setAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE,e.getMessage());
            throw e;
        }


        context.setAttribute("dependencyModelFound","true");
        if(logger.isDebugEnabled()){
            logger.debug("Dependency Model = " +dependencyModel);
        }
        logger.info("Building Inventory Model from DG context");
        InventoryModel inventoryModel = readInventoryModel(context);
        if(logger.isDebugEnabled()){
            logger.debug("Inventory Model = " +inventoryModel);
        }

        if(logger.isDebugEnabled()){
            logger.debug("Validating inventory model with dependency model");
        }
        try {
            validateInventoryModelWithDependencyModel(dependencyModel, inventoryModel);
        }catch (RuntimeException e){
            logger.error(e.getMessage());
            context.setAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE,e.getMessage());
            throw e;
        }
        logger.info("Creating flow builder");
        FlowBuilder flowBuilder = FlowBuilderFactory.getInstance().getFlowBuilder(
                FlowStrategies.findByString(flowStrategy));

        logger.info("Building Vnf flow model");
        VnfcFlowModel flowModel = null;
        try{
            flowModel = flowBuilder.buildFlowModel(dependencyModel,inventoryModel);
        }
        catch (InvalidDependencyModel e){
            String msg = EELFResourceManager.format(Msg.INVALID_DEPENDENCY_MODEL,params.get(Constants.VNF_TYPE), e.getMessage());
            logger.error(msg);
            context.setAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE,msg);
            throw e;
        }

        // remove VNFCs from the flow model where vserver list is empty
        reconcileFlowModel(flowModel);
        populateContext(flowModel,context);
        if(logger.isDebugEnabled()){
            logContext(context);
        }
        String msg = EELFResourceManager.format(Msg.SUCCESS_EVENT_MESSAGE, "GetVnfExecutionFlowData","VNF ID " + params.get(Constants.VNF_TYPE));
        context.setAttribute(org.openecomp.appc.Constants.ATTRIBUTE_SUCCESS_MESSAGE, msg);
    }

    private void validateInput(String dependencyType, String flowStrategy, Map<String, String> params) {
        DependencyTypes dependencyTypes = DependencyTypes.findByString(dependencyType);
        if(dependencyTypes == null){
            throw new RuntimeException("Dependency type from the input : " + dependencyType +" is invalid.");
        }
        FlowStrategies flowStrategies = FlowStrategies.findByString(flowStrategy);
        if(flowStrategies == null){
            throw new RuntimeException("Flow Strategy from the input : " + flowStrategy +" is invalid.");
        }
        String vnfType = params.get(Constants.VNF_TYPE);
        if(vnfType ==null || vnfType.length() ==0){
            throw new RuntimeException("Vnf Type is not passed in the input");
        }
        String vnfVersion = params.get(Constants.VNF_VERION);
        if(vnfVersion == null || vnfVersion.length() ==0){
            throw new RuntimeException("Vnf Version not found");
        }
    }

    private void logContext(SvcLogicContext context) {
        for(String key:context.getAttributeKeySet()){
            logger.debug(key + " = " + context.getAttribute(key) + "\n" );
        }
    }

    private void populateContext(VnfcFlowModel flowModel, SvcLogicContext context) {
        int flowIndex=0;
        Iterator<List<Vnfc>> iterator = flowModel.getModelIterator();
        while (iterator.hasNext()){
            for(Vnfc vnfc:iterator.next()){
                context.setAttribute("vnfcFlow["+flowIndex+"].vnfcName",vnfc.getVnfcName());
                context.setAttribute("vnfcFlow["+flowIndex+"].vnfcType",vnfc.getVnfcType());
                context.setAttribute("vnfcFlow["+flowIndex+"].resilienceType",vnfc.getResilienceType());
                context.setAttribute("vnfcFlow["+flowIndex+"].vmCount",Integer.toString(vnfc.getVserverList().size()));
                int vmIndex =0;
                for(Vserver vm :vnfc.getVserverList()){
                    context.setAttribute("vnfcFlow["+flowIndex+"].vm["+vmIndex+"].url",vm.getUrl());
                    vmIndex++;
                }
                flowIndex++;
            }
        }
        context.setAttribute("vnfcFlowCount",Integer.toString(flowIndex));
    }

    private InventoryModel readInventoryModel(SvcLogicContext context) {
        String vnfId = context.getAttribute("input.action-identifiers.vnf-id");
        String vnfType = context.getAttribute("vnf.type");
        String vnfVersion = context.getAttribute("vnf.version");
        String vnfcCountStr = context.getAttribute("vnf.vnfcCount");
        Integer vnfcCount = Integer.parseInt(vnfcCountStr);

        Vnf vnf = new Vnf(vnfId,vnfType,vnfVersion);

        for(Integer i=0;i<vnfcCount;i++){
            String vnfcName = context.getAttribute("vnf.vnfc["+ i+"].name");
            String vnfcType = context.getAttribute("vnf.vnfc["+ i+"].type");
            String vmCountStr = context.getAttribute("vnf.vnfc["+ i+"].vm_count");
            if(vnfcType ==null || vnfcType.length() ==0){
                throw new RuntimeException("Could not retrieve VNFC Type from DG Context for vnf.vnfc["+ i+"].type");
            }
            Integer vmCount = Integer.parseInt(vmCountStr);
            Vnfc vnfc = new Vnfc(vnfcType,null,vnfcName);
            for(Integer j=0;j<vmCount;j++){
                String vmURL = context.getAttribute("vnf.vnfc["+i+"].vm["+j+"].url");
                Vserver vm = new Vserver(vmURL);
                vnfc.addVm(vm);
            }
            vnf.addVnfc(vnfc);
        }
        return new InventoryModel(vnf);
    }

    private DependencyModelIdentifier readDependencyModelIdentifier(Map<String, String> params) {
        String vnfType = params.get(Constants.VNF_TYPE);
        String catalogVersion = params.get(Constants.VNF_VERION);
        return new DependencyModelIdentifier(vnfType,catalogVersion);
    }

    private void validateInventoryModelWithDependencyModel(VnfcDependencyModel dependencyModel, InventoryModel inventoryModel) {
        Set<String> dependencyModelVnfcSet = new HashSet<String>();
        Set<String> dependencyModelMandatoryVnfcSet = new HashSet<String>();
        Set<String> inventoryModelVnfcsSet = new HashSet<String>();

        for (Node<Vnfc> node : dependencyModel.getDependencies()) {
            dependencyModelVnfcSet.add(node.getChild().getVnfcType().toLowerCase());
            if (node.getChild().isMandatory()) {
                dependencyModelMandatoryVnfcSet.add(node.getChild().getVnfcType().toLowerCase());
            }
        }

        for (Vnfc vnfc : inventoryModel.getVnf().getVnfcs()) {
            inventoryModelVnfcsSet.add(vnfc.getVnfcType().toLowerCase());
        }

        // if dependency model and inventory model contains same set of VNFCs, validation succeed and hence return
        if (dependencyModelVnfcSet.equals(inventoryModelVnfcsSet)) {
            return;
        }

        if (inventoryModelVnfcsSet.size() >= dependencyModelVnfcSet.size()) {
            Set<String> difference = new HashSet<String>(inventoryModelVnfcsSet);
            difference.removeAll(dependencyModelVnfcSet);
            logger.error("Dependency model is missing following vnfc type(s): " + difference);
            throw new RuntimeException("Dependency model is missing following vnfc type(s): " + difference);
        } else {
            Set<String> difference = new HashSet<String>(dependencyModelVnfcSet);
            difference.removeAll(inventoryModelVnfcsSet);
            difference.retainAll(dependencyModelMandatoryVnfcSet);
            if (difference.size() > 0) {
                logger.error("Inventory model is missing following mandatory vnfc type(s): " + difference);
                throw new RuntimeException("Inventory model is missing following mandatory vnfc type(s): " + difference);
            }
        }
    }

    private void reconcileFlowModel(VnfcFlowModel flowModel) {
        Iterator<List<Vnfc>> flowIterator = flowModel.getModelIterator();
        while (flowIterator.hasNext()) {
            Iterator<Vnfc> vnfcIterator = flowIterator.next().iterator();
            while (vnfcIterator.hasNext()) {
                Vnfc vnfc = vnfcIterator.next();
                if (vnfc.getVserverList().size() == 0) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("No vservers present for Vnfc type: " + vnfc.getVnfcType() + ". Hence, removing it from the flow model.");
                    }
                    vnfcIterator.remove();
                }
            }
        }
    }
}
