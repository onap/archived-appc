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
 * This class is here because LCM upgrade actions are not implemented.
 * Hence this class is here to mock the handling response of LCM upgrade actions REST API.
 *
 * When backend is implemented, this file should be removed.
 */
public class MockUpgradeHelper extends AbstractMockHelper {
    private final String MOCK_UPGRADE_DIR = "/tmp/lcm/upgrade";
    private final String UPGRADE = "upgrade";

    public RequestHandlerOutput upgradePreCheck(RequestHandlerInput input) {
    	
        if (!mockConditionExists()) {
            status = buildStatusForErrorMsg(LCMCommandStatus.REJECTED,
                "The upgrade-pre-check command is not supported");
            return setOutputStatus();
        }

        String vnfId = input.getRequestContext().getActionIdentifiers().getVnfId();
        Map<String, String> jsonMap = getJsonMap(input.getRequestContext().getPayload());
        if (jsonMap == null) {
            status = buildStatusForErrorMsg(LCMCommandStatus.UNEXPECTED_ERROR, "payload reading failed");
            return setOutputStatus();
        }
        
        String upgradePath = String.format(MOCK_UPGRADE_DIR+"/%s-%s", jsonMap.get("existing-software-version"),jsonMap.get("new-software-version"));

        if (isDirectoryExist(upgradePath)) {
            status = buildStatusForErrorMsg(LCMCommandStatus.REJECTED,
                String.format("UpgradeCheck from %s to %s is already happened to VNF %s", jsonMap.get("existing-software-version"),jsonMap.get("new-software-version"), vnfId));
            return setOutputStatus();
        }

        File upgradeDir = new File(upgradePath);
        boolean success = upgradeDir.mkdir();
        status = success ?
            buildStatusWithoutParams(LCMCommandStatus.SUCCESS) :
            buildStatusForErrorMsg(LCMCommandStatus.REJECTED,
                String.format("Failed to upgradePreCheck for  VNF %s", vnfId));

        return  setOutputStatus();
    }
    
    public RequestHandlerOutput upgradePostCheck(RequestHandlerInput input) {
    	
        if (!mockConditionExists()) {
            status = buildStatusForErrorMsg(LCMCommandStatus.REJECTED,
                "The upgrade-post-check command is not supported");
            return setOutputStatus();
        }

        String vnfId = input.getRequestContext().getActionIdentifiers().getVnfId();
        Map<String, String> jsonMap = getJsonMap(input.getRequestContext().getPayload());
        if (jsonMap == null) {
            status = buildStatusForErrorMsg(LCMCommandStatus.UNEXPECTED_ERROR, "payload reading failed");
            return setOutputStatus();
        }
        
        String upgradePath = String.format(MOCK_UPGRADE_DIR+"/%s/%s", jsonMap.get("existing-software-version"),jsonMap.get("new-software-version"));

        if (isDirectoryExist(upgradePath)) {
            status = buildStatusForErrorMsg(LCMCommandStatus.REJECTED,
                String.format("UpgradePostcheck from %s to %s is already happened to VNF %s", jsonMap.get("existing-software-version"),jsonMap.get("new-software-version"), vnfId));
            return setOutputStatus();
        }

        File upgradeDir = new File(upgradePath);
        boolean success = upgradeDir.mkdir();
        status = success ?
            buildStatusWithoutParams(LCMCommandStatus.SUCCESS) :
            buildStatusForErrorMsg(LCMCommandStatus.REJECTED,
                String.format("Failed to upgradePostCheck for %s ", vnfId));

        return  setOutputStatus();
    }
    
    public RequestHandlerOutput upgradeSoftware(RequestHandlerInput input) {
    	
        if (!mockConditionExists()) {
            status = buildStatusForErrorMsg(LCMCommandStatus.REJECTED,
                "The upgrade-software command is not supported");
            return setOutputStatus();
        }

        String vnfId = input.getRequestContext().getActionIdentifiers().getVnfId();
        Map<String, String> jsonMap = getJsonMap(input.getRequestContext().getPayload());
        if (jsonMap == null) {
            status = buildStatusForErrorMsg(LCMCommandStatus.UNEXPECTED_ERROR, "payload reading failed");
            return setOutputStatus();
        }
        
        String upgradePath = String.format(MOCK_UPGRADE_DIR+"/%s/%s", jsonMap.get("existing-software-version"),jsonMap.get("new-software-version"));

        if (isDirectoryExist(upgradePath)) {
            status = buildStatusForErrorMsg(LCMCommandStatus.REJECTED,
                String.format("UpgradeSoftware from %s to %s is already happened to VNF %s", jsonMap.get("existing-software-version"),jsonMap.get("new-software-version"), vnfId));
            return setOutputStatus();
        }

        File upgradeDir = new File(upgradePath);
        boolean success = upgradeDir.mkdir();
        status = success ?
            buildStatusWithoutParams(LCMCommandStatus.SUCCESS) :
            buildStatusForErrorMsg(LCMCommandStatus.REJECTED,
                String.format("Failed to upgradeSoftware for  VNF %s", vnfId));

        return  setOutputStatus();
    }
    
