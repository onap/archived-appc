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

package org.onap.appc.dg.common.impl;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.onap.appc.dg.common.VnfExecutionFlow;
import org.onap.appc.dg.dependencymanager.DependencyManager;
import org.onap.appc.dg.dependencymanager.exception.DependencyModelNotFound;
import org.onap.appc.dg.dependencymanager.impl.DependencyModelFactory;
import org.onap.appc.dg.flowbuilder.FlowBuilder;
import org.onap.appc.dg.flowbuilder.exception.InvalidDependencyModelException;
import org.onap.appc.dg.flowbuilder.impl.FlowBuilderFactory;
import org.onap.appc.dg.objects.DependencyTypes;
import org.onap.appc.dg.objects.FlowStrategies;
import org.onap.appc.dg.objects.InventoryModel;
import org.onap.appc.dg.objects.Node;
import org.onap.appc.dg.objects.VnfcDependencyModel;
import org.onap.appc.dg.objects.VnfcFlowModel;
import org.onap.appc.domainmodel.Vnf;
import org.onap.appc.domainmodel.Vnfc;
import org.onap.appc.domainmodel.Vserver;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.i18n.Msg;
import org.onap.appc.metadata.objects.DependencyModelIdentifier;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

public class VnfExecutionFlowImpl implements VnfExecutionFlow {

    private final EELFLogger logger = EELFManager.getInstance().getLogger(VnfExecutionFlowImpl.class);
    private static final String VNFC_FLOW = "vnfcFlow[";
    private static final String VNF_VNFC = "vnf.vnfc[";

    /**
     * Constructor <p>Used through blueprint
     */
    public VnfExecutionFlowImpl() {
        // do nothing
    }

