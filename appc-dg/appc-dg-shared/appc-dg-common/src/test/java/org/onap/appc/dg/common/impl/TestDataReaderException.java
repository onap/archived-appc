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
package org.onap.appc.dg.common.impl;

import org.junit.Assert;
import org.junit.Test;

public class TestDataReaderException {

    @Test
    public void testConstructorWithThrowable() throws Exception {
        String message = "testing message";
        Throwable throwable = new Throwable(message);
        DataReaderException dataReaderException = new DataReaderException(throwable);
        Assert.assertEquals(throwable, dataReaderException.getCause());
        Assert.assertTrue(dataReaderException.getLocalizedMessage().contains(message));
        Assert.assertTrue(dataReaderException.getMessage().contains(message));
    }

    @Test
    public void testConstructorWithMessage() throws Exception {
        String message = "testing message";
        DataReaderException dataReaderException = new DataReaderException(message);
        Assert.assertTrue(dataReaderException.getCause() == null);
        Assert.assertEquals(message,dataReaderException.getLocalizedMessage());
        Assert.assertEquals(message, dataReaderException.getMessage());
    }
}
