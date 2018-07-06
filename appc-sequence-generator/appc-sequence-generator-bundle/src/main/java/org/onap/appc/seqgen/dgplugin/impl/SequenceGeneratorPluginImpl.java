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

package org.onap.appc.seqgen.dgplugin.impl;


import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.onap.appc.dg.flowbuilder.exception.InvalidDependencyModelException;
import org.onap.appc.dg.objects.InventoryModel;
import org.onap.appc.dg.objects.Node;
import org.onap.appc.dg.objects.VnfcDependencyModel;
import org.onap.appc.domainmodel.Vnf;
import org.onap.appc.domainmodel.Vnfc;
import org.onap.appc.domainmodel.Vserver;
import org.onap.appc.domainmodel.lcm.VNFOperation;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.seqgen.SequenceGenerator;
import org.onap.appc.seqgen.dgplugin.SequenceGeneratorPlugin;
import org.onap.appc.seqgen.impl.SequenceGeneratorFactory;
import org.onap.appc.seqgen.objects.Constants;
import org.onap.appc.seqgen.objects.SequenceGeneratorInput;
import org.onap.appc.seqgen.objects.Transaction;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.LinkedList;

public class SequenceGeneratorPluginImpl implements SequenceGeneratorPlugin {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(SequenceGeneratorPluginImpl.class);

    @Override
    public void generateSequence(Map<String, String> params, SvcLogicContext context) {
        ObjectMapper objectMapper = new ObjectMapper();
        String inputJSON = context.getAttribute("inputJSON");
        logger.debug("Input to Sequence Generator " + inputJSON);
        try {
            SequenceGeneratorInput sequenceGeneratorInput = buildSequenceGeneratorInput(inputJSON);
            List<Transaction> sequence = generateSequence(sequenceGeneratorInput);
            String output = objectMapper.writeValueAsString(sequence);
            logger.debug("Sequence Generator Output " + output);

            context.setAttribute("output", output);
        } catch (Exception e) {
            logger.error("Error generating sequence", e);
            context.setAttribute("error-code", "401");
            context.setAttribute("error-message", "Error generating sequence " + e.getMessage());
        }
    }

    private SequenceGeneratorInput buildSequenceGeneratorInput(String inputJson) throws IOException, APPCException {
        ObjectMapper objectMapper = new ObjectMapper();
        SequenceGeneratorInput sequenceGeneratorInput ;
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        sequenceGeneratorInput = objectMapper.readValue(inputJson, SequenceGeneratorInput.class);

        InventoryModel inventoryModel = buildInventoryModel(inputJson);
        sequenceGeneratorInput.setInventoryModel(inventoryModel);

        VnfcDependencyModel dependencyModel = buildDependencyModel(inputJson);
        if(dependencyModel!=null){
            validateInventoryModelWithDependencyModel(dependencyModel,inventoryModel);
        }
        sequenceGeneratorInput.setDependencyModel(dependencyModel);

        return sequenceGeneratorInput;
    }
    private List<Transaction> generateSequence(SequenceGeneratorInput sequenceGeneratorInput) throws Exception {
        if (sequenceGeneratorInput.getRequestInfo() == null) {
            throw new APPCException("Request info is not provided in the input");
        }
        String action = sequenceGeneratorInput.getRequestInfo().getAction();
        VNFOperation operation = VNFOperation.findByString(action);
        if (operation == null) {
            throw new APPCException("Invalid Action " + action);
        }
        if(Constants.ActionLevel.findByString(sequenceGeneratorInput.getRequestInfo().getActionLevel().toUpperCase())==null){
            throw new APPCException("Invalid Action Level " + sequenceGeneratorInput.getRequestInfo().getActionLevel());
        }
        SequenceGenerator sequenceGenerator = SequenceGeneratorFactory.getInstance().createSequenceGenerator(operation);
        return sequenceGenerator.generateSequence(sequenceGeneratorInput);
    }

