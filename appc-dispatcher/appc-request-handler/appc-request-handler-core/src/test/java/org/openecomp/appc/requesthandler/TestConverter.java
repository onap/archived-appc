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

package org.openecomp.appc.requesthandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Assert;
import org.junit.Test;
import org.openecomp.appc.domainmodel.lcm.*;
import org.openecomp.appc.executor.objects.LCMCommandStatus;
import org.openecomp.appc.requesthandler.conv.Converter;

import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;


public class TestConverter {
	private String expectedJsonBodyStr ="{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}}";
	private String expectedDmaapOutgoingMessageJsonStringTest ="{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"test\",\"type\":\"response\"}";
	private String expectedDmaapOutgoingMessageJsonStringRollback ="{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"rollback\",\"type\":\"response\"}";
	private String expectedDmaapOutgoingMessageJsonStringSnapshot ="{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"snapshot\",\"type\":\"response\"}";
	private String expectedDmaapOutgoingMessageJsonStringAudit ="{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"payload\":\"{}\",\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"audit\",\"type\":\"response\"}";
	private String expectedDmaapOutgoingMessageJsonStringHealthCheck ="{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"health-check\",\"type\":\"response\"}";
	private String expectedDmaapOutgoingMessageJsonStringLiveUpgrade ="{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"live-upgrade\",\"type\":\"response\"}";
	private String expectedDmaapOutgoingMessageJsonStringLock ="{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"lock\",\"type\":\"response\"}";
	private String expectedDmaapOutgoingMessageJsonStringModifyConfig ="{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"payload\":\"{}\",\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"config-modify\",\"type\":\"response\"}";
	private String expectedDmaapOutgoingMessageJsonStringSoftwareUpload ="{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"software-upload\",\"type\":\"response\"}";
	private String expectedDmaapOutgoingMessageJsonStringStop ="{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"stop\",\"type\":\"response\"}";
	private String expectedDmaapOutgoingMessageJsonStringSync ="{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"payload\":\"{}\",\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"sync\",\"type\":\"response\"}";
	private String expectedDmaapOutgoingMessageJsonStringTerminate ="{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"terminate\",\"type\":\"response\"}";
	private String expectedDmaapOutgoingMessageJsonStringUnlock ="{\"body\":{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}},\"cambria.partition\":\"MSO\",\"correlation-id\":\"reqid\",\"rpc-name\":\"unlock\",\"type\":\"response\"}";
	private String expectedJsonBodyStrwithPayload ="{\"output\":{\"common-header\":{\"api-ver\":\"2.0.0\",\"flags\":{},\"originator-id\":\"oid\",\"request-id\":\"reqid\",\"timestamp\":\"1970-01-01T00:00:01.000Z\"},\"payload\":\"{}\",\"status\":{\"code\":400,\"message\":\"SUCCESS - request has been processed successfully\"}}}";

	@Test
	public void convDateToZuluStringTest(){
		String dateToZuluString = Converter.convDateToZuluString(new Date(0L));
		Assert.assertEquals("1970-01-01T00:00:00.000Z", dateToZuluString);
	}

	@Test
	public void convAsyncResponseToBuilderTestTest() throws JsonProcessingException {
		ResponseContext asyncResponse = buildAsyncResponse();
		VNFOperation action = VNFOperation.Test;
		String rpcName = action.name().toLowerCase();
		String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
		Assert.assertEquals(expectedJsonBodyStr,jsonStr);
	}

