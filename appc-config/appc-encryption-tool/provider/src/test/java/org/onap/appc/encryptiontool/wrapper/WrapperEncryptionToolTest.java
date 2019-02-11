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

package org.onap.appc.encryptiontool.wrapper;

import java.io.IOException;
import java.sql.SQLException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.onap.ccsdk.sli.core.dblib.DbLibService;
import org.onap.ccsdk.sli.core.dblib.DBResourceManager;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.sun.rowset.CachedRowSetImpl;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DbServiceUtil.class)
public class WrapperEncryptionToolTest {

    private DbLibService dbLibService;
    private DBResourceManager dbResourceManager;


    @Test
    public void testAnsible() throws SQLException, IOException {
        PowerMockito.mockStatic(DbServiceUtil.class);
        dbLibService = Mockito.mock(DbLibService.class);
        dbResourceManager = Mockito.mock(DBResourceManager.class);
        CachedRowSetImpl rowset = Mockito.mock(CachedRowSetImpl.class);
        Mockito.when(rowset.first()).thenReturn(true);
        Mockito.when(rowset.getString(Mockito.anyString()))
            .thenReturn(null);
        PowerMockito.when(DbServiceUtil.getData(Mockito.anyString(), Mockito.anyList(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString())).thenReturn(rowset);
        PowerMockito.when(DbServiceUtil.initDbLibService()).thenReturn(dbResourceManager);
        WrapperEncryptionTool.main(new String[] {"VNF-TYPE", "ANSIBLE", "USER", "PASS", "ACTION", "PORT", "URL"});
        Mockito.verify(dbResourceManager).cleanUp();
    }

    @Test
    public void testNonAnsible() throws SQLException, IOException {
        PowerMockito.mockStatic(DbServiceUtil.class);
        dbLibService = Mockito.mock(DbLibService.class);
        dbResourceManager = Mockito.mock(DBResourceManager.class);
        CachedRowSetImpl rowset = Mockito.mock(CachedRowSetImpl.class);
        Mockito.when(rowset.first()).thenReturn(true);
        Mockito.when(rowset.getString(Mockito.anyString()))
            .thenReturn(null);
        PowerMockito.when(DbServiceUtil.getData(Mockito.anyString(), Mockito.anyList(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString())).thenReturn(rowset);
        PowerMockito.when(DbServiceUtil.initDbLibService()).thenReturn(dbResourceManager);
        WrapperEncryptionTool.main(new String[] {"VNF-TYPE", "TEST", "USER", "PASS", "ACTION", "PORT", "URL"});
        Mockito.verify(dbResourceManager).cleanUp();
    }
}
