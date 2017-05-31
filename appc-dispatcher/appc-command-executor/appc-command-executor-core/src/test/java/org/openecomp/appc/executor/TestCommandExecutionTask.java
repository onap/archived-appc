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

package org.openecomp.appc.executor;
/**
 *
 */

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openecomp.appc.domainmodel.lcm.*;
import org.openecomp.appc.domainmodel.lcm.Flags.Mode;
import org.openecomp.appc.executor.impl.CommandTask;
import org.openecomp.appc.executor.impl.CommandTaskFactory;
import org.openecomp.appc.executor.impl.LCMCommandTask;
import org.openecomp.appc.executor.impl.LCMReadonlyCommandTask;
import org.openecomp.appc.executor.objects.*;
import org.openecomp.appc.lifecyclemanager.LifecycleManager;
import org.openecomp.appc.requesthandler.RequestHandler;
import org.openecomp.appc.workflow.WorkFlowManager;
import org.openecomp.appc.workflow.objects.WorkflowRequest;
import org.openecomp.appc.workflow.objects.WorkflowResponse;
import org.openecomp.sdnc.sli.SvcLogicContext;
import org.openecomp.sdnc.sli.SvcLogicException;
import org.openecomp.sdnc.sli.SvcLogicResource;
import org.openecomp.sdnc.sli.aai.AAIService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.*;



@RunWith(PowerMockRunner.class)
@PrepareForTest( {FrameworkUtil.class, CommandTask.class, LCMCommandTask.class})
public class TestCommandExecutionTask {

	private static final String TTL_FLAG= "TTL";
	private static final String API_VERSION= "2.0.0";
	private static final String ORIGINATOR_ID= "1";
	private CommandTaskFactory factory ;

	private RequestHandler requestHandler;
	private WorkFlowManager workflowManager;
	private AAIService aaiService;
	private LifecycleManager lifecyclemanager;

	private final BundleContext bundleContext=Mockito.mock(BundleContext.class);
	private final Bundle bundleService=Mockito.mock(Bundle.class);
	private final ServiceReference sref=Mockito.mock(ServiceReference.class);

	@Before
	public void init() throws SvcLogicException {

		// ***
		AAIService aaiService = Mockito.mock(AAIService.class);;
		PowerMockito.mockStatic(FrameworkUtil.class);
		PowerMockito.when(FrameworkUtil.getBundle(AAIService.class)).thenReturn(bundleService);
		PowerMockito.when(bundleService.getBundleContext()).thenReturn(bundleContext);
		PowerMockito.when(bundleContext.getServiceReference(AAIService.class.getName())).thenReturn(sref);
		PowerMockito.when(bundleContext.<AAIService>getService(sref)).thenReturn(aaiService);
		PowerMockito.when(aaiService.query(anyString(),anyBoolean(),anyString(),anyString(),anyString(),anyString(),(SvcLogicContext)anyObject())).thenAnswer(new Answer<SvcLogicResource.QueryStatus>() {
			@Override
			public SvcLogicResource.QueryStatus answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				SvcLogicContext ctx =(SvcLogicContext)args[6];
				String prefix = (String)args[4];
				String key = (String)args[3];
				if(key.contains("'28'")){
					return  SvcLogicResource.QueryStatus.FAILURE ;
				}else if ( key.contains("'8'")) {
					return  SvcLogicResource.QueryStatus.NOT_FOUND ;
				}else {
					ctx.setAttribute(prefix + ".vnf-type", "FIREWALL");
					ctx.setAttribute(prefix + ".orchestration-status", "INSTANTIATED");
				}
				return  SvcLogicResource.QueryStatus.SUCCESS ;
			}
		});
		PowerMockito.when(aaiService.update(anyString(),anyString(),(Map)anyObject(),anyString(),(SvcLogicContext)anyObject())).thenReturn(SvcLogicResource.QueryStatus.SUCCESS);

		requestHandler =  Mockito.mock(RequestHandler.class);
		workflowManager = Mockito.mock(WorkFlowManager.class);
		lifecyclemanager = Mockito.mock(LifecycleManager.class );

