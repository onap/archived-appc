/*-
 * ============LICENSE_START=======================================================
 * openECOMP : APP-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 						reserved.
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
 */

package org.openecomp.appc.dg.common.impl;

import java.util.Map;

import org.openecomp.appc.adapter.dmaap.EventSender;
import org.openecomp.appc.adapter.dmaap.DmaapDestination;
import org.openecomp.appc.adapter.dmaap.event.EventMessage;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.sdnc.sli.SvcLogicContext;


public class EventSenderMock implements EventSender {
    EventMessage msg;
    DmaapDestination destination;

    @Override
    public boolean sendEvent(DmaapDestination destination, EventMessage msg) {
        if (destination != null && msg != null){
            this.msg = msg;
            this.destination = destination;
            return true;
        }
        else{
            return false;
        }
    }

    @Override
    public boolean sendEvent(DmaapDestination destination, Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        return false;
    }

    public EventMessage getMsg() {
        return msg;
    }

    public DmaapDestination getDestination() {
        return destination;
    }
}
