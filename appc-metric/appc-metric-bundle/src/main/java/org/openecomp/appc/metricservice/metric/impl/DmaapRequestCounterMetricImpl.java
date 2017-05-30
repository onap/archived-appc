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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.openecomp.appc.metricservice.metric.MetricType;
import org.openecomp.appc.metricservice.metric.DmaapRequestCounterMetric;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;


public class DmaapRequestCounterMetricImpl implements DmaapRequestCounterMetric {

    private  String name;
    private  MetricType metricType;
    private long recievedMessage;
    private long publishedMessage;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd");
    private static final EELFLogger logger = EELFManager.getInstance().getLogger(DmaapRequestCounterMetricImpl.class);
    public DmaapRequestCounterMetricImpl(String name, MetricType metricType, long recievedMessage, long publishedMessage) {
        this.name = name;
        this.metricType = metricType;
        this.recievedMessage = recievedMessage;
        this.publishedMessage=publishedMessage;
    }

    @Override
    public void incrementRecievedMessage() {
        this.recievedMessage+=1;
    }

    @Override
    public void incrementPublishedMessage() {
        this.publishedMessage+=1;
    }

    @Override
    public String value() {
        logger.debug("Value is getting calculated for metric :" + this.name);
        try{
            Calendar cal = Calendar.getInstance();
            cal.setTimeZone(TimeZone.getTimeZone("UTC"));
            String  date=dateFormat.format(cal.getTime());
            String value=date+"["+recievedMessage+"],["+publishedMessage+"]";
            logger.debug("Current value of the metric "+this.name+" :"+value);
            return value;
        }catch (Exception e){
            logger.debug("Cant format the date.");
        }
        return null;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public void reset() {
        this.recievedMessage=0;
        this.publishedMessage=0;
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
