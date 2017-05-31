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

package org.openecomp.appc.adapter.iaas.impl;

import com.woorea.openstack.base.client.OpenStackBaseException;
import com.woorea.openstack.heat.Heat;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.openecomp.appc.Constants;
import org.openecomp.appc.adapter.iaas.ProviderAdapter;
import org.openecomp.appc.adapter.openstack.heat.SnapshotResource;
import org.openecomp.appc.adapter.openstack.heat.StackResource;
import org.openecomp.appc.adapter.openstack.heat.model.CreateSnapshotParams;
import org.openecomp.appc.adapter.openstack.heat.model.Snapshot;
import org.openecomp.appc.configuration.Configuration;
import org.openecomp.appc.configuration.ConfigurationFactory;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.exceptions.UnknownProviderException;
import org.openecomp.appc.i18n.Msg;
import org.openecomp.appc.pool.Pool;
import org.openecomp.appc.pool.PoolExtensionException;
import org.openecomp.appc.util.StructuredPropertyHelper;
import org.openecomp.appc.util.StructuredPropertyHelper.Node;
import com.att.cdp.exceptions.*;
import com.att.cdp.openstack.OpenStackContext;
import com.att.cdp.openstack.connectors.HeatConnector;
import com.att.cdp.openstack.util.ExceptionMapper;
import com.att.cdp.pal.util.StringHelper;
import com.att.cdp.zones.*;
import com.att.cdp.zones.model.Image;
import com.att.cdp.zones.model.Server;
import com.att.cdp.zones.model.ServerBootSource;
import com.att.cdp.zones.model.Stack;
import com.att.cdp.zones.model.Server.Status;
import com.att.cdp.zones.spi.AbstractService;
import com.att.cdp.zones.spi.RequestState;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import org.openecomp.sdnc.sli.SvcLogicContext;
import org.slf4j.MDC;

import static com.att.eelf.configuration.Configuration.MDC_SERVICE_NAME;

import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * This class implements the {@link ProviderAdapter} interface. This interface defines the behaviors that our service
 * provides.
 */
@SuppressWarnings("javadoc")
public class ProviderAdapterImpl implements ProviderAdapter {

    /**
     * The name of the adapter
     */
    @SuppressWarnings("nls")
    private static final String ADAPTER_NAME = "Appc IaaS Adapter";

    /**
     * The username and password to use for dynamically created connections
     */
    private static String DEFAULT_USER;
    private static String DEFAULT_PASS;

    /**
     * The constant used to define the adapter name in the mapped diagnostic context
     */
    @SuppressWarnings("nls")
    private static final String MDC_ADAPTER = "adapter";

    /**
     * The constant used to define the service name in the mapped diagnostic context
     */
    @SuppressWarnings("nls")
    static final String MDC_SERVICE = "service";

    /**
     * The constant for the status code for a failed outcome
     */
    @SuppressWarnings("nls")
    private static final String OUTCOME_FAILURE = "failure";

    /**
     * The constant for the status code for a successful outcome
     */
    @SuppressWarnings("nls")
    private static final String OUTCOME_SUCCESS = "success";

    /**
     * A constant for the property token "provider" used in the structured property specifications
     */
    @SuppressWarnings("nls")
    private static final String PROPERTY_PROVIDER = "provider";

    /**
     * A constant for the property token "identity" used in the structured property specifications
     */
    @SuppressWarnings("nls")
    private static final String PROPERTY_PROVIDER_IDENTITY = "identity";

    /**
     * A constant for the property token "tenant" used in the structured property specifications
     */
    @SuppressWarnings("nls")
    private static final String PROPERTY_PROVIDER_TENANT = "tenant";

    /**
     * A constant for the property token "tenant name" used in the structured property specifications
     */
    @SuppressWarnings("nls")
    private static final String PROPERTY_PROVIDER_TENANT_NAME = "name";

    /**
     * A constant for the property token "password" used in the structured property specifications
     */
    @SuppressWarnings("nls")
    private static final String PROPERTY_PROVIDER_TENANT_PASSWORD = "password"; // NOSONAR

    /**
     * A constant for the property token "userid" used in the structured property specifications
     */
    @SuppressWarnings("nls")
    private static final String PROPERTY_PROVIDER_TENANT_USERID = "userid";

    /**
     * A constant for the property token "type" used in the structured property specifications
     */
    @SuppressWarnings("nls")
    private static final String PROPERTY_PROVIDER_TYPE = "type";

    /**
     * The name of the service to evacuate a server
     */
    @SuppressWarnings("nls")
    private static final String EVACUATE_SERVICE = "evacuateServer";

    /**
     * The name of the service to migrate a server
     */
    @SuppressWarnings("nls")
    private static final String MIGRATE_SERVICE = "migrateServer";

    /**
     * The name of the service to rebuild a server
     */
    @SuppressWarnings("nls")
    private static final String REBUILD_SERVICE = "rebuildServer";

    /**
     * The name of the service to restart a server
     */
    @SuppressWarnings("nls")
    private static final String RESTART_SERVICE = "restartServer";

    /**
	 * The name of the service to check status of  a server
	 */
	@SuppressWarnings("nls")
    private static final String VMSTATUSCHECK_SERVICE = "vmStatuschecker";


    /**
     * The name of the service to restart a server
     */
    @SuppressWarnings("nls")
    private static final String SNAPSHOT_SERVICE = "createSnapshot";

    /**
     * The name of the service to terminate a stack
     */
    @SuppressWarnings("nls")
    private static final String TERMINATE_STACK = "terminateStack";

    /**
     * The name of the service to snapshot a stack
     */
    @SuppressWarnings("nls")
    private static final String SNAPSHOT_STACK = "snapshotStack";

    /**
     * The name of a service to start a server
     */
    @SuppressWarnings("nls")
    private static final String START_SERVICE = "startServer";

    /**
     * The name of the service to stop a server
     */
    @SuppressWarnings("nls")
    private static final String STOP_SERVICE = "stopServer";

    /**
     * The name of the service to stop a server
     */
    @SuppressWarnings("nls")
    private static final String TERMINATE_SERVICE = "terminateServer";

    /**
     * The name of the service to lookup a server
     */
    @SuppressWarnings("nls")
    private static final String LOOKUP_SERVICE = "lookupServer";

    /**
     * The logger to be used
     */
    private static final EELFLogger logger = EELFManager.getInstance().getLogger(ProviderAdapterImpl.class);

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * The constant for a left parenthesis
     */
    private static final char LPAREN = '(';

    /**
     * The constant for a new line control code
     */
    private static final char NL = '\n';

    /**
     * The constant for a single quote
     */
    private static final char QUOTE = '\'';

    /**
     * The constant for a right parenthesis
     */
    private static final char RPAREN = ')';

    /**
     * The constant for a space
     */
    private static final char SPACE = ' ';

    /**
     * A reference to the adapter configuration object.
     */
    private Configuration configuration;

    /**
     * A cache of providers that are predefined.
     */
    private Map<String /* provider name */, ProviderCache> providerCache;

    /**
     * A list of valid initial VM statuses for a migrate operations
     */
    private static final Collection<Status> migratableStatuses = Arrays.asList(Status.READY, Status.RUNNING, Status.SUSPENDED);

    /**
     * This default constructor is used as a work around because the activator wasnt getting called
     */
    @SuppressWarnings("all")
    public ProviderAdapterImpl() {
        initialize();

    }

    /**
     * This constructor is used primarily in the test cases to bypass initialization of the adapter for isolated,
     * disconnected testing
     *
     * @param initialize
     *            True if the adapter is to be initialized, can false if not
     */
    @SuppressWarnings("all")
    public ProviderAdapterImpl(boolean initialize) {
        configuration = ConfigurationFactory.getConfiguration();
        if (initialize) {
            initialize();
        }
    }

    /**
     * @param props
     *            not used
     */
    public ProviderAdapterImpl(@SuppressWarnings("unused") Properties props) {
        initialize();

    }

    /**
     * Returns the symbolic name of the adapter
     *
     * @return The adapter name
     * @see org.openecomp.appc.adapter.iaas.ProviderAdapter#getAdapterName()
     */
    @Override
    public String getAdapterName() {
        return configuration.getProperty(Constants.PROPERTY_ADAPTER_NAME);
    }

    @SuppressWarnings("nls")
    @Override
    public Image createSnapshot(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        Image snapshot = null;
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();
        MDC.put(MDC_ADAPTER, ADAPTER_NAME);
        MDC.put(MDC_SERVICE, SNAPSHOT_SERVICE);
        MDC.put(MDC_SERVICE_NAME, "App-C IaaS Adapter:Snapshot");
        String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);
        logger.info(Msg.SNAPSHOTING_SERVER, appName);
        String msg;

