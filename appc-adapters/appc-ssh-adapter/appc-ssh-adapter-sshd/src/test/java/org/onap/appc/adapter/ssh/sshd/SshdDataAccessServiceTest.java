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

package org.onap.appc.adapter.ssh.sshd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.sql.SQLException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.appc.adapter.ssh.SshConnectionDetails;
import org.onap.ccsdk.sli.core.dblib.DbLibService;
import com.sun.rowset.CachedRowSetImpl;

public class SshdDataAccessServiceTest {

    private SshdDataAccessService sshdDataAccessService;
    private DbLibService db;
    @Before
    public void setUp() throws SQLException {
        sshdDataAccessService = new SshdDataAccessService();
        db = Mockito.mock(DbLibService.class);
        CachedRowSetImpl rowset = Mockito.mock(CachedRowSetImpl.class);
        Mockito.when(rowset.first()).thenReturn(true);
        Mockito.when(db.getData(Mockito.anyString(), Mockito.any(), Mockito.anyString())).thenReturn(rowset);
        sshdDataAccessService.setDbLibService(db);
    }

    @Test
    public void testSetSchema() {
        sshdDataAccessService.setSchema("test");
        assertEquals("test", sshdDataAccessService.getSchema());
    }

    @Test
    public void testSetDbLibService() {
        sshdDataAccessService.setDbLibService(db);
        assertEquals(false, sshdDataAccessService.getDbLibService().isActive());
    }

    @Test
    public void testRetrieveConnectionDetails() {
        SshConnectionDetails connectionDetails = new SshConnectionDetails();
        sshdDataAccessService.setDbLibService(db);
        assertTrue(sshdDataAccessService.retrieveConnectionDetails("test", connectionDetails));
    }

    @Test
    public void testRetrieveConfigFileName() {
        assertEquals(null, sshdDataAccessService.retrieveConfigFileName("test"));
    }
}
