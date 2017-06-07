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
import java.util.List;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.openecomp.appc.adapter.message.Consumer;

public class HttpDmaapConsumerImpl extends CommonHttpClient implements Consumer {

    private static final EELFLogger LOG = EELFManager.getInstance().getLogger(HttpDmaapConsumerImpl.class);

    // Default values
    private static final int DEFAULT_TIMEOUT_MS = 15000;
    private static final int DEFAULT_LIMIT = 1000;
    private static final String HTTPS_PORT = ":3905";
    private static final String URL_TEMPLATE = "%s/events/%s/%s/%s";

    private List<String> urls;
    private String filter;

    private boolean useHttps = false;

    public HttpDmaapConsumerImpl(Collection<String> hosts, String topicName, String consumerName, String consumerId) {
        this(hosts, topicName, consumerName, consumerId, null);
    }

    public HttpDmaapConsumerImpl(Collection<String> hosts, String topicName, String consumerName, String consumerId,
                                 String filter) {
        this(hosts, topicName, consumerName, consumerId, filter, null, null);
    }

    public HttpDmaapConsumerImpl(Collection<String> hosts, String topicName, String consumerName, String consumerId,
                                 String filter, String user, String password) {
        urls = new ArrayList<String>();
        for (String host : hosts) {
            urls.add(String.format(URL_TEMPLATE, formatHostString(host), topicName, consumerName, consumerId));
        }
        this.filter = filter;
        updateCredentials(user, password);
    }

    @Override
    public void updateCredentials(String user, String pass) {
        LOG.debug(String.format("Setting auth to %s for %s", user, this.toString()));
        this.setBasicAuth(user, pass);
    }

    @Override
    public List<String> fetch(int waitMs, int limit) {
        LOG.debug(String.format("Fetching up to %d records with %dms wait on %s", limit, waitMs, this.toString()));
        List<String> out = new ArrayList<String>();
        try {
            List<NameValuePair> urlParams = new ArrayList<NameValuePair>();
            urlParams.add(new BasicNameValuePair("timeout", String.valueOf(waitMs)));
            urlParams.add(new BasicNameValuePair("limit", String.valueOf(limit)));
            if (filter != null) {
                urlParams.add(new BasicNameValuePair("filter", filter));
            }

            URIBuilder builder = new URIBuilder(urls.get(0));
            builder.setParameters(urlParams);

            URI uri = builder.build();
            LOG.info(String.format("GET %s", uri));
            HttpGet request = getReq(uri, waitMs);
            CloseableHttpResponse response = getClient().execute(request);

            int httpStatus = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            String body = (entity != null) ? EntityUtils.toString(entity) : null;

            LOG.debug(String.format("Request to %s completed with status %d and a body size of %s", uri, httpStatus,
                (body != null ? body.length() : "null")));

            response.close();
            if (httpStatus == 200 && body != null) {
                JSONArray json = new JSONArray(body);
                LOG.info(String.format("Got %d messages from DMaaP", json.length()));
                for (int i = 0; i < json.length(); i++) {
                    out.add(json.getString(i));
                }
            } else {
                LOG.error(String.format("Did not get 200 from DMaaP. Got %d - %s", httpStatus, body));
                sleep(waitMs);
            }
        } catch (Exception e) {
            if (urls.size() > 1) {
                String failedUrl = urls.remove(0);
                urls.add(failedUrl);
                LOG.debug(String.format("Moving host %s to the end of the pool. New primary host is %s", failedUrl,
                    urls.get(0)));
            }
            LOG.error(String.format("Got exception while querying DMaaP. Message: %s", e.getMessage()), e);
            sleep(waitMs);
        }

        return out;
    }

    @Override
    public List<String> fetch() {
        return fetch(DEFAULT_TIMEOUT_MS, DEFAULT_LIMIT);
    }

    @Override
    public String toString() {
        String hostStr = (urls == null || urls.isEmpty()) ? "N/A" : urls.get(0);
        return String.format("Consumer listening to [%s]", hostStr);
    }

    @Override
    public void useHttps(boolean yes) {
        useHttps = yes;
    }

    private void sleep(int ms) {
        LOG.info(String.format("Sleeping for %ds after failed request", ms / 1000));
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e1) {
            LOG.error("Interrupted while sleeping");
        }
    }

	@Override
	public void close() {
		// Nothing to do		
	}

}
