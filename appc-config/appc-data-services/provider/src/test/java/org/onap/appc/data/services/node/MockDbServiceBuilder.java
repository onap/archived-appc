package org.onap.appc.data.services.node;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import org.onap.appc.data.services.db.DGGeneralDBService;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;

class MockDbServiceBuilder {

    private final DGGeneralDBService dbServiceMock;

    MockDbServiceBuilder() throws SvcLogicException {
        dbServiceMock = mock(DGGeneralDBService.class);

        doReturn(SvcLogicResource.QueryStatus.SUCCESS)
            .when(dbServiceMock)
            .getConfigFileReferenceByFileTypeNVnfType(any(SvcLogicContext.class), anyString(), anyString());
    }

    MockDbServiceBuilder configFileReference(String prefix, String fileType, SvcLogicResource.QueryStatus status) throws SvcLogicException {
        doReturn(status)
            .when(dbServiceMock)
            .getConfigFileReferenceByFileTypeNVnfType(any(SvcLogicContext.class), eq(prefix), eq(fileType));

        return this;
    }

    DGGeneralDBService build() {
        return dbServiceMock;
    }
}
