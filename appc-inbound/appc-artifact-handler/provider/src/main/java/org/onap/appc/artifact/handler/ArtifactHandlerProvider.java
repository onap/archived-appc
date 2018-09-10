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

package org.onap.appc.artifact.handler;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.org.onap.appc.artifacthandler.rev170321.ArtifactHandlerService;
import org.opendaylight.yang.gen.v1.org.onap.appc.artifacthandler.rev170321.UploadartifactInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.artifacthandler.rev170321.UploadartifactInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.artifacthandler.rev170321.UploadartifactOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.artifacthandler.rev170321.UploadartifactOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.artifacthandler.rev170321.uploadartifact.output.ConfigDocumentResponseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.rev130405.Services;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.rev130405.ServicesBuilder;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.onap.appc.artifact.handler.utils.ArtifactHandlerProviderUtil;
import org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;



public class ArtifactHandlerProvider implements AutoCloseable, ArtifactHandlerService, DataChangeListener {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(ArtifactHandlerProvider.class);
    private final String appName = "ArtifactsHandler";
    private final ExecutorService executor;
    protected DataBroker dataBroker;
    protected NotificationPublishService notificationService;
    protected RpcProviderRegistry rpcRegistry;
    private ListenerRegistration<DataChangeListener> dclServices;

    protected BindingAwareBroker.RpcRegistration<ArtifactHandlerService> rpcRegistration;

    public ArtifactHandlerProvider(DataBroker dataBroker2,
            NotificationPublishService notificationProviderService,
            RpcProviderRegistry rpcProviderRegistry) {
        this.log.info("Creating provider for " + appName);
        executor = Executors.newFixedThreadPool(10);
        dataBroker = dataBroker2;
        notificationService = notificationProviderService;
        rpcRegistry = rpcProviderRegistry;
        initialize();

    }

    public void initialize() {
        log.info("Initializing provider for " + appName);
        // Create the top level containers
        createContainers();
        try {
            ArtifactHandlerProviderUtil.loadProperties();
        } catch (Exception e) {
            log.error("Caught exception while trying to load properties file", e);
        }
        // Listener for changes to Services tree

        log.info("Initialization complete for " + appName);
    }
    private void createContainers() {
        final WriteTransaction t = dataBroker.newReadWriteTransaction();
        // Create the Services container
        t.merge(LogicalDatastoreType.CONFIGURATION,InstanceIdentifier.create(Services.class),new ServicesBuilder().build());
        t.merge(LogicalDatastoreType.OPERATIONAL,InstanceIdentifier.create(Services.class),new ServicesBuilder().build());

        try {
            CheckedFuture<Void, TransactionCommitFailedException> checkedFuture = t.submit();
            checkedFuture.get();
            log.info("Create containers succeeded!");

        } catch (InterruptedException | ExecutionException e) {
            log.error("Create containers failed",  e);
        }
    }


    @Override
    public void onDataChanged(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> arg0) {
        // TODO Auto-generated method stub

    }



    @Override
    public void close() throws Exception {

        log.info("Closing provider for " + appName);
        if(this.executor != null){
            executor.shutdown();
        }
        if(this.rpcRegistration != null){
            rpcRegistration.close();
        }
        log.info("Successfully closed provider for " + appName);

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
    public Future<RpcResult<UploadartifactOutput>> uploadartifact(UploadartifactInput input) {

        if (input == null || input.getDocumentParameters() == null || input.getDocumentParameters().getArtifactContents() == null ) {        
            RpcResult<UploadartifactOutput> rpcResult =
                    buildResponse1("N/A", "N/A", "INVALID_INPUT", "Invalid input, null or empty document information" , "Y");
            return Futures.immediateFuture(rpcResult);
        }
        UploadartifactInputBuilder inputBuilder = new UploadartifactInputBuilder(input);
        ConfigDocumentResponseBuilder configResponseBuilder = new ConfigDocumentResponseBuilder();
        UploadartifactOutputBuilder responseBuilder = new UploadartifactOutputBuilder();
        log.info("Received input = " + input );
        ArtifactHandlerProviderUtil designUtil = new ArtifactHandlerProviderUtil(input);
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
                throw new Exception("No Tempalte data found");                
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
}
