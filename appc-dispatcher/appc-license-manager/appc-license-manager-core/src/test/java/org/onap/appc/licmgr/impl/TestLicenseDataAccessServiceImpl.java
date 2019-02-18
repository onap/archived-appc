/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 Ericsson. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.licmgr.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import java.sql.SQLException;
import javax.sql.rowset.CachedRowSet;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.appc.licmgr.Constants;
import org.onap.appc.licmgr.exception.DataAccessException;
import org.onap.ccsdk.sli.core.dblib.DbLibService;

public class TestLicenseDataAccessServiceImpl {

    private LicenseDataAccessServiceImpl licenseDataAccessServiceImpl;
    private DbLibService dbLibService;
    private String[] field;
    private CachedRowSet data;

    @Before
    public void setUp() {
        licenseDataAccessServiceImpl = new LicenseDataAccessServiceImpl();
        dbLibService = Mockito.mock(DbLibService.class);
        data = Mockito.mock(CachedRowSet.class);
        licenseDataAccessServiceImpl.setDbLibService(dbLibService);
        licenseDataAccessServiceImpl.setSchema("sdnctl");
    }

    @Test
    public void testRetrieveLicenseModelData() throws SQLException {
        when(dbLibService.getData(anyString(), anyObject(), eq(Constants.NETCONF_SCHEMA))).thenReturn(data);
        when(data.first()).thenReturn(true);
        when(data.getString(anyString())).thenReturn("data");
        assertEquals("data", licenseDataAccessServiceImpl.retrieveLicenseModelData("vnf-type", "1.0", field)
                .get("ARTIFACT_CONTENT"));
    }

    @Test
    public void testRetrieveLicenseModelDataNull() throws SQLException {
        when(dbLibService.getData(anyString(), anyObject(), eq(Constants.NETCONF_SCHEMA))).thenReturn(data);
        assertNull(licenseDataAccessServiceImpl.retrieveLicenseModelData("vnf-type", "1.0", field)
                .get("ARTIFACT_CONTENT"));
    }

    @Test(expected = DataAccessException.class)
    public void testRetrieveLicenseModelDataException() throws SQLException {
        when(dbLibService.getData(anyString(), anyObject(), eq(Constants.NETCONF_SCHEMA)))
                .thenThrow(new SQLException());
        assertNull(licenseDataAccessServiceImpl.retrieveLicenseModelData("vnf-type", "1.0", field)
                .get("ARTIFACT_CONTENT"));
    }

}
