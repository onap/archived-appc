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

package org.openecomp.appc.metricservice.metric.impl;

import org.openecomp.appc.metricservice.metric.MetricType;
import org.openecomp.appc.metricservice.metric.DmaapRequestCounterBuilder;
import org.openecomp.appc.metricservice.metric.DmaapRequestCounterMetric;


public class DmaapRequestCounterBuilderImpl implements DmaapRequestCounterBuilder {
    private  String name;
    private  MetricType metricType;
    private long recievedMessage;
    private long publishedMessage;

    @Override
    public DmaapRequestCounterBuilder withName(String name) {
        this.name=name;
        return this;
    }

    @Override
    public DmaapRequestCounterBuilder withRecievedMessage(long value) {

        this.recievedMessage=value;
        return this;
    }

    @Override
    public DmaapRequestCounterBuilder withPublishedMessage(long value) {
        this.publishedMessage=value;
        return this;
    }

    @Override
    public DmaapRequestCounterBuilder withType(MetricType type) {
        this.metricType=type;
        return this;
    }

    @Override
    public DmaapRequestCounterMetric build() {
        return new DmaapRequestCounterMetricImpl(this.name,this.metricType,this.recievedMessage,this.publishedMessage);
    }
}
