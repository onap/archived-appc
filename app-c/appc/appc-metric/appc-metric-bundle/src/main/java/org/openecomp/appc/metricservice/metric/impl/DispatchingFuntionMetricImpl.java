/*-
 * ============LICENSE_START=======================================================
 * openECOMP : APP-C
 * ================================================================================
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
 */

package org.openecomp.appc.metricservice.metric.impl;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.openecomp.appc.metricservice.metric.DispatchingFuntionMetric;
import org.openecomp.appc.metricservice.metric.MetricType;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;


public class DispatchingFuntionMetricImpl implements DispatchingFuntionMetric {
    private  String name;
    private  MetricType metricType;
    private long acceptedRequested;
    private long rejectedRequest;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd");
    private static final EELFLogger logger = EELFManager.getInstance().getLogger(DmaapRequestCounterMetricImpl.class);

    public DispatchingFuntionMetricImpl(String name, MetricType metricType, long acceptedRequested, long rejectedRequest) {
        this.name = name;
        this.metricType = metricType;
        this.acceptedRequested = acceptedRequested;
        this.rejectedRequest = rejectedRequest;
    }

    @Override
    public void incrementAcceptedRequest() {
        this.acceptedRequested+=1;
    }

    @Override
    public void incrementRejectedRequest() {
        this.rejectedRequest+=1;
    }

    @Override
    public String value() {
        logger.debug("Value is getting calculated for metric :" + this.name);
        try{
            Calendar cal = Calendar.getInstance();
            cal.setTimeZone(TimeZone.getTimeZone("UTC"));
            String date=dateFormat.format(cal.getTime());
            String value=date+"["+acceptedRequested+","+rejectedRequest+"]"+"@"+(acceptedRequested+rejectedRequest);
            logger.debug("Current value of the metric "+this.name+" :"+value);
            return value ;

        }catch (Exception e){
            logger.debug("Cant format the date.");
        }
        return  null;

    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public void reset() {
        this.acceptedRequested=0;
        this.rejectedRequest=0;
    }

    @Override
    public MetricType type() {
        return this.metricType;
    }
    @Override
    public String toString() {
        return this.value();
    }
}
