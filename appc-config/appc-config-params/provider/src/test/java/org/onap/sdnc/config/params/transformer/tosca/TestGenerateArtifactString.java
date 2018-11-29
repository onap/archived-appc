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
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.onap.sdnc.config.params.transformer.CommonUtility;
import org.onap.sdnc.config.params.transformer.tosca.exceptions.ArtifactProcessorException;

public class TestGenerateArtifactString {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testStringArtifactGenerator() throws IOException, ArtifactProcessorException {

        String pdString = CommonUtility.getFileContent(new FileInputStream(new TestGenerateArtifactString().getClass()
                .getClassLoader().getResource("tosca/ExamplePropertyDefinition.yml").getFile()));
        OutputStream outstream = null;

        File tempFile = temporaryFolder.newFile("TestTosca.yml");
        outstream = new FileOutputStream(tempFile);
        ArtifactProcessorImpl arp = new ArtifactProcessorImpl();
        arp.generateArtifact(pdString, outstream);
        outstream.flush();
        outstream.close();

        String expectedTosca = CommonUtility.getFileContent(new FileInputStream(new TestGenerateArtifactString().getClass()
                .getClassLoader().getResource("tosca/ExpectedTosca.yml").getFile()));
        String toscaString = CommonUtility.getFileContent(new FileInputStream(tempFile));
        Assert.assertEquals(expectedTosca, toscaString);

    }

    @Test
    public void testArtifactGeneratorWithParameterNameBlank()
            throws IOException, ArtifactProcessorException {

        String pdString = CommonUtility.getFileContent(new FileInputStream(new TestGenerateArtifactString().getClass()
                .getClassLoader().getResource("tosca/ExamplePropertyDefinition2.yml").getFile()));
        OutputStream outstream = null;
        String expectedMsg = "Parameter name is empty,null or contains whitespace";

        File tempFile = temporaryFolder.newFile("TestTosca.yml");
        outstream = new FileOutputStream(tempFile);
        ArtifactProcessorImpl arp = new ArtifactProcessorImpl();
        try {
            arp.generateArtifact(pdString, outstream);
        } catch (ArtifactProcessorException e) {
            Assert.assertEquals(expectedMsg, e.getMessage());
        }
        outstream.flush();
        outstream.close();
    }

    @Test
    public void testArtifactGeneratorWithParameterNameNull()
            throws IOException, ArtifactProcessorException {

        String pdString = CommonUtility.getFileContent(new FileInputStream(new TestGenerateArtifactString().getClass()
                .getClassLoader().getResource("tosca/ExamplePropertyDefinition3.yml").getFile()));
        OutputStream outstream = null;
        String expectedMsg = "Parameter name is empty,null or contains whitespace";

        File tempFile = temporaryFolder.newFile("TestTosca.yml");
        outstream = new FileOutputStream(tempFile);
        ArtifactProcessorImpl arp = new ArtifactProcessorImpl();
        try {
            arp.generateArtifact(pdString, outstream);
        } catch (ArtifactProcessorException e) {
            Assert.assertEquals(expectedMsg, e.getMessage());
        }
        outstream.flush();
        outstream.close();
    }

    @Test
    public void testArtifactGeneratorWithKindNull() throws IOException, ArtifactProcessorException {

        String pdString = CommonUtility.getFileContent(new FileInputStream(new TestGenerateArtifactString().getClass()
                .getClassLoader().getResource("tosca/ExamplePropertyDefinition4.yml").getFile()));
        OutputStream outstream = null;
        String expectedMsg = "Kind in PropertyDefinition is blank or null";

        File tempFile = temporaryFolder.newFile("TestTosca.yml");
        outstream = new FileOutputStream(tempFile);
        ArtifactProcessorImpl arp = new ArtifactProcessorImpl();
        try {
            arp.generateArtifact(pdString, outstream);
        } catch (ArtifactProcessorException e) {
            Assert.assertEquals(expectedMsg, e.getMessage());
        }
        outstream.flush();
        outstream.close();
    }
}
