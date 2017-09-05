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
package org.openecomp.appc.data.services.db;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.junit.Test;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource.QueryStatus;
import org.openecomp.appc.data.services.db.DGGeneralDBService;

public class TestDGGeneralDBService {
    DGGeneralDBService dbService;
    private static String STRING_ENCODING = "utf-8";

    // @Before
    public void setUp() {
        Properties props = new Properties();
        InputStream propStr = getClass().getResourceAsStream("/svclogic.properties");
        if (propStr == null) {
            System.err.println("src/test/resources/svclogic.properties missing");
        }
        try {
            props.load(propStr);
            propStr.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Could not initialize properties");
        }
        // Add properties to global properties
        Enumeration propNames = props.keys();
        while (propNames.hasMoreElements()) {
            String propName = (String) propNames.nextElement();
            System.setProperty(propName, props.getProperty(propName));
        }
        dbService = DGGeneralDBService.initialise();
    }

    @Test(expected = Exception.class)
    public void testGetUploadConfig() throws SvcLogicException {

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vnf-name", "test");
        ctx.setAttribute("vnf-id", "test");
        QueryStatus status = dbService.getUploadConfigInfo(ctx, "test");
        assertEquals(status, "SUCCESS");

    }

    @Test(expected = Exception.class)
    public void testGetDeviceProtocolByVnfType() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vnf-type", "test");
        QueryStatus status = dbService.getDeviceProtocolByVnfType(ctx, "test");
        assertEquals(status, "SUCCESS");
    }

    @Test(expected = Exception.class)
    public void testGettConfigFileReferenceByFileTypeNVnfType() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vnf-type", "test");
        QueryStatus status = dbService.getConfigFileReferenceByFileTypeNVnfType(ctx, "test", "device_configuration");
        assertEquals(status, "SUCCESS");

    }

    @Test(expected = Exception.class)
    public void testGetDeviceAuthenticationByVnfType() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vnf-type", "test");
        QueryStatus status = dbService.getDeviceAuthenticationByVnfType(ctx, "test");
        assertEquals(status, "SUCCESS");

    }

    @Test(expected = Exception.class)
    public void testGetTemplate() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vnfc-type", "test");
        ctx.setAttribute("request-action", "Configure");
        QueryStatus status = dbService.getTemplate(ctx, "test", "config_template");
        assertEquals(status, "SUCCESS");

    }

    @Test(expected = Exception.class)
    public void testGetTemplateByVnfTypeNAction() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vnf-type", "test");
        ctx.setAttribute("request-action", "Configure");
        QueryStatus status = dbService.getTemplateByVnfTypeNAction(ctx, "test", "config_template");
        assertEquals(status, "SUCCESS");

    }

    @Test(expected = Exception.class)
    public void testGetTemplateByTemplateName() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("request-action", "Configure");
        ctx.setAttribute("vnf-type", "test");
        QueryStatus status = dbService.getTemplateByTemplateName(ctx, "test", "template.json");
        assertEquals(status, "SUCCESS");

    }

    @Test(expected = Exception.class)
    public void testGetTemplateByVnfType() throws SvcLogicException {

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vnf-type", "test");
        QueryStatus status = dbService.getTemplateByVnfType(ctx, "test", "config_template");
        assertEquals(status, "SUCCESS");

    }

    @Test(expected = Exception.class)
    public void testGetConfigureActionDGByVnfTypeNAction() throws SvcLogicException {

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vnf-type", "test");
        ctx.setAttribute("request-action", "ConfigModify");
        QueryStatus status = dbService.getConfigureActionDGByVnfTypeNAction(ctx, "test");
        assertEquals(status, "SUCCESS");

    }

    @Test(expected = Exception.class)
    public void testGetConfigureActionDGByVnfType() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vnf-type", "test");
        QueryStatus status = dbService.getConfigureActionDGByVnfType(ctx, "test");
        assertEquals(status, "SUCCESS");

    }

    @Test(expected = Exception.class)
    public void testGetMaxConfigFileId() throws SvcLogicException {

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vnf-id", "test");
        ctx.setAttribute("vm-name", "test");
        QueryStatus status = dbService.getMaxConfigFileId(ctx, "test", "device_configuration");
        assertEquals(status, "SUCCESS");

    }

    @Test(expected = Exception.class)
    public void testGetConfigFilesByVnfVmNCategory() throws SvcLogicException {

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vnf-id", "test");
        ctx.setAttribute("vm-name", "test");
        QueryStatus status = dbService.getConfigFilesByVnfVmNCategory(ctx, "test", "device_configuration", "test",
                "ibcx0001vm001");
        assertEquals(status, "SUCCESS");

    }

    @Test(expected = Exception.class)
    public void testGetDownloadConfigTemplateByVnf() throws SvcLogicException {

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vnf-type", "test");
        QueryStatus status = dbService.getDownloadConfigTemplateByVnf(ctx, "test");
        assertEquals(status, "SUCCESS");
    }

    @Test(expected = Exception.class)
    public void testSaveConfigTxLog() throws SvcLogicException, IOException {

        SvcLogicContext ctx = new SvcLogicContext();
        String message = IOUtils.toString(
                TestDGGeneralDBService.class.getClassLoader().getResourceAsStream("query/message3.txt"),
                STRING_ENCODING);
        ctx.setAttribute("request-id", "1234");
        String escapedMessage = StringEscapeUtils.escapeSql(message);
        ctx.setAttribute("log-message", escapedMessage);
        ctx.setAttribute("log-message-type", "request");
        QueryStatus status = dbService.saveConfigTransactionLog(ctx, "test");
        assertEquals(status, "SUCCESS");
    }

}
