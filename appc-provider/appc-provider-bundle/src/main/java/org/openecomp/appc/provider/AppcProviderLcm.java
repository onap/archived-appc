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

import java.text.ParseException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.lcm.rev160108.common.header.CommonHeader;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.lcm.rev160108.*;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.lcm.rev160108.status.Status;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.lcm.rev160108.status.StatusBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.openecomp.appc.Constants;
import org.openecomp.appc.configuration.Configuration;
import org.openecomp.appc.configuration.ConfigurationFactory;
import org.openecomp.appc.domainmodel.lcm.ResponseContext;
import org.openecomp.appc.domainmodel.lcm.RuntimeContext;
import org.openecomp.appc.executor.objects.LCMCommandStatus;
import org.openecomp.appc.executor.objects.Params;
import org.openecomp.appc.i18n.Msg;
import org.openecomp.appc.logging.LoggingConstants;
import org.openecomp.appc.logging.LoggingUtils;
import org.openecomp.appc.provider.lcm.util.RequestInputBuilder;
import org.openecomp.appc.provider.lcm.util.ValidationService;
import org.openecomp.appc.requesthandler.RequestHandler;
import org.openecomp.appc.requesthandler.objects.RequestHandlerInput;
import org.openecomp.appc.requesthandler.objects.RequestHandlerOutput;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import com.google.common.util.concurrent.Futures;
import org.slf4j.MDC;


public class AppcProviderLcm implements AutoCloseable, AppcProviderLcmService {

    private Configuration configuration = ConfigurationFactory.getConfiguration();
    private final EELFLogger logger = EELFManager.getInstance().getLogger(AppcProviderLcm.class);

    private final ExecutorService executor;

    private final String COMMON_ERROR_MESSAGE_TEMPLATE =  "Error processing %s input : %s";

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
    protected BindingAwareBroker.RpcRegistration<AppcProviderLcmService> rpcRegistration;


    /**
     * @param dataBroker
     * @param notificationProviderService
     * @param rpcProviderRegistry
     */
    @SuppressWarnings({
            "javadoc", "nls"
    })
    public AppcProviderLcm(DataBroker dataBroker, NotificationProviderService notificationProviderService,
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
     * @see org.opendaylight.yang.gen.v1.org.openecomp.appc.rev160108.AppcProviderLcmService#rebuild(RebuildInput)
     */
    @Override
    public Future<RpcResult<RebuildOutput>> rebuild(RebuildInput input) {
        logger.debug("Input received : " + input.toString());

        RebuildOutputBuilder outputBuilder = new RebuildOutputBuilder();
        String action = Action.Rebuild.toString() ;
        String rpcName = Action.Rebuild.name().toLowerCase();
        Status status = ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        if(null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext().commonHeader(input.getCommonHeader()).actionIdentifiers(input.getActionIdentifiers()).payload(input.getPayload()).action(action).rpcName(rpcName).build();
                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s", input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildParsingErrorStatus(e);

                LoggingUtils.logErrorMessage(
                        LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());

            }
        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<RebuildOutput> result = RpcResultBuilder.<RebuildOutput> status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);

    }


    /**
     * Restarts a specific VNF
     *
     * @see org.opendaylight.yang.gen.v1.org.openecomp.appc.rev160108.AppcProviderLcmService#restart(RestartInput)
     */
    @Override
    public Future<RpcResult<RestartOutput>> restart(RestartInput input) {
        logger.debug("Input received : " + input.toString());

        RestartOutputBuilder outputBuilder = new RestartOutputBuilder();
        String action = Action.Restart.toString() ;
        String rpcName = Action.Restart.name().toLowerCase();
        Status status = ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        if(null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext()
                        .commonHeader(input.getCommonHeader())
                        .actionIdentifiers(input.getActionIdentifiers())
                        .payload(input.getPayload())
                        .action(action)
                        .rpcName(rpcName)
                        .build();

                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s", input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildParsingErrorStatus(e);

                LoggingUtils.logErrorMessage(
                        LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());

            }
        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<RestartOutput> result = RpcResultBuilder.<RestartOutput> status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }


