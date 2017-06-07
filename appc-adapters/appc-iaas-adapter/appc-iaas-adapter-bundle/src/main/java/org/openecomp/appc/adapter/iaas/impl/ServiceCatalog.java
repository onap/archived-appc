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

import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.att.cdp.exceptions.ContextConnectionException;
import com.att.cdp.exceptions.ZoneException;
import com.att.cdp.openstack.util.ExceptionMapper;
import com.att.cdp.pal.util.Time;
import com.att.cdp.zones.ContextFactory;
import com.att.cdp.zones.spi.AbstractService;
import com.att.cdp.zones.spi.RequestState;
import com.att.cdp.zones.spi.AbstractService.State;

import com.woorea.openstack.base.client.OpenStackBaseException;
import com.woorea.openstack.base.client.OpenStackClientConnector;
import com.woorea.openstack.base.client.OpenStackResponseException;
import com.woorea.openstack.base.client.OpenStackSimpleTokenProvider;
import com.woorea.openstack.keystone.Keystone;
import com.woorea.openstack.keystone.api.TokensResource;
import com.woorea.openstack.keystone.model.Access;
import com.woorea.openstack.keystone.model.Access.Service;
import com.woorea.openstack.keystone.model.Access.Service.Endpoint;
import com.woorea.openstack.keystone.model.Authentication;
import com.woorea.openstack.keystone.model.Tenant;
import com.woorea.openstack.keystone.model.authentication.UsernamePassword;

/**
 * This class is used to capture and cache the service catalog for a specific OpenStack provider.
 * <p>
 * This is needed because the way the servers are represented in the ECOMP product is as their fully qualified URL's.
 * This is very problematic, because we cant identify their region from the URL, URL's change, and we cant identify the
 * versions of the service implementations. In otherwords, the URL does not provide us enough information.
 * </p>
 * <p>
 * The zone abstraction layer is designed to detect the versions of the services dynamically, and step up or down to
 * match those reported versions. In order to do that, we need to know before hand what region we are accessing (since
 * the supported versions may be different by regions). We will need to authenticate to the identity service in order to
 * do this, plus we have to duplicate the code supporting proxies and trusted hosts that exists in the abstraction
 * layer, but that cant be helped.
 * </p>
 * <p>
 * What we do to circumvent this is connect to the provider using the lowest supported identity api, and read the entire
 * service catalog into this object. Then, we parse the vm URL to extract the host and port and match that to the
 * compute services defined in the catalog. When we find a compute service that has the same host name and port,
 * whatever region that service is supporting is the region for that server.
 * </p>
 * <p>
 * While we really only need to do this for compute nodes, there is no telling what other situations may arise where the
 * full service catalog may be needed. Also, there is very little additional cost (additional RAM) associated with
 * caching the full service catalog since there is no way to list only a portion of it.
 * </p>
 */
public class ServiceCatalog {

    /**
     * The service name for the compute service endpoint
     */
    public static final String COMPUTE_SERVICE = "compute"; //$NON-NLS-1$

    /**
     * The service name for the identity service endpoint
     */
    public static final String IDENTITY_SERVICE = "identity"; //$NON-NLS-1$

    /**
     * The service name for the compute service endpoint
     */
    public static final String IMAGE_SERVICE = "image"; //$NON-NLS-1$

    /**
     * The service name for the network service endpoint
     */
    public static final String NETWORK_SERVICE = "network"; //$NON-NLS-1$

    /**
     * The service name for the orchestration service endpoint
     */
    public static final String ORCHESTRATION_SERVICE = "orchestration"; //$NON-NLS-1$

    /**
     * The service name for the volume service endpoint
     */
    public static final String VOLUME_SERVICE = "volume"; //$NON-NLS-1$

    /**
     * The service name for the persistent object service endpoint
     */
    public static final String OBJECT_SERVICE = "object-store"; //$NON-NLS-1$

    /**
     * The service name for the metering service endpoint
     */
    public static final String METERING_SERVICE = "metering"; //$NON-NLS-1$

    /**
     * The Openstack Access object that manages the authenticated token and access control
     */
    private Access access;

    /**
     * The time (local) that the token expires and we need to re-authenticate
     */
    @SuppressWarnings("unused")
    private long expiresLocal;

    /**
     * The set of all regions that have been defined
     */
    private Set<String> regions;

    /**
     * The read/write lock used to protect the cache contents
     */
    private ReadWriteLock rwLock;

    /**
     * A map of endpoints for each service organized by service type
     */
    private Map<String /* Service Type */, List<Service.Endpoint>> serviceEndpoints;

    /**
     * A map of service types that are published
     */
    private Map<String /* Service Type */, Service> serviceTypes;

