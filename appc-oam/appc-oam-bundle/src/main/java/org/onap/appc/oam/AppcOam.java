/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications (C) 2018 Ericsson
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

package org.onap.appc.oam;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.AppcOamService;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.AppcState;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.GetAppcStateInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.GetAppcStateOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.GetAppcStateOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.GetMetricsInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.GetMetricsOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.GetMetricsOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.MaintenanceModeInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.MaintenanceModeOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.MaintenanceModeOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.RestartInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.RestartOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.RestartOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.StartInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.StartOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.StartOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.StopInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.StopOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.StopOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.common.header.CommonHeader;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.get.metrics.output.Metrics;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.get.metrics.output.MetricsBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.get.metrics.output.metrics.KpiValues;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.get.metrics.output.metrics.KpiValuesBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.status.Status;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.i18n.Msg;
import org.onap.appc.metricservice.MetricRegistry;
import org.onap.appc.metricservice.MetricService;
import org.onap.appc.metricservice.metric.Metric;
import org.onap.appc.oam.processor.OamMmodeProcessor;
import org.onap.appc.oam.processor.OamRestartProcessor;
import org.onap.appc.oam.processor.OamStartProcessor;
import org.onap.appc.oam.processor.OamStopProcessor;
import org.onap.appc.oam.util.AsyncTaskHelper;
import org.onap.appc.oam.util.ConfigurationHelper;
import org.onap.appc.oam.util.OperationHelper;
import org.onap.appc.oam.util.StateHelper;
import org.onap.appc.statemachine.impl.readers.AppcOamMetaDataReader.AppcOperation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * RPC class of APP-C OAM API.
 * <p>Implement all the RPCs defined in AppcOamService through yang model definition.
 * <p>All RPC methods' JAVADOC are using "inheritDoc" to use the description from the yang model file.
 */
public class AppcOam implements AutoCloseable, AppcOamService {
    /**
     * Invalid state message format with fliexible operation, appc name and state values
     */
    public static final String INVALID_STATE_MESSAGE_FORMAT = "%s API is not allowed when %s is in the %s state.";

    private final EELFLogger logger = EELFManager.getInstance().getLogger(AppcOam.class);

    private boolean isMetricEnabled = false;

    /**
     * Represents our RPC implementation registration
     */
    private BindingAwareBroker.RpcRegistration<AppcOamService> rpcRegistration;


    /**
     * The yang rpc names with value mapping to AppcOperation
     */
    public enum RPC {
        maintenance_mode(AppcOperation.MaintenanceMode),
        start(AppcOperation.Start),
        stop(AppcOperation.Stop),
        restart(AppcOperation.Restart);

        AppcOperation appcOperation;

        RPC(AppcOperation appcOperation) {
            this.appcOperation = appcOperation;
        }

        public AppcOperation getAppcOperation() {
            return appcOperation;
        }
    }

    private AsyncTaskHelper asyncTaskHelper;
    private ConfigurationHelper configurationHelper;
    private OperationHelper operationHelper;
    private StateHelper stateHelper;

    /**
     * APP-C OAM contructor
     *
     * @param dataBroker                  object of The ODL data store broker. Provides access to a conceptual data
     *                                    tree store
     *                                    and also provides the ability to subscribe for changes to data under a
     *                                    given branch
     *                                    of the tree. Not used in this class.
     * @param notificationPublishService object of ODL Notification Service that provides publish/subscribe
     *                                    capabilities for YANG modeled notifications. Not used in this class.
     * @param rpcProviderRegistry         object of RpcProviderResigstry. Used to register our RPCs.
     */
    @SuppressWarnings({"unused", "nls"})
    public AppcOam(DataBroker dataBroker,
                   NotificationPublishService notificationPublishService,
                   RpcProviderRegistry rpcProviderRegistry) {

        configurationHelper = new ConfigurationHelper(logger);
        String appName = configurationHelper.getAppcName();
        logger.info(Msg.COMPONENT_INITIALIZING, appName, "oam");

        if (rpcProviderRegistry != null) {
            rpcRegistration = rpcProviderRegistry.addRpcImplementation(AppcOamService.class, this);
        }

        isMetricEnabled = configurationHelper.isMetricEnabled();

        initHelpers();

        logger.info(Msg.COMPONENT_INITIALIZED, appName, "oam");
    }

