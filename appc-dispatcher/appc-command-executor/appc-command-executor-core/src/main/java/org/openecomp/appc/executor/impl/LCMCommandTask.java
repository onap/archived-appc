/*-
 * ============LICENSE_START=======================================================
 * openECOMP : APP-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 						reserved.
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
 */

package org.openecomp.appc.executor.impl;


import org.apache.commons.lang3.StringUtils;
import org.openecomp.appc.domainmodel.lcm.CommonHeader;
import org.openecomp.appc.domainmodel.lcm.Status;
import org.openecomp.appc.domainmodel.lcm.VNFOperation;
import org.openecomp.appc.executor.UnstableVNFException;
import org.openecomp.appc.executor.objects.CommandExecutorInput;
import org.openecomp.appc.executor.objects.CommandResponse;
import org.openecomp.appc.executor.objects.LCMCommandStatus;
import org.openecomp.appc.executor.objects.Params;
import org.openecomp.appc.executor.objects.UniqueRequestIdentifier;
import org.openecomp.appc.lifecyclemanager.LifecycleManager;
import org.openecomp.appc.lifecyclemanager.objects.LifecycleException;
import org.openecomp.appc.lifecyclemanager.objects.NoTransitionDefinedException;
import org.openecomp.appc.lifecyclemanager.objects.VNFOperationOutcome;
import org.openecomp.appc.requesthandler.RequestHandler;
import org.openecomp.appc.workflow.WorkFlowManager;
import org.openecomp.appc.workflow.objects.WorkflowResponse;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.openecomp.sdnc.sli.SvcLogicContext;
import org.openecomp.sdnc.sli.SvcLogicException;
import org.openecomp.sdnc.sli.SvcLogicResource;
import org.openecomp.sdnc.sli.aai.AAIService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import java.util.HashMap;
import java.util.Map;


public class LCMCommandTask extends CommandTask {

	private AAIService aaiService;
	private LifecycleManager lifecyclemanager;

	private static final EELFLogger logger = EELFManager.getInstance().getLogger(LCMCommandTask.class);

	public LCMCommandTask(RequestHandler requestHandler, WorkFlowManager workflowManager,
						  LifecycleManager lifecyclemanager){
		setRequestHandler(requestHandler);
		setWorkflowManager(workflowManager);
		setLifecyclemanager(lifecyclemanager);
		getAAIservice();
	}

	public void setLifecyclemanager(LifecycleManager lifecyclemanager) {
		this.lifecyclemanager = lifecyclemanager;
	}


	private void getAAIservice() {
		BundleContext bctx = FrameworkUtil.getBundle(AAIService.class).getBundleContext();
		// Get AAIadapter reference
		ServiceReference sref = bctx.getServiceReference(AAIService.class.getName());
		if (sref != null) {
			logger.info("AAIService from bundlecontext");
			aaiService = (AAIService) bctx.getService(sref);

		} else {
			logger.info("AAIService error from bundlecontext");
			logger.warn("Cannot find service reference for org.openecomp.sdnc.sli.aai.AAIService");

		}
	}


	@Override
	public void onRequestCompletion(CommandExecutorInput request, CommandResponse response) {

		boolean isAAIUpdated = false;
		try {

			final int statusCode = request.getRuntimeContext().getResponseContext().getStatus().getCode();

			if (logger.isDebugEnabled()) {
				logger.debug("Workflow Execution Status = "+ statusCode);
			}

			boolean isSuccess = statusCode == 100 || statusCode == 400;

			if (isSuccess && VNFOperation.Terminate ==  request.getRuntimeContext().getRequestContext().getAction()) {
				SvcLogicContext ctx = new SvcLogicContext();
				ctx = getVnfdata(request.getRuntimeContext().getVnfContext().getId(), "vnf", ctx);
				isAAIUpdated = aaiService.deleteGenericVnfData(request.getRuntimeContext().getVnfContext().getId(), ctx.getAttribute("vnf.resource-version"));
			}
			else{
				isAAIUpdated = updateAAI(request.getRuntimeContext().getVnfContext().getId() , false, isSuccess);
			}
			logger.debug("isAAIUpdated = " + isAAIUpdated);
		}
		catch(Exception e1) {
			logger.error("Exception = " + e1);
			throw new RuntimeException(e1);
		}
		finally {
			super.onRequestCompletion(request, response , isAAIUpdated);
		}
	}

