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

package org.onap.appc.design.dbervices;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.onap.appc.design.services.util.DesignServiceConstants;

public class RequestValidator {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(RequestValidator.class);

    private RequestValidator() {}

    public static void validate(String action, String payload) throws RequestValidationException, IOException {
        log.info("payload" + payload);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode payloadObject = objectMapper.readTree(payload);
        log.info("payloadObject" + payloadObject.get(DesignServiceConstants.ARTIFACT_CONTENTS));

        String errorString;
        switch (action) {
            case DesignServiceConstants.GETDESIGNS:
                errorString = resolveGetDesignsErrorString(payloadObject);
                break;
            case DesignServiceConstants.GETARTIFACT:
                errorString = resolveGetArtifactErrorString(payloadObject);
                break;
            case DesignServiceConstants.GETSTATUS:
                errorString = resolveGetStatusErrorString(payloadObject);
                break;
            case DesignServiceConstants.SETSTATUS:
                errorString = resolveSetStatusErrorString(payloadObject);
                break;
            case DesignServiceConstants.UPLOADARTIFACT:
                errorString = resolveUploadArtifactErrorString(payloadObject);
                break;
            case DesignServiceConstants.SETPROTOCOLREFERENCE:
            case DesignServiceConstants.SETINCART:
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
        return nullOrEmptyUserId(payloadObject) ? DesignServiceConstants.USER_ID : null;
    }

    private static String resolveErrorString(JsonNode payloadObject) {
        if (nullOrEmptyAction(payloadObject)) {
            return DesignServiceConstants.ACTION;
        } else if (nullOrEmptyActionLevel(payloadObject)) {
            return DesignServiceConstants.ACTION_LEVEL;
        } else if (nullOrEmptyVnfType(payloadObject)) {
            return DesignServiceConstants.VNF_TYPE;
        } else if (nullOrEmptyProtocol(payloadObject)) {
            return DesignServiceConstants.PROTOCOL;
        }
        return null;
    }

    private static String resolveUploadArtifactErrorString(JsonNode payloadObject) {
        if (nullOrEmptyArtifactName(payloadObject)) {
            return DesignServiceConstants.ARTIFACT_NAME;
        } else if (doesNotContainReference(payloadObject)) {
            return DesignServiceConstants.ACTION;
        } else if (nullOrEmptyArtifactVersion(payloadObject)) {
            return DesignServiceConstants.ARTIFACT_VERSOIN;
        } else if (payloadObject.get(DesignServiceConstants.ARTIFACT_CONTENTS) == null) {
            return DesignServiceConstants.ARTIFACT_CONTENTS;
        } else if (nullOrEmptyArtifactType(payloadObject)) {
            return DesignServiceConstants.ARTIFACT_TYPE;
        } else if (nullOrEmptyVnfType(payloadObject)) {
            return DesignServiceConstants.VNF_TYPE;
        }
        return null;
    }

    private static boolean doesNotContainReference(JsonNode payloadObject) {
        return
            !payloadObject.get(DesignServiceConstants.ARTIFACT_NAME).textValue().contains("reference")
                && nullOrEmptyAction(payloadObject);
    }

    private static String resolveSetStatusErrorString(JsonNode payloadObject) {
        if (nullOrEmptyUserId(payloadObject)) {
            return DesignServiceConstants.USER_ID;
        } else if (nullOrEmptyVnfType(payloadObject)) {
            return DesignServiceConstants.VNF_TYPE;
        } else if (nullOrEmptyAction(payloadObject)) {
            return DesignServiceConstants.ACTION;
        } else if (nullOrEmptyArtifactType(payloadObject)) {
            return DesignServiceConstants.ARTIFACT_TYPE;
        } else if (nullOrEmptyStatus(payloadObject)) {
            return DesignServiceConstants.STATUS;
        }
        return null;
    }

    private static String resolveGetStatusErrorString(JsonNode payloadObject) {
        if (nullOrEmptyUserId(payloadObject)) {
            return DesignServiceConstants.USER_ID;
        } else if (nullOrEmptyVnfType(payloadObject)) {
            return DesignServiceConstants.VNF_TYPE;
        }
        return null;
    }

    private static String resolveGetArtifactErrorString(JsonNode payloadObject) {
        if (nullOrEmptyVnfType(payloadObject)) {
            return DesignServiceConstants.VNF_TYPE;
        } else if (nullOrEmptyArtifactType(payloadObject)) {
            return DesignServiceConstants.ARTIFACT_TYPE;
        } else if (nullOrEmptyArtifactName(payloadObject)) {
            return DesignServiceConstants.ARTIFACT_NAME;
        }
        return null;
    }

    private static boolean nullOrEmptyProtocol(JsonNode payloadObject) {
        return payloadObject.get(DesignServiceConstants.PROTOCOL) == null || payloadObject
            .get(DesignServiceConstants.PROTOCOL).textValue().isEmpty();
    }

    private static boolean nullOrEmptyActionLevel(JsonNode payloadObject) {
        return payloadObject.get(DesignServiceConstants.ACTION_LEVEL) == null || payloadObject
            .get(DesignServiceConstants.ACTION_LEVEL).textValue().isEmpty();
    }

    private static boolean nullOrEmptyArtifactVersion(JsonNode payloadObject) {
        return payloadObject.get(DesignServiceConstants.ARTIFACT_VERSOIN) == null || payloadObject
            .get(DesignServiceConstants.ARTIFACT_VERSOIN).textValue().isEmpty();
    }

    private static boolean nullOrEmptyStatus(JsonNode payloadObject) {
        return payloadObject.get(DesignServiceConstants.STATUS) == null || payloadObject
            .get(DesignServiceConstants.STATUS).textValue().isEmpty();
    }

    private static boolean nullOrEmptyAction(JsonNode payloadObject) {
        return payloadObject.get(DesignServiceConstants.ACTION) == null || payloadObject
            .get(DesignServiceConstants.ACTION).textValue().isEmpty();
    }

    private static boolean nullOrEmptyArtifactName(JsonNode payloadObject) {
        return payloadObject.get(DesignServiceConstants.ARTIFACT_NAME) == null || payloadObject
            .get(DesignServiceConstants.ARTIFACT_NAME).textValue().isEmpty();
    }

    private static boolean nullOrEmptyArtifactType(JsonNode payloadObject) {
        return payloadObject.get(DesignServiceConstants.ARTIFACT_TYPE) == null || payloadObject
            .get(DesignServiceConstants.ARTIFACT_TYPE).textValue().isEmpty();
    }

    private static boolean nullOrEmptyVnfType(JsonNode payloadObject) {
        return payloadObject.get(DesignServiceConstants.VNF_TYPE) == null || payloadObject
            .get(DesignServiceConstants.VNF_TYPE).textValue().isEmpty();
    }

    private static boolean nullOrEmptyUserId(JsonNode payloadObject) {
        return payloadObject.get(DesignServiceConstants.USER_ID) == null || payloadObject
            .get(DesignServiceConstants.USER_ID).textValue().isEmpty();
    }
}


