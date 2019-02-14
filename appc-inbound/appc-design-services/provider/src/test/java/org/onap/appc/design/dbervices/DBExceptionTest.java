/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright 2019 IBM
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
package org.onap.appc.design.dbervices;

import org.junit.Assert;
import org.junit.Test;

public class DBExceptionTest {

    @Test
    public void testDBException1() {
        DBException dbException = new DBException("DBException");
        Assert.assertEquals("DBException", dbException.getMessage());
    }

    @Test
    public void testDBException2() {
        DBException dbException = new DBException("DBException Occured", new Throwable("TestMsg"));
        Assert.assertEquals("org.onap.appc.design.dbervices.DBException", dbException.getClass().getName());
        Assert.assertEquals("TestMsg", dbException.getCause().getMessage());
    }

}