	@Override
	public void run() {
		CommandExecutorInput request = getCommandRequest();
		boolean isAAIUpdated;
		final String vnfId = request.getRuntimeContext().getVnfContext().getId();
		final String vnfType = request.getRuntimeContext().getVnfContext().getType();
		try {
			final CommonHeader commonHeader = request.getRuntimeContext().getRequestContext().getCommonHeader();
			final boolean forceFlag = commonHeader.getFlags().isForce();
			UniqueRequestIdentifier requestIdentifier = new UniqueRequestIdentifier(commonHeader.getOriginatorId(),
					commonHeader.getRequestId(), commonHeader.getSubRequestId());
			String requestIdentifierString = requestIdentifier.toIdentifierString();
			requestHandler.onRequestExecutionStart(vnfId,false, requestIdentifierString, forceFlag);

			final String currentStatus = request.getRuntimeContext().getVnfContext().getStatus();
			final VNFOperation action = request.getRuntimeContext().getRequestContext().getAction();

			final String nextState = lifecyclemanager.getNextState(vnfType, currentStatus, action.name());

			SvcLogicContext ctx = new SvcLogicContext();
			ctx=getVnfdata(vnfId, "onRequestExecutionStart", ctx);
			isAAIUpdated= postVnfdata(vnfId, nextState,"onRequestExecutionStart",ctx);
		} catch (NoTransitionDefinedException e) {
			logger.error("Error getting Next State for AAI Update:  " + e.getMessage(), e);
			Params params = new Params().addParam("actionName",e.event).addParam("currentState",e.currentState);
			request.getRuntimeContext().getResponseContext().setStatus(LCMCommandStatus.NO_TRANSITION_DEFINE_FAILURE.toStatus(params));
			isAAIUpdated = false;
		} catch (UnstableVNFException e) {
			logger.error(e.getMessage(), e);
			Params params = new Params().addParam("vnfId",vnfId);
			request.getRuntimeContext().getResponseContext().setStatus(LCMCommandStatus.UNSTABLE_VNF_FAILURE.toStatus(params));
			isAAIUpdated = false;
		}catch (Exception e) {
			logger.error("Error before Request Execution starts.", e);
			String errorMsg = StringUtils.isEmpty(e.getMessage()) ? e.toString() : e.getMessage();
			Params params = new Params().addParam("errorMsg",errorMsg);
			request.getRuntimeContext().getResponseContext().setStatus(LCMCommandStatus.UNEXPECTED_FAILURE.toStatus(params));
			isAAIUpdated =  false;
		}

		if (isAAIUpdated){
			super.execute();
		}else{
			String errorMsg = "Error updating A& AI before Workflow execution";
			logger.error(errorMsg);
			WorkflowResponse response = new WorkflowResponse();
			response.setResponseContext(request.getRuntimeContext().getResponseContext());
			CommandResponse commandResponse = super.buildCommandResponse(request, response);
			this.onRequestCompletion(request,commandResponse);
		}
	}



	private boolean updateAAI(String vnf_id , boolean isTTLEnd , boolean executionStatus)
	{
		String orchestrationStatus = null;
		String nextState;
		boolean callbackResponse;
		VNFOperationOutcome outcome;
		SvcLogicContext ctx = new SvcLogicContext();
		try {
			ctx=getVnfdata(vnf_id, "onRequestExecutionEnd",ctx);
			orchestrationStatus=ctx.getAttribute("onRequestExecutionEnd.orchestration-status");

			if(isTTLEnd){
				outcome = VNFOperationOutcome.FAILURE;
			}
			else if(executionStatus){
				outcome = VNFOperationOutcome.SUCCESS;
			}
			else{
				outcome = VNFOperationOutcome.FAILURE;
			}
			nextState = lifecyclemanager.getNextState(null,orchestrationStatus, outcome.toString()) ;
			callbackResponse= postVnfdata(vnf_id, nextState,"onRequestExecutionEnd",ctx);
			logger.debug("AAI posting  status: " + callbackResponse);

		} catch (NoTransitionDefinedException e) {
			logger.debug("Transition not defined for State = " + orchestrationStatus);
			callbackResponse =false;
		} catch (LifecycleException e) {
			logger.debug("State or command not registered with State Machine. State = " + orchestrationStatus);
			callbackResponse =false;
		}
		return callbackResponse;
	}


	private SvcLogicContext getVnfdata(String vnf_id, String prefix,SvcLogicContext ctx) {
		String key="vnf-id = '"+ vnf_id+"'";
		logger.debug("inside getVnfdata=== "+key);
		try {
			SvcLogicResource.QueryStatus response = aaiService.query("generic-vnf", false, null, key,prefix, null, ctx);
			if(SvcLogicResource.QueryStatus.NOT_FOUND.equals(response)){
				logger.warn("VNF " + vnf_id + " not found while updating A&AI");
				throw new RuntimeException("VNF not found for vnf_id = "+ vnf_id);
			}
			else if(SvcLogicResource.QueryStatus.FAILURE.equals(response)){
				throw new RuntimeException("Error Querying AAI with vnfID = " +vnf_id);
			}
			logger.info("AAIResponse: " + response.toString());
		} catch (SvcLogicException e) {
			logger.error("Error in getVnfdata "+ e);
			throw new RuntimeException(e);
		}
		return ctx;
	}

	private boolean postVnfdata(String vnf_id, String status,String prefix,SvcLogicContext ctx) {
		String key="vnf-id = '"+ vnf_id+"'";
		logger.debug("inside postVnfdata=== "+key);
		Map<String, String> data = new HashMap<>();
		data.put("orchestration-status", status);
		try {
			SvcLogicResource.QueryStatus response = aaiService.update("generic-vnf", key, data, prefix,  ctx);
			if(SvcLogicResource.QueryStatus.NOT_FOUND.equals(response)){
				logger.warn("VNF " + vnf_id + " not found while updating A&AI");
				return false;
			}
			logger.info("AAIResponse: " + response.toString());
			if(response.toString().equals("SUCCESS"))
			{
				return true;
			}
		} catch (SvcLogicException e) {
			logger.error("Error in postVnfdata "+ e);
			throw new RuntimeException(e);
		}
		return false;
	}

}
