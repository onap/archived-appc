/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.onap.appc.artifact.handler.sftp;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
/**
 *
 * @author km583p
 */
public class SftpSession {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(SftpConnect.class);
    private Session session;
    private ChannelSftp sftp;
    private  String host;
    private  String userId;
    private  String privateKeyPath;
    private  int MAX_TRIES = 3;

    public Session getSession() {
        return session;
    }

    public ChannelSftp getSftp() {
        return sftp;
    }


    public void startSession(String host,  String userId, String privateKeyPath) throws SvcLogicException {
        try {
            log.info("hostname:"+host+"userId:"+userId+"privateKeyPath:"+privateKeyPath);
            JSch jsch = new JSch();
            jsch.addIdentity(privateKeyPath);
			log.info("jsch created");
            this.session = jsch.getSession(userId, host);
	    java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            config.put("ConnectionAttempts", "3");
             log.info("before creating SFTP Session ");
            this.session.setConfig(config);
            for (int i = 0; i < MAX_TRIES; i++) {
                try {
                    this.session.connect();
                    log.info("APPC-MESSAGE:SFTP Session Created ");
                    break;
                } catch (JSchException ex) {
                    if (i == MAX_TRIES - 1) {
                        log.error("Error while connetcing to host"+host+"Attempted"+i+" more time(s).");
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
