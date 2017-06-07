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

package org.openecomp.appc.adapter.messaging.dmaap.http;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.openecomp.appc.adapter.message.Producer;

public class HttpDmaapProducerImpl extends CommonHttpClient implements Producer {

    private static final EELFLogger LOG = EELFManager.getInstance().getLogger(HttpDmaapProducerImpl.class);

    private static final String CONTENT_TYPE = "application/cambria";
    private static final String URL_TEMPLATE = "%s/events/%s";

    private List<String> hosts;
    private Set<String> topics;

    private boolean useHttps = false;

    public HttpDmaapProducerImpl(Collection<String> urls, String topicName) {
        hosts = new ArrayList<String>();
        topics = new HashSet<String>();
        topics.add(topicName);

        for (String host : urls) {
            hosts.add(formatHostString(host));
        }
    }

    public HttpDmaapProducerImpl(Collection<String> urls, Set<String> topicNames) {
        hosts = new ArrayList<String>();
        topics = topicNames;

        for (String host : urls) {
            hosts.add(formatHostString(host));
        }
    }

    @Override
    public void updateCredentials(String user, String pass) {
        LOG.debug(String.format("Setting auth to %s for %s", user, this.toString()));
        this.setBasicAuth(user, pass);
    }

    @Override
    public boolean post(String partition, String data) {
        int sent = 0;
        try {
            HttpPost request = postReq(null);
            request.setHeader("Content-Type", CONTENT_TYPE);
            request.setEntity(new StringEntity(bodyLine(partition, data)));

            for (String topic : topics) {
                String uriStr = String.format(URL_TEMPLATE, hosts.get(0), topic);
                try {
                    request.setURI(new URI(uriStr));
                    CloseableHttpResponse response = getClient().execute(request);
                    if (response.getStatusLine().getStatusCode() == 200) {
                        sent++;
                    }
                    response.close();
                } catch (Exception sendEx) {
                    LOG.error(String.format("Failed to send message to %s. Reason: %s", uriStr, sendEx.getMessage()),
                        sendEx);
                    if (hosts.size() > 1) {
                        String failedUrl = hosts.remove(0);
                        hosts.add(failedUrl);
                        LOG.debug(String.format("Moving host %s to the end of the pool. New primary host is %s",
                            failedUrl, hosts.get(0)));
                    }
                }
            }
        } catch (Exception buildEx) {
            LOG.error(
                String.format("Failed to build request with string [%s]. Message not sent to any topic. Reason: %s",
                    data, buildEx.getMessage()),
                buildEx);
        }
        return sent == topics.size();
    }

    @Override
    public void useHttps(boolean yes) {
        useHttps = yes;
    }

    /**
     * Format the body for the application/cambria content type with no partitioning.
     *
     * @param msg
     *            The message body to format
     * @return A string in the application/cambria content type
     */
    private String bodyLine(String partition, String msg) {
        String p = (partition == null) ? "" : partition;
        String m = (msg == null) ? "" : msg;
        return String.format("%d.%d.%s%s", p.length(), m.length(), p, m);
    }

	@Override
	public void close() {
		// Nothing to do		
	}
}
