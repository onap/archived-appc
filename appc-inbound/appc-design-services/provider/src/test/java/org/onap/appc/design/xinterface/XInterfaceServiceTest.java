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

package org.onap.appc.design.xinterface;

import static org.junit.Assert.assertNull;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.onap.appc.design.services.util.DesignServiceConstants;

public class XInterfaceServiceTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void testExecute() throws Exception {
        XInterfaceService service = Mockito.spy(new XInterfaceService());
        XResponseProcessor processor = Mockito.mock(XResponseProcessor.class);
        Mockito.when(processor.parseResponse(Mockito.anyString(), Mockito.anyString())).thenReturn(null);
        Mockito.when(service.getXResponseProcessor()).thenReturn(processor);
        assertNull(service.execute(DesignServiceConstants.GETINSTARDATA, "{}"));
    }

    @Test
    public void testExecuteException() throws Exception {
        XInterfaceService service = Mockito.spy(new XInterfaceService());
        XResponseProcessor processor = Mockito.mock(XResponseProcessor.class);
        Mockito.when(processor.parseResponse(Mockito.anyString(), Mockito.anyString())).thenThrow(new RuntimeException());
        Mockito.when(service.getXResponseProcessor()).thenReturn(processor);
        expectedEx.expect(RuntimeException.class);
        service.execute(DesignServiceConstants.GETINSTARDATA, "{}");
    }
}
