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
import org.openecomp.appc.adapter.iaas.provider.operation.common.enums.Outcome;
import org.openecomp.appc.adapter.iaas.provider.operation.impl.base.ProviderServerOperation;
import org.openecomp.appc.exceptions.UnknownProviderException;
import org.openecomp.appc.i18n.Msg;
import com.att.cdp.exceptions.ResourceNotFoundException;
import com.att.cdp.exceptions.ZoneException;
import com.att.cdp.zones.Context;
import com.att.cdp.zones.NetworkService;
import com.att.cdp.zones.model.ModelObject;
import com.att.cdp.zones.model.Network;
import com.att.cdp.zones.model.Port;
import com.att.cdp.zones.model.Server;
import com.att.cdp.zones.model.Subnet;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import org.openecomp.sdnc.sli.SvcLogicContext;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.slf4j.MDC;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static org.openecomp.appc.adapter.iaas.provider.operation.common.constants.Constants.MDC_SERVICE;
import static org.openecomp.appc.adapter.iaas.provider.operation.common.enums.Operation.RESTART_SERVICE;
import static org.openecomp.appc.adapter.utils.Constants.ADAPTER_NAME;


public class RestartServer extends ProviderServerOperation {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(RestartServer.class);
    private static EELFLogger metricsLogger = EELFManager.getInstance().getMetricsLogger();


