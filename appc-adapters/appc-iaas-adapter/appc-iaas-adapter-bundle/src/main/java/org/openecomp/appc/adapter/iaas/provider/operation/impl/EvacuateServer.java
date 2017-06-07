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
import org.openecomp.appc.adapter.iaas.impl.ProviderAdapterImpl;
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
import com.att.cdp.zones.Provider;
import com.att.cdp.zones.model.Hypervisor;
import com.att.cdp.zones.model.Hypervisor.Status;
import com.att.cdp.zones.model.Hypervisor.State;
import com.att.cdp.zones.model.Image;
import com.att.cdp.zones.model.ModelObject;
import com.att.cdp.zones.model.Server;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import org.openecomp.sdnc.sli.SvcLogicContext;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.slf4j.MDC;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static org.openecomp.appc.adapter.utils.Constants.ADAPTER_NAME;

public class EvacuateServer extends ProviderServerOperation {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(EvacuateServer.class);
    private static EELFLogger metricsLogger = EELFManager.getInstance().getMetricsLogger();
    private static final Configuration configuration = ConfigurationFactory.getConfiguration();
    private ProviderAdapterImpl paImpl = null;


    private void evacuateServer(RequestContext rc, @SuppressWarnings("unused") Server server, String target_host) throws ZoneException, RequestFailedException {

        String msg;
        Context ctx = server.getContext();
        Provider provider = ctx.getProvider();
        ComputeService service = ctx.getComputeService();
        
        /*
         * Pending is a bit of a special case. If we find the server is in a pending state, then the provider is in the
         * process of changing state of the server. So, lets try to wait a little bit and see if the state settles down
         * to one we can deal with. If not, then we have to fail the request.
         */
        try {
        	if (server.getStatus().equals(Server.Status.PENDING)) {
        		waitForStateChange(rc, server, Server.Status.READY, Server.Status.RUNNING, Server.Status.ERROR, Server.Status.SUSPENDED, Server.Status.PAUSED);
        	}
        } catch (RequestFailedException e) {
        	// evacuate is a special case. If the server is still in a Pending state, we want to continue with evacuate
        	logger.info("Evacuate server - ignore RequestFailedException from waitForStateChange() ...");
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
        MDC.put("TargetServiceName", "evacuate server");
        MDC.put("ClassName", "org.openecomp.appc.adapter.iaas.provider.operation.impl.EvacuteServer"); 
        

        try {
            while (rc.attempt()) {
                try {
                	logger.debug("Calling CDP moveServer - server id = " + server.getId());
                    service.moveServer(server.getId(), target_host);
                    // Wait for completion, expecting the server to go to a non pending state
                    waitForStateChange(rc, server, Server.Status.READY, Server.Status.RUNNING, Server.Status.ERROR, Server.Status.SUSPENDED, Server.Status.PAUSED);
                    break;
                } catch (ContextConnectionException e) {
                    msg = EELFResourceManager.format(Msg.CONNECTION_FAILED_RETRY, provider.getName(), service.getURL(),
                            ctx.getTenant().getName(), ctx.getTenant().getId(), e.getMessage(),
                            Long.toString(rc.getRetryDelay()), Integer.toString(rc.getAttempts()),
                            Integer.toString(rc.getRetryLimit()));
                    logger.error(msg, e);
                    metricsLogger.error(msg,e);
                    rc.delay();
                }
            }

        } catch (ZoneException e) {
            msg =
                    EELFResourceManager.format(Msg.EVACUATE_SERVER_FAILED, server.getName(), server.getId(), e.getMessage());
            logger.error(msg);
            metricsLogger.error(msg);
            throw new RequestFailedException("Evacute Server", msg, HttpStatus.BAD_GATEWAY_502, server);
        }

        if (rc.isFailed()) {
            msg = EELFResourceManager.format(Msg.CONNECTION_FAILED, provider.getName(), service.getURL());
            logger.error(msg);
            metricsLogger.error(msg);
            throw new RequestFailedException("Evacuate Server", msg, HttpStatus.BAD_GATEWAY_502, server);
        }
        rc.reset();
    }


    /**
     * @see org.openecomp.appc.adapter.iaas.ProviderAdapter#evacuateServer(java.util.Map, org.openecomp.sdnc.sli.SvcLogicContext)
     */
    private Server evacuateServer(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
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
        MDC.put("TargetServiceName", "evacuate server");
        MDC.put("ClassName", "org.openecomp.appc.adapter.iaas.provider.operation.impl.EvacuateServer");
        
        ctx.setAttribute("EVACUATE_STATUS", "ERROR");
        try {
            validateParametersExist(params, ProviderAdapter.PROPERTY_INSTANCE_URL,
                    ProviderAdapter.PROPERTY_PROVIDER_NAME);
            String vm_url = params.get(ProviderAdapter.PROPERTY_INSTANCE_URL);

            VMURL vm = VMURL.parseURL(vm_url);
            if (validateVM(rc, appName, vm_url, vm)) return null;
            
            IdentityURL ident = IdentityURL.parseURL(params.get(ProviderAdapter.PROPERTY_IDENTITY_URL));
            String identStr = (ident == null) ? null : ident.toString();
            
            // retrieve the optional parameters
            String rebuild_vm = params.get(ProviderAdapter.PROPERTY_REBUILD_VM);
            String targethost_id = params.get(ProviderAdapter.PROPERTY_TARGETHOST_ID);
            
            Context context = null;
            try {
                context = getContext(rc, vm_url, identStr);
                if (context != null) {
                	
                    server = lookupServer(rc, context, vm.getServerId());
                    logger.debug(Msg.SERVER_FOUND, vm_url, context.getTenantName(), server.getStatus().toString());
                    
                    // check target host status
                	if (isComputeNodeDown(context, targethost_id)) {
                		msg = EELFResourceManager.format(Msg.EVACUATE_SERVER_FAILED, server.getName(), server.getId(),
                                		"Target host " + targethost_id +" status is not UP/ENABLED");
                        logger.error(msg);
                        metricsLogger.error(msg);
                        throw new RequestFailedException("Evacuate Server", msg, HttpStatus.BAD_REQUEST_400, server);
                	}
                    
                    // save hypervisor name before evacuate
                    String hypervisor = server.getHypervisor().getHostName();

                    evacuateServer(rc, server, targethost_id);
                    
                    server.refreshAll();
                    String hypervisor_after_evacuate = server.getHypervisor().getHostName();
                    logger.debug("Hostname before evacuate: " + hypervisor + ", After evacuate: " + hypervisor_after_evacuate);
                    
                    // check hypervisor host name after evacuate. If it is unchanged, the evacuate failed.
                    if ((hypervisor != null) && (hypervisor.equals(hypervisor_after_evacuate))) {
                    	msg = EELFResourceManager.format(Msg.EVACUATE_SERVER_FAILED, server.getName(), server.getId(),
                    			"Hypervisor host " + hypervisor + " after evacuate is the same as before evacuate. Provider (ex. Openstack) recovery actions may be needed.");
                    	logger.error(msg);
                    	metricsLogger.error(msg);
                    	throw new RequestFailedException("Evacuate Server", msg, HttpStatus.INTERNAL_SERVER_ERROR_500, server);
  
                    }
                    
                    // check VM status after evacuate
                    if (server.getStatus() == Server.Status.ERROR) {
                    	msg = EELFResourceManager.format(Msg.EVACUATE_SERVER_FAILED, server.getName(), server.getId(),
                    			"VM is in ERROR state after evacuate. Provider (ex. Openstack) recovery actions may be needed.");
                    	logger.error(msg);
                    	metricsLogger.error(msg);
                    	throw new RequestFailedException("Evacuate Server", msg, HttpStatus.INTERNAL_SERVER_ERROR_500, server);
                    }
                    
                    context.close();
                    doSuccess(rc);
                    ctx.setAttribute("EVACUATE_STATUS", "SUCCESS");
                    
                    // If a snapshot exists, do a rebuild to apply the latest snapshot to the evacuated server.
                    // This is the default behavior unless the optional parameter is set to FALSE.
                    if ((rebuild_vm == null) || !(rebuild_vm.equalsIgnoreCase("false"))) {
                    	List<Image> snapshots = server.getSnapshots();
                        if (snapshots == null || snapshots.isEmpty()) {
                        	logger.debug("No snapshots available - skipping rebuild after evacuate");
                        } else if (paImpl != null) {
                    		logger.debug("Executing a rebuild after evacuate");
                    		paImpl.rebuildServer(params, ctx);
                    		// Check error code for rebuild errors. Evacuate had set it to 200 after
                    		// a successful evacuate. Rebuild updates the error code.
                    		String rebuildErrorCode = ctx.getAttribute(org.openecomp.appc.Constants.ATTRIBUTE_ERROR_CODE);
                            if (rebuildErrorCode != null) {
                            	try {
                            		int error_code = Integer.parseInt(rebuildErrorCode);
                            		if (error_code != HttpStatus.OK_200.getStatusCode()) {
                            			logger.debug("Rebuild after evacuate failed - error code=" + error_code
                            					+ ", message=" + ctx.getAttribute(org.openecomp.appc.Constants.ATTRIBUTE_ERROR_MESSAGE));
                            			msg = EELFResourceManager.format(Msg.EVACUATE_SERVER_REBUILD_FAILED, server.getName(), hypervisor,
                                    			hypervisor_after_evacuate, ctx.getAttribute(org.openecomp.appc.Constants.ATTRIBUTE_ERROR_MESSAGE));
                                    	logger.error(msg);
                                    	metricsLogger.error(msg);
                            			ctx.setAttribute("EVACUATE_STATUS", "ERROR");
                            			// update error message while keeping the error code the same as before
                            			doFailure(rc, HttpStatus.getHttpStatus(error_code), msg);
                            		}
                            	} catch (NumberFormatException e) {
                            		// ignore
                            	}
                			}
                    	}
                    }
                    
                }
            } catch (ResourceNotFoundException e) {
                msg = EELFResourceManager.format(Msg.SERVER_NOT_FOUND, e, vm_url);
                logger.error(msg);
                metricsLogger.error(msg);
                doFailure(rc, HttpStatus.NOT_FOUND_404, msg);
            } catch (RequestFailedException e) {
                doFailure(rc, e.getStatus(), e.getMessage());
            } catch (Throwable t) {
                msg = EELFResourceManager.format(Msg.SERVER_OPERATION_EXCEPTION, t, t.getClass().getSimpleName(),
                        Operation.EVACUATE_SERVICE.toString(), vm_url, context == null ? "Unknown" : context.getTenantName());
                logger.error(msg, t);
                metricsLogger.error(msg, t);
                doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, msg);
            }
        } catch (RequestFailedException e) {
        	msg = EELFResourceManager.format(Msg.EVACUATE_SERVER_FAILED, "n/a", "n/a", e.getMessage());
            logger.error(msg);
            metricsLogger.error(msg);
            doFailure(rc, e.getStatus(), e.getMessage());
        }

        return server;
    }

