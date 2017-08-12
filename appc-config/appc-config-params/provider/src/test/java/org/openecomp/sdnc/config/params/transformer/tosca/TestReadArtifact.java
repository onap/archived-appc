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

//import static org.junit.Assert;
import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdnc.config.params.data.PropertyDefinition;
import org.openecomp.sdnc.config.params.transformer.tosca.exceptions.ArtifactProcessorException;

import java.io.*;


/**
 * @author thakkerp
 * @since March 23,2017
 */
public class TestReadArtifact {
    @Test
    public void testReadArtifactPositive() throws ArtifactProcessorException, IOException {

        String toscaArtifact = getFileContent("tosca/ReadArtifactPositiveInputTosca.yml");
        ArtifactProcessorImpl artifact = new ArtifactProcessorImpl();
        PropertyDefinition ouptPD = artifact.readArtifact(toscaArtifact);
        Assert.assertEquals(ouptPD.getKind(),"Property Definition");
        Assert.assertEquals(ouptPD.getVersion(),"V1");

        Assert.assertEquals(ouptPD.getParameters().get(0).getDefaultValue(),"0.0.0.0");
        Assert.assertEquals(ouptPD.getParameters().get(0).getName(),"abc");
        Assert.assertEquals(ouptPD.getParameters().get(0).getSource(),"INSTAR");
        Assert.assertEquals(ouptPD.getParameters().get(0).getRuleType(),"interface-ip-address");
        Assert.assertEquals(ouptPD.getParameters().get(0).getDescription(),"param_desc");
        Assert.assertEquals(ouptPD.getParameters().get(0).getType(),"param1_type");
        Assert.assertEquals(ouptPD.getParameters().get(1).getRequestKeys().get(0).getKeyName(),"address_fqdn");
        Assert.assertEquals(ouptPD.getParameters().get(1).getRequestKeys().get(0).getKeyValue(),"000000000");
        Assert.assertEquals(ouptPD.getParameters().get(1).getRequestKeys().get(0).getKeyName(),"address_fqdn");
        Assert.assertEquals(ouptPD.getParameters().get(1).getRequestKeys().get(0).getKeyValue(),"000000000");
        Assert.assertEquals(ouptPD.getParameters().get(0).getResponseKeys().get(0).getUniqueKeyName(),"address-fqdn");
        //Assert.assertEquals(ouptPD.getParameters().get(0).getResponseKeys().get(0).getUniqueKeyValue(),"000000000");
        Assert.assertEquals(ouptPD.getParameters().get(0).getResponseKeys().get(0).getFieldKeyName(),"ipaddress-v4");

        Assert.assertEquals(ouptPD.getParameters().get(1).getDefaultValue(),"0:0:0:0:0:0:0:0");
        Assert.assertEquals(ouptPD.getParameters().get(1).getName(),"param 2");
        Assert.assertEquals(ouptPD.getParameters().get(1).getSource(),"INSTAR");
        Assert.assertEquals(ouptPD.getParameters().get(1).getRuleType(),"interface-ip-address");
        Assert.assertEquals(ouptPD.getParameters().get(1).getDescription(),"param2");
        Assert.assertEquals(ouptPD.getParameters().get(1).getType(),"param2 type");
        Assert.assertEquals(ouptPD.getParameters().get(1).getRequestKeys().get(0).getKeyName(),"address_fqdn");
        Assert.assertEquals(ouptPD.getParameters().get(1).getRequestKeys().get(0).getKeyValue(),"000000000");
        Assert.assertEquals(ouptPD.getParameters().get(1).getRequestKeys().get(1).getKeyName(),"address_type");
        Assert.assertEquals(ouptPD.getParameters().get(1).getRequestKeys().get(1).getKeyValue(),"v4");
        Assert.assertEquals(ouptPD.getParameters().get(1).getResponseKeys().get(0).getUniqueKeyName(),"address-fqdn");
        Assert.assertEquals(ouptPD.getParameters().get(1).getResponseKeys().get(0).getUniqueKeyValue(),"000000000");
        Assert.assertEquals(ouptPD.getParameters().get(1).getResponseKeys().get(0).getFieldKeyName(),"ipaddress-v4");

    }
@Test
    public void testReadArtifactNegetive() throws IOException {

        String toscaArtifact = getFileContent("tosca/ReadArtifactNegetiveInputTosca.yml");
        ArtifactProcessorImpl artifact = new ArtifactProcessorImpl();
        try {
            PropertyDefinition ouptPD = artifact.readArtifact(toscaArtifact);
        } catch (ArtifactProcessorException e) {
            Assert.assertNotNull(e);
            Assert.assertEquals(e.getMessage(),"Invalid input found <> source1 <reqk1:reqv1 , reqk2:reqv2>");
        }
    }

    private String getFileContent(String fileName) throws IOException
    {
        ClassLoader classLoader = new  TestReadArtifact().getClass().getClassLoader();
        InputStream is = new FileInputStream(classLoader.getResource(fileName).getFile());
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
