/*-
 * ============LICENSE_START=======================================================
 * ONAP : APP-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property.  All rights reserved.
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
 */

package org.openecomp.appc.seqgen.dgplugin.impl;


import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.appc.dg.objects.*;
import org.openecomp.appc.domainmodel.Vnf;
import org.openecomp.appc.domainmodel.Vnfc;
import org.openecomp.appc.domainmodel.Vserver;
import org.openecomp.appc.domainmodel.lcm.VNFOperation;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.seqgen.SequenceGenerator;
import org.openecomp.appc.seqgen.dgplugin.SequenceGeneratorPlugin;
import org.openecomp.appc.seqgen.impl.SequenceGeneratorFactory;
import org.openecomp.appc.seqgen.objects.Constants;
import org.openecomp.appc.seqgen.objects.SequenceGeneratorInput;
import org.openecomp.appc.seqgen.objects.Transaction;
import org.openecomp.sdnc.sli.SvcLogicContext;

import java.io.IOException;
import java.util.*;

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
        sequenceGeneratorInput.setDependencyModel(dependencyModel);

        return sequenceGeneratorInput;
    }
    private List<Transaction> generateSequence(SequenceGeneratorInput sequenceGeneratorInput) throws APPCException {
        if (sequenceGeneratorInput.getRequestInfo() == null) {
            throw new APPCException("Request info is not provided in the input");
        }
        String action = sequenceGeneratorInput.getRequestInfo().getAction();
        VNFOperation operation = VNFOperation.findByString(action);
        if (operation == null) {
            throw new APPCException("Invalid Action " + action);
        }
        SequenceGenerator sequenceGenerator = SequenceGeneratorFactory.getInstance().createSequenceGenerator(operation);
        return sequenceGenerator.generateSequence(sequenceGeneratorInput);
    }

    // Dependency model is an optional attribute and may contain null values
    private VnfcDependencyModel buildDependencyModel(String inputJson) throws IOException, APPCException {
        Set<Node<Vnfc>> dependency = new HashSet<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        JsonNode rootNode = objectMapper.readTree(inputJson);
        JsonNode vnfcs = getVnfcsNode(rootNode);
        if (vnfcs != null) {
            for (JsonNode vnfcNode : vnfcs) {
                String vnfcType = readVnfcType(vnfcNode);
                String mandatory = readMandatory(vnfcNode);
                String resilience = readResilience(vnfcNode);
                Vnfc vnfc = new Vnfc(vnfcType, resilience, null, Boolean.parseBoolean(mandatory));
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
                    Node<Vnfc> parentNode = getNode(dependency, parentVnfcType);
                    if (parentNode != null) {
                        currentNode.addParent(parentNode.getChild());
                    } else {
                        Vnfc parentVnfc = new Vnfc(parentVnfcType, null, null, false);
                        parentNode = new Node<>(parentVnfc);
                        currentNode.addParent(parentVnfc);
                        dependency.add(parentNode);
                    }
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
        objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
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
        String vnfVersion = vnfInfo.get("vnf-version").asText();

        Vnf vnf = new Vnf(vnfId, vnfType, vnfVersion);

        JsonNode vms = vnfInfo.get("vm");

        Map<Vnfc, List<Vserver>> vfcs = new HashMap<>();
        for (JsonNode vm : vms) {
            if(vm.get("vserver-id")== null){
                throw new APPCException("vserver-id not found ");
            }
            String vserverId = vm.get("vserver-id").asText();
            Vserver vserver = new Vserver(null, null, vserverId, null, null);
            JsonNode vnfc = vm.get("vnfc");
            if (vnfc.get("vnfc-name") == null) {
                throw new APPCException("vnfc-name not found for vserver " + vserverId);
            }
            String vnfcName = vnfc.get("vnfc-name").asText();
            if (vnfc.get("vnfc-type") == null) {
                throw new APPCException("vnfc-type not found for vserver " + vserverId);
            }
            String vnfcType = vnfc.get("vnfc-type").asText();
            if (StringUtils.isEmpty(vnfcType)) {
                throw new APPCException("vserver " + vserverId + " is not associated with any vnfc");
            }
            Vnfc vfc = new Vnfc(vnfcType, null, vnfcName);
            List<Vserver> vServers = vfcs.get(vfc);
            if (vServers == null) {
                vServers = new LinkedList<>();
                vfcs.put(vfc, vServers);
            }
            vServers.add(vserver);
        }

        for (Map.Entry<Vnfc, List<Vserver>> entry : vfcs.entrySet()) {
            Vnfc vnfc = entry.getKey();
            List<Vserver> vServers = vfcs.get(vnfc);
            vnfc.addVms(vServers);
            vnf.addVnfc(vnfc);
        }

        return new InventoryModel(vnf);
    }
}
