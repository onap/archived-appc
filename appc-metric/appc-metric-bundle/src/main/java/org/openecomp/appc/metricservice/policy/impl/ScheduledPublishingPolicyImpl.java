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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.openecomp.appc.configuration.Configuration;
import org.openecomp.appc.configuration.ConfigurationFactory;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.metricservice.MetricRegistry;
import org.openecomp.appc.metricservice.MetricService;
import org.openecomp.appc.metricservice.Publisher;
import org.openecomp.appc.metricservice.metric.Metric;
import org.openecomp.appc.metricservice.policy.ScheduledPublishingPolicy;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;


public class ScheduledPublishingPolicyImpl implements ScheduledPublishingPolicy {
    private long startTime;
    private long period;
    private Publisher[] publishers;
    private Metric[] metrics;
    private MetricRegistry metricRegistry;
    private static final EELFLogger logger = EELFManager.getInstance().getLogger(ScheduledPublishingPolicyImpl.class);
    private ScheduledExecutorService scheduleExecutor;
    private Configuration configuration;

    public ScheduledPublishingPolicyImpl(long startTime, long period, Publisher[] publishers, Metric[] metrics) {
        this.startTime = startTime;
        this.period = period;
        this.publishers = publishers;
        this.metrics = metrics;
        this.scheduleExecutor= Executors.newSingleThreadScheduledExecutor(getThreadFactory(true));
    }

    public ScheduledPublishingPolicyImpl( Publisher[] publishers, Metric[] metrics) {
        configuration = ConfigurationFactory.getConfiguration();
        Properties properties=configuration.getProperties();
        if(properties!=null){
            if(properties.getProperty("schedule.policy.metric.period")!=null && properties.getProperty("schedule.policy.metric.start.time")!=null){
                this.startTime = getConfigStartTime(properties);
                this.period = getConfigPeriod(properties);
                logger.info("Metric Properties read from configuration Start Time :"+this.startTime+", Period :"+this.period);
            }else if(properties.getProperty("schedule.policy.metric.period")!=null){
                this.startTime=1;
                this.period=getConfigPeriod(properties);
                logger.info("Metric Properties read from configuration Start Time :"+this.startTime+", Period :"+this.period);

            }else if(properties.getProperty("schedule.policy.metric.period")==null && properties.getProperty("schedule.policy.metric.start.time")!=null){
                this.startTime=getConfigStartTime("00:00:00",properties);
                this.period=(24*60*60*1000)-1;
                logger.info("Metric Properties read from configuration Start Time :"+this.startTime+", Period :"+this.period);

            }else{
                logger.info("Metric Properties coming as null,setting to default Start Time :1 ms,Period : 100000 ms");
                this.startTime = 1;
                this.period = 100000;
                logger.info("Metric Properties read from configuration Start Time :"+this.startTime+", Period :"+this.period);

            }
        } else  {
            logger.info("Metric Properties coming as null,setting to default Start Time :1 ms,Period : 100000 ms");
            this.startTime = 1;
            this.period = 100000;
            logger.info("Metric Properties read from configuration Start Time :"+this.startTime+", Period :"+this.period);
        }
        this.publishers = publishers;
        this.metrics = metrics;
        this.scheduleExecutor= Executors.newSingleThreadScheduledExecutor(getThreadFactory(true));
    }

    private long getConfigPeriod(Properties properties) {
        String period=properties.getProperty("schedule.policy.metric.period");
        logger.info("Metric period : " +period);
        long periodInMs=Integer.parseInt(period)*1000;
        logger.info("Metric period in long : " +periodInMs);
        return periodInMs;
    }

    private long getTimeInMs(String time) {
        String[] strings=time.split(":");
        if(strings.length==3) {
            long hour = Integer.parseInt(strings[0]) * 60 * 60 * 1000;
            long min = Integer.parseInt(strings[1]) * 60 * 1000;
            long sec = Integer.parseInt(strings[2]) * 1000;
            return hour+min+sec;
        }else{
            return 0;
        }

    }



    private long getConfigStartTime(Properties properties) {
        String startTime=properties.getProperty("schedule.policy.metric.start.time");
        if(startTime!=null){
            long timeDiff=(getTimeInMs(startTime))-(getTimeInMs((new SimpleDateFormat("HH:mm:ss")).format(new Date())));
            long period=getConfigPeriod(properties);
            if(timeDiff>=0){
                return timeDiff;
            }else{
                return period-((timeDiff*-1)%period);
            }
        }
        return 0;
    }

    private long getConfigStartTime(String startTime,Properties properties) {
        if(startTime!=null){
            long timeDiff=(getTimeInMs(startTime))-(getTimeInMs((new SimpleDateFormat("HH:mm:ss")).format(new Date())));
            long period=getConfigPeriod(properties);
            if(timeDiff>=0){
                return timeDiff%period;
            }else{
                return period-((timeDiff*-1)%period);
            }
        }
        return 0;
    }
    @Override
    public void onMetricChange(Metric metric) throws APPCException {
        //TODO
    }

    @Override
    public Metric[] metrics() {
        return metrics;
    }

    @Override
    public void init() {
        Properties properties=configuration.getProperties();
        boolean isMetricEnabled=false;
        if(properties!=null){
            String metricProperty=properties.getProperty("metric.enabled");
            if(metricProperty!=null){
                isMetricEnabled=Boolean.valueOf(metricProperty);
            }
        }
        if(isMetricEnabled){
            logger.info("Metric Service is enabled, hence policies getting scheduled");
            for(final Publisher publisher:this.getPublishers()){
                scheduleExecutor.scheduleWithFixedDelay(new Runnable()
                {
                    public void run() {
                        try {
                            publisher.publish(metricRegistry, metrics);
                            reset();
                        } catch (RuntimeException ex) {
                            logger.error("RuntimeException thrown from {}#report. Exception was suppressed.", publisher.getClass().getSimpleName(), ex);
                        }
                    }
                }
                        , startTime, period, TimeUnit.MILLISECONDS);
            }
        }else{
            logger.info("Metric Service is not enabled, hence policies not getting scheduled");

        }
    }

    @Override
    public long getStartTime() {
        return this.startTime;
    }

    @Override
    public long getPeriod() {
        return this.period;
    }

    @Override
    public Publisher[] getPublishers() {
        return this.publishers;
    }

    @Override
    public void reset() {
        for(Metric metric:this.metrics){
            metric.reset();}
    }


    private  ThreadFactory getThreadFactory(final boolean isDaemon){
        return new ThreadFactory() {
            public Thread newThread(Runnable r) {
                Thread t = Executors.defaultThreadFactory().newThread(r);
                t.setDaemon(isDaemon);
                return t;
            }
        };
    }

}
