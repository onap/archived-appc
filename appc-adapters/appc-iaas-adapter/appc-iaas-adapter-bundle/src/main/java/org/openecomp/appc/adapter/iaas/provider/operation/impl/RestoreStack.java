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
import org.openecomp.appc.adapter.iaas.impl.RequestContext;
import org.openecomp.appc.adapter.iaas.impl.RequestFailedException;
import org.openecomp.appc.adapter.iaas.provider.operation.common.enums.Operation;
import org.openecomp.appc.adapter.iaas.provider.operation.impl.base.ProviderStackOperation;
import org.openecomp.appc.adapter.openstack.heat.SnapshotResource;
import org.openecomp.appc.adapter.openstack.heat.StackResource;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.exceptions.UnknownProviderException;
import org.openecomp.appc.i18n.Msg;
import com.att.cdp.exceptions.ResourceNotFoundException;
import com.att.cdp.exceptions.ZoneException;
import com.att.cdp.openstack.OpenStackContext;
import com.att.cdp.openstack.connectors.HeatConnector;
import com.att.cdp.openstack.util.ExceptionMapper;
import com.att.cdp.zones.Context;
import com.att.cdp.zones.model.ModelObject;
import com.att.cdp.zones.model.Stack;
import com.att.cdp.zones.spi.RequestState;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import org.openecomp.sdnc.sli.SvcLogicContext;
import com.woorea.openstack.base.client.OpenStackBaseException;
import com.woorea.openstack.heat.Heat;
import org.glassfish.grizzly.http.util.HttpStatus;

import java.util.Map;

import static org.openecomp.appc.adapter.iaas.provider.operation.common.enums.Operation.RESTART_SERVICE;
import static org.openecomp.appc.adapter.utils.Constants.ADAPTER_NAME;


public class RestoreStack extends ProviderStackOperation {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(RestoreStack.class);

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

    public Stack restoreStack(Map<String, String> params, SvcLogicContext ctx) throws IllegalArgumentException, APPCException {
        Stack stack = null;
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();

        ctx.setAttribute("SNAPSHOT_STATUS", "STACK_NOT_FOUND");
        String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);

        String vm_url = null;
        Context context = null;

        try {

            validateParametersExist(params, ProviderAdapter.PROPERTY_INSTANCE_URL, ProviderAdapter.PROPERTY_PROVIDER_NAME,
                    ProviderAdapter.PROPERTY_STACK_ID, ProviderAdapter.PROPERTY_INPUT_SNAPSHOT_ID);

            String stackId = params.get(ProviderAdapter.PROPERTY_STACK_ID);
            vm_url = params.get(ProviderAdapter.PROPERTY_INSTANCE_URL);
            String snapshotId = params.get(ProviderAdapter.PROPERTY_INPUT_SNAPSHOT_ID);

            context = resolveContext(rc, params, appName, vm_url);


            if (context != null) {
                    stack = lookupStack(rc, context, stackId);
                    logger.debug(Msg.STACK_FOUND, vm_url, context.getTenantName(), stack.getStatus().toString());
                    logger.info(EELFResourceManager.format(Msg.TERMINATING_STACK, stack.getName()));
                    restoreStack(stack, snapshotId);
                    logger.info(EELFResourceManager.format(Msg.TERMINATE_STACK, stack.getName()));
                    context.close();
                    doSuccess(rc);
            }else {
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

    @Override
    protected ModelObject executeProviderOperation(Map<String, String> params, SvcLogicContext context) throws APPCException {

        setMDC(Operation.RESTORE_STACK.toString(), "App-C IaaS Adapter:Restore-Stack", ADAPTER_NAME);
        logOperation(Msg.RESTORING_STACK, params, context);
        return restoreStack(params, context);
    }
}
