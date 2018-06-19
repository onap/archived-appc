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
import java.io.*;
import java.net.InetAddress;
import java.util.*;
import org.apache.commons.io.FileUtils;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import com.jcraft.jsch.ChannelSftp;

public class SftpConnect {

    private static final String SDNC_CONFIG_DIR = "SDNC_CONFIG_DIR";
    private static final String APPC_PROPS = "/appc.properties";
    private  final EELFLogger log = EELFManager.getInstance().getLogger(SftpConnect.class);
    private static final String srcDir = "/opt/appcauth";
	private static final String descDir = "/opt/appcauth";
    private static final String propDir = System.getenv(SDNC_CONFIG_DIR);
    private static final String privateKeyPath = "/opt/app/bvc/tls/bvc-user.pem";
    private  String userId =null;
    private  String hostNames =null;

    public  void performSftp(SvcLogicContext ctx) throws SvcLogicException {
        String path = propDir + APPC_PROPS;
        try {
            File propFile = new File(path);
                if (!propFile.exists()) {
                    log.error("APPC-MESSAGE:Missing configuration properties file : " + propFile);
                }
                Properties properties = new Properties();
                InputStream input = new FileInputStream(propFile);
                properties.load(input);
                hostNames = properties.getProperty("appc.southbound.sync.hostnames");
                userId = properties.getProperty("appc.southbound.sync.user");
			
				   log.info("hostnamesList:"+hostNames+"userId:"+userId); 
				   if(hostNames!=null && !hostNames.isEmpty())
       {
        String[] hosts = hostNames.split(",");
        String currentHost = InetAddress.getLocalHost().getCanonicalHostName();
		 log.info("currentHost"+currentHost);
        for (String host : hosts) {
            if (!host.equals(currentHost)) 
                {
                    log.info("before sftp session");
                   SftpSession sftp = new SftpSession();
			    sftp.startSession(host, userId, privateKeyPath);
                    if (sftp.getSession().isConnected()) {
						ChannelSftp conn = sftp.getSftp();
                        log.info("APPC-MESSAGE:Sending Source File from:"+  currentHost + " to " + host );
                        InputStream stream = FileUtils.openInputStream(new File(srcDir + "/appc_southbound.properties"));
                        conn.rename(srcDir + "/appc_southbound.properties", descDir + "/appc_southbound.properties_bck");
                        conn.put(stream, descDir + "/appc_southbound.properties",conn.OVERWRITE);
						conn.chmod(Integer.parseInt("664", 8), descDir + "/appc_southbound.properties");
                        conn.chgrp(492746, descDir + "/appc_southbound.properties");
						sftp.stopSession();
                        
                    }
            }
        }
	   }
    }
         catch (Exception e) {
                ctx.setAttribute("error-code", "401");
                 ctx.setAttribute("error-message", e.getMessage().toString());
                   throw new SvcLogicException("Error While transfering  file: " + e.getMessage());
        }
    }
}
