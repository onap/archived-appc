/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 Ericsson
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
 *
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.design.dbervices;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.onap.appc.design.services.util.DesignServiceConstants;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import java.io.IOException;

public class RequestValidatorTest {


    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void testValidateArtifactWithVersion() throws RequestValidationException, IOException {
        RequestValidator.validate(DesignServiceConstants.GETAPPCTIMESTAMPUTC, "{}");
        expectedEx.expect(RequestValidationException.class);
        expectedEx.expectMessage("Missing input parameter :-artifact-contents -:");
        RequestValidator.validate(DesignServiceConstants.UPLOADADMINARTIFACT, "{\"" +
                DesignServiceConstants.ARTIFACT_NAME + "\":\"reference\", \"" +
                DesignServiceConstants.ARTIFACT_VERSOIN + "\":\"VERSION\"}");
    }

    @Test
    public void testValidateArtifact() throws RequestValidationException, IOException {
        RequestValidator.validate(DesignServiceConstants.GETAPPCTIMESTAMPUTC, "{}");
        expectedEx.expect(RequestValidationException.class);
        expectedEx.expectMessage("Missing input parameter :-artifact-version -:");
        RequestValidator.validate(DesignServiceConstants.UPLOADADMINARTIFACT, "{\"" +
                DesignServiceConstants.ARTIFACT_NAME + "\":\"reference\"}");
    }

    @Test
    public void testValidateVnf() throws RequestValidationException, IOException {
        expectedEx.expect(RequestValidationException.class);
        expectedEx.expectMessage("Missing input parameter :-vnf-type -:");
        RequestValidator.validate(DesignServiceConstants.CHECKVNF, "{}");
    }

    @Test
    public void testValidateInvalidAction() throws RequestValidationException, IOException {
        expectedEx.expect(RequestValidationException.class);
        expectedEx.expectMessage(" Action INVALID_ACTION not found while processing request ");
        RequestValidator.validate("INVALID_ACTION", "{}");
    }
}
