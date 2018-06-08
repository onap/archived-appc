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

package org.onap.appc.provider.lcm.mock;

import org.onap.appc.executor.objects.LCMCommandStatus;
import org.onap.appc.requesthandler.objects.RequestHandlerInput;
import org.onap.appc.requesthandler.objects.RequestHandlerOutput;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.onap.appc.util.JsonUtil;
import java.io.IOException;

import java.util.Map;


/**
 * This class is here because LCM quiesce backend is not implemented.
 * Hence this class is here to mock the handling response of LCM quiesce REST API.
 *
 * When backend is implemented, this file should be removed.
 */
public class MockQuiesceHelper extends AbstractMockHelper {
    private final String MOCK_QUIESCE_DIR = "/tmp/lcm/quiescetraffic";
    private final String PAUSE = "pause";

    public RequestHandlerOutput quiesceTraffic(RequestHandlerInput input) {
        if (!mockConditionExists()) {
            status = buildStatusForErrorMsg(LCMCommandStatus.REJECTED,
                "The quiesce-traffic command is not supported");
            return setOutputStatus();
        }

        String vnfId = input.getRequestContext().getActionIdentifiers().getVnfId();
        String vnfPath = String.format("%s/%s", MOCK_QUIESCE_DIR, vnfId);
        if (!isDirectoryExist(vnfPath)) {
            status = buildStatusForVnfId(LCMCommandStatus.VNF_NOT_FOUND, vnfId);
            return setOutputStatus();
        }
        
        Map<String, String> jsonMap = getJsonMap(input.getRequestContext().getPayload());
        if (jsonMap == null) {
            status = buildStatusForErrorMsg(LCMCommandStatus.UNEXPECTED_ERROR, "payload reading failed");
            return setOutputStatus();
        }

        String pausePath = String.format("%s/%s", vnfPath, PAUSE);
        if (isDirectoryExist(pausePath)) {
            status = buildStatusForErrorMsg(LCMCommandStatus.REJECTED,
                String.format("VNF %s is already quiesced", vnfId));
            return setOutputStatus();
        }

        File pauseDir = new File(pausePath);
        boolean success = pauseDir.mkdir();
        status = success ?
            buildStatusWithoutParams(LCMCommandStatus.ACCEPTED) :
            buildStatusForErrorMsg(LCMCommandStatus.REJECTED,
                String.format("Failed to quiesce traffic VNF %s", vnfId));

        return setOutputStatus();
    }

    private boolean mockConditionExists() {
        return isDirectoryExist(MOCK_QUIESCE_DIR);
    }

    private boolean isDirectoryExist(String path) {
        return Files.isDirectory(Paths.get(path));
    }
    
    private Map<String, String> getJsonMap(String jsonString) {
        try {
            return JsonUtil.convertJsonStringToFlatMap(jsonString);
        } catch (IOException e) {
            logger.error(String.format("MockQuiesceHelper got exception when convert json map for (%s)", jsonString), e);
        }
        return null;
    }
}
