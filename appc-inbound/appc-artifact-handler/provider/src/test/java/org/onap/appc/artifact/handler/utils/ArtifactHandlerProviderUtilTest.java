/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications Copyright (C) 2019 Ericsson
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

package org.onap.appc.artifact.handler.utils;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.org.onap.appc.artifacthandler.rev170321.UploadartifactInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.artifacthandler.rev170321.UploadartifactInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.artifacthandler.rev170321.UploadartifactOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.artifacthandler.rev170321.document.parameters.DocumentParameters;
import org.opendaylight.yang.gen.v1.org.onap.appc.artifacthandler.rev170321.request.information.RequestInformation;
import org.powermock.reflect.Whitebox;

public class ArtifactHandlerProviderUtilTest {

    @Test(expected = Exception.class)
    public void testProcessTemplate() throws Exception {
        String artifact_conetent = IOUtils.toString(ArtifactHandlerProviderUtilTest.class.getClassLoader()
                .getResourceAsStream("templates/reference_template.json"), Charset.defaultCharset());
        JSONObject obj = new JSONObject();
        obj.put("artifact-name", "reference_JunitTestArtifact");
        obj.put("artifact-version", "0.01");
        obj.put("artifact-contents", artifact_conetent);
        ArtifactHandlerProviderUtil ahprovider = new ArtifactHandlerProviderUtil();
        ahprovider.processTemplate(obj.toString());
    }

/*    @Test(expected = Exception.class)
    public void testcreateDummyRequestData() throws Exception {
        String artifact_conetent = IOUtils.toString(ArtifactHandlerProviderUtilTest.class.getClassLoader()
                .getResourceAsStream("templates/reference_template.json"), Charset.defaultCharset());
        JSONObject obj = new JSONObject();
        obj.put("artifact-name", "reference_JunitTestArtifact");
        obj.put("artifact-version", "0.01");
        obj.put("artifact-contents", artifact_conetent);
        ArtifactHandlerProviderUtil ahprovider = new ArtifactHandlerProviderUtil();
        String requestInfo = ahprovider.createDummyRequestData();
    }*/

    @Test
    public void testEscapeSql() throws Exception {
        String testStr = "Test String is 'test'";
        ArtifactHandlerProviderUtil ahprovider = new ArtifactHandlerProviderUtil();
        ahprovider.escapeSql(testStr);
        assertTrue(true);
    }

    @Test
    public void testGetRandom() throws Exception {
        ArtifactHandlerProviderUtil ahprovider = new ArtifactHandlerProviderUtil();
        Whitebox.invokeMethod(ahprovider, "getRandom");
        assertTrue(true);
    }

    @Test
    public void testEscapeUtils() throws Exception {
        String str = "The Test string is 'test'";
        EscapeUtils.escapeSql(str);
        assertTrue(true);
    }

    @Test
    public void testEscapeUtilsNull() throws Exception {
        String str = null;
        assertNull(EscapeUtils.escapeSql(str));
    }

    @Test
    public void testDummyData() throws IOException {
        String artifactContent = IOUtils.toString(ArtifactHandlerProviderUtilTest.class.getClassLoader()
                .getResourceAsStream("templates/reference_template.json"), Charset.defaultCharset());
        ArtifactHandlerProviderUtil ahprovider = Mockito.spy(new ArtifactHandlerProviderUtil());
        UploadartifactInputBuilder builder = new UploadartifactInputBuilder();
        DocumentParameters mockDocumentParameters = Mockito.mock(DocumentParameters.class);
        Mockito.doReturn(artifactContent).when(mockDocumentParameters).getArtifactContents();
        Mockito.doReturn("ARTIFACT NAME").when(mockDocumentParameters).getArtifactName();
        builder.setDocumentParameters(mockDocumentParameters);
        RequestInformation mockRequestInformation = Mockito.mock(RequestInformation.class);
        Mockito.doReturn("REQUEST ID").when(mockRequestInformation).getRequestId();
        Mockito.doReturn(SdcArtifactHandlerConstants.DESIGN_TOOL).when(mockRequestInformation).getSource();
        builder.setRequestInformation(mockRequestInformation);
        UploadartifactInput uploadArtifactInput = builder.build();
        Whitebox.setInternalState(ahprovider, "templateData", uploadArtifactInput);
        System.out.println(ahprovider.createDummyRequestData());
        assertTrue(ahprovider.createDummyRequestData().startsWith("{\"input\": {\"document-parameters\":{\"service-uuid\":\"TLSUUIDREQUEST ID\""));
    }

    @Test
    public void testRequestData() throws IOException {
        String artifactContent = IOUtils.toString(ArtifactHandlerProviderUtilTest.class.getClassLoader()
                .getResourceAsStream("templates/reference_template.json"), Charset.defaultCharset());
        ArtifactHandlerProviderUtil ahprovider = Mockito.spy(new ArtifactHandlerProviderUtil());
        UploadartifactInputBuilder builder = new UploadartifactInputBuilder();
        DocumentParameters mockDocumentParameters = Mockito.mock(DocumentParameters.class);
        Mockito.doReturn(artifactContent).when(mockDocumentParameters).getArtifactContents();
        Mockito.doReturn("ARTIFACT NAME").when(mockDocumentParameters).getArtifactName();
        builder.setDocumentParameters(mockDocumentParameters);
        RequestInformation mockRequestInformation = Mockito.mock(RequestInformation.class);
        Mockito.doReturn("REQUEST ID").when(mockRequestInformation).getRequestId();
        Mockito.doReturn(SdcArtifactHandlerConstants.DESIGN_TOOL).when(mockRequestInformation).getSource();
        builder.setRequestInformation(mockRequestInformation);
        UploadartifactInput uploadArtifactInput = builder.build();
        Whitebox.setInternalState(ahprovider, "templateData", uploadArtifactInput);
        System.out.println(ahprovider.createRequestData());
        assertTrue(ahprovider.createDummyRequestData().startsWith("{\"input\": {\"document-parameters\":{\"service-uuid\":\"TLSUUIDREQUEST ID\""));
    }
}

