/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 Ericsson. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.instar.node;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.onap.appc.instar.InstarClientActivator;
import org.onap.appc.system.node.SourceSystemNode;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class TestInstarClientActivator {

  private BundleContext ctx;
  private InstarClientActivator instarClientActivatorSpy;
  ServiceRegistration serviceRegistration;
  private List<ServiceRegistration> registrations = new LinkedList<>();

  @Before
  public void setUp() {
    ctx = Mockito.mock(BundleContext.class);
    instarClientActivatorSpy = new InstarClientActivator();
    serviceRegistration = Mockito.mock(ServiceRegistration.class);
    registrations.add(serviceRegistration);
  }

  @Test
  public void testStart() throws Exception {
    when(ctx.registerService(eq(SourceSystemNode.class.getName()), anyObject(), eq(null)))
        .thenReturn(serviceRegistration);
    instarClientActivatorSpy.start(ctx);
    List<ServiceRegistration> registrations = (List<ServiceRegistration>) Whitebox
        .getInternalState(instarClientActivatorSpy, "registrations");
    assertNotNull(registrations.get(0));
  }

  @Test
  public void testStop() throws Exception {
    Whitebox.setInternalState(instarClientActivatorSpy, "registrations", registrations);
    instarClientActivatorSpy.stop(ctx);
    verify(serviceRegistration, times(1)).unregister();
  }
}
