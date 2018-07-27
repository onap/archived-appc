/* 
 * ============LICENSE_START======================================================= 
 * ONAP : APPC 
 * ================================================================================ 
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved. 
 * ============================================================================= 
 * Modifications Copyright (C) 2018 IBM. 
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
 * ============LICENSE_END========================================================= 
 */

package org.onap.appc.requesthandler.exceptions;

import static org.junit.Assert.*;
import org.junit.Test;
import org.onap.appc.executor.objects.LCMCommandStatus;
import org.onap.appc.executor.objects.Params;

import junit.framework.Assert;

public class RequestValidationExceptionTest {
    @SuppressWarnings("deprecation")
    @Test
    public final void testRequestValidationExceptionWithParams() {
        String message = "tesingMessage";
        String command = "tesingCommand";
        Params p = new Params().addParam("actionName", command);
        RequestValidationException rve = new RequestValidationException(message, LCMCommandStatus.MISSING_MANDATORY_PARAMETER, p);
        rve.setLogMessage("testLogMessage");
        Assert.assertEquals("testLogMessage", rve.getLogMessage());
        rve.setTargetEntity("testTargetEntity");
        Assert.assertEquals("testTargetEntity", rve.getTargetEntity());
        rve.setTargetService("testTargetService");
        Assert.assertEquals("testTargetService", rve.getTargetService());
        assertNotNull(rve.getParams());
        assertNotNull(rve.getMessage());
    }
}
