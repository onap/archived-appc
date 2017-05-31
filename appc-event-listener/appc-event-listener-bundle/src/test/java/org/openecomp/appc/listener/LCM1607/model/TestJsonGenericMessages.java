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

package org.openecomp.appc.listener.LCM1607.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.openecomp.appc.listener.util.Mapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class TestJsonGenericMessages {/*

    @Test
    public void serializeIncomingMessage() {

        final String expectedJson = "{\"CommonHeader\":{\"TimeStamp\":\"2016-05-02 19:50:37.09\",\"TransactionID\":\"1\",\"APIver\":\"1.01\",\"RequestTrack\":[\"1\",\"4\",\"12\",\"3\"],\"Flags\":null,\"SubrequestID\":null,\"OriginatorID\":\"2\"},\"Payload\":\"{ \\\"command\\\": \\\"start\\\", \\\"target-id\\\": \\\"111\\\", \\\"flag10\\\": {\\\"object-1\\\": {\\\"key-1\\\": \\\"key\\\", \\\"value-1\\\": \\\"value\\\" }} }\",\"Action\":\"CONFIGURE\",\"ObjectID\":\"200\",\"TargetID\":\"100\"}";
        InputBody msg = createIncomingMessage();

        String json = Mapper.toJsonObject(msg).toString();
        //System.out.println(json);
        Assert.assertEquals(expectedJson, json);
    }

    @Test
    public void deserializeIncomingMessage() throws IOException {
        final String originalJson = "{\"CommonHeader\":{\"TimeStamp\":\"2016-05-02 19:50:37.09\",\"TransactionID\":\"1\",\"Flags\":{\"FORCE\":\"Y\",\"TTL\":\"12\"},\"SubrequestID\":\"2345\",\"OriginatorID\":\"2\",\"APIver\":\"1.01\"},  \"Payload\": \" \\\"Graceful\\\" : \\\"Yes\\\" \",\"Action\":\"CONFIGURE\",\"ObjectID\":\"200\",\"TargetID\":\"100\"}";

        ObjectMapper mapper = new ObjectMapper();
        InputBody msg = mapper.readValue(originalJson, InputBody.class);

        Assert.assertNotNull(msg);
        Assert.assertEquals("2016-05-02 19:50:37.09", msg.getCommonHeader().getTimeStamp());
        Assert.assertEquals("1", msg.getCommonHeader().getRequestID());
        Assert.assertEquals("1.01", msg.getCommonHeader().getApiVer());
        Assert.assertEquals("200", msg.getObjectId());
        Assert.assertEquals("100", msg.getTargetId());
        Assert.assertEquals(" \"Graceful\" : \"Yes\" ", msg.getPayload());
        Assert.assertEquals("CONFIGURE", msg.getAction());

    }

    @Test
    public void serializeResponseMessage() {
        InputBody imsg = createIncomingMessage();
        OutputBody omsg = new OutputBody(imsg);
        omsg.setStatus(new ResponseStatus("200", "OK"));

        String json = Mapper.toJsonObject(omsg).toString();
        System.out.println(json);
        //Assert.assertEquals(expectedJson, json);
        Assert.assertNotEquals("", json);

    }

    private InputBody createIncomingMessage() {
        InputBody msg = new InputBody();
        CommonHeader rh = new CommonHeader();
        rh.setTimeStamp("2016-05-02 19:50:37.09");
        rh.setApiVer("1.01");
        rh.setRequestID("1");
        rh.setOriginatorId("2");


        Map<String, String> flags = new HashMap<>();
        flags.put("FORCE", "Y");
        flags.put("TTL", "12");

        msg.setCommonHeader(rh);
        msg.setAction("CONFIGURE");
        msg.setTargetId("100");
        msg.setObjectId("200");
        msg.setPayloadAsString("{ \"command\": \"start\", \"target-id\": \"111\", \"flag10\": {\"object-1\": {\"key-1\": \"key\", \"value-1\": \"value\" }} }");
        return msg;
    }
*/
}
