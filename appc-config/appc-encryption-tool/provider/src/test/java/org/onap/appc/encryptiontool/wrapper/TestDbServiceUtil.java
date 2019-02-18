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

package org.onap.appc.encryptiontool.wrapper;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.rowset.CachedRowSet;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.ccsdk.sli.core.dblib.DBResourceManager;
import org.powermock.reflect.Whitebox;


public class TestDbServiceUtil {

    private DBResourceManager jdbcDataSource;
    private CachedRowSet cachedRowSet;
    private List<String> argList = new ArrayList<>();

    @Before
    public void setUp() {
        jdbcDataSource = Mockito.mock(DBResourceManager.class);
        cachedRowSet = Mockito.mock(CachedRowSet.class);
    }

    @Test
    public void testUpdateDB() throws SQLException {
        Whitebox.setInternalState(DbServiceUtil.class, "jdbcDataSource", jdbcDataSource);
        when(jdbcDataSource.writeData(eq("update tableName set  where "), anyObject(), eq(Constants.SCHEMA_SDNCTL)))
                .thenReturn(true);
        assertTrue(DbServiceUtil.updateDB("tableName", argList, "", ""));
    }

    @Test
    public void testGetData() throws SQLException {
        Whitebox.setInternalState(DbServiceUtil.class, "jdbcDataSource", jdbcDataSource);
        when(jdbcDataSource.getData(eq("select from tableName where "), anyObject(), eq(Constants.SCHEMA_SDNCTL)))
                .thenReturn(cachedRowSet);
        assertSame(cachedRowSet, DbServiceUtil.getData("tableName", argList, Constants.SCHEMA_SDNCTL, "", ""));
    }

    @Test
    public void testDeleteData() throws SQLException {
        Whitebox.setInternalState(DbServiceUtil.class, "jdbcDataSource", jdbcDataSource);
        when(jdbcDataSource.writeData(eq("delete from tableName"), anyObject(), eq(Constants.SCHEMA_SDNCTL)))
                .thenReturn(true);
        assertTrue(DbServiceUtil.deleteData("tableName", argList));
    }

    @Test
    public void testInsertDB() throws SQLException {
        Whitebox.setInternalState(DbServiceUtil.class, "jdbcDataSource", jdbcDataSource);
        when(jdbcDataSource.writeData(eq("INSERT INTO  tableName ( )   VALUES ()"), anyObject(),
                eq(Constants.SCHEMA_SDNCTL))).thenReturn(true);
        assertTrue(DbServiceUtil.insertDB("tableName", argList, "", ""));
    }
}
