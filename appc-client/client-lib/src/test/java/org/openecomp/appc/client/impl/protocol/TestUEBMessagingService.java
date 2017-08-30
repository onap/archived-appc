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

import org.junit.*;
import org.junit.runners.MethodSorters;
import org.openecomp.appc.client.impl.protocol.MessagingService;
import org.openecomp.appc.client.impl.protocol.UEBMessagingService;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Properties;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestUEBMessagingService {

    private static MessagingService ueb;

    public static void setUp() throws IOException, GeneralSecurityException, NoSuchFieldException, IllegalAccessException {

        Properties props = new Properties();
        String propFileName = "ueb.properties";

        InputStream input = TestUEBMessagingService.class.getClassLoader().getResourceAsStream(propFileName);

        props.load(input);

        ueb = new UEBMessagingService();
        ueb.init(props);
    }

    public void test1Send() throws IOException {
    System.out.println("Here");

        String message = "Test Message Service";
        ueb.send(null,message);
    }

    public void test2Fetch() throws IOException {

    System.out.println("Here2");
        List<String> messages = ueb.fetch(1);
        Assert.assertEquals(1,messages.size());
    }

}
