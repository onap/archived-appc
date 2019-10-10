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

package org.onap.appc.services.dmaapService;

public class PublishRequest {
    private String props;
    private String partition;
    private String message;
    private String topic;
    
    private PublishRequest(String props, String partition, String topic, String message) {
        this.props = props;
        this.partition = partition;
        this.message = message;
        this.topic = topic;
    }
    
    public String getProps() {
        return props;
    }


    public String getPartition() {
        return partition;
    }


    public String getMessage() {
        return message;
    }
    
    public String getTopic() {
        return topic;
    }

    public static PublishRequest parsePublishRequest(String data) {
        //body content: props, partition, topic, message
        String[] bodyParameters = new String[4];
        int[] bodyParameterSizes = new int[bodyParameters.length];
        for(int i = 0; i < bodyParameters.length; i ++) {
            String[] split = data.split("\\.", 2);
            try {
                bodyParameterSizes[i] = Integer.parseInt(split[0]);
            } catch(NumberFormatException e) {
                
            }
            data = split[1];
        }
        int cursor = 0;
        for(int i = 0; i < bodyParameters.length; i ++) {
            if(bodyParameterSizes[i] > 0) {
                bodyParameters[i] = data.substring(cursor, cursor + bodyParameterSizes[i]);
                cursor = cursor + bodyParameterSizes[i];
            }
        }
        return new PublishRequest(bodyParameters[0], bodyParameters[1], bodyParameters[2], bodyParameters[3]);
        
    }
    

}
