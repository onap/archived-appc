/*
* ============LICENSE_START=======================================================
* ONAP : APPC
* ================================================================================
* Copyright 2018 TechMahindra
*=================================================================================
* Modifications Copyright 2019 IBM.
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
package org.onap.appc.domainmodel.lcm;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestResponseContext {
    private ResponseContext responseContext;
    private Map<String, String> testadditionalContext;
    private CommonHeader commonHeader;
    private Status status;

    @Before
    public void setUp() {
        responseContext = new ResponseContext();
        commonHeader = new CommonHeader();
        commonHeader.setOriginatorId("originatorId");
        status = new Status();
        status.setCode(0);
    }

    @Test
    public void testgetAdditionalContext() {
        testadditionalContext = new HashMap<String, String>();
        testadditionalContext.put("A", "a");
        responseContext.setAdditionalContext(testadditionalContext);
        Assert.assertNotNull(responseContext.getAdditionalContext());
        Assert.assertTrue(responseContext.getAdditionalContext().containsKey("A"));
        Assert.assertTrue(responseContext.getAdditionalContext().containsValue("a"));
    }

    @Test
    public void testGetPayload() {
        responseContext.setPayload("ABC:2000");
        Assert.assertNotNull(responseContext.getPayload());
        Assert.assertSame("ABC:2000",responseContext.getPayload());
    }

    @Test
    public void testToString_ReturnNonEmptyString() {
        Assert.assertNotEquals("",responseContext.toString());
        Assert.assertNotEquals(null,responseContext.toString());
    }

    @Test
    public void testToString_ContainsString() {
        Assert.assertTrue(responseContext.toString().contains("ResponseContext{commonHeader"));
    }

    @Test
    public void testAddKeyValueToAdditionalContext() {
        String key = "key1";
        String value = "value1";
        responseContext.addKeyValueToAdditionalContext(key, value);
        Map<String, String> additionalContext = responseContext.getAdditionalContext();
        Assert.assertSame("value1", additionalContext.get("key1"));
    }

    @Test
    public void testGetPayloadObject() {
        responseContext.setPayloadObject("ABC:2000");
        Assert.assertSame("ABC:2000", responseContext.getPayloadObject());
    }

    @Test
    public void testGetCommonHeader() {
        responseContext.setCommonHeader(commonHeader);
        Assert.assertSame("originatorId", responseContext.getCommonHeader().getOriginatorId());
    }

    @Test
    public void testGetStatus() {
        responseContext.setStatus(status);
        Assert.assertSame(0, responseContext.getStatus().getCode());
    }

}