    /**
     * Initialize helper classes.
     * <p>Note: ConfigurationHelper initializetion is in included here
     * because it is needed for extracting the AppName used in the debug logs within the constructor.
     */
    private void initHelpers() {
        operationHelper = new OperationHelper();
        asyncTaskHelper = new AsyncTaskHelper(logger);
        stateHelper = new StateHelper(logger, configurationHelper);
    }

    /**
     * Implements the close of the service
     *
     * @see AutoCloseable#close()
     */
    @SuppressWarnings("nls")
    @Override
    public void close() throws Exception {
        String appName = configurationHelper.getAppcName();
        logger.info(Msg.COMPONENT_TERMINATING, appName, "oam");

        asyncTaskHelper.close();

        if (rpcRegistration != null) {
            rpcRegistration.close();
        }
        logger.info(Msg.COMPONENT_TERMINATED, appName, "oam");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListenableFuture<RpcResult<GetMetricsOutput>> getMetrics(GetMetricsInput getMetricsInput) {

        if (!isMetricEnabled) {
            logger.error("Metric Service not enabled returning failure");
            RpcResult<GetMetricsOutput> result = RpcResultBuilder.<GetMetricsOutput>
                    status(false).withError(RpcError.ErrorType.APPLICATION, "Metric Service not enabled").build();
            return Futures.immediateFuture(result);
        }

        MetricService metricService;
        try {
            metricService = operationHelper.getService(MetricService.class);
        } catch (APPCException e) {
            logger.error("MetricService not found", e);
            RpcResult<GetMetricsOutput> result = RpcResultBuilder.<GetMetricsOutput>
                    status(false).withError(RpcError.ErrorType.APPLICATION, "Metric Service not found").build();
            return Futures.immediateFuture(result);
        }

        Map<String, MetricRegistry> allMetricRegitry = metricService.getAllRegistry();
        if (allMetricRegitry == null || allMetricRegitry.isEmpty()) {
            logger.error("No metrics registered returning failure");
            RpcResult<GetMetricsOutput> result = RpcResultBuilder.<GetMetricsOutput>
                    status(false).withError(RpcError.ErrorType.APPLICATION, "No metrics Registered").build();
            return Futures.immediateFuture(result);
        }

        List<Metrics> metricsList = new ArrayList<>();

        logger.debug("Iterating metric registry list");
        for (MetricRegistry metricRegistry : allMetricRegitry.values()) {
            logger.debug("Iterating metric registry :" + metricRegistry.toString());
            Metric[] metrics = metricRegistry.metrics();
            if (metrics != null && metrics.length > 0) {
                logger.debug("Iterating though metrics in registry");
                for (Metric metric : metrics) {
                    logger.debug("Iterating though metrics: " + metric.name());
                    MetricsBuilder metricsBuilder = new MetricsBuilder();
                    metricsBuilder.setKpiName(metric.name());
                    metricsBuilder.setLastResetTime(metric.getLastModified());
                    List<KpiValues> kpiList = new ArrayList<>();
                    Map<String, String> metricsOutput = metric.getMetricsOutput();
                    for (Map.Entry<String, String> kpi : metricsOutput.entrySet()) {
                        KpiValuesBuilder kpiValuesBuilder = new KpiValuesBuilder();
                        kpiValuesBuilder.setName(kpi.getKey());
                        kpiValuesBuilder.setValue(kpi.getValue());
                        kpiList.add(kpiValuesBuilder.build());
                    }
                    metricsBuilder.setKpiValues(kpiList);
                    metricsList.add(metricsBuilder.build());
                }
            }
        }

        GetMetricsOutputBuilder outputBuilder = new GetMetricsOutputBuilder();
        outputBuilder.setMetrics(metricsList);
        RpcResult<GetMetricsOutput> result = RpcResultBuilder.<GetMetricsOutput>
                status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListenableFuture<RpcResult<StopOutput>> stop(StopInput stopInput) {
        logger.debug("Entering Stop with Input : " + stopInput);
        final CommonHeader commonHeader = stopInput.getCommonHeader();

        OamStopProcessor oamStopProcessor =
                getOamStopProcessor(logger, configurationHelper, stateHelper, asyncTaskHelper, operationHelper);
        Status status = oamStopProcessor.processRequest(stopInput);

        StopOutputBuilder stopOutputBuilder = new StopOutputBuilder();
        stopOutputBuilder.setStatus(status);
        stopOutputBuilder.setCommonHeader(commonHeader);
        return RpcResultBuilder.success(stopOutputBuilder.build()).buildFuture();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListenableFuture<RpcResult<RestartOutput>> restart(RestartInput input) {
        logger.debug("Entering restart with Input : " + input);
        final CommonHeader commonHeader = input.getCommonHeader();

        OamRestartProcessor oamRestartProcessor =
                getOamRestartProcessor(logger, configurationHelper, stateHelper, asyncTaskHelper, operationHelper);
        Status status = oamRestartProcessor.processRequest(input);

        RestartOutputBuilder restartOutputBuilder = new RestartOutputBuilder();
        restartOutputBuilder.setStatus(status);
        restartOutputBuilder.setCommonHeader(commonHeader);

        return RpcResultBuilder.success(restartOutputBuilder.build()).buildFuture();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListenableFuture<RpcResult<MaintenanceModeOutput>> maintenanceMode(MaintenanceModeInput maintenanceModeInput) {
        logger.debug("Entering MaintenanceMode with Input : " + maintenanceModeInput);
        final CommonHeader commonHeader = maintenanceModeInput.getCommonHeader();

        OamMmodeProcessor oamMmodeProcessor =
                getOamMmodeProcessor(logger, configurationHelper, stateHelper, asyncTaskHelper, operationHelper);
        Status status = oamMmodeProcessor.processRequest(maintenanceModeInput);

        MaintenanceModeOutputBuilder maintenanceModeOutputBuilder = new MaintenanceModeOutputBuilder();
        maintenanceModeOutputBuilder.setStatus(status);
        maintenanceModeOutputBuilder.setCommonHeader(commonHeader);
        return RpcResultBuilder.success(maintenanceModeOutputBuilder.build()).buildFuture();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListenableFuture<RpcResult<GetAppcStateOutput>> getAppcState(GetAppcStateInput getAppcStateInput) {
        AppcState appcState = stateHelper.getCurrentOamYangState();

        GetAppcStateOutputBuilder builder = new GetAppcStateOutputBuilder();
        builder.setState(appcState);
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListenableFuture<RpcResult<StartOutput>> start(StartInput startInput) {
        logger.debug("Input received : " + startInput);
        final CommonHeader commonHeader = startInput.getCommonHeader();

        OamStartProcessor oamStartProcessor =
                getOamStartProcessor(logger, configurationHelper, stateHelper, asyncTaskHelper, operationHelper);
        Status status = oamStartProcessor.processRequest(startInput);

        StartOutputBuilder startOutputBuilder = new StartOutputBuilder();
        startOutputBuilder.setStatus(status);
        startOutputBuilder.setCommonHeader(commonHeader);
        StartOutput startOutput = startOutputBuilder.build();
        return RpcResultBuilder.success(startOutput).buildFuture();
    }

    protected OamStartProcessor getOamStartProcessor(EELFLogger eelfLogger,
            ConfigurationHelper configurationHelper,
            StateHelper stateHelper,
            AsyncTaskHelper asyncTaskHelper,
            OperationHelper operationHelper) {
        return new OamStartProcessor(eelfLogger, configurationHelper, stateHelper, asyncTaskHelper, operationHelper);
    }

    protected OamStopProcessor getOamStopProcessor(EELFLogger eelfLogger,
            ConfigurationHelper configurationHelper,
            StateHelper stateHelper,
            AsyncTaskHelper asyncTaskHelper,
            OperationHelper operationHelper) {
        return new OamStopProcessor(eelfLogger, configurationHelper, stateHelper, asyncTaskHelper, operationHelper);
    }

    protected OamRestartProcessor getOamRestartProcessor(EELFLogger eelfLogger,
            ConfigurationHelper configurationHelper,
            StateHelper stateHelper,
            AsyncTaskHelper asyncTaskHelper,
            OperationHelper operationHelper) {
        return new OamRestartProcessor(eelfLogger, configurationHelper, stateHelper, asyncTaskHelper, operationHelper);
    }

    protected OamMmodeProcessor getOamMmodeProcessor(EELFLogger eelfLogger,
            ConfigurationHelper configurationHelper,
            StateHelper stateHelper,
            AsyncTaskHelper asyncTaskHelper,
            OperationHelper operationHelper) {
        return new OamMmodeProcessor(eelfLogger, configurationHelper, stateHelper, asyncTaskHelper, operationHelper);
    }
}
