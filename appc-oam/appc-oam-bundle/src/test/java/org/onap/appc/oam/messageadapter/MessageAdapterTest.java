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

package org.onap.appc.oam.messageadapter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.appc.adapter.message.MessageAdapterFactory;
import org.onap.appc.adapter.message.Producer;
import org.onap.appc.configuration.Configuration;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.common.header.CommonHeader;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.common.header.CommonHeaderBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.status.Status;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFLogger.Level;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.onap.appc.configuration.ConfigurationFactory;
import org.onap.appc.oam.AppcOam.RPC;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;


@RunWith(PowerMockRunner.class)
@PrepareForTest({FrameworkUtil.class, ConfigurationFactory.class, Converter.class})
public class MessageAdapterTest {

    private Producer fakeProducer;

    private MessageAdapter messageAdapter;


    @Before
    public final void setup() throws Exception {
        fakeProducer = mock(Producer.class);
        messageAdapter = new MessageAdapter();
    }

    @Test
    public void testGetProducerReturnsNull() throws Exception {
        MessageAdapter maSpy = Mockito.spy(messageAdapter);
        Mockito.doNothing().when(maSpy).createProducer();

        Producer producer = maSpy.getProducer();
        Assert.assertTrue("getProducer() did not return null", producer == null);
        Producer mySpyProducer = Whitebox.getInternalState(maSpy, "producer");
        Assert.assertTrue("MessageAdapter producer is not null", mySpyProducer == null);
        Mockito.verify(maSpy, Mockito.times(1)).createProducer();
    }

    @Test
    public void testGetProducerWithExistingProducer() throws Exception {
        MessageAdapter maSpy = Mockito.spy(messageAdapter);
        Whitebox.setInternalState(maSpy, "producer", fakeProducer);

        Producer producer = maSpy.getProducer();
        Assert.assertTrue("getProducer() returned null", producer == fakeProducer);
        Mockito.verify(maSpy, Mockito.times(0)).createProducer();
    }

    @Test
    public void testGetProducerWithCreateProducer() throws Exception {
        MessageAdapter maSpy = Mockito.spy(messageAdapter);
        Whitebox.setInternalState(maSpy, "producer", (Object) null);
        HashSet<String> pool = new HashSet<>();
        pool.add("NOT_HTTPS");
        pool.add("https");
        Whitebox.setInternalState(maSpy, "pool", pool);

        // Prepare all mocks
        mockStatic(FrameworkUtil.class);
        Bundle maBundle = mock(Bundle.class);
        PowerMockito.when(FrameworkUtil.getBundle(MessageAdapter.class)).thenReturn(maBundle);

        BundleContext maBundleContext = mock(BundleContext.class);
        Mockito.when(maBundle.getBundleContext()).thenReturn(maBundleContext);

        ServiceReference svcRef = mock(ServiceReference.class);
        Mockito.when(maBundleContext.getServiceReference(MessageAdapterFactory.class.getName())).thenReturn(svcRef);

        MessageAdapterFactory maFactory = mock(MessageAdapterFactory.class);
        Mockito.when(maBundleContext.getService(svcRef)).thenReturn(maFactory);
        Mockito.when(maFactory.createProducer(pool, (String) null, null, null)).thenReturn(fakeProducer);

        Producer producer = maSpy.getProducer();
        Assert.assertTrue("getProducer() result does not match", producer == fakeProducer);
        Producer mySpyProducer = Whitebox.getInternalState(maSpy, "producer");
        Assert.assertTrue("MessageAdapter producer does not match",mySpyProducer == fakeProducer);
        Mockito.verify(maSpy, Mockito.times(1)).createProducer();
    }

    @Test
    public void testUpdateProperties() {
        MessageAdapter maSpy = Mockito.spy(messageAdapter);
        Mockito.doNothing().when(maSpy).createProducer();
        Whitebox.setInternalState(maSpy, "isDisabled", false);
        PowerMockito.mockStatic(ConfigurationFactory.class);
        Configuration mockConfig = Mockito.mock(Configuration.class);
        Properties properties = new Properties();
        properties.setProperty("appc.OAM.poolMembers", "hostname1,hostname2");
        Mockito.when(ConfigurationFactory.getConfiguration()).thenReturn(mockConfig);
        Mockito.doReturn(properties).when(mockConfig).getProperties();
        maSpy.init();
        assertEquals(2, ((Set<String>)Whitebox.getInternalState(maSpy, "pool")).size());
    }

