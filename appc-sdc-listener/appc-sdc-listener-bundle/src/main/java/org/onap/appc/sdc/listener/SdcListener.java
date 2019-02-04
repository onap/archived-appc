/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.sdc.listener;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.onap.appc.configuration.Configuration;
import org.onap.appc.configuration.ConfigurationFactory;
import org.onap.sdc.api.IDistributionClient;
import org.onap.sdc.api.results.IDistributionClientResult;
import org.onap.sdc.impl.DistributionClientFactory;
import org.onap.sdc.utils.DistributionActionResultEnum;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * SDC listener handles bundle start and stop through start and stop method. <p>
 * Register connection with SDC server based on properties file configuration when start,
 * and disconnect with SDC server when stop.
 */
public class SdcListener {
    private final EELFLogger logger = EELFManager.getInstance().getLogger(SdcListener.class);

    /**
     * The bundle context
     */
    private IDistributionClient client;
    private SdcCallback callback;
    private SdcConfig config;
    private CountDownLatch latch;

    private Thread startThread = null;
    private String ukey;
    private String uval;

    @SuppressWarnings("unused")
    public void start() throws Exception {
        // Add timestamp to the log to differentiate the jmeter run testing calls.
        final long timeStamp = System.currentTimeMillis();
        logger.info(String.format("[%d] Starting SDC Listener", timeStamp));

        Configuration configuration = ConfigurationFactory.getConfiguration();
        Properties props = configuration.getProperties();
        config = new SdcConfig(props);
        ukey = props.getProperty("appc.sdc.provider.user");
        uval = props.getProperty("appc.sdc.provider.pass");
        logger.debug(String.format("[%d] created SDC config provider URL [%s]", timeStamp, config.getStoreOpURI().toString()));


        client = DistributionClientFactory.createDistributionClient();
        logger.debug(String.format("[%d] created SDC client", timeStamp));

        callback = new SdcCallback(config.getStoreOpURI(), client);
        logger.debug(String.format("[%d] created SDC callback", timeStamp));

        latch = new CountDownLatch(1);

        startThread = new Thread(new StartRunnable(timeStamp));
        startThread.setName(String.format("[%d] sdcListener start", timeStamp));
        logger.debug(String.format("[%d] created SDC initialization thread", timeStamp));
        startThread.start();
    }

    @SuppressWarnings("unused")
    public void stop() throws InterruptedException {
        // Add timestamp to the log to differentiate the jmeter run testing calls.
        final long timeStamp = System.currentTimeMillis();
        logger.info(String.format("[%d] Stopping SDC Listener", timeStamp));

        stopStartThread(timeStamp);

        if (latch != null) {
            logger.debug(String.format("[%d] waiting SDC latch count to 0 for 10 seconds", timeStamp));
            latch.await(10, TimeUnit.SECONDS);
            latch = null;
        }

        if (callback != null) {
            logger.debug(String.format("[%d] stopping SDC callback", timeStamp));
            callback.stop();
            callback = null;
        }
        if (client != null) {
            logger.debug(String.format("[%d] stopping SDC client", timeStamp));
            client.stop();
            client = null;

        }
        logger.info(String.format("[%d] SDC Listener stopped successfully", timeStamp));
    }

    void stopStartThread(long timeStamp) throws InterruptedException {
        if (startThread == null) {
            return;
        }

        if (startThread.getState() == Thread.State.TERMINATED) {
            logger.debug(String.format("[%d] SDC thread(%s) is already terminated.",
                    timeStamp, startThread.getName()));
        } else {
            logger.debug(String.format("[%d] SDC thread(%s) is to be interrupted with state(%s)",
                    timeStamp, startThread.getName(), startThread.getState().toString()));

            startThread.interrupt();

            logger.debug(String.format("[%d] SDC thread(%s) has been interrupted(%s) with state(%s)",
                    timeStamp, startThread.getName(), startThread.isInterrupted(),
                    startThread.getState().toString()));
        }
        startThread = null;
    }

    /**
     * Runnable implementation for actual initialization during SDC listener start
     */
    class StartRunnable implements Runnable {
        private final long timeStamp;

        StartRunnable(long theTimeStamp) {
            timeStamp = theTimeStamp;
        }

        /**
         * This run method calls SDC client for init and start which are synchronized calls along with stop.
         * To interrupt this thread at stop time, we added thread interrupted checking in each step
         * for earlier interruption.
         */
        @Override
        public void run() {
            if (!initialRegistration()) {
                logger.warn(String.format("[%d] SDC thread initial registration failed.", timeStamp));
            }

            if (isThreadInterrupted("after initial registration")) {
                return;
            }

            IDistributionClientResult result = client.init(config, callback);

            if (isThreadInterrupted("after client init")) {
                return;
            }

            if (result.getDistributionActionResult() == DistributionActionResultEnum.SUCCESS) {
                client.start();
            } else {
                logger.error(String.format("[%d] Could not register SDC client. %s - %s",
                        timeStamp, result.getDistributionActionResult(), result.getDistributionMessageResult()));
            }

            latch.countDown();
        }

        private boolean initialRegistration() {
            try {
                final String jsonTemplate =
                        "{\"consumerName\": \"%s\",\"consumerSalt\": \"%s\",\"consumerPassword\":\"%s\"}";
                String saltedPassStr = org.onap.tlv.sdc.security.Passwords.hashPassword(config.getPassword());
                if (saltedPassStr == null || !saltedPassStr.contains(":")) {
                    return false;
                }

                String[] saltedPass = saltedPassStr.split(":");
                String json = String.format(jsonTemplate, config.getUser(), saltedPass[0], saltedPass[1]);

                Map<String, String> headers = new HashMap<>();
                // TODO - Replace the header below to sdc's requirements. What should the new value be
                headers.put("USER_ID", "test");

                // TODO - How to format the url. Always same endpoint or ports?
                String host = config.getAsdcAddress();
                URL url = new URL(String.format("http%s://%s/sdc2/rest/v1/consumers",
                        host.contains("443") ? "s" : "", host));

                logger.info(String.format("Attempting to register user %s on %s with salted pass of %s",
                        config.getUser(), url, saltedPass[1]));

                ProviderOperations providerOperations = new ProviderOperations();
                ProviderOperations.setDefaultUrl(config.getStoreOpURI().toURL());
                ProviderOperations.setAuthentication(ukey, uval);
                ProviderResponse result = providerOperations.post(url, json, headers);
                return result.getStatus() == 200;
            } catch (Exception e) {
                logger.error(
                        "Error performing initial registration with SDC server. User may not be able to connect",
                        e);
                return false;
            }
        }

        private boolean isThreadInterrupted(String details) {
            if (Thread.currentThread().isInterrupted()) {
                logger.info(String.format("[%d] SDC thread interrupted %s.", timeStamp, details));
                return true;
            }
            return false;
        }
    }
}
