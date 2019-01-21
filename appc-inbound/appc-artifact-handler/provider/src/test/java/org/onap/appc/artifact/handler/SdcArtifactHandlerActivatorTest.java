/*-
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

package org.onap.appc.artifact.handler;

import java.util.LinkedList;
import org.junit.Test;
import org.mockito.Mockito;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.powermock.reflect.Whitebox;


public class SdcArtifactHandlerActivatorTest {

    @Test
    public void testStart() throws Exception {
        SdcArtifactHandlerActivator activator = new SdcArtifactHandlerActivator();
        BundleContext ctx = Mockito.mock(BundleContext.class);
        LinkedList<ServiceRegistration> list = Mockito.spy(new LinkedList<>());
        Whitebox.setInternalState(activator, "registrations", list);
        activator.start(ctx);
        Mockito.verify(list).add(Mockito.any());
    }

    @Test
    public void testStop() throws Exception {
        SdcArtifactHandlerActivator activator = new SdcArtifactHandlerActivator();
        BundleContext ctx = Mockito.mock(BundleContext.class);
        LinkedList<ServiceRegistration> list = Mockito.spy(new LinkedList<>());
        ServiceRegistration serviceRegistration = Mockito.mock(ServiceRegistration.class);
        list.add(serviceRegistration);
        Whitebox.setInternalState(activator, "registrations", list);
        activator.stop(ctx);
        Mockito.verify(serviceRegistration).unregister();
    }
}