    private void validateInventoryModelWithDependencyModel(VnfcDependencyModel dependencyModel, InventoryModel inventoryModel) throws APPCException {
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
            if (difference.size() > 0) {
                logger.error("Inventory model is missing following mandatory vnfc type(s): " + difference);
                throw new APPCException("VMs missing for the mandatory VNFC : " + difference);
            }
        }
    }

    // Dependency model is an optional attribute and may contain null values
    private VnfcDependencyModel buildDependencyModel(String inputJson) throws IOException, APPCException {
        Set<Node<Vnfc>> dependency = new HashSet<>();
        Set<String> parentVnfcs=new HashSet<>();
        Set<String> allVnfcTypes=new HashSet<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        JsonNode rootNode = objectMapper.readTree(inputJson);
        JsonNode vnfcs = getVnfcsNode(rootNode);
        if (vnfcs != null) {
            for (JsonNode vnfcNode : vnfcs) {
                String vnfcType = readVnfcType(vnfcNode);
                allVnfcTypes.add(vnfcType);
                String mandatory = readMandatory(vnfcNode);
                String resilience = readResilience(vnfcNode);
                Vnfc vnfc = new Vnfc();
                vnfc.setVnfcType(vnfcType);
                vnfc.setResilienceType(resilience);
                vnfc.setMandatory(Boolean.parseBoolean(mandatory));
                Node<Vnfc> currentNode = getNode(dependency, vnfcType);
                if (currentNode == null) {
                    currentNode = new Node<>(vnfc);
                    dependency.add(currentNode);
                } else {
                    currentNode.getChild().setMandatory(Boolean.valueOf(mandatory));
                    currentNode.getChild().setResilienceType(resilience);
                }
                JsonNode parents = vnfcNode.get("parents");
                for (JsonNode parent : parents) {
                    String parentVnfcType = parent.asText();
                    parentVnfcs.add(parentVnfcType);
                    Node<Vnfc> parentNode = getNode(dependency, parentVnfcType);
                    if (parentNode != null) {
                        currentNode.addParent(parentNode.getChild());
                    } else {
                        Vnfc parentVnfc=new Vnfc();
                        parentVnfc.setVnfcType(parentVnfcType);
                        parentVnfc.setMandatory(false);
                        parentNode = new Node<>(parentVnfc);
                        currentNode.addParent(parentVnfc);
                        dependency.add(parentNode);
                    }
                }

            }
            for(String parent:parentVnfcs){
                if(!allVnfcTypes.contains(parent)){
                    throw new APPCException("Dependency model missing vnfc type "+parent);
                }
            }
            return new VnfcDependencyModel(dependency);
        }
        return null;
    }

    private String readResilience(JsonNode vnfcNode) {
        String resilience = null;
        if (vnfcNode.get("resilience") != null) {
            resilience = vnfcNode.get("resilience").asText();
        }
        return resilience;
    }

    private String readMandatory(JsonNode vnfcNode) {
        String mandatory ;
        JsonNode mandatoryNode = vnfcNode.get("mandatory");
        if (mandatoryNode == null) {
            mandatory = "false";
        } else {
            mandatory = mandatoryNode.asText();
        }
        return mandatory;
    }

    private String readVnfcType(JsonNode vnfcNode) throws APPCException {
        JsonNode vnfcTypeNode = vnfcNode.get(Constants.VNFC_TYPE);
        if (vnfcTypeNode == null) {
            throw new APPCException("vnfc-type is not available in dependency info");
        }
        return vnfcTypeNode.asText();
    }

    private JsonNode getVnfcsNode(JsonNode rootNode) {
        JsonNode dependencyInfo = rootNode.get("dependency-info");
        JsonNode vnfcs = null;
        if (dependencyInfo != null) {
            vnfcs = dependencyInfo.get("vnfcs");
        }
        return vnfcs;
    }

    private Node<Vnfc> getNode(Set<Node<Vnfc>> dependency, String vnfcType) {
        for (Node<Vnfc> node : dependency) {
            if (node.getChild().getVnfcType().equals(vnfcType)) {
                return node;
            }
        }
        return null;
    }

    private InventoryModel buildInventoryModel(String inputJson) throws IOException, APPCException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(inputJson);
        JsonNode inventoryInfo = jsonNode.get("inventory-info");
        if (inventoryInfo == null) {
            throw new APPCException("inventory-info is not provided in the input");
        }
        JsonNode vnfInfo = inventoryInfo.get("vnf-info");
        if (vnfInfo == null) {
            throw new APPCException("vnf-info is not provided in the input");
        }

        String vnfId = vnfInfo.get("vnf-id").asText();
        String vnfType = vnfInfo.get("vnf-type").asText();
        String identityUrl = vnfInfo.get("identity-url").asText();
        Vnf vnf =new Vnf();
        vnf.setVnfId(vnfId);
        vnf.setVnfType(vnfType);
        vnf.setIdentityUrl(identityUrl);
        logger.debug("IdentityUrl in SeqGen:" + identityUrl);
        Map<Vnfc, List<Vserver>> vfcs = new HashMap<>();
        JsonNode vms = vnfInfo.get("vm");
        if(vms.size()<1){
            throw new APPCException("vm info not provided in the input");
        }
        for (JsonNode vm : vms) {
            if(vm.get("vserver-id")== null){
                throw new APPCException("vserver-id not found ");
            }
            String vserverId = vm.get("vserver-id").asText();
            String vmId =vm.get("vm-id").asText();
            Vserver vserver = new Vserver();
            vserver.setId(vserverId);
            vserver.setUrl(vmId);
            if (vm.get("vnfc")!=null&& vm.get("vnfc").get("vnfc-name") != null && vm.get("vnfc").get("vnfc-type")!= null) {
                Vnfc vfc = new Vnfc();
                vfc.setVnfcType(vm.get("vnfc").get("vnfc-type").asText());
                vfc.setVnfcName(vm.get("vnfc").get("vnfc-name").asText());
                vserver.setVnfc(vfc);
                List<Vserver> vServers = vfcs.get(vfc);
                if (vServers == null) {
                    vServers = new LinkedList<>();
                    vfcs.put(vfc, vServers);
                }
                vServers.add(vserver);
            }
            vnf.addVserver(vserver);
        }

        for (Map.Entry<Vnfc, List<Vserver>> entry : vfcs.entrySet()) {
            Vnfc vnfc = entry.getKey();
            List<Vserver> vServers = vfcs.get(vnfc);
            vnfc.addVservers(vServers);
        }

        return new InventoryModel(vnf);
    }
}
