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

public class JdbcRuntimeExceptionTest {

    @Test
    public void testConstructorWithMessage() throws Exception {
        String message = "testing message";
        JdbcRuntimeException jdbcRuntimeException = new JdbcRuntimeException(message);
        Assert.assertTrue(jdbcRuntimeException.getCause() == null);
        Assert.assertEquals(message, jdbcRuntimeException.getLocalizedMessage());
        Assert.assertEquals(message, jdbcRuntimeException.getMessage());
    }

    @Test
    public void testConstructorWithMessageAndThrowable() throws Exception {
        String message = "testing message";
        String tMessage = "throwable message";
        Throwable throwable = new Throwable(tMessage);
        JdbcRuntimeException jdbcRuntimeException = new JdbcRuntimeException(message, throwable);
        Assert.assertEquals(throwable, jdbcRuntimeException.getCause());
        Assert.assertTrue(jdbcRuntimeException.getLocalizedMessage().contains(message));
        Assert.assertTrue(jdbcRuntimeException.getMessage().contains(message));
    }
}
