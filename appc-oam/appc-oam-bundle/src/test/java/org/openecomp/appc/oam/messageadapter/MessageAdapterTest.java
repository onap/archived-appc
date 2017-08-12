/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.appc.oam.messageadapter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.appc.adapter.message.MessageAdapterFactory;
import org.openecomp.appc.adapter.message.Producer;

import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.HashSet;

import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;


@RunWith(PowerMockRunner.class)
@PrepareForTest({MessageAdapter.class, FrameworkUtil.class})
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
}
