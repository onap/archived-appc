/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications (C) 2018 Ericsson
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

package org.onap.appc.dg.common.impl;

import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.onap.appc.adapter.message.EventSender;
import org.onap.appc.adapter.message.MessageDestination;
import org.onap.appc.adapter.message.event.EventMessage;
import org.onap.appc.exceptions.APPCException;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


@RunWith(PowerMockRunner.class)
@PrepareForTest(FrameworkUtil.class)
public class DCAEReporterPluginImplTest {
    private SvcLogicContext ctx;
    private Map<String, String> params;

    private final BundleContext bundleContext = Mockito.mock(BundleContext.class);
    private final Bundle bundleService = Mockito.mock(Bundle.class);
    private final ServiceReference sref = Mockito.mock(ServiceReference.class);

    @InjectMocks
    private DCAEReporterPluginImpl dcaeReporterPlugin;
    @Spy
    private EventSenderMock eventSender = new EventSenderMock();

    private String apiVer = "2.0.0";
    private String requestId = "123";
    private String error = "test-error";

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        PowerMockito.mockStatic(FrameworkUtil.class);
        PowerMockito.when(FrameworkUtil.getBundle(Matchers.any(Class.class))).thenReturn(bundleService);
        PowerMockito.when(bundleService.getBundleContext()).thenReturn(bundleContext);
        PowerMockito.when(bundleContext.getServiceReference(Matchers.any(Class.class))).thenReturn(sref);
        PowerMockito.when(bundleContext.<EventSender>getService(sref)).thenReturn(eventSender);
    }

    @Test
    public void testReportErrorDescriptionNullBwcModeFalse() throws Exception {
        ctx = new SvcLogicContext();
        params = new HashMap<>();
        params.put("output.status.message", null);
        ctx.setAttribute("input.common-header.api-ver", apiVer);
        ctx.setAttribute("input.common-header.request-id", requestId);

        errorReasonNullAssert();
    }

    @Test
    public void testReportBwcFalse() throws Exception {
        ctx = new SvcLogicContext();
        params = new HashMap<>();
        ctx.setAttribute("isBwcMode", "false");
        params.put("output.status.message", error);
        ctx.setAttribute("input.common-header.api-ver", apiVer);
        ctx.setAttribute("input.common-header.request-id", requestId);

        positiveAssert();
    }

    @Test
    public void testReportErrorDefinition() throws APPCException {
        ctx = new SvcLogicContext();
        params = new HashMap<>();
        params.put(Constants.EVENT_MESSAGE, "ERROR DESCRIPTION");
        params.put(Constants.DG_ERROR_CODE, "200");
        ctx.setAttribute("input.common-header.api-ver", apiVer);
        ctx.setAttribute("input.common-header.request-id", requestId);
        dcaeReporterPlugin.report(params, ctx);
        EventMessage msg = eventSender.getMsg();
        Assert.assertEquals("ERROR DESCRIPTION", msg.getEventStatus().getReason());
    }

    @Test
    public void testReportSuccess() throws APPCException {
        ctx = new SvcLogicContext();
        params = new HashMap<>();
        params.put(Constants.DG_ERROR_CODE, "200");
        dcaeReporterPlugin.reportSuccess(params, ctx);
        EventMessage msg = eventSender.getMsg();
        Assert.assertEquals(new Integer(500), msg.getEventStatus().getCode());
    }

    private void errorReasonNullAssert() throws APPCException {
        dcaeReporterPlugin.report(params, ctx);
        MessageDestination destination = eventSender.getDestination();
        EventMessage msg = eventSender.getMsg();
        Assert.assertEquals("wrong API version", apiVer, msg.getEventHeader().getApiVer());
        Assert.assertEquals("wrong requestId", requestId, msg.getEventHeader().getEventId());
        Assert.assertEquals("wrong error message", "Unknown", msg.getEventStatus().getReason());
        Assert.assertEquals("wrong destination", destination.name(), "DCAE");
    }

    private void positiveAssert() throws APPCException {
        dcaeReporterPlugin.report(params, ctx);
        MessageDestination destination = eventSender.getDestination();
        EventMessage msg = eventSender.getMsg();
        Assert.assertEquals("wrong API version", apiVer, msg.getEventHeader().getApiVer());
        Assert.assertEquals("wrong requestId", requestId, msg.getEventHeader().getEventId());
        Assert.assertEquals("wrong error message", error, msg.getEventStatus().getReason());
        Assert.assertEquals("wrong destination", destination.name(), "DCAE");
    }
}
