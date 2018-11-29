/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
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

package org.onap.sdnc.config.params.transformer;

import java.io.FileInputStream;
import java.io.IOException;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.onap.sdnc.config.params.transformer.ArtificatTransformer;
import org.onap.sdnc.config.params.transformer.tosca.TestGenerateArtifactString;

public class TestArtifactTransformer {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testArtifactTransformerYAMLtoJSON() throws IOException {
        ArtificatTransformer transformer = new ArtificatTransformer();
        String pdString = CommonUtility.getFileContent(new FileInputStream(new TestGenerateArtifactString().getClass()
                .getClassLoader().getResource("tosca/ExamplePropertyDefinition.yml").getFile()));
        String output = transformer.transformYamlToJson(pdString);
        Assert.assertThat(output, CoreMatchers.containsString("response-keys"));
    }
}
