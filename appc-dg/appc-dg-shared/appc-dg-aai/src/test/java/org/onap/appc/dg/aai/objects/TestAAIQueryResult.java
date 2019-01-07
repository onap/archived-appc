/*
* ============LICENSE_START=======================================================
* ONAP : APPC
* ================================================================================
* Copyright 2018 TechMahindra
* ================================================================================
* Modifications Copyright (C) 2019 Ericsson
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

package org.onap.appc.dg.aai.objects;

import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestAAIQueryResult {
    private AAIQueryResult aaiQueryResult;
    private Map<String,String> additionProperties;


    @Before
    public void setUp() {
        aaiQueryResult=new AAIQueryResult();
        additionProperties = new HashMap<>();
    }

    @Test
    public void testGetRelationshipList() {
        Assert.assertTrue(aaiQueryResult. getRelationshipList().isEmpty());
    }

    @Test
    public void testGetRelationshipList_With_Data() {
        Relationship r1=new Relationship();
        r1.setRelatedLink("relatedLink");
        r1.setRelatedTo("relatedTo");
        r1.getRelatedProperties().put("1", "A");
        r1.getRelationShipDataMap().put("B", "b");
        aaiQueryResult.getRelationshipList().add(r1);
        Assert.assertEquals(1,aaiQueryResult.getRelationshipList().size());
    }

    @Test
    public void testGetAdditionProperties_IsEmpty() {
        Assert.assertTrue(aaiQueryResult.getAdditionProperties().isEmpty());
    }

    @Test
    public void testGetAdditionProperties_With_Data() {
        additionProperties.put("1", "A");
        Assert.assertTrue(additionProperties.containsKey("1"));
    }

    @Test
    public void testGetAdditionProperties_WithValidKey() {
        additionProperties.put("2", "B");
        Assert.assertEquals("B",additionProperties.get("2"));
    }

    @Test
    public void testGetAdditionProperties_WithInValidKey() {
        Assert.assertEquals(null,additionProperties.get("3"));
    }

    @Test
    public void testGetAdditionProperties_Size() {
        additionProperties.put("3", "C");
        additionProperties.put("4", "D");
        Assert.assertEquals(2, additionProperties.size());
    }
}
