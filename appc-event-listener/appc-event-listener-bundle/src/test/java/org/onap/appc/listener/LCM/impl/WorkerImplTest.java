/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2018 Nokia Solutions and Networks
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
package org.onap.appc.listener.LCM.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.onap.appc.listener.TestUtil.JSON_OUTPUT_BODY_STR;
import static org.onap.appc.listener.TestUtil.buildDmaapMessage;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.listener.EventHandler;
import org.onap.appc.listener.LCM.operation.ProviderOperations;
import org.onap.appc.listener.util.Mapper;

public class WorkerImplTest {

    private EventHandler mockEventHandler = mock(EventHandler.class);
    private ProviderOperations mockProviderOperations = mock(ProviderOperations.class);


    @Test(expected = IllegalStateException.class)
    public void should_throw_when_one_of_worker_fields_is_null() {

        WorkerImpl worker = new WorkerImpl(null, mockEventHandler, mockProviderOperations);
        worker.run();
    }

    @Test
    public void should_post_error_message_to_dmaap_on_exception() throws APPCException {

        when(mockProviderOperations.topologyDG(anyString(), any(JsonNode.class)))
            .thenThrow(new RuntimeException("test exception"));

        WorkerImpl worker = new WorkerImpl(buildDmaapMessage(), mockEventHandler, mockProviderOperations);
        worker.run();

        verify(mockEventHandler).postStatus(anyString(), anyString());
    }


    @Test
    public void should_post_message_to_dmaap_on_successful_run() throws APPCException {

        JsonNode testOutputJsonNode = Mapper.toJsonNodeFromJsonString(JSON_OUTPUT_BODY_STR);
        when(mockProviderOperations.topologyDG(anyString(), any(JsonNode.class)))
            .thenReturn(testOutputJsonNode);

        WorkerImpl worker = new WorkerImpl(buildDmaapMessage(), mockEventHandler, mockProviderOperations);
        worker.run();

        verify(mockEventHandler).postStatus(anyString(), anyString());
    }
}
