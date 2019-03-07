/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 IBM>
 * =============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.encryptiontool.fqdn;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class FqdnListTest {
    private FqdnList fqdnList;

    @Before
    public void setUp() {
        fqdnList = new FqdnList();
    }

    @Test
    public void testDescription() {
        fqdnList.setDescription("Description");
        assertEquals("Description", fqdnList.getDescription());
    }

    @Test
    public void testUsername() {
        fqdnList.setUsername("Username");
        assertEquals("Username", fqdnList.getUsername());
    }

    @Test
    public void testCreateDate() {
        fqdnList.setCreateDate("CreateDate");
        assertEquals("CreateDate", fqdnList.getCreateDate());
    }

    @Test
    public void testModifyUsername() {
        fqdnList.setModifyUsername("ModifyUsername");
        assertEquals("ModifyUsername", fqdnList.getModifyUsername());
    }

    @Test
    public void testModifyDate() {
        fqdnList.setModifyDate("ModifyDate");
        assertEquals("ModifyDate", fqdnList.getModifyDate());
    }

}
