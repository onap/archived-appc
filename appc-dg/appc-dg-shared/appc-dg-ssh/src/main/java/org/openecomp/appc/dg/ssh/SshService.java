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

package org.openecomp.appc.dg.ssh;

import java.util.Map;

import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.sdnc.sli.SvcLogicContext;
import org.openecomp.sdnc.sli.SvcLogicJavaPlugin;

/**
 * Set of common methods that can be called from DG.
 */
public interface SshService extends SvcLogicJavaPlugin {

	/**
	 * Input parameter for SHH connection details
	 */
	String PARAM_IN_connection_details = "connection_details";

	/**
	 * Input parameter for SSH command to be executed.
	 */
	String PARAM_IN_command = "command";

	/**
	 * Input parameter for SSH command timeout
	 */
	String PARAM_IN_timeout = "timeout";

	/**
	 * Output parameter - SSH command execution status.
	 */
	String PARAM_OUT_status = "status";

	/**
	 * Output parameter - content of SSH command stdout.
	 */
	String PARAM_OUT_stdout = "stdout";

	/**
	 * Output parameter - content of SSH command stderr.
	 */
	String PARAM_OUT_stderr = "stderr";

	/**
	 * Default SSH connection port.
	 */
	int DEF_port = 22;

	/**
	 * Default SSH command timeout
	 */
	long DEF_timeout = 120000;

	/**
	 * Default success status.
	 */
	int DEF_SUCCESS_STATUS = 0;

	/**
	 * Execute remote command over SSH.
	 *
	 * @param params contains list of input parameters required for the implementation
	 * @param ctx SLI service logic context
	 * @throws APPCException
	 */
	void exec(Map<String, String> params, SvcLogicContext ctx) throws APPCException;

	/**
	 * Execute remote command over SSH and check return status assuming that success status is 0.
	 * If non-zero status is returned - fail the execution by throwing exception with content written
	 * by command to stderr.
	 *
	 * @param params contains list of input parameters required for the implementation
	 * @param ctx SLI service logic context
	 * @throws APPCException
	 */
	void execWithStatusCheck(Map<String, String> params, SvcLogicContext ctx) throws APPCException;
}
