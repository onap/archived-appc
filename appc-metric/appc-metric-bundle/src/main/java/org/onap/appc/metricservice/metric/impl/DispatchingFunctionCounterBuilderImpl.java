/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications (C) 2019 Ericsson
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

package org.onap.appc.metricservice.metric.impl;

import org.onap.appc.metricservice.metric.DispatchingFunctionCounterBuilder;
import org.onap.appc.metricservice.metric.DispatchingFuntionMetric;
import org.onap.appc.metricservice.metric.MetricType;


public class DispatchingFunctionCounterBuilderImpl implements DispatchingFunctionCounterBuilder {
    private  String name;
    private  MetricType metricType;
    private long acceptedRequested;
    private long rejectedRequest;

    @Override
    public DispatchingFunctionCounterBuilder withName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public DispatchingFunctionCounterBuilder withAcceptRequestValue(long value) {
        this.acceptedRequested = value;
        return this;
    }
    @Override
    public DispatchingFunctionCounterBuilder withRejectRequestValue(long value) {
        this.rejectedRequest = value;
        return this;
    }
    @Override
    public DispatchingFunctionCounterBuilder withType(MetricType type) {
        this.metricType = type;
        return this;
    }

    @Override
    public DispatchingFuntionMetric build() {
        return new DispatchingFuntionMetricImpl(this.name, this.metricType, this.acceptedRequested, this.rejectedRequest);
    }
}
