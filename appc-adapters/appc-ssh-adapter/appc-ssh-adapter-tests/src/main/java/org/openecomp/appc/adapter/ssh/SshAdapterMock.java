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

package org.openecomp.appc.adapter.ssh;

import java.util.ArrayList;
import java.util.List;

public class SshAdapterMock implements SshAdapter {

	private List<SshConnectionMock> connectionMocks = new ArrayList<>();

	private int returnStatus;
	private String returnStdout;
	private String returnStderr;

	@Override
	public SshConnection getConnection(String host, int port, String username, String password) {
		SshConnectionMock sshConnectionMock = new SshConnectionMock(host, port, username, password);
		sshConnectionMock.setReturnStatus(returnStatus);
		sshConnectionMock.setReturnStdout(returnStdout);
		sshConnectionMock.setReturnStderr(returnStderr);
		connectionMocks.add(sshConnectionMock);
		return sshConnectionMock;
	}

	public List<SshConnectionMock> getConnectionMocks() {
		return connectionMocks;
	}

	public int getReturnStatus() {
		return returnStatus;
	}

	public void setReturnStatus(int returnStatus) {
		this.returnStatus = returnStatus;
	}

	public String getReturnStdout() {
		return returnStdout;
	}

	public void setReturnStdout(String returnStdout) {
		this.returnStdout = returnStdout;
	}

	public String getReturnStderr() {
		return returnStderr;
	}

	public void setReturnStderr(String returnStderr) {
		this.returnStderr = returnStderr;
	}
}
