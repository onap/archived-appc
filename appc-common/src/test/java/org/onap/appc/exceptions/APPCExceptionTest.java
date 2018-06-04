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

package org.onap.appc.exceptions;

import org.junit.Assert;
import org.junit.Test;
import org.powermock.reflect.Whitebox;


public class APPCExceptionTest {

    @Test
    public void testConstructorNoArgument() throws Exception {
        APPCException appcException = new APPCException();
        Assert.assertTrue(appcException.getCause() == null);
        Assert.assertTrue(appcException.getLocalizedMessage() == null);
        Assert.assertTrue(appcException.getMessage() == null);
    }

    @Test
    public void testConstructorWithMessaqge() throws Exception {
        String message = "testing message";
        APPCException appcException = new APPCException(message);
        Assert.assertTrue(appcException.getCause() == null);
        Assert.assertEquals(message, appcException.getLocalizedMessage());
        Assert.assertEquals(message, appcException.getMessage());
    }

    @Test
    public void testConstructorWithThrowable() throws Exception {
        String message = "testing message";
        Throwable throwable = new Throwable(message);
        APPCException appcException = new APPCException(throwable);
        Assert.assertEquals(throwable, appcException.getCause());
        Assert.assertTrue(appcException.getLocalizedMessage().contains(message));
        Assert.assertTrue(appcException.getMessage().contains(message));
    }

    @Test
    public void testConstructorWithMessageAndThrowable() throws Exception {
        String message = "testing message";
        String tMessage = "throwable message";
        Throwable throwable = new Throwable(tMessage);
        APPCException appcException = new APPCException(message, throwable);
        Assert.assertEquals(throwable, appcException.getCause());
        Assert.assertTrue(appcException.getLocalizedMessage().contains(message));
        Assert.assertTrue(appcException.getMessage().contains(message));
    }

    @Test
    public void testConstructorWithFourArguments() throws Exception {
        String message = "testing message";
        String tMessage = "throwable message";
        Throwable throwable = new Throwable(tMessage);
        APPCException appcException = new APPCException(message, throwable, true, true);
        Assert.assertEquals(throwable, appcException.getCause());
        Assert.assertTrue(appcException.getLocalizedMessage().contains(message));
        Assert.assertTrue(appcException.getMessage().contains(message));

        Assert.assertTrue(Whitebox.getInternalState(appcException, "stackTrace") != null);
        Assert.assertTrue(Whitebox.getInternalState(appcException, "suppressedExceptions") != null);

        appcException = new APPCException(message, throwable, false, false);
        Assert.assertTrue(Whitebox.getInternalState(appcException, "stackTrace") == null);
        Assert.assertTrue(Whitebox.getInternalState(appcException, "suppressedExceptions") == null);
    }
}
