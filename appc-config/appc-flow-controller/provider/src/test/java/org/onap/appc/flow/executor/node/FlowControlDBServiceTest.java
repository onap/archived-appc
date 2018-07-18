/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
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

package org.onap.appc.flow.executor.node;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.onap.appc.flow.controller.data.ActionIdentifier;
import org.onap.appc.flow.controller.data.Transaction;
import org.onap.appc.flow.controller.dbervices.FlowControlDBService;
import org.onap.appc.flow.controller.utils.FlowControllerConstants;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource.QueryStatus;
import org.onap.ccsdk.sli.core.dblib.DbLibService;
import org.onap.ccsdk.sli.adaptors.resource.sql.SqlResource;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.powermock.modules.junit4.PowerMockRunner;


public class FlowControlDBServiceTest {
	/*
	SvcLogicResource serviceLogic;
	
	SqlResource sqlrs;

	private static FlowControlDBService dgGeneralDBService = FlowControlDBService.initialise();

	// private Transaction transaction;

	@Before
	public void setUp() throws Exception {
		serviceLogic = new SqlResource();
	}
*/
	/*
	 * public final void testGetFlowReferenceData() throws Exception {
	 * 
	 * SvcLogicContext localContext = new SvcLogicContext();
	 * FlowControlDBService dgGeneralDBService =
	 * FlowControlDBService.initialise(); PowerMockito.spy(SqlResource.class);
	 * 
	 * Map<String, String> inParams = null;
	 * //PowerMockito.doReturn(dblibSvc).when(SqlResource.class, "query");
	 * Whitebox.invokeMethod(SqlResource.class, "query",anyString(),
	 * anyBoolean(), anyString(), anyString(), anyString(), anyString(),
	 * any(SvcLogicContext.class));
	 * 
	 * dgGeneralDBService.getFlowReferenceData(localContext, inParams,
	 * localContext); //Assert.assertEquals("SUCCESS", status);
	 * //Assert.assertNotEquals("Error - while getting FlowReferenceData",
	 * "FAILURE", status);
	 * 
	 * }
	 */
	/*
	 * @Test(expected = Exception.class) public final void
	 * testGetFlowReferenceData() throws Exception {
	 * 
	 * SvcLogicContext localContext = new SvcLogicContext();
	 * FlowControlDBService dgGeneralDBService =
	 * FlowControlDBService.initialise();
	 * PowerMockito.spy(FlowControlDBService.class);
	 * 
	 * PowerMockito.doReturn(dgGeneralDBService).when(SqlResource.class,
	 * "query"); String status =
	 * dgGeneralDBService.getDesignTimeFlowModel(localContext);
	 * Assert.assertEquals("SUCCESS", status);
	 * Assert.assertNotEquals("Error - while getting FlowReferenceData",
	 * "FAILURE", status);
	 * 
	 * }
	 */

