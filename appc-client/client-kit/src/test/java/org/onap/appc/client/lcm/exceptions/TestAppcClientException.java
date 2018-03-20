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

public class TestAppcClientException {
    @Test
    public void testConstructorNoArgument() throws Exception {
        AppcClientException appcClientException = new AppcClientException();
        Assert.assertTrue(appcClientException.getCause() == null);
        Assert.assertTrue(appcClientException.getLocalizedMessage() == null);
        Assert.assertTrue(appcClientException.getMessage() == null);
    }

    @Test
    public void testConstructorWithMessaqge() throws Exception {
        String message = "testing message";
        AppcClientException appcClientException = new AppcClientException(message);
        Assert.assertTrue(appcClientException.getCause() == null);
        Assert.assertEquals(message, appcClientException.getLocalizedMessage());
        Assert.assertEquals(message, appcClientException.getMessage());
    }

    @Test
    public void testConstructorWithMessageAndThrowable() throws Exception {
        String message = "testing message";
        String tMessage = "throwable message";
        Throwable throwable = new Throwable(tMessage);
        AppcClientException appcClientException =new AppcClientException(message, throwable);
        Assert.assertEquals(throwable, appcClientException.getCause());
        Assert.assertTrue(appcClientException.getLocalizedMessage().contains(message));
        Assert.assertTrue(appcClientException.getMessage().contains(message));
    }

    @Test
    public void testConstructorWithThrowable() throws Exception {
        String message = "testing message";
        Throwable throwable = new Throwable(message);
        AppcClientException appcClientException = new AppcClientException(throwable);
        Assert.assertEquals(throwable, appcClientException.getCause());
        Assert.assertTrue(appcClientException.getLocalizedMessage().contains(message));
        Assert.assertTrue(appcClientException.getMessage().contains(message));
    }
}
