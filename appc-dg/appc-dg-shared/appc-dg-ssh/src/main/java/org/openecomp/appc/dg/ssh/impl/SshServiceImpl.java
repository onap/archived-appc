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

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import org.openecomp.appc.adapter.ssh.SshAdapter;
import org.openecomp.appc.adapter.ssh.SshConnection;
import org.openecomp.appc.adapter.ssh.SshConnectionDetails;
import org.openecomp.appc.dg.ssh.SshService;
import org.openecomp.appc.exceptions.APPCException;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.openecomp.sdnc.sli.SvcLogicContext;


public class SshServiceImpl implements SshService {

	private static final EELFLogger logger = EELFManager.getInstance().getApplicationLogger();
	private static final ObjectMapper mapper = new ObjectMapper();

	private SshAdapter sshAdapter;

	public void setSshAdapter(SshAdapter sshAdapter) {
		this.sshAdapter = sshAdapter;
	}

	@Override
	public void exec(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
		SshConnectionDetails connectionDetails = resolveConnectionDetails(params.get(PARAM_IN_connection_details));
		String command = params.get(PARAM_IN_command);
		logger.debug("=> Connecting to SSH server...");
		SshConnection sshConnection = sshAdapter.getConnection(connectionDetails.getHost(), connectionDetails.getPort(), connectionDetails.getUsername(), connectionDetails.getPassword());
		sshConnection.connect();
		try {
			logger.debug("=> Connected to SSH server...");
			logger.debug("=> Running SSH command...");
			long timeout = DEF_timeout;
			String stimeout = params.get(PARAM_IN_timeout);
			if ((stimeout != null && !stimeout.isEmpty())) {
				timeout = Long.parseLong(stimeout);
			}
			sshConnection.setExecTimeout(timeout);
			ByteArrayOutputStream stdout = new ByteArrayOutputStream();
			ByteArrayOutputStream stderr = new ByteArrayOutputStream();
			int status = sshConnection.execCommand(command, stdout, stderr);
			String stdoutRes = stdout.toString();
			String stderrRes = stderr.toString();
			logger.debug("=> executed SSH command");
			ctx.setAttribute(PARAM_OUT_status, String.format("%01d", status));
			ctx.setAttribute(PARAM_OUT_stdout, stdoutRes);
			ctx.setAttribute(PARAM_OUT_stderr, stderrRes);
		} finally {
			sshConnection.disconnect();
		}
	}

	private SshConnectionDetails resolveConnectionDetails(String connectionDetailsStr) throws APPCException {
		SshConnectionDetails connectionDetails = null;
		try {
			connectionDetails = mapper.readValue(connectionDetailsStr, SshConnectionDetails.class);
			if (0 == connectionDetails.getPort()) connectionDetails.setPort(DEF_port);
		} catch (IOException e) {
			throw new APPCException(e);
		}
		return connectionDetails;
	}

	@Override
	public void execWithStatusCheck(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
		exec(params, ctx);
		int status = Integer.parseInt(ctx.getAttribute(PARAM_OUT_status));
		if(status != DEF_SUCCESS_STATUS) {
			StringBuilder errmsg = new StringBuilder();
			errmsg.append("SSH command returned error status [").append(status).append(']');
			String stderr = ctx.getAttribute(PARAM_OUT_stderr);
			if((stderr != null) && !stderr.isEmpty()) {
				errmsg.append(". Error: [").append(stderr).append(']');
			}
			throw new APPCException(errmsg.toString());
		}
	}
}
