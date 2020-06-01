/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications (C) 2019 Ericsson
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

package org.onap.appc.mdsal.provider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.onap.appc.Constants;
import org.onap.appc.mdsal.MDSALStore;
import org.onap.appc.mdsal.impl.MDSALStoreFactory;
import org.onap.appc.mdsal.objects.BundleInfo;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.org.onap.appc.mdsal.store.rev170925.MdsalStoreService;
import org.opendaylight.yang.gen.v1.org.onap.appc.mdsal.store.rev170925.StoreYangInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.mdsal.store.rev170925.StoreYangOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.mdsal.store.rev170925.StoreYangOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.mdsal.store.rev170925.response.Status;
import org.opendaylight.yang.gen.v1.org.onap.appc.mdsal.store.rev170925.response.StatusBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

public class MdsalStoreProvider implements MdsalStoreService ,AutoCloseable{

    protected DataBroker dataBroker;
    protected RpcProviderRegistry rpcRegistry;
    protected NotificationPublishService notificationService;

    protected BindingAwareBroker.RpcRegistration<MdsalStoreService> rpcRegistration;
    private final EELFLogger log = EELFManager.getInstance().getLogger(MdsalStoreProvider.class);
    private final ExecutorService executor;
    private static final String APP_NAME = "MdsalStoreProvider";

    public MdsalStoreProvider(DataBroker dataBroker2, NotificationPublishService notificationProviderService
            , RpcProviderRegistry rpcRegistry2){
        log.info("Creating provider for " + APP_NAME);
        executor = Executors.newFixedThreadPool(1);
        this.dataBroker = dataBroker2;
        this.notificationService = notificationProviderService;

        this.rpcRegistry = rpcRegistry2;

        if (this.rpcRegistry != null) {
            rpcRegistration = rpcRegistry.addRpcImplementation(MdsalStoreService.class, this);
        }
        log.info("Initialization complete for " + APP_NAME);

    }

    @Override
    public void close() throws Exception {
        log.info("Closing provider for " + APP_NAME);
        if(this.executor != null){
            executor.shutdown();
        }
        if(this.rpcRegistration != null){
            rpcRegistration.close();
        }
        log.info("Successfully closed provider for " + APP_NAME);
    }

    @Override
    public ListenableFuture<RpcResult<StoreYangOutput>> storeYang(StoreYangInput input) {
        Status status = null;
        String message = null;
        try{
            BundleInfo bundleInfo = new BundleInfo();
            bundleInfo.setName(input.getModuleName());
            bundleInfo.setDescription(input.getModuleName());
            bundleInfo.setLocation(input.getModuleName());

            MDSALStore store =  MDSALStoreFactory.createMDSALStore();

            Date revision = new SimpleDateFormat(Constants.YANG_REVISION_FORMAT).parse(Constants.YANG_REVISION);
            if(!store.isModulePresent(input.getModuleName(),revision)){
                message = "YANG module saved successfully";
                store.storeYangModule(input.getYang(),bundleInfo);
            }
            else{
                message = "YANG Module already available";
            }
            store.storeYangModuleOnLeader(input.getYang(),input.getModuleName());
            status = new StatusBuilder().setCode(200).setMessage(message).build();
        }
        catch (Exception e){

            message = "Error in storeYang of MdsalStoreProvider";
            log.error(message,e);
            status = new StatusBuilder().setCode(500).setMessage(message).build();
        }
        StoreYangOutputBuilder builder = new StoreYangOutputBuilder().setStatus(status);
        return Futures.immediateFuture(
                RpcResultBuilder
                        .<StoreYangOutput>status(true)
                        .withResult(builder.build())
                        .build());
    }
}
