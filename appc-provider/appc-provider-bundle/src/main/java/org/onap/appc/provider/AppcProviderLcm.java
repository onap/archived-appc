/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications Copyright (C) 2018-2019 Orange
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

package org.onap.appc.provider;

import java.text.ParseException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.onap.appc.Constants;
import org.onap.appc.configuration.Configuration;
import org.onap.appc.configuration.ConfigurationFactory;
import org.onap.appc.i18n.Msg;
import org.onap.appc.logging.LoggingConstants;
import org.onap.appc.logging.LoggingUtils;
import org.onap.appc.provider.lcm.service.AbstractBaseUtils;
import org.onap.appc.provider.lcm.service.ActionStatusService;
import org.onap.appc.provider.lcm.service.QueryService;
import org.onap.appc.provider.lcm.service.QuiesceTrafficService;
import org.onap.appc.provider.lcm.service.RebootService;
import org.onap.appc.provider.lcm.service.RequestExecutor;
import org.onap.appc.provider.lcm.service.ResumeTrafficService;
import org.onap.appc.provider.lcm.service.UpgradeService;
import org.onap.appc.provider.lcm.service.VolumeService;
import org.onap.appc.provider.lcm.service.ConfigScaleOutService;
import org.onap.appc.provider.lcm.service.PreConfigure;
import org.onap.appc.provider.lcm.service.PreRebuild;
import org.onap.appc.provider.lcm.service.GetConfigService;
import org.onap.appc.provider.lcm.service.PostRebuild;
import org.onap.appc.provider.lcm.service.PostEvacuateService;
import org.onap.appc.provider.lcm.service.PreMigrateService;
import org.onap.appc.provider.lcm.service.PostMigrateService;
import org.onap.appc.provider.lcm.service.DistributeTrafficService;
import org.onap.appc.provider.lcm.service.DistributeTrafficCheckService;
import org.onap.appc.provider.lcm.service.PreEvacuate;
import org.onap.appc.provider.lcm.service.ProvisioningService;
import org.onap.appc.provider.lcm.service.LicenseManagementService;
import org.onap.appc.provider.lcm.service.StartTraffic;
import org.onap.appc.provider.lcm.service.StatusTraffic;
import org.onap.appc.provider.lcm.service.StopTraffic;
import org.onap.appc.provider.lcm.service.ConfigScaleInService;
import org.onap.appc.provider.lcm.service.DownloadNESw;
import org.onap.appc.provider.lcm.service.ActivateNESw;
import org.onap.appc.provider.lcm.util.RequestInputBuilder;
import org.onap.appc.provider.lcm.util.ValidationService;
import org.onap.appc.requesthandler.objects.RequestHandlerInput;
import org.onap.appc.requesthandler.objects.RequestHandlerOutput;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.*;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.status.Status;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;


public class AppcProviderLcm extends AbstractBaseUtils implements AutoCloseable, AppcProviderLcmService {

    private Configuration configuration = ConfigurationFactory.getConfiguration();
    private final EELFLogger logger = EELFManager.getInstance().getLogger(AppcProviderLcm.class);

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
    protected BindingAwareBroker.RpcRegistration<AppcProviderLcmService> rpcRegistration;


    /**
     * @param dataBroker
     * @param notificationProviderService
     * @param rpcProviderRegistry
     */
    @SuppressWarnings({"javadoc", "nls"})
    public AppcProviderLcm(DataBroker dataBroker, NotificationPublishService notificationProviderService,
            RpcProviderRegistry rpcProviderRegistry) {

        String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);
        logger.info(Msg.COMPONENT_INITIALIZING, appName, "provider-lcm");

        executor = Executors.newFixedThreadPool(1);
        this.dataBroker = dataBroker;
        this.notificationService = notificationProviderService;
        this.rpcRegistry = rpcProviderRegistry;

        if (this.rpcRegistry != null) {
            rpcRegistration = rpcRegistry.addRpcImplementation(AppcProviderLcmService.class, this);
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


    /**
     * Rebuilds a specific VNF
     *
     * @see org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.AppcProviderLcmService#rebuild(RebuildInput)
     */
    @Override
    public ListenableFuture<RpcResult<RebuildOutput>> rebuild(RebuildInput input) {
        logger.debug("Input received : " + input.toString());

        RebuildOutputBuilder outputBuilder = new RebuildOutputBuilder();
        String action = Action.Rebuild.toString();
        String rpcName = Action.Rebuild.name().toLowerCase();
        Status status =
                ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        if (null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext()
                        .commonHeader(input.getCommonHeader()).actionIdentifiers(input.getActionIdentifiers())
                        .payload(input.getPayload()).action(action).rpcName(rpcName).build();
                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s",
                        input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildStatusWithParseException(e);

                LoggingUtils.logErrorMessage(LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());

            }
        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<RebuildOutput> result =
                RpcResultBuilder.<RebuildOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);

    }


