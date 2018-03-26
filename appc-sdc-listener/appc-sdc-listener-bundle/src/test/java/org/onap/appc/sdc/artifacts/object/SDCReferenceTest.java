/*
* ============LICENSE_START=======================================================
* ONAP : APPC
* ================================================================================
* Copyright 2018 TechMahindra
*=================================================================================
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* ============LICENSE_END=========================================================
*/
package org.onap.appc.sdc.artifacts.object;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SDCReferenceTest {
    private SDCReference sDCReference;
    
    @Before
    public void setUp() {
        sDCReference = new SDCReference();
    }
    @Test
    public void testGetVnfType() {
        sDCReference.setVnfType("vnfType");
        Assert.assertNotNull(sDCReference.getVnfType());
        Assert.assertEquals("vnfType", sDCReference.getVnfType());
    }

    @Test
    public void testGetVnfcType() {
        sDCReference.setVnfcType("vnfcType");
        Assert.assertNotNull(sDCReference.getVnfcType());
        Assert.assertEquals("vnfcType", sDCReference.getVnfcType());
    }
    
    @Test
    public void testGetFileCategory() {
        sDCReference.setFileCategory("fileCategory");
        Assert.assertNotNull(sDCReference.getFileCategory());
        Assert.assertEquals("fileCategory", sDCReference.getFileCategory());
    }
    
    @Test
    public void testGetAction() {
        sDCReference.setAction("action");
        Assert.assertNotNull(sDCReference.getAction());
        Assert.assertEquals("action", sDCReference.getAction());
    }
    
    @Test
    public void testGetArtifactType() {
        sDCReference.setArtifactType("artifactType");
        Assert.assertNotNull(sDCReference.getArtifactType());
        Assert.assertEquals("artifactType", sDCReference.getArtifactType());
    }
    
    @Test
    public void testGetArtifactName() {
        sDCReference.setArtifactName("artifactName");
        Assert.assertNotNull(sDCReference.getArtifactName());
        Assert.assertEquals("artifactName", sDCReference.getArtifactName());
    }
    
    @Test
    public void testToString_ReturnNonEmptyString() {
        assertNotEquals(sDCReference.toString(), "");
        assertNotEquals(sDCReference.toString(), null);
    }

    @Test
    public void testToString_ContainsString() {
        assertTrue(sDCReference.toString().contains("vnfType"));
    }
}
