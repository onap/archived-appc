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

import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.util.OsUtils;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.PublickeyAuthenticator;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.sftp.SftpSubsystem;
import org.apache.sshd.server.shell.ProcessShellFactory;
import org.hamcrest.CoreMatchers;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.openecomp.appc.adapter.ssh.SshAdapter;
import org.openecomp.appc.adapter.ssh.SshConnection;
import org.openecomp.appc.adapter.ssh.SshException;
import org.openecomp.appc.adapter.ssh.sshd.SshAdapterSshd;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.BindException;
import java.security.PublicKey;
import java.util.Collections;
import java.util.EnumSet;

public class SshAdapterTest {

    private static final boolean START_SERVER = true;
    private static final String SSH_HOST = "localhost";
    private static final int SSH_PORT = 2222;
    private static final String SSH_USERNAME = "test";
    private static final String SSH_PASSWORD = "test";
    private static final String F_TEST_CMD = "ping -%c 4 %s";

    private int sshPort = SSH_PORT;
    private SshServer sshd;
    private SshAdapter sshAdapter = new SshAdapterSshd();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testExecute() {
        String cmd = String.format(F_TEST_CMD, OsUtils.isUNIX() ? 'c' : 'n', "localhost");
        SshConnection sshConnection = connect(SSH_USERNAME, SSH_PASSWORD);
        try {
            System.out.println("SSH client connected. Server port [" + sshPort + "]. [" + getClass().getName() + "#" + System.identityHashCode(this) + "]");
            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            ByteArrayOutputStream stderr = new ByteArrayOutputStream();
            int status = execCmd(sshConnection, cmd, stdout, stderr, false);
            Assert.assertEquals(stdout.toString() + ". " + stderr.toString(), 0, status);
        } finally {
            disconnect(sshConnection);
        }
    }

    @Test
    public void testExecuteWithPty() {
        String cmd = String.format(F_TEST_CMD, OsUtils.isUNIX() ? 'c' : 'n', "localhost");
        SshConnection sshConnection = connect(SSH_USERNAME, SSH_PASSWORD);
        try {
            System.out.println("SSH client connected. Server port [" + sshPort + "]. [" + getClass().getName() + "#" + System.identityHashCode(this) + "]");
            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            int status = execCmd(sshConnection, cmd, stdout, null, true);
            Assert.assertEquals(stdout.toString() + ". " + stdout.toString(), 0, status);
        } finally {
            disconnect(sshConnection);
        }
    }

    @Test
    public void testExecuteInvalidCommand() {
        String cmd = String.format(F_TEST_CMD, OsUtils.isUNIX() ? 'c' : 'n', "nosuchhost");
        SshConnection sshConnection = connect(SSH_USERNAME, SSH_PASSWORD);
        try {
            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            ByteArrayOutputStream stderr = new ByteArrayOutputStream();
            int status = execCmd(sshConnection, cmd, stdout, stderr, false);
            Assert.assertNotEquals(stdout.toString() + ". " + stderr.toString(), 0, status);
        } finally {
            disconnect(sshConnection);
        }
    }

    @Test
    public void testWrongUsername() {
        thrown.expect(SshException.class);
        thrown.expectMessage(CoreMatchers.containsString("Authentication failed"));
        disconnect(connect("WrongUsername", SSH_PASSWORD));
    }

    @Test
    public void testWrongPassword() {
        thrown.expect(SshException.class);
        thrown.expectMessage(CoreMatchers.containsString("Authentication failed"));
        disconnect(connect(SSH_USERNAME, "WrongPassword"));
    }

    @Before
    public void beforeTest() throws IOException {
        if (START_SERVER) {
            startServer();
        }
    }

    @After
    public void afterTest() throws InterruptedException {
        stopServer();
    }

    private SshConnection connect(String username, String password) {
        SshConnection sshConnection = sshAdapter.getConnection(SSH_HOST, sshPort, username, password);
        sshConnection.connect();
        System.out.println("SSH client connected. Server port [" + sshPort + "]. [" + getClass().getName() + "#" + System.identityHashCode(this) + "]");
        return sshConnection;
    }

    private void disconnect(SshConnection sshConnection) {
        sshConnection.disconnect();
        System.out.println("SSH client disconnected. Server port [" + sshPort + "]. [" + getClass().getName() + "#" + System.identityHashCode(this) + "]");
    }

