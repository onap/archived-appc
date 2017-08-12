/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * 
 *  ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdnc.config.params.transformer.tosca;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openecomp.sdnc.config.params.data.Parameter;
import org.openecomp.sdnc.config.params.data.PropertyDefinition;
import org.openecomp.sdnc.config.params.data.RequestKey;
import org.openecomp.sdnc.config.params.data.ResponseKey;
import org.openecomp.sdnc.config.params.transformer.tosca.exceptions.ArtifactProcessorException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by pranavdi on 3/15/2017.
 */
public class TestGenerateArtifactObject
{
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testObjectArtifactProcessor() throws IOException, ArtifactProcessorException {

        String expectedTosca="node_types:\n" +
                "  VNF:\n" +
                "    derived_from: org.openecomp.genericvnf\n" +
                "    version: V1\n" +
                "    description: ''\n" +
                "    properties:\n" +
                "      LOCAL_ACCESS_IP_ADDR:\n" +
                "        type: string\n" +
                "        required: false\n" +
                "        default: 192.168.30.1\n" +
                "        status: SUPPORTED\n" +
                "      LOCAL_CORE_ALT_IP_ADDR:\n" +
                "        type: String\n" +
                "        required: false\n" +
                "        default: fd00:f4d5:ea06:1:0:110:254\n" +
                "        status: SUPPORTED\n" +
                "topology_template:\n" +
                "  node_templates:\n" +
                "    VNF_Template:\n" +
                "      type: VNF\n" +
                "      properties:\n" +
                "        LOCAL_ACCESS_IP_ADDR: <rule-type = myRule1> <response-keys = > <source-system = INSTAR> <request-keys = class-type:interface-ip-address , address_fqdn:someVal , address_type:v4>\n" +
                "        LOCAL_CORE_ALT_IP_ADDR: <rule-type = myRule2> <response-keys = name1:value1:field1> <source-system = INSTAR> <request-keys = >\n";
        //Create object
        PropertyDefinition pd = new PropertyDefinition();
        pd.setKind("VNF");
        pd.setVersion("V1");
        pd.setParameters(createParameters());

        //Call ArtifactProcessor
        OutputStream outstream=null;

        File toscaFile =temporaryFolder.newFile("TestTosca.yml");
        outstream = new FileOutputStream(toscaFile);
        ArtifactProcessorImpl arp = new ArtifactProcessorImpl();
        arp.generateArtifact(pd,outstream);
        outstream.flush();
        outstream.close();

        String toscaString = getFileContent(toscaFile);
        Assert.assertEquals(expectedTosca,toscaString);

    }

    @Test
    public void testPDpropertiesSetNull() throws IOException, ArtifactProcessorException {
        String expectedTosca = "node_types:\n" +
                "  PropertyDefinition:\n" +
                "    derived_from: org.openecomp.genericvnf\n" +
                "    version: V1\n" +
                "    description: ''\n" +
                "topology_template:\n" +
                "  node_templates:\n" +
                "    PropertyDefinition_Template:\n" +
                "      type: PropertyDefinition\n";
        //Create object
        PropertyDefinition pd = new PropertyDefinition();
        pd.setKind("PropertyDefinition");
        pd.setVersion("V1");
//        pd.setParameters(createParameters());

        //Call ArtifactProcessor
        OutputStream outstream=null;

        File toscaFile =temporaryFolder.newFile("TestTosca.yml");
        outstream = new FileOutputStream(toscaFile);

        ArtifactProcessorImpl arp = new ArtifactProcessorImpl();
        arp.generateArtifact(pd,outstream);
        outstream.flush();
        outstream.close();

        String toscaString = getFileContent(toscaFile);
        Assert.assertEquals(expectedTosca,toscaString);
    }

    @Test
    public void testArtifactGeneratorInvalidStream() throws IOException {
        String expectedMsg = "java.io.IOException: Stream Closed";
        PropertyDefinition pd = new PropertyDefinition();
        pd.setKind("VNF");
        pd.setVersion("V1");
        pd.setParameters(createParameters());

        //Call ArtifactProcessor
        OutputStream outstream=null;
        try {
            File toscaFile =temporaryFolder.newFile("TestTosca.yml");
            outstream = new FileOutputStream(toscaFile);
            outstream.close();
            ArtifactProcessorImpl arp = new ArtifactProcessorImpl();
            arp.generateArtifact(pd,outstream);
            Assert.fail();
        }
        catch (ArtifactProcessorException e)
        {
            Assert.assertEquals(expectedMsg,e.getMessage());
        }
    }

