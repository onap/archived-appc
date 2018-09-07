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

package org.onap.appc.provider;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.google.common.util.concurrent.Futures;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.AppcProviderService;
import org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.EvacuateInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.EvacuateOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.MigrateInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.MigrateOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.ModifyConfigInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.ModifyConfigOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.RebuildInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.RebuildOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.RestartInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.RestartOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.SnapshotInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.SnapshotOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.VmstatuscheckInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.VmstatuscheckOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.common.request.header.CommonRequestHeader;
import org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.config.payload.ConfigPayload;
import org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.vnf.resource.VnfResource;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.onap.appc.Constants;
import org.onap.appc.configuration.Configuration;
import org.onap.appc.configuration.ConfigurationFactory;
import org.onap.appc.i18n.Msg;
import org.onap.appc.provider.topology.TopologyService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/* ADDED FOR FUSION SERVICE CODE */

@SuppressWarnings("JavaDoc")
/**
 * Defines the APPC service provider.
 * <p>
 * The rpc definition in the YANG model is shown below. This model is used to generate code to manage the inputs and
 * outputs of the RPC service. For example, the input is defined by a class named {@link ConfigurationOperationInput},
 * which is generated from the name of the RPC and the "input" definition of the RPC. This class encapsulates the
 * various objects that are passed to the RPC and is used to obtain values from the input parameters.
 * </p>
 * <p>
 * Likewise, the outputs are defined by a class named {@link ConfigurationOperationOutput}. This class encapsulates the
 * defined outputs. To make construction of the outputs easier, there are also generated builder classes that are named
 * for the various elements of the output they "build", such as {@link ConfigurationResponseBuilder}.
 * </p>
 *
 * <pre>
 *   rpc configuration-operation {
 *      description "An operation to view, change, or audit the configuration of a VM";
 *      input {
 *          uses configuration-request-header;
 *          uses configuration-request;
 *      }
 *      output {
 *          uses common-response-header;
 *          uses configuration-response;
 *      }
 *  }
 * </pre>
 *
 */
public class AppcProvider implements AutoCloseable, AppcProviderService {

    private final EELFLogger logger = EELFManager.getInstance().getLogger(AppcProviderClient.class);

    private final ExecutorService executor;

    /**
     * The ODL data store broker. Provides access to a conceptual data tree store and also provides the ability to
     * subscribe for changes to data under a given branch of the tree.
     */
    protected DataBroker dataBroker;

    /**
     * ODL Notification Service that provides publish/subscribe capabilities for YANG modeled notifications.
     */
    protected NotificationPublishService notificationService;

    /**
     * Provides a registry for Remote Procedure Call (RPC) service implementations. The RPCs are defined in YANG models.
     */
    protected RpcProviderRegistry rpcRegistry;

    /**
     * Represents our RPC implementation registration
     */
    protected BindingAwareBroker.RpcRegistration<AppcProviderService> rpcRegistration;

    /**
     * The configuration
     */
    private Configuration configuration = ConfigurationFactory.getConfiguration();
    
    private AppcProviderClient appcProviderClient;

    /**
     * @param dataBroker2
     * @param notificationProviderService
     * @param rpcProviderRegistry
     */
    @SuppressWarnings({
        "javadoc", "nls"
    })
    public AppcProvider(DataBroker dataBroker2, NotificationPublishService notificationProviderService,
                        RpcProviderRegistry rpcProviderRegistry, AppcProviderClient appcProviderClient) {

        String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);
        logger.info(Msg.COMPONENT_INITIALIZING, appName, "provider");

        executor = Executors.newFixedThreadPool(1);
        dataBroker = dataBroker2;
        notificationService = notificationProviderService;
        rpcRegistry = rpcProviderRegistry;
        this.appcProviderClient = appcProviderClient;

        logger.info(Msg.COMPONENT_INITIALIZED, appName, "provider");
    }

    /**
     * Implements the close of the service
     *
     * @see java.lang.AutoCloseable#close()
     */
    @SuppressWarnings("nls")
    @Override
    public void close() throws Exception {
        String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);
        logger.info(Msg.COMPONENT_TERMINATING, appName, "provider");
        executor.shutdown();
        if (rpcRegistration != null) {
            rpcRegistration.close();
        }
        logger.info(Msg.COMPONENT_TERMINATED, appName, "provider");
    }

    public Future<RpcResult<ModifyConfigOutput>> modifyConfig(ModifyConfigInput input) {
        CommonRequestHeader hdr = input.getCommonRequestHeader();
        ConfigPayload data = input.getConfigPayload();
        RpcResult<ModifyConfigOutput> result = getTopologyService().modifyConfig(hdr, data);
        return Futures.immediateFuture(result);
    }

    /**
     * Rebuilds a specific VNF
     *
     * @see org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.AppcProviderService#rebuild(org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.RebuildInput)
     */
    @Override
    public Future<RpcResult<RebuildOutput>> rebuild(RebuildInput input) {

        CommonRequestHeader hdr = input.getCommonRequestHeader();
        VnfResource vnf = input.getVnfResource();

        RpcResult<RebuildOutput> result = getTopologyService().rebuild(hdr, vnf);
        return Futures.immediateFuture(result);
    }

    /**
     * Restarts a specific VNF
     *
     * @see org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.AppcProviderService#restart(org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.RestartInput)
     */
    @Override
    public Future<RpcResult<RestartOutput>> restart(RestartInput input) {
        CommonRequestHeader hdr = input.getCommonRequestHeader();
        VnfResource vnf = input.getVnfResource();

        RpcResult<RestartOutput> result = getTopologyService().restart(hdr, vnf);
        return Futures.immediateFuture(result);
    }

    /**
     * Migrates a specific VNF
     *
     * @see org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.AppcProviderService#migrate(org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.MigrateInput)
     */
    @Override
    public Future<RpcResult<MigrateOutput>> migrate(MigrateInput input) {
        CommonRequestHeader hdr = input.getCommonRequestHeader();
        VnfResource vnf = input.getVnfResource();

        RpcResult<MigrateOutput> result = getTopologyService().migrate(hdr, vnf);
        return Futures.immediateFuture(result);
    }

    /**
     * Evacuates a specific VNF
     *
     * @see org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.AppcProviderService#evacuate(org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.EvacuateInput)
     */
    @Override
    public Future<RpcResult<EvacuateOutput>> evacuate(EvacuateInput input) {

        return null;
    }

    /**
     * Evacuates a specific VNF
     *
     * @see org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.AppcProviderService#evacuate(org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.EvacuateInput)
     */
    @Override
    public Future<RpcResult<SnapshotOutput>> snapshot(SnapshotInput input) {
        CommonRequestHeader hdr = input.getCommonRequestHeader();
        VnfResource vnf = input.getVnfResource();

        RpcResult<SnapshotOutput> result = getTopologyService().snapshot(hdr, vnf);
        return Futures.immediateFuture(result);
    }

    /**
     * Checks status of a VM
     */
    @Override
    public Future<RpcResult<VmstatuscheckOutput>> vmstatuscheck(VmstatuscheckInput input) {
        CommonRequestHeader hdr = input.getCommonRequestHeader();
        VnfResource vnf = input.getVnfResource();

        TopologyService topology = getTopologyService();
        RpcResult<VmstatuscheckOutput> result = getTopologyService().vmstatuscheck(hdr, vnf);
        return Futures.immediateFuture(result);
    }

    TopologyService getTopologyService() {
        return new TopologyService(this);
    }
    
    public AppcProviderClient getClient() {
    	return appcProviderClient;
    }
}