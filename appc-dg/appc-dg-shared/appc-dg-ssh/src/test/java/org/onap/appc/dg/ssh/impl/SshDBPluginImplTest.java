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

package org.onap.appc.dg.ssh.impl;

import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.onap.appc.adapter.ssh.Constants;
import org.onap.appc.adapter.ssh.SshConnectionDetails;
import org.onap.appc.adapter.ssh.SshDataAccessException;
import org.onap.appc.adapter.ssh.SshDataAccessService;
import org.onap.appc.exceptions.APPCException;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

public class SshDBPluginImplTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void testRetrieveConnectionDetails() throws APPCException {
        SshDBPluginImpl impl = new SshDBPluginImpl();
        SshDataAccessService dataAccessServiceMock = Mockito.mock(SshDataAccessService.class);
        Mockito.doReturn(true).when(dataAccessServiceMock).retrieveConnectionDetails(Mockito.anyString(),
                Mockito.any(SshConnectionDetails.class));
        impl.setDataAccessService(dataAccessServiceMock);
        Map<String, String> params = new HashMap<>();
        SvcLogicContext ctx = new SvcLogicContext();
        impl.retrieveConnectionDetails(params, ctx);
        Assert.assertNotNull(ctx.getAttribute(Constants.CONNECTION_DETAILS_FIELD_NAME));
    }

    @Test
    public void testRetrieveConnectionDetailsAppcException() throws APPCException {
        SshDBPluginImpl impl = new SshDBPluginImpl();
        SshDataAccessService dataAccessServiceMock = Mockito.mock(SshDataAccessService.class);
        impl.setDataAccessService(dataAccessServiceMock);
        Map<String, String> params = new HashMap<>();
        SvcLogicContext ctx = new SvcLogicContext();
        expectedEx.expect(APPCException.class);
        impl.retrieveConnectionDetails(params, ctx);
    }

    @Test
    public void testRetrieveConnectionDetailsSshDataAccessException() throws APPCException {
        SshDBPluginImpl impl = new SshDBPluginImpl();
        SshDataAccessService dataAccessServiceMock = Mockito.mock(SshDataAccessService.class);
        Mockito.doThrow(new SshDataAccessException()).when(dataAccessServiceMock).retrieveConnectionDetails(Mockito.anyString(),
                Mockito.any(SshConnectionDetails.class));
        impl.setDataAccessService(dataAccessServiceMock);
        Map<String, String> params = new HashMap<>();
        SvcLogicContext ctx = new SvcLogicContext();
        expectedEx.expect(SshDataAccessException.class);
        impl.retrieveConnectionDetails(params, ctx);
    }

}
