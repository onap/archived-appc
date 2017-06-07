/*-
 * ============LICENSE_START=======================================================
 * APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Amdocs
 * ================================================================================
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */

package org.openecomp.appc.sdc.listener;
import org.openecomp.sdc.utils.DistributionStatusEnum;

import org.json.JSONException;
import org.json.JSONObject;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.sdc.api.IDistributionClient;
import org.openecomp.sdc.api.consumer.IDistributionStatusMessage;
import org.openecomp.sdc.api.notification.IArtifactInfo;
import org.openecomp.sdc.api.notification.INotificationData;
import org.openecomp.sdc.api.notification.IResourceInstance;
import org.openecomp.sdc.utils.DistributionStatusEnum;

public class Util {

    // TODO - Use the yang builder instead
    public static String toAsdcStoreDocumentInput(INotificationData notification, IResourceInstance resource,
        IArtifactInfo artifact, String data) {
        JSONObject json = new JSONObject();

        JSONObject requestInfo = new JSONObject();
        requestInfo.put("request-id", notification.getServiceUUID());
        requestInfo.put("request-action", "StoreAsdcDocumentRequest");
        requestInfo.put("source", "ASDC");

        JSONObject docParams = new JSONObject();
        docParams.put("service-uuid", notification.getServiceUUID());
        docParams.put("distribution-id", notification.getDistributionID());
        docParams.put("service-name", notification.getServiceName());
        docParams.put("service-description", notification.getServiceDescription());
        docParams.put("service-artifacts", "[]");
        docParams.put("resource-uuid", resource.getResourceUUID());
        docParams.put("resource-instance-name", resource.getResourceInstanceName());
        docParams.put("resource-name", resource.getResourceName());
        docParams.put("resource-version", resource.getResourceVersion());
        docParams.put("resource-type", resource.getResourceType());
        docParams.put("artifact-uuid", artifact.getArtifactUUID());
        docParams.put("artifact-name", artifact.getArtifactName());
        docParams.put("artifact-type", artifact.getArtifactType());
        docParams.put("artifact-version", artifact.getArtifactVersion());
        docParams.put("artifact-description", artifact.getArtifactDescription());
        docParams.put("artifact-contents", data);

        json.put("request-information", requestInfo);
        json.put("document-parameters", docParams);

        return String.format("{\"input\": %s}", json.toString());
    }

    public static boolean parseResponse(String input) throws APPCException {
        JSONObject result, output, response;
        try {
            result = new JSONObject(input);
            output = result.getJSONObject("output");
            response = output.getJSONObject("config-document-response");
            String id = response.getString("request-id");
            String status = response.getString("status");
            if (status.equals(DistributionStatusEnum.DEPLOY_OK.toString())) {
                return true;
            } else {
                String error = response.optString("error-reason");
                String msg = error.isEmpty() ? "No Reason Provided" : error;
                throw new APPCException(msg);
            }
        } catch (JSONException jse) {
            throw new APPCException("Did not get valid json from provider.", jse);
        }
    }

    public static IDistributionStatusMessage buildDistributionStatusMessage(final IDistributionClient client,
        final INotificationData data, final IArtifactInfo relevantArtifact, final DistributionStatusEnum status) {
        IDistributionStatusMessage statusMessage = new IDistributionStatusMessage() {

            @Override
            public long getTimestamp() {
                long currentTimeMillis = System.currentTimeMillis();
                return currentTimeMillis;
            }

            @Override
            public DistributionStatusEnum getStatus() {
                return status;
            }

            @Override
            public String getDistributionID() {
                return data.getDistributionID();
            }

            @Override
            public String getConsumerID() {
                return client.getConfiguration().getConsumerID();
            }

            @Override
            public String getArtifactURL() {
                return relevantArtifact.getArtifactURL();
            }
        };
        return statusMessage;
    }
}
