/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 Ericsson
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

package org.onap.appc.validationpolicy;

import static org.junit.Assert.assertEquals;
import java.sql.SQLException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.onap.ccsdk.sli.core.dblib.DbLibService;
import com.sun.rowset.CachedRowSetImpl;

public class RequestValidationPolicyTest {

    private final String TEST_CONTENT = "TEST_CONTENT";
    private final String ARTIFACT_CONTENT = "ARTIFACT_CONTENT";
    private DbLibService db;
    private RequestValidationPolicy policy;
    private CachedRowSetImpl rowset;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void setup() throws SQLException {
        db = Mockito.mock(DbLibService.class);
        rowset = Mockito.mock(CachedRowSetImpl.class);
        Mockito.when(rowset.next()).thenReturn(true);
        Mockito.when(db.getData(Mockito.anyString(), Mockito.any(), Mockito.anyString())).thenReturn(rowset);
        policy = new RequestValidationPolicy();
        policy.setDbLibService(db);
    }

    @Test
    public void testGetPolicyJson() throws SQLException {
        Mockito.when(rowset.getString(ARTIFACT_CONTENT)).thenReturn(TEST_CONTENT);
        assertEquals(TEST_CONTENT, policy.getPolicyJson());
    }

    @Test
    public void testGetPolicyJsonBlank() throws SQLException {
        Mockito.when(rowset.next()).thenThrow(new SQLException());
        expectedEx.expect(RuntimeException.class);
        policy.getPolicyJson();
    }

    @Test
    public void testGetPolicyJsonError() throws SQLException {
        Mockito.when(rowset.getString(ARTIFACT_CONTENT)).thenReturn("");
        policy.getPolicyJson();
    }

    @Test
    public void testGetInProgressRuleExecutor() {
        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("Rule executor not available, initialization of RequestValidationPolicy failed");
        policy.getInProgressRuleExecutor();
    }
}
