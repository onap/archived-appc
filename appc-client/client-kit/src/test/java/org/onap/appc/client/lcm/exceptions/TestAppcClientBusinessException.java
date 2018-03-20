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

public class TestAppcClientBusinessException {

    @Test
    public void testConstructorNoArgument() throws Exception {
        AppcClientBusinessException appcClientBusinessException = new AppcClientBusinessException();
        Assert.assertTrue(appcClientBusinessException.getCause() == null);
        Assert.assertTrue(appcClientBusinessException.getLocalizedMessage() == null);
        Assert.assertTrue(appcClientBusinessException.getMessage() == null);
    }

    @Test
    public void testConstructorWithMessage() throws Exception {
        String message = "testing message";
        AppcClientBusinessException appcClientBusinessException = new AppcClientBusinessException(message);
        Assert.assertTrue(appcClientBusinessException.getCause() == null);
        Assert.assertEquals(message, appcClientBusinessException.getLocalizedMessage());
        Assert.assertEquals(message, appcClientBusinessException.getMessage());
    }

    @Test
    public void testConstructorWithMessageAndThrowable() throws Exception {
        String message = "testing message";
        String tMessage = "throwable message";
        Throwable throwable = new Throwable(tMessage);
        AppcClientBusinessException appcClientBusinessException = new AppcClientBusinessException(message, throwable);
        Assert.assertEquals(throwable, appcClientBusinessException.getCause());
        Assert.assertTrue(appcClientBusinessException.getLocalizedMessage().contains(message));
        Assert.assertTrue(appcClientBusinessException.getMessage().contains(message));
    }

    @Test
    public void testConstructorWithThrowable() throws Exception {
        String message = "testing message";
        Throwable throwable = new Throwable(message);
        AppcClientBusinessException appcClientBusinessException = new AppcClientBusinessException(throwable);
        Assert.assertEquals(throwable, appcClientBusinessException.getCause());
        Assert.assertTrue(appcClientBusinessException.getLocalizedMessage().contains(message));
        Assert.assertTrue(appcClientBusinessException.getMessage().contains(message));
    }
}
