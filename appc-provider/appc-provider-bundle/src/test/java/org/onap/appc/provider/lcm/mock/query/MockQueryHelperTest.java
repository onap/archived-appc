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

package org.onap.appc.provider.lcm.mock.query;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.onap.appc.domainmodel.lcm.ActionIdentifiers;
import org.onap.appc.domainmodel.lcm.RequestContext;
import org.onap.appc.executor.objects.LCMCommandStatus;
import org.onap.appc.requesthandler.objects.RequestHandlerInput;
import org.powermock.reflect.Whitebox;

public class MockQueryHelperTest {

    @Test
    public void testVnfIdFound() {
        MockQueryHelper mockQueryHelper = new MockQueryHelper();
        Whitebox.setInternalState(mockQueryHelper, "MOCK_QUERY_FILENAME", "./src/test/resources/query");
        RequestHandlerInput requestHandlerInput = new RequestHandlerInput();
        RequestContext requestContext = new RequestContext();
        ActionIdentifiers actionIdentifiers = new ActionIdentifiers();
        actionIdentifiers.setVnfId("vSCP");
        requestContext.setActionIdentifiers(actionIdentifiers);
        requestHandlerInput.setRequestContext(requestContext);
        assertEquals(LCMCommandStatus.SUCCESS.getResponseCode(),
                mockQueryHelper.query(requestHandlerInput).getResponseContext().getStatus().getCode());
    }

    @Test
    public void testVnfIdNotFound() {
        MockQueryHelper mockQueryHelper = new MockQueryHelper();
        Whitebox.setInternalState(mockQueryHelper, "MOCK_QUERY_FILENAME", "./src/test/resources/query");
        RequestHandlerInput requestHandlerInput = new RequestHandlerInput();
        RequestContext requestContext = new RequestContext();
        ActionIdentifiers actionIdentifiers = new ActionIdentifiers();
        actionIdentifiers.setVnfId("OTHER_VNF_ID");
        requestContext.setActionIdentifiers(actionIdentifiers);
        requestHandlerInput.setRequestContext(requestContext);
        assertEquals(LCMCommandStatus.VNF_NOT_FOUND.getResponseCode(),
                mockQueryHelper.query(requestHandlerInput).getResponseContext().getStatus().getCode());
    }
}
