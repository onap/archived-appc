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

package org.openecomp.appc.sdc.listener;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.openecomp.appc.adapter.message.EventSender;
import org.openecomp.appc.sdc.artifacts.ArtifactProcessor;
import org.openecomp.appc.sdc.artifacts.impl.ArtifactProcessorFactory;
import org.openecomp.sdc.api.IDistributionClient;
import org.openecomp.sdc.api.consumer.INotificationCallback;
import org.openecomp.sdc.api.notification.IArtifactInfo;
import org.openecomp.sdc.api.notification.INotificationData;
import org.openecomp.sdc.api.notification.IResourceInstance;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.openecomp.sdc.utils.DistributionStatusEnum;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import java.net.URI;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class SdcCallback implements INotificationCallback {

    private final EELFLogger logger = EELFManager.getInstance().getLogger(SdcCallback.class);
    private ArtifactProcessorFactory artifactProcessorFactory=new ArtifactProcessorFactory();

    private URI storeUri;
    private IDistributionClient client;

    private EventSender eventSender = null;

    private ThreadPoolExecutor executor;
    private int threadCount = 10;

    private AtomicBoolean isRunning = new AtomicBoolean(false);


    public SdcCallback(URI storeUri, IDistributionClient client) {
        this.storeUri = storeUri;
        this.client = client;

        // Create the thread pool
        executor = new ThreadPoolExecutor(threadCount, threadCount, 1, TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(threadCount * 2));

        // Custom Named thread factory
        BasicThreadFactory threadFactory = new BasicThreadFactory.Builder().namingPattern("Appc-Listener-%d").build();
        executor.setThreadFactory(threadFactory);

        isRunning.set(true);
    }

    @Override
    public void activateCallback(INotificationData data) {
        if (null == eventSender) {
            try {
                BundleContext bctx = FrameworkUtil.getBundle(EventSender.class).getBundleContext();
                ServiceReference sref = bctx.getServiceReference(EventSender.class);
                eventSender = (EventSender) bctx.getService(sref);
            } catch (Exception e) {
                logger.error("SdcCallback failed on initializing EventSender", e);
            }
        }

        if (isRunning.get()) {

            for(IArtifactInfo artifact:data.getServiceArtifacts()){
                ArtifactProcessor artifactProcessor = artifactProcessorFactory.getArtifactProcessor(client, eventSender, data, null, artifact, storeUri);
                if(artifactProcessor!=null){
                    executor.submit(artifactProcessor);
                }
            }

            for (IResourceInstance resource : data.getResources()) {
                for (IArtifactInfo artifact : resource.getArtifacts()) {
                    logger.info(Util.toSdcStoreDocumentInput(data, resource, artifact, "abc"));
                    if (executor.getQueue().size() >= threadCount) {
                        // log warning about job backlog
                    }
                    ArtifactProcessor artifactProcessor = artifactProcessorFactory.getArtifactProcessor(client, eventSender, data, resource, artifact, storeUri);
                    if(artifactProcessor != null){
                        executor.submit(artifactProcessor);
                    }
                    else{
                        /* Before refactoring of the DownloadAndStoreOp class, the approach was to download all the
                            artifacts, send the download status, and then perform the processing of artifact if it is
                            required. Now that we are downloading the artifacts only when its processing is required,
                            we are sending the download status as positive just to have the same behaviour as before
                            refactoring.
                         */
                        client.sendDownloadStatus(Util.buildDistributionStatusMessage(client, data, artifact, DistributionStatusEnum.DOWNLOAD_OK));
                        logger.error("Artifact type not supported : " + artifact.getArtifactType());
                    }
                }
            }
        } else {
            // TODO - return a failed result so sdc knows we are shut down
        }
    }

    public void stop() {
        stop(10);
    }

    public void stop(int waitSec) {
        isRunning.set(false);
        logger.info(String.format("Stopping the SDC listener and waiting up to %ds for %d pending jobs", waitSec,
            executor.getQueue().size()));
        boolean cleanShutdown = false;
        executor.shutdown();
        try {
            cleanShutdown = executor.awaitTermination(waitSec, TimeUnit.SECONDS);
            executor.shutdownNow(); // In case of timeout
        } catch (InterruptedException e) {
            logger.error("Error in SdcCallback for stop(int waitSec) method due to InterruptedException: reason= " + e.getMessage());
        }
        logger.info(String.format("Attempting to shutdown cleanly: %s", cleanShutdown ? "SUCCESS" : "FAILURE"));
        logger.info("Shutdown complete.");
    }

}
