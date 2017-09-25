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

package org.openecomp.appc.dg.util.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;
import org.onap.ccsdk.sli.adaptors.aai.AAIClient;
import org.onap.ccsdk.sli.adaptors.aai.AAIService;
import org.powermock.reflect.Whitebox;

import java.util.HashMap;
import java.util.Map;

public class InputParameterValidationImplTest {
    private SvcLogicContext svcLogicContext;

    private InputParameterValidationImpl inputParameterValidation;

    @Before
    public void setUp() throws Exception {
        inputParameterValidation = new InputParameterValidationImpl();
        svcLogicContext = new SvcLogicContext();
        svcLogicContext.setAttribute("a", "b");
        svcLogicContext.setAttribute("b", "c");
    }

    @Test
    public void validateAttributeSuccess() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("a", "b");
        params.put("b", "c");

        inputParameterValidation.validateAttribute(params, svcLogicContext);

        Assert.assertEquals("true", svcLogicContext.getAttribute("validateAttribute"));
    }

    @Test
    public void validateAttributeFailure() throws Exception {
        // wrong value
        Map<String, String> params = new HashMap<>();
        params.put("e", "f");

        inputParameterValidation.validateAttribute(params, svcLogicContext);

        Assert.assertEquals("false", svcLogicContext.getAttribute("validateAttribute"));

        // null value
        params = new HashMap<>();
        params.put("e", null);

        inputParameterValidation.validateAttribute(params, svcLogicContext);

        Assert.assertEquals("false", svcLogicContext.getAttribute("validateAttribute"));
    }

    @Test
    public void validateAttributeNull() throws Exception {
        inputParameterValidation.validateAttribute(null, svcLogicContext);

        Assert.assertEquals("false", svcLogicContext.getAttribute("validateAttribute"));
    }

    @Test
    public void validateAttributeLengthSuccess() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("maximum_length_param", "2");
        params.put("a", "1");

        inputParameterValidation.validateAttributeLength(params, svcLogicContext);

        Assert.assertEquals("true", svcLogicContext.getAttribute("validateAttributeLength"));
    }

    @Test
    public void validateAttributeLengthFailure() throws Exception {
        // wrong key
        Map<String, String> params = new HashMap<>();
        params.put("maximum_length_param", "2");
        params.put("e", "1");

        inputParameterValidation.validateAttributeLength(params, svcLogicContext);

        Assert.assertEquals("false", svcLogicContext.getAttribute("validateAttributeLength"));

        //over length
        params = new HashMap<>();
        params.put("maximum_length_param", "2");
        params.put("c", "3");

        inputParameterValidation.validateAttributeLength(params, svcLogicContext);

        Assert.assertEquals("false", svcLogicContext.getAttribute("validateAttributeLength"));
    }

    @Test
    public void validateAttributeLengthNull() throws Exception {
        inputParameterValidation.validateAttributeLength(null, svcLogicContext);

        Assert.assertEquals("false", svcLogicContext.getAttribute("validateAttributeLength"));
    }

    @Test
    public void validateAttributeCharacterSuccess() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("special_characters", "z");
        params.put("a", "1");

        inputParameterValidation.validateAttributeCharacter(params, svcLogicContext);

        Assert.assertEquals("true", svcLogicContext.getAttribute("validateAttributeCharacter"));
    }

    @Test
    public void validateAttributeCharacterFailure() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("special_characters", "z");
        params.put("d", "1");

        inputParameterValidation.validateAttributeCharacter(params, svcLogicContext);

        Assert.assertEquals("false", svcLogicContext.getAttribute("validateAttributeCharacter"));
    }

    @Test
    public void validateAttributeCharacterNull() throws Exception {
        inputParameterValidation.validateAttributeCharacter(null, svcLogicContext);

        Assert.assertEquals("false", svcLogicContext.getAttribute("validateAttributeCharacter"));
    }

    @Test
    public void testGetValueFromContext() throws Exception {
        Map<String, String> result = Whitebox.invokeMethod(inputParameterValidation, "getValueFromContext",
            svcLogicContext);
        Assert.assertEquals("b", result.get("a"));
        Assert.assertEquals("c", result.get("b"));
    }
}