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
