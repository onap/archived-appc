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

package org.openecomp.appc.adapter.ssh.sshd;

import org.apache.sshd.ClientChannel;
import org.apache.sshd.ClientSession;
import org.apache.sshd.SshClient;
import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.future.OpenFuture;
import org.apache.sshd.common.KeyPairProvider;
import org.apache.sshd.common.keyprovider.FileKeyPairProvider;
import org.openecomp.appc.adapter.ssh.SshConnection;
import org.openecomp.appc.adapter.ssh.SshException;
import org.openecomp.appc.encryption.EncryptionTool;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

import java.io.OutputStream;
import java.security.KeyPair;

/**
 * Implementation of SshConnection interface based on Apache MINA SSHD library.
 */
class SshConnectionSshd implements SshConnection {

	private static final EELFLogger logger = EELFManager.getInstance().getApplicationLogger();

	private static final long AUTH_TIMEOUT = 60000;
	private static final long EXEC_TIMEOUT = 120000;

	private String host;
	private int port;
	private String username;
	private String password;
	private long timeout = EXEC_TIMEOUT;
	private String keyFile;
	private SshClient sshClient;
	private ClientSession clientSession;

	public SshConnectionSshd(String host, int port, String username, String password, String keyFile) {
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
		this.keyFile = keyFile;
	}

	public SshConnectionSshd(String host, int port, String username, String password) {
		this(host, port, username, password, null);
	}

	public SshConnectionSshd(String host, int port, String keyFile) {
		this(host, port, null, null, keyFile);
	}

	@Override
	public void connect() {
		sshClient = SshClient.setUpDefaultClient();
		sshClient.start();
		try {
			clientSession = sshClient.connect(EncryptionTool.getInstance().decrypt(username), host, port).await().getSession();
			if(password != null) {
				clientSession.addPasswordIdentity(EncryptionTool.getInstance().decrypt(password));
			}
			if(keyFile != null) {
				KeyPairProvider keyPairProvider = new FileKeyPairProvider(new String[]{keyFile});
				KeyPair keyPair = keyPairProvider.loadKeys().iterator().next();
				clientSession.addPublicKeyIdentity(keyPair);
			}
			AuthFuture authFuture = clientSession.auth();
			authFuture.await(AUTH_TIMEOUT);
			if(!authFuture.isSuccess()) {
				throw new SshException("Error establishing ssh connection to [" + username + "@" + host + ":" + port + "]. Authentication failed.");
			}
		} catch(RuntimeException e) {
			throw e;
		} catch(Exception e) {
			throw new SshException("Error establishing ssh connection to [" + username + "@" + host + ":" + port + "].", e);
		}
		if(logger.isDebugEnabled()) {
			logger.debug("SSH: connected to [" + toString() + "]");
		}
	}

	@Override
	public void disconnect() {
		try {
			if(logger.isDebugEnabled()) {
				logger.debug("SSH: disconnecting from [" + toString() + "]");
			}
			clientSession.close(false);
		} finally {
			if(sshClient != null) {
				sshClient.stop();
			}
		}
	}

	@Override
	public void setExecTimeout(long timeout) {
		this.timeout = timeout;
	}

	@Override
	public int execCommand(String cmd, OutputStream out, OutputStream err) {
		return execCommand(cmd, out, err, false);
	}

	@Override
	public int execCommandWithPty(String cmd, OutputStream out) {
		return execCommand(cmd, out, out, true);
	}

	private int execCommand(String cmd, OutputStream out, OutputStream err, boolean usePty) {
		try {
			if(logger.isDebugEnabled()) {
				logger.debug("SSH: executing command");
			}
			ChannelExec client = clientSession.createExecChannel(cmd);
            client.setUsePty(usePty); // use pseudo-tty?
			client.setOut(out);
			client.setErr(err);
			OpenFuture openFuture = client.open();
			int exitStatus = 0;
			try {
				client.waitFor(ClientChannel.CLOSED, timeout);
				openFuture.verify();
				Integer exitStatusI = client.getExitStatus();
				if(exitStatusI == null) {
					throw new SshException("Error executing command [" + cmd + "] over SSH [" + username + "@" + host + ":" + port + "]. Operation timed out.");
				}
				exitStatus = exitStatusI;
			} finally {
				client.close(false);
			}
			return exitStatus;
		} catch(RuntimeException e) {
			throw e;
		} catch(Exception t) {
			throw new SshException("Error executing command [" + cmd + "] over SSH [" + username + "@" + host + ":" + port + "]", t);
		}
	}

	@Override
	public String toString() {
		String address = host;
		if(username != null) {
			address = username + '@' +address;
		}
		return address;
	}
}
