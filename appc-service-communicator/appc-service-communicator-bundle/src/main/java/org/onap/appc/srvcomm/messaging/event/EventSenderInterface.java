package org.onap.appc.srvcomm.messaging.event;

import java.util.Map;

import org.onap.appc.exceptions.APPCException;
import org.onap.appc.srvcomm.messaging.MessageDestination;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;

public interface EventSenderInterface extends SvcLogicJavaPlugin {
    boolean sendEvent(MessageDestination destination, EventMessage msg);
    boolean sendEvent(MessageDestination destination, EventMessage msg,String eventTopicName);
    boolean sendEvent(MessageDestination destination, Map<String, String> params, SvcLogicContext ctx) throws APPCException;
}
