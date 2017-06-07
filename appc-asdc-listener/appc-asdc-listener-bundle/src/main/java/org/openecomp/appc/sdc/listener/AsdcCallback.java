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

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.openecomp.appc.adapter.message.EventSender;
import org.openecomp.sdc.api.IDistributionClient;
import org.openecomp.sdc.api.consumer.INotificationCallback;
import org.openecomp.sdc.api.notification.IArtifactInfo;
import org.openecomp.sdc.api.notification.INotificationData;
import org.openecomp.sdc.api.notification.IResourceInstance;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import java.net.URI;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class AsdcCallback implements INotificationCallback {

    private final EELFLogger logger = EELFManager.getInstance().getLogger(AsdcCallback.class);

    private URI storeUri;
    private IDistributionClient client;

    private EventSender eventSender = null;

    private ThreadPoolExecutor executor;
    private int threadCount = 10;

    private AtomicBoolean isRunning = new AtomicBoolean(false);


    public AsdcCallback(URI storeUri, IDistributionClient client) {
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
            } catch (Throwable t) {
                logger.error("AsdcCallback failed on initializing EventSender", t);
            }
        }

        if (isRunning.get()) {
            for (IResourceInstance resource : data.getResources()) {
                for (IArtifactInfo artifact : resource.getArtifacts()) {
                    logger.info(Util.toAsdcStoreDocumentInput(data, resource, artifact, "abc"));
                    if (executor.getQueue().size() >= threadCount) {
                        // log warning about job backlog
                    }
                    executor.submit(new DownloadAndStoreOp(client, eventSender, data, resource, artifact, storeUri));
                }
            }
        } else {
            // TODO - return a failed result so asdc knows we are shut down
        }
    }

    public void stop() {
        stop(10);
    }

    public void stop(int waitSec) {
        isRunning.set(false);
        logger.info(String.format("Stopping the ASDC listener and waiting up to %ds for %d pending jobs", waitSec,
            executor.getQueue().size()));
        boolean cleanShutdown = false;
        executor.shutdown();
        try {
            cleanShutdown = executor.awaitTermination(waitSec, TimeUnit.SECONDS);
            executor.shutdownNow(); // In case of timeout
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info(String.format("Attempting to shutdown cleanly: %s", cleanShutdown ? "SUCCESS" : "FAILURE"));
        logger.info("Shutdown complete.");
    }

}
