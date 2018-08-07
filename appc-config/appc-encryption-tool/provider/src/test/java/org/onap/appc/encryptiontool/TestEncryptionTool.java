/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
 * Modification Copyright (C) 2018 IBM.
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

package org.onap.appc.encryptiontool;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.onap.appc.encryptiontool.wrapper.DbServiceUtil;
import org.onap.appc.encryptiontool.wrapper.EncryptionTool;
import org.onap.appc.encryptiontool.wrapper.EncryptionToolDGWrapper;
import org.onap.appc.encryptiontool.wrapper.WrapperEncryptionTool;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import static org.junit.Assert.assertEquals;

public class TestEncryptionTool {

    //@Test
    public void testEncryptionTool() throws Exception {
        String[] input = new String[]{"testVnf_Type", "testUser", "testPassword11", "testAction1", "8080",
            "http://localhost:8080/restconf/healthcheck"};
        WrapperEncryptionTool.main(input);

    }

    @Test(expected = Exception.class)
    public void testgetPropertyDG() throws Exception {
        EncryptionToolDGWrapper et = new EncryptionToolDGWrapper();
        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> inParams = new HashMap<>();
        inParams.put("prefix", "test");
        inParams.put("propertyName", "testVnf_Type.testAction1.url");
        et.getProperty(inParams, ctx);
    }

    @Test(expected = Exception.class)
    public void testgetData() throws Exception {
        List<String> argList = null;
        String schema = "sdnctl";
        String tableName = "dual";
        String getselectData = "123";
        String getDataClasue = "123='123'";
        DbServiceUtil.getData(tableName, argList, schema, getselectData, getDataClasue);
    }

    @Test(expected = Exception.class)
    public void testupdateDB() throws Exception {
        String setClause = null;
        String tableName = "dual";
        List<String> inputArgs = null;
        String whereClause = "123='123'";
        DbServiceUtil.updateDB(tableName, inputArgs, whereClause, setClause);
    }

    @Test
    public void decrypt() throws Exception {
        EncryptionTool et = EncryptionTool.getInstance();
        System.out.println(et.decrypt("enc:Ai8KLw=="));
    }

    //@Test(expected=Exception.class)
    public void testupdateProperties() throws Exception {
        WrapperEncryptionTool.updateProperties(
            "testuser2", "", "abc3", "", "22", "testhost1", "Ansible");
    }

    //@Test(expected=Exception.class)
    public void testgetProperties() throws Exception {
        EncryptionToolDGWrapper et = new EncryptionToolDGWrapper();
        Map<String, String> inParams = new HashMap<>();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vnf-type", "test2");
        ctx.setAttribute("input.action", "Configure");
        ctx.setAttribute("APPC.protocol.PROTOCOL", "Ansible");
        inParams.put("propertyName", "user");
        inParams.put("prefix", "user");
        et.getProperty(inParams, ctx);
    }
    
    @Test
    public void testEncrypt() throws Exception {
        EncryptionTool et = EncryptionTool.getInstance();
        String s1=et.encrypt("sampleText");
        assertEquals("enc:MD4XHE0udVFHOw==", s1);
    }
}
