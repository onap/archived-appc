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

package org.openecomp.appc.adapter.messaging.dmaap.impl;

import java.io.IOException;
import java.util.*;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
//import com.att.nsa.cambria.client.CambriaClientBuilders;
//import com.att.nsa.cambria.client.CambriaClientBuilders.ConsumerBuilder;
//import com.att.nsa.cambria.client.CambriaConsumer;

import com.att.nsa.mr.client.MRClientFactory;
import com.att.nsa.mr.client.MRConsumer;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.appc.adapter.message.Consumer;
import org.openecomp.appc.configuration.Configuration;
import org.openecomp.appc.configuration.ConfigurationFactory;
import org.openecomp.appc.metricservice.MetricRegistry;
import org.openecomp.appc.metricservice.MetricService;
import org.openecomp.appc.metricservice.impl.MetricServiceImpl;
import org.openecomp.appc.metricservice.metric.Metric;
import org.openecomp.appc.metricservice.metric.MetricType;
import org.openecomp.appc.metricservice.metric.DmaapRequestCounterMetric;
import org.openecomp.appc.metricservice.policy.PublishingPolicy;
import org.openecomp.appc.metricservice.publisher.LogPublisher;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

public class DmaapConsumerImpl implements Consumer {

    private static final EELFLogger LOG = EELFManager.getInstance().getLogger(DmaapConsumerImpl.class);
    private static final Configuration configuration = ConfigurationFactory.getConfiguration();
    // Default values
    private static final int DEFAULT_TIMEOUT_MS = 60000;
    private static final int DEFAULT_LIMIT = 1000;
    private static MetricRegistry metricRegistry;
    private String topic;
    private DmaapRequestCounterMetric dmaapKpiMetric;
    private boolean isMetricEnabled=false;
    private boolean useHttps = false;
    private MRConsumer client = null;
    private Properties props = null;


    public DmaapConsumerImpl(Collection<String> urls, String topicName, String consumerGroupName, String consumerId,String user, String password) {
        this(urls, topicName, consumerGroupName, consumerId,user, password,null);

    }

    public DmaapConsumerImpl(Collection<String> urls, String topicName, String consumerGroupName, String consumerId,String user, String password,String filter) {
        this.topic = topicName;
        this.props = new Properties();
        String urlsStr = StringUtils.join(urls, ',');
        props.setProperty("host",urlsStr);
        props.setProperty("group",consumerGroupName);
        props.setProperty("id",consumerId);
        props.setProperty("username",user);
        props.setProperty("password",password);
        if(filter != null) {
            props.setProperty("filter", filter);
        }
    }


    private void initMetric() {
        LOG.debug("Metric getting initialized");
        MetricService metricService = getMetricservice();
        metricRegistry = metricService.createRegistry("APPC");
        dmaapKpiMetric = metricRegistry.metricBuilderFactory().
                dmaapRequestCounterBuilder().
                withName("DMAAP_KPI").withType(MetricType.COUNTER).
                withRecievedMessage(0)
                .withPublishedMessage(0)
                .build();
        if (metricRegistry.register(dmaapKpiMetric)) {
            Metric[] metrics = new Metric[]{dmaapKpiMetric};
            LogPublisher logPublisher = new LogPublisher(metricRegistry, metrics);
            LogPublisher[] logPublishers = new LogPublisher[1];
            logPublishers[0] = logPublisher;
            PublishingPolicy manuallyScheduledPublishingPolicy = metricRegistry.policyBuilderFactory().
                    scheduledPolicyBuilder().withPublishers(logPublishers).
                    withMetrics(metrics).
                    build();
            LOG.debug("Policy getting initialized");
            manuallyScheduledPublishingPolicy.init();
            LOG.debug("Metric initialized");
        }
    }
    private MRConsumer getClient() {
        return getClient(DEFAULT_TIMEOUT_MS, DEFAULT_LIMIT);
    }

