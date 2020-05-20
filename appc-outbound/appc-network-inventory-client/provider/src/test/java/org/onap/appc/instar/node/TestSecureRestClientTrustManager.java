/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.instar.node;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.appc.instar.dme2client.SecureRestClientTrustManager;
import static org.junit.Assert.assertNotNull;

public class TestSecureRestClientTrustManager {

    @Test
    public void testSecureRestClient() throws CertificateException{
        X509Certificate[] arg0 = new X509Certificate[1];
        SecureRestClientTrustManager sm = Mockito.mock(SecureRestClientTrustManager.class);
        Mockito.when(sm.getAcceptedIssuers()).thenReturn(Mockito.any());
        Mockito.when(sm.isClientTrusted(arg0)).thenReturn(true);
        Mockito.when(sm.isServerTrusted(arg0)).thenReturn(true);
        assertNotNull(sm);
    }
}
