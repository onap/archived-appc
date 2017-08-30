/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.appc.simulator.client.impl;

import org.openecomp.appc.client.lcm.api.ResponseHandler;
import org.openecomp.appc.client.lcm.exceptions.AppcClientException;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileWriter;
import java.util.concurrent.atomic.AtomicInteger;

public class JsonResponseHandler implements ResponseHandler<Object> {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final EELFLogger LOG = EELFManager.getInstance().getLogger(JsonResponseHandler.class);

    private String fileName = "default";
    private static final int ACCEPT_FAMILY = 1;
    private static final int SUCCESS_FAMILY = 4;
    private static final int INTERMEDIATE_MESSAGES =5;

    private AtomicInteger messageCount =new AtomicInteger(1);

    public void setFile(String name) {
        fileName = name;
    }

    @Override
    public void onResponse(Object response) {

        String output = null;
        try {
            output = OBJECT_MAPPER.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        LOG.debug("Received response : " + output);

        int errorCode = 0;
        boolean isFinal = true;
        try {
            JsonNode code = OBJECT_MAPPER.readTree(output).findValue("status").findValue("code");
            if (code == null)
            {
                LOG.error("Status code doesn't exist. Malformed response : " + output);
                flushToErrorFile(output, isFinal);
                return;
            }
            errorCode = code.asInt();
            errorCode = errorCode / 100;
            switch (errorCode) {
                case ACCEPT_FAMILY:
                    isFinal = false; // for ACCEPT that it is not a final response
                    break;
                case INTERMEDIATE_MESSAGES:
                    isFinal = false; // for 5xx series messages are not a final response
                    break;
                default:
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        switch (errorCode) {
            case ACCEPT_FAMILY: {
                try {
                    System.out.println("== THR#" + Thread.currentThread().getId() + " Got ACCEPT on ReqID <" +
                            OBJECT_MAPPER.readTree(output).findValue("common-header").findValue("request-id").asText() + ">");
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
                // no need to report ACCEPT into output file
                break;
            }
            case SUCCESS_FAMILY:
                flushToOutputFile(output, isFinal);
                break;
            case INTERMEDIATE_MESSAGES:
                flushToMessageFile(output, isFinal);
                break;
            default:
                flushToErrorFile(output, isFinal);
        }
    }

    @Override
    public void onException(AppcClientException exception) {
        flushToErrorFile("exception: " + exception, true);
    }

    private void flushToOutputFile(String output, boolean isFinal) {
        this.flushToFile(output, ".output", isFinal);
    }
    private void flushToMessageFile(String output, boolean isFinal) {
        this.flushToFile(output, ".message" + messageCount.getAndIncrement(), isFinal);

    }

    private void flushToErrorFile(String output, boolean isFinal) {
        this.flushToFile(output, ".error", isFinal);
    }

    private void flushToFile(String output, String suffix, boolean isFinal) {
        try (FileWriter fileWriter = new FileWriter(fileName + suffix);){
            LOG.info("Output file : " + fileName + suffix);

            fileWriter.write(output);
            fileWriter.flush();
            if (isFinal){
                fileWriter.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("== THR#" +Thread.currentThread().getId()+ " Output file : " + fileName + suffix);
    }
}
