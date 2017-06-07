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
import org.openecomp.sdc.api.consumer.IConfiguration;

import java.net.URI;
import java.util.*;

public class AsdcConfig implements IConfiguration {

	private String host;
	private String consumer;
	private String consumerId;
	private String env;
	private String keystorePath;
	private String keystorePass;
	private int pollingInterval; // Time between listening sessions
	private int pollingTimeout; // Time to listen for (dmaap timeout url param)/1000
	private List<String> types = new ArrayList<>(1);
	private String user;
	private String pass;

	private URI storeOp;

	Properties props;

	private final EELFLogger logger = EELFManager.getInstance().getLogger(AsdcConfig.class);

	public AsdcConfig(Properties props) throws Exception {
		this.props = props;
		init();
	}

	private void init() throws Exception {
		if (props != null) {
			// Keystore for ca cert
			keystorePath = props.getProperty("appc.asdc.keystore.path");
			keystorePass = props.getProperty("appc.asdc.keystore.pass");

			// ASDC host
			host = props.getProperty("appc.asdc.host");
			env = props.getProperty("appc.asdc.env");
			user = props.getProperty("appc.asdc.user");
			pass = props.getProperty("appc.asdc.pass");

			// DMaaP properties
			consumer = props.getProperty("appc.asdc.consumer");
			consumerId = props.getProperty("appc.asdc.consumer.id");

			pollingInterval = Integer.valueOf(props.getProperty("interval", "60"));

			// Client uses cambriaClient-0.2.4 which throws non relevant (wrong)
			// exceptions with times > 30s
			pollingTimeout = Integer.valueOf(props.getProperty("timeout", "25"));

			// Anything less than 60 and we risk 429 Too Many Requests
			if (pollingInterval < 60) {
				pollingInterval = 60;
			}

			if (pollingInterval > pollingInterval) {
				logger.warn(String.format(
						"Message acknowledgement may be delayed by %ds in the ADSC listener. [Listening Time: %s, Poll Period: %s]",
						pollingInterval - pollingTimeout, pollingTimeout, pollingInterval));
			}

			logParams();

			// Download type
			types.add("APPC_CONFIG");
			types.add("VF_LICENSE");

			storeOp = new URI(props.getProperty("appc.asdc.provider.url"));
		}
	}

	@Override
	public boolean activateServerTLSAuth() {
		return false;
	}

	//@Override
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

	public URI getStoreOpURI() {
		return storeOp;
	}

	/**
	 * Logs the relevant parameters
	 */
	public void logParams() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("ASDC Host", getAsdcAddress());
		params.put("ASDC Environment", getEnvironmentName());
		params.put("Consumer Name", getConsumerGroup());
		params.put("Consumer ID", getConsumerID());
		params.put("Poll Active Wait", String.valueOf(getPollingInterval()));
		params.put("Poll Timeout", String.valueOf(getPollingTimeout()));

		logger.info(String.format("ASDC Params: %s", params));
	}
}
