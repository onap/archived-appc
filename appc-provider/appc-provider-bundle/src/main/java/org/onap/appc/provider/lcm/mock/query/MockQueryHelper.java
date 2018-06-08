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

package org.onap.appc.provider.lcm.mock.query;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.VmState;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.VmStatus;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.query.output.QueryResults;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.query.output.QueryResultsBuilder;
import org.onap.appc.executor.objects.LCMCommandStatus;
import org.onap.appc.provider.lcm.mock.AbstractMockHelper;
import org.onap.appc.requesthandler.objects.RequestHandlerInput;
import org.onap.appc.requesthandler.objects.RequestHandlerOutput;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is here because LCM query backend is not implemented.
 * Hence this class is here to mock the handling response of LCM query REST API.
 *
 * When backend is implemented, this file should be removed.
 */
public class MockQueryHelper extends AbstractMockHelper {
    private final String MOCK_QUERY_FILENAME = "/tmp/lcm/query";

    /** VF_STATE value are listed at https://wiki.openstack.org/wiki/VMState#vm_state */
    private final Map<VmState, List<String>> VF_STATE_MAP = new HashMap<VmState, List<String>>() {
        {
            put(VmState.Inactive, Arrays.asList(
                "INITIALIZED",
                "PAUSED",
                "SUSPENDED",
                "STOPPED",
                "SOFT_DELETED",
                "HARD_DELETED",
                "ERROR"));
            put(VmState.Active, Arrays.asList(
                "ACTIVE",
                "RESCUED",
                "RESIZED"));
            put(VmState.Standby, Arrays.asList(
                "STANDBY"
            ));
        }
    };

    private final EELFLogger logger = EELFManager.getInstance().getLogger(MockQueryHelper.class);

    /**
     * Process service request through reading the mockFile. File should contain:
     *   - VNF_IDS: the list of VNF IDs, separated by comma
     *   - VMS_<a VNF id>: the list of VMs of the VNF ID, separated by comma
     *   - STATE_<a VM id>: the state of the VM
     *   - STATUS_<a VM id>: the status of the VMl
     * @param input of RequestHandleInput which contains the VNF ID
     * @return RequestHandlerOutput
     */
    public RequestHandlerOutput query(RequestHandlerInput input) {
        File file = new File(MOCK_QUERY_FILENAME);
        if (!file.exists()) {
            // when mock file does not exist, return generic service not supported
            status = buildStatusForErrorMsg(LCMCommandStatus.REJECTED, "The query command is not supported");
            return setOutputStatus();
        }

        logger.debug(String.format("MockQueryHelper loading property from file %s", MOCK_QUERY_FILENAME));
        try {
            properties.load(new FileInputStream(MOCK_QUERY_FILENAME));
        } catch (IOException e) {
            // when loading propertes from mock file failed, return with associated message
            status = buildStatusForErrorMsg(
                LCMCommandStatus.REJECTED, "cannot load properties from " + MOCK_QUERY_FILENAME);
            return setOutputStatus();
        }

        String key = "VNF_IDS";
        List<String> vnfIds =
            Arrays.asList(properties.getProperty(key, "").split(DELIMITER_COMMA));
        logger.debug(String.format("MockQueryHelper got vnfId %s", vnfIds.toString()));

        String vnfId = input.getRequestContext().getActionIdentifiers().getVnfId();
        if (!vnfIds.contains(vnfId)) {
            status = buildStatusForVnfId(LCMCommandStatus.VNF_NOT_FOUND, vnfId);
            return setOutputStatus();
        }

        key = "VMS_" + vnfId;
        List<String> vmIds =
            Arrays.asList(properties.getProperty(key, "").split(DELIMITER_COMMA));
        logger.debug(String.format("MockQueryHelper got vmId %s", vmIds.toString()));

        List<QueryResults> queryResultList = new ArrayList<>();
        VmState vfState;
        VmStatus vfStatus;
        boolean found = false;
        for (String vmId : vmIds) {
            // state
            vfState = VmState.Unknown;
            key = "STATE_" + vmId;
            String stateProp = properties.getProperty(key, "").toUpperCase();
            for (Map.Entry<VmState, List<String>> aEntry: VF_STATE_MAP.entrySet()) {
                if (aEntry.getValue().contains(stateProp)) {
                    vfState = aEntry.getKey();
                    break;
                }
            }

            // status
            vfStatus = VmStatus.Unknown;
            key = "STATUS_" + vmId;
            String statusProp = properties.getProperty(key, "unknown").toLowerCase();
            for (VmStatus otherVfStatus : VmStatus.values()) {
                if (statusProp.equalsIgnoreCase(otherVfStatus.name())) {
                    vfStatus = otherVfStatus;
                }
            }

            QueryResultsBuilder queryResultBuilder = new QueryResultsBuilder();
            queryResultBuilder.setVserverId(vmId);
            queryResultBuilder.setVmState(vfState);
            queryResultBuilder.setVmStatus(vfStatus);
            queryResultList.add(queryResultBuilder.build());
            found = true;
        }

        if (found) {
            status = buildStatusWithoutParams(LCMCommandStatus.SUCCESS);
            requestHandlerOutput.getResponseContext().setPayloadObject(queryResultList);
        } else {
            status = buildStatusForErrorMsg(LCMCommandStatus.VNF_NOT_FOUND, "no detailss for vnfId");
        }

        return setOutputStatus();
    }
}
