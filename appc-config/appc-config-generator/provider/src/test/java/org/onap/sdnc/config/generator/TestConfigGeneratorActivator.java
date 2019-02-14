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

package org.onap.sdnc.config.generator;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.powermock.reflect.Whitebox;

public class TestConfigGeneratorActivator {
  
  private ConfigGeneratorActivator configGeneratorActivator;
  private BundleContext ctx;
  
  @Before
  public void setUp() {
    configGeneratorActivator = new ConfigGeneratorActivator();
    ctx = Mockito.mock(BundleContext.class);
    ServiceRegistration serviceRegistration = Mockito.mock(ServiceRegistration.class);
    when(ctx.registerService(anyString(), anyObject(), eq(null))).thenReturn(serviceRegistration);
  }
  
  @Test
  public void testStart() throws Exception {
    configGeneratorActivator.start(ctx);
    List<ServiceRegistration> registrations = Whitebox.getInternalState(configGeneratorActivator, "registrations");
    assertNotNull(registrations.get(0));
  }
  
  @Test
  public void testStop() throws Exception {
    configGeneratorActivator.stop(ctx);
    List<ServiceRegistration> registrations = Whitebox.getInternalState(configGeneratorActivator, "registrations");
    assertTrue(registrations.isEmpty());
  }

}
