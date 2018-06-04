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

public class UnknownProviderExceptionTest {

    @Test
    public void testConstructorNoArgument() throws Exception {
        UnknownProviderException unknownProviderException = new UnknownProviderException();
        Assert.assertTrue(unknownProviderException.getCause() == null);
        Assert.assertTrue(unknownProviderException.getLocalizedMessage() == null);
        Assert.assertTrue(unknownProviderException.getMessage() == null);
    }

    @Test
    public void testConstructorWithMessaqge() throws Exception {
        String message = "testing message";
        UnknownProviderException unknownProviderException = new UnknownProviderException(message);
        Assert.assertTrue(unknownProviderException.getCause() == null);
        Assert.assertEquals(message, unknownProviderException.getLocalizedMessage());
        Assert.assertEquals(message, unknownProviderException.getMessage());
    }

    @Test
    public void testConstructorWithThrowable() throws Exception {
        String message = "testing message";
        Throwable throwable = new Throwable(message);
        UnknownProviderException unknownProviderException = new UnknownProviderException(throwable);
        Assert.assertEquals(throwable, unknownProviderException.getCause());
        Assert.assertTrue(unknownProviderException.getLocalizedMessage().contains(message));
        Assert.assertTrue(unknownProviderException.getMessage().contains(message));
    }

    @Test
    public void testConstructorWithMessageAndThrowable() throws Exception {
        String message = "testing message";
        String tMessage = "throwable message";
        Throwable throwable = new Throwable(tMessage);
        UnknownProviderException unknownProviderException =
                new UnknownProviderException(message, throwable);
        Assert.assertEquals(throwable, unknownProviderException.getCause());
        Assert.assertTrue(unknownProviderException.getLocalizedMessage().contains(message));
        Assert.assertTrue(unknownProviderException.getMessage().contains(message));
    }

    @Test
    public void testConstructorWithFourArguements() throws Exception {
        String message = "testing message";
        String tMessage = "throwable message";
        Throwable throwable = new Throwable(tMessage);
        UnknownProviderException unknownProviderException =
                new UnknownProviderException(message, throwable, true, true);
        Assert.assertEquals(throwable, unknownProviderException.getCause());
        Assert.assertTrue(unknownProviderException.getLocalizedMessage().contains(message));
        Assert.assertTrue(unknownProviderException.getMessage().contains(message));

        Assert.assertTrue(
                Whitebox.getInternalState(unknownProviderException, "stackTrace") != null);
        Assert.assertTrue(Whitebox.getInternalState(unknownProviderException,
                "suppressedExceptions") != null);

        unknownProviderException = new UnknownProviderException(message, throwable, false, false);
        Assert.assertTrue(
                Whitebox.getInternalState(unknownProviderException, "stackTrace") == null);
        Assert.assertTrue(Whitebox.getInternalState(unknownProviderException,
                "suppressedExceptions") == null);
    }

}