    private int execCmd(SshConnection sshConnection, String cmd, OutputStream stdout, OutputStream stderr, boolean usePty) {
        System.out.println("=> Running command [" + cmd + "] over SSH");
        int status;
        if (usePty) {
            status = sshConnection.execCommandWithPty(cmd, stdout);
        } else {
            status = sshConnection.execCommand(cmd, stdout, stderr);
        }
        System.out.println("=> Command [" + cmd + "] status is [" + status + "], stdout is [" + String.valueOf(stdout) + "], stderr is [" + String.valueOf(stderr) + "]");
        return status;
    }

    private void startServer() throws IOException {
        sshd = SshServer.setUpDefaultServer();
        sshd.setSubsystemFactories(Collections.<NamedFactory<Command>>singletonList(new SftpSubsystem.Factory()));
        sshd.setCommandFactory(new ScpCommandFactory(new CommandFactory() {

            public Command createCommand(String command) {
                EnumSet<ProcessShellFactory.TtyOptions> ttyOptions;
                if (OsUtils.isUNIX()) {
                    ttyOptions = EnumSet.of(ProcessShellFactory.TtyOptions.ONlCr);
                } else {
                    ttyOptions = EnumSet.of(ProcessShellFactory.TtyOptions.Echo, ProcessShellFactory.TtyOptions.ICrNl, ProcessShellFactory.TtyOptions.ONlCr);
                }
                return new ProcessShellFactory(command.split(" "), ttyOptions).create();
            }
        }));
        if (OsUtils.isUNIX()) {
            sshd.setShellFactory(new ProcessShellFactory(new String[]{"/bin/sh", "-i", "-l"},
                    EnumSet.of(ProcessShellFactory.TtyOptions.ONlCr)));
        } else {
            sshd.setShellFactory(new ProcessShellFactory(new String[]{"cmd.exe "},
                    EnumSet.of(ProcessShellFactory.TtyOptions.Echo, ProcessShellFactory.TtyOptions.ICrNl, ProcessShellFactory.TtyOptions.ONlCr)));
        }
//		if(SecurityUtils.isBouncyCastleRegistered()) {
//			sshd.setKeyPairProvider(new PEMGeneratorHostKeyProvider(System.getProperty("java.io.tmpdir") + "/key.pem"));
//		} else {
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(System.getProperty("java.io.tmpdir") + "/key.ser"));
//		}
        sshd.setPasswordAuthenticator(new PasswordAuthenticator() {

            @Override
            public boolean authenticate(String username, String password, ServerSession session) {
                return (SSH_USERNAME.equals(username) && SSH_PASSWORD.equals(password));
            }
        });
        sshd.setPublickeyAuthenticator(new PublickeyAuthenticator() {

            public boolean authenticate(String username, PublicKey key, ServerSession session) {
                return true;
            }
        });
        sshd.getProperties().put(SshServer.WELCOME_BANNER, "Welcome to SSHD\n");
        startServer0();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    private void startServer0() throws IOException {
        boolean serverStarted = false;
        IOException exception = null;
        while (!serverStarted && (sshPort < Integer.MAX_VALUE)) {
            try {
                System.out.println("Starting SSH server on port [" + sshPort + "]. [" + getClass().getName() + "#" + System.identityHashCode(this) + "]");
                sshd.setPort(sshPort);
                sshd.start();
                serverStarted = true;
            } catch (BindException e) {
                System.err.println("Cannot start SSH server on port [" + sshPort + "]. " + e.getMessage());
                if (exception == null) {
                    // store first thrown exception - will be thrown if cannot start the server
                    exception = e;
                }
                sshPort++;
            }
        }
        if (!serverStarted) {
            throw exception;
        }
        System.out.println("SSH server started on port [" + sshPort + "]. [" + getClass().getName() + "#" + System.identityHashCode(this) + "]");
    }

    private void stopServer() {
        try {
            if (sshd != null) {
                sshd.stop(true);
                System.out.println("SSH server stopped on port [" + sshPort + "]. [" + getClass().getName() + "#" + System.identityHashCode(this) + "]");
            }
        } catch (InterruptedException e) {
            System.err.println("=> Error stopping SSH server.");
            e.printStackTrace();
        } finally {
            sshd = null;
        }
    }
}
