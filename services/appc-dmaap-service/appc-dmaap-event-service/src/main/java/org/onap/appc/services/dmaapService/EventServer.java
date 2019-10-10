
package org.onap.appc.services.dmaapService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EventServer {
    
    @Autowired
    private PublishService publishService;
    
    @RequestMapping("/publish")
    public PublishResponse publish(@RequestBody String body) {
        PublishRequest req = PublishRequest.parsePublishRequest(body);
        String result = publishService.publishMessage(req.getProps(),req.getPartition(),
                req.getTopic(), req.getMessage());
        if(result.equals("Success")) {
            return new PublishResponse("response");
        }
        throw new MessagingException();
    }
    
}
