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

package org.openecomp.appc.adapter.iaas.provider.operation.impl;

import org.openecomp.appc.Constants;
import org.openecomp.appc.adapter.iaas.ProviderAdapter;
import org.openecomp.appc.adapter.iaas.impl.IdentityURL;
import org.openecomp.appc.adapter.iaas.impl.RequestContext;
import org.openecomp.appc.adapter.iaas.impl.RequestFailedException;
import org.openecomp.appc.adapter.iaas.impl.VMURL;
import org.openecomp.appc.adapter.iaas.provider.operation.common.enums.Operation;
import org.openecomp.appc.adapter.iaas.provider.operation.impl.base.ProviderServerOperation;
import org.openecomp.appc.configuration.Configuration;
import org.openecomp.appc.configuration.ConfigurationFactory;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.i18n.Msg;
import com.att.cdp.exceptions.ContextConnectionException;
import com.att.cdp.exceptions.ResourceNotFoundException;
import com.att.cdp.exceptions.ZoneException;
import com.att.cdp.zones.Context;
import com.att.cdp.zones.ImageService;
import com.att.cdp.zones.Provider;
import com.att.cdp.zones.model.Image;
import com.att.cdp.zones.model.ModelObject;
import com.att.cdp.zones.model.Server;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import org.openecomp.sdnc.sli.SvcLogicContext;
import org.glassfish.grizzly.http.util.HttpStatus;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import static org.openecomp.appc.adapter.iaas.provider.operation.common.constants.Constants.DATE_FORMAT;
import static org.openecomp.appc.adapter.utils.Constants.ADAPTER_NAME;

import org.slf4j.MDC;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


public class CreateSnapshot extends ProviderServerOperation {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(CreateSnapshot.class);
    private static EELFLogger metricsLogger = EELFManager.getInstance().getMetricsLogger();
    private static final Configuration configuration = ConfigurationFactory.getConfiguration();


    private String generateSnapshotName(String server) {
        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
        
        /*
         * Set Time for Metrics Logger
         */
        long startTime = System.currentTimeMillis();
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        df2.setTimeZone(tz);
        String startTimeStr = df.format(new Date());
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        String endTimeStr = String.valueOf(endTime);
        String durationStr = String.valueOf(duration);
        String endTimeStrUTC = df.format(new Date());
        MDC.put("EndTimestamp", endTimeStrUTC);
        MDC.put("ElapsedTime", durationStr);
        MDC.put("TargetEntity", "cdp");
        MDC.put("TargetServiceName", "create snapshot");
        MDC.put("ClassName", "org.openecomp.appc.adapter.iaas.provider.operation.impl.CreateSnapshot"); 
        
        metricsLogger.info("Snapshot Name Generated: Snapshot of %s at %s", server, df.format(new Date()));
        
        return String.format("Snapshot of %s at %s", server, df.format(new Date()));
    }

    private Image createSnapshot(RequestContext rc, Server server) throws ZoneException, RequestFailedException {
        Context context = server.getContext();
        Provider provider = context.getProvider();
        ImageService service = context.getImageService(); // Already checked access by this point

        String snapshotName = generateSnapshotName(server.getName());
        
        /*
         * Set Time for Metrics Logger
         */
        long startTime = System.currentTimeMillis();
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        df.setTimeZone(tz);
        String startTimeStr = df.format(new Date());
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        String endTimeStr = String.valueOf(endTime);
        String durationStr = String.valueOf(duration);
        String endTimeStrUTC = df.format(new Date());
        MDC.put("EndTimestamp", endTimeStrUTC);
        MDC.put("ElapsedTime", durationStr);
        MDC.put("TargetEntity", "cdp");
        MDC.put("TargetServiceName", "create snapshot");
        MDC.put("ClassName", "org.openecomp.appc.adapter.iaas.provider.operation.impl.CreateSnapshot"); 

        logger.info(String.format("Creating snapshot of server %s (%s) with name %s", server.getName(), server.getId(),
                snapshotName));
        metricsLogger.info(String.format("Creating snapshot of server %s (%s) with name %s", server.getName(), server.getId(),
                snapshotName));

        // Request Snapshot
        String msg;
        while (rc.attempt()) {
            try {
                server.createSnapshot(snapshotName);
                break;
            } catch (ContextConnectionException e) {
                msg = EELFResourceManager.format(Msg.CONNECTION_FAILED_RETRY, provider.getName(), service.getURL(),
                        context.getTenant().getName(), context.getTenant().getId(), e.getMessage(),
                        Long.toString(rc.getRetryDelay()), Integer.toString(rc.getAttempts()),
                        Integer.toString(rc.getRetryLimit()));
                logger.error(msg, e);
                metricsLogger.error(msg, e);
                rc.delay();
            }
        }
        if (rc.isFailed()) {
            msg = EELFResourceManager.format(Msg.CONNECTION_FAILED, provider.getName(), service.getURL());
            logger.error(msg);
            metricsLogger.error(msg);
            throw new RequestFailedException("Stop Server", msg, HttpStatus.BAD_GATEWAY_502, server);
        }
        rc.reset();

        // Locate snapshot image
        Image snapshot = null;
        while (rc.attempt()) {
            try {
                snapshot = service.getImageByName(snapshotName);
                if (snapshot != null) {
                    break;
                }
            } catch (ContextConnectionException e) {
                msg = EELFResourceManager.format(Msg.CONNECTION_FAILED_RETRY, provider.getName(), service.getURL(),
                        context.getTenant().getName(), context.getTenant().getId(), e.getMessage(),
                        Long.toString(rc.getRetryDelay()), Integer.toString(rc.getAttempts()),
                        Integer.toString(rc.getRetryLimit()));
                logger.error(msg, e);
                metricsLogger.error(msg, e);
                rc.delay();
            }
        }
        if (rc.isFailed()) {
            msg = EELFResourceManager.format(Msg.CONNECTION_FAILED, provider.getName(), service.getURL());
            logger.error(msg);
            metricsLogger.error(msg);
            throw new RequestFailedException("Stop Server", msg, HttpStatus.BAD_GATEWAY_502, server);
        }
        rc.reset();

        // Wait for it to be ready
        waitForStateChange(rc, snapshot, Image.Status.ACTIVE);

        return snapshot;
    }

