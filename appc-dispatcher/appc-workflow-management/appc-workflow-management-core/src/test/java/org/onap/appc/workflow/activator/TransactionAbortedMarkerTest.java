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

package org.onap.appc.workflow.activator;

import static org.junit.Assert.assertTrue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.onap.appc.transactionrecorder.TransactionRecorder;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.FrameworkUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(FrameworkUtil.class)
public class TransactionAbortedMarkerTest {

    private final BundleContext bundleContext = Mockito.mock(BundleContext.class);
    private final Bundle bundleService = Mockito.mock(Bundle.class);
    private final ServiceReference sref = Mockito.mock(ServiceReference.class);
    private final TransactionRecorder transactionRecorder = Mockito.mock(TransactionRecorder.class);

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void testRunTransactionRecorderServiceException() throws TransactionRecorderServiceNotFoundException {
        ScheduledExecutorService executor = Mockito.mock(ScheduledExecutorService.class);
        TransactionAbortedMarker tamt = Mockito.spy(new TransactionAbortedMarker(executor));
        Mockito.doThrow(new TransactionRecorderServiceNotFoundException(null)).when(tamt).lookupTransactionRecorder();
        tamt.run();
        Mockito.verify(executor).schedule(tamt, 30, TimeUnit.SECONDS);
    }

    @Test
    public void testRuntimeException() throws TransactionRecorderServiceNotFoundException {
        ScheduledExecutorService executor = Mockito.mock(ScheduledExecutorService.class);
        TransactionAbortedMarker tamt = Mockito.spy(new TransactionAbortedMarker(executor));
        Mockito.doThrow(new RuntimeException()).when(tamt).lookupTransactionRecorder();
        expectedEx.expect(RuntimeException.class);
        tamt.run();
    }

    @Test
    public void testLookupTransactionRecorder() throws TransactionRecorderServiceNotFoundException {
        PowerMockito.mockStatic(FrameworkUtil.class);
        PowerMockito.when(FrameworkUtil.getBundle(Matchers.any(Class.class))).thenReturn(bundleService);
        PowerMockito.when(bundleService.getBundleContext()).thenReturn(bundleContext);
        PowerMockito.when(bundleContext.getServiceReference(TransactionRecorder.class.getName())).thenReturn(sref);
        PowerMockito.when(bundleContext.<TransactionRecorder>getService(sref)).thenReturn(transactionRecorder);
        ScheduledExecutorService executor = Mockito.mock(ScheduledExecutorService.class);
        TransactionAbortedMarker tamt = Mockito.spy(new TransactionAbortedMarker(executor));
        assertTrue(tamt.lookupTransactionRecorder() instanceof TransactionRecorder);
    }

    @Test
    public void testLookupTransactionRecorderExceptionFlow() throws TransactionRecorderServiceNotFoundException {
        PowerMockito.mockStatic(FrameworkUtil.class);
        PowerMockito.when(FrameworkUtil.getBundle(Matchers.any(Class.class))).thenReturn(bundleService);
        PowerMockito.when(bundleService.getBundleContext()).thenReturn(bundleContext);
        PowerMockito.when(bundleContext.getServiceReference(TransactionRecorder.class.getName())).thenReturn(sref);
        PowerMockito.when(bundleContext.<TransactionRecorder>getService(sref)).thenReturn(null);
        ScheduledExecutorService executor = Mockito.mock(ScheduledExecutorService.class);
        TransactionAbortedMarker tamt = Mockito.spy(new TransactionAbortedMarker(executor));
        expectedEx.expect(TransactionRecorderServiceNotFoundException.class);
        expectedEx.expectMessage("Cannot find service org.onap.appc.transactionrecorder.TransactionRecorder");
        tamt.lookupTransactionRecorder();
    }
}
