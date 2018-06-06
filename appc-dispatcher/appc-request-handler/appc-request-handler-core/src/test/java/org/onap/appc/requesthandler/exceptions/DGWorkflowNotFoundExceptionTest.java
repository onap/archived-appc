/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.appc.requesthandler.exceptions;

import static org.junit.Assert.*;
import org.junit.Test;
import org.onap.appc.executor.objects.LCMCommandStatus;

public class DGWorkflowNotFoundExceptionTest {
    @Test
    public final void testDGWorkflowNotFoundException() {
        String message = "tesingMessage";
        String workflowModule = "tesingWfModule";
        String workflowName = "tesingWfName";
        String workflowVersion = "testingWfVersion";
        String vnfType = "testingVnfType";
        String action = "testingAction";
        DGWorkflowNotFoundException dgwfnfe = new DGWorkflowNotFoundException(message, workflowModule, workflowName,
                workflowVersion, vnfType, action);
        assertEquals(dgwfnfe.getMessage(), message);
        assertEquals(dgwfnfe.workflowModule, workflowModule);
        assertEquals(dgwfnfe.workflowName, workflowName);
        assertEquals(dgwfnfe.workflowVersion, workflowVersion);
        assertEquals(dgwfnfe.getLcmCommandStatus(), LCMCommandStatus.DG_WORKFLOW_NOT_FOUND);
    }
}
