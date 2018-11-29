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

package org.onap.sdnc.config.params.transformer.tosca;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.onap.sdnc.config.params.data.PropertyDefinition;
import org.onap.sdnc.config.params.transformer.ArtificatTransformer;
import org.onap.sdnc.config.params.transformer.tosca.exceptions.ArtifactProcessorException;

public class TestArtifactProcessor {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testArtifactProcessor() throws IOException, ArtifactProcessorException {

        ArtifactProcessor arp = ArtifactProcessorFactory.getArtifactProcessor();

        String pdString = getFileContent("tosca/ExamplePropertyDefinition.yml");
        OutputStream outstream = null;

        File tempFile = temporaryFolder.newFile("TestTosca.yml");
        outstream = new FileOutputStream(tempFile);
        arp.generateArtifact(pdString, outstream);
        outstream.flush();
        outstream.close();

        String expectedTosca = getFileContent("tosca/ExpectedTosca.yml");
        String toscaString = getFileContent(tempFile);
        Assert.assertEquals(expectedTosca, toscaString);
    }

    @Test
    public void testArtifactProcessorWithStringOutput()
            throws IOException, ArtifactProcessorException {

        ArtifactProcessor arp = ArtifactProcessorFactory.getArtifactProcessor();

        String pdString = getFileContent("tosca/ExamplePropertyDefinition.yml");
        OutputStream outstream = null;

        outstream = new ByteArrayOutputStream();
        arp.generateArtifact(pdString, outstream);
        outstream.flush();
        outstream.close();

        String expectedTosca = getFileContent("tosca/ExpectedTosca.yml");
        String toscaString = outstream.toString();
    }

    @Test
    public void testReadArtifact() throws IOException, ArtifactProcessorException {
        ArtifactProcessor arp = ArtifactProcessorFactory.getArtifactProcessor();
        String pdString = getFileContent("tosca/ExpectedTosca.yml");
        PropertyDefinition propertyDefinitionObj = arp.readArtifact(pdString);
        Assert.assertEquals(7, propertyDefinitionObj.getParameters().size());
    }

    private String getFileContent(String fileName) throws IOException {
        ClassLoader classLoader = new TestArtifactProcessor().getClass().getClassLoader();
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

    private String getFileContent(File file) throws IOException {
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
