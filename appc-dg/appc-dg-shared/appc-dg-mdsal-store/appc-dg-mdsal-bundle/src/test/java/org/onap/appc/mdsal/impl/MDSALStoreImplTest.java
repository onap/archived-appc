/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 Ericsson
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

package org.onap.appc.mdsal.impl;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.Date;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.mdsal.MDSALStore;
import org.onap.appc.mdsal.exception.MDSALStoreException;
import org.onap.appc.mdsal.objects.BundleInfo;
import org.onap.appc.rest.client.RestClientInvoker;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import com.att.eelf.configuration.EELFLogger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import java.net.URL;

@RunWith(PowerMockRunner.class)
@PrepareForTest(FrameworkUtil.class)
public class MDSALStoreImplTest {

    private final BundleContext bundleContext= Mockito.mock(BundleContext.class);
    private final Bundle bundleService=Mockito.mock(Bundle.class);
    private MDSALStoreImpl mdsalStore;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void setup() {
        mdsalStore = (MDSALStoreImpl) MDSALStoreFactory.createMDSALStore();
        PowerMockito.mockStatic(FrameworkUtil.class);
        PowerMockito.when(FrameworkUtil.getBundle(MDSALStoreImpl.class)).thenReturn(bundleService);
        PowerMockito.when(bundleService.getBundleContext()).thenReturn(bundleContext);
        PowerMockito.when(bundleContext.getBundle("TEST_MODULE_NAME")).thenReturn(bundleService);
    }

    @Test
    public void testMDSALStoreImpl() {
        Assert.assertTrue(mdsalStore.isModulePresent("TEST_MODULE_NAME", new Date()));
    }

    @Test
    public void testStoreYangModule() throws MDSALStoreException {
        expectedEx.expect(MDSALStoreException.class);
        expectedEx.expectMessage("Error storing yang module:");
        mdsalStore.storeYangModule("", new BundleInfo());
    }

    @Test
    public void testStoreYangModuleOnLeader() throws MDSALStoreException, APPCException, IllegalStateException, IOException {
        RestClientInvoker mockInvoker = Mockito.mock(RestClientInvoker.class);
        Whitebox.setInternalState(mdsalStore, "client", mockInvoker);
        HttpResponse mockResponse = Mockito.mock(HttpResponse.class);
        Mockito.doReturn(mockResponse).when(mockInvoker).doGet(Constants.GET_SHARD_LIST_PATH);
        String httpString = "{\"value\":{\"MemberName\":\"NodeName\"}}";
        InputStream is = new ByteArrayInputStream(httpString.getBytes(Charset.defaultCharset()));
        HttpEntity mockEntity = Mockito.mock(HttpEntity.class);
        Mockito.doReturn(is).when(mockEntity).getContent();
        Mockito.doReturn(mockEntity).when(mockResponse).getEntity();
        StatusLine mockStatusLine = Mockito.mock(StatusLine.class);
        Mockito.doReturn(mockStatusLine).when(mockResponse).getStatusLine();
        Mockito.doReturn(200).when(mockStatusLine).getStatusCode();
        HttpResponse mockLeaderResponse = Mockito.mock(HttpResponse.class);
        Mockito.doReturn(mockLeaderResponse).when(mockInvoker).doGet(String.format(Constants.GET_NODE_STATUS_PATH_FORMAT, "NodeName-shard-default-config"));
        String httpLeaderString = "{\"value\":{\"Leader\":\"NodeName-shard-default-config\"}}";
        InputStream isLeader = new ByteArrayInputStream(httpLeaderString.getBytes(Charset.defaultCharset()));
        HttpEntity mockLeaderEntity = Mockito.mock(HttpEntity.class);
        Mockito.doReturn(isLeader).when(mockLeaderEntity).getContent();
        Mockito.doReturn(mockLeaderEntity).when(mockLeaderResponse).getEntity();
        StatusLine mockLeaderStatusLine = Mockito.mock(StatusLine.class);
        Mockito.doReturn(mockLeaderStatusLine).when(mockLeaderResponse).getStatusLine();
        Mockito.doReturn(200).when(mockLeaderStatusLine).getStatusCode();
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        Whitebox.setInternalState(mdsalStore, "logger", mockLogger);
        mdsalStore.storeYangModuleOnLeader("", "");
        Mockito.verify(mockLogger).debug("Current node is a leader.");
    }
    
