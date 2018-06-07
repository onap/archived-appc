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

package org.onap.appc.design.xinterface;

import java.util.HashMap;

import org.onap.appc.design.services.util.DesignServiceConstants;
import org.onap.appc.instar.dme2client.Dme2Client;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class XResponseProcessor {

   private final EELFLogger log = EELFManager.getInstance().getLogger(XInterfaceService.class);
    Dme2Client dme2Client;

    public String parseResponse(String execute, String action) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode payloadObject = objectMapper.readTree(execute);
        log.info("payloadObject " + payloadObject);

        //String queryParam = null;
        String instarResponse = null;
        HashMap<String, String> payload = null;
        String ipAddress = null;
        String mask = null;

        try {

            // check the payload whether its having ipaddr along with subnet
            ipAddress = payloadObject.get(DesignServiceConstants.INSTAR_V4_ADDRESS) != null
                    ? payloadObject.get(DesignServiceConstants.INSTAR_V4_ADDRESS).textValue()
                    : (payloadObject.get(DesignServiceConstants.INSTAR_V6_ADDRESS) !=null)
                        ?payloadObject.get(DesignServiceConstants.INSTAR_V6_ADDRESS).textValue().toUpperCase()
                                :null;

            mask = payloadObject.get(DesignServiceConstants.INSTAR_V4_MASK) != null
                    ? payloadObject.get(DesignServiceConstants.INSTAR_V4_MASK).textValue()
                    : (payloadObject.get(DesignServiceConstants.INSTAR_V6_MASK) != null)
                            ? payloadObject.get(DesignServiceConstants.INSTAR_V6_MASK).textValue().toUpperCase()
                            : null;

            // TODO -short format

            /*if (mask != null) {
                queryParam = ipAddress + "," +mask ;
                log.info("Calling Instar with IpAddress "+ ipAddress + " Mask value: "+ mask );
            } else {
                queryParam = "ipAddress "+ipAddress ;
                log.info("Calling Instar with IpAddress "+ ipAddress);
            }*/

            payload = new HashMap<String, String>();
            payload.put("ipAddress", ipAddress);
            payload.put("mask", mask);
            log.info("Calling Instar with IpAddress "+ ipAddress + " Mask value: "+ mask );
            dme2Client = new Dme2Client("getVnfbyIpadress", "payload", payload);

            instarResponse = dme2Client.send();

            log.debug("Resposne from Instar = " + instarResponse);
            if (instarResponse == null || instarResponse.length() < 0)
                throw new Exception("No Data received from Instar for this action " + action);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return instarResponse;
    }
}
