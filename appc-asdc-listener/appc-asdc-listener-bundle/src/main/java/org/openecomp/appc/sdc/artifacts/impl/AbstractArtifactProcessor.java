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
import org.openecomp.appc.adapter.message.MessageDestination;
import org.openecomp.appc.adapter.message.event.EventHeader;
import org.openecomp.appc.adapter.message.event.EventMessage;
import org.openecomp.appc.adapter.message.event.EventStatus;
import org.openecomp.appc.sdc.listener.Util;
import org.openecomp.appc.exceptions.APPCException;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.openecomp.appc.sdc.artifacts.ArtifactProcessor;
import org.openecomp.appc.sdc.artifacts.helper.ArtifactStorageService;
import org.openecomp.appc.sdc.artifacts.object.SDCArtifact;
import org.openecomp.sdc.api.IDistributionClient;
import org.openecomp.sdc.api.notification.IArtifactInfo;
import org.openecomp.sdc.api.notification.INotificationData;
import org.openecomp.sdc.api.notification.IResourceInstance;
import org.openecomp.sdc.api.results.IDistributionClientDownloadResult;
import org.openecomp.sdc.utils.DistributionActionResultEnum;
import org.openecomp.sdc.utils.DistributionStatusEnum;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Provides abstrace implementation for SDC artifact processor
 */
public abstract class AbstractArtifactProcessor implements ArtifactProcessor {

    public static final String PAYLOAD_CHARSET = "UTF-8";
    private static final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";

    protected IDistributionClient client;
    protected EventSender eventSender;

    protected INotificationData notification;
    protected IResourceInstance resource;
    protected IArtifactInfo artifact;
    protected URI storeUri;

    private final EELFLogger logger = EELFManager.getInstance().getLogger(AbstractArtifactProcessor.class);

    protected ArtifactStorageService artifactStorageService;

    private AbstractArtifactProcessor(){
        artifactStorageService = new ArtifactStorageService();
    }

    AbstractArtifactProcessor(IDistributionClient client, EventSender eventSender, INotificationData notification, IResourceInstance resource,
                              IArtifactInfo artifact, URI storeUri){

        this();
        this.client = client;
        this.eventSender = eventSender;
        this.notification = notification;
        this.resource = resource;
        this.artifact = artifact;
        this.storeUri = storeUri;
    }

    @Override
    public void run(){

        try{
            logger.info(String.format("Attempting to download artifact %s", artifact));
            // Download artifact
            IDistributionClientDownloadResult download = client.download(artifact);

            logger.info(String.format("Download of artifact %s completed with status %s", artifact.getArtifactUUID(), download));

            // Notify of download status
            if (download.getDistributionActionResult() != DistributionActionResultEnum.SUCCESS) {
                client.sendDownloadStatus(Util.buildDistributionStatusMessage(client, notification, artifact,
                        DistributionStatusEnum.DOWNLOAD_ERROR), download.getDistributionMessageResult());
                sendDCAEEvent(notification.getDistributionID(), notification.getServiceName(), notification.getServiceVersion(), "Download is failed.");
                return;
            }

            client.sendDownloadStatus(Util.buildDistributionStatusMessage(client, notification, artifact, DistributionStatusEnum.DOWNLOAD_OK));

            processArtifact(download);

            client.sendDeploymentStatus(
                    Util.buildDistributionStatusMessage(client, notification, this.artifact, DistributionStatusEnum.DEPLOY_OK));
        }
        catch (Exception e){
            logger.error("Error processing artifact " + this.artifact.toString() ,e);

            client.sendDeploymentStatus(Util.buildDistributionStatusMessage(client, notification, artifact,
                    DistributionStatusEnum.DEPLOY_ERROR), e.getMessage());
            sendDCAEEvent(notification.getDistributionID(), notification.getServiceName(), notification.getServiceVersion(), e.getMessage());
        }
    }


    @Override
    public void processArtifact(IDistributionClientDownloadResult download) throws APPCException {
        String data = null;
        if(logger.isDebugEnabled()){
            logger.debug("Entry processArtifact in AbstractArtifactProcessor");
        }
        try {
            if (download.getArtifactPayload() != null) {
                data = new String(download.getArtifactPayload(), PAYLOAD_CHARSET);
            }
        } catch (UnsupportedEncodingException e) {
            logger.error("Error reading artifact with " + PAYLOAD_CHARSET + " encoding" + new String(download.getArtifactPayload()) ,e);
            throw new APPCException(e);
        }

        SDCArtifact sdcArtifact = getArtifactObject(data);
        logger.debug("Constructed SDCArtifact = " + sdcArtifact);
        processArtifact(sdcArtifact);

        if(logger.isDebugEnabled()){
            logger.debug("Exit processArtifact in AbstractArtifactProcessor");
        }
    }

    protected abstract void processArtifact(SDCArtifact artifact) throws APPCException;

    protected SDCArtifact getArtifactObject(String data){

        SDCArtifact sdcArtifact = new SDCArtifact();

        sdcArtifact.setArtifactUUID(this.artifact.getArtifactUUID());
        sdcArtifact.setArtifactName(this.artifact.getArtifactName());
        sdcArtifact.setArtifactType(this.artifact.getArtifactType());
        sdcArtifact.setArtifactVersion(this.artifact.getArtifactVersion());
        sdcArtifact.setArtifactDescription(this.artifact.getArtifactDescription());
        sdcArtifact.setArtifactContent(data);
        sdcArtifact.setCreationDate(getCurrentDateTime());

        sdcArtifact.setDistributionId(this.notification.getDistributionID());
        sdcArtifact.setServiceUUID(this.notification.getServiceUUID());
        sdcArtifact.setServiceName(this.notification.getServiceName());
        sdcArtifact.setServiceDescription(this.notification.getServiceDescription());

        sdcArtifact.setResourceName(this.resource.getResourceName());
        sdcArtifact.setResourceType(this.resource.getResourceType());
        sdcArtifact.setResourceVersion(this.resource.getResourceVersion());
        sdcArtifact.setResourceUUID(this.resource.getResourceUUID());
        sdcArtifact.setResourceInstanceName(this.resource.getResourceInstanceName());

        return sdcArtifact;
    }

    protected String getCurrentDateTime() {
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        Date date = new Date();
        return dateFormat.format(date);
    }

    private void sendDCAEEvent(String distributionID, String serviceName, String serviceVersion, String errorMessage) {
        if (null == eventSender){
            return;
        }
        String errorDescription = String.format("ASDC distribution of service '%s', version '%s' is failed with reason: '%s'",
                serviceName, serviceVersion, errorMessage);

        EventMessage eventMessage = new EventMessage(
                new EventHeader((new Date()).toString(), serviceVersion, distributionID),
                new EventStatus(401, errorDescription));

        eventSender.sendEvent(MessageDestination.DCAE, eventMessage);
    }

}
