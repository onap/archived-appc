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
import org.openecomp.sdc.api.consumer.IConfiguration;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class SdcConfig implements IConfiguration {

    private String host;
    private String consumer;
    private String consumerId;
    private String env;
    private String keystorePath;
    private String keystorePass;
    /** Polling internal is time between listening sessions */
    private int pollingInterval;
    /** Polling timeout is the time to listen for (dmaap timeout url param)/1000 */
    private int pollingTimeout;
    private List<String> types = new ArrayList<>();
    private String user;
    private String pass;

    private URI storeOp;

    private Properties props;

    private final EELFLogger logger = EELFManager.getInstance().getLogger(SdcConfig.class);

    SdcConfig(Properties props) throws Exception {
        this.props = props;
        init();
    }

    private void init() throws Exception {
        if (props == null) {
            logger.error("SdcConfig init is skipped due to properties is null");
            return;
        }

        // Keystore for ca cert
        keystorePath = props.getProperty("appc.sdc.keystore.path");
        keystorePass = props.getProperty("appc.sdc.keystore.pass");

        // ASDC host
        host = props.getProperty("appc.sdc.host");
        env = props.getProperty("appc.sdc.env");
        user = props.getProperty("appc.sdc.user");
        pass = props.getProperty("appc.sdc.pass");

        // DMaaP properties
        consumer = props.getProperty("appc.sdc.consumer");
        consumerId = props.getProperty("appc.sdc.consumer.id");

        pollingInterval = Integer.valueOf(props.getProperty("interval", "60"));

        // Client uses cambriaClient-0.2.4 which throws non relevant (wrong)
        // exceptions with times > 30s
        pollingTimeout = Integer.valueOf(props.getProperty("timeout", "25"));

        // Anything less than 60 and we risk 429 Too Many Requests
        if (pollingInterval < 60) {
            pollingInterval = 60;
        }

        if (pollingInterval > pollingTimeout) {
            logger.warn(String.format(
                    "Message acknowledgement may be delayed by %ds in the ADSC listener. [Listening Time: %s, Poll Period: %s]",
                    pollingInterval - pollingTimeout, pollingTimeout, pollingInterval));
        }

        logParams();

        // Download type
        /*
          This types seems redundant, as it looks from the code that they are not being used anywhere
        */
        types.add("APPC_CONFIG");
        types.add("VF_LICENSE");
        types.add("TOSCA_CSAR");

        storeOp = new URI(props.getProperty("appc.sdc.provider.url"));
    }

    @Override
    public boolean activateServerTLSAuth() {
        return false;
    }

    public boolean isFilterInEmptyResources() {
        return false;
    }

    @Override
    public String getAsdcAddress() {
        return host;
    }

    @Override
    public String getConsumerGroup() {
        return consumer;
    }

    @Override
    public String getConsumerID() {
        return consumerId;
    }

    @Override
    public String getEnvironmentName() {
        return env;
    }

    @Override
    public String getKeyStorePassword() {
        return keystorePass;
    }

    @Override
    public String getKeyStorePath() {
        return keystorePath;
    }

    @Override
    public String getPassword() {
        return pass;
    }

    @Override
    public int getPollingInterval() {
        return pollingInterval;
    }

    @Override
    public int getPollingTimeout() {
        return pollingTimeout;
    }

    @Override
    public List<String> getRelevantArtifactTypes() {
        return types;
    }

    @Override
    public String getUser() {
        return user;
    }

    URI getStoreOpURI() {
        return storeOp;
    }

    /**
     * Logs the relevant parameters
     */
    private void logParams() {
        Map<String, String> params = new HashMap<>();
        params.put("SDC Host", getAsdcAddress());
        params.put("SDC Environment", getEnvironmentName());
        params.put("Consumer Name", getConsumerGroup());
        params.put("Consumer ID", getConsumerID());
        params.put("Poll Active Wait", String.valueOf(getPollingInterval()));
        params.put("Poll Timeout", String.valueOf(getPollingTimeout()));

        logger.info(String.format("SDC Params: %s", params));
    }
}
