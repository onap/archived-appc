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

package org.openecomp.appc.adapter.iaas.provider.operation.impl.base;

import org.openecomp.appc.adapter.iaas.ProviderAdapter;
import org.openecomp.appc.adapter.iaas.impl.*;
import org.openecomp.appc.adapter.iaas.provider.operation.api.IProviderOperation;
import org.openecomp.appc.adapter.iaas.provider.operation.common.constants.Constants;
import org.openecomp.appc.adapter.iaas.provider.operation.common.enums.Outcome;
import org.openecomp.appc.configuration.Configuration;
import org.openecomp.appc.configuration.ConfigurationFactory;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.i18n.Msg;
import org.openecomp.appc.pool.Pool;
import org.openecomp.appc.pool.PoolExtensionException;
import com.att.cdp.zones.Context;
import com.att.cdp.zones.model.ModelObject;
import com.att.cdp.zones.model.Server;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import org.openecomp.sdnc.sli.SvcLogicContext;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.slf4j.MDC;

import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.openecomp.appc.adapter.iaas.provider.operation.common.constants.Constants.MDC_ADAPTER;
import static org.openecomp.appc.adapter.iaas.provider.operation.common.constants.Constants.MDC_SERVICE;
import static com.att.eelf.configuration.Configuration.MDC_SERVICE_NAME;

/**
 * @since September 26, 2016
 */
public abstract class ProviderOperation implements IProviderOperation {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(ProviderOperation.class);
    protected static final Configuration configuration = ConfigurationFactory.getConfiguration();

    public void setProviderCache(Map<String, ProviderCache> providerCache) {
        this.providerCache = providerCache;
    }

    /**
     * A cache of providers that are predefined.
     */
    private Map<String /* provider name */, ProviderCache> providerCache;


    public void setDefaultUser(String defaultUser) {
        DEFAULT_USER = defaultUser;
    }

    public void setDefaultPass(String defaultPass) {
        DEFAULT_PASS = defaultPass;
    }

    /**
     * The username and password to use for dynamically created connections
     */
    private static String DEFAULT_USER;
    private static String DEFAULT_PASS;


    /**
     * set MDC props
     * @param service
     * @param serviceName
     * @param adapterName
     */
    protected void setMDC(String service, String serviceName, String adapterName){
        MDC.put(MDC_ADAPTER, adapterName);
        MDC.put(MDC_SERVICE, service);
        MDC.put(MDC_SERVICE_NAME, serviceName);
    }

    /**
     * initial log of the operation
     * @param msg
     * @param params
     * @param context
     */
    protected void logOperation(Msg msg, Map<String, String> params, SvcLogicContext context){

        String appName = configuration.getProperty(org.openecomp.appc.Constants.PROPERTY_APPLICATION_NAME);
        logger.info(msg, appName);

        debugParameters(params);
        debugContext(context);
    }

    /**
     * This method is used to dump the value of the parameters to the log for debugging purposes.
     *
     * @param parameters
     *            The parameters to be printed to the log
     */
    private void debugParameters(Map<String, String> parameters) {
        for (String key : parameters.keySet()) {
            logger.debug(Msg.PROPERTY_VALUE, key, parameters.get(key));
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
        builder.append(Constants.LPAREN);
        builder.append(context.getStatus());
        builder.append(Constants.RPAREN);
        builder.append(", Attribute count ");
        builder.append(Constants.LPAREN);
        builder.append(keys == null ? "none" : Integer.toString(keys.size()));
        builder.append(Constants.RPAREN);
        if (keys != null && !keys.isEmpty()) {
            builder.append(Constants.NL);
            for (String key : keys) {
                String value = context.getAttribute(key);
                builder.append("Attribute ");
                builder.append(Constants.LPAREN);
                builder.append(key);
                builder.append(Constants.RPAREN);
                builder.append(", value ");
                builder.append(Constants.LPAREN);
                builder.append(value == null ? "" : value);
                builder.append(Constants.RPAREN);
                builder.append(Constants.NL);
            }
        }

        logger.debug(builder.toString());
    }


    /**
     * This method is used to validate that the parameters contain all required property names, and that the values are
     * non-null and non-empty strings. We are still not ensured that the value is valid, but at least it exists.
     *
     * @param parameters
     *            The parameters to be checked
     * @param propertyNames
     *            The list of property names that are required to be present.
     * @throws RequestFailedException
     *             If the parameters are not valid
     */
    protected void validateParametersExist(Map<String, String> parameters, String... propertyNames)
            throws RequestFailedException {
        boolean success = true;
        StringBuilder msg = new StringBuilder(EELFResourceManager.format(Msg.MISSING_REQUIRED_PROPERTIES, MDC.get(MDC_SERVICE)));
        msg.append(Constants.NL);
        for (String propertyName : propertyNames) {
            String value = parameters.get(propertyName);
            if (value == null || value.trim().length() == 0) {
                success = false;
                msg.append(Constants.QUOTE);
                msg.append(propertyName);
                msg.append(Constants.QUOTE);
                msg.append(Constants.SPACE);
            }
        }

        if (!success) {
            logger.error(msg.toString());
            throw new RequestFailedException("Check Parameters", msg.toString(), HttpStatus.BAD_REQUEST_400, (Server)null);
        }
    }

    /**
     * @param rc
     *            The request context that manages the state and recovery of the request for the life of its processing.
     * @param code
     * @param message
     */
    protected void doFailure(RequestContext rc, HttpStatus code, String message) {
        try {
            doFailure(rc, code, message, null);
        } catch (APPCException ignored) {/* never happens */}
    }

    protected void doFailure(RequestContext rc, HttpStatus code, String message, Throwable cause) throws APPCException {
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
        svcLogic.setStatus(Outcome.FAILURE.toString());
        svcLogic.setAttribute(org.openecomp.appc.Constants.ATTRIBUTE_ERROR_CODE, status);
        svcLogic.setAttribute(org.openecomp.appc.Constants.ATTRIBUTE_ERROR_MESSAGE, msg);

        if (null != cause) throw new APPCException(cause);
    }

    /**
     * @param rc
     *            The request context that manages the state and recovery of the request for the life of its processing.
     */
    @SuppressWarnings("static-method")
    protected void doSuccess(RequestContext rc) {
        SvcLogicContext svcLogic = rc.getSvcLogicContext();
        svcLogic.setStatus(Outcome.SUCCESS.toString());
        svcLogic.setAttribute(org.openecomp.appc.Constants.ATTRIBUTE_ERROR_CODE, Integer.toString(HttpStatus.OK_200.getStatusCode()));
    }

    protected boolean validateVM(RequestContext rc, String appName, String vm_url, VMURL vm)
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

    protected void validateVMURL(VMURL vm) throws RequestFailedException {
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
    protected Context getContext(RequestContext rc, String selfLinkURL, String providerName) {
        VMURL vm = VMURL.parseURL(selfLinkURL);
        IdentityURL ident = IdentityURL.parseURL(providerName);
        String appName = configuration.getProperty(org.openecomp.appc.Constants.PROPERTY_APPLICATION_NAME);

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

    protected Context resolveContext(RequestContext rc, Map<String, String> params, String appName, String vm_url)
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







    protected abstract ModelObject executeProviderOperation(Map<String, String> params, SvcLogicContext context) throws APPCException;

    @Override
    public ModelObject doOperation(Map<String, String> params, SvcLogicContext context) throws APPCException {

        return executeProviderOperation(params,context);
    }
}
