/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 IBM.
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

package org.onap.appc.flow.controller.executorImpl;

import static org.junit.Assert.assertTrue;

import java.security.cert.X509Certificate;

import org.junit.Before;
import org.junit.Test;

public class SecureRestClientTrustManagerTest {
    private SecureRestClientTrustManager secureRestClientTrustManager;

    @Before
    public void setUp() {
        secureRestClientTrustManager = new SecureRestClientTrustManager();
    }

    @Test
    public void testGetAcceptedIssuers() {
        assertTrue(secureRestClientTrustManager.getAcceptedIssuers() instanceof X509Certificate[]);
    }

    @Test
    public void testIsClientTrusted() {
        X509Certificate[] arg0 = {};
        assertTrue(secureRestClientTrustManager.isClientTrusted(arg0));
    }

    @Test
    public void testIsServerTrusted() {
        X509Certificate[] arg0 = {};
        assertTrue(secureRestClientTrustManager.isServerTrusted(arg0));
    }

}
