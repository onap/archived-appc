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

public class WorkflowNotFoundExceptionTest {

   @Test
    public final void test() {
       String sampleMsg = "sample testing message";
       String vnfId = "sample vnfId";
       String cmd = "sample command";
       WorkflowNotFoundException wfnfe = new WorkflowNotFoundException(sampleMsg, vnfId, cmd);
       assertNotNull(wfnfe.getMessage());
       assertEquals(sampleMsg, wfnfe.getMessage());
       assertEquals(wfnfe.getLcmCommandStatus(), LCMCommandStatus.WORKFLOW_NOT_FOUND);   }
}
