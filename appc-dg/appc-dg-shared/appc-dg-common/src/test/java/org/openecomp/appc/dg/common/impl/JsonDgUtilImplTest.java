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

package org.openecomp.appc.dg.common.impl;

import ch.qos.logback.core.Appender;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.appc.dg.common.impl.JsonDgUtilImpl;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.sdnc.sli.SvcLogicContext;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;

public class JsonDgUtilImplTest {

    private final Appender appender = mock(Appender.class);


    @Test
    public void testFlatAndAddToContext() throws Exception {
        JsonDgUtilImpl jsonDgUtil = new JsonDgUtilImpl();
        String key = "payload";
        String testValueKey = "test-key";
        String testValueValue = "test-value";
        String testValueKey2 = "test-key2";
        String testValueValue2 = "test-value2";
        String payload = "{\"" + testValueKey + "\": \"" + testValueValue + "\",\""+testValueKey2+"\": \""+testValueValue2+"\"}";

        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> params = new HashMap<>();
        params.put(key, payload);
        jsonDgUtil.flatAndAddToContext(params, ctx);


            Assert.assertEquals(ctx.getAttribute(testValueKey), testValueValue);
            Assert.assertEquals(ctx.getAttribute(testValueKey2), testValueValue2);



    }


    @Test
    public void testFlatAndAddToContextNegativeWrongPayload() throws Exception {
        JsonDgUtilImpl jsonDgUtil = new JsonDgUtilImpl();
        String key = "payload";
        String testValueKey = "test-key";
        String testValueValue = "test-value";
        String testValueKey2 = "test-key2";
        String testValueValue2 = "test-value2";
        String payload = "{{\"" + testValueKey + "\": \"" + testValueValue + "\",\""+testValueKey2+"\": \""+testValueValue2+"\"}";

        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> params = new HashMap<>();
        params.put(key, payload);
        try {
            jsonDgUtil.flatAndAddToContext(params, ctx);

        } catch (APPCException e) {
            Assert.assertNull(ctx.getAttribute(testValueKey));
            Assert.assertNull(ctx.getAttribute(testValueKey2));
            Assert.assertNotNull(ctx.getAttribute("error-message"));
        }


    }


    @Test
    public void testFlatAndAddToContextPayloadFromContext() throws Exception {
        JsonDgUtilImpl jsonDgUtil = new JsonDgUtilImpl();
        String key = "payload";
        String testValueKey = "test-key";
        String testValueValue = "test-value";
        String testValueKey2 = "test-key2";
        String testValueValue2 = "test-value2";
        String payload = "{\"" + testValueKey + "\": \"" + testValueValue + "\",\""+testValueKey2+"\": \""+testValueValue2+"\"}";

        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> params = new HashMap<>();
        params.put(key, "");
        ctx.setAttribute("input.payload", payload);
        jsonDgUtil.flatAndAddToContext(params, ctx);


        Assert.assertEquals(ctx.getAttribute(testValueKey), testValueValue);
        Assert.assertEquals(ctx.getAttribute(testValueKey2), testValueValue2);
    }

    @Test
    public void testFlatAndAddToContextNegativeNullPayload() throws Exception {
        JsonDgUtilImpl jsonDgUtil = new JsonDgUtilImpl();
        String testValueKey = "test-key";
        String testValueKey2 = "test-key2";
        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> params = new HashMap<>();
        jsonDgUtil.flatAndAddToContext(params, ctx);


        Assert.assertNull(ctx.getAttribute(testValueKey));
        Assert.assertNull(ctx.getAttribute(testValueKey2));
    }


    @Test
    public void testFlatAndAddToContextNegativeEmptyPayload() throws Exception {

        JsonDgUtilImpl jsonDgUtil = new JsonDgUtilImpl();
        String key = "payload";
        String testValueKey = "test-key";
        String testValueKey2 = "test-key2";

        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> params = new HashMap<>();
        params.put(key, "");
        jsonDgUtil.flatAndAddToContext(params, ctx);

        Assert.assertNull(ctx.getAttribute(testValueKey));
        Assert.assertNull(ctx.getAttribute(testValueKey2));
    }


    @Test
    public void testGenerateOutputPayloadFromContext() throws Exception {

        JsonDgUtilImpl jsonDgUtil = new JsonDgUtilImpl();
        String key = "output.payload";
        String key1 = "output.payload.test-key[0]";
        String key2 = "output.payload.test-key[1]";
        String testValueKey1 = "value1";
        String testValueKey2 = "value2";

        String key3 = "output.payload.test-key3";
        String testValueKey3 = "value3";

        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> params = new HashMap<>();
        ctx.setAttribute(key1, testValueKey1);
        ctx.setAttribute(key2, testValueKey2);
        ctx.setAttribute(key3, testValueKey3);
        jsonDgUtil.generateOutputPayloadFromContext(params, ctx);

        Assert.assertNotNull(ctx.getAttribute(key));

    }
}
