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
package org.onap.appc.yang.exception;

import org.junit.Assert;
import org.junit.Test;

public class TestYANGGenerationException {

    @Test
    public void testConstructorWithMessageAndThrowable() throws Exception {
        String message = "testing message";
        String tMessage = "throwable message";
        Throwable throwable = new Throwable(tMessage);
        YANGGenerationException yANGGenerationException =new YANGGenerationException(message, throwable);
        Assert.assertEquals(throwable, yANGGenerationException.getCause());
        Assert.assertTrue(yANGGenerationException.getLocalizedMessage().contains(message));
        Assert.assertTrue(yANGGenerationException.getMessage().contains(message));
    }

    @Test
    public void testConstructorWithMessaqge() throws Exception {
        String message = "testing message";
        YANGGenerationException yANGGenerationException = new YANGGenerationException(message);
        Assert.assertTrue(yANGGenerationException.getCause() == null);
        Assert.assertEquals(message, yANGGenerationException.getLocalizedMessage());
        Assert.assertEquals(message, yANGGenerationException.getMessage());
    }
}
