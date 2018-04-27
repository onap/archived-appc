/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 Nokia. All rights reserved.
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */
package org.onap.appc.adapter.chef.chefclient.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Date;

public class ChefApiHeaderFactory {

    private FormattedTimestamp formattedTimestamp = new FormattedTimestamp();

    public ImmutableMap<String, String> create(String methodName, String path, String body, String userId,
        String organizations, String pemPath) {

        String hashedBody = Utils.sha1AndBase64(body);
        String timeStamp = formattedTimestamp.format(new Date());

        Builder<String, String> builder = ImmutableMap.builder();
        builder
            .put("Content-type", "application/json")
            .put("Accept", "application/json")
            .put("X-Ops-Timestamp", timeStamp)
            .put("X-Ops-UserId", userId)
            .put("X-Chef-Version", "12.4.1")
            .put("X-Ops-Content-Hash", hashedBody)
            .put("X-Ops-Sign", "version=1.0")
            .build();

        String hashedPath = Utils.sha1AndBase64("/organizations/" + organizations + path);

        StringBuilder sb = new StringBuilder();
        sb.append("Method:").append(methodName).append("\n");
        sb.append("Hashed Path:").append(hashedPath).append("\n");
        sb.append("X-Ops-Content-Hash:").append(hashedBody).append("\n");
        sb.append("X-Ops-Timestamp:").append(timeStamp).append("\n");
        sb.append("X-Ops-UserId:").append(userId);

        String auth_String = Utils.signWithRSA(sb.toString(), pemPath);
        String[] auth_headers = Utils.splitAs60(auth_String);

        for (int i = 0; i < auth_headers.length; i++) {
            builder.put("X-Ops-Authorization-" + (i + 1), auth_headers[i]);
        }

        return builder.build();
    }

}
