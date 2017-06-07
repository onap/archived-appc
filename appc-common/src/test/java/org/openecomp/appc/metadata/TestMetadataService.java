/*-
 * ============LICENSE_START=======================================================
 * APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Amdocs
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */

package org.openecomp.appc.metadata;

import org.openecomp.sdnc.sli.resource.dblib.DbLibService;
import com.sun.rowset.CachedRowSetImpl;
import org.mockito.Mockito;
import org.openecomp.appc.metadata.impl.MetadataServiceImpl;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;



public class TestMetadataService {

    MetadataServiceImpl metadataService = new MetadataServiceImpl();

    TestMetadataService() throws SQLException {
        DbLibService dbLibService = mock(DbLibService.class);
        metadataService.setDbLibService(dbLibService);
        CachedRowSet mockRS  = new CachedRowSetImpl();
        Mockito.when(dbLibService.getData(anyString(), (ArrayList<String>)anyCollection(), anyString())).thenReturn(mockRS);
    }


}