    private Image createSnapshot(Map<String, String> params, SvcLogicContext ctx) throws APPCException {

        Image snapshot = null;
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();

        String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);
        String msg;
        
        
        /*
         * Set Time for Metrics Logger
         */
        long startTime = System.currentTimeMillis();
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        df.setTimeZone(tz);
        String startTimeStr = df.format(new Date());
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        String endTimeStr = String.valueOf(endTime);
        String durationStr = String.valueOf(duration);
        String endTimeStrUTC = df.format(new Date());
        MDC.put("EndTimestamp", endTimeStrUTC);
        MDC.put("ElapsedTime", durationStr);
        MDC.put("TargetEntity", "cdp");
        MDC.put("TargetServiceName", "create snapshot");
        MDC.put("ClassName", "org.openecomp.appc.adapter.iaas.provider.operation.impl.CreateSnapshot"); 
        
        try {
            validateParametersExist(params, ProviderAdapter.PROPERTY_INSTANCE_URL,
                    ProviderAdapter.PROPERTY_PROVIDER_NAME);
            String vm_url = params.get(ProviderAdapter.PROPERTY_INSTANCE_URL);

            VMURL vm = VMURL.parseURL(vm_url);
            if (validateVM(rc, appName, vm_url, vm)) return null;

            IdentityURL ident = IdentityURL.parseURL(params.get(ProviderAdapter.PROPERTY_IDENTITY_URL));
            String identStr = (ident == null) ? null : ident.toString();

            Context context = null;
            try {
                context = getContext(rc, vm_url, identStr);
                if (context != null) {
                    Server server = lookupServer(rc, context, vm.getServerId());
                    logger.debug(Msg.SERVER_FOUND, vm_url, context.getTenantName(), server.getStatus().toString());

                    if (hasImageAccess(rc, context)) {
                        snapshot = createSnapshot(rc, server);
                        doSuccess(rc);
                    } else {
                        msg = EELFResourceManager.format(Msg.REBUILD_SERVER_FAILED, server.getName(), server.getId(),
                                "Accessing Image Service Failed");
                        logger.error(msg);
                        metricsLogger.error(msg);
                        doFailure(rc, HttpStatus.FORBIDDEN_403, msg);
                    }
                    context.close();
                }
            } catch (ResourceNotFoundException e) {
                msg = EELFResourceManager.format(Msg.SERVER_NOT_FOUND, e, vm_url);
                logger.error(msg);
                metricsLogger.error(msg, e);
                doFailure(rc, HttpStatus.NOT_FOUND_404, msg);
            } catch (Throwable t) {
                msg = EELFResourceManager.format(Msg.SERVER_OPERATION_EXCEPTION, t, t.getClass().getSimpleName(),
                        Operation.SNAPSHOT_SERVICE.toString(), vm_url, context == null ? "Unknown" : context.getTenantName());
                logger.error(msg, t);
                doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, msg);
            }
        } catch (RequestFailedException e) {
            doFailure(rc, e.getStatus(), e.getMessage());
        }
        return snapshot;
    }

    @Override
    protected ModelObject executeProviderOperation(Map<String, String> params, SvcLogicContext context) throws APPCException {

        setMDC(Operation.SNAPSHOT_SERVICE.toString(), "App-C IaaS Adapter:Snapshot", ADAPTER_NAME);
        logOperation(Msg.SNAPSHOTING_SERVER, params, context);
        
        /*
         * Set Time for Metrics Logger
         */
        long startTime = System.currentTimeMillis();
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        df.setTimeZone(tz);
        String startTimeStr = df.format(new Date());
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        String endTimeStr = String.valueOf(endTime);
        String durationStr = String.valueOf(duration);
        String endTimeStrUTC = df.format(new Date());
        MDC.put("EndTimestamp", endTimeStrUTC);
        MDC.put("ElapsedTime", durationStr);
        MDC.put("TargetEntity", "cdp");
        MDC.put("TargetServiceName", "create snapshot");
        MDC.put("ClassName", "org.openecomp.appc.adapter.iaas.provider.operation.impl.CreateSnapshot"); 
        
        metricsLogger.info("Executing Provider Operation: Create Snapshot");
        
        return createSnapshot(params, context);
    }
}
