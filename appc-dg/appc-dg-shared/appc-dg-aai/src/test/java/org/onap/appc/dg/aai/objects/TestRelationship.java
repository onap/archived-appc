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

public class TestRelationship {
    private Relationship relationship;
    private Map<String,String> relationShipDataMap;
    private Map<String,String> relatedProperties;

    @Before
    public void setUp() {
        relationship=new Relationship();
        relationShipDataMap = new HashMap<>();
        relatedProperties = new HashMap<>();
    }

    @Test
    public void testGetRelatedTo() {
        relationship.setRelatedTo("relatedTo");
        Assert.assertNotNull(relationship.getRelatedTo());
        Assert.assertEquals("relatedTo", relationship.getRelatedTo());
    }

    @Test
    public void testGetRelatedLink() {
        relationship.setRelatedLink("relatedLink");
        Assert.assertNotNull(relationship.getRelatedLink());
        Assert.assertEquals("relatedLink", relationship.getRelatedLink());
    }

    @Test
    public void testgetRelationShipDataMap_IsEmpty() {
        Assert.assertTrue(relationship.getRelationShipDataMap().isEmpty());
    }

    @Test
    public void testGetRelationShipDataMap_With_Data() {
        relationShipDataMap.put("1", "A");
        Assert.assertTrue(relationShipDataMap.containsKey("1"));
    }

    @Test
    public void testGetRelationShipDataMap_WithValidKey() {
        relationShipDataMap.put("2", "B");
        Assert.assertEquals("B", relationShipDataMap.get("2"));
    }

    @Test
    public void testgetRelationShipDataMap_WithInValidKey() {
        Assert.assertEquals(null, relationShipDataMap.get("3"));
    }

    @Test
    public void testGetRelationShipDataMap_Size() {
        relationShipDataMap.put("3", "C");
        relationShipDataMap.put("4", "D");
        Assert.assertEquals(2, relationShipDataMap.size());
    }

    @Test
    public void testGetRelatedProperties_IsEmpty() {
        Assert.assertTrue(relationship.getRelatedProperties().isEmpty());
    }

    @Test
    public void testGetRelatedProperties_With_Data() {
        relatedProperties.put("1", "A");
        Assert.assertTrue(relatedProperties.containsKey("1"));
    }

    @Test
    public void testGetRelatedProperties_WithValidKey() {
        relatedProperties.put("2", "B");
        Assert.assertEquals("B",relatedProperties.get("2"));
    }

    @Test
    public void testGetRelatedProperties_WithInValidKey() {
        Assert.assertEquals(null,relatedProperties.get("3"));
    }

    @Test
    public void testGetRelatedProperties_Size() {
        relatedProperties.put("3", "C");
        relatedProperties.put("4", "D");
        Assert.assertEquals(2, relatedProperties.size());
    }
}