        try {
            validateParametersExist(rc, params, ProviderAdapter.PROPERTY_INSTANCE_URL,
                ProviderAdapter.PROPERTY_PROVIDER_NAME);
            String vm_url = params.get(ProviderAdapter.PROPERTY_INSTANCE_URL);
            debugParameters(params);
            debugContext(ctx);

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
                        doFailure(rc, HttpStatus.FORBIDDEN_403, msg);
                    }
                    context.close();
                }
            } catch (ResourceNotFoundException e) {
                msg = EELFResourceManager.format(Msg.SERVER_NOT_FOUND, e, vm_url);
                logger.error(msg);
                doFailure(rc, HttpStatus.NOT_FOUND_404, msg);
            } catch (Throwable t) {
                msg = EELFResourceManager.format(Msg.SERVER_OPERATION_EXCEPTION, t, t.getClass().getSimpleName(),
                    SNAPSHOT_SERVICE, vm_url, context == null ? "Unknown" : context.getTenantName());
                logger.error(msg, t);
                doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, msg);
            }
        } catch (RequestFailedException e) {
            doFailure(rc, e.getStatus(), e.getMessage());
        }
        return snapshot;
    }

    private boolean validateVM(RequestContext rc, String appName, String vm_url, VMURL vm)
                    throws RequestFailedException {
        String msg;
        if (vm == null) {
            msg = EELFResourceManager.format(Msg.INVALID_SELF_LINK_URL, appName, vm_url);
            logger.error(msg);
            doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, msg);
            return true;
        }
        validateVMURL(vm);
        return false;
    }

    private Image createSnapshot(RequestContext rc, Server server) throws ZoneException, RequestFailedException {
        Context context = server.getContext();
        Provider provider = context.getProvider();
        ImageService service = context.getImageService(); // Already checked access by this point

        String snapshotName = generateSnapshotName(server.getName());

        logger.info(String.format("Creating snapshot of server %s (%s) with name %s", server.getName(), server.getId(),
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
                rc.delay();
            }
        }
        if (rc.isFailed()) {
            msg = EELFResourceManager.format(Msg.CONNECTION_FAILED, provider.getName(), service.getURL());
            logger.error(msg);
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
                rc.delay();
            }
        }
        if (rc.isFailed()) {
            msg = EELFResourceManager.format(Msg.CONNECTION_FAILED, provider.getName(), service.getURL());
            logger.error(msg);
            throw new RequestFailedException("Stop Server", msg, HttpStatus.BAD_GATEWAY_502, server);
        }
        rc.reset();

        // Wait for it to be ready
        waitForStateChange(rc, snapshot, Image.Status.ACTIVE);

        return snapshot;
    }

    private String generateSnapshotName(String server) {
        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
        return String.format("Snapshot of %s at %s", server, df.format(new Date()));
    }

    /**
     * @see org.openecomp.appc.adapter.iaas.ProviderAdapter#evacuateServer(java.util.Map, org.openecomp.sdnc.sli.SvcLogicContext)
     */
    @SuppressWarnings("nls")
    @Override
    public Server evacuateServer(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        Server server = null;
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();
        MDC.put(MDC_ADAPTER, ADAPTER_NAME);
        MDC.put(MDC_SERVICE, EVACUATE_SERVICE);
        MDC.put(MDC_SERVICE_NAME, "App-C IaaS Adapter:Evacuate");
        String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);
        logger.info(Msg.EVACUATING_SERVER, appName);
        String msg;

        try {
            validateParametersExist(rc, params, ProviderAdapter.PROPERTY_INSTANCE_URL,
                ProviderAdapter.PROPERTY_PROVIDER_NAME);
            String vm_url = params.get(ProviderAdapter.PROPERTY_INSTANCE_URL);
            String providerName = params.get(ProviderAdapter.PROPERTY_PROVIDER_NAME);
            debugParameters(params);
            debugContext(ctx);

            VMURL vm = VMURL.parseURL(vm_url);
            if (validateVM(rc, appName, vm_url, vm)) return null;

            Context context = null;
            try {
                context = getContext(rc, vm_url, providerName);
                if (context != null) {
                    server = lookupServer(rc, context, vm.getServerId());
                    logger.debug(Msg.SERVER_FOUND, vm_url, context.getTenantName(), server.getStatus().toString());
                    evacuateServer(rc, server);
                    server.refreshStatus();
                    context.close();
                    doSuccess(rc);
                }
            } catch (ResourceNotFoundException e) {
                msg = EELFResourceManager.format(Msg.SERVER_NOT_FOUND, e, vm_url);
                logger.error(msg);
                doFailure(rc, HttpStatus.NOT_FOUND_404, msg);
            } catch (Throwable t) {
                msg = EELFResourceManager.format(Msg.SERVER_OPERATION_EXCEPTION, t, t.getClass().getSimpleName(),
                    EVACUATE_SERVICE, vm_url, context == null ? "Unknown" : context.getTenantName());
                logger.error(msg, t);
                doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, msg);
            }
        } catch (RequestFailedException e) {
            doFailure(rc, e.getStatus(), e.getMessage());
        }

        return server;
    }

    /**
     * @see org.openecomp.appc.adapter.iaas.ProviderAdapter#migrateServer(java.util.Map, org.openecomp.sdnc.sli.SvcLogicContext)
     */
    @SuppressWarnings("nls")
    @Override
    public Server migrateServer(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        Server server = null;
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();
        MDC.put(MDC_ADAPTER, ADAPTER_NAME);
        MDC.put(MDC_SERVICE, MIGRATE_SERVICE);
        MDC.put(MDC_SERVICE_NAME, "App-C IaaS Adapter:Migrate");
        String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);
        logger.info(Msg.MIGRATING_SERVER, appName);
        String msg;

        try {
            validateParametersExist(rc, params, ProviderAdapter.PROPERTY_INSTANCE_URL,
                ProviderAdapter.PROPERTY_PROVIDER_NAME);
            String vm_url = params.get(ProviderAdapter.PROPERTY_INSTANCE_URL);
            debugParameters(params);
            debugContext(ctx);

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
                    migrateServer(rc, server);
                    server.refreshStatus();
                    context.close();
                    doSuccess(rc);
                }
            } catch (ResourceNotFoundException e) {
                msg = EELFResourceManager.format(Msg.SERVER_NOT_FOUND, e, vm_url);
                logger.error(msg);
                doFailure(rc, HttpStatus.NOT_FOUND_404, msg);
            } catch (Throwable t) {
                msg = EELFResourceManager.format(Msg.SERVER_OPERATION_EXCEPTION, t, t.getClass().getSimpleName(),
                    MIGRATE_SERVICE, vm_url, context == null ? "Unknown" : context.getTenantName());
                logger.error(msg, t);
                doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, msg);
            }
        } catch (RequestFailedException e) {
            doFailure(rc, e.getStatus(), e.getMessage());
        }

        return server;
    }

    private void evacuateServer(RequestContext rc, @SuppressWarnings("unused") Server server) throws ZoneException, RequestFailedException {
        doFailure(rc, HttpStatus.NOT_IMPLEMENTED_501, "The operation 'EVACUATE' is not yet implemented");
    }

    private void migrateServer(RequestContext rc, Server server) throws ZoneException, RequestFailedException {
        String msg;
        Context ctx = server.getContext();
        ComputeService service = ctx.getComputeService();

        // Init status will equal final status
        Status initialStatus = server.getStatus();

        if (initialStatus == null) {
            throw new ZoneException("Failed to determine server's starting status");
        }

        // We can only migrate certain statuses
        if (!migratableStatuses.contains(initialStatus)) {
            throw new ZoneException(String.format("Cannot migrate server that is in %s state. Must be in one of [%s]",
                initialStatus, migratableStatuses));
        }

        boolean inConfirmPhase = false;
        try {
            while (rc.attempt()) {
                try {
                    if (!inConfirmPhase) {
                        // Initial migrate request
                        service.migrateServer(server.getId());
                        // Wait for change to verify resize
                        waitForStateChange(rc, server, Status.READY);
                        inConfirmPhase = true;
                    }

                    // Verify resize
                    service.processResize(server);
                    // Wait for complete. will go back to init status
                    waitForStateChange(rc, server, initialStatus);
                    logger.info("Completed migrate request successfully");
                    return;
                } catch (ContextConnectionException e) {
                    msg = getConnectionExceptionMessage(rc, ctx, e);
                    logger.error(msg, e);
                    rc.delay();
                }
            }
        } catch (ZoneException e) {
            String phase = inConfirmPhase ? "VERIFY MIGRATE" : "REQUEST MIGRATE";
            msg = EELFResourceManager.format(Msg.MIGRATE_SERVER_FAILED, server.getName(), server.getId(), phase,
                e.getMessage());
            generateEvent(rc, false, msg);
            logger.error(msg, e);
            throw new RequestFailedException("Migrate Server", msg, HttpStatus.METHOD_NOT_ALLOWED_405, server);
        }

    }

    /**
     * @see org.openecomp.appc.adapter.iaas.ProviderAdapter#rebuildServer(java.util.Map, org.openecomp.sdnc.sli.SvcLogicContext)
     */
    @SuppressWarnings("nls")
    @Override
    public Server rebuildServer(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        Server server = null;
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();
        MDC.put(MDC_ADAPTER, ADAPTER_NAME);
        MDC.put(MDC_SERVICE, REBUILD_SERVICE);
        MDC.put(MDC_SERVICE_NAME, "App-C IaaS Adapter:Rebuild");
        String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);
        logger.info(Msg.REBUILDING_SERVER, appName);
        String msg;

        try {
            validateParametersExist(rc, params, ProviderAdapter.PROPERTY_INSTANCE_URL,
                ProviderAdapter.PROPERTY_PROVIDER_NAME);
            String vm_url = params.get(ProviderAdapter.PROPERTY_INSTANCE_URL);
            debugParameters(params);
            debugContext(ctx);

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

                    // Manually checking image service until new PAL release
                    if (hasImageAccess(rc, context)) {
                        rebuildServer(rc, server);
                        doSuccess(rc);
                    } else {
                        msg = EELFResourceManager.format(Msg.REBUILD_SERVER_FAILED, server.getName(), server.getId(),
                            "Accessing Image Service Failed");
                        logger.error(msg);
                        doFailure(rc, HttpStatus.FORBIDDEN_403, msg);
                    }
                    context.close();
                }
            } catch (ResourceNotFoundException e) {
                msg = EELFResourceManager.format(Msg.SERVER_NOT_FOUND, e, vm_url);
                logger.error(msg);
                doFailure(rc, HttpStatus.NOT_FOUND_404, msg);
            } catch (Throwable t) {
                msg = EELFResourceManager.format(Msg.SERVER_OPERATION_EXCEPTION, t, t.getClass().getSimpleName(),
                    STOP_SERVICE, vm_url, context == null ? "Unknown" : context.getTenantName());
                logger.error(msg, t);
                doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, msg);
            }
        } catch (RequestFailedException e) {
            doFailure(rc, e.getStatus(), e.getMessage());
        }

        return server;
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
    @Override
    public Server restartServer(Map<String, String> params, SvcLogicContext ctx)
        throws UnknownProviderException, IllegalArgumentException {
        Server server = null;
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();
        MDC.put(MDC_ADAPTER, ADAPTER_NAME);
        MDC.put(MDC_SERVICE, RESTART_SERVICE);
        MDC.put(MDC_SERVICE_NAME, "App-C IaaS Adapter:Restart");
        String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);
        logger.info(Msg.RESTARTING_SERVER, appName);

        try {
            validateParametersExist(rc, params, ProviderAdapter.PROPERTY_INSTANCE_URL,
                ProviderAdapter.PROPERTY_PROVIDER_NAME);
            debugParameters(params);
            debugContext(ctx);
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
                    restartServer(rc, server);
                    context.close();
                    doSuccess(rc);
                }
            } catch (ResourceNotFoundException e) {
                String msg = EELFResourceManager.format(Msg.SERVER_NOT_FOUND, e, vm_url);
                logger.error(msg);
                doFailure(rc, HttpStatus.NOT_FOUND_404, msg);
            } catch (Throwable t) {
                String msg = EELFResourceManager.format(Msg.SERVER_OPERATION_EXCEPTION, t, t.getClass().getSimpleName(),
                    RESTART_SERVICE, vm_url, context == null ? "Unknown" : context.getTenantName());
                logger.error(msg, t);
                doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, msg);
            }
        } catch (RequestFailedException e) {
            doFailure(rc, e.getStatus(), e.getMessage());
        }

        return server;
    }

    /* *********************************************************************************/
	/* DEVEN PANCHAL: This method is used to check the status of the VM               */
	/**********************************************************************************/
    public Server vmStatuschecker(Map<String, String> params, SvcLogicContext ctx) throws UnknownProviderException, IllegalArgumentException {
       Server server = null;
       RequestContext rc = new RequestContext(ctx);
       rc.isAlive();
       MDC.put(MDC_ADAPTER, ADAPTER_NAME);
       MDC.put(MDC_SERVICE, VMSTATUSCHECK_SERVICE);
       MDC.put(MDC_SERVICE_NAME, "App-C IaaS Adapter: vmstatuscheck");
       String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);

       try {
           validateParametersExist(rc, params, ProviderAdapter.PROPERTY_INSTANCE_URL,
               ProviderAdapter.PROPERTY_PROVIDER_NAME);
           debugParameters(params);
           debugContext(ctx);
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

                   String statusvm;
                   switch (server.getStatus()) {
                   case DELETED:
                   	statusvm = "deleted";
                       break;

                   case RUNNING:
                   	statusvm = "running";
                       break;

                   case ERROR:
                   	statusvm = "error";
                   	break;

                   case READY:
                   	statusvm = "ready";
                       break;

                   case PAUSED:
                   	statusvm = "paused";
                       break;

                   case SUSPENDED:
                   	statusvm = "suspended";
                       break;

                   case PENDING:
                   	statusvm = "pending";
                       break;

                   default:
                   	statusvm = "default-unknown state-should never occur";
                       break;
               }


                   String statusofVM = statusvm;
                   context.close();
                   SvcLogicContext svcLogic = rc.getSvcLogicContext();
                   svcLogic.setStatus(OUTCOME_SUCCESS);
                   svcLogic.setAttribute("org.openecomp.statusofvm", statusofVM);
                   svcLogic.setAttribute(Constants.STATUS_OF_VM, statusofVM);
                   svcLogic.setAttribute(Constants.ATTRIBUTE_ERROR_CODE, Integer.toString(HttpStatus.OK_200.getStatusCode()));
               }
           } catch (ResourceNotFoundException e) {
               String msg = EELFResourceManager.format(Msg.SERVER_NOT_FOUND, e, vm_url);
               logger.error(msg);
               doFailure(rc, HttpStatus.NOT_FOUND_404, msg);
           } catch (Throwable t) {
               String msg = EELFResourceManager.format(Msg.SERVER_OPERATION_EXCEPTION, t, t.getClass().getSimpleName(),
                   RESTART_SERVICE, vm_url, context == null ? "Unknown" : context.getTenantName());
               logger.error(msg, t);
               doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, msg);
           }
       } catch (RequestFailedException e) {
           doFailure(rc, e.getStatus(), e.getMessage());
       }

       return server;
   }

   /* *********************************************************************************/


    /**
     * @see org.openecomp.appc.adapter.iaas.ProviderAdapter#startServer(java.util.Map, org.openecomp.sdnc.sli.SvcLogicContext)
     */
    @SuppressWarnings("nls")
    @Override
    public Server startServer(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        Server server = null;
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();
        MDC.put(MDC_ADAPTER, ADAPTER_NAME);
        MDC.put(MDC_SERVICE, START_SERVICE);
        String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);
        logger.info(Msg.RESTARTING_SERVER, appName);

        try {
            validateParametersExist(rc, params, ProviderAdapter.PROPERTY_INSTANCE_URL,
                ProviderAdapter.PROPERTY_PROVIDER_NAME);
            debugParameters(params);
            debugContext(ctx);
            String vm_url = params.get(ProviderAdapter.PROPERTY_INSTANCE_URL);
            String providerName = params.get(ProviderAdapter.PROPERTY_PROVIDER_NAME);

            VMURL vm = VMURL.parseURL(vm_url);
            if (validateVM(rc, appName, vm_url, vm)) return null;

            Context context = null;
            try {
                context = getContext(rc, vm_url, providerName);
                if (context != null) {
                    server = lookupServer(rc, context, vm.getServerId());
                    logger.debug(Msg.SERVER_FOUND, vm_url, context.getTenantName(), server.getStatus().toString());
                    stopServer(rc, server);
                    server.refreshStatus();
                    context.close();
                    doSuccess(rc);
                }
            } catch (ResourceNotFoundException e) {
                String msg = EELFResourceManager.format(Msg.SERVER_NOT_FOUND, e, vm_url);
                logger.error(msg);
                doFailure(rc, HttpStatus.NOT_FOUND_404, msg);
            } catch (Throwable t) {
                String msg = EELFResourceManager.format(Msg.SERVER_OPERATION_EXCEPTION, t, t.getClass().getSimpleName(),
                    START_SERVICE, vm_url, context == null ? "Unknown" : context.getTenantName());
                logger.error(msg, t);
                doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, msg);
            }
        } catch (RequestFailedException e) {
            doFailure(rc, e.getStatus(), e.getMessage());
        }

        return server;
    }

    /**
     * @see org.openecomp.appc.adapter.iaas.ProviderAdapter#stopServer(java.util.Map, org.openecomp.sdnc.sli.SvcLogicContext)
     */
    @SuppressWarnings("nls")
    @Override
    public Server stopServer(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        Server server = null;
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();
        MDC.put(MDC_ADAPTER, ADAPTER_NAME);
        MDC.put(MDC_SERVICE, STOP_SERVICE);
        String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);
        logger.info(Msg.STOPPING_SERVER, appName);

        try {
            validateParametersExist(rc, params, ProviderAdapter.PROPERTY_INSTANCE_URL,
                ProviderAdapter.PROPERTY_PROVIDER_NAME);
            debugParameters(params);
            debugContext(ctx);
            String vm_url = params.get(ProviderAdapter.PROPERTY_INSTANCE_URL);
            ctx.setAttribute("STOP_STATUS", "SUCCESS");

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
                    if (server.getStatus().equals(Status.PENDING)) {
                        throw new RequestFailedException("Server is in pending Status");
                    }
                    stopServer(rc, server);
                    server.refreshStatus();
                    if (server.getStatus().equals(Status.ERROR)) {
                        throw new RequestFailedException("Server is in ERROR state after operation");
                    }
                    context.close();
                    doSuccess(rc);
                }else{
                    ctx.setAttribute("STOP_STATUS", "SERVER_NOT_FOUND");
                }
            } catch (ResourceNotFoundException e) {
                String msg = EELFResourceManager.format(Msg.SERVER_NOT_FOUND, e, vm_url);
                ctx.setAttribute("STOP_STATUS", "SERVER_NOT_FOUND");
                logger.error(msg);
                doFailure(rc, HttpStatus.NOT_FOUND_404, msg);
            } catch (Throwable t) {
                String msg = EELFResourceManager.format(Msg.SERVER_OPERATION_EXCEPTION, t, t.getClass().getSimpleName(),
                    STOP_SERVICE, vm_url, context == null ? "Unknown" : context.getTenantName());
                logger.error(msg, t);
                ctx.setAttribute("STOP_STATUS", "ERROR");
                doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, msg);
            }
        } catch (RequestFailedException e) {
            logger.error(EELFResourceManager.format(Msg.STOP_SERVER_FAILED, appName, "n/a", "n/a", e.getMessage()));
            ctx.setAttribute("STOP_STATUS", "ERROR");
            doFailure(rc, e.getStatus(), e.getMessage());
        }

        return server;
    }

    /**
     * This method is used to validate that the parameters contain all required property names, and that the values are
     * non-null and non-empty strings. We are still not ensured that the value is valid, but at least it exists.
     *
     * @param ctx
     *            The request context object that manages the request
     * @param parameters
     *            The parameters to be checked
     * @param propertyNames
     *            The list of property names that are required to be present.
     * @throws RequestFailedException
     *             If the parameters are not valid
     */
    @SuppressWarnings({
        "nls", "static-method"
    })
    private void validateParametersExist(@SuppressWarnings("unused") RequestContext ctx, Map<String, String> parameters, String... propertyNames)
        throws RequestFailedException {
        boolean success = true;
        StringBuilder msg = new StringBuilder(EELFResourceManager.format(Msg.MISSING_REQUIRED_PROPERTIES, MDC.get(MDC_SERVICE)));
        msg.append(NL);
        for (String propertyName : propertyNames) {
            String value = parameters.get(propertyName);
            if (value == null || value.trim().length() == 0) {
                success = false;
                msg.append(QUOTE);
                msg.append(propertyName);
                msg.append(QUOTE);
                msg.append(SPACE);
            }
        }

        if (!success) {
            logger.error(msg.toString());
            throw new RequestFailedException("Check Parameters", msg.toString(), HttpStatus.BAD_REQUEST_400, (Server)null);
        }
    }

    /**
     * This method is used to create a diagnostic dump of the context for the log
     *
     * @param context
     *            The context to be dumped
     */
    @SuppressWarnings({
        "nls", "static-method"
    })
    private void debugContext(SvcLogicContext context) {
        Set<String> keys = context.getAttributeKeySet();
        StringBuilder builder = new StringBuilder();

        builder.append("Service Logic Context: Status ");
        builder.append(LPAREN);
        builder.append(context.getStatus());
        builder.append(RPAREN);
        builder.append(", Attribute count ");
        builder.append(LPAREN);
        builder.append(keys == null ? "none" : Integer.toString(keys.size()));
        builder.append(RPAREN);
        if (keys != null && !keys.isEmpty()) {
            builder.append(NL);
            for (String key : keys) {
                String value = context.getAttribute(key);
                builder.append("Attribute ");
                builder.append(LPAREN);
                builder.append(key);
                builder.append(RPAREN);
                builder.append(", value ");
                builder.append(LPAREN);
                builder.append(value == null ? "" : value);
                builder.append(RPAREN);
                builder.append(NL);
            }
        }

        logger.debug(builder.toString());
    }

    void validateVMURL(VMURL vm) throws RequestFailedException {
        String name = "vm-id";
        if (vm == null) {
            throw new RequestFailedException(String.format("The value %s cannot be null.", name));
        }

        // Check that its a good uri
        // This will probably never get hit bc of an earlier check while parsing
        // the string to a VMURL
        try {
            //noinspection ResultOfMethodCallIgnored
            URI.create(vm.toString());
        } catch (Exception e) {
            throw new RequestFailedException(
                String.format("The value %s is not well formed [%s].", name, vm.toString()));
        }

        // Check the tenant and vmid segments
        String patternRegex = "([0-9a-f]{8}(-)?[0-9a-f]{4}(-)?[0-9a-f]{4}(-)?[0-9a-f]{4}(-)?[0-9a-f]{12})";
        Pattern pattern = Pattern.compile(patternRegex, Pattern.CASE_INSENSITIVE);

        if (!pattern.matcher(vm.getTenantId()).matches()) {
            throw new RequestFailedException(
                String.format("The value %s has an invalid tenantId [%s].", name, vm.getTenantId()));
        }
        if (!pattern.matcher(vm.getServerId()).matches()) {
            throw new RequestFailedException(
                String.format("The value %s has an invalid serverId [%s].", name, vm.getServerId()));
        }
    }

    @SuppressWarnings("unused")
    private void validateIdentityURL(IdentityURL id) throws RequestFailedException {
        String name = "identity-url";
        if (id == null) {
            throw new RequestFailedException(String.format("The value %s cannot be null.", name));
        }

        // Check that its a good uri
        // This will probably never get hit bc of an earlier check while parsing
        // the string to a VMURL
        try {
            //noinspection ResultOfMethodCallIgnored
            URI.create(id.toString());
        } catch (Exception e) {
            throw new RequestFailedException(
                String.format("The value %s is not well formed [%s].", name, id.toString()));
        }
    }

    /**
     * This method is used to dump the value of the parameters to the log for debugging purposes.
     *
     * @param parameters
     *            The parameters to be printed to the log
     */
    @SuppressWarnings("static-method")
    private void debugParameters(Map<String, String> parameters) {
        for (String key : parameters.keySet()) {
            logger.debug(Msg.PROPERTY_VALUE, key, parameters.get(key));
        }
    }

    /**
     * @param rc
     *            The request context that manages the state and recovery of the request for the life of its processing.
     * @param code
     * @param message
     */
    @SuppressWarnings("static-method")
    private void doFailure(RequestContext rc, HttpStatus code, String message) {
        try {
            doFailure(rc, code, message, null);
        } catch (APPCException ignored) {/* never happens */}
    }


    private void doFailure(RequestContext rc, HttpStatus code, String message, Throwable cause) throws APPCException {
        SvcLogicContext svcLogic = rc.getSvcLogicContext();
        String msg = (message == null) ? code.getReasonPhrase() : message;
        if (msg.contains("\n")) {
            msg = msg.substring(0, msg.indexOf("\n"));
        }
        String status;
        try {
            status = Integer.toString(code.getStatusCode());
        } catch (Exception e) {
            status = "500";
        }
        svcLogic.setStatus(OUTCOME_FAILURE);
        svcLogic.setAttribute(Constants.ATTRIBUTE_ERROR_CODE, status);
        svcLogic.setAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE, msg);
        svcLogic.setAttribute(Constants.DG_OUTPUT_STATUS_MESSAGE, msg);

        if (null != cause) throw new APPCException(cause);
    }

    /**
     * @param rc
     *            The request context that manages the state and recovery of the request for the life of its processing.
     */
    @SuppressWarnings("static-method")
    private void doSuccess(RequestContext rc) {
        SvcLogicContext svcLogic = rc.getSvcLogicContext();
        svcLogic.setStatus(OUTCOME_SUCCESS);
        svcLogic.setAttribute(Constants.ATTRIBUTE_ERROR_CODE, Integer.toString(HttpStatus.OK_200.getStatusCode()));
    }

    /**
     * Generates the event indicating what happened
     *
     * @param rc
     *            The request context that manages the state and recovery of the request for the life of its processing.
     * @param success
     *            True if the event represents a successful outcome
     * @param msg
     *            The detailed message
     */
    private void generateEvent(@SuppressWarnings("unused") RequestContext rc, @SuppressWarnings("unused") boolean success, @SuppressWarnings("unused") String msg) {
        // indication to the DG to generate the event?
    }

    /**
     * This method is a general helper method used to locate a server given its fully-qualified self-link URL on a
     * supported provider, regardless of region(s), and to return an opened context that can be used to access that
     * server.
     *
     * @param rc
     *            The request context that wraps and manages the state of the request
     * @param selfLinkURL
     *            The fully-qualified self-link URL of the server
     * @param providerName
     *            The name of the provider to be searched
     * @return The context that can be used to access the server, or null if not found.
     */
    @SuppressWarnings("nls")
    private Context getContext(RequestContext rc, String selfLinkURL, String providerName) {
        VMURL vm = VMURL.parseURL(selfLinkURL);
        IdentityURL ident = IdentityURL.parseURL(providerName);
        String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);

        if (vm == null) {
            String msg = EELFResourceManager.format(Msg.INVALID_SELF_LINK_URL, appName, selfLinkURL);
            logger.error(msg);
            doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, msg);
            return null;
        }

        /*
         * Get the cache of tenants and contexts for the named provider, if one exists
         */
        ProviderCache cache = providerCache.get(providerName);

        /*
         * If one doesn't exist, try and create it. If we have enough information to create it successfully, add it to
         * the cache and continue, otherwise fail the request.
         */
        if (cache == null) {
            if (ident != null) {
                cache = createProviderCache(vm, ident);
            }
            if (cache != null) {
                providerCache.put(cache.getProviderName(), cache);
            } else {
                String msg =
                    EELFResourceManager.format(Msg.UNKNOWN_PROVIDER, providerName, providerCache.keySet().toString());
                logger.error(msg);
                doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, msg);
                return null;
            }
        }

        if (providerName == null) {
            logger
                .debug(String.format("Using the default provider cache [%s] since no valid identity url was passed in.",
                    cache.getIdentityURL()));
        }

        // get the tenant cache for the vm
        String identityURL = cache.getIdentityURL();
         TenantCache tenantCache = cache.getTenant(vm.getTenantId());

        if(tenantCache == null){
            //no tenantCache matching tenant, add tenant to the provider cache
                tenantCache = cache.addTenant(vm.getTenantId(),null,DEFAULT_USER, DEFAULT_PASS);

                if(tenantCache == null){
                    //tenant not found
                    String msg = EELFResourceManager.format(Msg.SERVER_NOT_FOUND, selfLinkURL);
                    logger.error(msg);
                    doFailure(rc, HttpStatus.NOT_FOUND_404, msg);
                    return null;
                }
        }

        //reserve the context
        String tenantName = tenantCache.getTenantName();
        String tenantId = tenantCache.getTenantId();
        String region = tenantCache.determineRegion(vm);

        if (region != null) {
            Pool<Context> pool = tenantCache.getPools().get(region);

            while (rc.attempt()) {
                try {
                    Context context = pool.reserve();

                    /*
                     * Insert logic here to test the context for connectivity because we may have gotten one from
                     * the pool that was previously created.
                     */
                    if (context.isStale()) {
                        context.relogin();
                    }
                    return context;
                } catch (PoolExtensionException e) {
                    String msg = EELFResourceManager.format(Msg.CONNECTION_FAILED_RETRY, providerName, identityURL,
                        tenantName, tenantId, e.getMessage(), Long.toString(rc.getRetryDelay()),
                        Integer.toString(rc.getAttempts()), Integer.toString(rc.getRetryLimit()));
                    logger.error(msg, e);
                    rc.delay();
                } catch (Exception e) {
                    String msg = EELFResourceManager.format(Msg.SERVER_OPERATION_EXCEPTION, e,
                        e.getClass().getSimpleName(), "find", selfLinkURL, tenantCache.getTenantName());

                    logger.error(msg, e);
                    doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, msg);
                    return null;
                }
            }

            String msg = EELFResourceManager.format(Msg.CONNECTION_FAILED, providerName, identityURL);
            logger.error(msg);
            doFailure(rc, HttpStatus.BAD_GATEWAY_502, msg);
            return null;
        }


        String msg = EELFResourceManager.format(Msg.SERVER_NOT_FOUND, selfLinkURL);
        logger.error(msg);
        doFailure(rc, HttpStatus.NOT_FOUND_404, msg);
        return null;
    }

    /**
     * initialize the provider adapter by building the context cache
     */
    private void initialize() {
        configuration = ConfigurationFactory.getConfiguration();

        /*
         * Initialize the provider cache for all defined providers. The definition of the providers uses a structured
         * property set, where the names form a hierarchical name space (dotted notation, such as one.two.three). Each
         * name in the name space can also be serialized by appending a sequence number. All nodes at the same level
         * with the same serial number are grouped together in the namespace hierarchy. This allows a hierarchical
         * multi-valued property to be defined, which can then be used to setup the provider and tenant caches.
         * <p>
         * For example, the following definitions show how the namespace hierarchy is defined for two providers, with
         * two tenants on the first provider and a single tenant for the second provider. <pre>
         * provider1.type=OpenStackProvider1
         * provider1.name=OpenStackProviderName1
         * provider1.identity=http://192.168.1.2:5000/v2.0
         * provider1.tenant1.name=MY-TENANT-NAME
         * provider1.tenant1.userid=userid
         * provider1.tenant1.password=userid@123
         * provider1.tenant2.name=MY-TENANT-NAME
         * provider1.tenant2.userid=userid
         * provider1.tenant2.password=userid@123
         * provider2.type=OpenStackProvider2
         * provider2.name=OpenStackProviderName2
         * provider2.identity=http://192.168.1.2:5000/v2.0
         * provider2.tenant1.name=MY-TENANT-NAME
         * provider2.tenant1.userid=userid
         * provider2.tenant1.password=userid@123
         * </pre>
         * </p>
         */
        providerCache = new HashMap<>();
        Properties properties = configuration.getProperties();
        List<Node> providers = StructuredPropertyHelper.getStructuredProperties(properties, PROPERTY_PROVIDER);

        for (Node provider : providers) {
            ProviderCache cache = new ProviderCache();
            List<Node> providerNodes = provider.getChildren();
            for (Node node : providerNodes) {
                if (node.getName().equals(PROPERTY_PROVIDER_TYPE)) {
                    cache.setProviderType(node.getValue());
                } else if (node.getName().equals(PROPERTY_PROVIDER_IDENTITY)) {
                    cache.setIdentityURL(node.getValue());
                    cache.setProviderName(node.getValue());
                } else if (node.getName().startsWith(PROPERTY_PROVIDER_TENANT)) {
                    String tenantName = null;
                    String userId = null;
                    String password = null;
                    for (Node node2 : node.getChildren()) {
                        switch (node2.getName()) {
                            case PROPERTY_PROVIDER_TENANT_NAME:
                                tenantName = node2.getValue();
                                break;
                            case PROPERTY_PROVIDER_TENANT_USERID:
                                userId = node2.getValue();
                                DEFAULT_USER = node2.getValue();
                                break;
                            case PROPERTY_PROVIDER_TENANT_PASSWORD:
                                password = node2.getValue();
                                DEFAULT_PASS = node2.getValue();
                                break;
                        }
                    }

                    cache.addTenant(null, tenantName, userId, password);
                }
            }

            /*
             * Add the provider to the set of providers cached
             */
            if (cache.getIdentityURL() != null && cache.getProviderType() != null) {
                providerCache.put(null, cache);
                providerCache.put(cache.getIdentityURL(), cache);
            }

            /*
             * Now, initialize the cache for the loaded provider
             */
            cache.initialize();
        }
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
    private void rebuildServer(RequestContext rc, Server server) throws ZoneException, RequestFailedException {

        ServerBootSource builtFrom = server.getBootSource();
        String msg;

        // Throw exception for non image/snap boot source
        if (ServerBootSource.VOLUME.equals(builtFrom)) {
            msg = String.format("Rebuilding is currently not supported for servers built from bootable volumes [%s]",
                server.getId());
            generateEvent(rc, false, msg);
            logger.error(msg);
            throw new RequestFailedException("Rebuild Server", msg, HttpStatus.FORBIDDEN_403, server);
        }
        /*
         * Pending is a bit of a special case. If we find the server is in a pending state, then the provider is in the
         * process of changing state of the server. So, lets try to wait a little bit and see if the state settles down
         * to one we can deal with. If not, then we have to fail the request.
         */
        Context context = server.getContext();
        Provider provider = context.getProvider();
        ComputeService service = context.getComputeService();
        if (server.getStatus().equals(Status.PENDING)) {
            waitForStateChange(rc, server, Status.READY, Status.RUNNING, Status.ERROR, Status.SUSPENDED, Status.PAUSED);
        }

        /*
         * Get the image to use. This is determined by the presence or absence of snapshot images. If any snapshots
         * exist, then the latest snapshot is used, otherwise the image used to construct the VM is used.
         */
        List<Image> snapshots = server.getSnapshots();
        String imageToUse;
        if (snapshots != null && !snapshots.isEmpty()) {
            imageToUse = snapshots.get(0).getId();
        } else {
            imageToUse = server.getImage();
            ImageService imageService = server.getContext().getImageService();
            try {
                while (rc.attempt()) {
                    try {
                        /*
                         * We are just trying to make sure that the image exists. We arent interested in the details at
                         * this point.
                         */
                        imageService.getImage(imageToUse);
                        break;
                    } catch (ContextConnectionException e) {
                        msg = EELFResourceManager.format(Msg.CONNECTION_FAILED_RETRY, provider.getName(),
                            imageService.getURL(), context.getTenant().getName(), context.getTenant().getId(),
                            e.getMessage(), Long.toString(rc.getRetryDelay()), Integer.toString(rc.getAttempts()),
                            Integer.toString(rc.getRetryLimit()));
                        logger.error(msg, e);
                        rc.delay();
                    }
                }
            } catch (ZoneException e) {
                msg = EELFResourceManager.format(Msg.IMAGE_NOT_FOUND, imageToUse, "rebuild");
                generateEvent(rc, false, msg);
                logger.error(msg);
                throw new RequestFailedException("Rebuild Server", msg, HttpStatus.METHOD_NOT_ALLOWED_405, server);
            }
        }
        if (rc.isFailed()) {
            msg = EELFResourceManager.format(Msg.CONNECTION_FAILED, provider.getName(), service.getURL());
            logger.error(msg);
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
                throw new RequestFailedException("Rebuild Server", msg, HttpStatus.METHOD_NOT_ALLOWED_405, server);

            case RUNNING:
                // Attempt to stop the server, then rebuild it
                stopServer(rc, server);
                rebuildServer(rc, server, imageToUse);
                startServer(rc, server);
                generateEvent(rc, true, OUTCOME_SUCCESS);
                break;

            case ERROR:
                msg = EELFResourceManager.format(Msg.SERVER_ERROR_STATE, server.getName(), server.getId(),
                    server.getTenantId(), "rebuild");
                generateEvent(rc, false, msg);
                logger.error(msg);
                throw new RequestFailedException("Rebuild Server", msg, HttpStatus.METHOD_NOT_ALLOWED_405, server);

            case READY:
                // Attempt to rebuild the server
                rebuildServer(rc, server, imageToUse);
                startServer(rc, server);
                generateEvent(rc, true, OUTCOME_SUCCESS);
                break;

            case PAUSED:
                // if paused, un-pause it, stop it, and rebuild it
                unpauseServer(rc, server);
                stopServer(rc, server);
                rebuildServer(rc, server, imageToUse);
                startServer(rc, server);
                generateEvent(rc, true, OUTCOME_SUCCESS);
                break;

            case SUSPENDED:
                // Attempt to resume the suspended server, stop it, and rebuild it
                resumeServer(rc, server);
                stopServer(rc, server);
                rebuildServer(rc, server, imageToUse);
                startServer(rc, server);
                generateEvent(rc, true, OUTCOME_SUCCESS);
                break;

            default:
                // Hmmm, unknown status, should never occur
                msg = EELFResourceManager.format(Msg.UNKNOWN_SERVER_STATE, server.getName(), server.getId(),
                    server.getTenantId(), server.getStatus().name());
                generateEvent(rc, false, msg);
                logger.error(msg);
                throw new RequestFailedException("Rebuild Server", msg, HttpStatus.METHOD_NOT_ALLOWED_405, server);
        }
    }

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
    private void restartServer(RequestContext rc, Server server) throws ZoneException, RequestFailedException {
        /*
         * Pending is a bit of a special case. If we find the server is in a pending state, then the provider is in the
         * process of changing state of the server. So, lets try to wait a little bit and see if the state settles down
         * to one we can deal with. If not, then we have to fail the request.
         */
        String msg;
        if (server.getStatus().equals(Status.PENDING)) {
            waitForStateChange(rc, server, Status.READY, Status.RUNNING, Status.ERROR, Status.SUSPENDED, Status.PAUSED);
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
                break;

            case RUNNING:
                // Attempt to stop and start the server
                stopServer(rc, server);
                startServer(rc, server);
                generateEvent(rc, true, OUTCOME_SUCCESS);
                break;

            case ERROR:
                msg = EELFResourceManager.format(Msg.SERVER_ERROR_STATE, server.getName(), server.getId(),
                    server.getTenantId(), "rebuild");
                generateEvent(rc, false, msg);
                logger.error(msg);
                throw new RequestFailedException("Rebuild Server", msg, HttpStatus.METHOD_NOT_ALLOWED_405, server);

            case READY:
                // Attempt to start the server
                startServer(rc, server);
                generateEvent(rc, true, OUTCOME_SUCCESS);
                break;

            case PAUSED:
                // if paused, un-pause it
                unpauseServer(rc, server);
                generateEvent(rc, true, OUTCOME_SUCCESS);
                break;

            case SUSPENDED:
                // Attempt to resume the suspended server
                resumeServer(rc, server);
                generateEvent(rc, true, OUTCOME_SUCCESS);
                break;

            default:
                // Hmmm, unknown status, should never occur
                msg = EELFResourceManager.format(Msg.UNKNOWN_SERVER_STATE, server.getName(), server.getId(),
                    server.getTenantId(), server.getStatus().name());
                generateEvent(rc, false, msg);
                logger.error(msg);
                break;
        }

    }

    /**
     * Resume a suspended server and wait for it to enter a running state
     *
     * @param rc
     *            The request context that manages the state and recovery of the request for the life of its processing.
     * @param server
     *            The server to be resumed
     * @throws ZoneException
     * @throws RequestFailedException
     */
    @SuppressWarnings("nls")
    private void resumeServer(RequestContext rc, Server server) throws ZoneException, RequestFailedException {
        logger.debug(Msg.RESUME_SERVER, server.getId());

        Context context = server.getContext();
        String msg;
        Provider provider = context.getProvider();
        ComputeService service = context.getComputeService();
        while (rc.attempt()) {
            try {
                server.resume();
                break;
            } catch (ContextConnectionException e) {
                msg = EELFResourceManager.format(Msg.CONNECTION_FAILED_RETRY, provider.getName(), service.getURL(),
                    context.getTenant().getName(), context.getTenant().getId(), e.getMessage(),
                    Long.toString(rc.getRetryDelay()), Integer.toString(rc.getAttempts()),
                    Integer.toString(rc.getRetryLimit()));
                logger.error(msg, e);
                rc.delay();
            }
        }
        if (rc.isFailed()) {
            msg = EELFResourceManager.format(Msg.CONNECTION_FAILED, provider.getName(), service.getURL());
            logger.error(msg);
            throw new RequestFailedException("Resume Server", msg, HttpStatus.BAD_GATEWAY_502, server);
        }
        rc.reset();
        waitForStateChange(rc, server, Status.RUNNING);
    }

    /**
     * Start the server and wait for it to enter a running state
     *
     * @param rc
     *            The request context that manages the state and recovery of the request for the life of its processing.
     * @param server
     *            The server to be started
     * @throws ZoneException
     * @throws RequestFailedException
     */
    @SuppressWarnings("nls")
    private void startServer(RequestContext rc, Server server) throws ZoneException, RequestFailedException {
        logger.debug(Msg.START_SERVER, server.getId());
        String msg;
        Context context = server.getContext();
        Provider provider = context.getProvider();
        ComputeService service = context.getComputeService();
        while (rc.attempt()) {
            try {
                server.start();
                break;
            } catch (ContextConnectionException e) {
                msg = EELFResourceManager.format(Msg.CONNECTION_FAILED_RETRY, provider.getName(), service.getURL(),
                    context.getTenant().getName(), context.getTenant().getId(), e.getMessage(),
                    Long.toString(rc.getRetryDelay()), Integer.toString(rc.getAttempts()),
                    Integer.toString(rc.getRetryLimit()));
                logger.error(msg, e);
                rc.delay();
            }
        }
        if (rc.isFailed()) {
            msg = EELFResourceManager.format(Msg.CONNECTION_FAILED, provider.getName(), service.getURL());
            logger.error(msg);
            throw new RequestFailedException("Start Server", msg, HttpStatus.BAD_GATEWAY_502, server);
        }
        rc.reset();
        waitForStateChange(rc, server, Status.RUNNING);
    }

    /**
     * Stop the specified server and wait for it to stop
     *
     * @param rc
     *            The request context that manages the state and recovery of the request for the life of its processing.
     * @param server
     *            The server to be stopped
     * @throws ZoneException
     * @throws RequestFailedException
     */
    @SuppressWarnings("nls")
    private void stopServer(RequestContext rc, Server server) throws ZoneException, RequestFailedException {
        logger.debug(Msg.STOP_SERVER, server.getId());

        String msg;
        Context context = server.getContext();
        Provider provider = context.getProvider();
        ComputeService service = context.getComputeService();
        while (rc.attempt()) {
            try {
                server.stop();
                break;
            } catch (ContextConnectionException e) {
                msg = EELFResourceManager.format(Msg.CONNECTION_FAILED_RETRY, provider.getName(), service.getURL(),
                    context.getTenant().getName(), context.getTenant().getId(), e.getMessage(),
                    Long.toString(rc.getRetryDelay()), Integer.toString(rc.getAttempts()),
                    Integer.toString(rc.getRetryLimit()));
                logger.error(msg, e);
                rc.delay();
            }
        }
        if (rc.isFailed()) {
            msg = EELFResourceManager.format(Msg.CONNECTION_FAILED, provider.getName(), service.getURL());
            logger.error(msg);
            throw new RequestFailedException("Stop Server", msg, HttpStatus.BAD_GATEWAY_502, server);
        }
        rc.reset();
        waitForStateChange(rc, server, Status.READY, Status.ERROR);
    }

    /**
     * Un-Pause a paused server and wait for it to enter a running state
     *
     * @param rc
     *            The request context that manages the state and recovery of the request for the life of its processing.
     * @param server
     *            The server to be un-paused
     * @throws ZoneException
     * @throws RequestFailedException
     */
    @SuppressWarnings("nls")
    private void unpauseServer(RequestContext rc, Server server) throws ZoneException, RequestFailedException {
        logger.debug(Msg.UNPAUSE_SERVER, server.getId());

        String msg;
        Context context = server.getContext();
        Provider provider = context.getProvider();
        ComputeService service = context.getComputeService();
        while (rc.attempt()) {
            try {
                server.unpause();
                break;
            } catch (ContextConnectionException e) {
                msg = EELFResourceManager.format(Msg.CONNECTION_FAILED_RETRY, provider.getName(), service.getURL(),
                    context.getTenant().getName(), context.getTenant().getId(), e.getMessage(),
                    Long.toString(rc.getRetryDelay()), Integer.toString(rc.getAttempts()),
                    Integer.toString(rc.getRetryLimit()));
                logger.error(msg, e);
                rc.delay();
            }
        }
        if (rc.isFailed()) {
            msg = EELFResourceManager.format(Msg.CONNECTION_FAILED, provider.getName(), service.getURL());
            logger.error(msg);
            throw new RequestFailedException("Unpause Server", msg, HttpStatus.BAD_GATEWAY_502, server);
        }
        rc.reset();
        waitForStateChange(rc, server, Status.RUNNING, Status.READY);
    }

    /**
     * Enter a pool-wait loop checking the server state to see if it has entered one of the desired states or not.
     * <p>
     * This method checks the state of the server periodically for one of the desired states. When the server enters one
     * of the desired states, the method returns a successful indication (true). If the server never enters one of the
     * desired states within the allocated timeout period, then the method returns a failed response (false). No
     * exceptions are thrown from this method.
     * </p>
     *
     * @param rc
     *            The request context that manages the state and recovery of the request for the life of its processing.
     * @param server
     *            The server to wait on
     * @param desiredStates
     *            A variable list of desired states, any one of which is allowed.
     * @throws RequestFailedException
     *             If the request times out or fails for some reason
     */
    @SuppressWarnings("nls")
    private void waitForStateChange(RequestContext rc, Server server, Server.Status... desiredStates)
        throws RequestFailedException {
        int pollInterval = configuration.getIntegerProperty(Constants.PROPERTY_OPENSTACK_POLL_INTERVAL);
        int timeout = configuration.getIntegerProperty(Constants.PROPERTY_SERVER_STATE_CHANGE_TIMEOUT);
        Context context = server.getContext();
        Provider provider = context.getProvider();
        ComputeService service = context.getComputeService();
        String msg;

        long endTime = System.currentTimeMillis() + (timeout * 1000); //

        while (rc.attempt()) {
            try {
                try {
                    server.waitForStateChange(pollInterval, timeout, desiredStates);
                    break;
                } catch (TimeoutException e) {
                    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
                    List<String> list = new ArrayList<>();
                    for (Server.Status desiredState : desiredStates) {
                        list.add(desiredState.name());
                    }
                    msg = EELFResourceManager.format(Msg.CONNECTION_FAILED_RETRY, provider.getName(), service.getURL(),
                        context.getTenant().getName(), context.getTenant().getId(), e.getMessage(),
                        Long.toString(rc.getRetryDelay()), Integer.toString(rc.getAttempts()),
                        Integer.toString(rc.getRetryLimit()));
                    logger.error(msg, e);
                    rc.delay();
                }
            } catch (ZoneException e) {
                List<String> list = new ArrayList<>();
                for (Server.Status desiredState : desiredStates) {
                    list.add(desiredState.name());
                }
                String reason = EELFResourceManager.format(Msg.STATE_CHANGE_EXCEPTION, e.getClass().getSimpleName(),
                    "server", server.getName(), server.getId(), StringHelper.asList(list), server.getStatus().name(),
                    e.getMessage());
                logger.error(reason);
                logger.error(EELFResourceManager.format(e));

                // Instead of failing we are going to wait and try again.
                // Timeout is reduced by delay time
                logger.info(String.format("Retrying in %ds", rc.getRetryDelay()));
                rc.delay();
                timeout = (int) (endTime - System.currentTimeMillis()) / 1000;
                // throw new RequestFailedException(e, operation, reason,
                // HttpStatus.BAD_GATEWAY_502, server);
            }
        }

        if (rc.isFailed()) {
            msg = EELFResourceManager.format(Msg.CONNECTION_FAILED, provider.getName(), service.getURL());
            logger.error(msg);
            throw new RequestFailedException("Waiting for State Change", msg, HttpStatus.BAD_GATEWAY_502, server);
        }
        rc.reset();
    }

    /**
     * Enter a pool-wait loop checking the server state to see if it has entered one of the desired states or not.
     * <p>
     * This method checks the state of the server periodically for one of the desired states. When the server enters one
     * of the desired states, the method returns a successful indication (true). If the server never enters one of the
     * desired states within the allocated timeout period, then the method returns a failed response (false). No
     * exceptions are thrown from this method.
     * </p>
     *
     * @param rc
     *            The request context that manages the state and recovery of the request for the life of its processing.
     * @param image
     *            The server to wait on
     * @param desiredStates
     *            A variable list of desired states, any one of which is allowed.
     * @throws RequestFailedException
     *             If the request times out or fails for some reason
     * @throws NotLoggedInException
     */
    @SuppressWarnings("nls")
    private void waitForStateChange(RequestContext rc, Image image, Image.Status... desiredStates)
        throws RequestFailedException, NotLoggedInException {
        int pollInterval = configuration.getIntegerProperty(Constants.PROPERTY_OPENSTACK_POLL_INTERVAL);
        int timeout = configuration.getIntegerProperty(Constants.PROPERTY_SERVER_STATE_CHANGE_TIMEOUT);
        Context context = image.getContext();
        Provider provider = context.getProvider();
        ImageService service = context.getImageService();
        String msg;

        long endTime = System.currentTimeMillis() + (timeout * 1000); //

        while (rc.attempt()) {
            try {
                try {
                    image.waitForStateChange(pollInterval, timeout, desiredStates);
                    break;
                } catch (TimeoutException e) {
                    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
                    List<String> list = new ArrayList<>();
                    for (Image.Status desiredState : desiredStates) {
                        list.add(desiredState.name());
                    }
                    msg = EELFResourceManager.format(Msg.CONNECTION_FAILED_RETRY, provider.getName(), service.getURL(),
                        context.getTenant().getName(), context.getTenant().getId(), e.getMessage(),
                        Long.toString(rc.getRetryDelay()), Integer.toString(rc.getAttempts()),
                        Integer.toString(rc.getRetryLimit()));
                    logger.error(msg, e);
                    rc.delay();
                }
            } catch (ZoneException e) {
                List<String> list = new ArrayList<>();
                for (Image.Status desiredState : desiredStates) {
                    list.add(desiredState.name());
                }
                String reason = EELFResourceManager.format(Msg.STATE_CHANGE_EXCEPTION, e.getClass().getSimpleName(),
                    "server", image.getName(), image.getId(), StringHelper.asList(list), image.getStatus().name(),
                    e.getMessage());
                logger.error(reason);
                logger.error(EELFResourceManager.format(e));

                // Instead of failing we are going to wait and try again.
                // Timeout is reduced by delay time
                logger.info(String.format("Retrying in %ds", rc.getRetryDelay()));
                rc.delay();
                timeout = (int) (endTime - System.currentTimeMillis()) / 1000;
                // throw new RequestFailedException(e, operation, reason,
                // HttpStatus.BAD_GATEWAY_502, server);
            }
        }

        if (rc.isFailed()) {
            msg = EELFResourceManager.format(Msg.CONNECTION_FAILED, provider.getName(), service.getURL());
            logger.error(msg);
            throw new RequestFailedException("Waiting for State Change", msg, HttpStatus.BAD_GATEWAY_502, new Server());
        }
        rc.reset();
    }

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
        String msg;
        Context context = server.getContext();
        Provider provider = context.getProvider();
        ComputeService service = context.getComputeService();

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
            }
        } catch (ZoneException e) {
            msg =
                EELFResourceManager.format(Msg.REBUILD_SERVER_FAILED, server.getName(), server.getId(), e.getMessage());
            logger.error(msg);
            throw new RequestFailedException("Rebuild Server", msg, HttpStatus.BAD_GATEWAY_502, server);
        }

        /*
         * Once we have started the process, now we wait for the final state of stopped. This should be the final state
         * (since we started the rebuild with the server stopped).
         */
        waitForStateChange(rc, server, Status.READY);

        if (rc.isFailed()) {
            msg = EELFResourceManager.format(Msg.CONNECTION_FAILED, provider.getName(), service.getURL());
            logger.error(msg);
            throw new RequestFailedException("Rebuild Server", msg, HttpStatus.BAD_GATEWAY_502, server);
        }
        rc.reset();
    }

    /**
     * Looks up the indicated server using the provided context and returns the server to the caller
     *
     * @param rc
     *            The request context
     * @param context
     *            The provider context
     * @param id
     *            The id of the server
     * @return The server, or null if there is a problem
     * @throws ZoneException
     *             If the server cannot be found
     * @throws RequestFailedException
     *             If the server cannot be found because we cant connect to the provider
     */
    @SuppressWarnings("nls")
    private Server lookupServer(RequestContext rc, Context context, String id)
        throws ZoneException, RequestFailedException {
        ComputeService service = context.getComputeService();
        Server server = null;
        String msg;
        Provider provider = context.getProvider();

        while (rc.attempt()) {
            try {
                server = service.getServer(id);
                break;
            } catch (ContextConnectionException e) {
                msg = EELFResourceManager.format(Msg.CONNECTION_FAILED_RETRY, provider.getName(), service.getURL(),
                    context.getTenant().getName(), context.getTenant().getId(), e.getMessage(),
                    Long.toString(rc.getRetryDelay()), Integer.toString(rc.getAttempts()),
                    Integer.toString(rc.getRetryLimit()));
                logger.error(msg, e);
                rc.delay();
            }
        }
        if (rc.isFailed()) {
            msg = EELFResourceManager.format(Msg.CONNECTION_FAILED, provider.getName(), service.getURL());
            logger.error(msg);
            doFailure(rc, HttpStatus.BAD_GATEWAY_502, msg);
            throw new RequestFailedException("Lookup Server", msg, HttpStatus.BAD_GATEWAY_502, server);
        }
        return server;
    }

    private String getConnectionExceptionMessage(RequestContext rc, Context ctx, ContextConnectionException e)
        throws ZoneException {
        return EELFResourceManager.format(Msg.CONNECTION_FAILED_RETRY, ctx.getProvider().getName(),
            ctx.getComputeService().getURL(), ctx.getTenant().getName(), ctx.getTenant().getId(), e.getMessage(),
            Long.toString(rc.getRetryDelay()), Integer.toString(rc.getAttempts()),
            Integer.toString(rc.getRetryLimit()));
    }

    private ProviderCache createProviderCache(VMURL vm, IdentityURL ident) {
        if (vm != null && ident != null) {
            ProviderCache cache = new ProviderCache();

            cache.setIdentityURL(ident.toString());
            cache.setProviderName(ident.toString());
            // cache.setProviderType("OpenStack");

            TenantCache tenant = cache.addTenant(vm.getTenantId(),null, DEFAULT_USER, DEFAULT_PASS);

            // Make sure we could initialize the the cache otherwise return null
            if (tenant != null && tenant.isInitialized()) {
                return cache;
            }
        }
        return null;
    }

    /**
     * This method is used to delete an existing virtual machine given the fully qualified URL of the machine.
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
     * @see org.openecomp.appc.adapter.iaas.ProviderAdapter#terminateServer(java.util.Map, org.openecomp.sdnc.sli.SvcLogicContext)
     */
    @SuppressWarnings("nls")
    @Override
    public Server terminateServer(Map<String, String> params, SvcLogicContext ctx)
        throws UnknownProviderException, IllegalArgumentException {
        Server server = null;
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();
        MDC.put(MDC_ADAPTER, ADAPTER_NAME);
        MDC.put(MDC_SERVICE, TERMINATE_SERVICE);
        MDC.put(MDC_SERVICE_NAME, "App-C IaaS Adapter:Terminate");
        String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);
		if (logger.isDebugEnabled()) {
			logger.debug("Inside org.openecomp.appc.adapter.iaas.impl.ProviderAdapter.terminateServer");
		}

        try {
            validateParametersExist(rc, params, ProviderAdapter.PROPERTY_INSTANCE_URL,
                ProviderAdapter.PROPERTY_PROVIDER_NAME);
            debugParameters(params);
            debugContext(ctx);
            String vm_url = params.get(ProviderAdapter.PROPERTY_INSTANCE_URL);
            ctx.setAttribute("TERMINATE_STATUS", "SUCCESS");

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
                    logger.info(EELFResourceManager.format(Msg.TERMINATING_SERVER, server.getName()));
                    terminateServer(rc, server);
                    logger.info(EELFResourceManager.format(Msg.TERMINATE_SERVER, server.getName()));
                    context.close();
                    doSuccess(rc);
                }else{
                    ctx.setAttribute("TERMINATE_STATUS", "SERVER_NOT_FOUND");
                }
            } catch (ResourceNotFoundException e) {
                String msg = EELFResourceManager.format(Msg.SERVER_NOT_FOUND, e, vm_url);
                logger.error(msg);
                doFailure(rc, HttpStatus.NOT_FOUND_404, msg);
                ctx.setAttribute("TERMINATE_STATUS", "SERVER_NOT_FOUND");
            } catch (Throwable t) {
                String msg = EELFResourceManager.format(Msg.SERVER_OPERATION_EXCEPTION, t, t.getClass().getSimpleName(),
                    RESTART_SERVICE, vm_url, context == null ? "Unknown" : context.getTenantName());
                logger.error(msg, t);
                doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, msg);
            }
        } catch (RequestFailedException e) {
            logger.error(EELFResourceManager.format(Msg.TERMINATE_SERVER_FAILED, appName, "n/a", "n/a", e.getMessage()));
            doFailure(rc, e.getStatus(), e.getMessage());
            ctx.setAttribute("TERMINATE_STATUS", "ERROR");
        }

        return server;
    }

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
    private void terminateServer(RequestContext rc, Server server) throws ZoneException, RequestFailedException {
        /*
         * Pending is a bit of a special case. If we find the server is in a pending state, then the provider is in the
         * process of changing state of the server. So, lets try to wait a little bit and see if the state settles down
         * to one we can deal with. If not, then we have to fail the request.
         */
        String msg;
        if (server.getStatus().equals(Status.PENDING)) {
            waitForStateChange(rc, server, Status.READY, Status.RUNNING, Status.ERROR, Status.SUSPENDED, Status.PAUSED);
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
                break;

            case RUNNING:
                // Attempt to stop and start the server
                logger.info("stopping SERVER");
                stopServer(rc, server);
                deleteServer(rc, server);
                logger.info("after delete SERVER");
                generateEvent(rc, true, OUTCOME_SUCCESS);
                break;

            case ERROR:

            case READY:

            case PAUSED:

            case SUSPENDED:
                // Attempt to delete the suspended server
                deleteServer(rc, server);
                generateEvent(rc, true, OUTCOME_SUCCESS);
                break;

            default:
                // Hmmm, unknown status, should never occur
                msg = EELFResourceManager.format(Msg.UNKNOWN_SERVER_STATE, server.getName(), server.getId(),
                    server.getTenantId(), server.getStatus().name());
                generateEvent(rc, false, msg);
                logger.error(msg);
                break;
        }

    }

    /**
     * Start the server and wait for it to enter a running state
     *
     * @param rc
     *            The request context that manages the state and recovery of the request for the life of its processing.
     * @param server
     *            The server to be started
     * @throws ZoneException
     * @throws RequestFailedException
     */
    @SuppressWarnings("nls")
    private void deleteServer(RequestContext rc, Server server) throws ZoneException, RequestFailedException {
        String msg;
        Context context = server.getContext();
        Provider provider = context.getProvider();
        ComputeService service = context.getComputeService();
        while (rc.attempt()) {
            try {
                logger.info("deleting SERVER");
                server.delete();
                break;
            } catch (ContextConnectionException e) {
                msg = EELFResourceManager.format(Msg.CONNECTION_FAILED_RETRY, provider.getName(), service.getURL(),
                    context.getTenant().getName(), context.getTenant().getId(), e.getMessage(),
                    Long.toString(rc.getRetryDelay()), Integer.toString(rc.getAttempts()),
                    Integer.toString(rc.getRetryLimit()));
                logger.error(msg, e);
                rc.delay();
            }
        }
        if (rc.isFailed()) {
            msg = EELFResourceManager.format(Msg.CONNECTION_FAILED, provider.getName(), service.getURL());
            logger.error(msg);
            throw new RequestFailedException("Delete Server", msg, HttpStatus.BAD_GATEWAY_502, server);
        }
        rc.reset();
    }

    private boolean hasImageAccess(@SuppressWarnings("unused") RequestContext rc, Context context) {
        logger.info("Checking permissions for image service.");
        try {
            ImageService service = context.getImageService();
            service.getImageByName("CHECK_IMAGE_ACCESS");
            logger.info("Image service is accessible.");
            return true;
        } catch (ZoneException e) {
            logger.warn("Image service could not be accessed. Some operations may fail.", e);
            return false;
        }
    }

    @SuppressWarnings("nls")
    @Override
    public Stack terminateStack(Map<String, String> params, SvcLogicContext ctx) throws IllegalArgumentException, APPCException {
        Stack stack = null;
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();

        ctx.setAttribute("TERMINATE_STATUS", "STACK_NOT_FOUND");
        String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);

        try {

            logAndValidate(params, ctx, rc, TERMINATE_STACK, "Terminate Stack",
                            ProviderAdapter.PROPERTY_INSTANCE_URL,
                            ProviderAdapter.PROPERTY_PROVIDER_NAME,
                            ProviderAdapter.PROPERTY_STACK_ID);

            String stackId = params.get(ProviderAdapter.PROPERTY_STACK_ID);
            String vm_url = params.get(ProviderAdapter.PROPERTY_INSTANCE_URL);

            Context context = resolveContext(rc, params, appName, vm_url);

            try {
                if (context != null) {
                    stack = lookupStack(rc, context, stackId);
                    logger.debug(Msg.STACK_FOUND, vm_url, context.getTenantName(), stack.getStatus().toString());
                    logger.info(EELFResourceManager.format(Msg.TERMINATING_STACK, stack.getName()));
                    deleteStack(rc, stack);
                    logger.info(EELFResourceManager.format(Msg.TERMINATE_STACK, stack.getName()));
                    context.close();
                    doSuccess(rc);
                }else{
                    ctx.setAttribute("TERMINATE_STATUS", "SERVER_NOT_FOUND");
                }
            } catch (ResourceNotFoundException e) {
                String msg = EELFResourceManager.format(Msg.STACK_NOT_FOUND, e, vm_url);
                logger.error(msg);
                doFailure(rc, HttpStatus.NOT_FOUND_404, msg);
            } catch (Throwable t) {
                String msg = EELFResourceManager.format(Msg.STACK_OPERATION_EXCEPTION, t, t.getClass().getSimpleName(),
                    TERMINATE_STACK, vm_url, context.getTenantName());
                logger.error(msg, t);
                doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, msg);
            }
        } catch (RequestFailedException e) {
            logger.error(EELFResourceManager.format(Msg.TERMINATE_STACK_FAILED, appName, "n/a", "n/a"));
            doFailure(rc, e.getStatus(), e.getMessage());
        }
        return stack;
    }

    @Override
    public Stack snapshotStack(Map<String, String> params, SvcLogicContext ctx) throws IllegalArgumentException, APPCException {
        Stack stack = null;
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();

        ctx.setAttribute("SNAPSHOT_STATUS", "STACK_NOT_FOUND");
        String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);

        String vm_url = null;
        Context context = null;
        try {

            logAndValidate(params, ctx, rc, SNAPSHOT_STACK, "Snapshot Stack",
                            ProviderAdapter.PROPERTY_INSTANCE_URL,
                            ProviderAdapter.PROPERTY_PROVIDER_NAME,
                            ProviderAdapter.PROPERTY_STACK_ID);

            String stackId = params.get(ProviderAdapter.PROPERTY_STACK_ID);
            vm_url = params.get(ProviderAdapter.PROPERTY_INSTANCE_URL);

            context = resolveContext(rc, params, appName, vm_url);

            if (context != null) {
                stack = lookupStack(rc, context, stackId);
                logger.debug(Msg.STACK_FOUND, vm_url, context.getTenantName(), stack.getStatus().toString());
                logger.info(EELFResourceManager.format(Msg.SNAPSHOTING_STACK, stack.getName()));

                Snapshot snapshot = snapshotStack(rc, stack);

                ctx.setAttribute(ProviderAdapter.DG_OUTPUT_PARAM_NAMESPACE +
                                ProviderAdapter.PROPERTY_SNAPSHOT_ID, snapshot.getId());

                logger.info(EELFResourceManager.format(Msg.STACK_SNAPSHOTED, stack.getName(), snapshot.getId()));
                context.close();
                doSuccess(rc);
            } else {
                ctx.setAttribute(Constants.DG_ATTRIBUTE_STATUS, "failure");
            }

        } catch (ResourceNotFoundException e) {
            String msg = EELFResourceManager.format(Msg.STACK_NOT_FOUND, e, vm_url);
            logger.error(msg);
            doFailure(rc, HttpStatus.NOT_FOUND_404, msg, e);
        } catch (RequestFailedException e) {
            logger.error(EELFResourceManager.format(Msg.MISSING_PARAMETER_IN_REQUEST, e.getReason(), "snapshotStack"));
            doFailure(rc, e.getStatus(), e.getMessage(), e);
        } catch (Throwable t) {
            String msg = EELFResourceManager.format(Msg.STACK_OPERATION_EXCEPTION, t, t.getClass().getSimpleName(),
                            "snapshotStack", vm_url, null == context ? "n/a" : context.getTenantName());
            logger.error(msg, t);
            doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, msg, t);
        }
        return stack;
    }

    @Override
    public Stack restoreStack(Map<String, String> params, SvcLogicContext ctx) throws IllegalArgumentException, APPCException {
        Stack stack = null;
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();

        ctx.setAttribute("SNAPSHOT_STATUS", "STACK_NOT_FOUND");
        String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);

        String vm_url = null;
        Context context = null;

        try {

            logAndValidate(params, ctx, rc, SNAPSHOT_STACK, "Snapshot Stack",
                            ProviderAdapter.PROPERTY_INSTANCE_URL,
                            ProviderAdapter.PROPERTY_PROVIDER_NAME,
                            ProviderAdapter.PROPERTY_STACK_ID,
                            ProviderAdapter.PROPERTY_INPUT_SNAPSHOT_ID);

            String stackId = params.get(ProviderAdapter.PROPERTY_STACK_ID);
            vm_url = params.get(ProviderAdapter.PROPERTY_INSTANCE_URL);

            String snapshotId = params.get(ProviderAdapter.PROPERTY_INPUT_SNAPSHOT_ID);

            context = resolveContext(rc, params, appName, vm_url);

            if (context != null) {
                stack = lookupStack(rc, context, stackId);
                logger.debug(Msg.STACK_FOUND, vm_url, context.getTenantName(), stack.getStatus().toString());
                logger.info(EELFResourceManager.format(Msg.RESTORING_STACK, stack.getName(), snapshotId));
                restoreStack(stack, snapshotId);
                logger.info(EELFResourceManager.format(Msg.STACK_RESTORED, stack.getName(), snapshotId));
                context.close();
                doSuccess(rc);
            } else {
                ctx.setAttribute(Constants.DG_ATTRIBUTE_STATUS, "failure");
            }

        } catch (ResourceNotFoundException e) {
            String msg = EELFResourceManager.format(Msg.STACK_NOT_FOUND, e, vm_url);
            logger.error(msg);
            doFailure(rc, HttpStatus.NOT_FOUND_404, msg, e);
        } catch (RequestFailedException e) {
            logger.error(EELFResourceManager.format(Msg.MISSING_PARAMETER_IN_REQUEST, e.getReason(), "restoreStack"));
            doFailure(rc, e.getStatus(), e.getMessage(), e);
        } catch (Throwable t) {
            String msg = EELFResourceManager.format(Msg.STACK_OPERATION_EXCEPTION, t, t.getClass().getSimpleName(),
                            "restoreStack", vm_url, null == context ? "n/a" : context.getTenantName());
            logger.error(msg, t);
            doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, msg, t);
        }
        return stack;
    }

    private void logAndValidate(Map<String, String> params, SvcLogicContext ctx, RequestContext rc, String methodName, String serviceName, String ... attributes)
                    throws RequestFailedException {
        MDC.put(MDC_ADAPTER, ADAPTER_NAME);
        MDC.put(MDC_SERVICE, SNAPSHOT_STACK);
        MDC.put(MDC_SERVICE_NAME, String.format("App-C IaaS Adapter:%s", serviceName));
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Inside org.openecomp.appc.adapter.iaas.impl.ProviderAdapter.%s", methodName));
        }

        validateParametersExist(rc, params, attributes);

        debugParameters(params);
        debugContext(ctx);
    }

    private Context resolveContext(RequestContext rc, Map<String, String> params, String appName, String vm_url)
                    throws RequestFailedException {

        VMURL vm = VMURL.parseURL(vm_url);
        if (vm == null) {
            String msg = EELFResourceManager.format(Msg.INVALID_SELF_LINK_URL, appName, vm_url);
            doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, msg);
            logger.error(msg);
            return null;
        }
        validateVMURL(vm);
        IdentityURL ident = IdentityURL.parseURL(params.get(ProviderAdapter.PROPERTY_IDENTITY_URL));
        String identStr = (ident == null) ? null : ident.toString();

        return getContext(rc, vm_url, identStr);

    }

    private void deleteStack(RequestContext rc, Stack stack) throws ZoneException, RequestFailedException {
        SvcLogicContext ctx = rc.getSvcLogicContext();
        Context context = stack.getContext();
        StackService stackService = context.getStackService();
        logger.debug("Deleting Stack: " + "id:{ " + stack.getId() + "}");
        stackService.deleteStack(stack);

        // wait for the stack deletion
        boolean success = waitForStackStatus(rc, stack, Stack.Status.DELETED);
        if (success) {
            ctx.setAttribute("TERMINATE_STATUS", "SUCCESS");
        } else {
            ctx.setAttribute("TERMINATE_STATUS", "ERROR");
            throw new RequestFailedException("Delete Stack failure : " + Msg.STACK_OPERATION_EXCEPTION.toString());
        }
    }

    private boolean waitForStackStatus(RequestContext rc, Stack stack, Stack.Status expectedStatus) throws ZoneException, RequestFailedException {
        SvcLogicContext ctx = rc.getSvcLogicContext();
        Context context = stack.getContext();
        StackService stackService = context.getStackService();

        int pollInterval = configuration.getIntegerProperty(Constants.PROPERTY_OPENSTACK_POLL_INTERVAL);
        int timeout = configuration.getIntegerProperty(Constants.PROPERTY_STACK_STATE_CHANGE_TIMEOUT);
        long maxTimeToWait = System.currentTimeMillis() + (long) timeout * 1000;
        Stack.Status stackStatus;
        while (System.currentTimeMillis() < maxTimeToWait) {
            stackStatus = stackService.getStack(stack.getName(), stack.getId()).getStatus();
            logger.debug("Stack status : " + stackStatus.toString());
            if (stackStatus == expectedStatus) {
                return true;
            } else if (stackStatus == Stack.Status.FAILED) {
                return false;
            } else {
                try {
                    Thread.sleep(pollInterval * 1000);
                } catch (InterruptedException e) {
                    logger.trace("Sleep threw interrupted exception, should never occur");
                }
            }
        }

        ctx.setAttribute("TERMINATE_STATUS", "ERROR");
        throw new TimeoutException("Timeout waiting for stack status change");

    }

    private Snapshot snapshotStack(@SuppressWarnings("unused") RequestContext rc, Stack stack) throws ZoneException, RequestFailedException {
        Snapshot snapshot = new Snapshot();
        Context context = stack.getContext();

        OpenStackContext osContext = (OpenStackContext)context;

        final HeatConnector heatConnector = osContext.getHeatConnector();
        ((OpenStackContext)context).refreshIfStale(heatConnector);

        trackRequest(context);
        RequestState.put("SERVICE", "Orchestration");
        RequestState.put("SERVICE_URL", heatConnector.getEndpoint());

        Heat heat = heatConnector.getClient();

        SnapshotResource snapshotResource = new SnapshotResource(heat);

        try {

            snapshot = snapshotResource.create(stack.getName(), stack.getId(), new CreateSnapshotParams()).execute();

            // wait for the stack deletion
            StackResource stackResource = new StackResource(heat);
            if (!waitForStack(stack, stackResource, "SNAPSHOT_COMPLETE")) {
                throw new RequestFailedException("Stack Snapshot failed.");
            }

        } catch (OpenStackBaseException e) {
            ExceptionMapper.mapException(e);
        }

        return snapshot;
    }

    private void restoreStack(Stack stack, String snapshotId) throws ZoneException, RequestFailedException {
        Context context = stack.getContext();

        OpenStackContext osContext = (OpenStackContext)context;

        final HeatConnector heatConnector = osContext.getHeatConnector();
        ((OpenStackContext)context).refreshIfStale(heatConnector);

        trackRequest(context);
        RequestState.put("SERVICE", "Orchestration");
        RequestState.put("SERVICE_URL", heatConnector.getEndpoint());

        Heat heat = heatConnector.getClient();

        SnapshotResource snapshotResource = new SnapshotResource(heat);

        try {

            snapshotResource.restore(stack.getName(), stack.getId(), snapshotId).execute();

            // wait for the snapshot restore
            StackResource stackResource = new StackResource(heat);
            if (!waitForStack(stack, stackResource, "RESTORE_COMPLETE")) {
                throw new RequestFailedException("Snapshot restore failed.");
            }

        } catch (OpenStackBaseException e) {
            ExceptionMapper.mapException(e);
        }

    }

    private boolean waitForStack(Stack stack, StackResource stackResource, String expectedStatus)
                    throws OpenStackBaseException, TimeoutException {
        int pollInterval = configuration.getIntegerProperty(Constants.PROPERTY_OPENSTACK_POLL_INTERVAL);
        int timeout = configuration.getIntegerProperty(Constants.PROPERTY_STACK_STATE_CHANGE_TIMEOUT);
        long maxTimeToWait = System.currentTimeMillis() + (long) timeout * 1000;

        while (System.currentTimeMillis() < maxTimeToWait) {
            String stackStatus = stackResource.show(stack.getName(), stack.getId()).execute().getStackStatus();
            logger.debug("Stack status : " + stackStatus);
            if (stackStatus.toUpperCase().contains("FAILED")) return false;
            if(checkStatus(expectedStatus, pollInterval, stackStatus)) return true;
        }
        throw new TimeoutException("Timeout waiting for stack status change");
    }

    private boolean checkStatus(String expectedStatus, int pollInterval, String actualStatus) {
        if (actualStatus.toUpperCase().equals(expectedStatus)) {
            return true;
        } else {
            try {
                Thread.sleep(pollInterval * 1000);
            } catch (InterruptedException ignored) {
            }
        }
        return false;
    }

    private void trackRequest(Context context, AbstractService.State... states) {
        RequestState.clear();

        if (null == states) return;
        for (AbstractService.State state : states) {
            RequestState.put(state.getName(), state.getValue());
        }

        Thread currentThread = Thread.currentThread();
        StackTraceElement[] stack = currentThread.getStackTrace();
        if (stack != null && stack.length > 0) {
            int index = 0;
            StackTraceElement element;
            for (; index < stack.length; index++) {
                element = stack[index];
                if ("trackRequest".equals(element.getMethodName())) {  //$NON-NLS-1$
                    break;
                }
            }
            index++;

            if (index < stack.length) {
                element = stack[index];
                RequestState.put(RequestState.METHOD, element.getMethodName());
                RequestState.put(RequestState.CLASS, element.getClassName());
                RequestState.put(RequestState.LINE_NUMBER, Integer.toString(element.getLineNumber()));
                RequestState.put(RequestState.THREAD, currentThread.getName());
                RequestState.put(RequestState.PROVIDER, context.getProvider().getName());
                RequestState.put(RequestState.TENANT, context.getTenantName());
                RequestState.put(RequestState.PRINCIPAL, context.getPrincipal());
            }
        }
    }

    private Stack lookupStack(RequestContext rc, Context context, String id)
        throws ZoneException, RequestFailedException {
        StackService stackService = context.getStackService();
        Stack stack = null;
        String msg;
        Provider provider = context.getProvider();
        while (rc.attempt()) {
            try {
                List<Stack> stackList = stackService.getStacks();
                for (Stack stackObj : stackList) {
                    if (stackObj.getId().equals(id)) {
                        stack = stackObj;
                        break;
                    }
                }
                break;
            } catch (ContextConnectionException e) {
                msg = EELFResourceManager.format(Msg.CONNECTION_FAILED_RETRY, provider.getName(), stackService.getURL(),
                    context.getTenant().getName(), context.getTenant().getId(), e.getMessage(),
                    Long.toString(rc.getRetryDelay()), Integer.toString(rc.getAttempts()),
                    Integer.toString(rc.getRetryLimit()));
                logger.error(msg, e);
                rc.delay();
            }

        }
        if (rc.isFailed()) {
            msg = EELFResourceManager.format(Msg.CONNECTION_FAILED, provider.getName(), stackService.getURL());
            logger.error(msg);
            doFailure(rc, HttpStatus.BAD_GATEWAY_502, msg);
            throw new RequestFailedException("Lookup Stack", msg, HttpStatus.BAD_GATEWAY_502, stack);
        }

        if (stack == null) {
            throw new ResourceNotFoundException("Stack not found with Id : {" + id + "}");
        }
        return stack;
    }

    @SuppressWarnings("nls")
    @Override
    public Server lookupServer(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        Server server = null;
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive(); //should we test the return and fail if false?
        MDC.put(MDC_ADAPTER, ADAPTER_NAME);
        MDC.put(MDC_SERVICE, LOOKUP_SERVICE);
        MDC.put(MDC_SERVICE_NAME, "App-C IaaS Adapter:LookupServer");
        String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);

        //for debugging merge into single method?
        debugParameters(params);
        debugContext(ctx);

        String vm_url = null;
        VMURL vm = null;
        try {

            //process vm_url
            validateParametersExist(rc, params, ProviderAdapter.PROPERTY_INSTANCE_URL,
                ProviderAdapter.PROPERTY_PROVIDER_NAME);
            vm_url = params.get(ProviderAdapter.PROPERTY_INSTANCE_URL);
            vm = VMURL.parseURL(vm_url);
            if (validateVM(rc, appName, vm_url, vm)) return null;


            //use try with resource to ensure context is closed (returned to pool)
            try(Context context = resolveContext(rc, params, appName, vm_url)){
              //resloveContext & getContext call doFailure and log errors before returning null
                if (context != null){
                    server = lookupServer(rc, context, vm.getServerId());
                    logger.debug(Msg.SERVER_FOUND, vm_url, context.getTenantName(), server.getStatus().toString());
                    ctx.setAttribute("serverFound", "success");
                    doSuccess(rc);
                }
            } catch (ZoneException e) {
                //server not found
                String msg = EELFResourceManager.format(Msg.SERVER_NOT_FOUND, e, vm_url);
                logger.error(msg);
                doFailure(rc, HttpStatus.NOT_FOUND_404, msg);
                ctx.setAttribute("serverFound", "failure");
            }  catch (IOException e) {
                //exception closing context
                String msg = EELFResourceManager.format(Msg.CLOSE_CONTEXT_FAILED, e, vm_url);
                logger.error(msg);
            } catch (Throwable t) {
                String msg = EELFResourceManager.format(Msg.SERVER_OPERATION_EXCEPTION, t, t.getClass().getSimpleName(),
                    LOOKUP_SERVICE, vm_url,  "Unknown" );
                logger.error(msg, t);
                doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, msg);
            }

        } catch (RequestFailedException e) {
            // parameters not valid, unable to connect to provider
            String msg = EELFResourceManager.format(Msg.SERVER_NOT_FOUND, e, vm_url);
            logger.error(msg);
            doFailure(rc, HttpStatus.NOT_FOUND_404, msg);
            ctx.setAttribute("serverFound", "failure");
        }
        return server;
    }
}
