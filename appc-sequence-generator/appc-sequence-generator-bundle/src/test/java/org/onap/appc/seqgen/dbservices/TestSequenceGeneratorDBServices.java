/*-
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

package org.onap.appc.seqgen.dbservices;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource.QueryStatus;

public class TestSequenceGeneratorDBServices {

    private SequenceGeneratorDBServices sequenceGeneratorDBServices;
    private SvcLogicContext localContext;
    private SvcLogicResource serviceLogic;

    @Before
    public void setUp() {
        localContext = new SvcLogicContext();
        localContext.setAttribute("artifact-content", "artifact");
        sequenceGeneratorDBServices = new SequenceGeneratorDBServices();
        serviceLogic = Mockito.mock(SvcLogicResource.class);
        Whitebox.setInternalState(sequenceGeneratorDBServices, "serviceLogic", serviceLogic);
    }

    @Test
    public void testGetOutputPayloadTemplate() throws SvcLogicException {
        when(serviceLogic.query(eq("SQL"), eq(false), eq(null), anyString(), eq(null), eq(null), eq(localContext)))
                .thenReturn(QueryStatus.SUCCESS);
        assertEquals("artifact", sequenceGeneratorDBServices.getOutputPayloadTemplate(localContext));
    }

    @Test(expected = SvcLogicException.class)
    public void testGetOutputPayloadTemplateException() throws SvcLogicException {
        when(serviceLogic.query(eq("SQL"), eq(false), eq(null), anyString(), eq(null), eq(null), eq(localContext)))
                .thenReturn(QueryStatus.FAILURE);
        sequenceGeneratorDBServices.getOutputPayloadTemplate(localContext);
    }

    @Test(expected = SvcLogicException.class)
    public void testGetOutputPayloadTemplateFailure() throws SvcLogicException {
        String query =
                "select artifact_content from asdc_artifacts where artifact_name = $artifactName  and internal_version = $maxInternalVersion ";
        when(serviceLogic.query("SQL", false, null, query, null, null, localContext)).thenReturn(QueryStatus.FAILURE);
        sequenceGeneratorDBServices.getOutputPayloadTemplate(localContext);
    }

    @Test
    public void testGetOutputPayloadTemplateNullContext() throws SvcLogicException {
        assertNull(sequenceGeneratorDBServices.getOutputPayloadTemplate(null));
    }

    @Test
    public void testInitialise() throws SvcLogicException {
        assertNotNull(SequenceGeneratorDBServices.initialise());
    }

}
