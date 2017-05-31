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

package org.openecomp.appc.metricservice.publisher;

import org.openecomp.appc.metricservice.MetricRegistry;
import org.openecomp.appc.metricservice.Publisher;
import org.openecomp.appc.metricservice.metric.Metric;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;


public class LogPublisher implements Publisher {
    private static final EELFLogger logger = EELFManager.getInstance().getLogger(LogPublisher.class);
    private MetricRegistry metricRegistry;
    private Metric[] metrics;

    public LogPublisher(MetricRegistry metricRegistry, Metric[] metrics) {
        this.metricRegistry = metricRegistry;
        this.metrics = metrics;
    }

    @Override
    public void publish(MetricRegistry metricRegistry, Metric[] metrics) {
        for(Metric metric:metrics){
            logger.debug("LOG PUBLISHER:"+metric.name()+":"+metric.toString());
        }
    }


}
