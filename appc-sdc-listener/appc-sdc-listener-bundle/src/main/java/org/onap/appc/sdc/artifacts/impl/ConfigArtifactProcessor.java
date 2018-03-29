/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.sdc.artifacts.impl;

import java.net.MalformedURLException;
import java.net.URI;
import org.onap.appc.adapter.message.EventSender;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.sdc.artifacts.object.SDCArtifact;
import org.onap.appc.sdc.listener.ProviderOperations;
import org.onap.appc.sdc.listener.ProviderResponse;
import org.onap.appc.sdc.listener.Util;
import org.onap.sdc.api.IDistributionClient;
import org.onap.sdc.api.notification.IArtifactInfo;
import org.onap.sdc.api.notification.INotificationData;
import org.onap.sdc.api.notification.IResourceInstance;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

/**
 * Artifact processor for config artifact type
 */
public class ConfigArtifactProcessor extends AbstractArtifactProcessor {

    private final EELFLogger logger = EELFManager.getInstance().getLogger(ConfigArtifactProcessor.class);

    /**
     * returns an instance of ConfigArtifactProcessor
     *
     * @param client an instance of IDistributionClient
     * @param eventSender an instance of EventSender
     * @param notification an instance of INotificationData
     * @param resource an instance of IResourceInstance
     * @param artifact an instance of IArtifactInfo
     * @param storeUri an instance of URI
     */
    public ConfigArtifactProcessor(IDistributionClient client, EventSender eventSender, INotificationData notification,
            IResourceInstance resource, IArtifactInfo artifact, URI storeUri) {
        super(client, eventSender, notification, resource, artifact, storeUri);
    }

    @Override
    public void processArtifact(SDCArtifact artifact) throws APPCException {
        String postData =
                Util.toSdcStoreDocumentInput(notification, resource, super.artifact, artifact.getArtifactContent());
        logger.debug("ConfigArtifactProcessor::processArtifact::postData=" + postData);
        try {
            if (null != storeUri) {
                logger.debug("ConfigArtifactProcessor::processArtifact::URI is" + storeUri.toString());
                ProviderResponse result = ProviderOperations.post(storeUri.toURL(), postData, null);
                if (result.getStatus() == 200) {
                    logger.debug("ConfigArtifactProcessor::processArtifact::post request success!!");
                    Util.parseResponse(result.getBody());
                } else {
                    logger.debug("ConfigArtifactProcessor::processArtifact()::post request failed!! Returned :"
                            + result.getStatus() + "-Result body- " + result.getBody());
                    throw new APPCException(
                            "ConfigArtifactProcessor::processArtifact: Invalid status retrurned from post "
                                    + result.getStatus());
                }
            } else {
                logger.error("ConfigArtifactProcessor::processArtifact, storeURI is null");
            }
        } catch (MalformedURLException | APPCException e) {
            logger.error("Error processing artifact : " + this.artifact.toString(), e);
            throw new APPCException(e.getMessage(), e);
        }
    }
}
