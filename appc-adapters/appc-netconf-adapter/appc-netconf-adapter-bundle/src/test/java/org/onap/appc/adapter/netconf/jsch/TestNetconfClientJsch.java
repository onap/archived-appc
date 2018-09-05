/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 Samsung
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
 *
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.adapter.netconf.jsch;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.onap.appc.adapter.netconf.NetconfConnectionDetails;
import org.onap.appc.adapter.netconf.internal.NetconfAdapter;
import org.onap.appc.exceptions.APPCException;

import java.io.IOException;

public class TestNetconfClientJsch {

    NetconfClientJsch netconfClientJsch;

    @Before
    public void SetUp() {
        netconfClientJsch = new NetconfClientJsch();
    }

    @Test (expected = APPCException.class)
    public void testConnect() throws APPCException, IOException {
        NetconfConnectionDetails connectionDetails = new NetconfConnectionDetails();
        connectionDetails.setHost("test");
        connectionDetails.setPort(8080);
        connectionDetails.setUsername("test");
        connectionDetails.setPassword("test");

        netconfClientJsch.connect(connectionDetails);
    }

    @Test (expected = NullPointerException.class)
    public void testExchangeMessage() throws APPCException, IOException {
        String message = "test";

        netconfClientJsch.exchangeMessage(message);
    }

    @Test (expected = NullPointerException.class)
    public void testConfigure() throws APPCException, IOException {
        String message = "test";

        netconfClientJsch.configure(message);
    }

    @Test (expected = NullPointerException.class)
    public void testConfigureOk() throws APPCException, IOException {
        String message = "<ok/>";

        netconfClientJsch.configure(message);
    }

    @Test (expected = NullPointerException.class)
    public void testConfigureNull() throws APPCException, IOException {
        String message = null;

        netconfClientJsch.configure(message);
    }
}
