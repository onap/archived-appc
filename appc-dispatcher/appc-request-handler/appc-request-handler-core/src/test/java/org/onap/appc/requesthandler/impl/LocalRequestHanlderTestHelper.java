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

package org.onap.appc.requesthandler.impl;

import org.mockito.Mockito;
import org.onap.appc.adapter.message.MessageAdapterFactory;
import org.onap.appc.adapter.message.Producer;
import org.onap.appc.domainmodel.lcm.ActionIdentifiers;
import org.onap.appc.domainmodel.lcm.ActionLevel;
import org.onap.appc.domainmodel.lcm.CommonHeader;
import org.onap.appc.domainmodel.lcm.Flags;
import org.onap.appc.domainmodel.lcm.RequestContext;
import org.onap.appc.domainmodel.lcm.RequestStatus;
import org.onap.appc.domainmodel.lcm.ResponseContext;
import org.onap.appc.domainmodel.lcm.RuntimeContext;
import org.onap.appc.domainmodel.lcm.VNFOperation;
import org.onap.appc.lockmanager.api.LockManager;
import org.onap.appc.requesthandler.RequestHandler;
import org.onap.appc.transactionrecorder.TransactionRecorder;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.powermock.api.mockito.PowerMockito;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Creates RequestContextInput for ActionStatus JUnits
 */
public interface LocalRequestHanlderTestHelper {
    default RuntimeContext createRequestHandlerRuntimeContext(String vnfId, String payload) {
        RuntimeContext context = new RuntimeContext();

        RequestContext requestContext = createRequestContext(VNFOperation.ActionStatus, "requestId1",
            vnfId, ActionLevel.MGMT, payload);
        context.setRequestContext(requestContext);

        ResponseContext resContext = new ResponseContext();
        resContext.setCommonHeader(context.getRequestContext().getCommonHeader());
        context.setResponseContext(resContext);

        return context;
    }

    default RequestContext createRequestContext(VNFOperation operation, String requestId, String vnfId,
                                                ActionLevel level, String payload) {
        RequestContext reqContext = new RequestContext();
        reqContext.setCommonHeader(getCommonHeader(requestId, "2.0.0", "originatorId"));
        reqContext.setActionLevel(level);
        reqContext.setAction(operation);
        reqContext.setPayload(payload);
        reqContext.setActionIdentifiers(getActionIdentifiers(vnfId, null, null));

        return reqContext;
    }

    default ActionIdentifiers getActionIdentifiers(String vnfId, String vnfcId, String vserverId) {
        ActionIdentifiers builder = new ActionIdentifiers();
        builder.setVnfId(vnfId);
        builder.setVnfcName(vnfcId);
        builder.setvServerId(vserverId);
        return builder;
    }

    default CommonHeader getCommonHeader(String requestId, String apiVer, String originatorId) {
        CommonHeader builder = new CommonHeader();
        builder.setRequestId(requestId);
        builder.setApiVer(apiVer);
        builder.setOriginatorId(originatorId);
        builder.setTimestamp(new Date(System.currentTimeMillis()));
        return builder;
    }

    default CommonHeader getCommonHeader(String requestId, Date date) {
        CommonHeader builder = new CommonHeader();
        builder.setRequestId(requestId);
        builder.setTimestamp(date);
        Flags flags = new Flags();
        flags.setTtl(0);
        builder.setFlags(flags);
        return builder;
    }

    default void setupForHandlerImplTests() {
        mockStatic(FrameworkUtil.class);
        Bundle myBundle = mock(Bundle.class);
        PowerMockito.when(FrameworkUtil.getBundle(any())).thenReturn(myBundle);

        BundleContext myBundleContext = mock(BundleContext.class);
        Mockito.when(myBundle.getBundleContext()).thenReturn(myBundleContext);

        ServiceReference svcRef = mock(ServiceReference.class);
        Mockito.when(myBundleContext.getServiceReference(MessageAdapterFactory.class.getName())).thenReturn(svcRef);

        Producer producer = mock(Producer.class);
        MessageAdapterFactory factory = mock(MessageAdapterFactory.class);
        Mockito.when(myBundleContext.getService(svcRef)).thenReturn(factory);
        Mockito.when(factory.createProducer(anyCollectionOf(String.class), anyString(), anyString(), anyString()))
        .thenReturn(producer);
    }
}
