/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 IBM
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

package org.onap.appc.design.validator;

import static org.junit.Assert.*;

import org.junit.Test;
import org.onap.appc.design.dbervices.RequestValidator;
import org.powermock.reflect.Whitebox;

public class TestRequestValidator {

    @Test
    public void TestExecuteGetDesigns() throws Exception {
        String action = "getDesigns";
        String payload = "{\"userID\":\"0000\"}";
        // RequestValidator.validate(action, payload);        
        String s = Whitebox.invokeMethod(RequestValidator.class, "validate", action, payload);
        assertEquals(null, s);
    }

    @Test(expected = Exception.class)
    public void TestExecuteGetArtifactException() throws Exception {
        String action = "getArtifact";
        String payload = "{\"userID\":\"0000\",\"vnf-type\":\"TestVnfType\",\"artifact-name\":\"TestArtifactName\"}";
        // RequestValidator.validate(action, payload);        
        String s = Whitebox.invokeMethod(RequestValidator.class, "validate", action, payload);
        assertEquals(null, s);
    }

    @Test
    public void TestExecuteGetArtifact() throws Exception {
        String action = "getArtifact";
        String payload = "{\"userID\":\"0000\",\"vnf-type\":\"TestVnfType\",\"artifact-name\":\"TestArtifactName\",\"artifact-type\":\"TestArtifactType\"}";
        // RequestValidator.validate(action, payload);        
        String s = Whitebox.invokeMethod(RequestValidator.class, "validate", action, payload);
        assertEquals(null, s);
    }

    @Test
    public void TestExecuteGetStatus() throws Exception {
        String action = "getStatus";
        String payload = "{\"userID\":\"0000\",\"vnf-type\":\"TestVnfType\"}";
        // RequestValidator.validate(action, payload);        
        String s = Whitebox.invokeMethod(RequestValidator.class, "validate", action, payload);
        assertEquals(null, s);
    }

    @Test
    public void TestExecuteSetStatus() throws Exception {
        String action = "setStatus";
        String payload = "{\"userID\":\"0000\",\"vnf-type\":\"TestVnfType\",\"artifact-type\":\"TestArtifactType\",\"action\":\"TestAction\",\"status\":\"TestStatus\"}";
        // RequestValidator.validate(action, payload);        
        String s = Whitebox.invokeMethod(RequestValidator.class, "validate", action, payload);
        assertEquals(null, s);
    }

    @Test
    public void TestExecuteUploadArtifact() throws Exception {
        String action = "uploadArtifact";
        String payload = "{\"artifact-name\":\"TestArtifactName\",\"vnf-type\":\"TestVnfType\",\"artifact-type\":\"TestArtifactType\",\"action\":\"TestAction\",\"artifact-version\":\"TestVersion\",\"artifact-contents\":\"TestContents\"}";
        // RequestValidator.validate(action, payload);        
        String s = Whitebox.invokeMethod(RequestValidator.class, "validate", action, payload);
        assertEquals(null, s);
    }

    @Test
    public void TestExecuteSetInCart() throws Exception {
        String action = "setInCart";
        String payload = "{\"vnf-type\":\"TestVnfType\",\"action\":\"TestAction\",\"action-level\":\"TestActionLevel\",\"protocol\":\"TestProtocol\"}";
        // RequestValidator.validate(action, payload);        
        String s = Whitebox.invokeMethod(RequestValidator.class, "validate", action, payload);
        assertEquals(null, s);
    }
}
