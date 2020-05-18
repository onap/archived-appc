/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications Copyright (C) 2018-2019 IBM
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;

import javax.sql.rowset.CachedRowSet;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.junit.Test;
import org.onap.ccsdk.sli.core.dblib.DbLibService;
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
        assertEquals(QueryStatus.SUCCESS, status);
    }
    
    @Test
    public void testSaveConfigFiles() throws SvcLogicException {

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("data-source", "test");
        ctx.setAttribute("service-instance-id", "test");
        ctx.setAttribute("request-action", "test");
        ctx.setAttribute("vnf-type", "test");
        ctx.setAttribute("vnfc-type", "test");
        ctx.setAttribute("vnf-id", "test");
        ctx.setAttribute("vnf-name", "test");
        ctx.setAttribute("vm-name", "test");
        ctx.setAttribute("file-category", "test");
        ctx.setAttribute("file-content", "test");
        
        MockDGGeneralDBService dbService =     MockDGGeneralDBService.initialise();
        QueryStatus status = dbService.saveConfigFiles(ctx, "test");
        assertEquals(QueryStatus.SUCCESS, status);

    }
    
    @Test
    public void testSavePrepareRelationship() throws SvcLogicException {

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("service-instance-id", "test");
        ctx.setAttribute("request-id", "test");
        
        
        MockDGGeneralDBService dbService =     MockDGGeneralDBService.initialise();
        QueryStatus status = dbService.savePrepareRelationship(ctx, "test","test", "test");
        assertEquals(QueryStatus.SUCCESS,status);

    }
    
    @Test
    public void testSavePrepareRelationshipWithSdcInd() throws SvcLogicException {

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("service-instance-id", "test");
        ctx.setAttribute("request-id", "test");
        MockDGGeneralDBService dbService =     MockDGGeneralDBService.initialise();
        QueryStatus status = dbService.savePrepareRelationship(ctx, "test", "test", "Y");
        assertEquals(QueryStatus.SUCCESS, status);

    }
    
    @Test
    public void testGetDownloadConfigTemplateByVnf() throws SvcLogicException {

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vnf-type", "test");
        MockDGGeneralDBService dbService =     MockDGGeneralDBService.initialise();
        QueryStatus status = dbService.getDownloadConfigTemplateByVnf(ctx, "test");
        assertEquals(QueryStatus.SUCCESS,status);
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
        assertNotNull(dbService);
    }

    @Test
    public void testGetTemplateWithTemplateModelId() throws Exception {
        DbLibService mockDbLibService = mock(DbLibService.class);
        CachedRowSet mockCachedRowSet = mock(CachedRowSet.class);
        when(mockCachedRowSet.next()).thenReturn(false);
        DGGeneralDBService dbService = new DGGeneralDBService(mockDbLibService);
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("request-action", "testRequestAction");
        ctx.setAttribute("vnf-type", "testVnfType");
        ctx.setAttribute("vnfc-type", "testVnfcType");
        String expectedStatement = "SELECT artifact_content file_content , asdc_artifacts_id config_file_id FROM ASDC_ARTIFACTS WHERE"
                + " asdc_artifacts_id = ( SELECT MAX(a.asdc_artifacts_id) configfileid FROM ASDC_ARTIFACTS a, ASDC_REFERENCE b"
                + " WHERE a.artifact_name = b.artifact_name AND file_category = ? AND action = ? AND vnf_type = ? AND vnfc_type = ? )"
                + " and ASDC_ARTIFACTS.artifact_name like ? ; ";
        ArrayList<String> expectedArguments = new ArrayList<>();
        expectedArguments.add("testFileCategory");
        expectedArguments.add("testRequestAction");
        expectedArguments.add("testVnfType");
        expectedArguments.add("testVnfcType");
        String templateModelId = "testTemplateModelId";
        expectedArguments.add("%_"+ templateModelId +"%");
        when(mockDbLibService.getData(any(), any(), any())).thenReturn(mockCachedRowSet);
        dbService.getTemplateWithTemplateModelId(ctx, "testPrefix", "testFileCategory", templateModelId);
        verify(mockDbLibService,times(1)).getData(expectedStatement, expectedArguments, null);
    }

    @Test
    public void testgetTemplateByVnfTypeNActionWithTemplateModelId() throws Exception {
        MockDGGeneralDBService dbService =     MockDGGeneralDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        String prefix="test";
        String templateModelId = "template001";
        String fileCategory="testCategory";
        dbService.getTemplateByVnfTypeNActionWithTemplateModelId(ctx, prefix, fileCategory, templateModelId);
        assertNotNull(dbService);
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
    public void testGetCapability() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vnf-type", "test");
        ctx.setAttribute("artifactName", "template001");
        ctx.setAttribute("maxInternalVersion", "1234");
        MockDGGeneralDBService dbService =     MockDGGeneralDBService.initialise();
        String status = dbService.getCapability(ctx, "test");
        assertNull(status);

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
    
    @Test
    public void testSaveUploadConfig() throws SvcLogicException
    {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("request-id", "test");
        ctx.setAttribute("request-action", "Configure");
        ctx.setAttribute("originator-id", "test");
        ctx.setAttribute("vnf-id", "test");
        ctx.setAttribute("vm-name", "test");
        ctx.setAttribute("vnf-host-ip-address", "10.0.0.1");
        ctx.setAttribute("vnf-type", "test");
        ctx.setAttribute("vnfc-type", "test");
        ctx.setAttribute("tmp.escaped.devicerunningconfig", "10.0.0.1");
        
        MockDGGeneralDBService dbService =     MockDGGeneralDBService.initialise();
        QueryStatus status= dbService.saveUploadConfig(ctx, "test");
        assertEquals(QueryStatus.SUCCESS, status);
    }
    
    @Test
    public void testUpdateUploadConfig() throws SvcLogicException
    {
        SvcLogicContext ctx = new SvcLogicContext();
        
        ctx.setAttribute("vnf-type", "test");
        ctx.setAttribute("vnfc-type", "test");
        
        MockDGGeneralDBService dbService =     MockDGGeneralDBService.initialise();
        QueryStatus status= dbService.updateUploadConfig(ctx, "test",10);
        assertEquals(QueryStatus.SUCCESS, status);
    }
    
    @Test
    public void testGetVnfcReferenceByVnfTypeNAction() throws SvcLogicException
    {
        SvcLogicContext ctx = new SvcLogicContext();
        
        ctx.setAttribute("vnf-type", "test");
        ctx.setAttribute("request-action", "test");
        
        MockDGGeneralDBService dbService =     MockDGGeneralDBService.initialise();
        QueryStatus status= dbService.getVnfcReferenceByVnfTypeNAction(ctx, "test");
        assertEquals(QueryStatus.SUCCESS, status);
    }
}
