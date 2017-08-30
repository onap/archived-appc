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

package org.openecomp.appc.sdc.artifacts.impl;

import org.openecomp.appc.adapter.message.EventSender;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.licmgr.Constants;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.apache.commons.lang.StringUtils;
import org.openecomp.appc.sdc.artifacts.object.SDCArtifact;
import org.openecomp.sdc.api.IDistributionClient;
import org.openecomp.sdc.api.notification.IArtifactInfo;
import org.openecomp.sdc.api.notification.INotificationData;
import org.openecomp.sdc.api.notification.IResourceInstance;

import java.net.URI;

/**
 * Artifact processor for VNF license artifact type
 */
public class LicenseArtifactProcessor extends AbstractArtifactProcessor {

    private final EELFLogger logger = EELFManager.getInstance().getLogger(LicenseArtifactProcessor.class);

    /**
     * returns an instance of ConfigArtifactProcessor
     * @param client an instance of IDistributionClient
     * @param eventSender an instance of EventSender
     * @param notification an instance of INotificationData
     * @param resource an instance of IResourceInstance
     * @param artifact an instance of IArtifactInfo
     * @param storeUri an instance of URI
     */
    public LicenseArtifactProcessor(IDistributionClient client, EventSender eventSender, INotificationData notification, IResourceInstance resource, IArtifactInfo artifact, URI storeUri) {
        super(client,eventSender,notification,resource,artifact,storeUri);
    }

    @Override
    public void processArtifact(SDCArtifact artifact) throws APPCException {

        String vnfType = artifact.getResourceName();
        String version = artifact.getResourceVersion();
        String packageArtifactID = artifact.getArtifactUUID();

        if (StringUtils.isEmpty(vnfType) ||
                StringUtils.isEmpty(version) ||
                StringUtils.isEmpty(packageArtifactID)) {
            String errStr = String.format("Missing information in SDC request. Details: resource_type='%s', resource_version='%s', artifactID='%s'", vnfType, version, packageArtifactID);
            logger.error(errStr);
            throw new APPCException(errStr);
        }

        try {
            SDCArtifact existingArtifact = artifactStorageService.retrieveSDCArtifact(vnfType, version,artifact.getArtifactType());

            if (existingArtifact ==null) { // new resource
                logger.debug("Artifact not found from database for vnfType = " + vnfType + " , version = " + version + " , artifactType = " + artifact.getArtifactType());
                artifactStorageService.storeSDCArtifact(artifact);
            } else { // duplicate
                logger.debug("Artifact retrieved from database = " + existingArtifact);
                logger.warn(String.format("Artifact of type '%s' already deployed for resource_type='%s' and resource_version='%s'", Constants.VF_LICENSE, vnfType, version));
            }

        } catch (Exception e) {
            logger.error("Error processing artifact : " + artifact.toString(),e);
            throw new APPCException(e.getMessage(),e);
        }
    }
}
