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
package org.onap.appc.dao.util.message;

import org.junit.Assert;
import org.junit.Test;

public class MessagesTest {
    private Messages messages=Messages.EXP_APPC_JDBC_CONNECT;

    @Test
    public void testName() {
        Assert.assertEquals("EXP_APPC_JDBC_CONNECT", messages.name());
    }

    @Test
    public void testEqual() {
        Assert.assertTrue(messages.equals(Messages.EXP_APPC_JDBC_CONNECT));
        Assert.assertFalse(messages.equals(null));
    }

    @Test
    public void testgetMessage() {
        Assert.assertEquals("Error connecting to JDBC using properties for schema [%s]", Messages.EXP_APPC_JDBC_CONNECT.getMessage());
    }

    @Test
    public void testformat() {
        Assert.assertEquals(("Error connecting to JDBC using properties for schema [%s]"), Messages.EXP_APPC_JDBC_CONNECT.format("%s"));
    }
}
