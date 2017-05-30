/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 						reserved.
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
import org.openecomp.appc.metricservice.metric.PrimitiveCounter;


public class DefaultPrimitiveCounter  implements PrimitiveCounter{
    private  String name;
    private  MetricType metricType;
    private  long counter;

    public DefaultPrimitiveCounter(String name, MetricType metricType, long counter) {
        this.name = name;
        this.metricType = metricType;
        this.counter = counter;
    }

    public DefaultPrimitiveCounter(String name, MetricType metricType) {
        this.counter=0;
        this.name = name;
        this.metricType = metricType;
    }

    @Override
    public void increment() {
        increment(1);
    }

    @Override
    public void increment(long value) {
        this.counter+=value;
    }

    @Override
    public void decrement() {
        decrement(1);
    }

    @Override
    public void decrement(long value) {
        this.counter-=value;
    }

    @Override
    public long value() {
        return this.counter;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public void reset() {
        this.counter=0 ;
    }

    @Override
    public String toString() {
        return "DefaultPrimitiveCounter{" +
                "name='" + name + '\'' +
                ", metricType=" + metricType +
                ", counter=" + counter +
                '}';
    }

    @Override
    public MetricType type() {
        return this.metricType;
    }
}
