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

package org.openecomp.appc.dg.ssh.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openecomp.appc.adapter.ssh.SshAdapterMock;
import org.openecomp.appc.adapter.ssh.SshConnectionDetails;
import org.openecomp.appc.adapter.ssh.SshConnectionMock;
import org.openecomp.appc.dg.ssh.SshService;
import org.openecomp.appc.dg.ssh.impl.SshServiceImpl;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.sdnc.sli.SvcLogicContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class SshServiceImplTest {

	private static final ObjectMapper mapper = new ObjectMapper();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testExec() throws APPCException, JsonProcessingException {
		String host = "testhost";
		String username = "testuser";
		String password = "testpassword";
		String command = "cat keystonerc_Test";

		SshServiceImpl sshService = new SshServiceImpl();
		SshAdapterMock sshAdapterMock = new SshAdapterMock();
		sshService.setSshAdapter(sshAdapterMock);

		System.out.println("=> Executing SSH command [" + command + "]...");

		Map<String, String> params = new HashMap<>();
		params.put(SshService.PARAM_IN_connection_details, createConnectionDetails(host,username,password));
		params.put(SshService.PARAM_IN_command, command);
		SvcLogicContext svcLogicContext = new SvcLogicContext(new Properties());
		sshService.exec(params, svcLogicContext);
		int status = Integer.parseInt(svcLogicContext.getAttribute(SshService.PARAM_OUT_status));
		String stdout = svcLogicContext.getAttribute(SshService.PARAM_OUT_stdout);
		String stderr = svcLogicContext.getAttribute(SshService.PARAM_OUT_stderr);
		System.out.println("=> SSH command [" + command + "] status is [" + status + "]. stdout is [" + stdout + "]. stderr is [" + stderr + "]");

		List<SshConnectionMock> connectionMocks = sshAdapterMock.getConnectionMocks();
		Assert.assertEquals(1, connectionMocks.size());
		SshConnectionMock connectionMock = connectionMocks.get(0);
		Assert.assertNotNull(connectionMock);
		Assert.assertEquals(host, connectionMock.getHost());
		Assert.assertEquals(SshService.DEF_port, connectionMock.getPort());
		Assert.assertEquals(username, connectionMock.getUsername());
		Assert.assertEquals(password, connectionMock.getPassword());
		Assert.assertEquals(1, connectionMock.getConnectCallCount());
		Assert.assertEquals(1, connectionMock.getDisconnectCallCount());
		List<String> executedCommands = connectionMock.getExecutedCommands();
		Assert.assertEquals(1, executedCommands.size());
		String executedCommand = executedCommands.get(0);
		Assert.assertEquals(command, executedCommand);
	}

	@Test
	public void testExecWithStatusCheck() throws APPCException, JsonProcessingException {
		String host = "testhost";
		String username = "testuser";
		String password = "testpassword";
		String command = "cat keystonerc_Test";

		SshServiceImpl sshService = new SshServiceImpl();
		SshAdapterMock sshAdapterMock = new SshAdapterMock();
		sshService.setSshAdapter(sshAdapterMock);

		System.out.println("=> Executing SSH command [" + command + "]...");
		Map<String, String> params = new HashMap<>();
		params.put(SshService.PARAM_IN_connection_details, createConnectionDetails(host,username,password));
		params.put(SshService.PARAM_IN_command, command);
		SvcLogicContext svcLogicContext = new SvcLogicContext(new Properties());
		sshService.execWithStatusCheck(params, svcLogicContext);
		int status = Integer.parseInt(svcLogicContext.getAttribute(SshService.PARAM_OUT_status));
		String stdout = svcLogicContext.getAttribute(SshService.PARAM_OUT_stdout);
		String stderr = svcLogicContext.getAttribute(SshService.PARAM_OUT_stderr);
		System.out.println("=> SSH command [" + command + "] status is [" + status + "]. stdout is [" + stdout + "]. stderr is [" + stderr + "]");

		List<SshConnectionMock> connectionMocks = sshAdapterMock.getConnectionMocks();
		Assert.assertEquals(1, connectionMocks.size());
		SshConnectionMock connectionMock = connectionMocks.get(0);
		Assert.assertNotNull(connectionMock);
		Assert.assertEquals(host, connectionMock.getHost());
		Assert.assertEquals(SshService.DEF_port, connectionMock.getPort());
		Assert.assertEquals(username, connectionMock.getUsername());
		Assert.assertEquals(password, connectionMock.getPassword());
		Assert.assertEquals(1, connectionMock.getConnectCallCount());
		Assert.assertEquals(1, connectionMock.getDisconnectCallCount());
		List<String> executedCommands = connectionMock.getExecutedCommands();
		Assert.assertEquals(1, executedCommands.size());
		String executedCommand = executedCommands.get(0);
		Assert.assertEquals(command, executedCommand);
	}

	/**
	 * Checks that execWithStatusCheck() throws appropriate exception if execution status != 0.
	 *
	 * @throws APPCException
	 * @throws JsonProcessingException
	 */
	@Test
	public void testExecWithStatusCheckFail() throws APPCException, JsonProcessingException {
		String host = "testhost";
		String username = "testuser";
		String password = "testpassword";
		String command = "cat keystonerc_Test";

		int expectedStatus = 2;
		String expectedErr = "Test failure";

		SshServiceImpl sshService = new SshServiceImpl();
		SshAdapterMock sshAdapterMock = new SshAdapterMock();
		sshAdapterMock.setReturnStatus(expectedStatus);
		sshAdapterMock.setReturnStderr(expectedErr);
		sshService.setSshAdapter(sshAdapterMock);

		thrown.expect(APPCException.class);
		thrown.expectMessage(CoreMatchers.containsString(expectedErr));

		System.out.println("=> Executing SSH command [" + command + "]...");
		Map<String, String> params = new HashMap<>();
		params.put(SshService.PARAM_IN_connection_details, createConnectionDetails(host,username,password));
		params.put(SshService.PARAM_IN_command, command);
		SvcLogicContext svcLogicContext = new SvcLogicContext(new Properties());
		// should fail, no need to perform further assertions
		sshService.execWithStatusCheck(params, svcLogicContext);
	}

	private String createConnectionDetails(String host, String username, String password) throws JsonProcessingException {
		SshConnectionDetails connDetails = new SshConnectionDetails();
		connDetails.setHost(host);
		connDetails.setUsername(username);
		connDetails.setPassword(password);
		return mapper.writeValueAsString(connDetails);
	}

}
