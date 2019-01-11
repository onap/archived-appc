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

package org.onap.appc.metricservice;
import org.junit.Assert;
import org.junit.Test;
import org.onap.appc.metricservice.impl.MetricServiceImpl;
import org.onap.appc.metricservice.metric.MetricType;
import org.onap.appc.metricservice.metric.PrimitiveCounter;
import org.onap.appc.metricservice.metric.DmaapRequestCounterMetric;
import org.onap.appc.metricservice.metric.DispatchingFuntionMetric;
import org.onap.appc.metricservice.metric.impl.DefaultPrimitiveCounter;
import org.onap.appc.metricservice.metric.impl.DmaapRequestCounterMetricImpl;
import org.onap.appc.metricservice.metric.impl.DispatchingFuntionMetricImpl;
import org.onap.appc.metricservice.metric.impl.DispatchingFunctionCounterBuilderImpl;
import org.onap.appc.metricservice.metric.impl.DmaapRequestCounterBuilderImpl;
import org.onap.appc.metricservice.metric.impl.PrimitiveCounterBuilderImpl;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;


public class TestMetricServiceImpl {
    @Test
    public void createRegistryTest() {
        MetricServiceImpl metricServiceImpl = new MetricServiceImpl();
        metricServiceImpl.createRegistry("anyName");
        MetricRegistry metricRegistry = metricServiceImpl.registry("anyName");
        Assert.assertNotNull(metricRegistry);
        Assert.assertTrue(metricServiceImpl.getAllRegistry().keySet().contains("anyName"));
    }

    @Test
    public void testDefaultPrimitiveCounter(){
        DefaultPrimitiveCounter df= new DefaultPrimitiveCounter("TEST", MetricType.COUNTER);
        df.increment();
        Assert.assertEquals(1, df.value());
        df.increment(25);
        Assert.assertEquals(2, df.value());
        df.decrement();
        Assert.assertEquals(1, df.value());
        Assert.assertNotNull(df.getLastModified());
        Assert.assertEquals("TEST", df.name());
        Assert.assertEquals(MetricType.COUNTER, df.type());
        df.reset();
        Assert.assertEquals(0, df.value());
        Assert.assertNotNull(df.getMetricsOutput());

    }

    @Test
    public void testDefaultPrimitiveCounterWithThreeArgsConstructor(){
        DefaultPrimitiveCounter obj= new DefaultPrimitiveCounter("TEST", MetricType.COUNTER,3);
        obj.increment();
        Assert.assertEquals(4, obj.value());
        obj.increment(25);
        Assert.assertEquals(5, obj.value());
        obj.decrement();
        Assert.assertEquals(4, obj.value());
        Assert.assertNotNull(obj.getLastModified());
        Assert.assertEquals("TEST", obj.name());
        Assert.assertEquals(MetricType.COUNTER, obj.type());
        obj.reset();
        Assert.assertEquals(0, obj.value());
        Assert.assertNotNull(obj.getMetricsOutput());
    }

    @Test
    public void testDmaapRequestCounterMetricImpl() {

        DmaapRequestCounterMetricImpl obj =new DmaapRequestCounterMetricImpl("TEST", MetricType.COUNTER,7,1);
        String date = getCurrentDate();

        obj.incrementPublishedMessage();
        obj.incrementRecievedMessage();
        Assert.assertEquals(2, Integer.parseInt(obj.getMetricsOutput().get("Total Published messages")));
        Assert.assertEquals(8, Integer.parseInt(obj.getMetricsOutput().get("Total Received messages")));
        Assert.assertEquals(date + "[8],[2]", obj.value());
        Assert.assertNotNull(obj.getLastModified());
        Assert.assertEquals("TEST", obj.name());
        Assert.assertEquals(MetricType.COUNTER, obj.type());
        obj.reset();
        Assert.assertEquals(0, Integer.parseInt(obj.getMetricsOutput().get("Total Published messages")));
        Assert.assertEquals(0, Integer.parseInt(obj.getMetricsOutput().get("Total Received messages")));

    }

    @Test
    public void testDispatchingFuntionMetricImpl() {

        DispatchingFuntionMetricImpl obj= new DispatchingFuntionMetricImpl("TEST", MetricType.COUNTER,7,1);
        String date = getCurrentDate();

        obj.incrementAcceptedRequest();
        obj.incrementRejectedRequest();
        Assert.assertEquals(10, Integer.parseInt(obj.getMetricsOutput().get("Total Received messages")));
        Assert.assertEquals(2, Integer.parseInt(obj.getMetricsOutput().get("Total Rejected messages")));
        Assert.assertEquals(date + "[8,2]@10", obj.value());
        Assert.assertNotNull(obj.getLastModified());
        Assert.assertEquals("TEST", obj.name());
        Assert.assertEquals(MetricType.COUNTER, obj.type());
        obj.reset();
        Assert.assertEquals(0, Integer.parseInt(obj.getMetricsOutput().get("Total Received messages")));
        Assert.assertEquals(0, Integer.parseInt(obj.getMetricsOutput().get("Total Rejected messages")));


    }

    @Test
    public void testDispatchingFunctionCounterBuilderImpl(){
        DispatchingFunctionCounterBuilderImpl obj = new DispatchingFunctionCounterBuilderImpl();
        String date = getCurrentDate();
        DispatchingFuntionMetric metric = obj.withName("TEST").withType(MetricType.COUNTER).withAcceptRequestValue(7).withRejectRequestValue(2).build();
        metric.incrementAcceptedRequest();
        metric.incrementRejectedRequest();
        Assert.assertEquals(date+"[8,3]@11", metric.value());
    }

    @Test
    public void testDmaapRequestCounterBuilderImpl(){
        DmaapRequestCounterBuilderImpl obj = new DmaapRequestCounterBuilderImpl();
        DmaapRequestCounterMetric metric = obj.withName("TEST").withPublishedMessage(1).withRecievedMessage(21).withType(MetricType.COUNTER).build();
        metric.incrementPublishedMessage();
        metric.incrementRecievedMessage();
        Assert.assertEquals(2, Integer.parseInt(metric.getMetricsOutput().get("Total Published messages")));
        Assert.assertEquals(22, Integer.parseInt(metric.getMetricsOutput().get("Total Received messages")));
    }

    @Test
    public void testPrimitiveCounterBuilderImpl(){
        PrimitiveCounterBuilderImpl obj = new PrimitiveCounterBuilderImpl();
        PrimitiveCounter counter = obj.withName("TEST").withType(MetricType.COUNTER).withValue(1).build();
        counter.increment();
        Assert.assertEquals(2, counter.value());
        counter.decrement();
        Assert.assertEquals(1, counter.value());
    }

    private String getCurrentDate() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd");
        return dateFormat.format(cal.getTime());

    }

}
