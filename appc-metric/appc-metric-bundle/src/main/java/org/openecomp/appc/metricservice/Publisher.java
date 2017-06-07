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

package org.openecomp.appc.metricservice;

import org.openecomp.appc.metricservice.metric.Metric;

/**
 * Low-level logic of exposing metric values in certain way. There might be plenty of options such as logging, JMX, etc.
 * API to be supported by any logic that is going to be plugged into metric service.
 * The publisher is considered as low-level technical unit which, potentially, can be reused to expose metrics from different registries
 *
 */
public interface Publisher {
   void  publish(MetricRegistry metricRegistry,Metric[] metrics);
}
