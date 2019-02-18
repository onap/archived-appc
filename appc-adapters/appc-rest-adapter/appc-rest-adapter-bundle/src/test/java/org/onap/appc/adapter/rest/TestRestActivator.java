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

package org.onap.appc.adapter.rest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class TestRestActivator {

    private RestActivator restActivator;
    private BundleContext context;
    private ServiceRegistration registration = null;;

    @Before
    public void setUp() {
        restActivator = new RestActivator();
        context = Mockito.mock(BundleContext.class);
        registration = Mockito.mock(ServiceRegistration.class);
        when(context.registerService(eq(RestAdapter.class), anyObject(), eq(null))).thenReturn(registration);
    }

    @Test
    public void testStart() throws Exception {
        restActivator.start(context);
        assertNotNull(Whitebox.getInternalState(restActivator, "registration"));
    }

    @Test
    public void testStop() throws Exception {
        restActivator.start(context);
        restActivator.stop(context);
        assertNull(Whitebox.getInternalState(restActivator, "registration"));
    }
}