    /**
     * Restarts a specific VNF
     *
     * @see org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.AppcProviderLcmService#restart(RestartInput)
     */
    @Override
    public ListenableFuture<RpcResult<RestartOutput>> restart(RestartInput input) {
        logger.debug("Input received : " + input.toString());

        RestartOutputBuilder outputBuilder = new RestartOutputBuilder();
        String action = Action.Restart.toString();
        String rpcName = Action.Restart.name().toLowerCase();
        Status status =
                ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        if (null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext()
                        .commonHeader(input.getCommonHeader()).actionIdentifiers(input.getActionIdentifiers())
                        .payload(input.getPayload()).action(action).rpcName(rpcName).build();

                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s",
                        input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildStatusWithParseException(e);

                LoggingUtils.logErrorMessage(LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());

            }
        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<RestartOutput> result =
                RpcResultBuilder.<RestartOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    /**
     * Start Application
     *
     * @see org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.AppcProviderLcmService#startApplication(StartApplicationInput)
     */
    @Override
    public ListenableFuture<RpcResult<StartApplicationOutput>> startApplication(StartApplicationInput input) {
        logger.debug("Input received : " + input.toString());

        StartApplicationOutputBuilder outputBuilder = new StartApplicationOutputBuilder();
        String action = Action.StartApplication.toString();
        String rpcName = Action.StartApplication.name().toLowerCase();
        Status status =
                ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        if (null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext()
                        .commonHeader(input.getCommonHeader()).actionIdentifiers(input.getActionIdentifiers())
                        .payload(input.getPayload()).action(action).rpcName(rpcName).build();

                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s",
                        input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildStatusWithParseException(e);

                LoggingUtils.logErrorMessage(LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());

            }
        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<StartApplicationOutput> result =
                RpcResultBuilder.<StartApplicationOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    /**
     * Migrates a specific VNF
     *
     * @see org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.AppcProviderLcmService#migrate(MigrateInput)
     */
    @Override
    public ListenableFuture<RpcResult<MigrateOutput>> migrate(MigrateInput input) {
        logger.debug("Input received : " + input.toString());

        MigrateOutputBuilder outputBuilder = new MigrateOutputBuilder();
        String action = Action.Migrate.toString();
        String rpcName = Action.Migrate.name().toLowerCase();
        Status status =
                ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        if (null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext()
                        .commonHeader(input.getCommonHeader()).actionIdentifiers(input.getActionIdentifiers())
                        .payload(input.getPayload()).action(action).rpcName(rpcName).build();
                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s",
                        input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildStatusWithParseException(e);

                LoggingUtils.logErrorMessage(LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());

            }
        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<MigrateOutput> result =
                RpcResultBuilder.<MigrateOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }


    /**
     * Evacuates a specific VNF
     *
     * @see org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.AppcProviderLcmService#evacuate(EvacuateInput)
     */
    @Override
    public ListenableFuture<RpcResult<EvacuateOutput>> evacuate(EvacuateInput input) {
        logger.debug("Input received : " + input.toString());

        EvacuateOutputBuilder outputBuilder = new EvacuateOutputBuilder();
        String action = Action.Evacuate.toString();
        String rpcName = Action.Evacuate.name().toLowerCase();
        Status status =
                ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        if (null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext()
                        .commonHeader(input.getCommonHeader()).actionIdentifiers(input.getActionIdentifiers())
                        .payload(input.getPayload()).action(action).rpcName(rpcName).build();
                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s",
                        input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildStatusWithParseException(e);

                LoggingUtils.logErrorMessage(LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());

            }
        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<EvacuateOutput> result =
                RpcResultBuilder.<EvacuateOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }


    /**
     * Evacuates a specific VNF
     *
     * @see org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.AppcProviderLcmService#snapshot(SnapshotInput)
     */
    @Override
    public ListenableFuture<RpcResult<SnapshotOutput>> snapshot(SnapshotInput input) {
        logger.debug("Input received : " + input.toString());

        SnapshotOutputBuilder outputBuilder = new SnapshotOutputBuilder();
        String action = Action.Snapshot.toString();
        String rpcName = Action.Snapshot.name().toLowerCase();
        Status status =
                ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        String identityUrl = input.getIdentityUrl();
        if (null == status) {
            try {
                RequestHandlerInput request =
                        new RequestInputBuilder().requestContext().commonHeader(input.getCommonHeader())
                                .actionIdentifiers(input.getActionIdentifiers()).payload(input.getPayload())
                                .action(action).rpcName(rpcName).additionalContext("identity-url", identityUrl).build();
                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s",
                        input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildStatusWithParseException(e);

                LoggingUtils.logErrorMessage(LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());
            }
        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<SnapshotOutput> result =
                RpcResultBuilder.<SnapshotOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public ListenableFuture<RpcResult<RollbackOutput>> rollback(RollbackInput input) {
        logger.debug("Input received : " + input.toString());

        RollbackOutputBuilder outputBuilder = new RollbackOutputBuilder();
        String rpcName = Action.Rollback.toString();
        Status status =
                ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), rpcName);
        String identityUrl = input.getIdentityUrl();
        String snapshotId = input.getSnapshotId();
        if (null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext()
                        .commonHeader(input.getCommonHeader()).actionIdentifiers(input.getActionIdentifiers())
                        .payload(input.getPayload()).additionalContext("identity-url", identityUrl)
                        .additionalContext("snapshot-id", snapshotId).action(rpcName).build();
                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s",
                        input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildStatusWithParseException(e);

                LoggingUtils.logErrorMessage(LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, rpcName, e.getMessage()),
                        this.getClass().getName());

            }
        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<RollbackOutput> result =
                RpcResultBuilder.<RollbackOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public ListenableFuture<RpcResult<SyncOutput>> sync(SyncInput input) {
        logger.debug("Input received : " + input.toString());
        SyncOutputBuilder outputBuilder = new SyncOutputBuilder();
        String action = Action.Sync.toString();
        String rpcName = Action.Sync.name().toLowerCase();
        Status status =
                ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        if (null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext()
                        .commonHeader(input.getCommonHeader()).actionIdentifiers(input.getActionIdentifiers())
                        .payload(input.getPayload()).action(action).rpcName(rpcName).build();
                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s",
                        input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildStatusWithParseException(e);

                LoggingUtils.logErrorMessage(LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());

            }
        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<SyncOutput> result =
                RpcResultBuilder.<SyncOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public ListenableFuture<RpcResult<QueryOutput>> query(QueryInput input) {
        logger.debug(String.format("LCM query received input: %s", input.toString()));
        QueryOutputBuilder outputBuilder = new QueryService().process(input);
        RpcResult<QueryOutput> result =
                RpcResultBuilder.<QueryOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public ListenableFuture<RpcResult<RebootOutput>> reboot(RebootInput input) {
        logger.debug(String.format("LCM reboot received input: %s", input.toString()));
        RebootOutputBuilder outputBuilder = new RebootService().reboot(input);
        RpcResult<RebootOutput> result =
                RpcResultBuilder.<RebootOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public ListenableFuture<RpcResult<AttachVolumeOutput>> attachVolume(AttachVolumeInput input) {
        logger.debug(String.format("LCM attachVolume received input: %s", input.toString()));
        AttachVolumeOutputBuilder outputBuilder = new VolumeService(true).attachVolume(input);
        RpcResult<AttachVolumeOutput> result =
                RpcResultBuilder.<AttachVolumeOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public ListenableFuture<RpcResult<DetachVolumeOutput>> detachVolume(DetachVolumeInput input) {
        logger.debug(String.format("LCM detachVolume received input: %s", input.toString()));
        DetachVolumeOutputBuilder outputBuilder = new VolumeService(false).detachVolume(input);
        RpcResult<DetachVolumeOutput> result =
                RpcResultBuilder.<DetachVolumeOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public ListenableFuture<RpcResult<QuiesceTrafficOutput>> quiesceTraffic(QuiesceTrafficInput input) {
        logger.debug(String.format("LCM quiesce received input: %s", input.toString()));
        QuiesceTrafficOutputBuilder outputBuilder = new QuiesceTrafficService().process(input);
        RpcResult<QuiesceTrafficOutput> result =
                RpcResultBuilder.<QuiesceTrafficOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public ListenableFuture<RpcResult<ResumeTrafficOutput>> resumeTraffic(ResumeTrafficInput input) {
        logger.debug(String.format("LCM resume received input: %s", input.toString()));
        ResumeTrafficOutputBuilder outputBuilder = new ResumeTrafficService().process(input);
        RpcResult<ResumeTrafficOutput> result =
                RpcResultBuilder.<ResumeTrafficOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public ListenableFuture<RpcResult<DistributeTrafficOutput>> distributeTraffic(DistributeTrafficInput input) {
        logger.debug(String.format("LCM DistributeTraffic, received input: %s", input.toString()));
        DistributeTrafficOutputBuilder outputBuilder = new DistributeTrafficService().process(input);
        RpcResult<DistributeTrafficOutput> result =
                RpcResultBuilder.<DistributeTrafficOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public ListenableFuture<RpcResult<DistributeTrafficCheckOutput>> distributeTrafficCheck(DistributeTrafficCheckInput input) {
        logger.debug(String.format("LCM DistributeTrafficCheck, received input: %s", input.toString()));
        DistributeTrafficCheckOutputBuilder outputBuilder = new DistributeTrafficCheckService().process(input);
        RpcResult<DistributeTrafficCheckOutput> result =
                RpcResultBuilder.<DistributeTrafficCheckOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public ListenableFuture<RpcResult<UpgradePreCheckOutput>> upgradePreCheck(UpgradePreCheckInput input) {
        logger.debug(String.format("LCM upgradeprecheck received input: %s", input.toString()));
        UpgradePreCheckOutputBuilder outputBuilder = new UpgradeService("upgradePre").upgradePreCheck(input);
        RpcResult<UpgradePreCheckOutput> result =
                RpcResultBuilder.<UpgradePreCheckOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public ListenableFuture<RpcResult<UpgradeSoftwareOutput>> upgradeSoftware(UpgradeSoftwareInput input) {
        logger.debug(String.format("LCM upgradesoftware received input: %s", input.toString()));
        UpgradeSoftwareOutputBuilder outputBuilder = new UpgradeService("upgradeSoft").upgradeSoftware(input);
        RpcResult<UpgradeSoftwareOutput> result =
                RpcResultBuilder.<UpgradeSoftwareOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public ListenableFuture<RpcResult<UpgradePostCheckOutput>> upgradePostCheck(UpgradePostCheckInput input) {
        logger.debug(String.format("LCM upgradepostcheck received input: %s", input.toString()));
        UpgradePostCheckOutputBuilder outputBuilder = new UpgradeService("upgradePost").upgradePostCheck(input);
        RpcResult<UpgradePostCheckOutput> result =
                RpcResultBuilder.<UpgradePostCheckOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public ListenableFuture<RpcResult<UpgradeBackupOutput>> upgradeBackup(UpgradeBackupInput input) {
        logger.debug(String.format("LCM backup received input: %s", input.toString()));
        UpgradeBackupOutputBuilder outputBuilder = new UpgradeService("upgradeBackup").upgradeBackup(input);
        RpcResult<UpgradeBackupOutput> result =
                RpcResultBuilder.<UpgradeBackupOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public ListenableFuture<RpcResult<UpgradeBackoutOutput>> upgradeBackout(UpgradeBackoutInput input) {
        logger.debug(String.format("LCM backout received input: %s", input.toString()));
        UpgradeBackoutOutputBuilder outputBuilder = new UpgradeService("upgradeBackout").upgradeBackout(input);
        RpcResult<UpgradeBackoutOutput> result =
                RpcResultBuilder.<UpgradeBackoutOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public ListenableFuture<RpcResult<PreConfigureOutput>> preConfigure(PreConfigureInput input) {
        logger.debug(String.format("LCM PreConfigure received input: %s", input.toString()));
        PreConfigureOutputBuilder outputBuilder = new PreConfigure().process(input);
        RpcResult<PreConfigureOutput> result =
                RpcResultBuilder.<PreConfigureOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public ListenableFuture<RpcResult<GetConfigOutput>> getConfig(GetConfigInput input) {
        logger.debug(String.format("LCM getConfig received input: %s", input.toString()));
        GetConfigOutputBuilder outputBuilder = new GetConfigService().process(input);
        RpcResult<GetConfigOutput> result =
                RpcResultBuilder.<GetConfigOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public ListenableFuture<RpcResult<PreRebuildOutput>> preRebuild(PreRebuildInput input) {
        logger.debug(String.format("LCM PreRebuild received input: %s", input.toString()));
        PreRebuildOutputBuilder outputBuilder = new PreRebuild().process(input);
        RpcResult<PreRebuildOutput> result =
                RpcResultBuilder.<PreRebuildOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public ListenableFuture<RpcResult<PostRebuildOutput>> postRebuild(PostRebuildInput input) {
        logger.debug(String.format("LCM PostRebuild received input: %s", input.toString()));
        PostRebuildOutputBuilder outputBuilder = new PostRebuild().process(input);
        RpcResult<PostRebuildOutput> result =
                RpcResultBuilder.<PostRebuildOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public ListenableFuture<RpcResult<PreEvacuateOutput>> preEvacuate(PreEvacuateInput input) {
        logger.debug(String.format("LCM PreEvacuate received input: %s", input.toString()));
        PreEvacuateOutputBuilder outputBuilder = new PreEvacuate().process(input);
        RpcResult<PreEvacuateOutput> result =
                RpcResultBuilder.<PreEvacuateOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public ListenableFuture<RpcResult<ProvisioningOutput>> provisioning(ProvisioningInput input) {
        logger.debug(String.format("LCM ProvisioningService received input: %s", input.toString()));
        ProvisioningOutputBuilder outputBuilder = new ProvisioningService().process(input);
        RpcResult<ProvisioningOutput> result =
                RpcResultBuilder.<ProvisioningOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

 @Override
    public ListenableFuture<RpcResult<LicenseManagementOutput>> licenseManagement(LicenseManagementInput input) {
        logger.debug(String.format("LCM licensemangementService received input: %s", input.toString()));
        LicenseManagementOutputBuilder outputBuilder = new LicenseManagementService().process(input);
        RpcResult<LicenseManagementOutput> result =
                RpcResultBuilder.<LicenseManagementOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public ListenableFuture<RpcResult<StartTrafficOutput>> startTraffic(StartTrafficInput input) {
        logger.debug(String.format("LCM StartTraffic received input: %s", input.toString()));
        StartTrafficOutputBuilder outputBuilder = new StartTraffic().process(input);
        RpcResult<StartTrafficOutput> result =
                RpcResultBuilder.<StartTrafficOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public ListenableFuture<RpcResult<StatusTrafficOutput>> statusTraffic(StatusTrafficInput input) {
        logger.debug(String.format("LCM StatusTraffic received input: %s", input.toString()));
        StatusTrafficOutputBuilder outputBuilder = new StatusTraffic().process(input);
        RpcResult<StatusTrafficOutput> result =
                RpcResultBuilder.<StatusTrafficOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public ListenableFuture<RpcResult<StopTrafficOutput>> stopTraffic(StopTrafficInput input) {
        logger.debug(String.format("LCM StopTraffic received input: %s", input.toString()));
        StopTrafficOutputBuilder outputBuilder = new StopTraffic().process(input);
        RpcResult<StopTrafficOutput> result =
                RpcResultBuilder.<StopTrafficOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public ListenableFuture<RpcResult<DownloadNESwOutput>> downloadNESw(DownloadNESwInput input) {
        logger.debug(String.format("LCM DownloadNESw received input: %s", input.toString()));
        DownloadNESwOutputBuilder outputBuilder = new DownloadNESw().process(input);
        RpcResult<DownloadNESwOutput> result =
                RpcResultBuilder.<DownloadNESwOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public ListenableFuture<RpcResult<ActivateNESwOutput>> activateNESw(ActivateNESwInput input) {
        logger.debug(String.format("LCM ActivateNESw received input: %s", input.toString()));
        ActivateNESwOutputBuilder outputBuilder = new ActivateNESw().process(input);
        RpcResult<ActivateNESwOutput> result =
                RpcResultBuilder.<ActivateNESwOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public ListenableFuture<RpcResult<TerminateOutput>> terminate(TerminateInput input) {
        logger.debug("Input received : " + input.toString());
        TerminateOutputBuilder outputBuilder = new TerminateOutputBuilder();
        Action myAction = Action.Terminate;
        String action = myAction.toString();
        String rpcName = getRpcName(myAction);
        Status status =
                ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        if (null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext()
                        .commonHeader(input.getCommonHeader()).actionIdentifiers(input.getActionIdentifiers())
                        .payload(input.getPayload()).action(action).rpcName(rpcName).build();
                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s",
                        input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {

                status = buildStatusWithParseException(e);

                LoggingUtils.logErrorMessage(LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());

            }

        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<TerminateOutput> result =
                RpcResultBuilder.<TerminateOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public ListenableFuture<RpcResult<ConfigureOutput>> configure(ConfigureInput input) {
        logger.debug("Input received : " + input.toString());
        ConfigureOutputBuilder outputBuilder = new ConfigureOutputBuilder();
        Action myAction = Action.Configure;
        String action = myAction.toString();
        String rpcName = getRpcName(myAction);
        Status status =
                ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        if (null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext()
                        .commonHeader(input.getCommonHeader()).actionIdentifiers(input.getActionIdentifiers())
                        .payload(input.getPayload()).action(action).rpcName(rpcName).build();
                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s",
                        input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildStatusWithParseException(e);

                LoggingUtils.logErrorMessage(LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());

            }
        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<ConfigureOutput> result =
                RpcResultBuilder.<ConfigureOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public ListenableFuture<RpcResult<ActionStatusOutput>> actionStatus(ActionStatusInput input) {
        logger.debug(String.format("Input received : %s", input.toString()));
        ActionStatusOutputBuilder outputBuilder = (new ActionStatusService()).queryStatus(input);
        RpcResult<ActionStatusOutput> result =
                RpcResultBuilder.<ActionStatusOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public ListenableFuture<RpcResult<ConfigModifyOutput>> configModify(ConfigModifyInput input) {
        logger.debug("Input received : " + input.toString());
        ConfigModifyOutputBuilder outputBuilder = new ConfigModifyOutputBuilder();
        Action myAction = Action.ConfigModify;
        String action = myAction.toString();
        String rpcName = getRpcName(myAction);
        Status status =
                ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        if (null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext()
                        .commonHeader(input.getCommonHeader()).actionIdentifiers(input.getActionIdentifiers())
                        .payload(input.getPayload()).action(action).rpcName(rpcName).build();
                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s",
                        input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildStatusWithParseException(e);

                LoggingUtils.logErrorMessage(LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());

            }
        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<ConfigModifyOutput> result =
                RpcResultBuilder.<ConfigModifyOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public ListenableFuture<RpcResult<ConfigScaleOutOutput>> configScaleOut(ConfigScaleOutInput input) {
        logger.debug("Input received : " + input.toString());
        ConfigScaleOutOutputBuilder outputBuilder = new ConfigScaleOutService().process(input);
        RpcResult<ConfigScaleOutOutput> result = RpcResultBuilder.<ConfigScaleOutOutput> status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }
    
    @Override
    public ListenableFuture<RpcResult<ConfigScaleInOutput>> configScaleIn(ConfigScaleInInput input) {
        logger.debug("Input received : " + input.toString());
        ConfigScaleInOutputBuilder outputBuilder = new ConfigScaleInService().process(input);
        RpcResult<ConfigScaleInOutput> result = RpcResultBuilder.<ConfigScaleInOutput> status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public ListenableFuture<RpcResult<PostEvacuateOutput>> postEvacuate(PostEvacuateInput input) {
        logger.debug("Input received : " + input.toString());
        PostEvacuateOutputBuilder outputBuilder = new PostEvacuateService().process(input);
        RpcResult<PostEvacuateOutput> result = RpcResultBuilder.<PostEvacuateOutput> status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

   @Override
    public ListenableFuture<RpcResult<PreMigrateOutput>> preMigrate(PreMigrateInput input) {
        logger.debug("Input received : " + input.toString());
        PreMigrateOutputBuilder outputBuilder = new PreMigrateService().process(input);
        RpcResult<PreMigrateOutput> result = RpcResultBuilder.<PreMigrateOutput> status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public ListenableFuture<RpcResult<PostMigrateOutput>> postMigrate(PostMigrateInput input) {
        logger.debug("Input received : " + input.toString());
        PostMigrateOutputBuilder outputBuilder = new PostMigrateService().process(input);
        RpcResult<PostMigrateOutput> result = RpcResultBuilder.<PostMigrateOutput> status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public ListenableFuture<RpcResult<ConfigRestoreOutput>> configRestore(ConfigRestoreInput input) {
        logger.debug("Input received : " + input.toString());
        ConfigRestoreOutputBuilder outputBuilder = new ConfigRestoreOutputBuilder();
        Action myAction = Action.ConfigRestore;
        String action = myAction.toString();
        String rpcName = getRpcName(myAction);
        Status status =
                ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        if (null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext()
                        .commonHeader(input.getCommonHeader()).actionIdentifiers(input.getActionIdentifiers())
                        .payload(input.getPayload()).action(action).rpcName(rpcName).build();
                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s",
                        input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildStatusWithParseException(e);

                LoggingUtils.logErrorMessage(LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());

            }
        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<ConfigRestoreOutput> result =
                RpcResultBuilder.<ConfigRestoreOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public ListenableFuture<RpcResult<TestOutput>> test(TestInput input) {
        logger.debug("Input received : " + input.toString());
        TestOutputBuilder outputBuilder = new TestOutputBuilder();
        Action myAction = Action.Test;
        String action = myAction.toString();
        String rpcName = getRpcName(myAction);
        Status status =
                ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        if (null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext()
                        .commonHeader(input.getCommonHeader()).actionIdentifiers(input.getActionIdentifiers())
                        .payload(input.getPayload()).action(action).rpcName(rpcName).build();
                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s",
                        input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildStatusWithParseException(e);

                LoggingUtils.logErrorMessage(LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());

            }
        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<TestOutput> result =
                RpcResultBuilder.<TestOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }


    @Override
    public ListenableFuture<RpcResult<StopOutput>> stop(StopInput input) {
        logger.debug("Input received : " + input.toString());
        StopOutputBuilder outputBuilder = new StopOutputBuilder();
        Action myAction = Action.Stop;
        String action = myAction.toString();
        String rpcName = getRpcName(myAction);
        Status status =
                ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        if (null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext()
                        .commonHeader(input.getCommonHeader()).actionIdentifiers(input.getActionIdentifiers())
                        .payload(input.getPayload()).action(action).rpcName(rpcName).build();
                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s",
                        input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildStatusWithParseException(e);

                LoggingUtils.logErrorMessage(LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());

            }
        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<StopOutput> result =
                RpcResultBuilder.<StopOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    /**
     * Starts a specific VNF
     *
     * @see org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.AppcProviderLcmService#start(StartInput)
     */
    @Override
    public ListenableFuture<RpcResult<StartOutput>> start(StartInput input) {
        logger.debug("Input received : " + input.toString());

        StartOutputBuilder outputBuilder = new StartOutputBuilder();
        Action myAction = Action.Start;
        String action = myAction.toString();
        String rpcName = getRpcName(myAction);
        Status status =
                ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        if (null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext()
                        .commonHeader(input.getCommonHeader()).actionIdentifiers(input.getActionIdentifiers())
                        .payload(input.getPayload()).action(action).rpcName(rpcName).build();
                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s",
                        input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildStatusWithParseException(e);

                LoggingUtils.logErrorMessage(LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());

            }
        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<StartOutput> result =
                RpcResultBuilder.<StartOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }


    @Override
    public ListenableFuture<RpcResult<AuditOutput>> audit(AuditInput input) {
        logger.debug("Input received : " + input.toString());
        AuditOutputBuilder outputBuilder = new AuditOutputBuilder();
        Action myAction = Action.Audit;
        String action = myAction.toString();
        String rpcName = getRpcName(myAction);
        Status status =
                ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        if (null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext()
                        .commonHeader(input.getCommonHeader()).actionIdentifiers(input.getActionIdentifiers())
                        .payload(input.getPayload()).action(action).rpcName(rpcName).build();
                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s",
                        input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildStatusWithParseException(e);

                LoggingUtils.logErrorMessage(LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());

            }
        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<AuditOutput> result =
                RpcResultBuilder.<AuditOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public ListenableFuture<RpcResult<SoftwareUploadOutput>> softwareUpload(SoftwareUploadInput input) {
        logger.debug("Input received : " + input.toString());
        SoftwareUploadOutputBuilder outputBuilder = new SoftwareUploadOutputBuilder();
        Action myAction = Action.SoftwareUpload;
        String action = myAction.toString();
        String rpcName = getRpcName(myAction);
        Status status =
                ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        if (null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext()
                        .commonHeader(input.getCommonHeader()).actionIdentifiers(input.getActionIdentifiers())
                        .payload(input.getPayload()).action(action).rpcName(rpcName).build();
                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s",
                        input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildStatusWithParseException(e);

                LoggingUtils.logErrorMessage(LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());

            }
        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<SoftwareUploadOutput> result =
                RpcResultBuilder.<SoftwareUploadOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public ListenableFuture<RpcResult<HealthCheckOutput>> healthCheck(HealthCheckInput input) {
        logger.debug("Input received : " + input.toString());
        HealthCheckOutputBuilder outputBuilder = new HealthCheckOutputBuilder();
        Action myAction = Action.HealthCheck;
        String action = myAction.toString();
        String rpcName = getRpcName(myAction);
        Status status =
                ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        if (null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext()
                        .commonHeader(input.getCommonHeader()).actionIdentifiers(input.getActionIdentifiers())
                        .payload(input.getPayload()).action(action).rpcName(rpcName).build();
                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s",
                        input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildStatusWithParseException(e);

                LoggingUtils.logErrorMessage(LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());

            }
        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<HealthCheckOutput> result =
                RpcResultBuilder.<HealthCheckOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public ListenableFuture<RpcResult<LiveUpgradeOutput>> liveUpgrade(LiveUpgradeInput input) {
        logger.debug("Input received : " + input.toString());
        LiveUpgradeOutputBuilder outputBuilder = new LiveUpgradeOutputBuilder();
        Action myAction = Action.LiveUpgrade;
        String action = myAction.toString();
        String rpcName = getRpcName(myAction);
        Status status =
                ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        if (null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext()
                        .commonHeader(input.getCommonHeader()).actionIdentifiers(input.getActionIdentifiers())
                        .payload(input.getPayload()).action(action).rpcName(rpcName).build();
                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s",
                        input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildStatusWithParseException(e);

                LoggingUtils.logErrorMessage(LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());

            }
        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<LiveUpgradeOutput> result =
                RpcResultBuilder.<LiveUpgradeOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }


    @Override
    public ListenableFuture<RpcResult<LockOutput>> lock(LockInput input) {
        logger.debug("Input received : " + input.toString());
        LockOutputBuilder outputBuilder = new LockOutputBuilder();
        Action myAction = Action.Lock;
        String action = myAction.toString();
        String rpcName = getRpcName(myAction);
        Status status =
                ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        if (null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext()
                        .commonHeader(input.getCommonHeader()).actionIdentifiers(input.getActionIdentifiers())
                        .payload(input.getPayload()).action(action).rpcName(rpcName).build();
                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s",
                        input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildStatusWithParseException(e);

                LoggingUtils.logErrorMessage(LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());

            }
        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<LockOutput> result =
                RpcResultBuilder.<LockOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }


    @Override
    public ListenableFuture<RpcResult<UnlockOutput>> unlock(UnlockInput input) {
        logger.debug("Input received : " + input.toString());
        UnlockOutputBuilder outputBuilder = new UnlockOutputBuilder();
        Action myAction = Action.Unlock;
        String action = myAction.toString();
        String rpcName = getRpcName(myAction);
        Status status =
                ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        if (null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext()
                        .commonHeader(input.getCommonHeader()).actionIdentifiers(input.getActionIdentifiers())
                        .payload(input.getPayload()).action(action).rpcName(rpcName).build();
                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s",
                        input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildStatusWithParseException(e);

                LoggingUtils.logErrorMessage(LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());

            }
        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<UnlockOutput> result =
                RpcResultBuilder.<UnlockOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public ListenableFuture<RpcResult<CheckLockOutput>> checkLock(CheckLockInput input) {
        logger.debug("Input received : " + input.toString());
        CheckLockOutputBuilder outputBuilder = new CheckLockOutputBuilder();
        Action myAction = Action.CheckLock;
        String action = myAction.toString();
        String rpcName = getRpcName(myAction);
        RequestHandlerOutput requestHandlerOutput = null;
        Status status =
                ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        if (null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext()
                        .commonHeader(input.getCommonHeader()).actionIdentifiers(input.getActionIdentifiers())
                        .action(action).rpcName(rpcName).build();
                requestHandlerOutput = executeRequest(request);

                status = buildStatusWithDispatcherOutput(requestHandlerOutput);
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s",
                        input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildStatusWithParseException(e);

                LoggingUtils.logErrorMessage(LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());

            }
        }

        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        if (requestHandlerOutput != null && requestHandlerOutput.getResponseContext().getStatus().getCode() == 400) {
            outputBuilder.setLocked(CheckLockOutput.Locked.valueOf(
                    requestHandlerOutput.getResponseContext().getAdditionalContext().get("locked").toUpperCase()));
        }
        RpcResult<CheckLockOutput> result =
                RpcResultBuilder.<CheckLockOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public ListenableFuture<RpcResult<ConfigBackupOutput>> configBackup(ConfigBackupInput input) {
        logger.debug("Input received : " + input.toString());
        ConfigBackupOutputBuilder outputBuilder = new ConfigBackupOutputBuilder();
        Action myAction = Action.ConfigBackup;
        String action = myAction.toString();
        String rpcName = getRpcName(myAction);
        Status status =
                ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        if (null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext()
                        .commonHeader(input.getCommonHeader()).actionIdentifiers(input.getActionIdentifiers())
                        .payload(input.getPayload()).action(action).rpcName(rpcName).build();
                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s",
                        input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildStatusWithParseException(e);

                LoggingUtils.logErrorMessage(LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());

            }
        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<ConfigBackupOutput> result =
                RpcResultBuilder.<ConfigBackupOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }


    @Override
    public ListenableFuture<RpcResult<ConfigBackupDeleteOutput>> configBackupDelete(ConfigBackupDeleteInput input) {
        logger.debug("Input received : " + input.toString());
        ConfigBackupDeleteOutputBuilder outputBuilder = new ConfigBackupDeleteOutputBuilder();
        Action myAction = Action.ConfigBackupDelete;
        String action = myAction.toString();
        String rpcName = getRpcName(myAction);
        Status status =
                ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        if (null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext()
                        .commonHeader(input.getCommonHeader()).actionIdentifiers(input.getActionIdentifiers())
                        .action(action).rpcName(rpcName).build();
                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s",
                        input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildStatusWithParseException(e);

                LoggingUtils.logErrorMessage(LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());

            }
        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<ConfigBackupDeleteOutput> result =
                RpcResultBuilder.<ConfigBackupDeleteOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }


    @Override
    public ListenableFuture<RpcResult<ConfigExportOutput>> configExport(ConfigExportInput input) {
        logger.debug("Input received : " + input.toString());
        ConfigExportOutputBuilder outputBuilder = new ConfigExportOutputBuilder();
        Action myAction = Action.ConfigExport;
        String action = myAction.toString();
        String rpcName = getRpcName(myAction);
        Status status =
                ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        if (null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext()
                        .commonHeader(input.getCommonHeader()).actionIdentifiers(input.getActionIdentifiers())
                        .action(action).rpcName(rpcName).build();
                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s",
                        input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildStatusWithParseException(e);

                LoggingUtils.logErrorMessage(LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());

            }
        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<ConfigExportOutput> result =
                RpcResultBuilder.<ConfigExportOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }


    public ListenableFuture<RpcResult<StopApplicationOutput>> stopApplication(StopApplicationInput input) {
        logger.debug("Input received : " + input.toString());
        StopApplicationOutputBuilder outputBuilder = new StopApplicationOutputBuilder();
        Action myAction = Action.StopApplication;
        String action = myAction.toString();
        String rpcName = getRpcName(myAction);
        Status status =
                ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        if (null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext()
                        .commonHeader(input.getCommonHeader()).actionIdentifiers(input.getActionIdentifiers())
                        .payload(input.getPayload()).action(action).rpcName(rpcName).build();
                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s",
                        input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildStatusWithParseException(e);

                LoggingUtils.logErrorMessage(LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());

            }
        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<StopApplicationOutput> result =
                RpcResultBuilder.<StopApplicationOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    RequestHandlerOutput executeRequest(RequestHandlerInput request) {
        return new RequestExecutor().executeRequest(request);
    }
}
