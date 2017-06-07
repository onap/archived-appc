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

package org.openecomp.appc.adapter.messaging.dmaap;

import java.io.*;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.att.nsa.mr.client.MRConsumer;
import org.json.JSONObject;
import org.openecomp.appc.adapter.messaging.dmaap.impl.DmaapUtil;

import com.att.nsa.mr.client.MRBatchingPublisher;
import com.att.nsa.mr.client.MRClientFactory;
import com.att.nsa.mr.client.MRPublisher.message;


/**
 *An example of how to use the Java publisher.
 */
public class SimpleExamplePublisher
{

    public static void main(String []args) throws InterruptedException, Exception{
        int msgCount = 1;
        SimpleExamplePublisher publisher = new SimpleExamplePublisher();

        int i=0;

        String topicProducerPropFileName = DmaapUtil.createProducerPropFile("org.openecomp.appc.UNIT-TEST", null);
        while (i< msgCount)
        {
            publisher.publishMessage(topicProducerPropFileName,i);
            i++;
        }

        fetchMessage();
    }


    public void publishMessage( String producerFilePath,int count  ) throws IOException, InterruptedException, Exception
    {
        // create our publisher
        final MRBatchingPublisher pub = MRClientFactory.createBatchingPublisher (producerFilePath);
        // publish some messages
        final JSONObject msg1 = new JSONObject ();
        msg1.put ( "Partition:2", "Message:" +count);
        //msg1.put ( "greeting", "Hello  .." );

        pub.send ( "2", msg1.toString());
        // close the publisher to make sure everything's sent before exiting. The batching
        // publisher interface allows the app to get the set of unsent messages. It could
        // write them to disk, for example, to try to send them later.
        final List<message> stuck = pub.close ( 20, TimeUnit.SECONDS );
        if ( stuck.size () > 0 )
        {
            System.err.println ( stuck.size() + " messages unsent" );
        }
        else
        {
            System.out.println ( "Clean exit; all messages sent." );
        }
    }


    public static void fetchMessage()
    {
        int count = 0;

        try
        {
            String topic = "org.openecomp.appc.UNIT-TEST";
            Properties props = new Properties();
            props.put("id", "1");
            props.put("group", "group1");
            String topicConsumerPropFileName1 = DmaapUtil.createConsumerPropFile(topic,props);
            final MRConsumer consumer1 = MRClientFactory.createConsumer ( topicConsumerPropFileName1);

            props = new Properties();
            props.put("id", "2");
            props.put("group", "group2");
            String topicConsumerPropFileName2 = DmaapUtil.createConsumerPropFile(topic,props);
            final MRConsumer consumer2 = MRClientFactory.createConsumer ( topicConsumerPropFileName2);

            for ( String msg : consumer1.fetch () )
            {
                count++;
                System.out.println ( "consumer1 "+count + ": " + msg );
            }
            for ( String msg : consumer2.fetch () )
            {
                count++;
                System.out.println ( "consumer1 "+count + ": " + msg );
            }


        }
        catch ( Exception x )
        {
            System.out.println("inside cons exc");
            System.err.println ( x.getClass().getName () + ": " + x.getMessage () );
        }
    }
}









