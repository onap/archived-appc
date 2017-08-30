/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.appc.client.impl.protocol;

import org.openecomp.appc.client.impl.core.MessageContext;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

class APPCMessageReaderWriter implements MessageReader, MessageWriter {

    private final ObjectMapper mapper;
    private final EELFLogger LOG = EELFManager.getInstance().getLogger(APPCMessageReaderWriter.class);

    APPCMessageReaderWriter() {
        mapper = new ObjectMapper();
    }

    public String read(String payload, MessageContext context) throws ProtocolException {
        try {
            ProtocolMessage protocolMessage = mapper.readValue(payload, ProtocolMessage.class);
            context.setType(protocolMessage.getType());
            context.setRpc(protocolMessage.getRpcName());
            context.setCorrelationID(protocolMessage.getCorrelationID());
            context.setPartiton(protocolMessage.getPartition());
            String body = protocolMessage.getBody().toString();
            LOG.debug("Received body : <" + body + ">");
            return body;
        } catch (IOException e) {
            throw new ProtocolException(e);
        }

    }

    public String write(String payload, MessageContext context) throws ProtocolException {
        try {
            ProtocolMessage protocolMessage = new ProtocolMessage();
            protocolMessage.setVersion("2.0");
            protocolMessage.setType(context.getType());
            protocolMessage.setRpcName(context.getRpc());
            protocolMessage.setCorrelationID(context.getCorrelationID());
            protocolMessage.setPartition(context.getPartiton());
            JsonNode body = mapper.readTree(payload);
            protocolMessage.setBody(body);
            String message = mapper.writeValueAsString(protocolMessage);
            return message;
        } catch (IOException e) {
            throw new ProtocolException(e);
        }
    }

}
