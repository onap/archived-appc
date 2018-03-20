/*
* ============LICENSE_START=======================================================
* ONAP : APPC
* ================================================================================
* Copyright 2018 TechMahindra
*=================================================================================
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* ============LICENSE_END=========================================================
*/
package org.onap.appc.client.lcm.exceptions;

import org.junit.Assert;
import org.junit.Test;

public class TestAppcClientInternalException {
    @Test
    public void testConstructorNoArgument() throws Exception {
        AppcClientInternalException appcClientInternalException = new AppcClientInternalException();
        Assert.assertTrue(appcClientInternalException.getCause() == null);
        Assert.assertTrue(appcClientInternalException.getLocalizedMessage() == null);
        Assert.assertTrue(appcClientInternalException.getMessage() == null);
    }

    @Test
    public void testConstructorWithMessage() throws Exception {
        String message = "testing message";
        AppcClientInternalException appcClientInternalException = new AppcClientInternalException(message);
        Assert.assertTrue(appcClientInternalException.getCause() == null);
        Assert.assertEquals(message, appcClientInternalException.getLocalizedMessage());
        Assert.assertEquals(message, appcClientInternalException.getMessage());
    }

    @Test
    public void testConstructorWithMessageAndThrowable() throws Exception {
        String message = "testing message";
        String tMessage = "throwable message";
        Throwable throwable = new Throwable(tMessage);
        AppcClientInternalException appcClientInternalException = new AppcClientInternalException(message, throwable);
        Assert.assertEquals(throwable, appcClientInternalException.getCause());
        Assert.assertTrue(appcClientInternalException.getLocalizedMessage().contains(message));
        Assert.assertTrue(appcClientInternalException.getMessage().contains(message));
    }

    @Test
    public void testConstructorWithThrowable() throws Exception {
        String message = "testing message";
        Throwable throwable = new Throwable(message);
        AppcClientInternalException appcClientInternalException = new AppcClientInternalException(throwable);
        Assert.assertEquals(throwable, appcClientInternalException.getCause());
        Assert.assertTrue(appcClientInternalException.getLocalizedMessage().contains(message));
        Assert.assertTrue(appcClientInternalException.getMessage().contains(message));
    }
}