    /*
     * Check if a Compute node is down.
     * 
     * This method attempts to find a given host in the list of hypervisors for a given
     * context. The only case where a node is considered down is if a matching hypervisor
     * is found and it's state and status are not UP/ENABLED.
     * 
     * @param context
     * 			The current context
     * @param host
     * 			The host name (short or fully qualified) of a compute node
     * @return true if the node is determined as down, false for all other cases
     */
    private boolean isComputeNodeDown(Context context, String host) throws ZoneException {
    	ComputeService service = context.getComputeService();
    	boolean node_down = false;
        
        // Check host status. A node is considered down only if a matching target host is
        //  found and it's state/status is not UP/ENABLED.
        if ((host != null) && !(host.isEmpty())) {
        	List<Hypervisor> hypervisors = service.getHypervisors();
        	logger.debug("List of Hypervisors retrieved: " + Arrays.toString(hypervisors.toArray()));
            for (Hypervisor h : hypervisors) {
                if (h.getHostName().startsWith(host)) {
                	// host matches one of the hypervisors
                    State hstate = h.getState();
                    Status hstatus = h.getStatus();
                    logger.debug("Host matching hypervisor: " + h.getHostName() + ", State/Status: "
                    				+ hstate.toString() + "/" + hstatus.toString());
                    if ((hstate != null) && (hstatus != null)) {
                    	if (!(hstate.equals(State.UP)) || !(hstatus.equals(Status.ENABLED))) {
                    		node_down = true;
                    	}
                    }
                }
            }
        }
        return node_down;
    }
    
    @Override
    protected ModelObject executeProviderOperation(Map<String, String> params, SvcLogicContext context) throws APPCException {

        setMDC(Operation.EVACUATE_SERVICE.toString(), "App-C IaaS Adapter:Evacuate", ADAPTER_NAME);
        logOperation(Msg.EVACUATING_SERVER, params, context);
        
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
        MDC.put("TargetServiceName", "evacuate server");
        MDC.put("ClassName", "org.openecomp.appc.adapter.iaas.provider.operation.impl.EvacuateServer"); 
        
        
        metricsLogger.info("Executing Provider Operation: Evacuate");
        return evacuateServer(params, context);
    }
    
    public void setProvideAdapterRef(ProviderAdapterImpl pai) {
    	paImpl = pai;
    }
}
