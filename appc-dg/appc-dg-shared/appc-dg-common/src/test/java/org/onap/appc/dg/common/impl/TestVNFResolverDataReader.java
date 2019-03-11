/*-
 * ============LICENSE_START=======================================================
 * ONAP - APPC
 * ================================================================================
 * Copyright (C) 2019 IBM.
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
 * ============LICENSE_END=========================================================
 */
package org.onap.appc.dg.common.impl;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

public class TestVNFResolverDataReader {
    
    VNFResolverDataReader vNFResolverDataReader;
    Collection<String> list = Arrays.asList("action", "api_version", "vnf_type", "vnf_version");
    String queryStatement = "select vnf_type,vnf_version,api_version,action,dg_name,dg_version,dg_module FROM VNF_DG_MAPPING";

    @Before
    public void setUp() {
        vNFResolverDataReader = new VNFResolverDataReader();
    }

    @Test
    public void testGetAttributeNamesAndQueryStmt() {
        assertEquals(list, vNFResolverDataReader.getAttributeNames());
        assertEquals(queryStatement, vNFResolverDataReader.getQueryStmt());
    }
}
