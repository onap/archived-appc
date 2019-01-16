/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications (C) 2019 Ericsson
 * =============================================================================
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

package org.onap.appc.provider.lcm.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.Payload;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.action.identifiers.ActionIdentifiers;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.common.header.CommonHeader;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.common.header.common.header.Flags;
import org.onap.appc.domainmodel.lcm.ActionLevel;
import org.onap.appc.domainmodel.lcm.RequestContext;
import org.onap.appc.domainmodel.lcm.VNFOperation;
import org.onap.appc.requesthandler.objects.RequestHandlerInput;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;



public class RequestInputBuilder {
    private final EELFLogger logger = EELFManager.getInstance().getApplicationLogger();

    private static final String FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    private RequestContext requestContext;
    private String rpcName;


    public RequestInputBuilder requestContext() {
        this.requestContext = new RequestContext();
        return this;
    }

    public RequestInputBuilder action(String action) {
        this.requestContext.setAction(VNFOperation.findByString(action));
        return this;
    }


    public RequestInputBuilder additionalContext(String key, String value) {
        this.requestContext.addKeyValueToAdditionalContext(key, value);
        return this;
    }

    public RequestInputBuilder payload(Payload payload) {
        if (payload != null) {
            this.requestContext.setPayload(payload.getValue());
        }
        return this;
    }

    public RequestHandlerInput  build (){
        RequestHandlerInput request = new RequestHandlerInput();
        request.setRequestContext(this.requestContext);
        request.setRpcName(rpcName);
        return  request;
    }

    public RequestInputBuilder rpcName(String rpcName) {
        this.rpcName = rpcName;
        return this;
    }

    public RequestInputBuilder commonHeader(CommonHeader commonHeader) throws ParseException {
        org.onap.appc.domainmodel.lcm.CommonHeader header = new org.onap.appc.domainmodel.lcm.CommonHeader();
        this.requestContext.setCommonHeader(header);

        try {
            if(null != commonHeader.getTimestamp()) {
                SimpleDateFormat format = new SimpleDateFormat(FORMAT);
                format.setLenient(false);
                format.setTimeZone(TimeZone.getTimeZone("UTC"));
                header.setTimestamp(format.parse(commonHeader.getTimestamp().getValue()));
            }else{
                throw new ParseException("Missing mandatory parameter : timestamp " , 0);
            }
        } catch (ParseException e) {
            logger.error(String.format("DATE format is incorrect: %s", e.getMessage()));
            throw e;
        }
        header.setApiVer(commonHeader.getApiVer());
        header.setRequestId(commonHeader.getRequestId());
        header.setOriginatorId(commonHeader.getOriginatorId());
        header.setSubRequestId(commonHeader.getSubRequestId());

        Flags inFlags = commonHeader.getFlags();
        org.onap.appc.domainmodel.lcm.Flags flags = new org.onap.appc.domainmodel.lcm.Flags();
        if (inFlags != null) {

            if(null != inFlags.getForce()) {
                flags.setForce(Boolean.parseBoolean(inFlags.getForce().toString().toLowerCase()));
            }
            if(null!=inFlags.getMode()) {
                flags.setMode(inFlags.getMode().name());
            }
            if(null!=  inFlags.getTtl()) {
                flags.setTtl(inFlags.getTtl());
            }

        }
        this.requestContext.getCommonHeader().setFlags(flags);
        return this;
    }

    public RequestInputBuilder actionIdentifiers(ActionIdentifiers actionIdentifiers)  throws ParseException  {
        if(null!= actionIdentifiers) {
            org.onap.appc.domainmodel.lcm.ActionIdentifiers actionIds = new org.onap.appc.domainmodel.lcm.ActionIdentifiers();
            actionIds.setServiceInstanceId(actionIdentifiers.getServiceInstanceId());
            actionIds.setVnfcName(actionIdentifiers.getVnfcName());
            actionIds.setvServerId(actionIdentifiers.getVserverId());
            actionIds.setVnfId(actionIdentifiers.getVnfId());
            actionIds.setVfModuleId(actionIdentifiers.getVfModuleId());
            this.requestContext.setActionIdentifiers(actionIds);

            ActionLevel actionLevel=ActionLevel.VNF;
            this.requestContext.setActionLevel(actionLevel);
            return this;
        }else{
            throw new ParseException("Missing action identifier" , 0);
        }
    }

}