    private List<Parameter> createParameters()
    {
        //Create single Parameter object 1
        Parameter singleParameter1 = new Parameter();
        singleParameter1.setName("LOCAL_ACCESS_IP_ADDR");
//        singleParameter1.setList(false);
        singleParameter1.setRequired(false);
        singleParameter1.setSource("INSTAR");
        singleParameter1.setDefaultValue("192.168.30.1");
        singleParameter1.setRuleType("myRule1");
        singleParameter1.setRequestKeys(createRequestKeys());

        //Create single Parameter object 2
        Parameter singleParameter2 = new Parameter();
        singleParameter2.setName("LOCAL_CORE_ALT_IP_ADDR");
        singleParameter2.setType("String");
//        singleParameter2.setList(false);
        singleParameter2.setRequired(false);
        singleParameter2.setSource("INSTAR");
        singleParameter2.setDefaultValue("fd00:f4d5:ea06:1:0:110:254");
        singleParameter2.setRuleType("myRule2");
        singleParameter2.setResponseKeys(createResponseKeys());


        //Add the Parameter objects to the List
        List<Parameter> parameterList = new ArrayList<Parameter>();
        parameterList.add(singleParameter1);
        parameterList.add(singleParameter2);
        return parameterList;
    }

    private List<RequestKey> createRequestKeys()
    {
        //Create RequestKey object 1
        RequestKey requestKey1 = new RequestKey();
        requestKey1.setKeyName("class-type");
        requestKey1.setKeyValue("interface-ip-address");

        //Create RequestKey object 2
        RequestKey requestKey2 = new RequestKey();
        requestKey2.setKeyName("address_fqdn");
        requestKey2.setKeyValue("someVal");

        //Create RequestKey object 3
        RequestKey requestKey3 = new RequestKey();
        requestKey3.setKeyName("address_type");
        requestKey3.setKeyValue("v4");

        //Add the RequestKey Objects to the List
        List<RequestKey> requestKeyList = new ArrayList<RequestKey>();
        requestKeyList.add(requestKey1);
        requestKeyList.add(requestKey2);
        requestKeyList.add(requestKey3);
        return  requestKeyList;
    }

    private List<ResponseKey> createResponseKeys()
    {
        //Create RequestKey object 1
        ResponseKey responseKey1 = new ResponseKey();

        responseKey1.setUniqueKeyName("name1");
        responseKey1.setUniqueKeyValue("value1");
        responseKey1.setFieldKeyName("field1");

        //Add the RequestKey Objects to the List
        List<ResponseKey> responseKeyList = new ArrayList<ResponseKey>();
        responseKeyList.add(responseKey1);

        return  responseKeyList;
    }

    private Parameter createParameter()
    {
        Parameter singleParameter1 = new Parameter();
        singleParameter1.setName("LOCAL_ACCESS_IP_ADDR");
        //singleParameter1.setList(false);
        singleParameter1.setRequired(false);
        singleParameter1.setSource("INSTAR");
        singleParameter1.setDefaultValue("192.168.30.1");
        singleParameter1.setRequestKeys(createRequestKeys());
        singleParameter1.setResponseKeys(createResponseKeys());
        return singleParameter1;
    }

    //@Test
    public void testPDnull() throws IOException, ArtifactProcessorException {
        PropertyDefinition pd = null;
        OutputStream outstream=null;

        outstream = new FileOutputStream(".\\TestTosca.yml");
        ArtifactProcessorImpl arp = new ArtifactProcessorImpl();
        arp.generateArtifact(pd,outstream);
        outstream.flush();
        outstream.close();


    }

    private String getFileContent(File file) throws IOException
    {
        InputStream is = new FileInputStream(file);
        BufferedReader buf = new BufferedReader(new InputStreamReader(is));
        String line = buf.readLine();
        StringBuilder sb = new StringBuilder();

        while (line != null) {
            sb.append(line).append("\n");
            line = buf.readLine();
        }
        String fileString = sb.toString();
        is.close();
        return fileString;
    }
}
