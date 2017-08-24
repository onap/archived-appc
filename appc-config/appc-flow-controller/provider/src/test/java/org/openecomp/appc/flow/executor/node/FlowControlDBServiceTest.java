package org.openecomp.appc.flow.executor.node;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openecomp.appc.flow.controller.data.Transaction;
import org.openecomp.appc.flow.controller.dbervices.FlowControlDBService;
import org.openecomp.sdnc.sli.SvcLogicContext;
import org.openecomp.sdnc.sli.SvcLogicException;
import org.openecomp.sdnc.sli.SvcLogicResource;
import org.openecomp.sdnc.sli.SvcLogicResource.QueryStatus;
import org.openecomp.sdnc.sli.resource.dblib.DbLibService;
import org.openecomp.sdnc.sli.resource.sql.SqlResource;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.powermock.modules.junit4.PowerMockRunner;

@PrepareForTest({ SqlResource.class, SvcLogicResource.class })
@RunWith(PowerMockRunner.class)
public class FlowControlDBServiceTest {

@Mock
	 SvcLogicResource serviceLogic;
@Mock
SqlResource sqlrs;
@Mock
QueryStatus dblibSvc ;

	private static FlowControlDBService dgGeneralDBService = FlowControlDBService.initialise();

	private Transaction transaction;;

	@Before
	public void setUp() throws Exception {
		serviceLogic = new SqlResource();
	}
	
	
	/*public final void testGetFlowReferenceData() throws Exception {

		SvcLogicContext localContext = new SvcLogicContext();
		FlowControlDBService dgGeneralDBService = FlowControlDBService.initialise();
		PowerMockito.spy(SqlResource.class);

		Map<String, String> inParams = null;
		//PowerMockito.doReturn(dblibSvc).when(SqlResource.class, "query");
		Whitebox.invokeMethod(SqlResource.class, "query",anyString(), anyBoolean(), anyString(), anyString(), anyString(), anyString(), any(SvcLogicContext.class));
		
        dgGeneralDBService.getFlowReferenceData(localContext, inParams, localContext);
		//Assert.assertEquals("SUCCESS", status);
		//Assert.assertNotEquals("Error - while getting FlowReferenceData", "FAILURE", status);

	}*/

	@Test(expected=Exception.class)
	public final void testGetFlowReferenceData() throws Exception {

		SvcLogicContext localContext = new SvcLogicContext();
		FlowControlDBService dgGeneralDBService = FlowControlDBService.initialise();
		PowerMockito.spy(FlowControlDBService.class);
		

		PowerMockito.doReturn(dgGeneralDBService).when(SqlResource.class, "query");
        String status = dgGeneralDBService.getDesignTimeFlowModel(localContext);
		Assert.assertEquals("SUCCESS", status);
		Assert.assertNotEquals("Error - while getting FlowReferenceData", "FAILURE", status);

	}

	
	@Test(expected=Exception.class)
	public final void testGetDesignTimeFlowModel() throws Exception {
		SvcLogicContext localContext = new SvcLogicContext();
		String status = dgGeneralDBService.getDesignTimeFlowModel(localContext) ;
		Assert.assertEquals("SUCCESS", status);
		Assert.assertNotEquals("Error - while getting FlowReferenceData", "FAILURE", status);

		
		

	}

	@Test(expected=Exception.class)
	public final void testLoadSequenceIntoDB() throws SvcLogicException {
		  

		SvcLogicContext localContext = new SvcLogicContext();
		QueryStatus status = dgGeneralDBService.loadSequenceIntoDB(localContext) ;
		Assert.assertEquals("SUCCESS", status);
		Assert.assertNotEquals("Error - while getting FlowReferenceData", "FAILURE", status);
		/*SvcLogicContext ctx = new SvcLogicContext();
		
		if (serviceLogic != null && localContext != null) {
			String queryString = "INSERT INTO " + FlowControllerConstants.DB_REQUEST_ARTIFACTS
					+ " set request_id =  ' kusuma_test' , action = 'Configure', action_level =  'VNF' , vnf_type = 'vComp' , category = 'config_Template'  , artifact_content = '', updated_date = sysdate() ";
			Mockito.when(serviceLogic.save("SQL", false, false, queryString, null, null, localContext))
					.thenReturn(status);
			Assert.assertEquals("SUCCESS", status);
			Assert.assertNotEquals("Error - while getting FlowReferenceData", "FAILURE", status);*/

		

	}

	@Test(expected=Exception.class)
	public final void testPopulateModuleAndRPC() throws SvcLogicException {
		SvcLogicContext localContext = new SvcLogicContext();
		SvcLogicContext ctx = new SvcLogicContext();
		String vnf_type = "test";
	 dgGeneralDBService.populateModuleAndRPC(transaction, vnf_type);;
	

	}

	@Test(expected=Exception.class)
	public final void testGetDependencyInfo() throws SvcLogicException {
		SvcLogicContext localContext = new SvcLogicContext();
		 String status = dgGeneralDBService.getDependencyInfo(localContext);
			Assert.assertEquals("SUCCESS", status);
			Assert.assertNotEquals("Error - while getting FlowReferenceData", "FAILURE", status);
		
	}

	@Test(expected=Exception.class)
	public final void testGetCapabilitiesData() throws SvcLogicException {
		SvcLogicContext localContext = new SvcLogicContext();
		String status = dgGeneralDBService.getCapabilitiesData(localContext);		
			Assert.assertEquals("SUCCESS", status);
			Assert.assertNotEquals("Error - while getting FlowReferenceData", "FAILURE", status);
		
	}

}
