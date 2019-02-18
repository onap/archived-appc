/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 Ericsson
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
 *
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.design.services.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Properties;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DesignServiceConstants.class})
public class ArtifactHandlerClientTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void setup() throws NoSuchAlgorithmException, KeyManagementException {
        PowerMockito.mockStatic(DesignServiceConstants.class);
    }

    @Test
    public void testConstructorException() throws IOException {
        expectedEx.expect(IOException.class);
        new ArtifactHandlerClient();
    }

    @Test
    public void testConstructor() throws IOException {
        PowerMockito.when(DesignServiceConstants.getEnvironmentVariable(ArtifactHandlerClient.SDNC_CONFIG_DIR_VAR))
            .thenReturn("src/test/resources");
        ArtifactHandlerClient client = new ArtifactHandlerClient();
        assertTrue(client instanceof ArtifactHandlerClient);
        Properties props = Whitebox.getInternalState(client, "props");
        assertTrue(props.containsKey("appc.upload.provider.url"));
    }

    @Test
    public void testCreateArtifactData() throws IOException {
        PowerMockito.when(DesignServiceConstants.getEnvironmentVariable(ArtifactHandlerClient.SDNC_CONFIG_DIR_VAR))
            .thenReturn("src/test/resources");
        ArtifactHandlerClient client = new ArtifactHandlerClient();
        assertEquals("{\"input\": {\"request-information\":{\"request-id\":\"\",\"request-action\":"
                + "\"StoreSdcDocumentRequest\",\"source\":\"Design-tool\"},\"document-parameters\":"
                + "{\"artifact-version\":\"TEST\",\"artifact-name\":\"TEST\",\"artifact-contents\":"
                + "\"TEST\"}}}", client.createArtifactData("{\"" + DesignServiceConstants.ARTIFACT_NAME
                + "\":\"TEST\", \"" + DesignServiceConstants.ARTIFACT_VERSOIN + "\":\"TEST\", \"" +
                DesignServiceConstants.ARTIFACT_CONTENTS + "\":\"TEST\"}", ""));
    }
}
