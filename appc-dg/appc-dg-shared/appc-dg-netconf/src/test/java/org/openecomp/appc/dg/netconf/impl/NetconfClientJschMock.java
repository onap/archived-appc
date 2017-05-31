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

package org.openecomp.appc.dg.netconf.impl;

import org.openecomp.appc.adapter.netconf.NetconfClient;
import org.openecomp.appc.adapter.netconf.NetconfConnectionDetails;
import org.openecomp.appc.exceptions.APPCException;


public class NetconfClientJschMock implements NetconfClient {

    private boolean connection;
    private String lastMessage;
    private String answer = "answer";
    private String configuration;
    private NetconfConnectionDetails lastConnectionDetails;

    public boolean isConnection() {
        return connection;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getAnswer() {
        return answer;
    }

    public String getConf() {
        return configuration;
    }

    public void setConf(String configuration) {
        this.configuration = configuration;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public NetconfConnectionDetails getLastConnectionDetails() {
        return lastConnectionDetails;
    }

    @Override
    public void connect(NetconfConnectionDetails connectionDetails) throws APPCException {
        this.connection = true;
        this.lastConnectionDetails = connectionDetails;

    }

    @Override
    public String exchangeMessage(String message) throws APPCException {
        if (connection) {
            this.lastMessage = message;
            return answer;
        } else return null;
    }

    @Override
    public void configure(String configuration) throws APPCException {
        if (connection) {
            this.configuration = configuration;
        }

    }

    @Override
    public String getConfiguration() throws APPCException {
        if (connection) {
            return configuration;
        } else return null;
    }

    @Override
    public void disconnect() throws APPCException {
        this.connection = false;

    }
}
