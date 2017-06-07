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

package org.openecomp.appc.dg.dependencymanager.helper;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.appc.dg.flowbuilder.exception.InvalidDependencyModel;
import org.openecomp.appc.dg.objects.Node;
import org.openecomp.appc.dg.objects.VnfcDependencyModel;
import org.openecomp.appc.domainmodel.Vnfc;

import java.io.IOException;
import java.util.*;


public class DependencyModelParser {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(DependencyModelParser.class);
    private static Map<String, String> dependencyMap;
    private static final String PROPERTIES = "properties";
    private static final String ACTIVE_ACTIVE = "Active-Active";
    private static final String ACTIVE_PASSIVE = "Active-Passive";
    private static final String HIGH_AVAILABLITY = "high_availablity";
    private static final String MANDATORY = "mandatory";
    private static final String TOPOLOGY_TEMPLATE = "topology_template";

    static {
        Map<String, String> dependencyTypeMappingMap =new HashMap<>();
        dependencyTypeMappingMap.put("geo-activeactive", ACTIVE_ACTIVE);
        dependencyTypeMappingMap.put("geo-activestandby", ACTIVE_PASSIVE);
        dependencyTypeMappingMap.put("local-activeactive", ACTIVE_ACTIVE);
        dependencyTypeMappingMap.put("local-activestandby", ACTIVE_PASSIVE);
        dependencyMap = Collections.unmodifiableMap(dependencyTypeMappingMap);
    }

    public VnfcDependencyModel generateDependencyModel(String vnfModel,String vnfType) {
        Set<Node<Vnfc>> dependencies = new HashSet<>();
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        boolean mandatory;
        String resilienceType;
        String prefix = "org.openecomp.resource.vfc."+vnfType+".abstract.nodes.";
        try {
            ObjectNode root = (ObjectNode) mapper.readTree(vnfModel);
            logger.debug("VNF Model after parsing: " + root);

            if(root.get(TOPOLOGY_TEMPLATE) == null || root.get(TOPOLOGY_TEMPLATE).get("node_templates") == null) {
                throw new InvalidDependencyModel("Dependency model is missing 'topology_template' or  'node_templates' elements");
            }

            JsonNode topologyTemplateNode = root.get(TOPOLOGY_TEMPLATE);
            JsonNode nodeTemplateNode = topologyTemplateNode.get("node_templates");
            Iterator<Map.Entry<String, JsonNode>> itretor  = nodeTemplateNode.fields();
            for (JsonNode yamlNode : nodeTemplateNode) {
                logger.debug("Processing node: " + yamlNode);
                String vnfcType = itretor.next().getKey();
                String type = yamlNode.get("type").textValue();
                type = type.substring(0,type.lastIndexOf(".")+1);
                if(type.concat(vnfcType).toLowerCase().startsWith(prefix.concat(vnfcType).toLowerCase())) {

                    if(yamlNode.get(PROPERTIES).findValue(HIGH_AVAILABLITY) == null || yamlNode.get(PROPERTIES).findValue(HIGH_AVAILABLITY).asText().isEmpty()) {
                        resilienceType = ACTIVE_ACTIVE;
                    }else {
                        resilienceType = dependencyMap.get(yamlNode.get(PROPERTIES).findValue(HIGH_AVAILABLITY).textValue());
                    }

                    if(yamlNode.get(PROPERTIES).findValue(MANDATORY) == null || yamlNode.get(PROPERTIES).findValue(MANDATORY).asText().isEmpty()) {
                        mandatory = false;
                    }else {
                        mandatory = yamlNode.get(PROPERTIES).findValue(MANDATORY).booleanValue();
                    }
                    String[] parentList = getDependencyArray(yamlNode);
                    Node<Vnfc> vnfcNode = getNode(dependencies, vnfcType);
                    if (vnfcNode != null) {
                        logger.debug("Dependency node already exists for vnfc Type: " + vnfcType);
                        if (StringUtils.isEmpty(vnfcNode.getChild().getResilienceType())) {
                            logger.debug("Updating resilience type, dependencies and mandatory attribute for VNFC type: " + vnfcType);
                            vnfcNode.getChild().setResilienceType(resilienceType);
                            if (parentList != null && parentList.length > 0) {
                                addDependencies(dependencies, vnfcNode, parentList);
                            }
                            vnfcNode.getChild().setMandatory(mandatory);
                        }

                    } else {
                        logger.debug("Creating dependency node for  : " + vnfcType);
                        vnfcNode = new Node<>(new Vnfc(vnfcType, resilienceType, null, mandatory));
                        if (parentList != null && parentList.length > 0)
                            addDependencies(dependencies, vnfcNode, parentList);
                        logger.debug("Adding VNFC to dependency model : " + vnfcNode);
                        dependencies.add(vnfcNode);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error parsing dependency model : " + vnfModel);
            logger.error("Error message : " + e);
            throw new InvalidDependencyModel("Error parsing dependency model. " + e.getMessage());
        }
        return new VnfcDependencyModel(dependencies);
    }

    private void addDependencies(Set<Node<Vnfc>> nodes, Node node, String[] parentList) {
        for (String type : parentList) {
            String parentType = getVnfcType(type);
            Node<Vnfc> parentNode = getNode(nodes, parentType);
            if (parentNode != null) {
                logger.debug("VNFC already exists for VNFC type: " + parentType + ". Adding it to parent list ");
                node.addParent(parentNode.getChild());
            } else {
                logger.debug("VNFC does not exist for VNFC type: " + parentType + ". Creating new VNFC ");
                parentNode = new Node<>(new Vnfc(parentType, null));
                node.addParent(parentNode.getChild());
                logger.debug("Adding VNFC to dependency model : " + parentNode);
                nodes.add(parentNode);
            }
        }
    }

    private String[] getDependencyArray(JsonNode node) {
        JsonNode requirementsNode = node.get("requirements");
        List<String> dependencyList  = new ArrayList();
        if(requirementsNode!=null) {
            for (JsonNode internalNode : requirementsNode) {
                if (nodeNullCheck(internalNode) &&"tosca.capabilities.Node".equalsIgnoreCase(internalNode.get("capability").asText())
                        && "tosca.relationships.DependsOn".equalsIgnoreCase(internalNode.get("relationship").asText())) {
                    if(internalNode.get("node") != null) {
                        dependencyList.add(internalNode.get("node").asText());
                    }else{
                        throw new InvalidDependencyModel("Error parsing dependency model. " + "Dependent Node not found for "+ node.get("type"));
                    }
                }
            }
            return  dependencyList.toArray(new String[0]);
        }else{
            return new String[0];
        }
    }

    private boolean nodeNullCheck(JsonNode internalNode) {
        return internalNode.get("dependency") != null && internalNode.get("capability") != null && internalNode.get("relationship") != null;
    }

    private Node<Vnfc> getNode(Set<Node<Vnfc>> nodes, String vnfcType) {
        Iterator itr = nodes.iterator();
        Node<Vnfc> node;
        while (itr.hasNext()) {
            node = (Node<Vnfc>) itr.next();
            if (node.getChild().getVnfcType().equalsIgnoreCase(vnfcType)) {
                return node;
            }
        }
        return null;
    }

    private String getVnfcType(String type) {
        return type.substring(type.lastIndexOf('.') + 1, type.length());
    }

}