    /**
     * This method handles the case of restarting a server once we have found the server and have obtained the abstract
     * representation of the server via the context (i.e., the "Server" object from the CDP-Zones abstraction).
     *
     * @param rc
     *            The request context that manages the state and recovery of the request for the life of its processing.
     * @param server
     *            The server object representing the server we want to operate on
     * @throws ZoneException
     */
	@SuppressWarnings("nls")
	private void restartServer(RequestContext rc, Server server, SvcLogicContext ctx)
			throws ZoneException, RequestFailedException {

		/*
		 * Pending is a bit of a special case. If we find the server is in a
		 * pending state, then the provider is in the process of changing state
		 * of the server. So, lets try to wait a little bit and see if the state
		 * settles down to one we can deal with. If not, then we have to fail
		 * the request.
		 */
		String msg;
		if (server.getStatus().equals(Server.Status.PENDING)) {
			waitForStateChange(rc, server, Server.Status.READY, Server.Status.RUNNING, Server.Status.ERROR,
					Server.Status.SUSPENDED, Server.Status.PAUSED);
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
		MDC.put("TargetServiceName", "restart server");
		MDC.put("ClassName", "org.openecomp.appc.adapter.iaas.provider.operation.impl.RestartServer");

		String skipHypervisorCheck = null;
		if (ctx != null) {
			skipHypervisorCheck = ctx.getAttribute(ProviderAdapter.SKIP_HYPERVISOR_CHECK);

		}

		// Always perform Virtual Machine/Hypervisor Status/Network checks
		// unless the skip is set to true
		if (skipHypervisorCheck == null || (!skipHypervisorCheck.equalsIgnoreCase("true"))) {

			// Check of the Hypervisor for the VM Server is UP and reachable
			
			checkHypervisor(server);
		
		}

		/*
		 * We determine what to do based on the current state of the server
		 */
		
			switch (server.getStatus()) {
			case DELETED:
				// Nothing to do, the server is gone
				msg = EELFResourceManager.format(Msg.SERVER_DELETED, server.getName(), server.getId(),
						server.getTenantId(), "restarted");
				generateEvent(rc, false, msg);
				logger.error(msg);
				metricsLogger.error(msg);
				break;

			case RUNNING:
				// Attempt to stop and start the server
				stopServer(rc, server);
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
				// Attempt to start the server
				startServer(rc, server);
				generateEvent(rc, true, Outcome.SUCCESS.toString());
				metricsLogger.info("Server status: READY");
				break;

			case PAUSED:
				// if paused, un-pause it
				unpauseServer(rc, server);
				generateEvent(rc, true, Outcome.SUCCESS.toString());
				metricsLogger.info("Server status: PAUSED");
				break;

			case SUSPENDED:
				// Attempt to resume the suspended server
				resumeServer(rc, server);
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
				break;
			}
		

	}

    /**
     * This method is used to restart an existing virtual machine given the fully qualified URL of the machine.
     * <p>
     * The fully qualified URL contains enough information to locate the appropriate server. The URL is of the form
     * <pre>
     *  [scheme]://[host[:port]] / [path] / [tenant_id] / servers / [vm_id]
     * </pre> Where the various parts of the URL can be parsed and extracted and used to locate the appropriate service
     * in the provider service catalog. This then allows us to open a context using the CDP abstraction, obtain the
     * server by its UUID, and then perform the restart.
     * </p>
     *
     * @throws UnknownProviderException
     *             If the provider cannot be found
     * @throws IllegalArgumentException
     *             if the expected argument(s) are not defined or are invalid
     * @see org.openecomp.appc.adapter.iaas.ProviderAdapter#restartServer(java.util.Map, org.openecomp.sdnc.sli.SvcLogicContext)
     */
    @SuppressWarnings("nls")
    private Server restartServer(Map<String, String> params, SvcLogicContext ctx)
            throws UnknownProviderException, IllegalArgumentException {
        Server server = null;
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();

        String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);


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
        MDC.put("TargetServiceName", "GET server status");
        MDC.put("ClassName", "org.openecomp.appc.adapter.iaas.provider.operation.impl.RestartServer");

        ctx.setAttribute("RESTART_STATUS", "ERROR");
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
                    rc.reset();
                    server = lookupServer(rc, context, vm.getServerId());
                    logger.debug(Msg.SERVER_FOUND, vm_url, context.getTenantName(), server.getStatus().toString());
                    rc.reset();
                    restartServer(rc, server, ctx);
                    context.close();
                    doSuccess(rc);
                    ctx.setAttribute("RESTART_STATUS", "SUCCESS");
                    String msg = EELFResourceManager.format(Msg.SUCCESS_EVENT_MESSAGE, "RestartServer", vm_url);
                    ctx.setAttribute(org.openecomp.appc.Constants.ATTRIBUTE_SUCCESS_MESSAGE, msg);
                }
            } catch (RequestFailedException e) {
            	doFailure(rc, e.getStatus(), e.getMessage());
            }	 
            catch (ResourceNotFoundException e) {
                String msg = EELFResourceManager.format(Msg.SERVER_NOT_FOUND, e, vm_url);
                logger.error(msg);
                metricsLogger.error(msg);
                doFailure(rc, HttpStatus.NOT_FOUND_404, msg);
            } catch (Throwable t) {
                String msg = EELFResourceManager.format(Msg.SERVER_OPERATION_EXCEPTION, t, t.getClass().getSimpleName(),
                        RESTART_SERVICE.toString(), vm_url, context == null ? "Unknown" : context.getTenantName());
                logger.error(msg, t);
                metricsLogger.error(msg, t);
                doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, msg);
            }
        } catch (RequestFailedException e) {
            doFailure(rc, e.getStatus(), e.getMessage());
        }

        return server;
    }

    @Override
    protected ModelObject executeProviderOperation(Map<String, String> params, SvcLogicContext context) throws UnknownProviderException {

        setMDC(RESTART_SERVICE.toString(), "App-C IaaS Adapter:Restart", ADAPTER_NAME);
        logOperation(Msg.RESTARTING_SERVER, params, context);
        
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
        MDC.put("TargetServiceName", "execute restart");
        MDC.put("ClassName", "org.openecomp.appc.adapter.iaas.provider.operation.impl.RestartServer"); 
        
        metricsLogger.info("Executing Provider Operation: Restart");
        
        
        return restartServer(params, context);
    }
}
