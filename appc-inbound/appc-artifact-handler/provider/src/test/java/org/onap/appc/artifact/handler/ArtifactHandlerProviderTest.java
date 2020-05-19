/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 Ericsson
 * ================================================================================
 * Modifications Copyright (C) 2019 IBM.
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

package org.onap.appc.artifact.handler;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.appc.artifact.handler.utils.ArtifactHandlerProviderUtil;
import org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.org.onap.appc.artifacthandler.rev170321.ArtifactHandlerService;
import org.opendaylight.yang.gen.v1.org.onap.appc.artifacthandler.rev170321.UploadartifactInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.artifacthandler.rev170321.UploadartifactInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.artifacthandler.rev170321.UploadartifactOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.artifacthandler.rev170321.document.parameters.DocumentParameters;
import org.opendaylight.yang.gen.v1.org.onap.appc.artifacthandler.rev170321.request.information.RequestInformation;
import org.opendaylight.yangtools.yang.common.RpcResult;

import com.google.common.util.concurrent.CheckedFuture;

public class ArtifactHandlerProviderTest {

    private DataBroker dataBroker = Mockito.mock(DataBroker.class);
    private RpcProviderRegistry rpcRegistry = Mockito.mock(RpcProviderRegistry.class);
    private NotificationPublishService notificationService = Mockito.mock(NotificationPublishService.class);
    private ReadWriteTransaction writeTransaction = Mockito.mock(ReadWriteTransaction.class);
    private CheckedFuture checkedFuture = Mockito.mock(CheckedFuture.class);
    private BindingAwareBroker.RpcRegistration<ArtifactHandlerService> rpcRegistration;

    private ArtifactHandlerProvider artifactHandlerProvider;

    @Before
    public void setup() {
        rpcRegistration =
                (BindingAwareBroker.RpcRegistration<ArtifactHandlerService>) Mockito.mock(BindingAwareBroker.RpcRegistration.class);
        Mockito.doReturn(writeTransaction).when(dataBroker).newReadWriteTransaction();
        Mockito.doReturn(checkedFuture).when(writeTransaction).submit();
    }


    @Test
    public void testUploadArtifactNullInput() throws InterruptedException, ExecutionException {
        artifactHandlerProvider = new ArtifactHandlerProvider(dataBroker, notificationService, rpcRegistry);
        UploadartifactInput uploadArtifactInput = Mockito.mock(UploadartifactInput.class);
        Future<RpcResult<UploadartifactOutput>> output = (Future<RpcResult<UploadartifactOutput>>) artifactHandlerProvider.uploadartifact(uploadArtifactInput);
        assertTrue(output.get().getResult() instanceof UploadartifactOutput);
    }

    @Test
    public void testUploadArtifactException() throws Exception {
        artifactHandlerProvider = Mockito.spy(new ArtifactHandlerProvider(dataBroker, notificationService, rpcRegistry));
        UploadartifactInputBuilder builder = new UploadartifactInputBuilder();
        DocumentParameters mockDocumentParameters = Mockito.mock(DocumentParameters.class);
        Mockito.doReturn("ARTIFACT CONTENTS").when(mockDocumentParameters).getArtifactContents();
        Mockito.doReturn("ARTIFACT NAME").when(mockDocumentParameters).getArtifactName();
        builder.setDocumentParameters(mockDocumentParameters);
        RequestInformation mockRequestInformation = Mockito.mock(RequestInformation.class);
        Mockito.doReturn("REQUEST ID").when(mockRequestInformation).getRequestId();
        Mockito.doReturn(SdcArtifactHandlerConstants.DESIGN_TOOL).when(mockRequestInformation).getSource();
        builder.setRequestInformation(mockRequestInformation);
        UploadartifactInput uploadArtifactInput = builder.build();
        ArtifactHandlerProviderUtil mockProvider = Mockito.mock(ArtifactHandlerProviderUtil.class);
        Mockito.doThrow(new Exception()).when(mockProvider).processTemplate(Mockito.anyString());
        Mockito.doReturn(mockProvider).when(artifactHandlerProvider).getArtifactHandlerProviderUtil(Mockito.any(UploadartifactInput.class));
        Future<RpcResult<UploadartifactOutput>> output = (Future<RpcResult<UploadartifactOutput>>) artifactHandlerProvider.uploadartifact(uploadArtifactInput);
        assertTrue(output.get().getResult() instanceof UploadartifactOutput);
    }

