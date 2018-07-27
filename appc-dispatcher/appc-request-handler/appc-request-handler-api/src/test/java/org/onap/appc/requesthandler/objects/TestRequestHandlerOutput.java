/*
* ============LICENSE_START=======================================================
* ONAP : APPC
* ================================================================================
* Copyright 2018 TechMahindra
*=================================================================================
* Modifications Copyright 2018 IBM.
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
package org.onap.appc.requesthandler.objects;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.onap.appc.domainmodel.lcm.ResponseContext;

public class TestRequestHandlerOutput {
    private RequestHandlerOutput requestHandlerOutput;

    @Before
    public void SetUp() {
        requestHandlerOutput= new RequestHandlerOutput();
    }

    @Test
    public void testToString_ReturnNonEmptyString() {
        Assert.assertNotEquals(requestHandlerOutput.toString(), "");
        Assert.assertNotEquals(requestHandlerOutput.toString(), null);
    }

    @Test
    public void testToString_ContainsString() {
        Assert.assertTrue(requestHandlerOutput.toString().contains("responseContext"));
    }
    
    @Test
    public void testGetResponseContext() {
        ResponseContext responseContext= new ResponseContext();
        requestHandlerOutput.setResponseContext(responseContext);
        Assert.assertEquals(responseContext, requestHandlerOutput.getResponseContext());
    }
}
