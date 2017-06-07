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
import org.openecomp.appc.adapter.iaas.provider.operation.common.enums.Outcome;
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
import com.att.cdp.zones.ImageService;
import com.att.cdp.zones.Provider;
import com.att.cdp.zones.model.Image;
import com.att.cdp.zones.model.ModelObject;
import com.att.cdp.zones.model.Server;
import com.att.cdp.zones.model.ServerBootSource;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import org.openecomp.sdnc.sli.SvcLogicContext;
import org.glassfish.grizzly.http.util.HttpStatus;

import java.util.List;
import java.util.Map;

import static org.openecomp.appc.adapter.iaas.provider.operation.common.constants.Constants.MDC_SERVICE;
import static org.openecomp.appc.adapter.iaas.provider.operation.common.enums.Operation.STOP_SERVICE;
import static org.openecomp.appc.adapter.utils.Constants.ADAPTER_NAME;

import org.slf4j.MDC;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @since September 26, 2016
 */
public class RebuildServer extends ProviderServerOperation {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(RebuildServer.class);
    private static EELFLogger metricsLogger = EELFManager.getInstance().getMetricsLogger();
    private static final Configuration configuration = ConfigurationFactory.getConfiguration();

    /**
     * Rebuild the indicated server with the indicated image. This method assumes the server has been determined to be
     * in the correct state to do the rebuild.
     *
     * @param rc
     *            The request context that manages the state and recovery of the request for the life of its processing.
     * @param server
     *            the server to be rebuilt
     * @param image
     *            The image to be used (or snapshot)
     * @throws RequestFailedException
     *             if the server does not change state in the allotted time
     */
    @SuppressWarnings("nls")
    private void rebuildServer(RequestContext rc, Server server, String image) throws RequestFailedException {
        logger.debug(Msg.REBUILD_SERVER, server.getId());
        
        String msg;
        Context context = server.getContext();
        Provider provider = context.getProvider();
        ComputeService service = context.getComputeService();  
        
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
        MDC.put("TargetServiceName", "rebuild server");
        MDC.put("ClassName", "org.openecomp.appc.adapter.iaas.provider.operation.impl.RebuildServer"); 

        try {
            while (rc.attempt()) {
                try {
                    server.rebuild(image);
                    break;
                } catch (ContextConnectionException e) {
                    msg = EELFResourceManager.format(Msg.CONNECTION_FAILED_RETRY, provider.getName(), service.getURL(),
                            context.getTenant().getName(), context.getTenant().getId(), e.getMessage(),
                            Long.toString(rc.getRetryDelay()), Integer.toString(rc.getAttempts()),
                            Integer.toString(rc.getRetryLimit()));
                    logger.error(msg, e);
                    metricsLogger.error(msg,e);
                    rc.delay();
                }
            }

            /*
             * We need to provide some time for OpenStack to start processing the request.
             */
            try {
                Thread.sleep(10L * 1000L);
            } catch (InterruptedException e) {
                logger.trace("Sleep threw interrupted exception, should never occur");
                metricsLogger.trace("Sleep threw interrupted exception, should never occur");
            }
        } catch (ZoneException e) {
            msg =
                    EELFResourceManager.format(Msg.REBUILD_SERVER_FAILED, server.getName(), server.getId(), e.getMessage());
            logger.error(msg);
            metricsLogger.error(msg);
            throw new RequestFailedException("Rebuild Server", msg, HttpStatus.BAD_GATEWAY_502, server);
        }

        rc.reset();
        /*
         * Once we have started the process, now we wait for the final state of stopped. This should be the final state
         * (since we started the rebuild with the server stopped).
         */
        waitForStateChange(rc, server, Server.Status.READY);

        if (rc.isFailed()) {
            msg = EELFResourceManager.format(Msg.CONNECTION_FAILED, provider.getName(), service.getURL());
            logger.error(msg);
            metricsLogger.error(msg);
            throw new RequestFailedException("Rebuild Server", msg, HttpStatus.BAD_GATEWAY_502, server);
        }
        rc.reset();
    }

