/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 Ericsson
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

import java.io.IOException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.onap.appc.dg.flowbuilder.exception.InvalidDependencyModelException;
import org.onap.appc.dg.objects.VnfcDependencyModel;
import org.onap.appc.dg.objects.Node;
import org.onap.appc.domainmodel.Vnfc;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class DependencyModelParserTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    DependencyModelParser parser = Mockito.spy(new DependencyModelParser());
    ObjectMapper mockMapper;

    @Before
    public void setup() throws JsonProcessingException, IOException {
        mockMapper = Mockito.mock(ObjectMapper.class);
        ObjectNode mockNode = Mockito.mock(ObjectNode.class);
        Mockito.doReturn(mockMapper).when(parser).getMapper();
        Mockito.doReturn(mockNode).when(mockMapper).readTree("VNF_MODEL");
    }

    @Test
    public void testGenerateDependencyModel() throws InvalidDependencyModelException, JsonProcessingException, IOException {
        ObjectNode topologyTemplate = new ObjectNode(JsonNodeFactory.instance);
        topologyTemplate = (ObjectNode) new ObjectMapper().readTree(jsonString);
        Mockito.doReturn(topologyTemplate).when(mockMapper).readTree("VNF_MODEL");
        VnfcDependencyModel model = parser.generateDependencyModel("VNF_MODEL", "VNF_TYPE");
        Assert.assertEquals(2, model.getDependencies().size());
    }

    @Test
    public void testGenerateDependencyModelWithNode() throws InvalidDependencyModelException, JsonProcessingException, IOException {
        ObjectNode topologyTemplate = new ObjectNode(JsonNodeFactory.instance);
        topologyTemplate = (ObjectNode) new ObjectMapper().readTree(jsonString);
        Node<Vnfc> vnfc = new Node(new Vnfc());
        Mockito.doReturn(vnfc).when(parser).getNode(Mockito.anySet(), Mockito.anyString());
        Mockito.doReturn(topologyTemplate).when(mockMapper).readTree("VNF_MODEL");
        VnfcDependencyModel model = parser.generateDependencyModel("VNF_MODEL", "VNF_TYPE");
        Assert.assertEquals(0, model.getDependencies().size());
    }

    @Test
    public void testGenerateDependencyModelExceptionFlow() throws InvalidDependencyModelException {
        expectedEx.expect(InvalidDependencyModelException.class);
        expectedEx.expectMessage("Dependency model is missing 'topology_template' or  'node_templates' elements");
        VnfcDependencyModel model = parser.generateDependencyModel("VNF_MODEL", "VNF_TYPE");
    }

    private String jsonString = "{\"topology_template\": {" + 
            "        \"node_templates\": {" + 
            "            \"Property Definition_Template\": {" + 
            "                \"type\": \"org.onap.resource.vfc.vnf_type.abstract.nodes.property definition\"," + 
            "                \"properties\": {" + 
            "                    \"mandatory\": \"true\"," + 
            "                    \"high_availablity\": \"Active-Passive\"" + 
            "                },\"requirements\": [" + 
            "        {" + 
            "            \"dependency\": {" + 
            "                \"capability\": \"tosca.capabilities.Node\"," + 
            "                \"node\": \"tosca.nodes.Root\"," + 
            "                \"relationship\": \"tosca.relationships.DependsOn\"," + 
            "                \"occurrences\": [" + 
            "                    0," + 
            "                    \"UNBOUNDED\"" + 
            "                ]" + 
            "            }" + 
            "        }" + 
            "    ]" + 
            "            },\"tosca.nodes.Root\": {\"type\": \"VNFC_NAME\"}" + 
            "        }" + 
            "    }" + 
            "}";
}
