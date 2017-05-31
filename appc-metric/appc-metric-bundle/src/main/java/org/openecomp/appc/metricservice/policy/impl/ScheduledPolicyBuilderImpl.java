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

package org.openecomp.appc.metricservice.policy.impl;

import org.openecomp.appc.metricservice.Publisher;
import org.openecomp.appc.metricservice.metric.Metric;
import org.openecomp.appc.metricservice.policy.PublishingPolicy;
import org.openecomp.appc.metricservice.policy.ScheduledPolicyBuilder;


public class ScheduledPolicyBuilderImpl implements ScheduledPolicyBuilder {

    private long startTime;
    private long period;
    private Publisher[] publishers;
    private Metric[] metrics;

    @Override
    public ScheduledPolicyBuilder withStartTime(long time) {
       this.startTime=time;
        return this;
    }

    @Override
    public ScheduledPolicyBuilder withPeriod(long period) {
        this.period=period;
        return this;
    }

    @Override
    public ScheduledPolicyBuilder withPublishers(Publisher[] publishers) {
        this.publishers=publishers;
        return this;
    }

    @Override
    public ScheduledPolicyBuilder withMetrics(Metric[] metrics) {
        this.metrics=metrics;
        return this;
    }

    @Override
    public PublishingPolicy build() {
        return new ScheduledPublishingPolicyImpl(this.publishers,this.metrics);
    }
}
