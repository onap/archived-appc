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

import org.openecomp.appc.configuration.ConfigurationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.*;

/**
 * Provides basic methods for exchanging netconf messages.
 */
public class NetconfAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(NetconfAdapter.class);
    private static final long MAX_WAITING_TIME = 1800000;
    private static ExecutorService executor = Executors.newFixedThreadPool(5);

    // device input stream
    private InputStream in;
    // device output stream
    private OutputStream out;
    private long maxWaitingTime = ConfigurationFactory.getConfiguration().getLongProperty("org.openecomp.appc.netconf.recv.timeout", MAX_WAITING_TIME);

    /**
     * Constructor.
     *
     * @param in  InputStream this instance will read netconf messages from
     * @param out OutputStream this instance will write netconf messages to
     * @throws IOException
     */
    public NetconfAdapter(InputStream in, OutputStream out) throws IOException {
        this.in = in;
        this.out = out;
    }

    /**
     * Receives netconf message from InputStream and return it's text (without netconf frame characters).
     *
     * @return text of message received from netconf device
     * @throws IOException
     */
    public String receiveMessage() throws IOException {

        final NetconfMessage message = new NetconfMessage();
        final byte[] buf = new byte[1024];

        //int readByte = 1;
        // Read data with timeout
        Callable<Boolean> readTask = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                int c;
                while ((c = in.read(buf)) > 0) {
                    if (c > 0) {
                        message.append(buf, 0, c);
                        if (message.isCompleted()) {
                            break;
                        }
                    }
                }

                if (c < 0) {
                    return false;
                }
                return true;
            }
        };

        Future<Boolean> future = executor.submit(readTask);
        Boolean status;
        try {
            status = future.get(maxWaitingTime, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new IOException(e);
        }

        if (status == false) {
            throw new IOException("Failed to read netconf message");
        }


        String text = message.getText();
        if (text != null) {
            text = text.trim();
        }
        if (LOG.isDebugEnabled()) {
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("Sending message to netconf device:\n" + text);
        }
        out.write(new NetconfMessage(text).getFrame());
        out.flush();
    }
}
