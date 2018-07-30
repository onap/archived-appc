/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
 * Modifications Copyright (C) 2018 IBM
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

package org.onap.appc.design.dbervices;

import static org.onap.appc.design.services.util.DesignServiceConstants.ACTION;
import static org.onap.appc.design.services.util.DesignServiceConstants.ACTION_LEVEL;
import static org.onap.appc.design.services.util.DesignServiceConstants.ARTIFACT_CONTENTS;
import static org.onap.appc.design.services.util.DesignServiceConstants.ARTIFACT_NAME;
import static org.onap.appc.design.services.util.DesignServiceConstants.ARTIFACT_TYPE;
import static org.onap.appc.design.services.util.DesignServiceConstants.ARTIFACT_VERSOIN;
import static org.onap.appc.design.services.util.DesignServiceConstants.GETARTIFACT;
import static org.onap.appc.design.services.util.DesignServiceConstants.GETAPPCTIMESTAMPUTC;
import static org.onap.appc.design.services.util.DesignServiceConstants.GETDESIGNS;
import static org.onap.appc.design.services.util.DesignServiceConstants.GETSTATUS;
import static org.onap.appc.design.services.util.DesignServiceConstants.PROTOCOL;
import static org.onap.appc.design.services.util.DesignServiceConstants.SETINCART;
import static org.onap.appc.design.services.util.DesignServiceConstants.SETPROTOCOLREFERENCE;
import static org.onap.appc.design.services.util.DesignServiceConstants.SETSTATUS;
import static org.onap.appc.design.services.util.DesignServiceConstants.STATUS;
import static org.onap.appc.design.services.util.DesignServiceConstants.UPLOADARTIFACT;
import static org.onap.appc.design.services.util.DesignServiceConstants.USER_ID;
import static org.onap.appc.design.services.util.DesignServiceConstants.VNF_TYPE;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

public class RequestValidator {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(RequestValidator.class);

    public RequestValidator() {
    }

    public static void validate(String action, String payload) throws RequestValidationException, IOException {
        log.info("validate: action:"  +  action );
        log.info("validate: payload:" + payload);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode payloadObject = objectMapper.readTree(payload);
        log.info("payloadObject:" + payloadObject.get(ARTIFACT_CONTENTS));

        String errorString= null;
        switch (action) {
            case GETDESIGNS:
                errorString = resolveGetDesignsErrorString(payloadObject);
                break;
            case GETAPPCTIMESTAMPUTC:
                log.info("validate: No payload validation needed for Timestamp.");
                break;
            case GETARTIFACT:
                errorString = resolveGetArtifactErrorString(payloadObject);
                break;
            case GETSTATUS:
                errorString = resolveGetStatusErrorString(payloadObject);
                break;
            case SETSTATUS:
                errorString = resolveSetStatusErrorString(payloadObject);
                break;
            case UPLOADARTIFACT:
                errorString = resolveUploadArtifactErrorString(payloadObject);
                break;
            case SETPROTOCOLREFERENCE:
            case SETINCART:
                errorString = resolveErrorString(payloadObject);
                break;
            default:
                throw new RequestValidationException(" Action " + action + " not found while processing request ");
        }
        checkForErrorString(errorString);
    }

    private static void checkForErrorString(String errorString) throws RequestValidationException {
        if (errorString != null) {
            throw new RequestValidationException(" Missing input parameter :-" + errorString + " -:");
        }
    }

    private static String resolveGetDesignsErrorString(JsonNode payloadObject) {
        return nullOrEmpty(payloadObject, USER_ID) ? USER_ID : null;
    }

    private static String resolveErrorString(JsonNode payloadObject) {
        if (nullOrEmpty(payloadObject, ACTION)) {
            return ACTION;
        } else if (nullOrEmpty(payloadObject, ACTION_LEVEL)) {
            return ACTION_LEVEL;
        } else if (nullOrEmpty(payloadObject, VNF_TYPE)) {
            return VNF_TYPE;
        } else if (nullOrEmpty(payloadObject, PROTOCOL)) {
            return PROTOCOL;
        }
        return null;
    }

    private static String resolveUploadArtifactErrorString(JsonNode payloadObject) {
        if (nullOrEmpty(payloadObject, ARTIFACT_NAME)) {
            return ARTIFACT_NAME;
        } else if (doesNotContainReference(payloadObject)) {
            return ACTION;
        } else if (nullOrEmpty(payloadObject, ARTIFACT_VERSOIN)) {
            return ARTIFACT_VERSOIN;
        } else if (payloadObject.get(ARTIFACT_CONTENTS) == null) {
            return ARTIFACT_CONTENTS;
        } else if (nullOrEmpty(payloadObject, ARTIFACT_TYPE)) {
            return ARTIFACT_TYPE;
        } else if (nullOrEmpty(payloadObject, VNF_TYPE)) {
            return VNF_TYPE;
        }
        return null;
    }

    private static boolean doesNotContainReference(JsonNode payloadObject) {
        return
            !payloadObject.get(ARTIFACT_NAME).textValue().contains("reference")
                && nullOrEmpty(payloadObject, ARTIFACT_NAME);
    }

    private static String resolveSetStatusErrorString(JsonNode payloadObject) {
        if (nullOrEmpty(payloadObject, USER_ID)) {
            return USER_ID;
        } else if (nullOrEmpty(payloadObject, VNF_TYPE)) {
            return VNF_TYPE;
        } else if (nullOrEmpty(payloadObject, ACTION)) {
            return ACTION;
        } else if (nullOrEmpty(payloadObject, ARTIFACT_TYPE)) {
            return ARTIFACT_TYPE;
        } else if (nullOrEmpty(payloadObject, STATUS)) {
            return STATUS;
        }
        return null;
    }

    private static String resolveGetStatusErrorString(JsonNode payloadObject) {
        if (nullOrEmpty(payloadObject, USER_ID)) {
            return USER_ID;
        } else if (nullOrEmpty(payloadObject, VNF_TYPE)) {
            return VNF_TYPE;
        }
        return null;
    }

    private static String resolveGetArtifactErrorString(JsonNode payloadObject) {
        if (nullOrEmpty(payloadObject, VNF_TYPE)) {
            return VNF_TYPE;
        } else if (nullOrEmpty(payloadObject, ARTIFACT_TYPE)) {
            return ARTIFACT_TYPE;
        } else if (nullOrEmpty(payloadObject, ARTIFACT_NAME)) {
            return ARTIFACT_NAME;
        }
        return null;
    }

    private static boolean nullOrEmpty(JsonNode payloadObject, String param) {
        return payloadObject.get(param) == null || payloadObject
            .get(param).textValue().isEmpty();
    }

}


