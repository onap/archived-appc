package org.onap.appc.services.dmaapService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR, reason="Error Publishing Messsage")
public class MessagingException extends RuntimeException {
    

}
