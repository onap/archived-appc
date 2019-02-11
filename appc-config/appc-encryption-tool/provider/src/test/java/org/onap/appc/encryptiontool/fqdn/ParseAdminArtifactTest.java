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

package org.onap.appc.encryptiontool.fqdn;

import static org.hamcrest.CoreMatchers.isA;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.onap.ccsdk.sli.adaptors.resource.sql.SqlResource;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource.QueryStatus;
import org.powermock.reflect.Whitebox;

public class ParseAdminArtifactTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void test() throws SvcLogicException {
        ParseAdminArtifcat artifact = ParseAdminArtifcat.initialise();
        SqlResource sqlResource = Mockito.mock(SqlResource.class);
        Mockito.when(sqlResource.query(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.any(SvcLogicContext.class))).thenReturn(QueryStatus.FAILURE);
        Whitebox.setInternalState(artifact, "serviceLogic", sqlResource);
        SvcLogicContext ctx = new SvcLogicContext();
        expectedEx.expect(RuntimeException.class);
        expectedEx.expectCause(isA(SvcLogicException.class));
        artifact.getAdminArtifact(ctx);
    }

}
