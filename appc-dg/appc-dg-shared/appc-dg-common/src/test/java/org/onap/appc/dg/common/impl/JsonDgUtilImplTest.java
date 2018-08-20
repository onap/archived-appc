/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications Copyright (C) 2018 IBM.
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

package org.onap.appc.dg.common.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.onap.appc.exceptions.APPCException;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

public class JsonDgUtilImplTest {

    private static final ThreadLocal<SimpleDateFormat> DATE_TIME_PARSER_THREAD_LOCAL = ThreadLocal
            .withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    private JsonDgUtilImpl jsonDgUtil;

    @Before
    public void setUp() {
        jsonDgUtil = new JsonDgUtilImpl();
    }

    @Test
    public void testFlatAndAddToContext() throws Exception {
        String key = "payload";
        String testValueKey = "test-key";
        String testValueValue = "test-value";
        String testValueKey2 = "test-key2";
        String testValueValue2 = "test-value2";
        String payload = "{\"" + testValueKey + "\": \"" + testValueValue + "\",\"" + testValueKey2 + "\": \""
                + testValueValue2 + "\"}";

        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> params = new HashMap<>();
        params.put(key, payload);
        jsonDgUtil.flatAndAddToContext(params, ctx);

        Assert.assertEquals(ctx.getAttribute(testValueKey), testValueValue);
        Assert.assertEquals(ctx.getAttribute(testValueKey2), testValueValue2);

    }

    @Test
    public void testFlatAndAddToContextNegativeWrongPayload() throws Exception {
        String key = "payload";
        String testValueKey = "test-key";
        String testValueValue = "test-value";
        String testValueKey2 = "test-key2";
        String testValueValue2 = "test-value2";
        String payload = "{{\"" + testValueKey + "\": \"" + testValueValue + "\",\"" + testValueKey2 + "\": \""
                + testValueValue2 + "\"}";

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
        String key = "payload";
        String testValueKey = "test-key";
        String testValueValue = "test-value";
        String testValueKey2 = "test-key2";
        String testValueValue2 = "test-value2";
        String payload = "{\"" + testValueKey + "\": \"" + testValueValue + "\",\"" + testValueKey2 + "\": \""
                + testValueValue2 + "\"}";

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

    @Test
    public void testCvaasFileNameAndFileContentToContext() throws Exception {

        String key1 = "running-config.upload-date";
        String testValueKey1 = "2004-02-09 00:00:00:000";
        Long epochUploadTimestamp = DATE_TIME_PARSER_THREAD_LOCAL.get().parse(testValueKey1).getTime();
        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> params = new HashMap<>();
        ctx.setAttribute(key1, testValueKey1);
        jsonDgUtil.cvaasFileNameAndFileContentToContext(params, ctx);
        assertNotNull(ctx.getAttribute("cvaas-file-content"));
        assertTrue(ctx.getAttribute("cvaas-file-content").contains(epochUploadTimestamp.toString()));
    }

    @Test(expected = APPCException.class)
    public void testCvaasFileNameAndFileContentToContextForEmptyParams() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> params = new HashMap<>();
        jsonDgUtil.cvaasFileNameAndFileContentToContext(params, ctx);
    }

    @Test(expected = APPCException.class)
    public void testCheckFileCreated() throws APPCException {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("cvaas-file-name", "testCvaasFile");
        Map<String, String> params = new HashMap<>();
        params.put("vnf-id", "testVnfId");
        jsonDgUtil.checkFileCreated(params, ctx);
    }
}
