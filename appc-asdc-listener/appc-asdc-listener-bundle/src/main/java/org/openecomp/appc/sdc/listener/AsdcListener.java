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
import org.openecomp.sdc.impl.DistributionClientFactory;
import org.openecomp.sdc.utils.DistributionActionResultEnum;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.openecomp.appc.configuration.Configuration;
import org.openecomp.appc.configuration.ConfigurationFactory;
import org.openecomp.sdc.api.IDistributionClient;
import org.openecomp.sdc.api.results.IDistributionClientResult;
import org.openecomp.sdc.impl.DistributionClientFactory;
import org.openecomp.sdc.utils.DistributionActionResultEnum;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class AsdcListener {

    /**
     * The bundle context
     */
    private IDistributionClient client;
    private AsdcCallback callback;
    private AsdcConfig config;
    private CountDownLatch latch;


    private final EELFLogger logger = EELFManager.getInstance().getLogger(AsdcListener.class);

    @SuppressWarnings("unused")
    public void start() throws Exception {
        logger.info("Starting bundle ASDC Listener");
        Configuration configuration = ConfigurationFactory.getConfiguration();
        Properties props = configuration.getProperties();

        config = new AsdcConfig(props);

        client = DistributionClientFactory.createDistributionClient();
        callback = new AsdcCallback(config.getStoreOpURI(), client);

        latch = new CountDownLatch(1);

        new Thread(new Runnable() {
            @Override
            public void run() {
                initialRegistration(config);

                IDistributionClientResult result = client.init(config, callback);

                if (result.getDistributionActionResult() == DistributionActionResultEnum.SUCCESS) {
                    client.start();
                } else {
                    logger.error(String.format("Could not register ASDC client. %s - %s", result.getDistributionActionResult(),
                                    result.getDistributionMessageResult()));
                }

                latch.countDown();
            }
        }).start();
    }

    @SuppressWarnings("unused")
    public void stop() throws InterruptedException {
        logger.info("Stopping ASDC Listener");
        latch.await(10, TimeUnit.SECONDS);

        if (callback != null) {
            callback.stop();
        }
        if (client != null) {
            client.stop();

        }
        logger.info("ASDC Listener stopped successfully");
    }

    private boolean initialRegistration(AsdcConfig config) {
        try {
            final String jsonTemplate = "{\"consumerName\": \"%s\",\"consumerSalt\": \"%s\",\"consumerPassword\":\"%s\"}";
            String saltedPassStr = org.openecomp.tlv.sdc.security.Passwords.hashPassword(config.getPassword());
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
            URL url = new URL(
                    String.format("http%s://%s/sdc2/rest/v1/consumers", host.contains("443") ? "s" : "", host));

            logger.info(String.format("Attempting to register user %s on %s with salted pass of %s", config.getUser(),
                    url, saltedPass[1]));

            ProviderResponse result = ProviderOperations.post(url, json, headers);
            return result.getStatus() == 200;
        } catch (Exception e) {
            logger.error("Error performing initial registration with ASDC server. User may not be able to connect", e);
            return false;
        }
    }
}
