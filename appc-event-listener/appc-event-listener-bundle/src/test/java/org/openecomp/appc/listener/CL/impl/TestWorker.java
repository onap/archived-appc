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
 */

package org.openecomp.appc.listener.CL.impl;

import static org.junit.Assert.assertNotNull;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.appc.configuration.ConfigurationFactory;
import org.openecomp.appc.listener.EventHandler;
import org.openecomp.appc.listener.ListenerProperties;
import org.openecomp.appc.listener.CL.impl.ProviderOperations;
import org.openecomp.appc.listener.CL.impl.WorkerImpl;
import org.openecomp.appc.listener.CL.model.IncomingMessage;
import org.openecomp.appc.listener.impl.EventHandlerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestWorker {

    private static final Logger LOG = LoggerFactory.getLogger(WorkerImpl.class);

    private IncomingMessage msg;
    private EventHandler dmaap;

    @Before
    public void setup() {
        Properties props = ConfigurationFactory.getConfiguration().getProperties();
        String activeEndpoint = props.getProperty("appc.ClosedLoop.provider.url");
        assertNotNull(activeEndpoint);
        ProviderOperations.setUrl(activeEndpoint);

        String vmUrl = props.getProperty("test.vm_url");
        assertNotNull(vmUrl);
        msg = new IncomingMessage();
        // Client and Time are for ID
        msg.setRequestClient("APPC");
        msg.setRequestTime("TEST");
        msg.setRequest("Restart");
        msg.setUrl(vmUrl);

        dmaap = new EventHandlerImpl(new ListenerProperties("appc.ClosedLoop", props));
    }

    @Test
    public void testWorker() {
        WorkerImpl w = new WorkerImpl(msg, dmaap);
        w.run();
    }

}
