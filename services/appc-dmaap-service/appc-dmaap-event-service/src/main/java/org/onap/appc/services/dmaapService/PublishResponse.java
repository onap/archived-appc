package org.onap.appc.services.dmaapService;

public class PublishResponse {

    private String status;
    
    public PublishResponse(String status) {
        this.status = status;
    }
    
    public String getName() {
        return status;
    }
    
}
