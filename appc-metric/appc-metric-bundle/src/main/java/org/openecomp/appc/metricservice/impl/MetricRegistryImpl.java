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

package org.openecomp.appc.metricservice.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.openecomp.appc.metricservice.MetricRegistry;
import org.openecomp.appc.metricservice.metric.Counter;
import org.openecomp.appc.metricservice.metric.Metric;
import org.openecomp.appc.metricservice.metric.MetricBuilderFactory;
import org.openecomp.appc.metricservice.metric.impl.MetricBuilderFactoryImpl;
import org.openecomp.appc.metricservice.policy.PolicyBuilderFactory;
import org.openecomp.appc.metricservice.policy.PublishingPolicy;
import org.openecomp.appc.metricservice.policy.impl.PolicyBuilderFactoryImpl;


public class MetricRegistryImpl implements MetricRegistry {
    private String name;
    private Map<String,Metric> concurrentMetricMap=new ConcurrentHashMap<String,Metric>();

    public MetricRegistryImpl(String name) {
        this.name = name;
    }

    @Override
    public boolean register(Metric metric) {
        if(concurrentMetricMap.get(metric.name())==null){
            concurrentMetricMap.put(metric.name(),metric);
            return true;
        }
        return false;
    }

    @Override
    public void attach(PublishingPolicy publishPolicy) {
//TODO
    }

    @Override
    public MetricBuilderFactory metricBuilderFactory() {
        return new MetricBuilderFactoryImpl();
    }

    @Override
    public PolicyBuilderFactory policyBuilderFactory() {
        return new PolicyBuilderFactoryImpl() ;
    }

    @Override
    public Counter counter(String value) {
        if(concurrentMetricMap.get(value)!=null )
            return (Counter)concurrentMetricMap.get(value) ;
        else
            return null;

    }

    @Override
    public Counter[] counters() {
        return (Counter[])concurrentMetricMap.values().toArray();
    }

    @Override
    public Metric[] metrics() {
        java.util.Collection<Metric> var = concurrentMetricMap.values();
        return var.toArray(new Metric[var.size()]);
    }

    @Override
    public Metric metric(String metricName) {
        return concurrentMetricMap.get(metricName);
    }

    @Override
    public void dispose() {
        concurrentMetricMap.clear();
    }
}
