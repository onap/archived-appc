/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
 * 
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.services;

import org.onap.appc.services.dmaapService.PublishRequest;

import org.junit.Assert;
import org.junit.Test;

public class PublishRequestTest {
    
    @Test
    public void testParsePublishRequest() {
        //props, partition, topic, message
        String props = "props.test";
        String partition = "testPartition";
        String topic = "testTopic";
        String message = "sameple message data";
        String data = props.length() + "." + partition.length() + "." + topic.length()
        + "." + message.length() + "." + props + partition + topic + message;
        PublishRequest publishRequest = PublishRequest.parsePublishRequest(data);
        Assert.assertTrue(props.equals(publishRequest.getProps()));
        Assert.assertTrue(partition.equals(publishRequest.getPartition()));
        Assert.assertTrue(topic.equals(publishRequest.getTopic()));
        Assert.assertTrue(message.equals(publishRequest.getMessage()));
        
    }
    
    @Test
    public void testParsePublishRequest_period_in_message() {
        //props, partition, topic, message
        String props = "props.test";
        String partition = "testPartition";
        String topic = "testTopic";
        String message = "sameple. message. dat.a";
        String data = props.length() + "." + partition.length() + "." + topic.length()
        + "." + message.length() + "." + props + partition + topic + message;
        PublishRequest publishRequest = PublishRequest.parsePublishRequest(data);
        Assert.assertTrue(props.equals(publishRequest.getProps()));
        Assert.assertTrue(partition.equals(publishRequest.getPartition()));
        Assert.assertTrue(topic.equals(publishRequest.getTopic()));
        Assert.assertTrue(message.equals(publishRequest.getMessage()));
        
    }
    
    @Test
    public void testParsePublishRequest_with_missing_value() {
        //props, partition, topic, message
        String props = "props.test";
        String partition = "testPartition";
        String topic = "";
        String message = "sameple message data";
        String data = props.length() + "." + partition.length() + "." + topic.length()
        + "." + message.length() + "." + props + partition + topic + message;
        PublishRequest publishRequest = PublishRequest.parsePublishRequest(data);
        Assert.assertTrue(props.equals(publishRequest.getProps()));
        Assert.assertTrue(partition.equals(publishRequest.getPartition()));
        Assert.assertTrue(null == publishRequest.getTopic());
        Assert.assertTrue(message.equals(publishRequest.getMessage()));
        
    }

}
