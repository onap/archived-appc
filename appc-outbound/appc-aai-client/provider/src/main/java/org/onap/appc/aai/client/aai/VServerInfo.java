/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2018 Nokia Solutions and Networks
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

package org.onap.appc.aai.client.aai;

import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class VServerInfo {

    private String vserverId;
    private String tenantId;
    private String cloudOwner;
    private String cloudRegionId;

    public VServerInfo(Map<String, String> params) throws MissingParameterException {
        vserverId = params.get("vserverId");
        if (StringUtils.isBlank(vserverId)) {
            throw new MissingParameterException("VServerId is missing");
        }

        tenantId = params.get("tenantId");
        if (StringUtils.isBlank(tenantId)) {
            throw new MissingParameterException("TenantId is missing");
        }

        cloudOwner = params.get("cloudOwner");
        if (StringUtils.isBlank(cloudOwner)) {
            throw new MissingParameterException("Cloud Owner is missing");
        }

        cloudRegionId = params.get("cloudRegionId");
        if (StringUtils.isBlank(cloudRegionId)) {
            throw new MissingParameterException("Cloud region Id is missing");
        }
    }

    public String getVserverId() {
        return vserverId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getCloudOwner() {
        return cloudOwner;
    }

    public String getCloudRegionId() {
        return cloudRegionId;
    }
}