    public RequestHandlerOutput upgradeBackup(RequestHandlerInput input) {
    	
        if (!mockConditionExists()) {
            status = buildStatusForErrorMsg(LCMCommandStatus.REJECTED,
                "The upgrade-backup command is not supported");
            return setOutputStatus();
        }

        String vnfId = input.getRequestContext().getActionIdentifiers().getVnfId();
        Map<String, String> jsonMap = getJsonMap(input.getRequestContext().getPayload());
        if (jsonMap == null) {
            status = buildStatusForErrorMsg(LCMCommandStatus.UNEXPECTED_ERROR, "payload reading failed");
            return setOutputStatus();
        }
        
        String upgradePath = String.format(MOCK_UPGRADE_DIR+"/%s/%s", jsonMap.get("existing-software-version"),jsonMap.get("new-software-version"));

        if (isDirectoryExist(upgradePath)) {
            status = buildStatusForErrorMsg(LCMCommandStatus.REJECTED,
                String.format("UpgradeBackup from %s to %s is already happened to VNF %s", jsonMap.get("existing-software-version"),jsonMap.get("new-software-version"), vnfId));
            return setOutputStatus();
        }

        File upgradeDir = new File(upgradePath);
        boolean success = upgradeDir.mkdir();
        status = success ?
            buildStatusWithoutParams(LCMCommandStatus.SUCCESS) :
            buildStatusForErrorMsg(LCMCommandStatus.REJECTED,
                String.format("Failed to upgradeBackup for VNF %s", vnfId));

        return  setOutputStatus();
    }
    
    public RequestHandlerOutput upgradeBackout(RequestHandlerInput input) {
    	
        if (!mockConditionExists()) {
            status = buildStatusForErrorMsg(LCMCommandStatus.REJECTED,
                "The upgrade-backout command is not supported");
            return setOutputStatus();
        }

        String vnfId = input.getRequestContext().getActionIdentifiers().getVnfId();
        Map<String, String> jsonMap = getJsonMap(input.getRequestContext().getPayload());
        if (jsonMap == null) {
            status = buildStatusForErrorMsg(LCMCommandStatus.UNEXPECTED_ERROR, "payload reading failed");
            return setOutputStatus();
        }
        
        String upgradePath = String.format(MOCK_UPGRADE_DIR+"/%s/%s", jsonMap.get("existing-software-version"),jsonMap.get("new-software-version"));

        if (isDirectoryExist(upgradePath)) {
            status = buildStatusForErrorMsg(LCMCommandStatus.REJECTED,
                String.format("UpgradeBackout from %s to %s is already happened to VNF %s", jsonMap.get("existing-software-version"),jsonMap.get("new-software-version"), vnfId));
            return setOutputStatus();
        }

        File upgradeDir = new File(upgradePath);
        boolean success = upgradeDir.mkdir();
        status = success ?
            buildStatusWithoutParams(LCMCommandStatus.SUCCESS) :
            buildStatusForErrorMsg(LCMCommandStatus.REJECTED,
                String.format("Failed to upgradebackout for  VNF %s", vnfId));

        return  setOutputStatus();
    }

    private boolean mockConditionExists() {
        return isDirectoryExist(MOCK_UPGRADE_DIR);
    }

    private boolean isDirectoryExist(String path) {
        return Files.isDirectory(Paths.get(path));
    }

    private Map<String, String> getJsonMap(String jsonString) {
        try {
            return JsonUtil.convertJsonStringToFlatMap(jsonString);
        } catch (IOException e) {
            logger.error(String.format("MockUpgradeHelper got exception when convert json map for (%s)", jsonString), e);
        }
        return null;
    }
}
