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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openecomp.appc.Constants;
import org.openecomp.appc.configuration.Configuration;
import org.openecomp.appc.configuration.ConfigurationFactory;
import org.openecomp.appc.i18n.Msg;
import org.openecomp.appc.pool.Allocator;
import org.openecomp.appc.pool.Destructor;
import org.openecomp.appc.pool.Pool;
import org.openecomp.appc.pool.PoolSpecificationException;
import com.att.cdp.exceptions.ContextConnectionException;
import com.att.cdp.exceptions.ZoneException;
import com.att.cdp.zones.Context;
import com.att.cdp.zones.ContextFactory;
import com.att.cdp.zones.Provider;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.woorea.openstack.connector.JaxRs20Connector;
//import com.sun.jersey.api.client.ClientHandlerException;
import com.woorea.openstack.keystone.model.Access.Service.Endpoint;

/**
 * This class maintains a cache of tenants within a specific provider.
 * <p>
 * Providers may be multi-tenant, such as OpenStack, where the available services and resources vary from one tenant to
 * another. Therefore, the provider cache maintains a cache of tenants and the service catalogs for each, as well as the
 * credentials used to access the tenants, and a pool of Context objects for each tenant. The context pool allows use of
 * the CDP abstraction layer to access the services of the provider within the specific tenant.
 * </p>
 */
public class TenantCache implements Allocator<Context>, Destructor<Context> {

    public static final String POOL_PROVIDER_NAME = "pool.provider.name";
    public static final String POOL_TENANT_NAME = "pool.tenant.name";
    //public static final String CLIENT_CONNECTOR_CLASS = "com.woorea.openstack.connector.JerseyConnector";
    public static final String CLIENT_CONNECTOR_CLASS = "com.woorea.openstack.connector.JaxRs20Connector";
    /**
     * The provider we are part of
     */
    private ProviderCache provider;

    /**
     * The password used to authenticate
     */
    private String password;

    /**
     * The context pools by region used to access this tenant
     */
    private Map<String /* region */, Pool<Context>> pools = new HashMap<>();

    /**
     * The tenant id
     */
    private String tenantId;

    /**
     * The tenant name
     */
    private String tenantName;

    /**
     * The user id used to authenticate
     */
    private String userid;

    /**
     * The configuration of this adapter
     */
    private Configuration configuration;

    /**
     * The service catalog for this provider
     */
    private ServiceCatalog catalog;

    /**
     * Set to true when the cache has been initialized
     */
    private boolean initialized;

    /**
     * The logger to use
     */
    private EELFLogger logger;

    /**
     * Construct the cache of tenants for the specified provider
     *
     * @param provider
     *            The provider
     */
    public TenantCache(ProviderCache provider) {
        configuration = ConfigurationFactory.getConfiguration();
        logger = EELFManager.getInstance().getLogger(getClass());
        this.provider = provider;
        configuration = ConfigurationFactory.getConfiguration();
    }

    /**
     * @return True when the cache has been initialized. A tenant cache is initialized when the service catalog for the
     *         tenant on the specified provider has been loaded and processed.
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Initializes the tenant cache.
     * <p>
     * This method authenticates to the provider and obtains the service catalog. For the service catalog we can
     * determine all supported regions for this provider, as well as all published services and their endpoints. We will
     * cache and maintain a copy of the service catalog for later queries.
     * </p>
     * <p>
     * Once the catalog has been obtained, we create a context pool for each region defined. The context allows access
     * to services of a single region only, so we need a separate context by region. It is possible to operate on
     * resources that span regions, but to do so will require acquiring a context for each region of interest.
     * </p>
     * <p>
     * The context pool maintains the reusable context objects and allocates them as needed. This class is registered as
     * the allocator and destructor for the pool, so that we can create a new context when needed, and close it when no
     * longer used.
     * </p>
     */
    public void initialize() {
        logger.debug("Initializing TenantCache");

        int min = configuration.getIntegerProperty(Constants.PROPERTY_MIN_POOL_SIZE);
        int max = configuration.getIntegerProperty(Constants.PROPERTY_MAX_POOL_SIZE);
        int delay = configuration.getIntegerProperty(Constants.PROPERTY_RETRY_DELAY);
        int limit = configuration.getIntegerProperty(Constants.PROPERTY_RETRY_LIMIT);

        String url = provider.getIdentityURL();
        String tenant = tenantName == null ? tenantId : tenantName;
        Properties properties = configuration.getProperties();

        int attempt = 1;
        while (attempt <= limit) {
            try {
                catalog = new ServiceCatalog(url, tenant, userid, password, properties);
                tenantId = catalog.getTenantId();
                tenantName = catalog.getTenantName();

                for (String region : catalog.getRegions()) {
                    try {
                        Pool<Context> pool = new Pool<>(min, max);
                        pool.setProperty(ContextFactory.PROPERTY_IDENTITY_URL, url);
                        pool.setProperty(ContextFactory.PROPERTY_TENANT, tenantName);
                        pool.setProperty(ContextFactory.PROPERTY_CLIENT_CONNECTOR_CLASS, CLIENT_CONNECTOR_CLASS);
                        pool.setProperty(ContextFactory.PROPERTY_RETRY_DELAY,
                            configuration.getProperty(Constants.PROPERTY_RETRY_DELAY));
                        pool.setProperty(ContextFactory.PROPERTY_RETRY_LIMIT,
                            configuration.getProperty(Constants.PROPERTY_RETRY_LIMIT));
                        pool.setProperty(ContextFactory.PROPERTY_REGION, region);
                        if (properties.getProperty(ContextFactory.PROPERTY_TRUSTED_HOSTS) != null) {
                            pool.setProperty(ContextFactory.PROPERTY_TRUSTED_HOSTS,
                                properties.getProperty(ContextFactory.PROPERTY_TRUSTED_HOSTS));
                        }
                        pool.setAllocator(this);
                        pool.setDestructor(this);
                        pools.put(region, pool);
                        logger.debug(String.format("Put pool for region %s", region));
                    } catch (PoolSpecificationException e) {
                        logger.error("Error creating pool", e);
                        e.printStackTrace();
                    }
                }
                initialized = true;
                break;
            } catch (ContextConnectionException e) {
                attempt++;
                if (attempt <= limit) {
                    logger.error(Msg.CONNECTION_FAILED_RETRY, provider.getProviderName(), url, tenantName, tenantId, e.getMessage(), Integer.toString(delay), Integer.toString(attempt),
                            Integer.toString(limit));

                    try {
                        Thread.sleep(delay * 1000L);
                    } catch (InterruptedException ie) {
                        // ignore
                    }
                }
            } catch ( ZoneException e) {
                logger.error(e.getMessage());
                break;
            }
        }

        if (!initialized) {
            logger.error(Msg.CONNECTION_FAILED, provider.getProviderName(), url);
        }
    }

