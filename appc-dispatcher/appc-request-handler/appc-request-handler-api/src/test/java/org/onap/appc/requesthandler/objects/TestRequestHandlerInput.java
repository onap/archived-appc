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
import org.onap.appc.domainmodel.lcm.RequestContext;

public class TestRequestHandlerInput {
    private RequestHandlerInput requestHandlerInput;

    @Before
    public void SetUp() {
        requestHandlerInput= new RequestHandlerInput();
    }

    @Test
    public void testGetRpcName() {
        requestHandlerInput.setRpcName("rpcName");
        Assert.assertNotNull(requestHandlerInput.getRpcName());
        Assert.assertEquals("rpcName",requestHandlerInput.getRpcName());
    }

    @Test
    public void testToString_ReturnNonEmptyString() {
        Assert.assertNotEquals(requestHandlerInput.toString(), "");
        Assert.assertNotEquals(requestHandlerInput.toString(), null);
    }

    @Test
    public void testToString_ContainsString() {
        Assert.assertTrue(requestHandlerInput.toString().contains("requestContext"));
    }
    
    @Test
    public void testGetRequestContext() {
        RequestContext requestContext= new RequestContext();
        requestHandlerInput.setRequestContext(requestContext);
        Assert.assertEquals(requestContext, requestHandlerInput.getRequestContext());
    }
}