    /**
     * Migrates a specific VNF
     *
     * @see org.opendaylight.yang.gen.v1.org.openecomp.appc.rev160108.AppcProviderLcmService#migrate(MigrateInput)
     */
    @Override
    public Future<RpcResult<MigrateOutput>> migrate(MigrateInput input) {
        logger.debug("Input received : " + input.toString());

        MigrateOutputBuilder outputBuilder = new MigrateOutputBuilder();
        String action = Action.Migrate.toString() ;
        String rpcName = Action.Migrate.name().toLowerCase();
        Status status = ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        if(null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext().commonHeader(input.getCommonHeader()).actionIdentifiers(input.getActionIdentifiers()).payload(input.getPayload()).action(action).rpcName(rpcName).build();
                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s", input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildParsingErrorStatus(e);

                LoggingUtils.logErrorMessage(
                        LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());

            }
        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<MigrateOutput> result = RpcResultBuilder.<MigrateOutput> status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }


    /**
     * Evacuates a specific VNF
     *
     * @see org.opendaylight.yang.gen.v1.org.openecomp.appc.rev160108.AppcProviderLcmService#evacuate(EvacuateInput)
     */
    @Override
    public Future<RpcResult<EvacuateOutput>> evacuate(EvacuateInput input) {
        logger.debug("Input received : " + input.toString());

        EvacuateOutputBuilder outputBuilder = new EvacuateOutputBuilder();
        String action = Action.Evacuate.toString() ;
        String rpcName = Action.Evacuate.name().toLowerCase();
        Status status = ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        if(null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext().commonHeader(input.getCommonHeader()).actionIdentifiers(input.getActionIdentifiers()).payload(input.getPayload()).action(action).rpcName(rpcName).build();
                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s", input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildParsingErrorStatus(e);

                LoggingUtils.logErrorMessage(
                        LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());

            }
        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<EvacuateOutput> result = RpcResultBuilder.<EvacuateOutput> status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }


