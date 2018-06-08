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
import org.onap.appc.util.JsonUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 * This class is here because LCM attachVolume and detachVolume backends are not implemented.
 * Hence this class is here to mock the handling response of LCM attachVolume and detachVolume REST API.
 *
 * When backend is implemented, this file should be removed.
 */
public class MockVolumeHelper extends AbstractMockHelper {
    private final String MOCK_VOLUME_DIR = "/tmp/lcm/volume";
    private final String VOLUME_ID_KEY = "volumeAttachment.volumeId";

    public RequestHandlerOutput attachVolume(RequestHandlerInput input) {
        if (!mockConditionExists()) {
            status = buildStatusForErrorMsg(LCMCommandStatus.REJECTED,
                "The attach-volume command is not supported");
            return setOutputStatus();
        }

        String vserverId = input.getRequestContext().getActionIdentifiers().getVserverId();
        String vserverPath = String.format("%s/%s", MOCK_VOLUME_DIR, vserverId);
        if (!isDirectoryExist(vserverPath)) {
            status = buildStatusForId(LCMCommandStatus.VSERVER_NOT_FOUND, vserverId);
            return setOutputStatus();
        }

        Map<String, String> jsonMap = getJsonMap(input.getRequestContext().getPayload());
        if (jsonMap == null) {
            status = buildStatusForErrorMsg(LCMCommandStatus.UNEXPECTED_ERROR, "payload reading failed");
            return setOutputStatus();
        }

        String volumeId = jsonMap.get(VOLUME_ID_KEY);
        String volumeIdPath = String.format("%s/%s", vserverPath, volumeId);
        if (isDirectoryExist(volumeIdPath)) {
            status = buildStatusForErrorMsg(LCMCommandStatus.REJECTED,
                String.format("Volume %s is already attached to VM %s", volumeId, vserverId));
            return setOutputStatus();
        }

        File volumeDir = new File(volumeIdPath);
        boolean success = volumeDir.mkdir();
        status = success ?
            buildStatusWithoutParams(LCMCommandStatus.SUCCESS) :
            buildStatusForErrorMsg(LCMCommandStatus.REJECTED,
                String.format("Failed to attach volume %s to VM %s", volumeId, vserverId));

        return  setOutputStatus();
    }

    public RequestHandlerOutput detachVolume(RequestHandlerInput input) {
        if (!mockConditionExists()) {
            status = buildStatusForErrorMsg(LCMCommandStatus.REJECTED,
                "The detach-volume command is not supported");
            return setOutputStatus();
        }

        String vserverId = input.getRequestContext().getActionIdentifiers().getVserverId();
        String vserverPath = String.format("%s/%s", MOCK_VOLUME_DIR, vserverId);
        if (!isDirectoryExist(vserverPath)) {
            status = buildStatusForId(LCMCommandStatus.VSERVER_NOT_FOUND, vserverId);
            return setOutputStatus();
        }

        Map<String, String> jsonMap = getJsonMap(input.getRequestContext().getPayload());
        if (jsonMap == null) {
            status = buildStatusForErrorMsg(LCMCommandStatus.UNEXPECTED_ERROR, "payload reading failed");
            return setOutputStatus();
        }

        String volumeId = jsonMap.get(VOLUME_ID_KEY);
        String volumeIdPath = String.format("%s/%s", vserverPath, volumeId);
        if (!isDirectoryExist(volumeIdPath)) {
            status = buildStatusForErrorMsg(LCMCommandStatus.REJECTED,
                String.format("Volume %s is not attached to VM %s", volumeId, vserverId));
            return setOutputStatus();
        }

        File volumeDir = new File(volumeIdPath);
        boolean success = volumeDir.delete();
        status = success ?
            buildStatusWithoutParams(LCMCommandStatus.SUCCESS) :
            buildStatusForErrorMsg(LCMCommandStatus.REJECTED,
                String.format("Failed to attach volume %s to VM %s", volumeId, vserverId));

        return  setOutputStatus();
    }

    private boolean mockConditionExists() {
        return isDirectoryExist(MOCK_VOLUME_DIR);
    }

    private boolean isDirectoryExist(String path) {
        return Files.isDirectory(Paths.get(path));
    }

    private Map<String, String> getJsonMap(String jsonString) {
        try {
            return JsonUtil.convertJsonStringToFlatMap(jsonString);
        } catch (IOException e) {
            logger.error(String.format("MockVolumeHelper got exception when convert json map for (%s)", jsonString), e);
        }
        return null;
    }
}
