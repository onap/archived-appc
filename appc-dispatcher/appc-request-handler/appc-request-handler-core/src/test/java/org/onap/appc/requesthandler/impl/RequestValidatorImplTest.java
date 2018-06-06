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

package org.onap.appc.requesthandler.impl;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.onap.appc.configuration.Configuration;
import org.onap.appc.exceptions.APPCException;

import java.util.Properties;

import static org.mockito.Mockito.mock;

public class RequestValidatorImplTest {
    private Configuration mockConfig = mock(Configuration.class);
    private RequestValidatorImpl impl;

    @Before
    public void setUp() throws Exception {
        impl = new RequestValidatorImpl();
        Whitebox.setInternalState(impl, "configuration", mockConfig);
    }

    // TODO: remove Ignore when initialize method actually throws APPCException
    @Ignore
    @Test(expected = APPCException.class)
    public void testInitializeWithNullConfigProps() throws Exception {
        Mockito.doReturn(null).when(mockConfig).getProperties();
        impl.initialize();
    }

    // TODO: remove Ignore when initialize method actually throws APPCException
    @Ignore
    @Test(expected = APPCException.class)
    public void testInitializeWithoutEndpointProp() throws Exception {
        Properties mockProp = mock(Properties.class);
        Mockito.doReturn(null).when(mockProp).getProperty(RequestValidatorImpl.SCOPE_OVERLAP_ENDPOINT);
        Mockito.doReturn(mockProp).when(mockConfig).getProperties();
        impl.initialize();
    }

    // TODO: remove Ignore when initialize method actually throws APPCException
    @Ignore
    @Test(expected = APPCException.class)
    public void testInitializeWithMalFormatEndpoint() throws Exception {
        Properties mockProp = mock(Properties.class);
        Mockito.doReturn("a/b/c").when(mockProp).getProperty(RequestValidatorImpl.SCOPE_OVERLAP_ENDPOINT);
        Mockito.doReturn(mockProp).when(mockConfig).getProperties();
        impl.initialize();
    }
}
