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
package org.onap.appc.mdsal.objects;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class TestBundleInfo {

    private BundleInfo bundleInfo;

    @Before
    public void setUp() {
        bundleInfo = new BundleInfo();

    }

    @Test
    public void testGetName() {
        bundleInfo.setName("name");
        assertNotNull(bundleInfo.getName());
        assertEquals(bundleInfo.getName(), "name");
    }

    @Test
    public void testGetDescription() {
        bundleInfo.setDescription("description");
        assertNotNull(bundleInfo.getDescription());
        assertEquals(bundleInfo.getDescription(), "description");
    }

    @Test
    public void testGetVersion() {
        bundleInfo.setVersion(2000);
        assertNotNull(bundleInfo.getVersion());
        assertEquals(bundleInfo.getVersion(), Integer.valueOf(2000));
    }

    @Test
    public void testGetLocation() {
        bundleInfo.setLocation("location");
        assertNotNull(bundleInfo.getLocation());
        assertEquals(bundleInfo.getLocation(), "location");
    }

}
