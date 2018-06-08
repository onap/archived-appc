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
import java.io.FileInputStream;
import java.io.IOException;

/**
 * This class is here because LCM reboot backend is not implemented.
 * Hence this class is here to mock the handling response of LCM reboot REST API.
 * <p>
 * When backend is implemented, this file should be removed.
 */
public class MockRebootHelper extends AbstractMockHelper {
    private final String MOCK_REBOOT_FILENAME = "/tmp/lcm/reboot";

    /**
     * Process service request through reading the mockFile.
     * If the file doesn't exist, it will return "The reboot command is not supported"
     * Otherwise, it will build an accepted result.
     *
     * @param requestHandlerInput of the input
     * @return RequestHandlerOutput
     */
    public RequestHandlerOutput reboot(RequestHandlerInput requestHandlerInput) {
        File file = new File(MOCK_REBOOT_FILENAME);
        if (!file.exists()) {
            // when mock file does not exist, return generic service not supported
            status = buildStatusForErrorMsg(LCMCommandStatus.REJECTED, "The reboot command is not supported");
        } else {
            try {
                properties.load(new FileInputStream(MOCK_REBOOT_FILENAME));
                status = buildStatusWithoutParams(LCMCommandStatus.ACCEPTED);
            } catch (IOException e) {
                // when loading propertes from mock file failed, return with associated message
                status = buildStatusForErrorMsg(LCMCommandStatus.REJECTED,
                    String.format("cannot load properties from %s", MOCK_REBOOT_FILENAME));
            }
        }

        return setOutputStatus();
    }
}