    /**
     * @return An instance of MRConsumer created from our class variables
     */
    private synchronized MRConsumer getClient(int waitMs, int limit) {
        try {
            props.setProperty("timeout",String.valueOf(waitMs));
            props.setProperty("limit",String.valueOf(limit));
            String topicProducerPropFileName = DmaapUtil.createConsumerPropFile(topic,props);
            return MRClientFactory.createConsumer ( topicProducerPropFileName);
        } catch (IOException e1) {
            LOG.error("failed to createConsumer",e1);
            return null;
        }
    }

    @Override
    public synchronized void updateCredentials(String key, String secret) {
        LOG.info(String.format("Setting auth to %s for %s", key, this.toString()));
        String user = key;
        String password = secret;
        props.setProperty("user",String.valueOf(user));
        props.setProperty("password",String.valueOf(password));
        client = null;
    }

    @Override
    public List<String> fetch(int waitMs, int limit) {
        Properties properties=configuration.getProperties();
        if(properties!=null && properties.getProperty("metric.enabled")!=null ){
          isMetricEnabled=Boolean.valueOf(properties.getProperty("metric.enabled"));
        }
        if(isMetricEnabled){
            initMetric();
        }
        LOG.debug(String.format("Fetching up to %d records with %dms wait on %s", limit, waitMs, this.toString()));
        List<String> out = new ArrayList<String>();

        // Create client once and reuse it on subsequent fetches. This is
        // to support failover to other servers in the DMaaP cluster.
        if (client == null) {
        	LOG.info("Getting DMaaP Client ...");
        	client = getClient(waitMs, limit);
        }
        try {
            for (String s : client.fetch(waitMs, limit)) {
                out.add(s);
                if(isMetricEnabled){
                    ((DmaapRequestCounterMetric)metricRegistry.metric("DMAAP_KPI")).incrementRecievedMessage();
                }
            }
            LOG.debug(String.format("Got %d records from %s", out.size(), this.toString()));
        } catch (Exception e) {
            // Connection exception
            LOG.error(String.format("Dmaap Connection Issue Detected. %s", e.getMessage()));
            e.printStackTrace();
            try {
                LOG.warn(String.format("Sleeping for %dms to compensate for connection failure", waitMs));
                Thread.sleep(waitMs);
            } catch (InterruptedException e2) {
                LOG.warn(String.format("Failed to wait for %dms after bad fetch", waitMs));
            }
        }


        return out;
    }

    /**
     * Close consumer Dmaap client
     */
    @Override
    public void close() {
    	LOG.debug("Closing Dmaap consumer client....");
    	if (client != null) {
    		client.close();
    	}
    }

    @Override
    public List<String> fetch() {
        return fetch(DEFAULT_TIMEOUT_MS, DEFAULT_LIMIT);
    }

    @Override
    public String toString() {
        String hostStr = (props == null || props.getProperty("host") == null? "N/A" : props.getProperty("host"));
        String group = (props == null || props.getProperty("group") == null? "N/A" : props.getProperty("group"));
        String id = (props == null || props.getProperty("id") == null? "N/A" : props.getProperty("id"));
        return String.format("Consumer %s/%s listening to %s on [%s]", group, id, topic, hostStr);
    }

    @Override
    public void useHttps(boolean yes) {
        useHttps = yes;
    }


    private MetricService getMetricservice() {
        BundleContext bctx = FrameworkUtil.getBundle(MetricService.class).getBundleContext();
        // Get AAIadapter reference
        ServiceReference sref = bctx.getServiceReference(MetricService.class.getName());
        if (sref != null) {
            LOG.info("Metric Service from bundlecontext");
            return (MetricServiceImpl) bctx.getService(sref);

        } else {
            LOG.info("Metric Service error from bundlecontext");
            LOG.warn("Cannot find service reference for org.openecomp.appc.metricservice.MetricService");
            return null;

        }
    }

    public  Metric getMetric(String name){
        return metricRegistry.metric(name);
    }
}
