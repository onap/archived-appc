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
package org.onap.appc.dao.util.exception;

import org.junit.Assert;
import org.junit.Test;

public class TestDBConnectionPoolException {

    @Test
    public void testConstructorNoArgument() throws Exception {
        DBConnectionPoolException dBConnectionPoolException = new DBConnectionPoolException();
        Assert.assertTrue(dBConnectionPoolException.getCause() == null);
        Assert.assertTrue(dBConnectionPoolException.getLocalizedMessage() == null);
        Assert.assertTrue(dBConnectionPoolException.getMessage() == null);
    }

    @Test
    public void testConstructorWithMessage() throws Exception {
        String message = "testing message";
        DBConnectionPoolException dBConnectionPoolException = new DBConnectionPoolException(message);
        Assert.assertTrue(dBConnectionPoolException.getCause() == null);
        Assert.assertEquals(message, dBConnectionPoolException.getLocalizedMessage());
        Assert.assertEquals(message, dBConnectionPoolException.getMessage());
    }

    @Test
    public void testConstructorWithMessageAndThrowable() throws Exception {
        String message = "testing message";
        String tMessage = "throwable message";
        Throwable throwable = new Throwable(tMessage);
        DBConnectionPoolException dBConnectionPoolException = new DBConnectionPoolException(message, throwable);
        Assert.assertEquals(throwable, dBConnectionPoolException.getCause());
        Assert.assertTrue(dBConnectionPoolException.getLocalizedMessage().contains(message));
        Assert.assertTrue(dBConnectionPoolException.getMessage().contains(message));
    }

    @Test
    public void testConstructorWithThrowable() throws Exception {
        String message = "testing message";
        Throwable throwable = new Throwable(message);
        DBConnectionPoolException dBConnectionPoolException = new DBConnectionPoolException(throwable);
        Assert.assertEquals(throwable, dBConnectionPoolException.getCause());
        Assert.assertTrue(dBConnectionPoolException.getLocalizedMessage().contains(message));
        Assert.assertTrue(dBConnectionPoolException.getMessage().contains(message));
    }
}
