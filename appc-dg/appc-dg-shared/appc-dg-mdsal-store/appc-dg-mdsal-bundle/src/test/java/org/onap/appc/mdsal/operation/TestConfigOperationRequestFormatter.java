package org.onap.appc.mdsal.operation;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class TestConfigOperationRequestFormatter {
	ConfigOperationRequestFormatter requestFormatter;

	@Before
	public void setUp() {
		requestFormatter = new ConfigOperationRequestFormatter();
	}

	@Test
	public void testBuildPath_ValidPath() {
		String validBuildPath = "/restconf/config/appc-dg-mdsal-store:appc-dg-mdsal-bundle/appc-dg-mdsal-bundle/appc-dg-mdsal-model/";
		String module="appc-dg-mdsal-store";
		String containerName = "appc-dg-mdsal-bundle";
		String[] subModules = { "appc-dg-mdsal-bundle", "appc-dg-mdsal-model" };

		if (((module == null|module.equals("")  ) || (containerName.equals("") | containerName == null)
				|| (subModules.equals("") | subModules == null|subModules.length==0))) {
			fail("Missing required values for :module/containerName/subModules");
		}

		assertEquals(validBuildPath, requestFormatter.buildPath(module, containerName, subModules));
	}

	@Test
	public void testBuildPath_InvalidPath() {
		String inValidBuildPath = "/restcon/config/appc-dg-mdsal-store appc-dg-mdsal-bundle/appc-dg-mdsal-bundle/appc-dg-mdsal-model/";
		String module="appc-dg-mdsal-store";
		String containerName = "appc-dg-mdsal-bundle";
		String[] subModules = { "appc-dg-mdsal-bundle", "appc-dg-mdsal-model" };
		assertNotEquals(inValidBuildPath, requestFormatter.buildPath(module, containerName, subModules));
		
	}
}
