/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Copyright (C) 2018 Ericsson
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

import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import org.onap.sdnc.config.params.data.PropertyDefinition;
import org.onap.sdnc.config.params.transformer.CommonUtility;
import org.onap.sdnc.config.params.transformer.tosca.exceptions.ArtifactProcessorException;

public class TestReadArtifact {
    @Test
    public void testReadArtifactPositive() throws ArtifactProcessorException, IOException {

        String toscaArtifact = CommonUtility.getFileContent("tosca/ReadArtifactPositiveInputTosca.yml");
        ArtifactProcessorImpl artifact = new ArtifactProcessorImpl();
        PropertyDefinition ouptPD = artifact.readArtifact(toscaArtifact);
        Assert.assertEquals(ouptPD.getKind(), "Property Definition");
        Assert.assertEquals(ouptPD.getVersion(), "V1");

        Assert.assertEquals(ouptPD.getParameters().get(0).getDefaultValue(), "0.0.0.0");
        Assert.assertEquals(ouptPD.getParameters().get(0).getName(), "abc");
        Assert.assertEquals(ouptPD.getParameters().get(0).getSource(), "source");
        Assert.assertEquals(ouptPD.getParameters().get(0).getRuleType(), "interface-ip-address");
        Assert.assertEquals(ouptPD.getParameters().get(0).getDescription(), "param_desc");
        Assert.assertEquals(ouptPD.getParameters().get(0).getType(), "param1_type");
        Assert.assertEquals(ouptPD.getParameters().get(1).getRequestKeys().get(0).getKeyName(),
                "address_fqdn");
        Assert.assertEquals(ouptPD.getParameters().get(1).getRequestKeys().get(0).getKeyValue(),
                "0");
        Assert.assertEquals(ouptPD.getParameters().get(1).getRequestKeys().get(0).getKeyName(),
                "address_fqdn");
        Assert.assertEquals(ouptPD.getParameters().get(1).getRequestKeys().get(0).getKeyValue(),
                "0");
        Assert.assertEquals(
                ouptPD.getParameters().get(0).getResponseKeys().get(0).getUniqueKeyName(),
                "address-0");
        Assert.assertEquals(
                ouptPD.getParameters().get(0).getResponseKeys().get(0).getFieldKeyName(), "0");

        Assert.assertEquals(ouptPD.getParameters().get(1).getDefaultValue(), "value");
        Assert.assertEquals(ouptPD.getParameters().get(1).getName(), "param 2");
        Assert.assertEquals(ouptPD.getParameters().get(1).getSource(), "source");
        Assert.assertEquals(ouptPD.getParameters().get(1).getRuleType(), "interface-ip-address");
        Assert.assertEquals(ouptPD.getParameters().get(1).getDescription(), "param2");
        Assert.assertEquals(ouptPD.getParameters().get(1).getType(), "param2 type");
        Assert.assertEquals(ouptPD.getParameters().get(1).getRequestKeys().get(0).getKeyName(),
                "address_fqdn");
        Assert.assertEquals(ouptPD.getParameters().get(1).getRequestKeys().get(0).getKeyValue(),
                "0");
        Assert.assertEquals(ouptPD.getParameters().get(1).getRequestKeys().get(1).getKeyName(),
                "address_type");
        Assert.assertEquals(ouptPD.getParameters().get(1).getRequestKeys().get(1).getKeyValue(),
                "v4");
        Assert.assertEquals(
                ouptPD.getParameters().get(1).getResponseKeys().get(0).getUniqueKeyName(),
                "address-0");
        Assert.assertEquals(
                ouptPD.getParameters().get(1).getResponseKeys().get(0).getUniqueKeyValue(), "0");
        Assert.assertEquals(
                ouptPD.getParameters().get(1).getResponseKeys().get(0).getFieldKeyName(), "0");

    }

    @Test
    public void testReadArtifactNegetive() throws IOException {

        String toscaArtifact = CommonUtility.getFileContent("tosca/ReadArtifactNegetiveInputTosca.yml");
        ArtifactProcessorImpl artifact = new ArtifactProcessorImpl();
        try {
            PropertyDefinition ouptPD = artifact.readArtifact(toscaArtifact);
        } catch (ArtifactProcessorException e) {
            Assert.assertNotNull(e);
            Assert.assertEquals(e.getMessage(),
                    "Invalid input found <> source1 <reqk1:reqv1 , reqk2:reqv2>");
        }
    }

}
