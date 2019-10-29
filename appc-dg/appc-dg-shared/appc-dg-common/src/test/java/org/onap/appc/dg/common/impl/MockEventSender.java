package org.onap.appc.dg.common.impl;

import org.onap.appc.srvcomm.messaging.MessageDestination;
import org.onap.appc.srvcomm.messaging.event.EventMessage;
import org.onap.appc.srvcomm.messaging.event.EventSender;

public class MockEventSender extends EventSender {
    
    private EventMessage eventMessage;
    private String topic;
    private MessageDestination dest;
    private boolean success = true;
    
    @Override
    public boolean sendEvent(MessageDestination destination, EventMessage msg, String eventTopicName) {
        eventMessage = msg;
        topic = eventTopicName;
        dest = destination;
        return success;
    }
    
    @Override
    public boolean sendEvent(MessageDestination destination, EventMessage msg) {
        eventMessage = msg;
        topic = null;
        dest = destination;
        return success;
    }
    public void reset() {
        eventMessage = null;
        topic = null;
        dest = null;
        success = true;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public EventMessage getMessage() {
        return eventMessage;
    }
    
    public String getTopic() {
        return topic;
    }
    
    public MessageDestination getDestination() {
        return dest;
    }

}
