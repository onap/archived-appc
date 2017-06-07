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

import org.openecomp.appc.adapter.message.EventSender;
import org.openecomp.appc.adapter.message.MessageDestination;
import org.openecomp.appc.adapter.message.event.EventHeader;
import org.openecomp.appc.adapter.message.event.EventMessage;
import org.openecomp.appc.adapter.message.event.EventStatus;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.licmgr.Constants;
import org.openecomp.appc.licmgr.LicenseManager;
import org.openecomp.sdc.api.IDistributionClient;
import org.openecomp.sdc.api.notification.IArtifactInfo;
import org.openecomp.sdc.api.notification.INotificationData;
import org.openecomp.sdc.api.notification.IResourceInstance;
import org.openecomp.sdc.api.results.IDistributionClientDownloadResult;
import org.openecomp.sdc.utils.DistributionActionResultEnum;
import org.openecomp.sdc.utils.DistributionStatusEnum;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import static org.openecomp.appc.licmgr.Constants.ASDC_ARTIFACTS_FIELDS.*;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("JavaDoc")
public class DownloadAndStoreOp implements Runnable {

    public static final String PAYLOAD_CHARSET = "UTF-8";

    private static final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";

    private final EELFLogger logger = EELFManager.getInstance().getLogger(DownloadAndStoreOp.class);

    private IDistributionClient client;
    private EventSender eventSender;

    private INotificationData notification;
    private IResourceInstance resource;
    private IArtifactInfo artifact;

    private URI storeUri;

    public DownloadAndStoreOp(IDistributionClient client, EventSender eventSender, INotificationData notification, IResourceInstance resource,
                              IArtifactInfo artifact, URI storeUri) {
        this.client = client;
        this.eventSender = eventSender;
        this.notification = notification;
        this.resource = resource;
        this.artifact = artifact;
        this.storeUri = storeUri;
    }

    @Override
    public void run() {
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

        String data = null;
        try {
            if (download.getArtifactPayload() != null) {
                data = new String(download.getArtifactPayload(), PAYLOAD_CHARSET);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        boolean providerSuccess = false;
        String providerReason = "Unknown Error";
        // Send data to provider
        if (data != null && artifact != null) {
            switch(artifact.getArtifactType()) {
                case "APPC_CONFIG":
                    String postData = Util.toAsdcStoreDocumentInput(notification, resource, artifact, data);
                    try {
                        ProviderResponse result = ProviderOperations.post(storeUri.toURL(), postData, null);
                        if (result.getStatus() == 200) {
                            providerSuccess = Util.parseResponse(result.getBody());
                            providerReason = "Success";
                        }
                    } catch (MalformedURLException | APPCException e) {
                        providerReason = e.getMessage();
                        e.printStackTrace();
                    }
                    break;

                case "VF_LICENSE":
                    BundleContext bctx = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
                    ServiceReference srefLicenseService = bctx.getServiceReference(LicenseManager.class);
                    LicenseManager licenseService = (LicenseManager) bctx.getService(srefLicenseService);

                    Map<String, String> artifactPayload = prepareArtifactPayloadParamsMap(data);

                    String vnfType = artifactPayload.get(RESOURCE_NAME.name());
                    String version = artifactPayload.get(RESOURCE_VERSION.name());
                    String packageArtifactID = artifactPayload.get(ARTIFACT_UUID.name());

                    try {
                        if (null == vnfType || null == version || null == packageArtifactID || vnfType.isEmpty() || version.isEmpty() || packageArtifactID.isEmpty()) {
                            throw new APPCException(String.format("Missing information in ASDC request. Details: resource_type='%s', resource_version='%s', artifactID='%s'", vnfType, version, packageArtifactID));
                        }

                        Map<String, String> existingArtifactPayload = licenseService.retrieveLicenseModelData(vnfType, version);

                        if (existingArtifactPayload.isEmpty()) { // new resource
                            licenseService.storeArtifactPayload(artifactPayload);
                        } else { // duplicate
                            logger.warn(String.format("Artifact of type '%s' already deployed for resource_type='%s' and resource_version='%s'", Constants.VF_LICENSE, vnfType, version));
                        }

                        providerSuccess = true;

                    } catch (Exception e) {
                        providerSuccess = false;
                        providerReason = e.getMessage();
                    }
                    break;

                default:
                    throw new UnsupportedOperationException("Artifact type " + artifact.getArtifactType() + " is not supported");
            }

        }

        // Notify of provider's response
        if (providerSuccess) {
            client.sendDeploymentStatus(
                            Util.buildDistributionStatusMessage(client, notification, artifact, DistributionStatusEnum.DEPLOY_OK));
        } else {
            client.sendDeploymentStatus(Util.buildDistributionStatusMessage(client, notification, artifact,
                            DistributionStatusEnum.DEPLOY_ERROR), providerReason);
            sendDCAEEvent(notification.getDistributionID(), notification.getServiceName(), notification.getServiceVersion(), providerReason);
        }

    }

    /**
     * Prepares Artifact Payload params map
     * @param data
     * @return Map<String,String>
     */
    private Map<String, String> prepareArtifactPayloadParamsMap(String data) {
        Map<String, String> paramsMap = new HashMap<>();

        paramsMap.put(SERVICE_UUID.name(), this.notification.getServiceUUID());
        paramsMap.put(DISTRIBUTION_ID.name(), this.notification.getDistributionID());
        paramsMap.put(SERVICE_NAME.name(), this.notification.getServiceName());
        paramsMap.put(SERVICE_DESCRIPTION.name(), this.notification.getServiceDescription());
        paramsMap.put(RESOURCE_UUID.name(), this.resource.getResourceUUID());
        paramsMap.put(RESOURCE_INSTANCE_NAME.name(), this.resource.getResourceInstanceName());
        paramsMap.put(RESOURCE_NAME.name(), this.resource.getResourceName());
        paramsMap.put(RESOURCE_VERSION.name(), this.resource.getResourceVersion());
        paramsMap.put(RESOURCE_TYPE.name(), this.resource.getResourceType());
        paramsMap.put(ARTIFACT_UUID.name(), this.artifact.getArtifactUUID());
        paramsMap.put(ARTIFACT_TYPE.name(), this.artifact.getArtifactType());
        paramsMap.put(ARTIFACT_VERSION.name(), this.artifact.getArtifactVersion());
        paramsMap.put(ARTIFACT_DESCRIPTION.name(), this.artifact.getArtifactDescription());
        paramsMap.put(CREATION_DATE.name(), getCurrentDateTime());
        paramsMap.put(ARTIFACT_NAME.name(), this.artifact.getArtifactName());
        paramsMap.put(ARTIFACT_CONTENT.name(), data);

        return paramsMap;
    }


    private String getCurrentDateTime() {
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        Date date = new Date();
        return dateFormat.format(date);
    }

    private void sendDCAEEvent(String distributionID, String serviceName, String serviceVersion, String errorMessage) {
        if (null == eventSender) return;
        String errorDescription = String.format("ASDC distribution of service '%s', version '%s' is failed with reason: '%s'",
                        serviceName, serviceVersion, errorMessage);

        EventMessage eventMessage = new EventMessage(
                        new EventHeader((new java.util.Date()).toString(), serviceVersion, distributionID),
                        new EventStatus(401, errorDescription));

        eventSender.sendEvent(MessageDestination.DCAE, eventMessage);
    }

}