    @Override
    public void getVnfExecutionFlowData(Map<String, String> params, SvcLogicContext context) {
        String dependencyType = params.get(Constants.DEPENDENCY_TYPE);
        String flowStrategy = params.get(Constants.FLOW_STRATEGY);
        DependencyModelIdentifier modelIdentifier = readDependencyModelIdentifier(params);
        VnfcDependencyModel dependencyModel;
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
            String msg = EELFResourceManager
                .format(Msg.DEPENDENCY_MODEL_NOT_FOUND, params.get(Constants.VNF_TYPE));
            logger.error(msg, e);
            context.setAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE, msg);
            context.setAttribute("dependencyModelFound", "false");
            return;
        } catch (InvalidDependencyModelException e) {
            String msg = EELFResourceManager
                .format(Msg.INVALID_DEPENDENCY_MODEL, params.get(Constants.VNF_TYPE));
            logger.error(msg, e);
            context.setAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE, msg);
            throw new VnfExecutionInternalException(e);
        } catch (APPCException e) {
            logger.error(e.getMessage());
            context.setAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE, e.getMessage());
            throw new VnfExecutionInternalException(e);
        } catch (RuntimeException e) {
            logger.error(e.getMessage());
            context.setAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE, e.getMessage());
            throw e;
        }

        context.setAttribute("dependencyModelFound", "true");
        if (logger.isDebugEnabled()) {
            logger.debug("Dependency Model = " + dependencyModel);
        }
        logger.info("Building Inventory Model from DG context");
        InventoryModel inventoryModel;
        try {
            inventoryModel = readInventoryModel(context);
        } catch (APPCException e) {
            logger.error(e.getMessage());
            context.setAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE, e.getMessage());
            throw new VnfExecutionInternalException(e);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Inventory Model = " + inventoryModel);
            logger.debug("Validating inventory model with dependency model");
        }
        try {
            validateInventoryModelWithDependencyModel(dependencyModel, inventoryModel);
        } catch (APPCException e) {
            logger.error(e.getMessage());
            context.setAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE, e.getMessage());
            throw new VnfExecutionInternalException(e);
        }
        logger.info("Creating flow builder");
        FlowBuilder flowBuilder = FlowBuilderFactory.getInstance().getFlowBuilder(
            FlowStrategies.findByString(flowStrategy));

        logger.info("Building Vnf flow model");
        VnfcFlowModel flowModel;
        try {
            flowModel = flowBuilder.buildFlowModel(dependencyModel, inventoryModel);
        } catch (InvalidDependencyModelException e) {
            String msg = EELFResourceManager
                .format(Msg.INVALID_DEPENDENCY_MODEL, params.get(Constants.VNF_TYPE), e.getMessage());
            logger.error(msg);
            context.setAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE, msg);
            throw new VnfExecutionInternalException(e);
        }

        // remove VNFCs from the flow model where vserver list is empty
        reconcileFlowModel(flowModel);
        populateContext(flowModel, context);
        if (logger.isDebugEnabled()) {
            logContext(context);
        }
        String msg = EELFResourceManager
            .format(Msg.SUCCESS_EVENT_MESSAGE, "GetVnfExecutionFlowData", "VNF ID " + params.get(Constants.VNF_TYPE));
        context.setAttribute(org.onap.appc.Constants.ATTRIBUTE_SUCCESS_MESSAGE, msg);
    }

    private void validateInput(String dependencyType, String flowStrategy, Map<String, String> params)
        throws APPCException {
        DependencyTypes dependencyTypes = DependencyTypes.findByString(dependencyType);
        if (dependencyTypes == null) {
            throw new APPCException("Dependency type from the input : " + dependencyType + " is invalid.");
        }
        FlowStrategies flowStrategies = FlowStrategies.findByString(flowStrategy);
        if (flowStrategies == null) {
            throw new APPCException("Flow Strategy from the input : " + flowStrategy + " is invalid.");
        }
        String vnfType = params.get(Constants.VNF_TYPE);
        if (nullOrEmpty(vnfType)) {
            throw new APPCException("Vnf Type is not passed in the input");
        }
        String vnfVersion = params.get(Constants.VNF_VERION);
        if (nullOrEmpty(vnfVersion)) {
            throw new APPCException("Vnf Version not found");
        }
    }

    private boolean nullOrEmpty(String vnfType) {
        return vnfType == null || vnfType.isEmpty();
    }

    private void logContext(SvcLogicContext context) {
        for (String key : context.getAttributeKeySet()) {
            logger.debug(key + " = " + context.getAttribute(key) + "\n");
        }
    }

    private void populateContext(VnfcFlowModel flowModel, SvcLogicContext context) {
        int flowIndex = 0;
        Iterator<List<Vnfc>> iterator = flowModel.getModelIterator();
        while (iterator.hasNext()) {
            for (Vnfc vnfc : iterator.next()) {
                context.setAttribute(VNFC_FLOW + flowIndex + "].vnfcName", vnfc.getVnfcName());
                context.setAttribute(VNFC_FLOW + flowIndex + "].vnfcType", vnfc.getVnfcType());
                context.setAttribute(VNFC_FLOW + flowIndex + "].resilienceType", vnfc.getResilienceType());
                context
                    .setAttribute(VNFC_FLOW + flowIndex + "].vmCount", Integer.toString(vnfc.getVserverList().size()));
                int vmIndex = 0;
                for (Vserver vm : vnfc.getVserverList()) {
                    context.setAttribute(VNFC_FLOW + flowIndex + "].vm[" + vmIndex + "].url", vm.getUrl());
                    vmIndex++;
                }
                flowIndex++;
            }
        }
        context.setAttribute("vnfcFlowCount", Integer.toString(flowIndex));
    }

    private InventoryModel readInventoryModel(SvcLogicContext context) throws APPCException {
        String vnfId = context.getAttribute("input.action-identifiers.vnf-id");
        String vnfType = context.getAttribute("vnf.type");
        String vnfVersion = context.getAttribute("vnf.version");
        String vnfcCountStr = context.getAttribute("vnf.vnfcCount");
        Integer vnfcCount = Integer.parseInt(vnfcCountStr);
        Vnf vnf = createVnf(vnfId, vnfType, vnfVersion);
        for (Integer i = 0; i < vnfcCount; i++) {
            String vnfcName = context.getAttribute(VNF_VNFC + i + "].name");
            String vnfcType = context.getAttribute(VNF_VNFC + i + "].type");
            String vmCountStr = context.getAttribute(VNF_VNFC + i + "].vm_count");
            if (nullOrEmpty(vnfcType)) {
                throw new APPCException("Could not retrieve VNFC Type from DG Context for vnf.vnfc[" + i + "].type");
            }
            Integer vmCount = Integer.parseInt(vmCountStr);
            Vnfc vnfc = createVnfc(vnfcName, vnfcType);
            for (Integer j = 0; j < vmCount; j++) {
                String vmURL = context.getAttribute(VNF_VNFC + i + "].vm[" + j + "].url");
                Vserver vm = createVserver(vmURL);
                vm.setVnfc(vnfc);
                vnfc.addVserver(vm);
                vnf.addVserver(vm);
            }
        }
        return new InventoryModel(vnf);
    }

    private Vserver createVserver(String vmURL) {
        Vserver vserver = new Vserver();
        vserver.setUrl(vmURL);
        return vserver;
    }

    private Vnfc createVnfc(String vnfcName, String vnfcType) {
        Vnfc vnfc = new Vnfc();
        vnfc.setVnfcName(vnfcName);
        vnfc.setVnfcType(vnfcType);
        return vnfc;
    }

    private Vnf createVnf(String vnfId, String vnfType, String vnfVersion) {
        Vnf vnf = new Vnf();
        vnf.setVnfType(vnfType);
        vnf.setVnfId(vnfId);
        vnf.setVnfVersion(vnfVersion);
        return vnf;
    }

    private DependencyModelIdentifier readDependencyModelIdentifier(Map<String, String> params) {
        String vnfType = params.get(Constants.VNF_TYPE);
        String catalogVersion = params.get(Constants.VNF_VERION);
        return new DependencyModelIdentifier(vnfType, catalogVersion);
    }

    private void validateInventoryModelWithDependencyModel(VnfcDependencyModel dependencyModel,
        InventoryModel inventoryModel) throws APPCException {
        Set<String> dependencyModelVnfcSet = new HashSet<>();
        Set<String> dependencyModelMandatoryVnfcSet = new HashSet<>();
        Set<String> inventoryModelVnfcsSet = new HashSet<>();
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
            Set<String> difference = new HashSet<>(inventoryModelVnfcsSet);
            difference.removeAll(dependencyModelVnfcSet);
            logger.error("Dependency model is missing following vnfc type(s): " + difference);
            throw new APPCException("Dependency model is missing following vnfc type(s): " + difference);
        } else {
            Set<String> difference = new HashSet<>(dependencyModelMandatoryVnfcSet);
            difference.removeAll(inventoryModelVnfcsSet);
            if (!difference.isEmpty()) {
                logger.error("Inventory model is missing following mandatory vnfc type(s): " + difference);
                throw new APPCException("Inventory model is missing following mandatory vnfc type(s): " + difference);
            }
        }
    }

    private void reconcileFlowModel(VnfcFlowModel flowModel) {
        Iterator<List<Vnfc>> flowIterator = flowModel.getModelIterator();
        while (flowIterator.hasNext()) {
            Iterator<Vnfc> vnfcIterator = flowIterator.next().iterator();
            while (vnfcIterator.hasNext()) {
                Vnfc vnfc = vnfcIterator.next();
                tryRemoveInterator(vnfcIterator, vnfc);
            }
        }
    }

    private void tryRemoveInterator(Iterator<Vnfc> vnfcIterator, Vnfc vnfc) {
        if (vnfc.getVserverList().isEmpty()) {
            if (logger.isDebugEnabled()) {
                logger.debug("No vservers present for Vnfc type: " + vnfc.getVnfcType()
                    + ". Hence, removing it from the flow model.");
            }
            vnfcIterator.remove();
        }
    }
}
