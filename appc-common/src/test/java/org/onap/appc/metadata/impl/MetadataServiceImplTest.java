/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2018 Nokia Solutions and Networks
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
 * ============LICENSE_END=========================================================
 */
package org.onap.appc.metadata.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.ArrayList;
import javax.sql.rowset.CachedRowSet;
import org.junit.Before;
import org.junit.Test;
import org.onap.appc.cache.MetadataCache;
import org.onap.appc.metadata.objects.DependencyModelIdentifier;
import org.onap.ccsdk.sli.core.dblib.DbLibService;

public class MetadataServiceImplTest {


    private MetadataServiceImpl metadataService = new MetadataServiceImpl();
    private DbLibService mockDbLibService = mock(DbLibService.class);
    private CachedRowSet mockCachedRowSet = mock(CachedRowSet.class);
    private MetadataCache<DependencyModelIdentifier, String> mockCache = mock(MetadataCache.class);
    private DependencyModelIdentifier mockModelIdentifier = mock(DependencyModelIdentifier.class);

    @Before
    public void setup() throws SQLException {
        metadataService.setDbLibService(mockDbLibService);
        metadataService.setCache(mockCache);
    }

    @Test
    public void getVnfModel_should_return_vnfModel_when_present_in_cache() throws SQLException {
        when(mockCache.getObject(mockModelIdentifier)).thenReturn("test-vnf-model");

        assertEquals("test-vnf-model", metadataService.getVnfModel(mockModelIdentifier));

        verify(mockCache).getObject(mockModelIdentifier);
        verify(mockDbLibService, never()).getData(anyString(), any(ArrayList.class), anyString());
        verify(mockCache, never()).putObject(any(DependencyModelIdentifier.class), anyString());
    }

    @Test
    public void getVnfModel_should_read_from_database_when_null_vnfName_and_return_when_found()
        throws SQLException {

        when(mockCache.getObject(any(DependencyModelIdentifier.class))).thenReturn(null);
        when(mockModelIdentifier.getCatalogVersion()).thenReturn("test-vnf-catalog-version");
        when(mockDbLibService.getData(anyString(), any(ArrayList.class), anyString())).thenReturn(mockCachedRowSet);
        when(mockCachedRowSet.first()).thenReturn(true);
        when(mockCachedRowSet.getString("ARTIFACT_CONTENT")).thenReturn("test-vnf-model");

        assertEquals("test-vnf-model", metadataService.getVnfModel(mockModelIdentifier));

        verify(mockDbLibService).getData(anyString(), any(ArrayList.class), anyString());
        verify(mockCachedRowSet).getString("ARTIFACT_CONTENT");
        verify(mockCache).putObject(mockModelIdentifier, "test-vnf-model");
    }

    @Test(expected = RuntimeException.class)
    public void getVnfModel_should_read_from_database_when_null_vnfName_and_throw_when_invalid_dependency_model()
        throws SQLException {

        when(mockCache.getObject(any(DependencyModelIdentifier.class))).thenReturn(null);
        when(mockModelIdentifier.getCatalogVersion()).thenReturn(null);

        when(mockDbLibService.getData(anyString(), any(ArrayList.class), anyString())).thenReturn(mockCachedRowSet);
        when(mockCachedRowSet.first()).thenReturn(true);
        when(mockCachedRowSet.getString("ARTIFACT_CONTENT")).thenReturn(null);

        assertEquals(null, metadataService.getVnfModel(mockModelIdentifier));

        verify(mockDbLibService).getData(anyString(), any(ArrayList.class), anyString());
        verify(mockCachedRowSet).getString("ARTIFACT_CONTENT");
        verify(mockCache, never()).putObject(mockModelIdentifier, "test-vnf-model");

    }

    @Test(expected = RuntimeException.class)
    public void getVnfModel_should_read_from_database_when_null_vnfName_and_throw_when_database_error()
        throws SQLException {

        when(mockCache.getObject(any(DependencyModelIdentifier.class))).thenReturn(null);
        when(mockModelIdentifier.getCatalogVersion()).thenReturn(null);
        when(mockDbLibService.getData(anyString(), any(ArrayList.class), anyString())).thenThrow(new SQLException());

        assertEquals(null, metadataService.getVnfModel(mockModelIdentifier));

        verify(mockDbLibService).getData(anyString(), any(ArrayList.class), anyString());
        verify(mockCachedRowSet, times(0)).getString("ARTIFACT_CONTENT");
        verify(mockCache, never()).putObject(any(DependencyModelIdentifier.class), anyString());
    }


    @Test
    public void getVnfModel_should_read_from_database_when_null_vnfName_and_return_null_when_not_found()
        throws SQLException {

        when(mockCache.getObject(any(DependencyModelIdentifier.class))).thenReturn(null);
        when(mockModelIdentifier.getCatalogVersion()).thenReturn(null);
        when(mockDbLibService.getData(anyString(), any(ArrayList.class), anyString())).thenReturn(mockCachedRowSet);
        when(mockCachedRowSet.first()).thenReturn(false);

        assertEquals(null, metadataService.getVnfModel(mockModelIdentifier));

        verify(mockDbLibService).getData(anyString(), any(ArrayList.class), anyString());
        verify(mockCachedRowSet, times(0)).getString("ARTIFACT_CONTENT");
        verify(mockCache, never()).putObject(any(DependencyModelIdentifier.class), anyString());
    }

}
