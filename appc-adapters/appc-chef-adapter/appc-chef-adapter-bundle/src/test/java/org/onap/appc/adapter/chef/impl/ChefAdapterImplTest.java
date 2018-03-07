/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 Nokia. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */
package org.onap.appc.adapter.chef.impl;


import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import org.json.JSONObject;
import org.junit.Test;
import org.onap.appc.adapter.chef.ChefAdapter;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

public class ChefAdapterImplTest {

    private static final String EXPECTED_NODE_OBJECT_ATTR_NAME = "chef.nodeObject";

    @Test
    public void nodeObjectBuilder_shouldBuildJsonNodeObject_forPassedParams_andAddToSvcLogicContext() {
        // GIVEN
        Map<String, String> params = givenInputParams();
        SvcLogicContext svcLogicContext = new SvcLogicContext();

        // WHEN
        ChefAdapter chefAdapter = new ChefAdapterFactory().create();
        chefAdapter.nodeObejctBuilder(params, svcLogicContext);

        // THEN
        assertThat(resultJson(svcLogicContext)).isEqualTo(expectedJson());
    }

    private String resultJson(SvcLogicContext svcLogicContext) {
        String resultJsonString = svcLogicContext.getAttribute(EXPECTED_NODE_OBJECT_ATTR_NAME);
        return new JSONObject(resultJsonString).toString();
    }

    private Map<String, String> givenInputParams() {
        return ImmutableMap.<String, String>builder()
            .put("nodeobject.name", "testNodeName")
            .put("nodeobject.normal", "val:normal")
            .put("nodeobject.overrides", "val:override")
            .put("nodeobject.defaults", "val:default")
            .put("nodeobject.run_list", "val1,val2,val3")
            .put("nodeobject.chef_environment", "testChefEnvVal")
            .build();
    }

    private String expectedJson() {
        JSONObject expectedJson = new JSONObject();
        expectedJson.put("json_class", "Chef::Node");
        expectedJson.put("chef_type", "node");
        expectedJson.put("automatic", Collections.emptyMap());
        expectedJson.put("name", "testNodeName");
        expectedJson.put("normal", ImmutableMap.of("val", "normal"));
        expectedJson.put("override", ImmutableMap.of("val", "override"));
        expectedJson.put("default", ImmutableMap.of("val", "default"));
        expectedJson.put("run_list", ImmutableList.of("val1", "val2", "val3"));
        expectedJson.put("chef_environment", "testChefEnvVal");
        return expectedJson.toString();
    }

    @Test
    public void combineStrings_shouldConcatenateTwoParamStrings_andSetThemInSvcContext() {
        // GIVEN
        Map<String, String> params = ImmutableMap
            .of("dgContext", "contextValue", "String1", "paramString1", "String2", "paramString2");
        SvcLogicContext svcLogicContext = new SvcLogicContext();

        // WHEN
        ChefAdapter chefAdapter = new ChefAdapterFactory().create();
        chefAdapter.combineStrings(params, svcLogicContext);

        // THEN
        assertThat(svcLogicContext.getAttribute("contextValue")).isEqualTo("paramString1paramString2");
    }
}