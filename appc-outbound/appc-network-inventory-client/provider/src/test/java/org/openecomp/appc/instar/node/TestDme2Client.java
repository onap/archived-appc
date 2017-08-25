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

package org.openecomp.appc.instar.node;

import java.util.HashMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.appc.instar.dme2client.Dme2Client;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class TestDme2Client {
    
    @Test(expected=Exception.class)
    public void testSendtoInstar() throws Exception{
        
        HashMap<String,String> data = new HashMap<String,String>();
        data.put("subtext","value");
        PowerMockito.mockStatic(System.class);
        PowerMockito.when((System.getenv("test"))).thenReturn("test");
        Dme2Client dme2 = new Dme2Client("opt","subtext",data);
    }
}
