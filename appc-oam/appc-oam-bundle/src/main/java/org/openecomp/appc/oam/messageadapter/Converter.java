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

package org.openecomp.appc.oam.messageadapter;

import org.openecomp.appc.oam.AppcOam;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.oam.rev170303.*;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.oam.rev170303.common.header.CommonHeader;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.oam.rev170303.status.Status;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataContainer;


import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class Converter {
    private static final String ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final SimpleDateFormat isoFormatter = new SimpleDateFormat(ISO_FORMAT);
    private static final EELFLogger logger = EELFManager.getInstance().getLogger(Converter.class);
    static {
        isoFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }


    private static Builder<?> convAsyncResponseToBuilder1(AppcOam.RPC rpcName, CommonHeader commonHeader, Status status) {
        Builder<?> outObj = null;
        if(rpcName == null){
            throw new IllegalArgumentException("empty asyncResponse.rpcName");
        }
        if(commonHeader == null){
            throw new IllegalArgumentException("empty asyncResponse.commonHeader");
        }
        if(status == null){
            throw new IllegalArgumentException("empty asyncResponse.status");
        }
        switch (rpcName){
            case stop:
                outObj = new StopOutputBuilder();
                ((StopOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((StopOutputBuilder)outObj).setStatus(status);
                return outObj;

            case start:
                outObj = new StartOutputBuilder();
                ((StartOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((StartOutputBuilder)outObj).setStatus(status);
                return outObj;
            default:
                throw new IllegalArgumentException(rpcName+" action is not supported");
        }
    }

    public static String convAsyncResponseToUebOutgoingMessageJsonString(OAMContext oamContext) throws JsonProcessingException {
        AppcOam.RPC rpcName = oamContext.getRpcName();
        CommonHeader commonHeader = oamContext.getCommonHeader();
        Status status = oamContext.getStatus();

        DmaapOutgoingMessage dmaapOutgoingMessage = convAsyncResponseToUebOutgoingMessage(rpcName,commonHeader,status);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.addMixInAnnotations(dmaapOutgoingMessage.getBody().getOutput().getClass(), MixInFlagsMessage.class);
        objectMapper.addMixInAnnotations(Status.class, MixIn.class);
        objectMapper.addMixInAnnotations(CommonHeader.class, MixInCommonHeader.class);
        ObjectWriter writer = objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL).configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY,true).writer();
        return writer.writeValueAsString(dmaapOutgoingMessage);
    }

    private static DmaapOutgoingMessage convAsyncResponseToUebOutgoingMessage(AppcOam.RPC rpcName, CommonHeader commonHeader, Status status) throws JsonProcessingException {
        DmaapOutgoingMessage outObj = new DmaapOutgoingMessage();
        String correlationID = commonHeader.getRequestId();
        outObj.setCorrelationID(correlationID);
        outObj.setType("response");
        outObj.setRpcName(rpcName.name());
        Builder<?> builder = Converter.convAsyncResponseToBuilder1(rpcName,commonHeader,status);
        Object messageBody = builder.build();

        DmaapOutgoingMessage.Body body = new DmaapOutgoingMessage.Body(messageBody);
        outObj.setBody(body);
        return outObj;
    }


    abstract class MixIn {
        @JsonIgnore
        abstract Class<? extends DataContainer> getImplementedInterface(); // to be removed during serialization

        @JsonValue
        abstract java.lang.String getValue();
    }
    abstract class MixInCommonHeader extends MixIn {
        @JsonProperty("request-id")
        abstract java.lang.String getRequestId();

        @JsonProperty("originator-id")
        abstract java.lang.String getOriginatorId();

    }
    abstract class MixInFlagsMessage extends MixIn {
        @JsonProperty("common-header")
        abstract  CommonHeader getCommonHeader();
    }



}
