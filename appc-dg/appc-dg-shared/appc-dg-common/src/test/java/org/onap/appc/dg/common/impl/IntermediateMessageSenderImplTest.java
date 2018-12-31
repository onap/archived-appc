/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 Ericsson
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
import org.onap.appc.adapter.message.MessageAdapterFactory;
import org.onap.appc.adapter.message.Producer;
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
public class IntermediateMessageSenderImplTest {

    private SvcLogicContext ctx;
    private Map<String, String> params;

    private final BundleContext bundleContext = Mockito.mock(BundleContext.class);
    private final Bundle bundleService = Mockito.mock(Bundle.class);
    private final ServiceReference sref = Mockito.mock(ServiceReference.class);
    private final MessageAdapterFactory mockFactory = Mockito.mock(MessageAdapterFactory.class);
    private final Producer mockProducer = Mockito.mock(Producer.class);

    @InjectMocks
    private IntermediateMessageSenderImpl intermediateMessageSenderImpl;

    @Spy
    private EventSenderMock eventSender = new EventSenderMock();

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        PowerMockito.mockStatic(FrameworkUtil.class);
        PowerMockito.when(FrameworkUtil.getBundle(Matchers.any(Class.class))).thenReturn(bundleService);
        PowerMockito.when(bundleService.getBundleContext()).thenReturn(bundleContext);
        PowerMockito.when(bundleContext.getServiceReference(Matchers.any(Class.class))).thenReturn(sref);
        PowerMockito.when(bundleContext.<MessageAdapterFactory>getService(sref)).thenReturn(mockFactory);
        PowerMockito.when(mockFactory.createProducer(Matchers.anyCollection(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString())).thenReturn(mockProducer);
    }

    @Test
    public void testSendEmptyMessage() {
        intermediateMessageSenderImpl.init();
        ctx = new SvcLogicContext();
        params = new HashMap<>();
        intermediateMessageSenderImpl.sendMessage(params, ctx);
        Assert.assertEquals("FAILURE", ctx.getAttribute("STATUS"));
    }

    @Test
    public void testSendMessage() {
        intermediateMessageSenderImpl.init();
        ctx = new SvcLogicContext();
        ctx.setAttribute("input.common-header.request-id", "REQUEST-ID");
        params = new HashMap<>();
        params.put("message", "TEST MESSAGE");
        params.put("code", "TEST CODE");
        intermediateMessageSenderImpl.sendMessage(params, ctx);
        Assert.assertEquals("SUCCESS", ctx.getAttribute("STATUS"));
    }
}