    /**
     * The tenant that we are accessing
     */
    private Tenant tenant;

    /**
     * A "token provider" that manages the authentication token that we obtain when logging in
     */
    private OpenStackSimpleTokenProvider tokenProvider;

    public static final String CLIENT_CONNECTOR_CLASS = "com.woorea.openstack.connector.JaxRs20Connector";

    /**
     * Create the ServiceCatalog cache and load it from the specified provider
     * 
     * @param identityURL
     *            The identity service URL to connect to
     * @param tenantIdentifier
     *            The name or id of the tenant to authenticate with. If the ID is a UUID format (32-character
     *            hexadecimal string), then the authentication is done using the tenant ID, otherwise it is done using
     *            the name.
     * @param principal
     *            The user id to authenticate to the provider
     * @param credential
     *            The password to authenticate to the provider
     * @param properties
     *            Additional properties used to configure the connection, such as proxy and trusted hosts lists
     * @throws ZoneException
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public ServiceCatalog(String identityURL, String tenantIdentifier, String principal, String credential,
                          Properties properties) throws ZoneException {
        rwLock = new ReentrantReadWriteLock();
        serviceTypes = new HashMap<>();
        serviceEndpoints = new HashMap<>();
        regions = new HashSet<>();

        Class<?> connectorClass;
        OpenStackClientConnector connector;
        try {
            connectorClass = Class.forName(CLIENT_CONNECTOR_CLASS);
            connector = (OpenStackClientConnector) connectorClass.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return;
        }
        Keystone keystone = new Keystone(identityURL, connector);

        String proxyHost = properties.getProperty(ContextFactory.PROPERTY_PROXY_HOST);
        String proxyPort = properties.getProperty(ContextFactory.PROPERTY_PROXY_PORT);
        String trustedHosts = properties.getProperty(ContextFactory.PROPERTY_TRUSTED_HOSTS, ""); //$NON-NLS-1$
        if (proxyHost != null && proxyHost.length() > 0) {
            keystone.getProperties().setProperty(com.woorea.openstack.common.client.Constants.PROXY_HOST, proxyHost);
            keystone.getProperties().setProperty(com.woorea.openstack.common.client.Constants.PROXY_PORT, proxyPort);
        }
        if (trustedHosts != null) {
            keystone.getProperties().setProperty(com.woorea.openstack.common.client.Constants.TRUST_HOST_LIST,
                trustedHosts);
        }

        Authentication authentication = new UsernamePassword(principal, credential);
        TokensResource tokens = keystone.tokens();
        TokensResource.Authenticate authenticate = tokens.authenticate(authentication);
        if (tenantIdentifier.length() == 32 && tenantIdentifier.matches("[0-9a-fA-F]+")) { //$NON-NLS-1$
            authenticate = authenticate.withTenantId(tenantIdentifier);
        } else {
            authenticate = authenticate.withTenantName(tenantIdentifier);
        }

        /*
         * We have to set up the TrackRequest TLS collection for the ExceptionMapper
         */
        trackRequest();
        RequestState.put(RequestState.PROVIDER, "OpenStackProvider");
        RequestState.put(RequestState.TENANT, tenantIdentifier);
        RequestState.put(RequestState.PRINCIPAL, principal);

