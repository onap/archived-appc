/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications Copyright (C) 2018 Ericsson
 * =============================================================================
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
 * 
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.adapter.netconf.jsch;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSubsystem;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Properties;

import org.onap.appc.adapter.netconf.NetconfClient;
import org.onap.appc.adapter.netconf.NetconfConnectionDetails;
import org.onap.appc.adapter.netconf.internal.NetconfAdapter;
import org.onap.appc.adapter.netconf.internal.NetconfConstMessages;
import org.onap.appc.encryption.EncryptionTool;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.i18n.Msg;
import com.att.eelf.i18n.EELFResourceManager;

/**
 * Implementation of NetconfClient interface based on JCraft jsch library.
 */
public class NetconfClientJsch implements NetconfClient {

    private static final int SESSION_CONNECT_TIMEOUT = 30000;
    private static final int CHANNEL_CONNECT_TIMEOUT = 10000;

    private Session session;
    private Channel channel;
    private NetconfAdapter netconfAdapter;


    @Override
    public void connect(NetconfConnectionDetails connectionDetails) throws APPCException {
        String host = connectionDetails.getHost();
        int port = connectionDetails.getPort();
        String username = connectionDetails.getUsername();
        String password = connectionDetails.getPassword();
        try {
            JSch.setLogger(new JSchLogger());
            JSch jsch = getJSch();
            session = jsch.getSession(EncryptionTool.getInstance().decrypt(username), host, port);
            session.setPassword(EncryptionTool.getInstance().decrypt(password));
            session.setConfig("StrictHostKeyChecking", "no");

            Properties additionalProps = connectionDetails.getAdditionalProperties();
            if((additionalProps != null) && !additionalProps.isEmpty()) {
                session.setConfig(additionalProps);
            }

            session.connect(SESSION_CONNECT_TIMEOUT);
            session.setTimeout(10000);

            createConnection(connectionDetails);

        } catch(Exception e) {
            String message = EELFResourceManager.format(Msg.CANNOT_ESTABLISH_CONNECTION, host, String.valueOf(port), username);
            throw new APPCException(message, e);
        }
    }

    @Override
    public String exchangeMessage(String message) throws APPCException {
        try {
            netconfAdapter.sendMessage(message);
            return netconfAdapter.receiveMessage();
        } catch(IOException e) {
            throw new APPCException(e);
        }
    }

    @Override
    public void configure(String configuration) throws APPCException {
        try {
            isOk(exchangeMessage(configuration));
        } catch(IOException e) {
            throw new APPCException(e);
        }
    }

    @Override
    public String getConfiguration() throws APPCException {
        return exchangeMessage(NetconfConstMessages.GET_RUNNING_CONFIG);
    }

    @Override
    public void disconnect() {
        try {
            if((channel != null) && !channel.isClosed()) {
                netconfAdapter.sendMessage(NetconfConstMessages.CLOSE_SESSION);
                isOk(netconfAdapter.receiveMessage());
            }
        } catch(IOException e) {
            throw new RuntimeException("Error closing netconf device", e);
        } finally {
            netconfAdapter = null;
            if(channel != null) {
                channel.disconnect();
                channel = null;
            }
            if(session != null) {
                session.disconnect();
                session = null;
            }
        }
    }

    private void createConnection(NetconfConnectionDetails connectionDetails) throws APPCException {
        try {
            channel = session.openChannel("subsystem");
            ((ChannelSubsystem)channel).setSubsystem("netconf");
            netconfAdapter = getNetconfAdapter(channel.getInputStream(), channel.getOutputStream());
            channel.connect(CHANNEL_CONNECT_TIMEOUT);
            hello(connectionDetails.getCapabilities());
        } catch(Exception e) {
            disconnect();
            throw new APPCException(e);
        }
    }

    private void hello(List<String> capabilities) throws IOException {
        String helloIn = netconfAdapter.receiveMessage();
        if(helloIn == null) {
            throw new IOException("Expected hello message, but nothing received from netconf device");
        }
        if(helloIn.contains("<rpc-error>")) {
            throw new IOException("Expected hello message, but received error from netconf device:\n" + helloIn);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(NetconfConstMessages.CAPABILITIES_START);
        sb.append(NetconfConstMessages.CAPABILITIES_BASE);
        if(capabilities != null) {
            for(String capability: capabilities) {
                sb.append("    ").append(capability).append("\n");
            }
        }
        sb.append(NetconfConstMessages.CAPABILITIES_END);
        String helloOut = sb.toString();
        netconfAdapter.sendMessage(helloOut);
    }

    private void isOk(String response) throws IOException {
        if(response == null) {
            throw new IOException("No response from netconf device");
        }
        if(!response.contains("<ok/>")) {
            throw new IOException("Error response from netconf device: \n" + response);
        }
    }
    
    protected JSch getJSch() {
        return new JSch();
    }
    
    protected NetconfAdapter getNetconfAdapter(InputStream inputStream, OutputStream outputStream) throws IOException {
        return new NetconfAdapter(inputStream, outputStream);
    }
}