    /**
     * This method accepts a fully qualified compute node URL and uses that to determine which region of the provider
     * hosts that compute node.
     *
     * @param url
     *            The parsed URL of the compute node
     * @return The region name, or null if no region of this tenant hosts that compute node.
     */
    public String determineRegion(VMURL url) {
        logger.debug(String.format("Attempting to determine VM region for %s", url));
        String region = null;
        Pattern urlPattern = Pattern.compile("[^:]+://([^:/]+)(?::([0-9]+)).*");

        if (url != null) {
            for (Endpoint endpoint : catalog.getEndpoints(ServiceCatalog.COMPUTE_SERVICE)) {
                String endpointUrl = endpoint.getPublicURL();
                Matcher matcher = urlPattern.matcher(endpointUrl);
                if (matcher.matches()) {
                    if (url.getHost().equals(matcher.group(1))) {
                        if (url.getPort() != null) {
                            if (!url.getPort().equals(matcher.group(2))) {
                                continue;
                            }
                        }

                        region = endpoint.getRegion();
                        break;
                    }
                }
            }
        }
        logger.debug(String.format("Region for %s is %s", url, region));
        return region;
    }

    /**
     * @return the value of provider
     */
    public ProviderCache getProvider() {
        return provider;
    }

    /**
     * @param provider
     *            the value for provider
     */
    public void setProvider(ProviderCache provider) {
        this.provider = provider;
    }

    /**
     * @return the value of password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password
     *            the value for password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the value of tenantId
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * @param tenantId
     *            the value for tenantId
     */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    /**
     * @return the value of tenantName
     */
    public String getTenantName() {
        return tenantName;
    }

    /**
     * @param tenantName
     *            the value for tenantName
     */
    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    /**
     * @return the value of userid
     */
    public String getUserid() {
        return userid;
    }

    /**
     * @param userid
     *            the value for userid
     */
    public void setUserid(String userid) {
        this.userid = userid;
    }

    /**
     * @return the value of pools
     */
    public Map<String, Pool<Context>> getPools() {
        return pools;
    }

    /**
     * @see org.openecomp.appc.pool.Allocator#allocate(org.openecomp.appc.pool.Pool)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Context allocate(Pool<Context> pool) {
        logger.debug("Allocationg context for pool");
        Class<? extends Provider> providerClass;
        try {
            providerClass = (Class<? extends Provider>) Class.forName("com.att.cdp.openstack.OpenStackProvider");
            // String providerType = provider.getProviderType();

            // Context context = ContextFactory.getContext(providerType, pool.getProperties());
            Context context = ContextFactory.getContext(providerClass, pool.getProperties());
            context.login(userid, password);
            return context;
        } catch (IllegalStateException | IllegalArgumentException | ZoneException | ClassNotFoundException e) {
            logger.debug("Failed to allocate context for pool", e);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @see org.openecomp.appc.pool.Destructor#destroy(java.lang.Object, org.openecomp.appc.pool.Pool)
     */
    @Override
    public void destroy(Context context, Pool<Context> pool) {
        try {
            context.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return the service catalog for this provider
     */
    public ServiceCatalog getServiceCatalog() {
        return catalog;
    }
}
