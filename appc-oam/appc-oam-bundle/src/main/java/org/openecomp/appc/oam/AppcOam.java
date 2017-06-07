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

package org.openecomp.appc.oam;

import org.openecomp.appc.Constants;
import org.openecomp.appc.configuration.Configuration;
import org.openecomp.appc.configuration.ConfigurationFactory;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.executor.objects.Params;
import org.openecomp.appc.i18n.Msg;
import org.openecomp.appc.logging.LoggingConstants;
import org.openecomp.appc.logging.LoggingUtils;
import org.openecomp.appc.metricservice.MetricRegistry;
import org.openecomp.appc.metricservice.MetricService;
import org.openecomp.appc.metricservice.metric.Metric;
import org.openecomp.appc.requesthandler.LCMStateManager;
import org.openecomp.appc.requesthandler.RequestHandler;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import com.google.common.util.concurrent.Futures;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.oam.rev170303.*;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.oam.rev170303.common.header.CommonHeader;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.oam.rev170303.get.metrics.output.Metrics;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.oam.rev170303.get.metrics.output.MetricsBuilder;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.oam.rev170303.get.metrics.output.metrics.KpiValues;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.oam.rev170303.get.metrics.output.metrics.KpiValuesBuilder;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.oam.rev170303.status.Status;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.oam.rev170303.status.StatusBuilder;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.MDC;

import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.*;

import org.openecomp.appc.oam.messageadapter.*;


import static com.att.eelf.configuration.Configuration.*;

