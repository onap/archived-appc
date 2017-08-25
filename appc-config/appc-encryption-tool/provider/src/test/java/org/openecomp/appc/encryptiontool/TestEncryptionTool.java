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

package org.openecomp.appc.encryptiontool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.openecomp.appc.encryptiontool.wrapper.DbServiceUtil;
import org.openecomp.appc.encryptiontool.wrapper.EncryptionToolDGWrapper;
import org.openecomp.appc.encryptiontool.wrapper.WrapperEncryptionTool;
import org.openecomp.sdnc.sli.SvcLogicContext;

public class TestEncryptionTool {

	@Test
	public void testEncryptionTool() throws Exception{
		String [] input = new String[] {"testVnf_Type","testUser","testPassword11", "testAction1", "8080", "http://localhost:8080/restconf/healthcheck"};
		WrapperEncryptionTool.main(input);

	}
	@Test(expected=Exception.class)
	public void testgetPropertyDG() throws Exception{
		EncryptionToolDGWrapper et = new EncryptionToolDGWrapper();		
		SvcLogicContext ctx = new SvcLogicContext();		
		Map<String, String> inParams = new HashMap<String, String>();
		inParams.put("prefix", "test");
		inParams.put("propertyName", "testVnf_Type.testAction1.url");
		et.getProperty(inParams, ctx);
	}
	@Test(expected=Exception.class)
	public void testgetData() throws Exception
	{
		DbServiceUtil d = new DbServiceUtil();
		ArrayList argList = null;
		String schema ="sdnctl";
		String tableName ="dual";
		String getselectData ="123";
		String getDataClasue="123='123'";
		d.getData(tableName, argList, schema, getselectData, getDataClasue);
	}
	@Test(expected=Exception.class)
	public void testupdateDB() throws Exception
	{
		DbServiceUtil d = new DbServiceUtil();
		String setCluase = null;
		String schema ="sdnctl";
		String tableName ="dual";
		ArrayList inputArgs = null;
		String whereClause="123='123'";
		d.updateDB(tableName, inputArgs, schema, whereClause, setCluase);
	}
}
