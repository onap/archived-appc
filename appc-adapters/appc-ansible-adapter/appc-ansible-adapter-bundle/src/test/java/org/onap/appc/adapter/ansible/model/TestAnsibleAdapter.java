/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */
package org.onap.appc.adapter.ansible.model;

import static org.junit.Assert.assertNotNull;
import java.lang.reflect.*;
import org.junit.Test;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;

public class TestAnsibleAdapter {

    private Class[] parameterTypes;
    private AnsibleMessageParser ansibleMessageParser;
    private Method m;
    private String name;
    private Class[] parameterTypes2;
    private Method m2;

    @Test
    public void callPrivateConstructorsMethodsForCodeCoverage() throws SecurityException, NoSuchMethodException,
            IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {

        /* test constructors */
        Class<?>[] classesOne = { AnsibleMessageParser.class };
        for (Class<?> clazz : classesOne) {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            name = constructor.getName();
            constructor.setAccessible(true);
            assertNotNull(constructor.newInstance());
        }
        Class<?>[] classesTwo = { AnsibleServerEmulator.class };
        for (Class<?> clazz : classesTwo) {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            name = constructor.getName();
            constructor.setAccessible(true);
            assertNotNull(constructor.newInstance());
        }
        Class<?>[] classesThree = { AnsibleResult.class };
        for (Class<?> clazz : classesThree) {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            name = constructor.getName();
            constructor.setAccessible(true);
            assertNotNull(constructor.newInstance());
        }

        /* test methods */
        ansibleMessageParser = new AnsibleMessageParser();
        parameterTypes = new Class[1];
        parameterTypes[0] = java.lang.String.class;

        m = ansibleMessageParser.getClass().getDeclaredMethod("getFilePayload", parameterTypes);
        m.setAccessible(true);
        assertNotNull(m.invoke(ansibleMessageParser, "{\"test\": test}"));
        // test logging-suppression for an invalid host value (Fortify Log Forging fix)
        String input = "{\"Results\":{\"192.168.1.10\":{\"Id\":\"101\",\"StatusCode\":200,\"StatusMessage\":\"SUCCESS\"},\"192%168%1%10\":{\"Id\":\"102\",\"StatusCode\":200,\"StatusMessage\":\"SUCCESS\"},\"server-dev.att.com\":{\"Id\":\"103\",\"StatusCode\":200,\"StatusMessage\":\"SUCCESS\"}},\"StatusCode\":200,\"StatusMessage\":\"FINISHED\"}";
        JSONObject postResponse = new JSONObject(input);
        // ansibleResult = parseGetResponseNested(ansibleResult, postResponse);
        parameterTypes2 = new Class[2];
        parameterTypes2[0] = AnsibleResult.class;
        parameterTypes2[1] = JSONObject.class;
        m2 = ansibleMessageParser.getClass().getDeclaredMethod("parseGetResponseNested", parameterTypes2);
        m2.setAccessible(true);
        AnsibleResult ansibleResult = new AnsibleResult();

        // assertNotNull(m2.invoke(ansibleMessageParser, ansibleResult, postResponse));
        m2.invoke(ansibleMessageParser, ansibleResult, postResponse);
    }
}
