/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications (C) 2019 Ericsson
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

package org.onap.appc.dg.dependencymanager.helper;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.onap.appc.dg.flowbuilder.exception.InvalidDependencyModelException;
import org.onap.appc.dg.objects.Node;
import org.onap.appc.dg.objects.VnfcDependencyModel;
import org.onap.appc.domainmodel.Vnfc;

public class DependencyModelParser {

    private final EELFLogger logger = EELFManager.getInstance().getLogger(DependencyModelParser.class);

    private static final String PROPERTIES = "properties";
    private static final String ACTIVE_ACTIVE = "Active-Active";
    private static final String ACTIVE_PASSIVE = "Active-Passive";
    private static final String HIGH_AVAILABLITY = "high_availablity";
    private static final String HIGH_AVAILABILITY = "high_availability";
    private static final String MANDATORY = "mandatory";
    private static final String TOPOLOGY_TEMPLATE = "topology_template";
    private static final String RELATIONSHIP = "relationship";

    private static Map<String, String> dependencyMap;

    static {
        Map<String, String> dependencyTypeMappingMap = new HashMap<>();
        dependencyTypeMappingMap.put("geo-activeactive", ACTIVE_ACTIVE);
        dependencyTypeMappingMap.put("geo-activestandby", ACTIVE_PASSIVE);
        dependencyTypeMappingMap.put("local-activeactive", ACTIVE_ACTIVE);
        dependencyTypeMappingMap.put("local-activestandby", ACTIVE_PASSIVE);
        dependencyMap = Collections.unmodifiableMap(dependencyTypeMappingMap);
    }

