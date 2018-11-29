/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications Copyright (C) 2018 Ericsson
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

package org.onap.sdnc.config.params.transformer.tosca;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.onap.sdnc.config.params.data.Parameter;
import org.onap.sdnc.config.params.data.PropertyDefinition;
import org.onap.sdnc.config.params.data.RequestKey;
import org.onap.sdnc.config.params.data.ResponseKey;
import org.onap.sdnc.config.params.transformer.tosca.exceptions.ArtifactProcessorException;

public class TestGenerateArtifactObject {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testObjectArtifactProcessor() throws IOException, ArtifactProcessorException {

        String expectedTosca = "node_types:\n" + "  VNF:\n"
                + "    derived_from: org.openecomp.genericvnf\n" + "    version: V1\n"
                + "    description: ''\n" + "    properties:\n" + "      LOCAL_ACCESS_IP_ADDR:\n"
                + "        type: string\n" + "        required: false\n"
                + "        default: 0.0.0.0\n" + "        status: SUPPORTED\n"
                + "      LOCAL_CORE_ALT_IP_ADDR:\n" + "        type: String\n"
                + "        required: false\n" + "        default: value\n"
                + "        status: SUPPORTED\n" + "topology_template:\n" + "  node_templates:\n"
                + "    VNF_Template:\n" + "      type: VNF\n" + "      properties:\n"
                + "        LOCAL_ACCESS_IP_ADDR: <rule-type = myRule1> <response-keys = > <source-system = source> <request-keys = class-type:interface-ip-address , address_fqdn:someVal , address_type:v4>\n"
                + "        LOCAL_CORE_ALT_IP_ADDR: <rule-type = myRule2> <response-keys = name1:value1:field1> <source-system = source> <request-keys = >\n";
        // Create object
        PropertyDefinition pd = new PropertyDefinition();
        pd.setKind("VNF");
        pd.setVersion("V1");
        pd.setParameters(createParameters());

        // Call ArtifactProcessor
        OutputStream outstream = null;

        File toscaFile = temporaryFolder.newFile("TestTosca.yml");
        outstream = new FileOutputStream(toscaFile);
        ArtifactProcessorImpl arp = new ArtifactProcessorImpl();
        arp.generateArtifact(pd, outstream);
        outstream.flush();
        outstream.close();

        String toscaString = FileUtils.readFileToString(toscaFile, StandardCharsets.UTF_8);
        Assert.assertEquals(expectedTosca, toscaString);

    }

    @Test
    public void testPDpropertiesSetNull() throws IOException, ArtifactProcessorException {
        String expectedTosca = "node_types:\n" + "  PropertyDefinition:\n"
                + "    derived_from: org.openecomp.genericvnf\n" + "    version: V1\n"
                + "    description: ''\n" + "topology_template:\n" + "  node_templates:\n"
                + "    PropertyDefinition_Template:\n" + "      type: PropertyDefinition\n";
        // Create object
        PropertyDefinition pd = new PropertyDefinition();
        pd.setKind("PropertyDefinition");
        pd.setVersion("V1");
        OutputStream outstream = null;

        File toscaFile = temporaryFolder.newFile("TestTosca.yml");
        outstream = new FileOutputStream(toscaFile);

        ArtifactProcessorImpl arp = new ArtifactProcessorImpl();
        arp.generateArtifact(pd, outstream);
        outstream.flush();
        outstream.close();

        String toscaString = FileUtils.readFileToString(toscaFile, StandardCharsets.UTF_8);

        Assert.assertEquals(expectedTosca, toscaString);
    }

    @Test
    public void testArtifactGeneratorInvalidStream() throws IOException {
        String expectedMsg = "java.io.IOException: Stream Closed";
        PropertyDefinition pd = new PropertyDefinition();
        pd.setKind("VNF");
        pd.setVersion("V1");
        pd.setParameters(createParameters());

        // Call ArtifactProcessor
        OutputStream outstream = null;
        try {
            File toscaFile = temporaryFolder.newFile("TestTosca.yml");
            outstream = new FileOutputStream(toscaFile);
            outstream.close();
            ArtifactProcessorImpl arp = new ArtifactProcessorImpl();
            arp.generateArtifact(pd, outstream);
            Assert.fail();
        } catch (ArtifactProcessorException e) {
            Assert.assertEquals(expectedMsg, e.getMessage());
        }
    }

    private List<Parameter> createParameters() {
        // Create single Parameter object 1
        Parameter singleParameter1 = new Parameter();
        singleParameter1.setName("LOCAL_ACCESS_IP_ADDR");
        singleParameter1.setRequired(false);
        singleParameter1.setSource("source");
        singleParameter1.setDefaultValue("0.0.0.0");
        singleParameter1.setRuleType("myRule1");
        singleParameter1.setRequestKeys(createRequestKeys());

        // Create single Parameter object 2
        Parameter singleParameter2 = new Parameter();
        singleParameter2.setName("LOCAL_CORE_ALT_IP_ADDR");
        singleParameter2.setType("String");
        singleParameter2.setRequired(false);
        singleParameter2.setSource("source");
        singleParameter2.setDefaultValue("value");
        singleParameter2.setRuleType("myRule2");
        singleParameter2.setResponseKeys(createResponseKeys());

        // Add the Parameter objects to the List
        List<Parameter> parameterList = new ArrayList<Parameter>();
        parameterList.add(singleParameter1);
        parameterList.add(singleParameter2);
        return parameterList;
    }

    private List<RequestKey> createRequestKeys() {
        // Create RequestKey object 1
        RequestKey requestKey1 = new RequestKey();
        requestKey1.setKeyName("class-type");
        requestKey1.setKeyValue("interface-ip-address");

        // Create RequestKey object 2
        RequestKey requestKey2 = new RequestKey();
        requestKey2.setKeyName("address_fqdn");
        requestKey2.setKeyValue("someVal");

        // Create RequestKey object 3
        RequestKey requestKey3 = new RequestKey();
        requestKey3.setKeyName("address_type");
        requestKey3.setKeyValue("v4");

        // Add the RequestKey Objects to the List
        List<RequestKey> requestKeyList = new ArrayList<RequestKey>();
        requestKeyList.add(requestKey1);
        requestKeyList.add(requestKey2);
        requestKeyList.add(requestKey3);
        return requestKeyList;
    }

    private List<ResponseKey> createResponseKeys() {
        // Create RequestKey object 1
        ResponseKey responseKey1 = new ResponseKey();

        responseKey1.setUniqueKeyName("name1");
        responseKey1.setUniqueKeyValue("value1");
        responseKey1.setFieldKeyName("field1");

        // Add the RequestKey Objects to the List
        List<ResponseKey> responseKeyList = new ArrayList<ResponseKey>();
        responseKeyList.add(responseKey1);

        return responseKeyList;
    }

    @Test(expected = Exception.class)
    public void testPDnull() throws IOException, ArtifactProcessorException {
        PropertyDefinition pd = null;
        OutputStream outstream = null;

        outstream = new FileOutputStream(".\\TestTosca.yml");
        ArtifactProcessorImpl arp = new ArtifactProcessorImpl();
        arp.generateArtifact(pd, outstream);
        outstream.flush();
        outstream.close();
    }
}