	/*
	 * @Ignore("Test is taking 60 seconds")
	 * 
	 * @Test(expected = Exception.class) public final void
	 * testGetDesignTimeFlowModel() throws Exception { SvcLogicContext
	 * localContext = new SvcLogicContext(); String status =
	 * dgGeneralDBService.getDesignTimeFlowModel(localContext);
	 * Assert.assertEquals("SUCCESS", status);
	 * Assert.assertNotEquals("Error - while getting FlowReferenceData",
	 * "FAILURE", status);
	 * 
	 * }
	 */
	/*
	
	@Ignore("Test is taking 60 seconds")
	@Test(expected = Exception.class)
	public final void testLoadSequenceIntoDB() throws SvcLogicException {

		SvcLogicContext localContext = new SvcLogicContext();
		QueryStatus status = dgGeneralDBService.loadSequenceIntoDB(localContext);
		Assert.assertEquals("SUCCESS", status);
		Assert.assertNotEquals("Error - while getting FlowReferenceData", "FAILURE", status);
		 * SvcLogicContext ctx = new SvcLogicContext();
		 * 
		 * if (serviceLogic != null && localContext != null) { String
		 * queryString = "INSERT INTO " +
		 * FlowControllerConstants.DB_REQUEST_ARTIFACTS +
		 * " set request_id =  ' kusuma_test' , action = 'Configure', action_level =  'VNF' , vnf_type = 'vComp' , category = 'config_Template'  , artifact_content = '', updated_date = sysdate() "
		 * ; Mockito.when(serviceLogic.save("SQL", false, false, queryString,
		 * null, null, localContext)) .thenReturn(status);
		 * Assert.assertEquals("SUCCESS", status);
		 * Assert.assertNotEquals("Error - while getting FlowReferenceData",
		 * "FAILURE", status);
		 */
	/*
	}
	
	 * @Ignore
	 * 
	 * @Test(expected = Exception.class) public final void
	 * testPopulateModuleAndRPC() throws Exception { SvcLogicContext
	 * localContext = new SvcLogicContext(); SvcLogicContext ctx = new
	 * SvcLogicContext(); String vnf_type = "test";
	 * dgGeneralDBService.populateModuleAndRPC(transaction, vnf_type);
	 * 
	 * }
	 * 
	 * 
	 * @Ignore("Test is taking 60 seconds")
	 * 
	 * @Test(expected=Exception.class) public final void testGetDependencyInfo()
	 * throws SvcLogicException { SvcLogicContext localContext = new
	 * SvcLogicContext(); String status =
	 * dgGeneralDBService.getDependencyInfo(localContext);
	 * Assert.assertEquals("SUCCESS", status);
	 * Assert.assertNotEquals("Error - while getting FlowReferenceData",
	 * "FAILURE", status);
	 * 
	 * }
	 * 
	 * @Ignore("Test is taking 60 seconds")
	 * 
	 * @Test(expected=Exception.class) public final void
	 * testGetCapabilitiesData() throws SvcLogicException { SvcLogicContext
	 * localContext = new SvcLogicContext(); String status =
	 * dgGeneralDBService.getCapabilitiesData(localContext);
	 * Assert.assertEquals("SUCCESS", status);
	 * Assert.assertNotEquals("Error - while getting FlowReferenceData",
	 * "FAILURE", status);
	 * 
	 * }
	 */

	@Test
	public final void testGetCapabilitiesData1() throws Exception {
		MockDBService dbService = MockDBService.initialise();
		SvcLogicContext ctx = new SvcLogicContext();
		ctx.setAttribute("test", "test");
		String status = dbService.getCapabilitiesData(ctx);
		assertEquals("TestArtifactContent", status);

	}

	@Test
	public final void testGetDependencyInfo() throws Exception {
		MockDBService dbService = MockDBService.initialise();
		SvcLogicContext ctx = new SvcLogicContext();
		String status = dbService.getDependencyInfo(ctx);
		assertEquals("TestArtifactContent", status);
	}

	@Test
	public final void testGetDesignTimeFlowModel() throws Exception {
		MockDBService dbService = MockDBService.initialise();
		SvcLogicContext ctx = new SvcLogicContext();
		String status = dbService.getDesignTimeFlowModel(ctx);
		assertEquals("TestArtifactContent", status);
	}

	@Test
	public final void testGetFlowReferenceData() throws Exception {
		MockDBService dbService = MockDBService.initialise();
		SvcLogicContext ctx = new SvcLogicContext();
		Map<String, String> inParams = null;
		dbService.getFlowReferenceData(ctx, inParams, ctx);
		assertEquals("TestSequence", ctx.getAttribute("SEQUENCE_TYPE"));
	}

	@Test
	public final void testLoadSequenceIntoDB1() throws Exception {
		MockDBService dbService = MockDBService.initialise();
		SvcLogicContext ctx = new SvcLogicContext();
		QueryStatus result = dbService.loadSequenceIntoDB(ctx);
		assertEquals("SUCCESS", result.toString());
	}

	@Test
	public final void testPopulateModuleAndRPC() throws Exception {
		MockDBService dbService = MockDBService.initialise();
		Transaction transaction = new Transaction();
		String vnfType = "TestVNF";
		dbService.populateModuleAndRPC(transaction, vnfType);
		assertEquals("TestModule", transaction.getExecutionModule());
	}

	@Test
	public void testHasSingleProtocol() throws Exception {
		MockDBService dbService = MockDBService.initialise();
		SvcLogicContext ctx = new SvcLogicContext();
		String vnfTType = "TestVNF";
		String fn = "test";
		Transaction transaction = new Transaction();
		boolean result = Whitebox.invokeMethod(dbService, "hasSingleProtocol", transaction, vnfTType, fn, ctx);
	    assertEquals(true, result);
	}
}
