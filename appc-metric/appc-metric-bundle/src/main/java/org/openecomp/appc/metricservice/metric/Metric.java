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

package org.openecomp.appc.metricservice.metric;

import java.util.HashMap;

/**
 *
 * a measure of system parameter at the current moment. Each metric is identified by name.
 * In general case, a metric just reflects its (almost) real-time value and is not responsible for maintaining its historical data.
 * One that needs to build series of a metric values for statistical/analytic purposes should query the value and store it for further processing.
 * Metrics can be of different types - counters, timers etc.
 * The initial service implementation supports simple (flat) counters only.
 *
 */
public interface Metric {
    String name();
    void reset();
    MetricType type();
    /**
     * This API will be used to get all the running Metrics Output.
     * @return HashMap <String,String> in which
     * the First String(Key) will be the name of the KPI property
     * and another String(Value of the Key) will be the Value of
     * that property for that KPI
     */
    HashMap<String,String> getMetricsOutput();
    /**
     * Return last modified date for  KPI in string format
     * @return - last modified date for KPI
     */
    String getLastModified();
}