	@Test
	public void convAsyncResponseToDmaapOutgoingMessageJsonStringTestTest() throws JsonProcessingException {
		ResponseContext asyncResponse = buildAsyncResponse();
		VNFOperation action = VNFOperation.Test;
		String rpcName = action.name().toLowerCase();
		String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
		System.out.println("jsonStr = " + jsonStr);
		Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringTest,jsonStr);
	}

	@Test
	public void convPayloadObjectToJsonStringTest() throws JsonProcessingException, ParseException {
		String jsonString = Converter.convPayloadObjectToJsonString("any valid JSON string value");
		Assert.assertEquals("any valid JSON string value", jsonString);

		HashMap<String, String> hashMap = new HashMap<>();
		hashMap.put("key","value");
		jsonString = Converter.convPayloadObjectToJsonString(hashMap);
		Assert.assertEquals("{\"key\":\"value\"}", jsonString);
	}

	@Test
	public void convAsyncResponseToBuilderRollbackTest() throws JsonProcessingException {
		ResponseContext asyncResponse = buildAsyncResponse();
		VNFOperation action = VNFOperation.Rollback;
		String rpcName = action.name().toLowerCase();
		String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
		Assert.assertEquals(expectedJsonBodyStr,jsonStr);
	}

	@Test
	public void convAsyncResponseToDmaapOutgoingMessageJsonStringRollbackTest() throws JsonProcessingException {
		ResponseContext asyncResponse = buildAsyncResponse();
		VNFOperation action = VNFOperation.Rollback;
		String rpcName = action.name().toLowerCase();
		String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
		System.out.println("jsonStr = " + jsonStr);
		Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringRollback,jsonStr);
	}

	@Test
	public void convAsyncResponseToBuilderSnapshotTest() throws JsonProcessingException {
		ResponseContext asyncResponse = buildAsyncResponse();
		VNFOperation action = VNFOperation.Snapshot;
		String rpcName = action.name().toLowerCase();
		String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
		Assert.assertEquals(expectedJsonBodyStr,jsonStr);
	}

	@Test
	public void convAsyncResponseToDmaapOutgoingMessageJsonStringSnapshotTest() throws JsonProcessingException {
		ResponseContext asyncResponse = buildAsyncResponse();
		VNFOperation action = VNFOperation.Snapshot;
		String rpcName = action.name().toLowerCase();
		String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
		System.out.println("jsonStr = " + jsonStr);
		Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringSnapshot,jsonStr);
	}
	@Test
	public void convAsyncResponseToBuilderAuditTest() throws JsonProcessingException {
		ResponseContext asyncResponse = buildAsyncResponsewithPayload();
		VNFOperation action = VNFOperation.Audit;
		String rpcName = action.name().toLowerCase();
		String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
		Assert.assertEquals(expectedJsonBodyStrwithPayload,jsonStr);
	}

	@Test
	public void convAsyncResponseToDmaapOutgoingMessageJsonStringAuditTest() throws JsonProcessingException {
		ResponseContext asyncResponse = buildAsyncResponsewithPayload();
		VNFOperation action = VNFOperation.Audit;
		String rpcName = action.name().toLowerCase();
		String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
		System.out.println("jsonStr = " + jsonStr);
		Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringAudit,jsonStr);
	}
	@Test
	public void convAsyncResponseToBuilderHealthCheckTest() throws JsonProcessingException {
		ResponseContext asyncResponse = buildAsyncResponse();
		VNFOperation action = VNFOperation.HealthCheck;
		String rpcName = convertActionNameToUrl(action.name());
		String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
		Assert.assertEquals(expectedJsonBodyStr,jsonStr);
	}

	@Test
	public void convAsyncResponseToDmaapOutgoingMessageJsonStringHealthCheckTest() throws JsonProcessingException {
		ResponseContext asyncResponse = buildAsyncResponse();
		VNFOperation action = VNFOperation.HealthCheck;
		String rpcName = convertActionNameToUrl(action.name());
		String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
		System.out.println("jsonStr = " + jsonStr);
		Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringHealthCheck,jsonStr);
	}
	@Test
	public void convAsyncResponseToBuilderLiveUpgradeTest() throws JsonProcessingException {
		ResponseContext asyncResponse = buildAsyncResponse();
		VNFOperation action = VNFOperation.LiveUpgrade;
		String rpcName = convertActionNameToUrl(action.name());
		String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
		Assert.assertEquals(expectedJsonBodyStr,jsonStr);
	}

	@Test
	public void convAsyncResponseToDmaapOutgoingMessageJsonStringLiveUpgradeTest() throws JsonProcessingException {
		ResponseContext asyncResponse = buildAsyncResponse();
		VNFOperation action = VNFOperation.LiveUpgrade;
		String rpcName = convertActionNameToUrl(action.name());
		String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
		System.out.println("jsonStr = " + jsonStr);
		Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringLiveUpgrade,jsonStr);
	}
	@Test
	public void convAsyncResponseToBuilderLockTest() throws JsonProcessingException {
		ResponseContext asyncResponse = buildAsyncResponse();
		VNFOperation action = VNFOperation.Lock;
		String rpcName = convertActionNameToUrl(action.name());

		String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
		Assert.assertEquals(expectedJsonBodyStr,jsonStr);
	}

	@Test
	public void convAsyncResponseToDmaapOutgoingMessageJsonStringLockTest() throws JsonProcessingException {
		ResponseContext asyncResponse = buildAsyncResponse();
		VNFOperation action = VNFOperation.Lock;
		String rpcName = convertActionNameToUrl(action.name());
		String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
		System.out.println("jsonStr = " + jsonStr);
		Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringLock,jsonStr);
	}
	@Test
	public void convAsyncResponseToBuilderModifyConfigTest() throws JsonProcessingException {
		ResponseContext asyncResponse = buildAsyncResponsewithPayload();
		VNFOperation action = VNFOperation.ConfigModify;
		String rpcName = convertActionNameToUrl(action.name());
		String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
		Assert.assertEquals(expectedJsonBodyStrwithPayload,jsonStr);
	}

	@Test
	public void convAsyncResponseToDmaapOutgoingMessageJsonStringModifyConfigTest() throws JsonProcessingException {
		ResponseContext asyncResponse = buildAsyncResponsewithPayload();
		VNFOperation action = VNFOperation.ConfigModify;
		String rpcName = convertActionNameToUrl(action.name());
		String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
		System.out.println("jsonStr = " + jsonStr);
		Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringModifyConfig,jsonStr);
	}
	@Test
	public void convAsyncResponseToBuilderSoftwareUploadTest() throws JsonProcessingException {
		ResponseContext asyncResponse = buildAsyncResponse();
		VNFOperation action = VNFOperation.SoftwareUpload;
		String rpcName = convertActionNameToUrl(action.name());

		String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
		Assert.assertEquals(expectedJsonBodyStr,jsonStr);
	}

	@Test
	public void convAsyncResponseToDmaapOutgoingMessageJsonStringSoftwareUploadTest() throws JsonProcessingException {
		ResponseContext asyncResponse = buildAsyncResponse();
		VNFOperation action = VNFOperation.SoftwareUpload;
		String rpcName = convertActionNameToUrl(action.name());
		String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
		System.out.println("jsonStr = " + jsonStr);
		Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringSoftwareUpload,jsonStr);
	}
	@Test
	public void convAsyncResponseToBuilderStopTest() throws JsonProcessingException {
		ResponseContext asyncResponse = buildAsyncResponse();
		VNFOperation action = VNFOperation.Stop;
		String rpcName = convertActionNameToUrl(action.name());
		String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
		Assert.assertEquals(expectedJsonBodyStr,jsonStr);
	}

	@Test
	public void convAsyncResponseToDmaapOutgoingMessageJsonStringStopTest() throws JsonProcessingException {
		ResponseContext asyncResponse = buildAsyncResponse();
		VNFOperation action = VNFOperation.Stop;
		String rpcName = convertActionNameToUrl(action.name());
		String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
		System.out.println("jsonStr = " + jsonStr);
		Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringStop,jsonStr);
	}
	@Test
	public void convAsyncResponseToBuilderSync() throws JsonProcessingException {
		ResponseContext asyncResponse = buildAsyncResponsewithPayload();
		VNFOperation action = VNFOperation.Sync;
		String rpcName = convertActionNameToUrl(action.name());

		String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
		Assert.assertEquals(expectedJsonBodyStrwithPayload,jsonStr);
	}

	@Test
	public void convAsyncResponseToDmaapOutgoingMessageJsonStringSync() throws JsonProcessingException {
		ResponseContext asyncResponse = buildAsyncResponsewithPayload();
		VNFOperation action = VNFOperation.Sync;
		String rpcName = convertActionNameToUrl(action.name());
		String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
		System.out.println("jsonStr = " + jsonStr);
		Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringSync,jsonStr);
	}
	@Test
	public void convAsyncResponseToBuilderTerminateTest() throws JsonProcessingException {
		ResponseContext asyncResponse = buildAsyncResponsewithPayload();
		VNFOperation action = VNFOperation.Sync;
		String rpcName = convertActionNameToUrl(action.name());
		String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
		Assert.assertEquals(expectedJsonBodyStrwithPayload,jsonStr);
	}

	@Test
	public void convAsyncResponseToDmaapOutgoingMessageJsonStringTerminateTest() throws JsonProcessingException {
		ResponseContext asyncResponse = buildAsyncResponse();
		VNFOperation action = VNFOperation.Terminate;
		String rpcName = convertActionNameToUrl(action.name());
		String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
		System.out.println("jsonStr = " + jsonStr);
		Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringTerminate,jsonStr);
	}
	@Test
	public void convAsyncResponseToBuilderUnlockTest() throws JsonProcessingException {
		ResponseContext asyncResponse = buildAsyncResponse();
		VNFOperation action = VNFOperation.Unlock;
		String rpcName = convertActionNameToUrl(action.name());
		String jsonStr = Converter.convAsyncResponseToJsonStringBody(action, rpcName, asyncResponse);
		Assert.assertEquals(expectedJsonBodyStr,jsonStr);
	}

	@Test
	public void convAsyncResponseToDmaapOutgoingMessageJsonStringUnlockTest() throws JsonProcessingException {
		ResponseContext asyncResponse = buildAsyncResponse();
		VNFOperation action = VNFOperation.Unlock;
		String rpcName = convertActionNameToUrl(action.name());
		String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(action, rpcName, asyncResponse);
		System.out.println("jsonStr = " + jsonStr);
		Assert.assertEquals(expectedDmaapOutgoingMessageJsonStringUnlock,jsonStr);
	}
	/*@Test
	public void convAsyncResponseToBuilderTest() throws JsonProcessingException {
		AsyncResponse asyncResponse = buildAsyncResponse();
		String jsonStr = Converter.convAsyncResponseToJsonStringBody(asyncResponse);
		Assert.assertEquals(expectedJsonBodyStr,jsonStr);
	}

	@Test
	public void convAsyncResponseToDmaapOutgoingMessageJsonStringTest() throws JsonProcessingException {
		AsyncResponse asyncResponse = buildAsyncResponse();
		String jsonStr = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(asyncResponse);
		System.out.println("jsonStr = " + jsonStr);
		Assert.assertEquals(expectedDmaapOutgoingMessageJsonString,jsonStr);
	}*/


	private ResponseContext buildAsyncResponse() {
		ResponseContext asyncResponse = createResponseContextWithSubObjects();
		asyncResponse.setStatus(LCMCommandStatus.SUCCESS.toStatus(null));
		asyncResponse.getCommonHeader().setOriginatorId("oid");
		asyncResponse.getCommonHeader().setApiVer("2.0.0");
		asyncResponse.getCommonHeader().setRequestId("reqid");
		asyncResponse.getCommonHeader().setTimestamp(Instant.ofEpochMilli(1000L));
		asyncResponse.setPayload("any valid JSON string value. Json escape characters need to be added to make it a valid json string value");
		return asyncResponse;
	}

	private ResponseContext buildAsyncResponsewithPayload() {
		ResponseContext asyncResponse = createResponseContextWithSubObjects();
		asyncResponse.setStatus(LCMCommandStatus.SUCCESS.toStatus(null));
		asyncResponse.getCommonHeader().setOriginatorId("oid");
		asyncResponse.getCommonHeader().setApiVer("2.0.0");
		asyncResponse.getCommonHeader().setRequestId("reqid");
		asyncResponse.getCommonHeader().setTimestamp(Instant.ofEpochMilli(1000L));
		asyncResponse.setPayload("{}");
		return asyncResponse;
	}

	private ResponseContext createResponseContextWithSubObjects() {

		ResponseContext responseContext = new ResponseContext();
		CommonHeader commonHeader = new CommonHeader();
		responseContext.setCommonHeader(commonHeader);
		responseContext.setStatus(new Status(0, null));
		commonHeader.setFlags(new Flags(null, false, 0));
		return responseContext;
	}

	private String convertActionNameToUrl(String action) {
		String regex = "([a-z])([A-Z]+)";
		String replacement = "$1-$2";
		return action.replaceAll(regex, replacement)
				.toLowerCase();
	}


}



