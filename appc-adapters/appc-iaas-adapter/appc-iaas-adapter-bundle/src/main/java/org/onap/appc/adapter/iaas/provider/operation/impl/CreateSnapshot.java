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

package org.onap.appc.adapter.iaas.provider.operation.impl;

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
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.onap.appc.Constants;
import org.onap.appc.adapter.iaas.ProviderAdapter;
import org.onap.appc.adapter.iaas.impl.IdentityURL;
import org.onap.appc.adapter.iaas.impl.RequestContext;
import org.onap.appc.adapter.iaas.impl.RequestFailedException;
import org.onap.appc.adapter.iaas.impl.VMURL;
import org.onap.appc.adapter.iaas.provider.operation.common.enums.Operation;
import org.onap.appc.adapter.iaas.provider.operation.impl.base.ProviderServerOperation;
import org.onap.appc.configuration.Configuration;
import org.onap.appc.configuration.ConfigurationFactory;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.i18n.Msg;
import org.onap.appc.logging.LoggingConstants;
import org.slf4j.MDC;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import static org.onap.appc.adapter.iaas.provider.operation.common.constants.Constants.DATE_FORMAT;
import static org.onap.appc.adapter.utils.Constants.ADAPTER_NAME;

public class CreateSnapshot extends ProviderServerOperation {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(CreateSnapshot.class);
    private static EELFLogger metricsLogger = EELFManager.getInstance().getMetricsLogger();
    private static final Configuration configuration = ConfigurationFactory.getConfiguration();

    private String generateSnapshotName(String server) {
        setTimeForMetricsLogger();
        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
        metricsLogger.info("Snapshot Name Generated: Snapshot of %s at %s", server, df.format(new Date()));
        return String.format("Snapshot of %s at %s", server, df.format(new Date()));
    }

    private Image createSnapshot(RequestContext rc, Server server) throws ZoneException, RequestFailedException {
        Context context = server.getContext();
        Provider provider = context.getProvider();
        ImageService service = context.getImageService(); // Already checked access by this point
        String snapshotName = generateSnapshotName(server.getName());
        setTimeForMetricsLogger();
        logger.info(String.format("Creating snapshot of server %s (%s) with name %s", server.getName(), server.getId(),
                snapshotName));
        metricsLogger.info(String.format("Creating snapshot of server %s (%s) with name %s", server.getName(),
                server.getId(), snapshotName));
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
        setTimeForMetricsLogger();
        try {
            validateParametersExist(params, ProviderAdapter.PROPERTY_INSTANCE_URL,
                    ProviderAdapter.PROPERTY_PROVIDER_NAME);
            String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);
            String vm_url = params.get(ProviderAdapter.PROPERTY_INSTANCE_URL);
            VMURL vm = VMURL.parseURL(vm_url);
            if (validateVM(rc, appName, vm_url, vm))
                return null;
            IdentityURL ident = IdentityURL.parseURL(params.get(ProviderAdapter.PROPERTY_IDENTITY_URL));
            String identStr = (ident == null) ? null : ident.toString();
            snapshot = createSnapshotNested(snapshot, rc, vm, vm_url, identStr);
        } catch (RequestFailedException e) {
            doFailure(rc, e.getStatus(), e.getMessage());
        }
        return snapshot;
    }

    private Image createSnapshotNested(Image SnapShot, RequestContext RcContext, VMURL vm, String vmUrl,
            String identStr) throws APPCException {
        String msg;
        Context context = null;
        String tenantName = "Unknown";// this variable is also used in catch
        try {
            context = getContext(RcContext, vmUrl, identStr);
            if (context != null) {
                tenantName = context.getTenantName();
                Server server = lookupServer(RcContext, context, vm.getServerId());
                logger.debug(Msg.SERVER_FOUND, vmUrl, tenantName, server.getStatus().toString());
                if (hasImageAccess(RcContext, context)) {
                    SnapShot = createSnapshot(RcContext, server);
                    doSuccess(RcContext);
                } else {
                    msg = EELFResourceManager.format(Msg.IMAGE_SERVICE_FAILED, server.getName(), server.getId(),
                            "Accessing Image Service Failed");
                    logger.error(msg);
                    metricsLogger.error(msg);
                    doFailure(RcContext, HttpStatus.FORBIDDEN_403, msg);
                }
                context.close();
            }
        } catch (ResourceNotFoundException e) {
            msg = EELFResourceManager.format(Msg.SERVER_NOT_FOUND, e, vmUrl);
            logger.error(msg);
            metricsLogger.error(msg, e);
            doFailure(RcContext, HttpStatus.NOT_FOUND_404, msg);
        } catch (Exception e1) {
            msg = EELFResourceManager.format(Msg.SERVER_OPERATION_EXCEPTION, e1, e1.getClass().getSimpleName(),
                    Operation.SNAPSHOT_SERVICE.toString(), vmUrl, tenantName);
            logger.error(msg, e1);
            doFailure(RcContext, HttpStatus.INTERNAL_SERVER_ERROR_500, msg);
        }
        return SnapShot;
    }

    @Override
    protected ModelObject executeProviderOperation(Map<String, String> params, SvcLogicContext context)
            throws APPCException {
        setMDC(Operation.SNAPSHOT_SERVICE.toString(), "App-C IaaS Adapter:Snapshot", ADAPTER_NAME);
        logOperation(Msg.SNAPSHOTING_SERVER, params, context);
        setTimeForMetricsLogger();
        metricsLogger.info("Executing Provider Operation: Create Snapshot");
        return createSnapshot(params, context);
    }

    private void setTimeForMetricsLogger() {
        MDC.put(LoggingConstants.MDCKeys.TARGET_ENTITY, "cdp");
        MDC.put(LoggingConstants.MDCKeys.TARGET_SERVICE_NAME, "create snapshot");
        MDC.put(LoggingConstants.MDCKeys.CLASS_NAME,
                "org.onap.appc.adapter.iaas.provider.operation.impl.CreateSnapshot");
    }
}