    @Test
    public void testPost() throws JsonProcessingException {
        MessageAdapter maSpy = Mockito.spy(messageAdapter);
        OAMContext oamContext = new OAMContext();
        oamContext.setRpcName(RPC.maintenance_mode);
        CommonHeader mockCommonHeader = Mockito.mock(CommonHeader.class);
        Mockito.doReturn("TEST REQUEST ID").when(mockCommonHeader).getRequestId();
        oamContext.setCommonHeader(mockCommonHeader);
        Status mockStatus = Mockito.mock(Status.class);
        oamContext.setStatus(mockStatus);
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        Mockito.doReturn(true).when(mockLogger).isTraceEnabled();
        Mockito.doReturn(true).when(mockLogger).isDebugEnabled();
        Whitebox.setInternalState(maSpy, "logger", mockLogger);
        PowerMockito.mockStatic(Converter.class);
        Mockito.when(Converter.convAsyncResponseToUebOutgoingMessageJsonString(oamContext)).thenReturn("{cambriaPartition='MSO', rpcName='maintenance_mode',"
                + " body=Body{output=MaintenanceModeOutput [_commonHeader=CommonHeader, hashCode: 14584991,"
                + " _status=Status, hashCode: 24801521, augmentation=[]]}}");
        Mockito.doNothing().when(maSpy).createProducer();
        maSpy.post(oamContext);
        Mockito.verify(mockLogger).trace(Mockito.contains("Entering to post"));
        Mockito.verify(mockLogger).trace("Exiting from post with (success = false)");
    }

    @Test
    public void testPostExceptionFlow() throws JsonProcessingException {
        MessageAdapter maSpy = Mockito.spy(messageAdapter);
        OAMContext oamContext = new OAMContext();
        oamContext.setRpcName(RPC.maintenance_mode);
        CommonHeader mockCommonHeader = Mockito.mock(CommonHeader.class);
        Mockito.doReturn("TEST REQUEST ID").when(mockCommonHeader).getRequestId();
        oamContext.setCommonHeader(mockCommonHeader);
        Status mockStatus = Mockito.mock(Status.class);
        oamContext.setStatus(mockStatus);
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        Mockito.doReturn(false).when(mockLogger).isTraceEnabled();
        Mockito.doReturn(false).when(mockLogger).isDebugEnabled();
        Whitebox.setInternalState(maSpy, "logger", mockLogger);
        PowerMockito.mockStatic(Converter.class);
        Mockito.when(Converter.convAsyncResponseToUebOutgoingMessageJsonString(oamContext)).thenThrow(new JsonProcessingException("ERROR") {});
        Mockito.doNothing().when(maSpy).createProducer();
        maSpy.post(oamContext);
        Mockito.verify(mockLogger).error(Mockito.contains("Error generating Json from UEB message"));
    }

    @Test
    public void testPostExceptionFlow2() throws JsonProcessingException {
        MessageAdapter maSpy = Mockito.spy(messageAdapter);
        OAMContext oamContext = new OAMContext();
        oamContext.setRpcName(RPC.maintenance_mode);
        CommonHeader mockCommonHeader = Mockito.mock(CommonHeader.class);
        Mockito.doReturn("TEST REQUEST ID").when(mockCommonHeader).getRequestId();
        oamContext.setCommonHeader(mockCommonHeader);
        Status mockStatus = Mockito.mock(Status.class);
        oamContext.setStatus(mockStatus);
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        Mockito.doReturn(false).when(mockLogger).isTraceEnabled();
        Mockito.doReturn(false).when(mockLogger).isDebugEnabled();
        Whitebox.setInternalState(maSpy, "logger", mockLogger);
        PowerMockito.mockStatic(Converter.class);
        Mockito.when(Converter.convAsyncResponseToUebOutgoingMessageJsonString(oamContext)).thenThrow(new RuntimeException("ERROR"));
        Mockito.doNothing().when(maSpy).createProducer();
        maSpy.post(oamContext);
        Mockito.verify(mockLogger).error(Mockito.contains("Error sending message to UEB ERROR"), Mockito.any(RuntimeException.class));
    }
}