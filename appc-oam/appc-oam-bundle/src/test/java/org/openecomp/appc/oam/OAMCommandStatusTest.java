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

package org.openecomp.appc.oam;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.appc.executor.objects.Params;

import java.util.HashMap;
import java.util.Map;

public class OAMCommandStatusTest {
    private Map<OAMCommandStatus, Integer> CODE_MAP = new HashMap<OAMCommandStatus, Integer>() {
        {
            put(OAMCommandStatus.ABORT,             304);
            put(OAMCommandStatus.ACCEPTED,          100);
            put(OAMCommandStatus.INVALID_PARAMETER, 302);
            put(OAMCommandStatus.REJECTED,          300);
            put(OAMCommandStatus.SUCCESS,           400);
            put(OAMCommandStatus.TIMEOUT,           303);
            put(OAMCommandStatus.UNEXPECTED_ERROR,  200);
        }
    };
    private Map<OAMCommandStatus, String> MSG_MAP = new HashMap<OAMCommandStatus, String>() {
        {
            put(OAMCommandStatus.ABORT,             "OPERATION ABORT - ${errorMsg}");
            put(OAMCommandStatus.ACCEPTED,          "ACCEPTED - request accepted");
            put(OAMCommandStatus.INVALID_PARAMETER, "INVALID PARAMETER - ${errorMsg}");
            put(OAMCommandStatus.REJECTED,          "REJECTED - ${errorMsg}");
            put(OAMCommandStatus.SUCCESS,           "SUCCESS - request has been processed successfully");
            put(OAMCommandStatus.TIMEOUT,           "OPERATION TIMEOUT REACHED - ${errorMsg}");
            put(OAMCommandStatus.UNEXPECTED_ERROR,  "UNEXPECTED ERROR - ${errorMsg}");
        }
    };

    @Test
    public void testGetResponseMessage() throws Exception {
        for (OAMCommandStatus oamCommandStatus : OAMCommandStatus.values()) {
            String expectedMsg = MSG_MAP.get(oamCommandStatus);
            Assert.assertEquals(String.format("Should have message (%s).", expectedMsg),
                    expectedMsg, oamCommandStatus.getResponseMessage());
        }
    }

    @Test
    public void testGetResponseCode() throws Exception {
        for (OAMCommandStatus oamCommandStatus : OAMCommandStatus.values()) {
            Integer expectedCode = CODE_MAP.get(oamCommandStatus);
            Assert.assertEquals(String.format("Should have code (%d).", expectedCode),
                    expectedCode, Integer.valueOf(oamCommandStatus.getResponseCode()));
        }
    }

    @Test
    public void testGetFormattedMessage() throws Exception {
        String message = "test message";
        Params params = new Params().addParam("errorMsg", message);
        for (OAMCommandStatus oamCommandStatus : OAMCommandStatus.values()) {
            String expectedMsg1 = MSG_MAP.get(oamCommandStatus);
            String expectedMsg2 = expectedMsg1.replaceAll("\\$\\{errorMsg\\}", message);
            Assert.assertEquals("Should returned " + expectedMsg1,
                    expectedMsg1, oamCommandStatus.getFormattedMessage(null));
            Assert.assertEquals("Should returned " + expectedMsg2,
                    expectedMsg2, oamCommandStatus.getFormattedMessage(params));
        }
    }

    @Test
    public void testToString() throws Exception {
        for (OAMCommandStatus oamCommandStatus : OAMCommandStatus.values()) {
            String expectedString = String.format(oamCommandStatus.TO_STRING_FORMAT,
                    CODE_MAP.get(oamCommandStatus), MSG_MAP.get(oamCommandStatus));
            Assert.assertEquals(String.format("Should have string (%s).", expectedString),
                    expectedString, oamCommandStatus.toString());
        }
    }
}
