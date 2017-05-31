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

package org.openecomp.appc.adapter.ssh.sshd;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import org.openecomp.appc.adapter.ssh.SshAdapter;
import org.openecomp.appc.adapter.ssh.SshConnection;
import org.openecomp.appc.adapter.ssh.sshd.SshAdapterSshd;

public class SshAdapterSample {

	public static void main(String[] args) {
		String host = "hostname";
		int port = 22;
		String username = "user";
		String password = "secret";
		String command = "ls";

		SshAdapter sshAdapter = new SshAdapterSshd();
		SshConnection sshConnection = sshAdapter.getConnection(host, port, username, password);
		sshConnection.connect();
		try {
			OutputStream stdout = new ByteArrayOutputStream();
			OutputStream stderr = new ByteArrayOutputStream();
			int status = sshConnection.execCommand(command, stdout, stderr);
			if(status == 0) {
				System.out.println("Command executed successfully. Output:\n" + stdout.toString());
			} else {
				System.err.println("Command returned status " + status + ". Error:\n" + stderr.toString());
			}
		} finally {
			sshConnection.disconnect();
		}
	}
}
