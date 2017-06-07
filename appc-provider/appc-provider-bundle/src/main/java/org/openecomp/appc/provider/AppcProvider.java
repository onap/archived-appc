/*-
 * ============LICENSE_START=======================================================
 * APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Amdocs
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
 * ============LICENSE_END=========================================================
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */

package org.openecomp.appc.provider;

import com.google.common.util.concurrent.Futures;
import org.json.JSONObject;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.rev160104.*;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.rev160104.common.request.header.CommonRequestHeader;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.rev160104.responseattributes.StatusBuilder;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.rev160104.responseheader.ResponseHeaderBuilder;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.rev160104.vnf.resource.VnfResource;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.rev160104.config.payload.ConfigPayload;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.openecomp.appc.Constants;
import org.openecomp.appc.configuration.Configuration;
import org.openecomp.appc.configuration.ConfigurationFactory;
import org.openecomp.appc.executor.objects.LCMCommandStatus;
import org.openecomp.appc.executor.objects.Params;
import org.openecomp.appc.i18n.Msg;
import org.openecomp.appc.provider.lcm.util.RequestInputBuilder;
import org.openecomp.appc.provider.lcm.util.ValidationService;
import org.openecomp.appc.provider.topology.TopologyService;
import org.openecomp.appc.requesthandler.RequestHandler;
import org.openecomp.appc.requesthandler.objects.RequestHandlerInput;
import org.openecomp.appc.requesthandler.objects.RequestHandlerOutput;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.rev160104.status.Status;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.rev160104.Action;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.TimeZone;
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

    private ListenerRegistration<DataChangeListener> dclServices;

    /**
     * The ODL data store broker. Provides access to a conceptual data tree store and also provides the ability to
     * subscribe for changes to data under a given branch of the tree.
     */
    protected DataBroker dataBroker;

    /**
     * ODL Notification Service that provides publish/subscribe capabilities for YANG modeled notifications.
     */
    protected NotificationProviderService notificationService;

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

    /**
     * @param dataBroker2
     * @param notificationProviderService
     * @param rpcProviderRegistry
     */
    @SuppressWarnings({
        "javadoc", "nls"
    })
    public AppcProvider(DataBroker dataBroker2, NotificationProviderService notificationProviderService,
                        RpcProviderRegistry rpcProviderRegistry) {

        String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);
        logger.info(Msg.COMPONENT_INITIALIZING, appName, "provider");

        executor = Executors.newFixedThreadPool(1);
        dataBroker = dataBroker2;
        notificationService = notificationProviderService;
        rpcRegistry = rpcProviderRegistry;

        if (rpcRegistry != null) {
            rpcRegistration = rpcRegistry.addRpcImplementation(AppcProviderService.class, this);
        }

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


public Future<RpcResult<ModifyConfigOutput>> modifyConfig(ModifyConfigInput input){
	CommonRequestHeader hdr = input.getCommonRequestHeader();        
	ConfigPayload data = input.getConfigPayload();
    TopologyService topology = new TopologyService(this);
    RpcResult<ModifyConfigOutput> result = topology.modifyConfig(hdr, data);
    return Futures.immediateFuture(result);
}
    /**
     * Rebuilds a specific VNF
     *
     * @see org.opendaylight.yang.gen.v1.org.openecomp.appc.rev160104.AppcProviderService#rebuild(org.opendaylight.yang.gen.v1.org.openecomp.appc.rev160104.RebuildInput)
     */
    @Override
    public Future<RpcResult<RebuildOutput>> rebuild(RebuildInput input) {

        CommonRequestHeader hdr = input.getCommonRequestHeader();
        VnfResource vnf = input.getVnfResource();

        TopologyService topology = new TopologyService(this);
        RpcResult<RebuildOutput> result = topology.rebuild(hdr, vnf);
        return Futures.immediateFuture(result);
    }

    /**
     * Restarts a specific VNF
     *
     * @see org.opendaylight.yang.gen.v1.org.openecomp.appc.rev160104.AppcProviderService#restart(org.opendaylight.yang.gen.v1.org.openecomp.appc.rev160104.RestartInput)
     */
    @Override
    public Future<RpcResult<RestartOutput>> restart(RestartInput input) {
        CommonRequestHeader hdr = input.getCommonRequestHeader();
        VnfResource vnf = input.getVnfResource();

        TopologyService topology = new TopologyService(this);
        RpcResult<RestartOutput> result = topology.restart(hdr, vnf);
        return Futures.immediateFuture(result);
    }

    /**
     * Migrates a specific VNF
     *
     * @see org.opendaylight.yang.gen.v1.org.openecomp.appc.rev160104.AppcProviderService#migrate(org.opendaylight.yang.gen.v1.org.openecomp.appc.rev160104.MigrateInput)
     */
    @Override
    public Future<RpcResult<MigrateOutput>> migrate(MigrateInput input) {
        CommonRequestHeader hdr = input.getCommonRequestHeader();
        VnfResource vnf = input.getVnfResource();

        TopologyService topology = new TopologyService(this);
        RpcResult<MigrateOutput> result = topology.migrate(hdr, vnf);
        return Futures.immediateFuture(result);
    }

    /**
     * Evacuates a specific VNF
     *
     * @see org.opendaylight.yang.gen.v1.org.openecomp.appc.rev160104.AppcProviderService#evacuate(org.opendaylight.yang.gen.v1.org.openecomp.appc.rev160104.EvacuateInput)
     */
    @Override
    public Future<RpcResult<EvacuateOutput>> evacuate(EvacuateInput input) {
        CommonRequestHeader hdr = input.getCommonRequestHeader();
        VnfResource vnf = input.getVnfResource();

        TopologyService topology = new TopologyService(this);
        return null;
    }

    /**
     * Evacuates a specific VNF
     *
     * @see org.opendaylight.yang.gen.v1.org.openecomp.appc.rev160104.AppcProviderService#evacuate(org.opendaylight.yang.gen.v1.org.openecomp.appc.rev160104.EvacuateInput)
     */
    @Override
    public Future<RpcResult<SnapshotOutput>> snapshot(SnapshotInput input) {
        CommonRequestHeader hdr = input.getCommonRequestHeader();
        VnfResource vnf = input.getVnfResource();

        TopologyService topology = new TopologyService(this);
        RpcResult<SnapshotOutput> result = topology.snapshot(hdr, vnf);
        return Futures.immediateFuture(result);
    }
    
    
    /**
     * Checks status of a VM
    */
    @Override
    public Future<RpcResult<VmstatuscheckOutput>> vmstatuscheck(VmstatuscheckInput input) {
    CommonRequestHeader hdr = input.getCommonRequestHeader();
    VnfResource vnf = input.getVnfResource();

    TopologyService topology = new TopologyService(this);
    RpcResult<VmstatuscheckOutput> result = topology.vmstatuscheck(hdr, vnf);
    return Futures.immediateFuture(result);
    }	

}
