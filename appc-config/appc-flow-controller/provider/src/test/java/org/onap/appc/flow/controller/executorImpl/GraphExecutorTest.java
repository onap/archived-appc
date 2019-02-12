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

package org.onap.appc.flow.controller.executorImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.onap.appc.flow.controller.data.Transaction;
import org.onap.appc.flow.controller.utils.FlowControllerConstants;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicService;
import org.osgi.framework.FrameworkUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFLogger.Level;
import com.att.eelf.configuration.EELFManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

@RunWith(PowerMockRunner.class)
@PrepareForTest(FrameworkUtil.class)
public class GraphExecutorTest {

    private final BundleContext bundleContext = Mockito.mock(BundleContext.class);
    private final Bundle bundleService = Mockito.mock(Bundle.class);
    private final ServiceReference sref = Mockito.mock(ServiceReference.class);
    private SvcLogicService svcLogic = null;
    private Map<String, String> params;
    private EELFLogger log = EELFManager.getInstance().getLogger(GraphExecutor.class);

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        log.setLevel(Level.DEBUG);
        svcLogic = Mockito.mock(SvcLogicService.class);
        PowerMockito.mockStatic(FrameworkUtil.class);
        PowerMockito.when(FrameworkUtil.getBundle(Matchers.any(Class.class))).thenReturn(bundleService);
        PowerMockito.when(bundleService.getBundleContext()).thenReturn(bundleContext);
        PowerMockito.when(bundleContext.getServiceReference(SvcLogicService.NAME)).thenReturn(sref);
        PowerMockito.when(bundleContext.<SvcLogicService>getService(sref)).thenReturn(svcLogic);
        params = new HashMap<>();
    }

    @Test
    public void testExecuteGraph() throws SvcLogicException {
        GraphExecutor graphExecutor = new GraphExecutor();
        Whitebox.setInternalState(GraphExecutor.class, "log", log);
        Properties properties = new Properties();
        properties.put("TEST", "TEST");
        Mockito.when(svcLogic.execute(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.any(Properties.class))).thenReturn(properties);
        assertEquals(properties, graphExecutor.executeGraph(null, null, null, null, new Properties()));
    }

    @Test
    public void testExecute() throws Exception {
        GraphExecutor graphExecutor = new GraphExecutor();
        Whitebox.setInternalState(GraphExecutor.class, "log", log);
        Properties properties = new Properties();
        properties.put("TEST", "TEST");
        Transaction transaction = Mockito.spy(new Transaction());
        transaction.setExecutionRPC("EXECUTION_RPC");
        transaction.setPayload("PAYLOAD");
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("TEST", "TEST");
        Mockito.when(svcLogic.execute(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.any(Properties.class))).thenReturn(properties);
        assertNull(graphExecutor.execute(transaction, ctx));
    }

    @Test
    public void testExecuteFailure() throws Exception {
        GraphExecutor graphExecutor = new GraphExecutor();
        Whitebox.setInternalState(GraphExecutor.class, "log", log);
        Properties properties = new Properties();
        properties.put(GraphExecutor.SVC_LOGIC_STATUS_PARAM, FlowControllerConstants.FAILURE);
        Transaction transaction = Mockito.spy(new Transaction());
        transaction.setExecutionRPC("EXECUTION_RPC");
        transaction.setPayload("PAYLOAD");
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("TEST", "TEST");
        Mockito.when(svcLogic.execute(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.any(Properties.class))).thenReturn(properties);
        assertNull(graphExecutor.execute(transaction, ctx));
    }

    @Test
    public void testExecuteSuccess() throws Exception {
        GraphExecutor graphExecutor = new GraphExecutor();
        Whitebox.setInternalState(GraphExecutor.class, "log", log);
        Properties properties = new Properties();
        properties.put(GraphExecutor.SVC_LOGIC_STATUS_PARAM, FlowControllerConstants.SUCCESS);
        Transaction transaction = Mockito.spy(new Transaction());
        transaction.setExecutionRPC("EXECUTION_RPC");
        transaction.setPayload("PAYLOAD");
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("TEST", "TEST");
        Mockito.when(svcLogic.execute(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.any(Properties.class))).thenReturn(properties);
        assertNull(graphExecutor.execute(transaction, ctx));
    }
}
