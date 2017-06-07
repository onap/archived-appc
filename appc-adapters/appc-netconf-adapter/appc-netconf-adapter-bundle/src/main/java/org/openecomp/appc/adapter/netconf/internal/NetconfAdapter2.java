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

package org.openecomp.appc.adapter.netconf.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Provides basic methods for exchanging netconf messages.
 */
public class NetconfAdapter2 {

    private static final Logger LOG = LoggerFactory.getLogger(NetconfAdapter2.class);

    // device input pipe
    private final PipedOutputStream pipedOutIn = new PipedOutputStream();
    private PipedInputStream in;
    // device output pipe
    private final PipedInputStream pipedInOut = new PipedInputStream();
    private PipedOutputStream out;

    /**
     * Constructor.
     *
     * @throws IOException
     */
    public NetconfAdapter2() throws IOException {
        in = new PipedInputStream(pipedOutIn);
        out = new PipedOutputStream(pipedInOut);
    }

    /**
     * @return InputStream this instance will read netconf messages from.
     */
    public InputStream getIn() {
        return in;
    }

    /**
     * @return OutputStream this instance will write netconf messages to.
     */
    public OutputStream getOut() {
        return out;
    }

    /**
     * Receives netconf message from InputStream and return it's text (without netconf frame characters).
     *
     * @return text of message received from netconf device
     * @throws IOException
     */
    public String receiveMessage() throws IOException {
        NetconfMessage message = new NetconfMessage();
        byte[] buf = new byte[1024];
        int c;
        while((c = pipedInOut.read(buf)) > 0) {
                message.append(buf, 0, c);
                if (message.isCompleted()) {
                    break;
                }
        }
        String text = message.getText();
        if(LOG.isDebugEnabled()) {
            LOG.debug("Received message from netconf device:\n" + text);
        }
        return text;
    }

    /**
     * Sends netconf message with provided text (adds netconf frame characters and sends the message).
     *
     * @param text text of message to be sent to netconf device
     * @throws IOException
     */
    public void sendMessage(final String text) throws IOException {
        if(LOG.isDebugEnabled()) {
            LOG.debug("Sending message to netconf device:\n" + text);
        }
        pipedOutIn.write(new NetconfMessage(text).getFrame());
    }
}