    /**
     * Evacuates a specific VNF
     *
     * @see org.opendaylight.yang.gen.v1.org.openecomp.appc.rev160108.AppcProviderLcmService#snapshot(SnapshotInput)
     */
    @Override
    public Future<RpcResult<SnapshotOutput>> snapshot(SnapshotInput input) {
        logger.debug("Input received : " + input.toString());

        SnapshotOutputBuilder outputBuilder = new SnapshotOutputBuilder();
        String action = Action.Snapshot.toString() ;
        String rpcName = Action.Snapshot.name().toLowerCase();
        Status status = ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        String identityUrl = input.getIdentityUrl();
        if(null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext().commonHeader(input.getCommonHeader()).actionIdentifiers(input.getActionIdentifiers()).payload(input.getPayload()).action(action).rpcName(rpcName).additionalContext("identity-url", identityUrl).build();
                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s", input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildParsingErrorStatus(e);

                LoggingUtils.logErrorMessage(
                        LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());
            }
        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<SnapshotOutput> result = RpcResultBuilder.<SnapshotOutput> status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public Future<RpcResult<RollbackOutput>> rollback(RollbackInput input) {
        logger.debug("Input received : " + input.toString());

        RollbackOutputBuilder outputBuilder = new RollbackOutputBuilder();
        String rpcName = Action.Rollback.toString() ;
        Status status = ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), rpcName);
        String identityUrl =  input.getIdentityUrl();
        String snapshotId = input.getSnapshotId();
        if(null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext().commonHeader(input.getCommonHeader()).actionIdentifiers(input.getActionIdentifiers()).payload(input.getPayload()).additionalContext("identity-url", identityUrl).additionalContext("snapshot-id", snapshotId).action(rpcName).build();
                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s", input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildParsingErrorStatus(e);

                LoggingUtils.logErrorMessage(
                        LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, rpcName, e.getMessage()),
                        this.getClass().getName());

            }
        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<RollbackOutput> result = RpcResultBuilder.<RollbackOutput> status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public Future<RpcResult<SyncOutput>> sync(SyncInput input) {
        logger.debug("Input received : " + input.toString());
        SyncOutputBuilder outputBuilder = new SyncOutputBuilder();
        String action = Action.Sync.toString() ;
        String rpcName = Action.Sync.name().toLowerCase();
        Status status = ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        if(null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext().commonHeader(input.getCommonHeader()).actionIdentifiers(input.getActionIdentifiers()).payload(input.getPayload()).action(action).rpcName(rpcName).build();
                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s", input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildParsingErrorStatus(e);

                LoggingUtils.logErrorMessage(
                        LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());

            }
        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<SyncOutput> result = RpcResultBuilder.<SyncOutput> status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    private Status buildParsingErrorStatus(ParseException e){
        LCMCommandStatus requestParsingFailure = LCMCommandStatus.REQUEST_PARSING_FAILED;
        String errorMessage = e.getMessage() != null ? e.getMessage() : e.toString();
        Params params = new Params().addParam("errorMsg", errorMessage);
        return buildStatus(requestParsingFailure.getResponseCode(), requestParsingFailure.getFormattedMessage(params));
    }

    private Status buildStatus(Integer code,String message){
        StatusBuilder status = new StatusBuilder();
        status.setCode(code);
        status.setMessage(message);
        return status.build();
    }

    private Status buildStatusWithDispatcherOutput(RequestHandlerOutput requestHandlerOutput){
        Integer statusCode = requestHandlerOutput.getResponseContext().getStatus().getCode();
        String statusMessage = requestHandlerOutput.getResponseContext().getStatus().getMessage();
        return  buildStatus(statusCode, statusMessage);
    }

    private RequestHandlerOutput executeRequest(RequestHandlerInput request){

        RequestHandler handler = getRequestHandler();
        RequestHandlerOutput requestHandlerOutput;
        try {
            requestHandlerOutput = handler.handleRequest(request);
        } catch (Exception e) {

            final String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);
            final String reason = EELFResourceManager.format(Msg.EXCEPTION_CALLING_DG, e, appName, e.getClass().getSimpleName(), "", e.getMessage());

            logger.info("UNEXPECTED FAILURE while executing " + request.getRequestContext().getAction().name());


            final ResponseContext responseContext = new ResponseContext();
            requestHandlerOutput = new RequestHandlerOutput();
            requestHandlerOutput.setResponseContext(responseContext);
            responseContext.setCommonHeader(request.getRequestContext().getCommonHeader());
            String errorMessage = e.getMessage() != null ? e.getMessage() : e.toString();
            Params params = new Params().addParam("errorMsg", errorMessage);
            responseContext.setStatus(LCMCommandStatus.UNEXPECTED_ERROR.toStatus(params));

            LoggingUtils.logErrorMessage(
                    LoggingConstants.TargetNames.APPC_PROVIDER,
                    reason,
                    this.getClass().getName());


        }
        return requestHandlerOutput;
    }

    private RequestHandler getRequestHandler(){
        RequestHandler handler ;
        final BundleContext context = FrameworkUtil.getBundle(RequestHandler.class).getBundleContext();
        final ServiceReference reference = context.getServiceReference(RequestHandler.class.getName());

        if (reference != null) {
            handler = (RequestHandler) context.getService(reference);
        } else {
            logger.error("Cannot find service reference for " + RequestHandler.class.getName());
            throw new RuntimeException();
        }
        return  handler ;
    }


    @Override
    public Future<RpcResult<TerminateOutput>> terminate(TerminateInput input) {
        logger.debug("Input received : " + input.toString());
        TerminateOutputBuilder outputBuilder = new TerminateOutputBuilder();
        String action = Action.Terminate.toString() ;
        String rpcName = Action.Terminate.name().toLowerCase();
        Status status = ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        if(null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext().commonHeader(input.getCommonHeader()).actionIdentifiers(input.getActionIdentifiers()).payload(input.getPayload()).action(action).rpcName(rpcName).build();
                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s", input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {

                status = buildParsingErrorStatus(e);

                LoggingUtils.logErrorMessage(
                        LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());

            }

        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<TerminateOutput> result = RpcResultBuilder.<TerminateOutput> status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public Future<RpcResult<ConfigureOutput>> configure(ConfigureInput input) {
        logger.debug("Input received : " + input.toString());
        ConfigureOutputBuilder outputBuilder = new ConfigureOutputBuilder();
        String action = Action.Configure.toString() ;
        String rpcName = "configure";
        Status status = ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        if(null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext().commonHeader(input.getCommonHeader()).actionIdentifiers(input.getActionIdentifiers()).payload(input.getPayload()).action(action).rpcName(rpcName).build();
                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s", input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildParsingErrorStatus(e);

                LoggingUtils.logErrorMessage(
                        LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());

            }
        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<ConfigureOutput> result = RpcResultBuilder.<ConfigureOutput> status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public Future<RpcResult<ConfigModifyOutput>> configModify(ConfigModifyInput input) {
        logger.debug("Input received : " + input.toString());
        ConfigModifyOutputBuilder outputBuilder = new ConfigModifyOutputBuilder();
        String action = Action.ConfigModify.toString() ;
        String rpcName = "config-modify";
        Status status = ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        if(null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext().commonHeader(input.getCommonHeader()).actionIdentifiers(input.getActionIdentifiers()).payload(input.getPayload()).action(action).rpcName(rpcName).build();
                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s", input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildParsingErrorStatus(e);

                LoggingUtils.logErrorMessage(
                        LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());

            }
        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<ConfigModifyOutput> result = RpcResultBuilder.<ConfigModifyOutput> status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public Future<RpcResult<ConfigScaleoutOutput>> configScaleout(ConfigScaleoutInput input) {
        logger.debug("Input received : " + input.toString());
        ConfigScaleoutOutputBuilder outputBuilder = new ConfigScaleoutOutputBuilder();
        String action = Action.ConfigScaleOut.toString() ;
        String rpcName = "config-scaleout";
        Status status = ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        if(null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext().commonHeader(input.getCommonHeader()).actionIdentifiers(input.getActionIdentifiers()).payload(input.getPayload()).action(action).rpcName(rpcName).build();
                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s", input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildParsingErrorStatus(e);

                LoggingUtils.logErrorMessage(
                        LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());

            }
        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<ConfigScaleoutOutput> result = RpcResultBuilder.<ConfigScaleoutOutput> status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public Future<RpcResult<ConfigRestoreOutput>> configRestore(ConfigRestoreInput input) {
        logger.debug("Input received : " + input.toString());
        ConfigRestoreOutputBuilder outputBuilder = new ConfigRestoreOutputBuilder();
        String action = Action.ConfigRestore.toString() ;
        String rpcName = "config-restore";
        Status status = ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        if(null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext().commonHeader(input.getCommonHeader()).actionIdentifiers(input.getActionIdentifiers()).payload(input.getPayload()).action(action).rpcName(rpcName).build();
                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s", input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildParsingErrorStatus(e);

                LoggingUtils.logErrorMessage(
                        LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());

            }
        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<ConfigRestoreOutput> result = RpcResultBuilder.<ConfigRestoreOutput> status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public Future<RpcResult<TestOutput>> test(TestInput input) {
        logger.debug("Input received : " + input.toString());
        TestOutputBuilder outputBuilder = new TestOutputBuilder();
        String action = Action.Test.toString() ;
        String rpcName = Action.Test.name().toLowerCase();
        Status status = ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        if(null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext().commonHeader(input.getCommonHeader()).actionIdentifiers(input.getActionIdentifiers()).payload(input.getPayload()).action(action).rpcName(rpcName).build();
                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s", input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildParsingErrorStatus(e);

                LoggingUtils.logErrorMessage(
                        LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());

            }
        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<TestOutput> result = RpcResultBuilder.<TestOutput> status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }


    @Override
    public Future<RpcResult<StopOutput>> stop(StopInput input) {
        logger.debug("Input received : " + input.toString());
        StopOutputBuilder outputBuilder = new StopOutputBuilder();
        String action = Action.Stop.toString() ;
        String rpcName = Action.Stop.name().toLowerCase();
        Status status = ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        if(null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext().commonHeader(input.getCommonHeader()).actionIdentifiers(input.getActionIdentifiers()).payload(input.getPayload()).action(action).rpcName(rpcName).build();
                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s", input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildParsingErrorStatus(e);

                LoggingUtils.logErrorMessage(
                        LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());

            }
        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<StopOutput> result = RpcResultBuilder.<StopOutput> status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    /**
     * Starts a specific VNF
     *
     * @see org.opendaylight.yang.gen.v1.org.openecomp.appc.rev160108.AppcProviderLcmService#start(StartInput)
     */
    @Override
    public Future<RpcResult<StartOutput>> start(StartInput input) {
        logger.debug("Input received : " + input.toString());

        StartOutputBuilder outputBuilder = new StartOutputBuilder();
        String action = Action.Start.toString() ;
        String rpcName = Action.Start.name().toLowerCase();
        Status status = ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        if(null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext()
                        .commonHeader(input.getCommonHeader())
                        .actionIdentifiers(input.getActionIdentifiers())
                        .payload(input.getPayload())
                        .action(action)
                        .rpcName(rpcName).build();
                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s", input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildParsingErrorStatus(e);

                LoggingUtils.logErrorMessage(
                        LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());

            }
        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<StartOutput> result = RpcResultBuilder.<StartOutput> status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }


    @Override
    public Future<RpcResult<AuditOutput>> audit(AuditInput input) {
        logger.debug("Input received : " + input.toString());
        AuditOutputBuilder outputBuilder = new AuditOutputBuilder();
        String action = Action.Audit.toString();
        String rpcName = Action.Audit.name().toLowerCase();
        Status status = ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        if(null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext().commonHeader(input.getCommonHeader()).actionIdentifiers(input.getActionIdentifiers()).payload(input.getPayload()).action(action).rpcName(rpcName).build();
                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s", input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildParsingErrorStatus(e);

                LoggingUtils.logErrorMessage(
                        LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());

            }
        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<AuditOutput> result = RpcResultBuilder.<AuditOutput> status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public Future<RpcResult<SoftwareUploadOutput>> softwareUpload(SoftwareUploadInput input) {
        logger.debug("Input received : " + input.toString());
        SoftwareUploadOutputBuilder outputBuilder = new SoftwareUploadOutputBuilder();
        String action = Action.SoftwareUpload.toString() ;
        String rpcName = convertActionNameToUrl(action);
        Status status = ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        if(null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().
                        requestContext().commonHeader(input.getCommonHeader()).actionIdentifiers(input.getActionIdentifiers()).payload(input.getPayload()).action(action).rpcName(rpcName).build();
                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s", input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildParsingErrorStatus(e);

                LoggingUtils.logErrorMessage(
                        LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());

            }
        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<SoftwareUploadOutput> result = RpcResultBuilder.<SoftwareUploadOutput> status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public Future<RpcResult<HealthCheckOutput>> healthCheck(HealthCheckInput input) {
        logger.debug("Input received : " + input.toString());
        HealthCheckOutputBuilder outputBuilder = new HealthCheckOutputBuilder();
        String action = Action.HealthCheck.toString() ;
        String rpcName = convertActionNameToUrl(action);
        Status status = ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        if(null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext().commonHeader(input.getCommonHeader()).actionIdentifiers(input.getActionIdentifiers()).payload(input.getPayload()).action(action).rpcName(rpcName).build();
                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s", input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildParsingErrorStatus(e);

                LoggingUtils.logErrorMessage(
                        LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());

            }
        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<HealthCheckOutput> result = RpcResultBuilder.<HealthCheckOutput> status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public Future<RpcResult<LiveUpgradeOutput>> liveUpgrade(LiveUpgradeInput input) {
        logger.debug("Input received : " + input.toString());
        LiveUpgradeOutputBuilder outputBuilder = new LiveUpgradeOutputBuilder();
        String action = Action.LiveUpgrade.toString() ;
        String rpcName = convertActionNameToUrl(action);
        Status status = ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        if(null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext().commonHeader(input.getCommonHeader()).actionIdentifiers(input.getActionIdentifiers()).payload(input.getPayload()).action(action).rpcName(rpcName).build();
                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s", input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildParsingErrorStatus(e);

                LoggingUtils.logErrorMessage(
                        LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());

            }
        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<LiveUpgradeOutput> result = RpcResultBuilder.<LiveUpgradeOutput> status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }


    @Override
    public Future<RpcResult<LockOutput>> lock(LockInput input) {
        logger.debug("Input received : " + input.toString());
        LockOutputBuilder outputBuilder = new LockOutputBuilder();
        String action = Action.Lock.toString() ;
        String rpcName = Action.Lock.name().toLowerCase();
        Status status = ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        if(null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext().commonHeader(input.getCommonHeader()).actionIdentifiers(input.getActionIdentifiers()).payload(input.getPayload()).action(action).rpcName(rpcName).build();
                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s", input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildParsingErrorStatus(e);

                LoggingUtils.logErrorMessage(
                        LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());

            }
        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<LockOutput> result = RpcResultBuilder.<LockOutput> status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }


    @Override
    public Future<RpcResult<UnlockOutput>> unlock(UnlockInput input) {
        logger.debug("Input received : " + input.toString());
        UnlockOutputBuilder outputBuilder = new UnlockOutputBuilder();
        String action = Action.Unlock.toString() ;
        String rpcName = Action.Unlock.name().toLowerCase();
        Status status = ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        if(null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext().commonHeader(input.getCommonHeader()).actionIdentifiers(input.getActionIdentifiers()).payload(input.getPayload()).action(action).rpcName(rpcName).build();
                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s", input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildParsingErrorStatus(e);

                LoggingUtils.logErrorMessage(
                        LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());

            }
        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<UnlockOutput> result = RpcResultBuilder.<UnlockOutput> status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public Future<RpcResult<CheckLockOutput>> checkLock(CheckLockInput input) {
        logger.debug("Input received : " + input.toString());
        CheckLockOutputBuilder outputBuilder = new CheckLockOutputBuilder();
        String action = Action.CheckLock.toString() ;
        String rpcName = Action.CheckLock.name().toLowerCase();
        RequestHandlerOutput requestHandlerOutput=null;
        Status status = ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        if(null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext().commonHeader(input.getCommonHeader()).actionIdentifiers(input.getActionIdentifiers()).action(action).rpcName(rpcName).build();
                requestHandlerOutput=executeRequest(request);

                status = buildStatusWithDispatcherOutput(requestHandlerOutput);
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s", input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildParsingErrorStatus(e);

                LoggingUtils.logErrorMessage(
                        LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());

            }
        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        if(requestHandlerOutput.getResponseContext().getStatus().getCode() == 400) {
            outputBuilder.setLocked(CheckLockOutput.Locked.valueOf(requestHandlerOutput.getResponseContext().getAdditionalContext().get("locked").toUpperCase()));
        }
        RpcResult<CheckLockOutput> result = RpcResultBuilder.<CheckLockOutput> status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public Future<RpcResult<ConfigBackupOutput>> configBackup(ConfigBackupInput input) {
        logger.debug("Input received : " + input.toString());
        ConfigBackupOutputBuilder outputBuilder = new ConfigBackupOutputBuilder();
        String action = Action.ConfigBackup.toString() ;
        String rpcName = Action.ConfigBackup.name().toLowerCase();
        Status status = ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        if(null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext().commonHeader(input.getCommonHeader()).actionIdentifiers(input.getActionIdentifiers()).action(action).rpcName(rpcName).build();
                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s", input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildParsingErrorStatus(e);

                LoggingUtils.logErrorMessage(
                        LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());

            }
        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<ConfigBackupOutput> result = RpcResultBuilder.<ConfigBackupOutput> status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }


    @Override
    public Future<RpcResult<ConfigBackupDeleteOutput>> configBackupDelete(ConfigBackupDeleteInput input) {
        logger.debug("Input received : " + input.toString());
        ConfigBackupDeleteOutputBuilder outputBuilder = new ConfigBackupDeleteOutputBuilder();
        String action = Action.ConfigBackupDelete.toString() ;
        String rpcName = Action.ConfigBackupDelete.name().toLowerCase();
        Status status = ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        if(null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext().commonHeader(input.getCommonHeader()).actionIdentifiers(input.getActionIdentifiers()).action(action).rpcName(rpcName).build();
                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s", input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildParsingErrorStatus(e);

                LoggingUtils.logErrorMessage(
                        LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());

            }
        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<ConfigBackupDeleteOutput> result = RpcResultBuilder.<ConfigBackupDeleteOutput> status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }


    @Override
    public Future<RpcResult<ConfigExportOutput>> configExport(ConfigExportInput input) {
        logger.debug("Input received : " + input.toString());
        ConfigExportOutputBuilder outputBuilder = new ConfigExportOutputBuilder();
        String action = Action.ConfigExport.toString() ;
        String rpcName = Action.ConfigExport.name().toLowerCase();
        Status status = ValidationService.getInstance().validateInput(input.getCommonHeader(), input.getAction(), action);
        if(null == status) {
            try {
                RequestHandlerInput request = new RequestInputBuilder().requestContext().commonHeader(input.getCommonHeader()).actionIdentifiers(input.getActionIdentifiers()).action(action).rpcName(rpcName).build();
                status = buildStatusWithDispatcherOutput(executeRequest(request));
                logger.info(String.format("Execute of '%s' finished with status %s. Reason: %s", input.getActionIdentifiers(), status.getCode(), status.getMessage()));
            } catch (ParseException e) {
                status = buildParsingErrorStatus(e);

                LoggingUtils.logErrorMessage(
                        LoggingConstants.TargetNames.APPC_PROVIDER,
                        String.format(COMMON_ERROR_MESSAGE_TEMPLATE, action, e.getMessage()),
                        this.getClass().getName());

            }
        }
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        RpcResult<ConfigExportOutput> result = RpcResultBuilder.<ConfigExportOutput> status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    private String convertActionNameToUrl(String action) {
        String regex = "([a-z])([A-Z]+)";
        String replacement = "$1-$2";
        return action.replaceAll(regex, replacement)
                .toLowerCase();
    }


}
