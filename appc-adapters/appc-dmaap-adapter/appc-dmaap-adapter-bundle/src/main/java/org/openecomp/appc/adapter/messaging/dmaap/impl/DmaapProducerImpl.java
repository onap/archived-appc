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

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
//import com.att.nsa.cambria.client.CambriaBatchingPublisher;
//import com.att.nsa.cambria.client.CambriaClientBuilders;
//import com.att.nsa.cambria.client.CambriaClientBuilders.PublisherBuilder;

import com.att.nsa.mr.client.MRBatchingPublisher;
import com.att.nsa.mr.client.MRClientFactory;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.appc.adapter.message.Producer;
import org.openecomp.appc.adapter.messaging.dmaap.impl.DmaapUtil;
import org.openecomp.appc.configuration.Configuration;
import org.openecomp.appc.configuration.ConfigurationFactory;
import org.openecomp.appc.metricservice.MetricRegistry;
import org.openecomp.appc.metricservice.MetricService;
import org.openecomp.appc.metricservice.metric.Metric;
import org.openecomp.appc.metricservice.metric.MetricType;
import org.openecomp.appc.metricservice.metric.DmaapRequestCounterMetric;
import org.openecomp.appc.metricservice.policy.PublishingPolicy;
import org.openecomp.appc.metricservice.publisher.LogPublisher;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

public class DmaapProducerImpl implements Producer {

    private static final EELFLogger LOG = EELFManager.getInstance().getLogger(DmaapProducerImpl.class);
    private static final Configuration configuration = ConfigurationFactory.getConfiguration();

    private Set<String> topics = new HashSet<String>();

    private Properties props = null;
    private static MetricRegistry metricRegistry;
    private boolean useHttps = false;
    private DmaapRequestCounterMetric dmaapKpiMetric;
    private boolean isMetricEnabled=false;
    
    private Set<MRBatchingPublisher> clients;

    
    public DmaapProducerImpl(Collection<String> urls, String topicName, String user, String password) {
        this(urls, (Set<String>)null, user, password);
        this.topics = new HashSet<>();
        if (topicName != null) {
            for (String topic : topicName.split(",")) {
                topics.add(topic);
            }
        }
    }

    public DmaapProducerImpl(Collection<String> urls, Set<String> topicNames, String user, String password) {
        topics = topicNames;
        if(urls == null || user == null || password == null){
            throw new IllegalArgumentException("one of these mandaory argument is null: urls, user, password" );
        }
        this.props = new Properties();
        String urlsStr = StringUtils.join(urls, ',');
        props.setProperty("host",urlsStr);
        props.setProperty("id", UUID.randomUUID().toString());
        props.setProperty("username",user);
        props.setProperty("password",password);
    }
    private void initMetric() {
        LOG.debug("Metric getting initialized");
        MetricService metricService = getMetricservice();
        metricRegistry=metricService.createRegistry("APPC");
        dmaapKpiMetric = metricRegistry.metricBuilderFactory().
                dmaapRequestCounterBuilder().
                withName("DMAAP_KPI").withType(MetricType.COUNTER).
                withRecievedMessage(0)
                .withPublishedMessage(0)
                .build();
        if(metricRegistry.register(dmaapKpiMetric)) {
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
    private Set<MRBatchingPublisher> getClients() {
        Set<MRBatchingPublisher> out = new HashSet<MRBatchingPublisher>();
        for (String topic : topics) {
            try {
                String topicProducerPropFileName = DmaapUtil.createProducerPropFile(topic,props);
                final MRBatchingPublisher client = MRClientFactory.createBatchingPublisher (topicProducerPropFileName);
                out.add(client);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return out;
    }

    @Override
    public synchronized void updateCredentials(String key, String secret) {
        LOG.info(String.format("Setting auth to %s for %s", key, this.toString()));
        String user = key;
        String password = secret;
        props.setProperty("user",String.valueOf(user));
        props.setProperty("password",String.valueOf(password));
        clients = null;
    }

    @Override
    public boolean post(String partition, String data) {
        boolean success = true;
        Properties properties=configuration.getProperties();
        if(properties!=null && properties.getProperty("metric.enabled")!=null ){
            isMetricEnabled=Boolean.valueOf(properties.getProperty("metric.enabled"));
        }
        if(isMetricEnabled){
            initMetric();
        }
        
        // Create clients once and reuse them on subsequent posts. This is 
        // to support failover to other servers in the Dmaap cluster.
        if ((clients == null) || (clients.isEmpty())) {
        	LOG.info("Getting CambriaBatchingPublisher Clients ...");
        	clients = getClients();
        }
        
        for (MRBatchingPublisher client : clients) {
            try {
                LOG.debug(String.format("Posting %s to %s", data, client));
                client.send(partition, data);
            } catch (IOException e) {
                e.printStackTrace();
                success = false;
            }
        }
        if(isMetricEnabled){
            ( (DmaapRequestCounterMetric) metricRegistry.metric("DMAAP_KPI")).incrementPublishedMessage();
        }
        return success;
    }

    /**
     * Close producer Dmaap client
     */
    @Override
    public void close() {
    	if ((clients == null) || (clients.isEmpty())) {
    		return;
    	}

    	LOG.debug("Closing Dmaap producer clients....");
    	for (MRBatchingPublisher client : clients) {
            try {
            	client.close(1, TimeUnit.SECONDS);
            }  catch (IOException | InterruptedException e) {
                LOG.warn(String.format("Failed to cleanly close Dmaap connection for [%s]", client));
                e.printStackTrace();
            }
        }
    }

    @Override
    public void useHttps(boolean yes) {
        useHttps = yes;
    }

    private MetricService getMetricservice() {
/*
        return AppcDmaapAdapterActivator.getMetricService();
*/

        BundleContext bctx = FrameworkUtil.getBundle(MetricService.class).getBundleContext();
        ServiceReference sref = bctx.getServiceReference(MetricService.class.getName());
        if (sref != null) {
            LOG.info("Metric Service from bundlecontext");
            return (MetricService) bctx.getService(sref);

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