    @Test
    public void testUploadArtifact() throws InterruptedException, ExecutionException {
        artifactHandlerProvider = Mockito.spy(new ArtifactHandlerProvider(dataBroker, notificationService, rpcRegistry));
        UploadartifactInputBuilder builder = new UploadartifactInputBuilder();
        DocumentParameters mockDocumentParameters = Mockito.mock(DocumentParameters.class);
        Mockito.doReturn("ARTIFACT CONTENTS").when(mockDocumentParameters).getArtifactContents();
        Mockito.doReturn("ARTIFACT NAME").when(mockDocumentParameters).getArtifactName();
        builder.setDocumentParameters(mockDocumentParameters);
        RequestInformation mockRequestInformation = Mockito.mock(RequestInformation.class);
        Mockito.doReturn("REQUEST ID").when(mockRequestInformation).getRequestId();
        Mockito.doReturn(SdcArtifactHandlerConstants.DESIGN_TOOL).when(mockRequestInformation).getSource();
        builder.setRequestInformation(mockRequestInformation);
        UploadartifactInput uploadArtifactInput = builder.build();
        ArtifactHandlerProviderUtil mockProvider = Mockito.mock(ArtifactHandlerProviderUtil.class);
        Mockito.doReturn(mockProvider).when(artifactHandlerProvider).getArtifactHandlerProviderUtil(Mockito.any(UploadartifactInput.class));
        Future<RpcResult<UploadartifactOutput>> output = (Future<RpcResult<UploadartifactOutput>>) artifactHandlerProvider.uploadartifact(uploadArtifactInput);
        assertTrue(output.get().getResult() instanceof UploadartifactOutput);
    }

    @Test
    public void testUploadArtifact2() throws InterruptedException, ExecutionException {
        artifactHandlerProvider = Mockito.spy(new ArtifactHandlerProvider(dataBroker, notificationService, rpcRegistry));
        UploadartifactInputBuilder builder = new UploadartifactInputBuilder();
        DocumentParameters mockDocumentParameters = Mockito.mock(DocumentParameters.class);
        Mockito.doReturn("ARTIFACT CONTENTS").when(mockDocumentParameters).getArtifactContents();
        Mockito.doReturn("ARTIFACT NAME").when(mockDocumentParameters).getArtifactName();
        builder.setDocumentParameters(mockDocumentParameters);
        RequestInformation mockRequestInformation = Mockito.mock(RequestInformation.class);
        Mockito.doReturn("REQUEST ID").when(mockRequestInformation).getRequestId();
        builder.setRequestInformation(mockRequestInformation);
        UploadartifactInput uploadArtifactInput = builder.build();
        ArtifactHandlerProviderUtil mockProvider = Mockito.mock(ArtifactHandlerProviderUtil.class);
        Mockito.doReturn(mockProvider).when(artifactHandlerProvider).getArtifactHandlerProviderUtil(Mockito.any(UploadartifactInput.class));
        Future<RpcResult<UploadartifactOutput>> output = (Future<RpcResult<UploadartifactOutput>>) artifactHandlerProvider.uploadartifact(uploadArtifactInput);
        assertTrue(output.get().getResult() instanceof UploadartifactOutput);
    }

    @Test
    public void testUploadArtifact3() throws InterruptedException, ExecutionException {
        artifactHandlerProvider = Mockito.spy(new ArtifactHandlerProvider(dataBroker, notificationService, rpcRegistry));
        UploadartifactInputBuilder builder = new UploadartifactInputBuilder();
        DocumentParameters mockDocumentParameters = Mockito.mock(DocumentParameters.class);
        Mockito.doReturn("ARTIFACT CONTENTS").when(mockDocumentParameters).getArtifactContents();
        Mockito.doReturn("ARTIFACT NAME").when(mockDocumentParameters).getArtifactName();
        builder.setDocumentParameters(mockDocumentParameters);
        RequestInformation mockRequestInformation = Mockito.mock(RequestInformation.class);
        Mockito.doReturn("REQUEST ID").when(mockRequestInformation).getRequestId();
        Mockito.doReturn(SdcArtifactHandlerConstants.ACTION).when(mockRequestInformation).getSource();
        builder.setRequestInformation(mockRequestInformation);
        UploadartifactInput uploadArtifactInput = builder.build();
        ArtifactHandlerProviderUtil mockProvider = Mockito.mock(ArtifactHandlerProviderUtil.class);
        Mockito.doReturn(mockProvider).when(artifactHandlerProvider).getArtifactHandlerProviderUtil(Mockito.any(UploadartifactInput.class));
        Future<RpcResult<UploadartifactOutput>> output = (Future<RpcResult<UploadartifactOutput>>) artifactHandlerProvider.uploadartifact(uploadArtifactInput);
        assertTrue(output.get().getResult() instanceof UploadartifactOutput);
    }
    
    @Test
    public void testClose() throws Exception
    {
        artifactHandlerProvider = new ArtifactHandlerProvider(dataBroker, notificationService, rpcRegistry);
        artifactHandlerProvider.close();
        assertNotNull(artifactHandlerProvider);
    }
    
    @Test
    public void testGetArtifactHandlerProviderUtil() throws Exception
    {
        artifactHandlerProvider = new ArtifactHandlerProvider(dataBroker, notificationService, rpcRegistry);
        UploadartifactInput uploadArtifactInput = Mockito.mock(UploadartifactInput.class);
        assertTrue(artifactHandlerProvider.getArtifactHandlerProviderUtil(uploadArtifactInput) instanceof ArtifactHandlerProviderUtil);
    }

}
