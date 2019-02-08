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

import static org.junit.Assert.assertNotNull;
import java.util.concurrent.ScheduledExecutorService;
import org.junit.Test;
import org.mockito.Mockito;
import org.osgi.framework.BundleContext;
import org.powermock.reflect.Whitebox;

public class WorkflowManagerActivatorTest {

    @Test
    public void testStart() {
        BundleContext bundleContext = Mockito.mock(BundleContext.class);
        WorkflowManagerActivator activator = new WorkflowManagerActivator();
        activator.start(bundleContext);
        assertNotNull(Whitebox.getInternalState(activator, "executor"));
    }

    @Test
    public void testStop() throws Exception {
        BundleContext bundleContext = Mockito.mock(BundleContext.class);
        WorkflowManagerActivator activator = new WorkflowManagerActivator();
        ScheduledExecutorService executor = Mockito.mock(ScheduledExecutorService.class);
        Whitebox.setInternalState(activator, "executor", executor);
        activator.stop(bundleContext);
        Mockito.verify(executor).shutdown();
    }

}
