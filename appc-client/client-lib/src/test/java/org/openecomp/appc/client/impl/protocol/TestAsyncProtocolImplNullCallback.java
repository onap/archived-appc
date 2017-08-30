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

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.appc.client.impl.protocol.AsyncProtocol;
import org.openecomp.appc.client.impl.protocol.AsyncProtocolImpl;
import org.openecomp.appc.client.impl.protocol.ProtocolException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TestAsyncProtocolImplNullCallback {

    private static AsyncProtocol protocol;

    public void testSetUpNoCallback() throws IOException {

        Properties props = new Properties();
        String propFileName = "ueb.properties";

        InputStream input = TestAsyncProtocolImpl.class.getClassLoader().getResourceAsStream(propFileName);

        props.load(input);

        protocol = new AsyncProtocolImpl();

        try {
            protocol.init(props, null);
        } catch (ProtocolException e) {
            Assert.assertTrue(true);
        } catch (Exception e){
            Assert.assertFalse(e.getMessage(),false);
        }
    }
}
