/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modification Copyright (C) 2018 IBM
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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.junit.Test;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource.QueryStatus;
import org.onap.appc.data.services.db.DGGeneralDBService;

public class TestDGGeneralDBService {
    DGGeneralDBService dbService;
    private static String STRING_ENCODING = "utf-8";

    @Test
    public void testGetUploadConfig() throws SvcLogicException {

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vnf-name", "test");
        ctx.setAttribute("vnf-id", "test");
        MockDGGeneralDBService dbService =     MockDGGeneralDBService.initialise();
        QueryStatus status = dbService.getUploadConfigInfo(ctx, "test");
        assertEquals(status, QueryStatus.SUCCESS);

    }

    @Test
    public void testGetDeviceProtocolByVnfType() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vnf-type", "test");
        MockDGGeneralDBService dbService =     MockDGGeneralDBService.initialise();
        QueryStatus status = dbService.getDeviceProtocolByVnfType(ctx, "test");
        assertEquals(status, QueryStatus.SUCCESS);
    }

    @Test
    public void testGettConfigFileReferenceByFileTypeNVnfType() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vnf-type", "test");
        MockDGGeneralDBService dbService =     MockDGGeneralDBService.initialise();
        QueryStatus status = dbService.getConfigFileReferenceByFileTypeNVnfType(ctx, "test", "device_configuration");
        assertEquals(status, QueryStatus.SUCCESS);

    }

    @Test
    public void testGetDeviceAuthenticationByVnfType() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vnf-type", "test");
        MockDGGeneralDBService dbService =     MockDGGeneralDBService.initialise();
        QueryStatus status = dbService.getDeviceAuthenticationByVnfType(ctx, "test");
        assertEquals(status, QueryStatus.SUCCESS);

    }

    @Test
    public void testGetTemplate() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vnfc-type", "test");
        ctx.setAttribute("request-action", "Configure");
        MockDGGeneralDBService dbService =     MockDGGeneralDBService.initialise();
        QueryStatus status = dbService.getTemplate(ctx, "test", "config_template");
        assertEquals(status, QueryStatus.SUCCESS);

    }

    @Test
    public void testGetTemplateByVnfTypeNAction() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vnf-type", "test");
        ctx.setAttribute("request-action", "ConfigScaleOut");
        MockDGGeneralDBService dbService =     MockDGGeneralDBService.initialise();
        QueryStatus status = dbService.getTemplateByVnfTypeNAction(ctx, "test", "config_template");
        assertEquals(status, QueryStatus.SUCCESS);

    }

    @Test
    public void testGetTemplateByTemplateName() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("request-action", "Configure");
        ctx.setAttribute("vnf-type", "test");
        MockDGGeneralDBService dbService =     MockDGGeneralDBService.initialise();
        QueryStatus status = dbService.getTemplateByTemplateName(ctx, "test", "template.json");
        assertEquals(status, QueryStatus.SUCCESS);

    }

    @Test
    public void testGetTemplateByVnfType() throws SvcLogicException {

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vnf-type", "test");
        MockDGGeneralDBService dbService =     MockDGGeneralDBService.initialise();
        QueryStatus status = dbService.getTemplateByVnfType(ctx, "test", "config_template");
        assertEquals(status, QueryStatus.SUCCESS);

    }

    @Test
    public void testGetConfigureActionDGByVnfTypeNAction() throws SvcLogicException {

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vnf-type", "test");
        ctx.setAttribute("request-action", "ConfigModify");
        MockDGGeneralDBService dbService =     MockDGGeneralDBService.initialise();
        QueryStatus status = dbService.getConfigureActionDGByVnfTypeNAction(ctx, "test");
        assertEquals(status, QueryStatus.SUCCESS);

    }

    @Test
    public void testGetConfigureActionDGByVnfType() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vnf-type", "test");
        MockDGGeneralDBService dbService =     MockDGGeneralDBService.initialise();
        QueryStatus status = dbService.getConfigureActionDGByVnfType(ctx, "test");
        assertEquals(status, QueryStatus.SUCCESS);

    }

    @Test
    public void testGetMaxConfigFileId() throws SvcLogicException {

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vnf-id", "test");
        ctx.setAttribute("vm-name", "test");
        MockDGGeneralDBService dbService =     MockDGGeneralDBService.initialise();
        QueryStatus status = dbService.getMaxConfigFileId(ctx, "test", "device_configuration");
        assertEquals(status, QueryStatus.SUCCESS);

    }

    @Test
    public void testGetConfigFilesByVnfVmNCategory() throws SvcLogicException {

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vnf-id", "test");
        ctx.setAttribute("vm-name", "test");
        MockDGGeneralDBService dbService =     MockDGGeneralDBService.initialise();
        QueryStatus status = dbService.getConfigFilesByVnfVmNCategory(ctx, "test", "device_configuration", "test",
                "ibcx0001vm001");
        assertEquals(status, QueryStatus.SUCCESS);

    }

    @Test
    public void getVnfcReferenceByVnfcTypeNAction() throws SvcLogicException {

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vnf-type", "testVnf");
        ctx.setAttribute("vnfc-type", "testVnfc");
        ctx.setAttribute("request-action", "Configure");
        MockDGGeneralDBService dbService =     MockDGGeneralDBService.initialise();
        QueryStatus status = dbService.getVnfcReferenceByVnfcTypeNAction(ctx, "test");
        assertEquals(status, QueryStatus.SUCCESS);
    }
    
    @Test
    public void testGetDownloadConfigTemplateByVnf() throws SvcLogicException {

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vnf-type", "test");
        MockDGGeneralDBService dbService =     MockDGGeneralDBService.initialise();
        QueryStatus status = dbService.getDownloadConfigTemplateByVnf(ctx, "test");
        assertEquals(status, QueryStatus.SUCCESS);
    }

    @Test
    public void testSaveConfigTxLog() throws SvcLogicException, IOException {

        SvcLogicContext ctx = new SvcLogicContext();
        String message = IOUtils.toString(
                TestDGGeneralDBService.class.getClassLoader().getResourceAsStream("query/message3.txt"),
                STRING_ENCODING);
        ctx.setAttribute("request-id", "1234");
        String escapedMessage = StringEscapeUtils.escapeSql(message);
        ctx.setAttribute("log-message", escapedMessage);
        ctx.setAttribute("log-message-type", "request");
        MockDGGeneralDBService dbService =     MockDGGeneralDBService.initialise();
        QueryStatus status = dbService.saveConfigTransactionLog(ctx, "test");
        assertEquals(status, QueryStatus.SUCCESS);
    }

    @Test
    public void testGetVnfcReferenceByVnfTypeNActionWithTemplateModelId() throws Exception {
        MockDGGeneralDBService dbService =     MockDGGeneralDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        String prefix="test";
        String templateModelId = "template001";
        dbService.getVnfcReferenceByVnfTypeNActionWithTemplateModelId(ctx, prefix, templateModelId);
    }

    @Test
    public void testGetTemplateWithTemplateModelId() throws Exception {
        MockDGGeneralDBService dbService =     MockDGGeneralDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        String prefix="test";
        String templateModelId = "template001";
        String fileCategory="testCategory";
        dbService.getTemplateWithTemplateModelId(ctx, prefix, fileCategory, templateModelId);
    }

    @Test
    public void testgetTemplateByVnfTypeNActionWithTemplateModelId() throws Exception {
        MockDGGeneralDBService dbService =     MockDGGeneralDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        String prefix="test";
        String templateModelId = "template001";
        String fileCategory="testCategory";
        dbService.getTemplateByVnfTypeNActionWithTemplateModelId(ctx, prefix, fileCategory, templateModelId);
    }

    @Test
    public void testGetConfigFileReferenceByVnfType() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vnf-type", "test");
        ctx.setAttribute("request-action", "Configure");
        MockDGGeneralDBService dbService =     MockDGGeneralDBService.initialise();
        QueryStatus status = dbService.getConfigFileReferenceByVnfType(ctx, "test");
        assertEquals(status, QueryStatus.SUCCESS);

    }
    
    @Test
    public void testGetTemplateByArtifactType() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vnf-type", "test");
        ctx.setAttribute("request-action", "Configure");
        MockDGGeneralDBService dbService =     MockDGGeneralDBService.initialise();
        QueryStatus status = dbService.getTemplateByArtifactType(ctx, "test","XML","APPC-CONFIG");
        assertEquals(QueryStatus.SUCCESS, status);

    }
    
    @Test
    public void testCleanContextPropertyByPrefix()
    {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vnf-type", "test");
        ctx.setAttribute("request-action", "Configure");
        ctx.setAttribute(".vnfc-type", "Configure");
        MockDGGeneralDBService dbService =     MockDGGeneralDBService.initialise();
        dbService.cleanContextPropertyByPrefix(ctx, "test");
        assertEquals(2,ctx.getAttributeKeySet().size());
    }
}
