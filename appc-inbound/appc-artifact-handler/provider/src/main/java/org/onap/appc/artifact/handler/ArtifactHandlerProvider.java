/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.appc.artifact.handler;

import org.onap.appc.artifact.handler.utils.ArtifactHandlerProviderUtil;
import org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.org.onap.appc.artifacthandler.rev170321.ArtifactHandlerService;
import org.opendaylight.yang.gen.v1.org.onap.appc.artifacthandler.rev170321.UploadartifactInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.artifacthandler.rev170321.UploadartifactInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.artifacthandler.rev170321.UploadartifactOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.artifacthandler.rev170321.UploadartifactOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.artifacthandler.rev170321.uploadartifact.output.ConfigDocumentResponseBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

public class ArtifactHandlerProvider implements AutoCloseable, ArtifactHandlerService {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(ArtifactHandlerProvider.class);
    private final String appName = "ArtifactsHandler";
    protected DataBroker dataBroker;
    protected NotificationPublishService notificationService;
    protected RpcProviderRegistry rpcRegistry;
    
    public ArtifactHandlerProvider(DataBroker dataBroker2, NotificationPublishService notificationProviderService,
            RpcProviderRegistry rpcProviderRegistry) {
        this.log.info("Creating provider for " + appName);
        dataBroker = dataBroker2;
        notificationService = notificationProviderService;
        rpcRegistry = rpcProviderRegistry;
        initialize();
    }
    
    public void initialize() {
        log.info("Initializing provider for " + appName);
        try {
            ArtifactHandlerProviderUtil.loadProperties();
        } catch (Exception e) {
            log.error("Caught exception while trying to load properties file", e);
        }
        log.info("Initialization complete for " + appName);
    }

    private RpcResult<UploadartifactOutput> buildResponse1(
            String svcRequestId,
            String topic,
            String code,
            String message,
            String finalInd) {

        UploadartifactOutputBuilder responseBuilder = new UploadartifactOutputBuilder();
        ConfigDocumentResponseBuilder configResponseBuilder=new ConfigDocumentResponseBuilder();
        configResponseBuilder.setRequestId(svcRequestId);
        configResponseBuilder.setStatus(code);
        configResponseBuilder.setErrorReason(message);
        RpcResult<UploadartifactOutput> rpcResult = RpcResultBuilder.<UploadartifactOutput> status(true)
                .withResult(responseBuilder.build()).build();
        return rpcResult;
    }
    @Override
    public ListenableFuture<RpcResult<UploadartifactOutput>> uploadartifact(UploadartifactInput input) {

        if (input == null || input.getDocumentParameters() == null || input.getDocumentParameters().getArtifactContents() == null ) {
            RpcResult<UploadartifactOutput> rpcResult =
                    buildResponse1("N/A", "N/A", "INVALID_INPUT", "Invalid input, null or empty document information" , "Y");
            return Futures.immediateFuture(rpcResult);
        }
        UploadartifactInputBuilder inputBuilder = new UploadartifactInputBuilder(input);
        ConfigDocumentResponseBuilder configResponseBuilder = new ConfigDocumentResponseBuilder();
        UploadartifactOutputBuilder responseBuilder = new UploadartifactOutputBuilder();
        log.info("Received input = " + input );
        ArtifactHandlerProviderUtil designUtil = getArtifactHandlerProviderUtil(input);
        configResponseBuilder.setRequestId(input.getRequestInformation().getRequestId());
        try{
            if(input.getRequestInformation().getSource() !=null){
                if(input.getRequestInformation().getSource().equalsIgnoreCase(SdcArtifactHandlerConstants.DESIGN_TOOL)){
                        designUtil.processTemplate(designUtil.createDummyRequestData());
                        configResponseBuilder.setStatus(ArtifactHandlerProviderUtil.DistributionStatusEnum.DEPLOY_OK.toString());
                }
                else
                {
                    designUtil.processTemplate(designUtil.createRequestData());
                    configResponseBuilder.setStatus(ArtifactHandlerProviderUtil.DistributionStatusEnum.DEPLOY_OK.toString());
                }
            }
            else
            {
                throw new Exception("No Template data found");
            }
        }
        catch (Exception e) {

            configResponseBuilder.setErrorReason(e.getMessage());
            configResponseBuilder.setStatus(ArtifactHandlerProviderUtil.DistributionStatusEnum.DEPLOY_ERROR.toString());
            log.error("Caught exception looking for Artifact Handler", e);
            log.info("Caught exception looking for Artifact Handler: ");
        }

        responseBuilder.setConfigDocumentResponse(configResponseBuilder.build());
        RpcResult<UploadartifactOutput> rpcResult = RpcResultBuilder.<UploadartifactOutput> status(true).withResult(responseBuilder.build()).build();
        return Futures.immediateFuture(rpcResult);
    }

    @Override
    public void close() throws Exception {
        log.info("Closing provider for " + appName);
        log.info("Successfully closed provider for " + appName);
    }

    protected ArtifactHandlerProviderUtil getArtifactHandlerProviderUtil(UploadartifactInput input) {
        return new ArtifactHandlerProviderUtil(input);
    }
}
