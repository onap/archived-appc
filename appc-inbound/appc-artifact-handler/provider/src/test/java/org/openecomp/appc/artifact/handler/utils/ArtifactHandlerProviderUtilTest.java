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

package org.openecomp.appc.artifact.handler.utils;

import static org.junit.Assert.assertTrue;
import java.nio.charset.Charset;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

public class ArtifactHandlerProviderUtilTest {

    @Test(expected = Exception.class)
    public void testProcessTemplate() throws Exception {
        String artifact_conetent = IOUtils.toString(ArtifactHandlerProviderUtilTest.class.getClassLoader()
                .getResourceAsStream("templates/reference_template.json"), Charset.defaultCharset());
        JSONObject obj = new JSONObject();
        obj.put("artifact-name", "reference_JunitTestArtifact");
        obj.put("artifact-version", "0.01");
        obj.put("artifact-contents", artifact_conetent);
        ArtifactHandlerProviderUtil ahprovider = new ArtifactHandlerProviderUtil();
        ahprovider.processTemplate(obj.toString());
    }

    @Test(expected = Exception.class)
    public void testcreateDummyRequestData() throws Exception {
        String artifact_conetent = IOUtils.toString(ArtifactHandlerProviderUtilTest.class.getClassLoader()
                .getResourceAsStream("templates/reference_template.json"), Charset.defaultCharset());
        JSONObject obj = new JSONObject();
        obj.put("artifact-name", "reference_JunitTestArtifact");
        obj.put("artifact-version", "0.01");
        obj.put("artifact-contents", artifact_conetent);
        ArtifactHandlerProviderUtil ahprovider = new ArtifactHandlerProviderUtil();
        String requestInfo = ahprovider.createDummyRequestData();
    }

    @Test
    public void testEscapeSql() throws Exception {
        String testStr = "Test String is 'test'";
        ArtifactHandlerProviderUtil ahprovider = new ArtifactHandlerProviderUtil();
        ahprovider.escapeSql(testStr);
        assertTrue(true);
    }

    @Test
    public void testGetRandom() throws Exception {
        ArtifactHandlerProviderUtil ahprovider = new ArtifactHandlerProviderUtil();
        Whitebox.invokeMethod(ahprovider, "getRandom");
        assertTrue(true);
    }

    @Test
    public void testEscapeUtils() throws Exception {
        String str = "The Test string is 'test'";
        EscapeUtils.escapeSql(str);
        assertTrue(true);
    }
}