    public VnfcDependencyModel generateDependencyModel(String vnfModel, String vnfType)
        throws InvalidDependencyModelException {
        Set<Node<Vnfc>> dependencies = new HashSet<>();
        ObjectMapper mapper = getMapper();
        boolean mandatory;
        String resilienceType;
        String prefix = "org.onap.resource.vfc." + vnfType + ".abstract.nodes.";
        try {
            ObjectNode root = (ObjectNode) mapper.readTree(vnfModel);

            if (root.get(TOPOLOGY_TEMPLATE) == null || root.get(TOPOLOGY_TEMPLATE).get("node_templates") == null) {
                throw new InvalidDependencyModelException(
                    "Dependency model is missing 'topology_template' or  'node_templates' elements");
            }

            JsonNode topologyTemplateNode = root.get(TOPOLOGY_TEMPLATE);
            JsonNode nodeTemplateNode = topologyTemplateNode.get("node_templates");
            Iterator<Map.Entry<String, JsonNode>> iterator = nodeTemplateNode.fields();
            for (JsonNode yamlNode : nodeTemplateNode) {
                logger.debug("Processing node: " + yamlNode);
                String fullvnfcType = iterator.next().getValue().get("type").textValue();
                String vnfcType = getQualifiedVnfcType(fullvnfcType);
                String type = yamlNode.get("type").textValue();
                type = type.substring(0, type.lastIndexOf('.') + 1);
                if (type.concat(vnfcType).toLowerCase().startsWith(prefix.concat(vnfcType).toLowerCase())) {

                    resilienceType = resolveResilienceType(yamlNode);
                    mandatory = resolveMandatory(yamlNode);
                    String[] parentList = getDependencyArray(yamlNode, nodeTemplateNode);
                    Node<Vnfc> vnfcNode = getNode(dependencies, vnfcType);
                    if (vnfcNode != null) {
                        //This code appears to be unreachable
                        logger.debug("Dependency node already exists for vnfc Type: " + vnfcType);
                        if (StringUtils.isEmpty(vnfcNode.getChild().getResilienceType())) {
                            logger.debug("Updating resilience type, "
                                + "dependencies and mandatory attribute for VNFC type: " + vnfcType);
                            vnfcNode.getChild().setResilienceType(resilienceType);
                            tryFillNode(dependencies, parentList, vnfcNode);
                            vnfcNode.getChild().setMandatory(mandatory);
                        }
                    } else {
                        logger.debug("Creating dependency node for  : " + vnfcType);
                        vnfcNode = new Node<>(createVnfc(mandatory, resilienceType, vnfcType));
                        tryFillNode(dependencies, parentList, vnfcNode);
                        logger.debug("Adding VNFC to dependency model : " + vnfcNode);
                        dependencies.add(vnfcNode);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error parsing dependency model : " + vnfModel);
            logger.error("Error message : " + e);
            throw new InvalidDependencyModelException("Error parsing dependency model. " + e.getMessage());
        }
        return new VnfcDependencyModel(dependencies);
    }

    private void tryFillNode(Set<Node<Vnfc>> dependencies, String[] parentList, Node<Vnfc> vnfcNode) {
        if (parentList.length > 0) {
            fillNode(dependencies, vnfcNode, parentList);
        }
    }

    private boolean resolveMandatory(JsonNode yamlNode) {
        return !mandatoryDoesNotExist(yamlNode) && yamlNode.get(PROPERTIES).findValue(MANDATORY).booleanValue();
    }

    private boolean mandatoryDoesNotExist(JsonNode yamlNode) {
        return yamlNode.get(PROPERTIES).findValue(MANDATORY) == null ||
            yamlNode.get(PROPERTIES).findValue(MANDATORY).asText().isEmpty();
    }

    private String resolveResilienceType(JsonNode yamlNode) {
        String resilienceType;
        // If "high_availability" is present then use the correctly spelled property name
        if (yamlNode.get(PROPERTIES).findValue(HIGH_AVAILABILITY) != null &&
                !yamlNode.get(PROPERTIES).findValue(HIGH_AVAILABILITY).asText().isEmpty()) {
            resilienceType = dependencyMap
                    .get(yamlNode.get(PROPERTIES).findValue(HIGH_AVAILABILITY).textValue());
        }
        // The property name "high_availability" was misspelled in the code so leaving in the code to check for the 
        // incorrectly spelled version to avoid breaking existing configurations using the misspelled version
        else if (yamlNode.get(PROPERTIES).findValue(HIGH_AVAILABLITY) == null ||
                yamlNode.get(PROPERTIES).findValue(HIGH_AVAILABLITY).asText().isEmpty()) {
            resilienceType = ACTIVE_ACTIVE;
        } else {
            resilienceType = dependencyMap
                    .get(yamlNode.get(PROPERTIES).findValue(HIGH_AVAILABLITY).textValue());
        }
        return resilienceType;
    }

    private Vnfc createVnfc(boolean mandatory, String resilienceType, String vnfcType) {
        Vnfc vnfc = new Vnfc();
        vnfc.setMandatory(mandatory);
        vnfc.setResilienceType(resilienceType);
        vnfc.setVnfcType(vnfcType);
        return vnfc;
    }

    private String getQualifiedVnfcType(String fullvnfcType) {
        return fullvnfcType.substring(fullvnfcType.lastIndexOf('.') + 1, fullvnfcType.length());
    }

    private void fillNode(Set<Node<Vnfc>> nodes, Node<Vnfc> node, String[] parentList) {
        for (String type : parentList) {
            String parentType = getVnfcType(type);
            Node<Vnfc> parentNode = getNode(nodes, parentType);
            if (parentNode != null) {
                logger.debug("VNFC already exists for VNFC type: " + parentType + ". Adding it to parent list ");
                node.addParent(parentNode.getChild());
            } else {
                logger.debug("VNFC does not exist for VNFC type: " + parentType + ". Creating new VNFC ");
                parentNode = new Node<>(createVnfc(false, null, parentType));
                node.addParent(parentNode.getChild());
                logger.debug("Adding VNFC to dependency model : " + parentNode);
                nodes.add(parentNode);
            }
        }
    }

    private String[] getDependencyArray(JsonNode node, JsonNode nodeTemplateNode)
        throws InvalidDependencyModelException {
        JsonNode requirementsNode = node.get("requirements");
        Set<String> dependencyList = new HashSet<>();
        if (requirementsNode != null) {
            for (JsonNode internalNode : requirementsNode) {
                //TODO : In this release we are supporting both relationship = tosca.capabilities.Node  and relationship =tosca.relationships.DependsOn we need to remove one of them in next release post confirming with SDC team
                if (verifyNode(internalNode)) {
                    parseDependencyModel(node, nodeTemplateNode, dependencyList, internalNode);
                }
            }
            return dependencyList.toArray(new String[0]);
        } else {
            return new String[0];
        }
    }

    private void parseDependencyModel(JsonNode node, JsonNode nodeTemplateNode, Set<String> dependencyList,
        JsonNode internalNode) throws InvalidDependencyModelException {

        if (internalNode.findValue("node") != null) {
            String nodeName = internalNode.findValue("node").asText();
            String fullVnfcName = nodeTemplateNode.get(nodeName).get("type").asText();
            dependencyList.add(getQualifiedVnfcType(fullVnfcName));
        } else {
            throw new InvalidDependencyModelException(
                "Error parsing dependency model. " + "Dependent Node not found for " + node.get("type"));
        }
    }

    private boolean verifyNode(JsonNode internalNode) {
        return nodeNullCheck(internalNode) &&
            "tosca.capabilities.Node".equalsIgnoreCase(internalNode.findValue("capability").asText()) &&
            ("tosca.relationships.DependsOn".equalsIgnoreCase(internalNode.findValue(RELATIONSHIP).asText()) ||
                "tosca.capabilities.Node".equalsIgnoreCase(internalNode.findValue(RELATIONSHIP).asText()));
    }

    private boolean nodeNullCheck(JsonNode internalNode) {
        return internalNode.get("dependency") != null && internalNode.findValue("capability") != null
            && internalNode.findValue(RELATIONSHIP) != null;
    }

    protected Node<Vnfc> getNode(Set<Node<Vnfc>> nodes, String vnfcType) {
        Iterator<Node<Vnfc>> itr = nodes.iterator();
        Node<Vnfc> node;
        while (itr.hasNext()) {
            node = itr.next();
            if (node.getChild().getVnfcType().equalsIgnoreCase(vnfcType)) {
                return node;
            }
        }
        return null;
    }
    private String getVnfcType(String type) {
        return type.substring(type.lastIndexOf('.') + 1, type.length());
    }

    protected ObjectMapper getMapper() {
        return new ObjectMapper(new YAMLFactory());
    }
}