    /**
     * This method is called to rebuild the provided server.
     * <p>
     * If the server was booted from a volume, then the request is failed immediately and no action is taken. Rebuilding
     * a VM from a bootable volume, where the bootable volume itself is not rebuilt, serves no purpose.
     * </p>
     *
     * @param rc
     *            The request context that manages the state and recovery of the request for the life of its processing.
     * @param server
     * @throws ZoneException
     * @throws RequestFailedException
     */
    @SuppressWarnings("nls")
	private void rebuildServer(RequestContext rc, Server server, SvcLogicContext ctx)
			throws ZoneException, RequestFailedException {

		ServerBootSource builtFrom = server.getBootSource();
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
		MDC.put("TargetServiceName", "rebuild server");
		MDC.put("ClassName", "org.openecomp.appc.adapter.iaas.provider.operation.impl.RebuildServer");

		// Throw exception for non image/snap boot source
		if (ServerBootSource.VOLUME.equals(builtFrom)) {
			msg = String.format("Rebuilding is currently not supported for servers built from bootable volumes [%s]",
					server.getId());
			generateEvent(rc, false, msg);
			logger.error(msg);
			metricsLogger.error(msg);
			throw new RequestFailedException("Rebuild Server", msg, HttpStatus.FORBIDDEN_403, server);
		}

		/*
		 * Pending is a bit of a special case. If we find the server is in a
		 * pending state, then the provider is in the process of changing state
		 * of the server. So, lets try to wait a little bit and see if the state
		 * settles down to one we can deal with. If not, then we have to fail
		 * the request.
		 */
		Context context = server.getContext();
		Provider provider = context.getProvider();
		ComputeService service = context.getComputeService();
		if (server.getStatus().equals(Server.Status.PENDING)) {
            rc.reset();
            waitForStateChange(rc, server, Server.Status.READY, Server.Status.RUNNING, Server.Status.ERROR,
					Server.Status.SUSPENDED, Server.Status.PAUSED);
		}

		// Is the skip Hypervisor check attribute populated?
		String skipHypervisorCheck = null;
		if (ctx != null) {
			skipHypervisorCheck = ctx.getAttribute(ProviderAdapter.SKIP_HYPERVISOR_CHECK);

		}

		// Always perform Hypervisor Status checks
		// unless the skip is set to true
		if (skipHypervisorCheck == null || (!skipHypervisorCheck.equalsIgnoreCase("true"))) {

			// Check of the Hypervisor for the VM Server is UP and reachable
			checkHypervisor(server);
		}

		/*
		 * Get the image to use. This is determined by the presence or
			 * absence of snapshot images. If any snapshots exist, then the
			 * latest snapshot is used, otherwise the image used to construct
			 * the VM is used.
			 */
			List<Image> snapshots = server.getSnapshots();
			String imageToUse;
			if (snapshots != null && !snapshots.isEmpty()) {
				imageToUse = snapshots.get(0).getId();
			} else {
				imageToUse = server.getImage();
				ImageService imageService = server.getContext().getImageService();
                rc.reset();
				try {
					while (rc.attempt()) {
						try {
							/*
							 * We are just trying to make sure that the image
							 * exists. We arent interested in the details at
							 * this point.
							 */
							imageService.getImage(imageToUse);
							break;
						} catch (ContextConnectionException e) {
							msg = EELFResourceManager.format(Msg.CONNECTION_FAILED_RETRY, provider.getName(),
									imageService.getURL(), context.getTenant().getName(), context.getTenant().getId(),
									e.getMessage(), Long.toString(rc.getRetryDelay()),
									Integer.toString(rc.getAttempts()), Integer.toString(rc.getRetryLimit()));
							logger.error(msg, e);
							metricsLogger.error(msg);
							rc.delay();
						}
					}
				} catch (ZoneException e) {
					msg = EELFResourceManager.format(Msg.IMAGE_NOT_FOUND, imageToUse, "rebuild");
					generateEvent(rc, false, msg);
					logger.error(msg);
					metricsLogger.error(msg);
					throw new RequestFailedException("Rebuild Server", msg, HttpStatus.METHOD_NOT_ALLOWED_405, server);
				}
			}
			if (rc.isFailed()) {
				msg = EELFResourceManager.format(Msg.CONNECTION_FAILED, provider.getName(), service.getURL());
				logger.error(msg);
				metricsLogger.error(msg);
				throw new RequestFailedException("Rebuild Server", msg, HttpStatus.BAD_GATEWAY_502, server);
			}
			rc.reset();

        /*
         * We determine what to do based on the current state of the server
         */
        switch (server.getStatus()) {
            case DELETED:
                // Nothing to do, the server is gone
                msg = EELFResourceManager.format(Msg.SERVER_DELETED, server.getName(), server.getId(),
                        server.getTenantId(), "rebuilt");
                generateEvent(rc, false, msg);
                logger.error(msg);
                metricsLogger.error(msg);
                throw new RequestFailedException("Rebuild Server", msg, HttpStatus.METHOD_NOT_ALLOWED_405, server);

            case RUNNING:
                // Attempt to stop the server, then rebuild it
                stopServer(rc, server);
                rc.reset();
                rebuildServer(rc, server, imageToUse);
                rc.reset();
                startServer(rc, server);
                generateEvent(rc, true, Outcome.SUCCESS.toString());
                metricsLogger.info("Server status: RUNNING");
                break;

            case ERROR:
                msg = EELFResourceManager.format(Msg.SERVER_ERROR_STATE, server.getName(), server.getId(),
                        server.getTenantId(), "rebuild");
                generateEvent(rc, false, msg);
                logger.error(msg);
                metricsLogger.error(msg);
                throw new RequestFailedException("Rebuild Server", msg, HttpStatus.METHOD_NOT_ALLOWED_405, server);

            case READY:
                // Attempt to rebuild the server
                rebuildServer(rc, server, imageToUse);
                rc.reset();
                startServer(rc, server);
                generateEvent(rc, true, Outcome.SUCCESS.toString());
                metricsLogger.info("Server status: READY");
                break;

            case PAUSED:
                // if paused, un-pause it, stop it, and rebuild it
                unpauseServer(rc, server);
                rc.reset();
                stopServer(rc, server);
                rc.reset();
                rebuildServer(rc, server, imageToUse);
                rc.reset();
                startServer(rc, server);
                generateEvent(rc, true, Outcome.SUCCESS.toString());
                metricsLogger.info("Server status: PAUSED");
                break;

            case SUSPENDED:
                // Attempt to resume the suspended server, stop it, and rebuild it
                resumeServer(rc, server);
                rc.reset();
                stopServer(rc, server);
                rc.reset();
                rebuildServer(rc, server, imageToUse);
                rc.reset();
                startServer(rc, server);
                generateEvent(rc, true, Outcome.SUCCESS.toString());
                metricsLogger.info("Server status: SUSPENDED");
                break;

			default:
				// Hmmm, unknown status, should never occur
				msg = EELFResourceManager.format(Msg.UNKNOWN_SERVER_STATE, server.getName(), server.getId(),
						server.getTenantId(), server.getStatus().name());
				generateEvent(rc, false, msg);
				logger.error(msg);
				metricsLogger.error(msg);
				throw new RequestFailedException("Rebuild Server", msg, HttpStatus.METHOD_NOT_ALLOWED_405, server);
			}


	}

