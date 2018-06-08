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

package org.onap.appc.instar.interfaceImpl;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import java.io.IOException;
import java.util.Map;
import org.onap.appc.instar.dme2client.Dme2Client;
import org.onap.appc.instar.interfaces.RestClientInterface;
import org.onap.appc.instar.utils.InstarClientConstant;

public class InstarRestClientImpl implements RestClientInterface {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(InstarRestClientImpl.class);
    private Map<String, String> requestData = null;
    private Dme2Client dme2Client;

    public InstarRestClientImpl(Map<String, String> instarRequestData) {

        this.requestData = instarRequestData;
    }

    @Override
    public String sendRequest(String operation) throws InstarResponseException, IOException {

        String instarResponse;
        try {
            if (operation != null && operation
                .equalsIgnoreCase(InstarClientConstant.OPERATION_GET_IPADDRESS_BY_VNF_NAME)) {
                dme2Client = new Dme2Client(operation, InstarClientConstant.VNF_NAME, requestData);
            }
            instarResponse = dme2Client.send();
            log.info("Resposne in InstarRestClientImpl = " + instarResponse);
            if (instarResponse == null || instarResponse.isEmpty()) {
                throw new InstarResponseException("No Data received from Instar for this call " + operation);
            }
        } catch (Exception e) {
            throw e;
        }
        return instarResponse;
    }
}
