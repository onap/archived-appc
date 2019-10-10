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

import org.onap.appc.adapter.messaging.dmaap.http.HttpDmaapConsumerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

@RestController
public class EventServer {
    
    private static final EELFLogger LOG = EELFManager.getInstance().getLogger(EventServer.class);

    @Autowired
    private PublishService publishService;

    @RequestMapping("/publish")
    public PublishResponse publish(@RequestBody String body) {
        PublishRequest req = PublishRequest.parsePublishRequest(body);
        String result = publishService.publishMessage(req.getProps(),req.getPartition(),
                req.getTopic(), req.getMessage());
        if(result.equals("Success")) {
            return new PublishResponse(result);
        }
        LOG.error("Error during message publish: " + result);
        throw new MessagingException();
    }

}
