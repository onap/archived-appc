/*-
 * ============LICENSE_START=======================================================
 * ONAP : APP-C
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property.  All rights reserved.
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
package org.onap.appc.artifact.handler.sftp;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

public class SftpSession {
    private static final EELFLogger log = EELFManager.getInstance().getLogger(SftpConnect.class);
    private Session session;
    private ChannelSftp sftp;
    private String host;
    private String userId;
    private String privateKeyPath;
    private int MAX_TRIES = 3;

    public Session getSession() {
        return session;
    }

    public ChannelSftp getSftp() {
        return sftp;
    }

    public void startSession(String host, String userId, String privateKeyPath) throws SvcLogicException {
        try {
            log.info("hostname:" + host + "userId:" + userId + "privateKeyPath:" + privateKeyPath);
            JSch jsch = new JSch();
            jsch.addIdentity(privateKeyPath);
            log.info("jsch created");
            this.session = jsch.getSession(userId, host);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            config.put("ConnectionAttempts", "3");
            log.info("before creating SFTP Session.");
            this.session.setConfig(config);
            for (int i = 0; i < MAX_TRIES; i++) {
                try {
                    this.session.connect();
                    log.info("APPC-MESSAGE:SFTP Session Created ");
                    break;
                } catch (JSchException ex) {
                    if (i == MAX_TRIES - 1) {
                        log.error("Error while connetcing to host" + host + "Attempted" + i + " more time(s).");
                        throw ex;
                    }
                    continue;
                }
            }
            log.info("Before channel  Created ");
            Channel channel = this.session.openChannel("sftp");
            channel.connect();
            this.sftp = (ChannelSftp) channel;
            log.info("APPC-MESSAGE:SFTP Channel Connected ");
        } catch (Exception e) {
            log.info("APPC-MESSAGE:" + e.getMessage());
            throw new SvcLogicException(e.getMessage());

        }
    }

    public void stopSession() throws Exception {
        try {
            if (this.sftp != null && this.sftp.isConnected()) {
                log.info("APPC-MESSAGE:SFTP Channel Disconnected ");
                this.sftp.disconnect();

            }
        } catch (Exception e) {
            throw new SvcLogicException(e.getMessage());
        }
    }

}