		factory = new CommandTaskFactory();
		factory.setLifecyclemanager(lifecyclemanager);
		factory.setWorkflowManager(workflowManager);
		factory.setRequestHandler(requestHandler);
		Mockito.when(workflowManager.executeWorkflow((WorkflowRequest)anyObject())).thenReturn(getWorkflowResponse () );
	}


	@Test
	public void testFactory(){
		CommandTask task = factory.getExecutionTask("Configure", null);
		assertEquals(LCMCommandTask.class,task.getClass() );
		task = factory.getExecutionTask("Sync", null);
		assertEquals(LCMReadonlyCommandTask.class,task.getClass() );

	}



	@Test
	public void testOnRequestCompletion(){
		Mockito.doNothing().when(requestHandler).onRequestTTLEnd((RuntimeContext) anyObject(),anyBoolean());
		RuntimeContext request = pouplateCommandExecutorInput("FIREWALL", 30, "1.0", Instant.now(), API_VERSION, "11", ORIGINATOR_ID, "", VNFOperation.Configure, "1", "");
		CommandResponse response = getCommandResponse(VNFOperation.Configure, true, "11", "","1");
        LCMCommandTask executionTask = new LCMCommandTask(request, requestHandler,workflowManager,lifecyclemanager);
		executionTask.onRequestCompletion(response);
	}

	@Test
	public void testRunGetConfig(){
		    RuntimeContext request = pouplateCommandExecutorInput("FIREWALL", 30, "1.0", Instant.now(), API_VERSION, "11", ORIGINATOR_ID, "", VNFOperation.Sync, "1", "");
        LCMReadonlyCommandTask readonlyCommandTask = new LCMReadonlyCommandTask(request, requestHandler,workflowManager);
		readonlyCommandTask.run();
	}

	@Test
	public void testRun(){
	    RuntimeContext request = pouplateCommandExecutorInput("FIREWALL", 30, "1.0", Instant.now(), API_VERSION, "11", ORIGINATOR_ID, "", VNFOperation.Sync, "1", "");
		LCMCommandTask executionTask = new LCMCommandTask(request, requestHandler,workflowManager,lifecyclemanager);
		executionTask.run();
	}

	@Test
	public void testRunNegative(){
	    RuntimeContext request = pouplateCommandExecutorInput("FIREWALL", 30, "1.0", Instant.now(), API_VERSION, "11", ORIGINATOR_ID, "", VNFOperation.Sync, "1", "");
        LCMCommandTask executionTask = new LCMCommandTask(request, requestHandler,workflowManager,lifecyclemanager);
		executionTask.run();
	}


	CommandResponse getCommandResponse(VNFOperation action , boolean success, String responseId, String payload, String vnfId){
		RuntimeContext runtimeContext = new RuntimeContext();
		ResponseContext responseContext = new ResponseContext();
		runtimeContext.setResponseContext(responseContext);
		RequestContext requestContext = new RequestContext();
		runtimeContext.setRequestContext(requestContext);
		CommonHeader commonHeader = new CommonHeader();
		requestContext.setCommonHeader(commonHeader);
		responseContext.setCommonHeader(commonHeader);
		commonHeader.setFlags(new Flags(null, false, 0));
		ActionIdentifiers actionIdentifiers = new ActionIdentifiers();
		requestContext.setActionIdentifiers(actionIdentifiers);
		VNFContext vnfContext = new VNFContext();
		runtimeContext.setVnfContext(vnfContext);
		requestContext.setAction(action);
		runtimeContext.setRpcName(action.name().toLowerCase());
		commonHeader.setApiVer(API_VERSION);
		responseContext.setStatus(new Status(100, null));
		commonHeader.setRequestId(responseId);
		responseContext.setPayload(payload);
		commonHeader.setTimestamp(Instant.now());
		vnfContext.setId(vnfId);
        return new CommandResponse(runtimeContext);
	}



	@Test
	public void testPositiveFlow_configure()  {

		String requestId = "1";

		RuntimeContext commandExecutorInput = pouplateCommandExecutorInput("FIREWALL",30, "1.0", Instant.now(), API_VERSION, requestId, ORIGINATOR_ID, "", VNFOperation.Configure, "33", "");
	}


	private Map<String,Object> setTTLInFlags( String value){
		Map<String,Object> flags = new HashMap<String,Object>();
		if( value != null || !("".equalsIgnoreCase(value))){
			flags.put(TTL_FLAG, value);
		}
		return flags;
	}


	public WorkflowResponse getWorkflowResponse (){
		WorkflowResponse wfResponse = new WorkflowResponse();
		ResponseContext responseContext = createResponseContextWithSuObjects();
		wfResponse.setResponseContext(responseContext);
		responseContext.setPayload("");
		wfResponse.getResponseContext().setStatus(new Status(100, null));
		return wfResponse;
	}

	private RuntimeContext pouplateCommandExecutorInput(String vnfType, int ttl, String vnfVersion, Instant timeStamp, String apiVersion, String requestId, String originatorID, String subRequestID, VNFOperation action, String vnfId , String payload){
		RuntimeContext commandExecutorInput = createCommandExecutorInputWithSubObjects();
		RequestContext requestContext = commandExecutorInput.getRequestContext();
		ResponseContext responseContext = createResponseContextWithSuObjects();
		commandExecutorInput.setResponseContext(responseContext);

		requestContext.getCommonHeader().setFlags(new Flags(null, false, ttl));
		requestContext.getCommonHeader().setApiVer(apiVersion);
		requestContext.getCommonHeader().setTimestamp(timeStamp);
		requestContext.getCommonHeader().setRequestId(requestId);
		requestContext.getCommonHeader().setSubRequestId(subRequestID);
		requestContext.getCommonHeader().setOriginatorId(originatorID);
		requestContext.setAction(action);
		requestContext.setPayload(payload);
		requestContext.getActionIdentifiers().setVnfId(vnfId);
		VNFContext vnfContext = commandExecutorInput.getVnfContext();
		vnfContext.setType(vnfType);
		vnfContext.setId(vnfId);
		vnfContext.setVersion(vnfVersion);
		return commandExecutorInput;
	}

	private RuntimeContext createCommandExecutorInputWithSubObjects() {
		return createRuntimeContextWithSubObjects();
	}

	private RuntimeContext createRuntimeContextWithSubObjects() {
		RuntimeContext runtimeContext = new RuntimeContext();
		RequestContext requestContext = new RequestContext();
		runtimeContext.setRequestContext(requestContext);
		CommonHeader commonHeader = new CommonHeader();
		requestContext.setCommonHeader(commonHeader);
		commonHeader.setFlags(new Flags(null, false, 0));
		ActionIdentifiers actionIdentifiers = new ActionIdentifiers();
		requestContext.setActionIdentifiers(actionIdentifiers);
		VNFContext vnfContext = new VNFContext();
		runtimeContext.setVnfContext(vnfContext);
		return runtimeContext;

	}

	private ResponseContext createResponseContextWithSuObjects(){
		ResponseContext responseContext = new ResponseContext();
		CommonHeader commonHeader = new CommonHeader();
		responseContext.setCommonHeader(commonHeader);
		responseContext.setStatus(new Status(0, null));
		commonHeader.setFlags(new Flags(null, false, 0));
		return responseContext;
	}

}

