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
package org.onap.appc.mdsal.operation;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class TestConfigOperationRequestFormatter {
    private ConfigOperationRequestFormatter requestFormatter;

    @Before
    public void setUp() {
        requestFormatter = new ConfigOperationRequestFormatter();
    }

    @Test
    public void testBuildPath_ValidPath() {
        String validBuildPath = "/restconf/config/appc-dg-mdsal-store:appc-dg-mdsal-bundle/appc-dg-mdsal-bundle/appc-dg-mdsal-model/";
        String module = "appc-dg-mdsal-store";
        String containerName = "appc-dg-mdsal-bundle";
        String[] subModules = { "appc-dg-mdsal-bundle", "appc-dg-mdsal-model" };
        assertEquals(validBuildPath, requestFormatter.buildPath(module, containerName, subModules));
    }
    @Test
    public void testBuildPath_One_SubModule() {
        String validBuildPath = "/restconf/config/appc-dg-mdsal-store:appc-dg-mdsal-bundle/appc-dg-mdsal-bundle/";
        String module = "appc-dg-mdsal-store";
        String containerName = "appc-dg-mdsal-bundle";
        String[] subModules = { "appc-dg-mdsal-bundle"};
        assertEquals(validBuildPath, requestFormatter.buildPath(module, containerName, subModules));
    }
    @Test
    public void testBuildPath_Zero_SubModule() {
        String validBuildPathWithoutSubModule = "/restconf/config/appc-dg-mdsal-store:appc-dg-mdsal-bundle/";
        String module = "appc-dg-mdsal-store";
        String containerName = "appc-dg-mdsal-bundle";
        assertEquals(validBuildPathWithoutSubModule, requestFormatter.buildPath(module, containerName));
    }
    @Test
    public void testBuildPath_InvalidPath() {
        String inValidBuildPath = "/restcon/config/appc-dg-mdsal-storeappc-dg-mdsal-bundle/appc-dg-mdsal-bundle/appc-dg-mdsal-model/";
        String module = "appc-dg-mdsal-store";
        String containerName = "appc-dg-mdsal-bundle";
        String[] subModules = { "appc-dg-mdsal-bundle", "appc-dg-mdsal-model" };
        assertNotEquals(inValidBuildPath, requestFormatter.buildPath(module, containerName,subModules));

    }
}
