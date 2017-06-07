/*-
 * ============LICENSE_START=======================================================
 * APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Amdocs
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */

package org.openecomp.appc;

import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.openecomp.appc.yang.YANGGenerator;
import org.openecomp.appc.yang.exception.YANGGenerationException;
import org.openecomp.appc.yang.impl.YANGGeneratorFactory;

import java.io.*;

/**
 * The Class TestYANGGenerator - Junit Test Class for all related test cases.
 */
@Ignore
public class TestYANGGenerator {

	private YANGGenerator yangGenerator = YANGGeneratorFactory.getYANGGenerator();
	private static String tosca;
	private static String toscaWithSyntaxError;
	private static String expectedYang;

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	/**
	 * Run before test method.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Before
	public void runBeforeTestMethod() throws IOException {
		tosca= getFileContent("tosca/toscaFile.yml");
		toscaWithSyntaxError = getFileContent("tosca/toscaFileWithSyntaxError.yml");
		expectedYang = getFileContent("yang/expectedYang.yang");
	}

	/**
	 * Test YANG generator for success.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws YANGGenerationException the YANG generation exception
	 */
	@Test
	public void TestYANGGeneratorForSuccess() throws IOException, YANGGenerationException {
		File tempFile = temporaryFolder.newFile("generatedYang.yang");
		OutputStream out = new FileOutputStream(tempFile);
		Assert.assertNotNull(tosca);
		Assert.assertFalse("tosca file is emply or blank", tosca.equals(""));
		yangGenerator.generateYANG("ATD456", tosca, out);
		out.flush();
		out.close();
		String generatedYang = getFileContent(tempFile);
		Assert.assertEquals(expectedYang,generatedYang);
	}

	@Test(expected = YANGGenerationException.class)
	public void testYangGenerationForSyntaxError() throws IOException, YANGGenerationException {
		File tempFile = temporaryFolder.newFile("generatedYang.yang");
		OutputStream out = new FileOutputStream(tempFile);
		yangGenerator.generateYANG("ATD456",toscaWithSyntaxError,out);
	}


	/**
	 * Test for Yang Generator which generates YANG that is not matching with expected YANG.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws YANGGenerationException - the YANG generation exception
	 */
	@Test
	public void unmatchedYangGenerationTest() throws IOException, YANGGenerationException {
		File tempFile = temporaryFolder.newFile("generatedYang.yang");
		OutputStream out = new FileOutputStream(tempFile);
		yangGenerator.generateYANG("112476", tosca, out);
		out.flush();
		out.close();
		String generatedYang = getFileContent(tempFile);
		Assert.assertNotSame(expectedYang, generatedYang);

	}

    /**
     * Yang generation test for empty tosca input.
     *
     * @throws YANGGenerationException the YANG generation exception
     */
    @Test(expected = YANGGenerationException.class)
    public void YangGenerationTestForEmptyUniqueIDInput() throws IOException, YANGGenerationException {
//        OutputStream out = new FileOutputStream(classLoader.getResource("yang/generatedYang.yang").getFile());
		File tempFile = temporaryFolder.newFile("generatedYang.yang");
		OutputStream out = new FileOutputStream(tempFile);
        yangGenerator.generateYANG("", tosca, out);
    }

    /**
     * Yang generation test for empty tosca input.
     *
     * @throws YANGGenerationException the YANG generation exception
     */
    @Test(expected = YANGGenerationException.class)
    public void YangGenerationTestForUnSupportedType() throws IOException, YANGGenerationException {
        tosca= getFileContent("tosca/toscaFileWithUnsupportedTypes.yml");
		File tempFile = temporaryFolder.newFile("generatedYang.yang");
		OutputStream out = new FileOutputStream(tempFile);
        yangGenerator.generateYANG("", tosca, out);
    }

	/**
	 * Yang generation test for empty tosca input.
	 *
	 * @throws YANGGenerationException the YANG generation exception
	 */
	@Test(expected = YANGGenerationException.class)
	public void YangGenerationTestForEmptyToscaInput() throws IOException, YANGGenerationException {
		File tempFile = temporaryFolder.newFile("generatedYang.yang");
		OutputStream out = new FileOutputStream(tempFile);
		yangGenerator.generateYANG("1111", "", out);
	}

	/**
	 * YANG generation test with invalid method arguments.
	 *
	 * @throws YANGGenerationException the YANG generation exception
	 */
	@Test(expected = YANGGenerationException.class)
	public void YANGGenerationTestWithInvalidMethodArguments() throws YANGGenerationException {	
		yangGenerator.generateYANG("112476", "ToscaSAMPLE", null);
	}

	@Test(expected = YANGGenerationException.class)
	public void YANGGenerationTestWithIOException() throws IOException, YANGGenerationException {
		File tempFile = temporaryFolder.newFile("generatedYang.yang");
		OutputStream out = new FileOutputStream(tempFile);
		out.flush();
		out.close();
		yangGenerator.generateYANG("1111", tosca, out);
	}


	private String getFileContent(String fileName) throws IOException
	{
		ClassLoader classLoader = new TestYANGGenerator().getClass().getClassLoader();
		InputStream is = new FileInputStream(classLoader.getResource(fileName).getFile());
		BufferedReader buf = new BufferedReader(new InputStreamReader(is));
		String line = buf.readLine();
		StringBuilder sb = new StringBuilder();

		while (line != null) {
			sb.append(line).append("\n");
			line = buf.readLine();
		}
		String fileString = sb.toString();
		is.close();
		return fileString;
	}

	private String getFileContent(File file) throws IOException
	{
		InputStream is = new FileInputStream(file);
		BufferedReader buf = new BufferedReader(new InputStreamReader(is));
		String line = buf.readLine();
		StringBuilder sb = new StringBuilder();

		while (line != null) {
			sb.append(line).append("\n");
			line = buf.readLine();
		}
		String fileString = sb.toString();
		is.close();
		return fileString;
	}

}
