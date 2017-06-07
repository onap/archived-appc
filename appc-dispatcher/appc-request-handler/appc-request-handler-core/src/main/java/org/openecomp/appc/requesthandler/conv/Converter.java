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

package org.openecomp.appc.requesthandler.conv;

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
import com.fasterxml.jackson.databind.SerializationFeature;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.lcm.rev160108.*;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.lcm.rev160108.common.header.CommonHeader;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.lcm.rev160108.common.header.CommonHeaderBuilder;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.lcm.rev160108.common.header.common.header.Flags;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.lcm.rev160108.common.header.common.header.FlagsBuilder;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.lcm.rev160108.status.Status;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.lcm.rev160108.status.StatusBuilder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.openecomp.appc.domainmodel.lcm.ResponseContext;
import org.openecomp.appc.domainmodel.lcm.VNFOperation;
import org.openecomp.appc.requesthandler.impl.DmaapOutgoingMessage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


public class Converter {
    public static final String ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String MODE_FLAG = "MODE";
    public static final String FORCE_FLAG = "FORCE";
    public static final String TTL_FLAG = "TTL";
    public final static String DMaaP_ROOT_VALUE = "output";
    private static final SimpleDateFormat isoFormatter = new SimpleDateFormat(ISO_FORMAT);
    private static final EELFLogger logger = EELFManager.getInstance().getLogger(Converter.class);
    static {
        isoFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static Builder<?> convAsyncResponseToBuilder(VNFOperation vnfOperation, String rpcName, ResponseContext response) {
        Builder<?> outObj = null;
        if(response == null){
            throw new IllegalArgumentException("empty asyncResponse");
        }
        if(vnfOperation == null){
            throw new IllegalArgumentException("empty asyncResponse.action");
        }
        Action action = Action.valueOf(vnfOperation.name());
        CommonHeader commonHeader = convAsyncResponseTorev160108CommonHeader(response);
        Status status = convAsyncResponseTorev160108Status(response);
        Payload payload = convAsyncResponseTorev160108Payload(response);
        switch (action){
            case Rollback:
                outObj = new RollbackOutputBuilder();
                ((RollbackOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((RollbackOutputBuilder)outObj).setStatus(status);
                return outObj;
            case Snapshot:
                outObj = new SnapshotOutputBuilder();
                ((SnapshotOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((SnapshotOutputBuilder)outObj).setStatus(status);
                try {
                    ((SnapshotOutputBuilder) outObj).setSnapshotId(response.getAdditionalContext().get("output.snapshot-id"));
                } catch (NullPointerException ignored) {
                    // in case of negative response, snapshotID does not populated, so just ignore NPL
                }
                return outObj;
            case Audit:
                outObj = new AuditOutputBuilder();
                ((AuditOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((AuditOutputBuilder)outObj).setStatus(status);
                ((AuditOutputBuilder)outObj).setPayload(payload);
                return outObj;
            case HealthCheck:
                outObj = new HealthCheckOutputBuilder();
                ((HealthCheckOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((HealthCheckOutputBuilder)outObj).setStatus(status);
                return outObj;
            case LiveUpgrade:
                outObj = new LiveUpgradeOutputBuilder();
                ((LiveUpgradeOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((LiveUpgradeOutputBuilder)outObj).setStatus(status);
                return outObj;
            case Lock:
                outObj = new LockOutputBuilder();
                ((LockOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((LockOutputBuilder)outObj).setStatus(status);
                return outObj;
            case Configure:
                outObj = new ConfigureOutputBuilder();
                ((ConfigureOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((ConfigureOutputBuilder)outObj).setStatus(status);
                ((ConfigureOutputBuilder)outObj).setPayload(payload);
                return outObj;
            case ConfigModify:
                outObj = new ConfigModifyOutputBuilder();
                ((ConfigModifyOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((ConfigModifyOutputBuilder)outObj).setStatus(status);
                ((ConfigModifyOutputBuilder)outObj).setPayload(payload);
                return outObj;
            case ConfigScaleOut:
                outObj = new ConfigScaleoutOutputBuilder();
                ((ConfigScaleoutOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((ConfigScaleoutOutputBuilder)outObj).setStatus(status);
                ((ConfigScaleoutOutputBuilder)outObj).setPayload(payload);
                return outObj;
            case ConfigRestore:
                outObj = new ConfigRestoreOutputBuilder();
                ((ConfigRestoreOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((ConfigRestoreOutputBuilder)outObj).setStatus(status);
                ((ConfigRestoreOutputBuilder)outObj).setPayload(payload);
                return outObj;
            case SoftwareUpload:
                outObj = new SoftwareUploadOutputBuilder();
                ((SoftwareUploadOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((SoftwareUploadOutputBuilder)outObj).setStatus(status);
                return outObj;
            case Stop:
                outObj = new StopOutputBuilder();
                ((StopOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((StopOutputBuilder)outObj).setStatus(status);
                return outObj;
            case Sync:
                outObj = new SyncOutputBuilder();
                ((SyncOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((SyncOutputBuilder)outObj).setStatus(status);
                ((SyncOutputBuilder)outObj).setPayload(payload);
                return outObj;
            case Terminate:
                outObj = new TerminateOutputBuilder();
                ((TerminateOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((TerminateOutputBuilder)outObj).setStatus(status);
                return outObj;
            case Test:
                outObj = new TestOutputBuilder();
                ((TestOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((TestOutputBuilder)outObj).setStatus(status);
                return outObj;
            case Unlock:
                outObj = new UnlockOutputBuilder();
                ((UnlockOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((UnlockOutputBuilder)outObj).setStatus(status);
                return outObj;
            case Restart:
                outObj = new RestartOutputBuilder();
                ((RestartOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((RestartOutputBuilder)outObj).setStatus(status);
                return outObj;
            case Rebuild:
                outObj = new RebuildOutputBuilder();
                ((RebuildOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((RebuildOutputBuilder)outObj).setStatus(status);
                return outObj;
            case Migrate:
                outObj = new MigrateOutputBuilder();
                ((MigrateOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((MigrateOutputBuilder)outObj).setStatus(status);
                return outObj;
            case Evacuate:
                outObj = new EvacuateOutputBuilder();
                ((EvacuateOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((EvacuateOutputBuilder)outObj).setStatus(status);
                return outObj;
            case ConfigBackup:
                outObj = new ConfigBackupOutputBuilder();
                ((ConfigBackupOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((ConfigBackupOutputBuilder)outObj).setStatus(status);
                ((ConfigBackupOutputBuilder)outObj).setPayload(payload);
                return outObj;
            case ConfigBackupDelete:
                outObj = new ConfigBackupDeleteOutputBuilder();
                ((ConfigBackupDeleteOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((ConfigBackupDeleteOutputBuilder)outObj).setStatus(status);
                ((ConfigBackupDeleteOutputBuilder)outObj).setPayload(payload);
                return outObj;
            case ConfigExport:
                outObj = new ConfigExportOutputBuilder();
                ((ConfigExportOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((ConfigExportOutputBuilder)outObj).setStatus(status);
                return outObj;
            case Start:
                outObj = new StartOutputBuilder();
                ((StartOutputBuilder)outObj).setCommonHeader(commonHeader);
                ((StartOutputBuilder)outObj).setStatus(status);
                return outObj;
            default:
                throw new IllegalArgumentException(action+" action is not supported");
        }
    }

    public static Payload convAsyncResponseTorev160108Payload(ResponseContext inObj)  {
        Payload payload = null;
        if(inObj.getPayload() != null) {
            payload = new Payload(inObj.getPayload());
        }
        return payload;
    }

    public static String convPayloadObjectToJsonString(Object inObj) throws ParseException {
        String payloadAsString = null;
        if(inObj != null) {

                if(inObj instanceof String){
                    payloadAsString = (String)inObj;
                }else {
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        payloadAsString = objectMapper.writeValueAsString(inObj);
//                        payloadAsString = objectMapper.writeValueAsString(payloadAsString);
                    } catch (JsonProcessingException e) {
                        String errMsg = "Error serialize payload json to string";
                        throw new ParseException(errMsg + "-" + e.toString(), 0);
                    }
                }
        }
        return payloadAsString;
    }

    public static Status convAsyncResponseTorev160108Status(ResponseContext inObj) {
        StatusBuilder statusBuilder = new StatusBuilder();
        statusBuilder.setCode(inObj.getStatus().getCode());
        statusBuilder.setMessage(inObj.getStatus().getMessage());
        return statusBuilder.build();
    }

    public static CommonHeader convAsyncResponseTorev160108CommonHeader(ResponseContext inObj) {
        CommonHeader outObj = null;
        if(inObj == null){
            throw new IllegalArgumentException("empty asyncResponse");
        }

        CommonHeaderBuilder commonHeaderBuilder = new CommonHeaderBuilder();
        org.opendaylight.yang.gen.v1.org.openecomp.appc.lcm.rev160108.common.header.common.header.Flags commonHeaderFlags = null;
        if(inObj.getCommonHeader().getFlags() != null){
            commonHeaderFlags = Converter.convFlagsMapTorev160108Flags(inObj.getCommonHeader().getFlags());
            commonHeaderBuilder.setFlags(commonHeaderFlags);
        }


        commonHeaderBuilder.setApiVer(inObj.getCommonHeader().getApiVer());
        commonHeaderBuilder.setRequestId(inObj.getCommonHeader().getRequestId());
        if(inObj.getCommonHeader().getSubRequestId() != null){
            commonHeaderBuilder.setSubRequestId(inObj.getCommonHeader().getSubRequestId());
        }

        if(inObj.getCommonHeader().getOriginatorId() != null){
            commonHeaderBuilder.setOriginatorId(inObj.getCommonHeader().getOriginatorId());
        }

        if(inObj.getCommonHeader().getTimeStamp() != null){
            String zuluTimestampStr = Converter.convDateToZuluString(Date.from(inObj.getCommonHeader().getTimeStamp()));
            ZULU zuluTimestamp = new ZULU(zuluTimestampStr);
            commonHeaderBuilder.setTimestamp(zuluTimestamp);
        }
        outObj = commonHeaderBuilder.build();
        return outObj;

    }

    public static String convDateToZuluString(Date timeStamp) {
        return isoFormatter.format(timeStamp);
    }

    public static org.opendaylight.yang.gen.v1.org.openecomp.appc.lcm.rev160108.common.header.common.header.Flags
    convFlagsMapTorev160108Flags(org.openecomp.appc.domainmodel.lcm.Flags flags) {
        Flags rev160108flags = null;
        boolean anyFlag = false;
        FlagsBuilder flagsBuilder = new FlagsBuilder();
        /*
         * TODO: The below flags are related to APP-C request and should not be sent back - uncomment when response flags are introduced.
         */
        /*
        if(flags.containsKey(FORCE_FLAG)){
            org.opendaylight.yang.gen.v1.org.openecomp.appc.lcm.rev160108.common.header.common.header.Flags.Force force =
                    org.opendaylight.yang.gen.v1.org.openecomp.appc.lcm.rev160108.common.header.common.header.Flags.Force.valueOf(flags.get(FORCE_FLAG).toString());
            flagsBuilder.setForce(force);
            anyFlag = true;
        }
        if(flags.containsKey(MODE_FLAG)){
            org.opendaylight.yang.gen.v1.org.openecomp.appc.lcm.rev160108.common.header.common.header.Flags.Mode mode =
                    org.opendaylight.yang.gen.v1.org.openecomp.appc.lcm.rev160108.common.header.common.header.Flags.Mode.valueOf(flags.get(MODE_FLAG).toString());
            flagsBuilder.setMode(mode);
            anyFlag = true;
        }
        if(flags.containsKey(TTL_FLAG)){
            flagsBuilder.setTtl(Integer.valueOf(flags.get(TTL_FLAG).toString()));
            anyFlag = true;
        }
        if(anyFlag){
            rev160108flags = flagsBuilder.build();
        }
         */

        rev160108flags = flagsBuilder.build();
        return rev160108flags;
    }

    public static String convAsyncResponseToJsonStringBody(VNFOperation vnfOperation, String rpcName, ResponseContext asyncResponse) throws JsonProcessingException {
        Builder<?> builder = Converter.convAsyncResponseToBuilder(vnfOperation, rpcName, asyncResponse);
        Object message = builder.build();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.addMixInAnnotations(message.getClass(), MixInFlagsMessage.class);
        objectMapper.addMixInAnnotations(CommonHeader.class, MixInCommonHeader.class);
        objectMapper.addMixInAnnotations(Flags.class, MixIn.class);
        objectMapper.addMixInAnnotations(Status.class, MixIn.class);
        objectMapper.addMixInAnnotations(Payload.class, MixIn.class);
        objectMapper.addMixInAnnotations(ZULU.class, MixIn.class);

//				.configure(SerializationConfig.Feature.SORT_PROPERTIES_ALPHABETICALLY,true)
        ObjectWriter writer = objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL).configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY,true)
                .writer(SerializationFeature.WRAP_ROOT_VALUE).withRootName(DMaaP_ROOT_VALUE).withoutFeatures(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
        return writer.writeValueAsString(message);
    }

    public static String convAsyncResponseToDmaapOutgoingMessageJsonString(VNFOperation vnfOperation, String rpcName, ResponseContext asyncResponse) throws JsonProcessingException {
        DmaapOutgoingMessage dmaapOutgoingMessage = convAsyncResponseToDmaapOutgoingMessage(vnfOperation, rpcName, asyncResponse);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.addMixInAnnotations(dmaapOutgoingMessage.getBody().getOutput().getClass(), MixInFlagsMessage.class);
        objectMapper.addMixInAnnotations(CommonHeader.class, MixInCommonHeader.class);
        objectMapper.addMixInAnnotations(Flags.class, MixIn.class);
        objectMapper.addMixInAnnotations(Status.class, MixIn.class);
        objectMapper.addMixInAnnotations(Payload.class, MixIn.class);
        objectMapper.addMixInAnnotations(ZULU.class, MixIn.class);

//				.configure(SerializationConfig.Feature.SORT_PROPERTIES_ALPHABETICALLY,true)
        ObjectWriter writer = objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL).configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY,true).writer();
        return writer.writeValueAsString(dmaapOutgoingMessage);
    }

    public static DmaapOutgoingMessage convAsyncResponseToDmaapOutgoingMessage(VNFOperation vnfOperation, String rpcName, ResponseContext asyncResponse) throws JsonProcessingException {
        DmaapOutgoingMessage outObj = new DmaapOutgoingMessage();
        String correlationID = getCorrelationID(asyncResponse);
        outObj.setCorrelationID(correlationID);
        outObj.setType("response");
        outObj.setRpcName(rpcName);
        Builder<?> builder = Converter.convAsyncResponseToBuilder(vnfOperation, rpcName, asyncResponse);
        Object messageBody = builder.build();
        DmaapOutgoingMessage.Body body = new DmaapOutgoingMessage.Body(messageBody);
        outObj.setBody(body);
        return outObj;
    }

    private static String getCorrelationID(ResponseContext context) {
        return context.getCommonHeader().getRequestId()
                + (context.getCommonHeader().getSubRequestId() == null ?
                    "":"-" + context.getCommonHeader().getSubRequestId());
    }

    abstract class MixIn {
        @JsonIgnore
        abstract Class<? extends DataContainer> getImplementedInterface(); // to be removed during serialization

        @JsonValue
        abstract java.lang.String getValue();
    }
    abstract class MixInCommonHeader extends MixIn {
        @JsonProperty("api-ver")
        abstract java.lang.String getApiVer();
        @JsonProperty("originator-id")
        abstract java.lang.String getOriginatorId();
        @JsonProperty("request-id")
        abstract java.lang.String getRequestId();
        @JsonProperty("sub-request-id")
        abstract java.lang.String getSubRequestId();

    }
    abstract class MixInFlagsMessage extends MixIn {
        @JsonProperty("common-header")
        abstract  CommonHeader getCommonHeader();
    }
}
