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
package org.onap.appc.adapter.iaas.provider.operation.impl;

import com.att.cdp.zones.model.ModelObject;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.onap.appc.adapter.iaas.ProviderAdapter;
import org.onap.appc.exceptions.APPCException;
import com.att.cdp.exceptions.ZoneException;
import com.att.cdp.zones.model.Server;
import com.att.cdp.zones.model.Server.Status;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import java.util.Map;
import static org.mockito.Mockito.*;

public class TestMigrateServer {

    @Test
    public void should_migrateSuspendedServer() throws ZoneException {
        MockGenerator mg = new MockGenerator(Status.SUSPENDED);
        Server server = mg.getServer();
        MigrateServer rbs = new MigrateServer();
        rbs.setProviderCache(mg.getProviderCacheMap());
        try {
            rbs.executeProviderOperation(mg.getParams(), mg.getSvcLogicContext());
        } catch (APPCException e) {
            Assert.fail("Exception during MigrateServer.executeProviderOperation");
        }
        verify(mg.getComputeService()).migrateServer(MockGenerator.SERVER_ID);
        verify(server, atLeastOnce()).waitForStateChange(anyInt(), anyInt(), Matchers.anyVararg());
    }

    @Test
    public void should_returnNullAsServer(){

        //  given
        Map<String, String> params = mock(Map.class);
        SvcLogicContext svcLogicContext = mock(SvcLogicContext.class);
        MockGenerator mockGenerator = new MockGenerator(Status.READY);
        MigrateServer migrateServer = new MigrateServer();
        migrateServer.setProviderCache(mockGenerator.getProviderCacheMap());
        ModelObject modelObject = new Server();

        //  when
        when(params.get(ProviderAdapter.PROPERTY_INSTANCE_URL)).thenReturn(null);
        try {
            modelObject = migrateServer.executeProviderOperation(params,svcLogicContext);
        } catch (APPCException e) {
            Assert.fail("Exception during MigrateServer.executeProviderOperation");
        }

        //  then
        Assert.assertNull(modelObject);
    }
}