    /**
     * @see org.openecomp.appc.adapter.iaas.ProviderAdapter#rebuildServer(java.util.Map, org.openecomp.sdnc.sli.SvcLogicContext)
     */
    @SuppressWarnings("nls")
    public Server rebuildServer(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
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
        MDC.put("TargetServiceName", "rebuild server");
        MDC.put("ClassName", "org.openecomp.appc.adapter.iaas.provider.operation.impl.RebuildServer");

        try {
            validateParametersExist(params, ProviderAdapter.PROPERTY_INSTANCE_URL,
                    ProviderAdapter.PROPERTY_PROVIDER_NAME);
            String vm_url = params.get(ProviderAdapter.PROPERTY_INSTANCE_URL);

            VMURL vm = VMURL.parseURL(vm_url);
            if (validateVM(rc, appName, vm_url, vm)) return null;

            IdentityURL ident = IdentityURL.parseURL(params.get(ProviderAdapter.PROPERTY_IDENTITY_URL));
            String identStr = (ident == null) ? null : ident.toString();
            ctx.setAttribute("REBUILD_STATUS", "ERROR");

            Context context = null;
            try {
                context = getContext(rc, vm_url, identStr);
                if (context != null) {
                    rc.reset();
                    server = lookupServer(rc, context, vm.getServerId());
                    logger.debug(Msg.SERVER_FOUND, vm_url, context.getTenantName(), server.getStatus().toString());

                    // Manually checking image service until new PAL release
                    if (hasImageAccess(rc, context)) {
                        rebuildServer(rc, server, ctx);
                        doSuccess(rc);
                        ctx.setAttribute("REBUILD_STATUS", "SUCCESS");
                    } else {
                        msg = EELFResourceManager.format(Msg.REBUILD_SERVER_FAILED, server.getName(), server.getId(),
                                "Accessing Image Service Failed");
                        logger.error(msg);
                        metricsLogger.error(msg);
                        doFailure(rc, HttpStatus.FORBIDDEN_403, msg);
                    }
                    context.close();
                }
                else
                {
                    ctx.setAttribute("REBUILD_STATUS", "CONTEXT_NOT_FOUND");
                }
            }
            catch (RequestFailedException e) {
                doFailure(rc, e.getStatus(), e.getMessage());
                ctx.setAttribute("REBUILD_STATUS", "ERROR");
            }
            catch (ResourceNotFoundException e) {
                msg = EELFResourceManager.format(Msg.SERVER_NOT_FOUND, e, vm_url);
                ctx.setAttribute("REBUILD_STATUS", "ERROR");
                logger.error(msg);
                metricsLogger.error(msg);
                doFailure(rc, HttpStatus.NOT_FOUND_404, msg);
            } catch (Throwable t) {
                msg = EELFResourceManager.format(Msg.SERVER_OPERATION_EXCEPTION, t, t.getClass().getSimpleName(),
                        STOP_SERVICE.toString(), vm_url, context == null ? "Unknown" : context.getTenantName());
                ctx.setAttribute("REBUILD_STATUS", "ERROR");
                logger.error(msg, t);
                metricsLogger.error(msg);
                doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, msg);
            }
        } catch (RequestFailedException e) {
            doFailure(rc, e.getStatus(), e.getMessage());
            ctx.setAttribute("REBUILD_STATUS", "ERROR");
        }

        return server;
    }

    @Override
    protected ModelObject executeProviderOperation(Map<String, String> params, SvcLogicContext context) throws APPCException {

        setMDC(Operation.REBUILD_SERVICE.toString(), "App-C IaaS Adapter:Rebuild", ADAPTER_NAME);
        logOperation(Msg.REBUILDING_SERVER, params, context);
        
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
        MDC.put("TargetServiceName", "rebuild server");
        MDC.put("ClassName", "org.openecomp.appc.adapter.iaas.provider.operation.impl.RebuildServer");

        
        metricsLogger.info("Executing Provider Operation: Rebuild");
        
        
        return rebuildServer(params, context);
    }
}
