package org.onap.appc.metadata.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
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
    public void getVnfModel_should_return_vnfModel_when_present_in_cache() {
        MetadataCache<DependencyModelIdentifier, String> cache = mock(MetadataCache.class);
        when(cache.getObject(any(DependencyModelIdentifier.class))).thenReturn("test-vnf-model");
        metadataService.setCache(cache);

        assertEquals("test-vnf-model", metadataService.getVnfModel(mockModelIdentifier));

        verify(cache, times(1)).getObject(mockModelIdentifier);
        verify(mockCache, times(0)).putObject(any(DependencyModelIdentifier.class), anyString());

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

        verify(mockDbLibService, times(1))
            .getData(anyString(), any(ArrayList.class), anyString());
        verify(mockCachedRowSet, times(1)).getString("ARTIFACT_CONTENT");
        verify(mockCache, times(1)).putObject(mockModelIdentifier, "test-vnf-model");
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

        verify(mockDbLibService, times(1))
            .getData(anyString(), any(ArrayList.class), anyString());
        verify(mockCachedRowSet, times(1)).getString("ARTIFACT_CONTENT");
        verify(mockCache, times(0)).putObject(mockModelIdentifier, "test-vnf-model");

    }

    @Test(expected = RuntimeException.class)
    public void getVnfModel_should_read_from_database_when_null_vnfName_and_throw_when_database_error()
        throws SQLException {

        when(mockCache.getObject(any(DependencyModelIdentifier.class))).thenReturn(null);
        when(mockModelIdentifier.getCatalogVersion()).thenReturn(null);
        when(mockDbLibService.getData(anyString(), any(ArrayList.class), anyString())).thenThrow(new SQLException());

        assertEquals(null, metadataService.getVnfModel(mockModelIdentifier));

        verify(mockDbLibService, times(1))
            .getData(anyString(), any(ArrayList.class), anyString());
        verify(mockCachedRowSet, times(0)).getString("ARTIFACT_CONTENT");
        verify(mockCache, times(0)).putObject(any(DependencyModelIdentifier.class), anyString());
    }


    @Test
    public void getVnfModel_should_read_from_database_when_null_vnfName_and_return_null_when_not_found()
        throws SQLException {

        when(mockCache.getObject(any(DependencyModelIdentifier.class))).thenReturn(null);
        when(mockModelIdentifier.getCatalogVersion()).thenReturn(null);
        when(mockDbLibService.getData(anyString(), any(ArrayList.class), anyString())).thenReturn(mockCachedRowSet);
        when(mockCachedRowSet.first()).thenReturn(false);

        assertEquals(null, metadataService.getVnfModel(mockModelIdentifier));

        verify(mockDbLibService, times(1))
            .getData(anyString(), any(ArrayList.class), anyString());
        verify(mockCachedRowSet, times(0)).getString("ARTIFACT_CONTENT");
        verify(mockCache, times(0)).putObject(any(DependencyModelIdentifier.class), anyString());
    }

}
