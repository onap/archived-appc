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
import com.att.cdp.zones.ComputeService;
import com.att.cdp.zones.Context;
import com.att.cdp.zones.model.ModelObject;
import com.att.cdp.zones.model.Server;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import org.openecomp.sdnc.sli.SvcLogicContext;
import org.glassfish.grizzly.http.util.HttpStatus;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static org.openecomp.appc.adapter.iaas.provider.operation.common.constants.Constants.MDC_SERVICE;
import static org.openecomp.appc.adapter.iaas.provider.operation.common.enums.Operation.MIGRATE_SERVICE;
import static org.openecomp.appc.adapter.utils.Constants.ADAPTER_NAME;

import org.slf4j.MDC;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


/**
 * @since September 26, 2016
 */
public class MigrateServer extends ProviderServerOperation {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(EvacuateServer.class);
    private static EELFLogger metricsLogger = EELFManager.getInstance().getMetricsLogger();
    private static final Configuration configuration = ConfigurationFactory.getConfiguration();

    /**
     * A list of valid initial VM statuses for a migrate operations
     */
    private final Collection<Server.Status> migratableStatuses = Arrays.asList(Server.Status.READY, Server.Status.RUNNING, Server.Status.SUSPENDED);


    private String getConnectionExceptionMessage(RequestContext rc, Context ctx, ContextConnectionException e)
            throws ZoneException {
        return EELFResourceManager.format(Msg.CONNECTION_FAILED_RETRY, ctx.getProvider().getName(),
                ctx.getComputeService().getURL(), ctx.getTenant().getName(), ctx.getTenant().getId(), e.getMessage(),
                Long.toString(rc.getRetryDelay()), Integer.toString(rc.getAttempts()),
                Integer.toString(rc.getRetryLimit()));
    }

	private void migrateServer(RequestContext rc, Server server, SvcLogicContext svcCtx)
			throws ZoneException, RequestFailedException {
		String msg;
		Context ctx = server.getContext();
		ComputeService service = ctx.getComputeService();

		// Init status will equal final status
		Server.Status initialStatus = server.getStatus();

		if (initialStatus == null) {
			throw new ZoneException("Failed to determine server's starting status");
		}

		// We can only migrate certain statuses
		if (!migratableStatuses.contains(initialStatus)) {
			throw new ZoneException(String.format("Cannot migrate server that is in %s state. Must be in one of [%s]",
					initialStatus, migratableStatuses));
		}

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
		MDC.put("TargetServiceName", "migrate server");
		MDC.put("ClassName", "org.openecomp.appc.adapter.iaas.provider.operation.impl.MigrateServer");
		// Is the skip Hypervisor check attribute populated?
		String skipHypervisorCheck = null;
		if (svcCtx != null) {
			skipHypervisorCheck = svcCtx.getAttribute(ProviderAdapter.SKIP_HYPERVISOR_CHECK);

		}

		// // Always perform Hypervisor check
		// unless the skip is set to true
		
		if (skipHypervisorCheck == null || (!skipHypervisorCheck.equalsIgnoreCase("true"))) {

			// Check of the Hypervisor for the VM Server is UP and reachable
			
			checkHypervisor(server);
			
		}
		
			boolean inConfirmPhase = false;
			try {
				while (rc.attempt()) {
					try {
						if (!inConfirmPhase) {
							// Initial migrate request
							service.migrateServer(server.getId());
							// Wait for change to verify resize
							waitForStateChange(rc, server, Server.Status.READY);
							inConfirmPhase = true;
						}

						// Verify resize
						service.processResize(server);
						// Wait for complete. will go back to init status
						waitForStateChange(rc, server, initialStatus);
						logger.info("Completed migrate request successfully");
						metricsLogger.info("Completed migrate request successfully");
						return;
					} catch (ContextConnectionException e) {
						msg = getConnectionExceptionMessage(rc, ctx, e);
						logger.error(msg, e);
						metricsLogger.error(msg, e);
						rc.delay();
					}
				}
			} catch (ZoneException e) {
				String phase = inConfirmPhase ? "VERIFY MIGRATE" : "REQUEST MIGRATE";
				msg = EELFResourceManager.format(Msg.MIGRATE_SERVER_FAILED, server.getName(), server.getId(), phase,
						e.getMessage());
				generateEvent(rc, false, msg);
				logger.error(msg, e);
				metricsLogger.error(msg, e);
				throw new RequestFailedException("Migrate Server", msg, HttpStatus.METHOD_NOT_ALLOWED_405, server);
			}
		
	}

    /**
     * @see org.openecomp.appc.adapter.iaas.ProviderAdapter#migrateServer(java.util.Map, org.openecomp.sdnc.sli.SvcLogicContext)
     */
    private Server migrateServer(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        Server server = null;
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
        MDC.put("TargetServiceName", "migrate server");
        MDC.put("ClassName", "org.openecomp.appc.adapter.iaas.provider.operation.impl.MigrateServer"); 

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
                    server = lookupServer(rc, context, vm.getServerId());
                    logger.debug(Msg.SERVER_FOUND, vm_url, context.getTenantName(), server.getStatus().toString());
                    migrateServer(rc, server, ctx);
                    server.refreshStatus();
                    context.close();
                    doSuccess(rc);
                }
            } catch (RequestFailedException e) {
                doFailure(rc, e.getStatus(), e.getMessage());
            }
            catch (ResourceNotFoundException e) {
                msg = EELFResourceManager.format(Msg.SERVER_NOT_FOUND, e, vm_url);
                logger.error(msg);
                metricsLogger.error(msg);
                doFailure(rc, HttpStatus.NOT_FOUND_404, msg);
            } catch (Throwable t) {
                msg = EELFResourceManager.format(Msg.SERVER_OPERATION_EXCEPTION, t, t.getClass().getSimpleName(),
                        MIGRATE_SERVICE.toString(), vm_url, context == null ? "Unknown" : context.getTenantName());
                logger.error(msg, t);
                metricsLogger.error(msg);
                doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, msg);
            }
        } catch (RequestFailedException e) {
            doFailure(rc, e.getStatus(), e.getMessage());
        }

        return server;
    }

    @Override
    protected ModelObject executeProviderOperation(Map<String, String> params, SvcLogicContext context) throws APPCException {

        setMDC(Operation.MIGRATE_SERVICE.toString(), "App-C IaaS Adapter:Migrate", ADAPTER_NAME);
        logOperation(Msg.MIGRATING_SERVER, params, context);
        
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
        MDC.put("TargetServiceName", "migrate server");
        MDC.put("ClassName", "org.openecomp.appc.adapter.iaas.provider.operation.impl.MigrateServer"); 
        
        
        metricsLogger.info("Executing Provider Operation: Migrate");
        
        return migrateServer(params,context);
    }
}
