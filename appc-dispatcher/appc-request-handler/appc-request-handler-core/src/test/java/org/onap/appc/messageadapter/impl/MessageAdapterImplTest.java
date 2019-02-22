/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 Ericsson
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
 *
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.messageadapter.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.onap.appc.adapter.message.MessageAdapterFactory;
import org.onap.appc.adapter.message.Producer;
import org.onap.appc.configuration.Configuration;
import org.onap.appc.configuration.ConfigurationFactory;
import org.onap.appc.requesthandler.conv.Converter;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.att.eelf.configuration.EELFLogger.Level;

@RunWith(PowerMockRunner.class)
@PrepareForTest({FrameworkUtil.class, Converter.class})
public class MessageAdapterImplTest {

    private Configuration mockConfig = ConfigurationFactory.getConfiguration();
    private final BundleContext bundleContext = Mockito.mock(BundleContext.class);
    private final Bundle bundleService = Mockito.mock(Bundle.class);
    private final ServiceReference sref = Mockito.mock(ServiceReference.class);
    private final Producer producer = Mockito.mock(Producer.class);
    private static final EELFLogger logger = EELFManager.getInstance().getLogger(MessageAdapterImpl.class);
    private MessageAdapterImpl impl;

    @Before
    public void setUp() throws Exception {
        logger.setLevel(Level.TRACE);
        impl = PowerMockito.spy(new MessageAdapterImpl());
        //originalLogger = (EELFLogger) Whitebox.getInternalState(MessageAdapterImpl.class, "logger");
        Whitebox.setInternalState(impl, "configuration", mockConfig);
        PowerMockito.mockStatic(FrameworkUtil.class);
        PowerMockito.when(FrameworkUtil.getBundle(MessageAdapterImpl.class)).thenReturn(bundleService);
        PowerMockito.when(bundleService.getBundleContext()).thenReturn(bundleContext);
        PowerMockito.when(bundleContext.getServiceReference(MessageAdapterFactory.class.getName())).thenReturn(sref);
        PowerMockito.when(bundleContext.getService(sref)).thenReturn(producer);
        PowerMockito.mockStatic(Converter.class);
        PowerMockito.when(Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(
                Mockito.any(), Mockito.any(), Mockito.any())).thenReturn("{}");
    }

    @Test
    public void testSuccess() throws JsonProcessingException {
        PowerMockito.when(Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(
                Mockito.any(), Mockito.any(), Mockito.any())).thenReturn("{}");
        Mockito.when(producer.post(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Whitebox.setInternalState(impl, "producer", producer);
        Whitebox.setInternalState(impl, "partition", "PARTITION");
        assertTrue(impl.post(null, null, null));
    }

    @Test
    public void testJsonException() throws JsonProcessingException {
        PowerMockito.when(Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(
                Mockito.any(), Mockito.any(), Mockito.any())).thenThrow(new JsonProcessingException("TEST") {});
        Whitebox.setInternalState(impl, "partition", "PARTITION");
        assertFalse(impl.post(null, null, null));
    }

    @Test
    public void testException() throws JsonProcessingException {
        PowerMockito.when(Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(
                Mockito.any(), Mockito.any(), Mockito.any())).thenThrow(new RuntimeException());
        Whitebox.setInternalState(impl, "partition", "PARTITION");
        assertFalse(impl.post(null, null, null));
    }
}