    @Test
    public void testStoreYangModuleOnLeaderNotLeader() throws MDSALStoreException, APPCException, IllegalStateException, IOException {
        RestClientInvoker mockInvoker = Mockito.mock(RestClientInvoker.class);
        Whitebox.setInternalState(mdsalStore, "client", mockInvoker);
        HttpResponse mockResponse = Mockito.mock(HttpResponse.class);
        Mockito.doReturn(mockResponse).when(mockInvoker).doGet(Constants.GET_SHARD_LIST_PATH);
        String httpString = "{\"value\":{\"MemberName\":\"NodeName\"}}";
        InputStream is = new ByteArrayInputStream(httpString.getBytes(Charset.defaultCharset()));
        HttpEntity mockEntity = Mockito.mock(HttpEntity.class);
        Mockito.doReturn(is).when(mockEntity).getContent();
        Mockito.doReturn(mockEntity).when(mockResponse).getEntity();
        StatusLine mockStatusLine = Mockito.mock(StatusLine.class);
        Mockito.doReturn(mockStatusLine).when(mockResponse).getStatusLine();
        Mockito.doReturn(200).when(mockStatusLine).getStatusCode();
        HttpResponse mockLeaderResponse = Mockito.mock(HttpResponse.class);
        Mockito.doReturn(mockLeaderResponse).when(mockInvoker).doGet(String.format(Constants.GET_NODE_STATUS_PATH_FORMAT, "NodeName-shard-default-config"));
        String httpLeaderString = "{\"value\":{\"Leader\":\"OtherShardName\",\"PeerAddresses\":\"OtherShardName@adf:a\"}}";
        InputStream isLeader = new ByteArrayInputStream(httpLeaderString.getBytes(Charset.defaultCharset()));
        HttpEntity mockLeaderEntity = Mockito.mock(HttpEntity.class);
        Mockito.doReturn(isLeader).when(mockLeaderEntity).getContent();
        Mockito.doReturn(mockLeaderEntity).when(mockLeaderResponse).getEntity();
        StatusLine mockLeaderStatusLine = Mockito.mock(StatusLine.class);
        Mockito.doReturn(mockLeaderStatusLine).when(mockLeaderResponse).getStatusLine();
        Mockito.doReturn(200).when(mockLeaderStatusLine).getStatusCode();
        MDSALStoreImpl mdsalStoreSpy = Mockito.spy((MDSALStoreImpl) MDSALStoreFactory.createMDSALStore());
        RestClientInvoker mockRemoteInvoker = Mockito.mock(RestClientInvoker.class);
        Mockito.doReturn(mockRemoteInvoker).when(mdsalStoreSpy).getRestClientInvoker(Mockito.any(URL.class));
        StatusLine mockRemoteStatusLine = Mockito.mock(StatusLine.class);
        Mockito.doReturn(200).when(mockRemoteStatusLine).getStatusCode();
        HttpResponse mockLeaderRemoteResponse = Mockito.mock(HttpResponse.class);
        Mockito.doReturn(mockLeaderResponse).when(mockRemoteInvoker).doPost(Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(mockRemoteStatusLine).when(mockLeaderRemoteResponse).getStatusLine();
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        Whitebox.setInternalState(mdsalStoreSpy, "logger", mockLogger);
        mdsalStoreSpy.storeYangModuleOnLeader("", "");
        Mockito.verify(mockLogger).debug("Yang module successfully loaded on leader. Response code: 200");
    }

    @Test
    public void testStoreJson() throws MDSALStoreException, APPCException, IllegalStateException, IOException {
        RestClientInvoker mockInvoker = Mockito.mock(RestClientInvoker.class);
        Whitebox.setInternalState(mdsalStore, "client", mockInvoker);
        HttpResponse mockResponse = Mockito.mock(HttpResponse.class);
        Mockito.doReturn(mockResponse).when(mockInvoker).doPut(Mockito.anyString(), Mockito.anyString());
        String httpString = "{\"value\":{\"MemberName\":\"NodeName\"}}";
        InputStream is = new ByteArrayInputStream(httpString.getBytes(Charset.defaultCharset()));
        HttpEntity mockEntity = Mockito.mock(HttpEntity.class);
        Mockito.doReturn(is).when(mockEntity).getContent();
        Mockito.doReturn(mockEntity).when(mockResponse).getEntity();
        StatusLine mockStatusLine = Mockito.mock(StatusLine.class);
        Mockito.doReturn(mockStatusLine).when(mockResponse).getStatusLine();
        Mockito.doReturn(200).when(mockStatusLine).getStatusCode();
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        Whitebox.setInternalState(mdsalStore, "logger", mockLogger);
        mdsalStore.storeJson("", "", "");
        Mockito.verify(mockLogger).debug("Configuration JSON stored to MD-SAL store successfully. Response code: 200");
    }

    @Test
    public void testStoreJsonRestconfResponse() throws MDSALStoreException, APPCException, IllegalStateException, IOException {
        RestClientInvoker mockInvoker = Mockito.mock(RestClientInvoker.class);
        Whitebox.setInternalState(mdsalStore, "client", mockInvoker);
        HttpResponse mockResponse = Mockito.mock(HttpResponse.class);
        Mockito.doReturn(mockResponse).when(mockInvoker).doPut(Mockito.anyString(), Mockito.anyString());
        String httpString = "{\"errors\":{\"error\":{\"error-message\":{\"error-message\":\"ERROR_MESSAGE\"}}}}";
        InputStream is = new ByteArrayInputStream(httpString.getBytes(Charset.defaultCharset()));
        HttpEntity mockEntity = Mockito.mock(HttpEntity.class);
        Mockito.doReturn(is).when(mockEntity).getContent();
        Mockito.doReturn(mockEntity).when(mockResponse).getEntity();
        StatusLine mockStatusLine = Mockito.mock(StatusLine.class);
        Mockito.doReturn(mockStatusLine).when(mockResponse).getStatusLine();
        Mockito.doReturn(199).when(mockStatusLine).getStatusCode();
        expectedEx.expect(MDSALStoreException.class);
        expectedEx.expectMessage("Failed to load config JSON to MD SAL store. Error Message:");
        mdsalStore.storeJson("", "", "");
    }
}