import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class AppcOam implements AutoCloseable, AppcOamService {

    private Configuration configuration = ConfigurationFactory.getConfiguration();
    private final EELFLogger logger = EELFManager.getInstance().getLogger(AppcOam.class);

    private boolean isMetricEnabled = false;


    private final ScheduledExecutorService scheduledExecutorService;

    private volatile ScheduledFuture<?> outstandingLCMRequestMonitorSheduledFuture;


    private MessageAdapter messageAdapter;


    /**
     * The ODL data store broker. Provides access to a conceptual data tree store and also provides the ability to
     * subscribe for changes to data under a given branch of the tree.
     */
    private DataBroker dataBroker;

    /**
     * ODL Notification Service that provides publish/subscribe capabilities for YANG modeled notifications.
     */
    private NotificationProviderService notificationService;

    /**
     * Provides a registry for Remote Procedure Call (RPC) service implementations. The RPCs are defined in YANG models.
     */
    private RpcProviderRegistry rpcRegistry;

    /**
     * Represents our RPC implementation registration
     */
    private BindingAwareBroker.RpcRegistration<AppcOamService> rpcRegistration;


    /**
     * The yang rpc names
     */
    public enum RPC {
        start,
        stop,
        ;
    }


    /**
     * @param dataBroker
     * @param notificationProviderService
     * @param rpcProviderRegistry
     */
    @SuppressWarnings({
            "javadoc", "nls"
    })
    public AppcOam(DataBroker dataBroker, NotificationProviderService notificationProviderService,
                   RpcProviderRegistry rpcProviderRegistry) {

        String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);
        logger.info(Msg.COMPONENT_INITIALIZING, appName, "oam");

        this.dataBroker = dataBroker;
        this.notificationService = notificationProviderService;
        this.rpcRegistry = rpcProviderRegistry;

        if (this.rpcRegistry != null) {
            rpcRegistration = rpcRegistry.addRpcImplementation(AppcOamService.class, this);
        }

        Properties properties = configuration.getProperties();
        if (properties != null && properties.getProperty("metric.enabled") != null) {
            isMetricEnabled = Boolean.valueOf(properties.getProperty("metric.enabled"));
        }


        messageAdapter = new MessageAdapter();
        messageAdapter.init();


        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(
                new ThreadFactory(){

                    @Override
                    public Thread newThread(Runnable runnable) {
                        Bundle bundle = FrameworkUtil.getBundle(AppcOam.class);
                        return new Thread(runnable,bundle.getSymbolicName() + " scheduledExecutor");
                    }
                }
        );

        logger.info(Msg.COMPONENT_INITIALIZED, appName, "oam");
    }

    /**
     * Implements the close of the service
     *
     * @see AutoCloseable#close()
     */
    @SuppressWarnings("nls")
    @Override
    public void close() throws Exception {
        String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);
        logger.info(Msg.COMPONENT_TERMINATING, appName, "oam");
        scheduledExecutorService.shutdown();
        if (rpcRegistration != null) {
            rpcRegistration.close();
        }
        logger.info(Msg.COMPONENT_TERMINATED, appName, "oam");
    }

    @Override
    public Future<RpcResult<GetMetricsOutput>> getMetrics() {

        GetMetricsOutputBuilder outputBuilder = new GetMetricsOutputBuilder();

        if (!isMetricEnabled){
            logger.error("Metric Service not enabled returning failure");
            RpcResult<GetMetricsOutput> result = RpcResultBuilder.<GetMetricsOutput> status(false).withError(RpcError.ErrorType.APPLICATION,"Metric Service not enabled").build();
            return Futures.immediateFuture(result);
        }

        MetricService metricService = null;
        try {
            metricService = getService(MetricService.class);
        } catch (APPCException e){
            logger.error("MetricService not found",e);
            RpcResult<GetMetricsOutput> result = RpcResultBuilder.<GetMetricsOutput> status(false).withError(RpcError.ErrorType.APPLICATION,"Metric Service not found").build();
            return Futures.immediateFuture(result);
        }
        Map<String,MetricRegistry> allMetricRegitry = metricService.getAllRegistry();

        if(allMetricRegitry == null || allMetricRegitry.isEmpty()){
            logger.error("No metrics registered returning failure");
            RpcResult<GetMetricsOutput> result = RpcResultBuilder.<GetMetricsOutput> status(false).withError(RpcError.ErrorType.APPLICATION,"No metrics Registered").build();
            return Futures.immediateFuture(result);
        }
        List<Metrics> metricsList = new ArrayList<>();

        logger.debug("Iterating metric registry list");
        for (MetricRegistry metricRegistry :  allMetricRegitry.values() ) {
            logger.debug("Iterating metric registry :" + metricRegistry.toString());
            Metric[] metrics = metricRegistry.metrics() ;
            if(metrics!= null && metrics.length >0) {
                logger.debug("Iterating though metrics in registry");
                for (Metric metric : metrics) {
                    logger.debug("Iterating though metrics: "+ metric.name());
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
        outputBuilder.setMetrics(metricsList);
        RpcResult<GetMetricsOutput> result = RpcResultBuilder.<GetMetricsOutput> status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    @Override
    public Future<RpcResult<StopOutput>> stop(StopInput stopInput){
        logger.debug("Input received : " + stopInput);
        final Date startTime = new Date();
        Status status = this.buildStatus(OAMCommandStatus.ACCEPTED);
        final  CommonHeader commonHeader = stopInput.getCommonHeader();

        try {
            setInitialLogProperties(commonHeader,RPC.stop);

            //Close the gate so that no more new LCM request will be excepted.
            LCMStateManager lcmStateManager = getService(LCMStateManager.class);
            lcmStateManager.disableLCMOperations();
            //Begin monitoring outstanding LCM request
            scheduleOutstandingLCMRequestMonitor(commonHeader,startTime);
        } catch(Throwable t) {
            status = unexpectedOAMError(t,RPC.stop);
        }
        finally {
            LoggingUtils.auditWarn(startTime.toInstant(),
                    new Date(System.currentTimeMillis()).toInstant(),
                    String.valueOf(status.getCode()),
                    status.getMessage(),
                    this.getClass().getCanonicalName(),
                    Msg.OAM_OPERATION_STOPPING,
                    getAppcName()
            );
            this.clearRequestLogProperties();
        }

        StopOutputBuilder stopOutputBuilder = new StopOutputBuilder();
        stopOutputBuilder.setStatus(status);
        stopOutputBuilder.setCommonHeader(commonHeader);
        StopOutput stopOutput = stopOutputBuilder.build();
        return RpcResultBuilder.success(stopOutput).buildFuture();
    }

    @Override
    public Future<RpcResult<StartOutput>> start(StartInput startInput){
        logger.debug("Input received : " + startInput);
        final Date startTime = new Date();
        Status status = this.buildStatus(OAMCommandStatus.ACCEPTED);
        final CommonHeader commonHeader = startInput.getCommonHeader();

        try {


            setInitialLogProperties(commonHeader,RPC.start);

            this.scheduleStartingAPPC(commonHeader,startTime);
        } catch(Throwable t) {
            status = unexpectedOAMError(t,RPC.start);
        }
        finally {
            LoggingUtils.auditWarn(startTime.toInstant(),
                    new Date(System.currentTimeMillis()).toInstant(),
                    String.valueOf(status.getCode()),
                    status.getMessage(),
                    this.getClass().getCanonicalName(),
                    Msg.OAM_OPERATION_STARTING,
                    getAppcName()
            );
            this.clearRequestLogProperties();
        }

        StartOutputBuilder startOutputBuilder = new StartOutputBuilder();
        startOutputBuilder.setStatus(status);
        startOutputBuilder.setCommonHeader(commonHeader);
        StartOutput startOutput = startOutputBuilder.build();
        return RpcResultBuilder.success(startOutput).buildFuture();
    }

    private <T> T getService(Class<T> _class) throws APPCException {
        BundleContext bctx = FrameworkUtil.getBundle(_class).getBundleContext();
        ServiceReference sref = bctx.getServiceReference(_class.getName());
        if (sref != null) {
            if(logger.isTraceEnabled()) {
                logger.debug("Using the BundleContext to fetched the service reference for " + _class.getName());

            }
            return (T) bctx.getService(sref);
        } else {
            throw new APPCException("Using the BundleContext failed to to fetch service reference for " + _class.getName());
        }
    }

    private Status buildStatus(OAMCommandStatus osmCommandStatus){
        StatusBuilder status = new StatusBuilder();
        status.setCode(osmCommandStatus.getResponseCode());
        status.setMessage(osmCommandStatus.getResponseMessage());
        return status.build();
    }

    private Status buildStatus(OAMCommandStatus osmCommandStatus,Params params){
        StatusBuilder status = new StatusBuilder();
        status.setCode(osmCommandStatus.getResponseCode());
        status.setMessage(osmCommandStatus.getFormattedMessage(params));
        return status.build();
    }



    private void clearRequestLogProperties() {
        try {
            MDC.remove(MDC_KEY_REQUEST_ID);
            MDC.remove(MDC_SERVICE_INSTANCE_ID);
            MDC.remove(MDC_SERVICE_NAME);
            MDC.remove(LoggingConstants.MDCKeys.PARTNER_NAME);
            MDC.remove(LoggingConstants.MDCKeys.TARGET_VIRTUAL_ENTITY);
        } catch (Exception e) {

        }
    }

    private void setInitialLogProperties(CommonHeader commonHeader,RPC action) {

        try {
            MDC.put(MDC_KEY_REQUEST_ID, commonHeader.getRequestId());
            MDC.put(LoggingConstants.MDCKeys.PARTNER_NAME, commonHeader.getOriginatorId());
            MDC.put(MDC_INSTANCE_UUID, ""); // value should be created in the future
            try {
                MDC.put(MDC_SERVER_FQDN, InetAddress.getLocalHost().getCanonicalHostName()); //Don't change it to a .getHostName() again please. It's wrong!
                MDC.put(MDC_SERVER_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress());
                MDC.put(LoggingConstants.MDCKeys.SERVER_NAME, InetAddress.getLocalHost().getHostName());
                MDC.put(MDC_SERVICE_NAME, action.name());
            } catch (Exception e) {
                logger.debug("MDC constant error",e);
            }
        } catch (RuntimeException e) {
            //ignore
        }
    }


    private void storeErrorMessageToLog(Status status, String additionalMessage) {
        LoggingUtils.logErrorMessage(
                String.valueOf(status.getCode()),
                status.getMessage(),
                LoggingConstants.TargetNames.APPC,
                LoggingConstants.TargetNames.APPC_OAM_PROVIDER,
                additionalMessage,
                this.getClass().getCanonicalName());
    }

    private String getAppcName(){
        return configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);
    }

    private Status unexpectedOAMError(Throwable t,RPC action){
        final String appName = getAppcName();

        String exceptionMessage = t.getMessage() != null ? t.getMessage() : t.toString();

        String errorMessage = EELFResourceManager.format(Msg.OAM_OPERATION_EXCEPTION, t, appName, t.getClass().getSimpleName(), action.name(), exceptionMessage);

        Params params = new Params().addParam("errorMsg", exceptionMessage);
        Status status = buildStatus(
                OAMCommandStatus.UNEXPECTED_ERROR,
                params
        );

        storeErrorMessageToLog(status,errorMessage);
        return status;
    }


    private int getInprogressLCMRequestCount() throws APPCException {
        RequestHandler requestHandler = getService(RequestHandler.class);

        if(requestHandler == null) {
            return 0;
        }

        int inprogressRequestCount = requestHandler.getInprogressRequestCount();
        return inprogressRequestCount;
    }



    private void scheduleOutstandingLCMRequestMonitor(final CommonHeader commonHeader,final Date startTime){


        class MyCommand implements Runnable{

            public ScheduledFuture<?> myScheduledFuture = null;

            @Override
            public void run() {
                try {
                    setInitialLogProperties(commonHeader, RPC.stop);


                    logDebug("Executing stopping task ");

                    ScheduledFuture<?> currentScheduledFuture = AppcOam.this.outstandingLCMRequestMonitorSheduledFuture;

                    //cancel myself if I am not the current outstandingLCMRequestMonitor
                    if(currentScheduledFuture != myScheduledFuture){
                        myScheduledFuture.cancel(false);
                        return;
                    }

                    Status status = buildStatus(OAMCommandStatus.SUCCESS);


                    try {

                        //log status and return if there are still LCM request in progress
                        int inprogressRequestCount = getInprogressLCMRequestCount();
                        if (inprogressRequestCount > 0) {
                            logDebug("The application '%s' has '%s' outstanding LCM request to complete before coming to a complete stop.  ",
                                                getAppcName(),
                                                inprogressRequestCount
                                        );
                            return;
                        }

                    } catch (Throwable t) {
                        status = unexpectedOAMError(t, RPC.stop);
                        myScheduledFuture.cancel(false);
                    }

                    try {
                        OAMContext oamContext = new OAMContext();
                        oamContext.setRpcName(RPC.stop);
                        oamContext.setCommonHeader(commonHeader);
                        oamContext.setStatus(status);
                        messageAdapter.post(oamContext);
                    } catch(Throwable t) {
                        status = unexpectedOAMError(t,RPC.stop);
                    }

                    LoggingUtils.auditWarn(startTime.toInstant(),
                            new Date(System.currentTimeMillis()).toInstant(),
                            String.valueOf(status.getCode()),
                            status.getMessage(),
                            this.getClass().getCanonicalName(),
                            Msg.OAM_OPERATION_STOPPED,
                            getAppcName()
                    );
                    myScheduledFuture.cancel(false);

                } finally {
                    clearRequestLogProperties();
                }
            }
        };

        MyCommand command = new MyCommand();

        long initialDelay = 10000;
        long delay = initialDelay;


        command.myScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(
                command,
                initialDelay,
                delay,
                TimeUnit.MILLISECONDS
        );
        this.outstandingLCMRequestMonitorSheduledFuture = command.myScheduledFuture;
    }




    private void scheduleStartingAPPC(final CommonHeader commonHeader,final Date startTime){


        class MyCommand implements Runnable{


            @Override
            public void run() {
                try {
                    setInitialLogProperties(commonHeader, RPC.start);

                    logDebug("Executing starting task ");

                    Status status = buildStatus(OAMCommandStatus.SUCCESS);

                    try {
                        LCMStateManager lcmStateManager = getService(LCMStateManager.class);
                        lcmStateManager.enableLCMOperations();
                        //cancel the current outstandingLCMRequestMonitor
                        outstandingLCMRequestMonitorSheduledFuture = null;
                    } catch(Throwable t) {
                        status = unexpectedOAMError(t,RPC.start);
                    }

                    try {
                       OAMContext oamContext = new OAMContext();
                       oamContext.setRpcName(RPC.start);
                       oamContext.setCommonHeader(commonHeader);
                       oamContext.setStatus(status);
                       messageAdapter.post(oamContext);
                    } catch(Throwable t) {
                      status = unexpectedOAMError(t,RPC.start);
                    }

                    LoggingUtils.auditWarn(startTime.toInstant(),
                            new Date(System.currentTimeMillis()).toInstant(),
                            String.valueOf(status.getCode()),
                            status.getMessage(),
                            this.getClass().getCanonicalName(),
                            Msg.OAM_OPERATION_STARTED,
                            getAppcName()
                    );
                } finally {
                    clearRequestLogProperties();
                }
            }
        };

        MyCommand command = new MyCommand();
        long initialDelay = 1000;

        scheduledExecutorService.schedule(
                command,
                initialDelay,
                TimeUnit.MILLISECONDS
        );
    }


    private void logDebug(String message,Object... args){
        if (logger.isDebugEnabled()) {
            logger.debug(String.format(message,args));
        }
    }
}
