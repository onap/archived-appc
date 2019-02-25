/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications Copyright (C) 2019 Ericsson
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

package org.onap.appc.metricservice.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.onap.appc.metricservice.MetricRegistry;
import org.onap.appc.metricservice.metric.Counter;
import org.onap.appc.metricservice.metric.Metric;
import org.onap.appc.metricservice.metric.MetricBuilderFactory;
import org.onap.appc.metricservice.metric.impl.MetricBuilderFactoryImpl;
import org.onap.appc.metricservice.policy.PolicyBuilderFactory;
import org.onap.appc.metricservice.policy.PublishingPolicy;
import org.onap.appc.metricservice.policy.impl.PolicyBuilderFactoryImpl;


public class MetricRegistryImpl implements MetricRegistry {
    private String name;
    // Map can contain Counters, DispatchingFunctionMetrics and DMaapRequestCounterMetrics
    // and there are methods to retrieve only the 'Counter' types
    private Map<String, Metric> concurrentMetricMap = new ConcurrentHashMap<>();

    public MetricRegistryImpl(String name) {
        this.name = name;
    }

    @Override
    public boolean register(Metric metric) {
        return (concurrentMetricMap.putIfAbsent(metric.name(), metric) == null);
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
        Metric metric = concurrentMetricMap.get(value);
        if (metric instanceof Counter) {
            return (Counter)metric;
        }
        else return null;
    }

    @Override
    public Counter[] counters() {
        List<Counter> counterList = new ArrayList<>();
        for (Metric m: concurrentMetricMap.values()) {
            if (m instanceof Counter) {
                counterList.add((Counter) m);
            }
        }
        return counterList.toArray(new Counter[counterList.size()]);
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
