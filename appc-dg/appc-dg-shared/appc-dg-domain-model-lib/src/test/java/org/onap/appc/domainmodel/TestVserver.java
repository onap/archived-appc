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
package org.onap.appc.domainmodel;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class TestVserver {
        private Vserver vserver;

        @Before
        public void SetUp() {
            vserver=new Vserver();
           
        }

        @Test
        public void testGetUrl() {
            vserver.setUrl("/ABC.com");
            assertNotNull(vserver.getUrl());
            assertEquals(vserver.getUrl(),"/ABC.com");
        }

        @Test
        public void testGetTenantId() {
            vserver.setTenantId("A00");
            assertNotNull(vserver.getTenantId());
            assertEquals(vserver.getTenantId(),"A00");
        }

        @Test
        public void testGetId() {
            vserver.setId("1");
            assertNotNull(vserver.getId());
            assertEquals(vserver.getId(),"1");
        }

        @Test
        public void testGetRelatedLink() {
            vserver.setRelatedLink("one");
            assertNotNull(vserver.getRelatedLink());
            assertEquals(vserver.getRelatedLink(),"one");
        }

        @Test
        public void testGetName() {
            vserver.setName("APPC");
            assertNotNull(vserver.getName());
            assertEquals(vserver.getName(),"APPC");
        }

        @Test
        public void testToString_ReturnNonEmptyString() {
            assertNotEquals(vserver.toString(), "");
            assertNotEquals(vserver.toString(), null);
        }

        @Test
        public void testToString_ContainsString() {
            assertTrue(vserver.toString().contains("Vserver"));
        }

}
