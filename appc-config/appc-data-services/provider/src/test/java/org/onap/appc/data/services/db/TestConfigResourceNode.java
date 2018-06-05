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

package org.onap.appc.data.services.db;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Ignore;
import org.junit.Test;
import org.onap.appc.data.services.AppcDataServiceConstant;
import org.onap.appc.data.services.node.ConfigResourceNode;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

@Deprecated // class must be rewritten
public class TestConfigResourceNode {

    @Deprecated // timeout due NPE
    @Ignore("Test is taking 60 seconds")
    @Test(expected = Exception.class)
    public void testgetConfigFileReferenc() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        ConfigResourceNode dbService = new ConfigResourceNode(DGGeneralDBService.initialise());
        Map<String, String> map = new HashMap<>();
        dbService.getConfigFileReference(map, ctx);
    }

    @Deprecated // timeout due NPE
    @Ignore("Test is taking 60 seconds")
    @Test(expected = Exception.class)
    public void testgetTemplate() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        ctx.setAttribute(AppcDataServiceConstant.INPUT_PARAM_FILE_CATEGORY, "test");
        ctx.setAttribute(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX, "tmp.test");
        ctx.setAttribute("template-name", "test.json");

        ConfigResourceNode dbService = new ConfigResourceNode(DGGeneralDBService.initialise());
        Map<String, String> map = new HashMap<>();
        dbService.getTemplate(map, ctx);

    }

    @Deprecated // timeout due NPE
    @Ignore("Test is taking 60 seconds")
    @Test(expected = Exception.class)
    public void testgetVnfcReference() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        ConfigResourceNode dbService = new ConfigResourceNode(DGGeneralDBService.initialise());
        Map<String, String> map = new HashMap<>();
        map.put(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX, "tmp.test");
        dbService.getVnfcReference(map, ctx);
    }

    @Deprecated // timeout due NPE
    @Ignore("Test is taking 60 seconds")
    @Test(expected = Exception.class)
    public void testgetSmmChainKeyFiles() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        ctx.setAttribute("site-location","test/location");
        ConfigResourceNode dbService = new ConfigResourceNode(DGGeneralDBService.initialise());
        Map<String, String> map = new HashMap<>();
        map.put(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX, "tmp.test");
        dbService.getSmmChainKeyFiles(map, ctx);
    }

    @Deprecated // timeout due NPE
    @Ignore("Test is taking 60 seconds")
    @Test(expected = Exception.class)
    public void testgetDownloadConfigTemplateByVnf() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");    
        ConfigResourceNode dbService = new ConfigResourceNode(DGGeneralDBService.initialise());
        Map<String, String> map = new HashMap<>();
        map.put(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX, "tmp.test");
        dbService.getDownloadConfigTemplateByVnf(map, ctx);

    }

    @Deprecated // timeout due NPE
    @Ignore("Test is taking 60 seconds")
    @Test(expected = Exception.class)
    public void testgetCommonConfigInfo() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        ConfigResourceNode dbService = new ConfigResourceNode(DGGeneralDBService.initialise());
        Map<String, String> map = new HashMap<>();
        dbService.getCommonConfigInfo(map, ctx);

    }

    @Deprecated // timeout due NPE
    @Ignore("Test is taking 60 seconds")
    @Test(expected = Exception.class)
    public void testupdateUploadConfigss() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        ConfigResourceNode dbService = new ConfigResourceNode(DGGeneralDBService.initialise());
        Map<String, String> map = new HashMap<>();
        dbService.updateUploadConfig(map, ctx);

    }

    @Deprecated // timeout due NPE
    @Ignore("Test is taking 60 seconds")
    @Test(expected = Exception.class)
    public void testgetConfigFilesByVnfVmNCategory() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();
        ConfigResourceNode node = new ConfigResourceNode(DGGeneralDBService.initialise());
        Map<String, String> inParams = new HashMap<>();
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX, "response-prefix");
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_FILE_CATEGORY, "config_template");
        inParams.put((String) AppcDataServiceConstant.INPUT_PARAM_VNF_ID, "test");
        inParams.put((String) AppcDataServiceConstant.INPUT_PARAM_VM_NAME, "test");
        node.getConfigFilesByVnfVmNCategory(inParams, ctx);
    }

    @Deprecated // timeout due NPE
    @Ignore("Test is taking 60 seconds")
    @Test(expected = Exception.class)
    public void testsaveConfigTransactionLog() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();
        ConfigResourceNode node = new ConfigResourceNode(DGGeneralDBService.initialise());
        Map<String, String> inParams = new HashMap<>();
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_MESSAGE, "testMessage");
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX, "response-prefix");
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_MESSAGE_TYPE, "testmessage");
        ctx.setAttribute("request-id", "tets-id");
        node.saveConfigTransactionLog(inParams, ctx);

    }

    @Deprecated // timeout due NPE
    @Ignore("Test is taking 60 seconds")
    @Test(expected = Exception.class)
    public void testsaveConfigBlock() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();
        ConfigResourceNode node = new ConfigResourceNode(DGGeneralDBService.initialise());
        Map<String, String> inParams = new HashMap<>();
        inParams.put(AppcDataServiceConstant.INPUT_PARAM_RESPONSE_PREFIX, "tmp");
        ctx.setAttribute("configuration", "test");
        ctx.setAttribute("tmp.convertconfig.escapeData", "test");
        ctx.setAttribute("tmp.merge.mergedData", "test");
        node.saveConfigBlock(inParams, ctx);
    }

}
