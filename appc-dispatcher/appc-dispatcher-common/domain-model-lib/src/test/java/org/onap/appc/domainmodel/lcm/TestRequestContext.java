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
package org.onap.appc.domainmodel.lcm;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class TestRequestContext {
    private RequestContext requestContext;
    private Map<String, String> testadditionalContext;

    @Before
    public void setUp() {
        requestContext = new RequestContext();
    }

    @Test
    public void testGetActionLevel_ValidEnumConstant() {
        requestContext.setActionLevel(ActionLevel.VM);
        Assert.assertNotNull(requestContext. getActionLevel());
        Assert.assertEquals(requestContext. getActionLevel(),ActionLevel.VM);
    }

    @Test
    public void testgetAdditionalContext() {
        testadditionalContext=new HashMap<String, String>();
        testadditionalContext.put("A", "a");
        requestContext.setAdditionalContext(testadditionalContext);
        Assert.assertNotNull(requestContext.getAdditionalContext());
        Assert.assertTrue(requestContext.getAdditionalContext().containsKey("A"));
        Assert.assertTrue(requestContext.getAdditionalContext().containsValue("a"));
    }
    
    @Test
    public void testAddKeyValueToAdditionalContext() {
        String key="key1";
        String value="value1";
        requestContext.addKeyValueToAdditionalContext(key, value);
        Map<String, String> additionalContext= requestContext.getAdditionalContext();
        Assert.assertEquals("value1", additionalContext.get("key1"));
    }

    @Test
    public void testGetPayload() {
        requestContext.setPayload("ABC:2000");
        Assert.assertNotNull(requestContext.getPayload());
        Assert.assertEquals(requestContext.getPayload(), "ABC:2000");
    }

    @Test
    public void testToString_ReturnNonEmptyString() {
        assertNotEquals(requestContext.toString(), "");
        assertNotEquals(requestContext.toString(), null);
    }

    @Test
    public void testToString_ContainsString() {
        assertTrue(requestContext.toString().contains("RequestContext{commonHeader"));
    }
    
    @Test
    public void testGetSetCommonHeader()
    {
        CommonHeader commonHeader = new CommonHeader();
        requestContext.setCommonHeader(commonHeader);
        assertEquals(commonHeader, requestContext.getCommonHeader());
    }
    
    @Test
    public void testGetSetActionIdentifiers()
    {
        ActionIdentifiers actionIdentifiers= new ActionIdentifiers();
        requestContext.setActionIdentifiers(actionIdentifiers);
        assertEquals(actionIdentifiers, requestContext.getActionIdentifiers());
    }
    
    @Test
    public void testGetSetAction()
    {
        VNFOperation action= VNFOperation.ActionStatus;
        requestContext.setAction(action);
        assertEquals(action, requestContext.getAction());
    }

}
