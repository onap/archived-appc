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

package org.openecomp.appc.provider.lcm.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.opendaylight.yang.gen.v1.org.openecomp.appc.lcm.rev160108.Payload;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.lcm.rev160108.action.identifiers.ActionIdentifiers;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.lcm.rev160108.common.header.CommonHeader;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.lcm.rev160108.common.header.common.header.Flags;
import org.openecomp.appc.domainmodel.lcm.Flags.Mode;
import org.openecomp.appc.domainmodel.lcm.RequestContext;
import org.openecomp.appc.domainmodel.lcm.VNFOperation;
import org.openecomp.appc.requesthandler.objects.RequestHandlerInput;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;



public class RequestInputBuilder {
    private static EELFLogger logger = EELFManager.getInstance().getApplicationLogger();

    private static final String FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    private RequestContext requestContext;
    private String rpcName;

    public RequestInputBuilder() {
    }


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
        org.openecomp.appc.domainmodel.lcm.CommonHeader header = new org.openecomp.appc.domainmodel.lcm.CommonHeader();
        this.requestContext.setCommonHeader(header);

        try {
            if(null != commonHeader.getTimestamp()) {
                SimpleDateFormat format = new SimpleDateFormat(FORMAT);
                format.setLenient(false);
                format.setTimeZone(TimeZone.getTimeZone("UTC"));
                header.setTimestamp(format.parse(commonHeader.getTimestamp().getValue()).toInstant());
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
        boolean force = false;
        Mode mode = null;
        int ttl = 0;
        if (inFlags != null) {

            if (null != inFlags.getForce()) {
                force = Boolean.parseBoolean(inFlags.getForce().toString().toLowerCase());
            }
            if (null != inFlags.getMode()) {
                mode = Mode.valueOf(inFlags.getMode().name());
            }
            if (null != inFlags.getTtl()) {
                ttl = inFlags.getTtl();
            }

        }
        this.requestContext.getCommonHeader().setFlags(new org.openecomp.appc.domainmodel.lcm.Flags(mode, force, ttl));
        return this;
    }

    public RequestInputBuilder actionIdentifiers(ActionIdentifiers actionIdentifiers)  throws ParseException  {
        if(null!= actionIdentifiers) {
            org.openecomp.appc.domainmodel.lcm.ActionIdentifiers actionIds = new org.openecomp.appc.domainmodel.lcm.ActionIdentifiers();
            actionIds.setServiceInstanceId(actionIdentifiers.getServiceInstanceId());
            actionIds.setVnfcName(actionIdentifiers.getVnfcName());
            actionIds.setvServerId(actionIdentifiers.getVserverId());
            actionIds.setVnfId(actionIdentifiers.getVnfId());
            this.requestContext.setActionIdentifiers(actionIds);
            return this;
        }else{
            throw new ParseException("Missing action identifier" , 0);
        }
    }


}
