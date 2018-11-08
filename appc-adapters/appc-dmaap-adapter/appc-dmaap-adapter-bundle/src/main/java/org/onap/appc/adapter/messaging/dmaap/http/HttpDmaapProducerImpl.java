/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.adapter.messaging.dmaap.http;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.onap.appc.adapter.message.Producer;

public class HttpDmaapProducerImpl extends CommonHttpClient implements Producer {

    private static final EELFLogger LOG = EELFManager.getInstance().getLogger(HttpDmaapProducerImpl.class);

    private static final String CONTENT_TYPE = "application/cambria";
    private static final String URL_TEMPLATE = "%s/events/%s";

    private List<String> hosts;
    private Set<String> topics;

    public HttpDmaapProducerImpl() {
        //for test purposes
    }

    public HttpDmaapProducerImpl(Collection<String> urls, String topicName) {
        topics = new HashSet<>();
        topics.add(topicName);

        hosts = new ArrayList<>();
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
    	LOG.debug("Entering HttpDmaapProducerImpl::: post ");
        long sent = 0;
        try {
            HttpPost request = postReq(null);
            request.setHeader("Content-Type", CONTENT_TYPE);
            request.setEntity(new StringEntity(bodyLine(partition, data)));

            LOG.debug("Before sendRequest HttpDmaapProducerImpl::: post ");
            sent = topics.stream()
                .filter(topic -> sendRequest(request, topic))
                .count();

        } catch (Exception buildEx) {
            LOG.error(
                String.format("Failed to build request with string [%s]. Message not sent to any topic. Reason: %s",
                    data, buildEx.getMessage()),
                buildEx);
        }
        LOG.debug("Exiting HttpDmaapProducerImpl::: post ");
        return sent == topics.size();
    }

    private boolean sendRequest(HttpPost request, String topic) {
        boolean successful = false;
        String uriStr = String.format(URL_TEMPLATE, hosts.get(0), topic);
        try {
            request.setURI(new URI(uriStr));
            LOG.debug("HttpDmaapProducerImpl::: before sendRequest()");
            CloseableHttpResponse response = getClient().execute(request);
            LOG.debug("HttpDmaapProducerImpl::: after sendRequest()");
            if (response.getStatusLine().getStatusCode() == 200) {
                successful = true;
            }
            else {
            	LOG.debug("HttpDmaapProducerImpl::: did not receive 200 for sendRequest");
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
        return successful;
    }

    /**
     * Format the body for the application/cambria content type with no partitioning. See
     *
     * @param message
     *            The message body to format
     * @return A string in the application/cambria content type
     */
    private String bodyLine(String partition, String message) {
        String prt = (partition == null) ? "" : partition;
        String msg = (message == null) ? "" : message;
        return String.format("%d.%d.%s%s", prt.length(), msg.length(), prt, msg);
    }

}