        try {
            access = authenticate.execute();
            expiresLocal = getLocalExpiration(access);
            tenant = access.getToken().getTenant();
            tokenProvider = new OpenStackSimpleTokenProvider(access.getToken().getId());
            keystone.setTokenProvider(tokenProvider);
            parseServiceCatalog(access.getServiceCatalog());
        } catch (OpenStackBaseException e) {
            ExceptionMapper.mapException(e);
        } catch (Exception ex) {
            throw new ContextConnectionException(ex.getMessage());
        }
    }

    /**
     * Returns the list of service endpoints for the published service type
     * 
     * @param serviceType
     *            The service type to obtain the endpoints for
     * @return The list of endpoints for the service type, or null if none exist
     */
    public List<Service.Endpoint> getEndpoints(String serviceType) {
        Lock readLock = rwLock.readLock();
        readLock.lock();
        try {
            return serviceEndpoints.get(serviceType);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Computes the local time when the access token will expire, after which we will need to re-login to access the
     * provider.
     * 
     * @param accessKey
     *            The access key used to access the provider
     * @return The local time the key expires
     */
    private static long getLocalExpiration(Access accessKey) {
        Date now = Time.getCurrentUTCDate();
        if (accessKey != null && accessKey.getToken() != null) {
            Calendar issued = accessKey.getToken().getIssued_at();
            Calendar expires = accessKey.getToken().getExpires();
            if (issued != null && expires != null) {
                long tokenLife = expires.getTimeInMillis() - issued.getTimeInMillis();
                return now.getTime() + tokenLife;
            }
        }
        return now.getTime();
    }

    /**
     * @return The set of all regions that are defined
     */
    public Set<String> getRegions() {
        Lock readLock = rwLock.readLock();
        readLock.lock();
        try {
            return regions;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * @return A list of service types that are published
     */
    public List<String> getServiceTypes() {
        Lock readLock = rwLock.readLock();
        readLock.lock();
        try {
            ArrayList<String> result = new ArrayList<>();
            result.addAll(serviceTypes.keySet());
            return result;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * @return The tenant id
     */
    public String getTenantId() {
        Lock readLock = rwLock.readLock();
        readLock.lock();
        try {
            return tenant.getId();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * @return The tenant name
     */
    public String getTenantName() {
        Lock readLock = rwLock.readLock();
        readLock.lock();
        try {
            return tenant.getName();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Returns an indication if the specified service type is published by this provider
     * 
     * @param serviceType
     *            The service type to check for
     * @return True if a service of that type is published
     */
    public boolean isServicePublished(String serviceType) {
        Lock readLock = rwLock.readLock();
        readLock.lock();
        try {
            return serviceTypes.containsKey(serviceType);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Parses the service catalog and caches the results
     * 
     * @param services
     *            The list of services published by this provider
     */
    private void parseServiceCatalog(List<Service> services) {
        Lock lock = rwLock.writeLock();
        lock.lock();
        try {
            serviceTypes.clear();
            serviceEndpoints.clear();
            regions.clear();

            for (Service service : services) {
                String type = service.getType();
                serviceTypes.put(type, service);

                List<Service.Endpoint> endpoints = service.getEndpoints();
                for (Service.Endpoint endpoint : endpoints) {
                    List<Service.Endpoint> endpointList = serviceEndpoints.get(type);
                    if (endpointList == null) {
                        endpointList = new ArrayList<>();
                        serviceEndpoints.put(type, endpointList);
                    }
                    endpointList.add(endpoint);

                    String region = endpoint.getRegion();
                    if (!regions.contains(region)) {
                        regions.add(region);
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * This method is used to provide a diagnostic listing of the service catalog
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        Lock lock = rwLock.readLock();
        lock.lock();
        try {
            builder.append(String.format("Service Catalog: tenant %s, id[%s], description[%s]\n", tenant.getName(), //$NON-NLS-1$
                tenant.getId(), tenant.getDescription()));
            if (regions != null && !regions.isEmpty()) {
                builder.append(String.format("%d regions:\n", regions.size())); //$NON-NLS-1$
                for (String region : regions) {
                    builder.append("\t" + region + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
            builder.append(String.format("%d services:\n", serviceEndpoints.size())); //$NON-NLS-1$
            for (String serviceType : serviceEndpoints.keySet()) {
                List<Endpoint> endpoints = serviceEndpoints.get(serviceType);
                Service service = serviceTypes.get(serviceType);

                builder.append(String.format("\t%s [%s] - %d endpoints\n", service.getType(), service.getName(), //$NON-NLS-1$
                    endpoints.size()));
                for (Endpoint endpoint : endpoints) {
                    builder.append(String.format("\t\tRegion [%s], public URL [%s]\n", endpoint.getRegion(), //$NON-NLS-1$
                        endpoint.getPublicURL()));
                }
            }
        } finally {
            lock.unlock();
        }

        return builder.toString();
    }

    /**
     * Initializes the request state for the current requested service.
     * <p>
     * This method is used to track requests made to the various service implementations and to provide additional
     * information for diagnostic purposes. The <code>RequestState</code> class stores the state in thread-local storage
     * and is available to all code on that thread.
     * </p>
     * <p>
     * This method first obtains the stack trace and scans the stack backward for the call to this method. It then backs
     * up one more call and assumes that method is the request that we are "tracking".
     * </p>
     * 
     * @param states
     *            A variable argument list of additional state values that the caller wants to add to the request state
     *            thread-local object to track the context.
     */
    protected void trackRequest(State... states) {
        RequestState.clear();

        for (State state : states) {
            RequestState.put(state.getName(), state.getValue());
        }

        Thread currentThread = Thread.currentThread();
        StackTraceElement[] stack = currentThread.getStackTrace();
        if (stack != null && stack.length > 0) {
            int index = 0;
            StackTraceElement element = null;
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
                // RequestState.put(RequestState.PROVIDER, context.getProvider().getName());
                // RequestState.put(RequestState.TENANT, context.getTenantName());
                // RequestState.put(RequestState.PRINCIPAL, context.getPrincipal());
            }
        }
    }
}
