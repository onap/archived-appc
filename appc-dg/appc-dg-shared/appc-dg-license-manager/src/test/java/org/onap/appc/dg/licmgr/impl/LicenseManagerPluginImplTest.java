package org.onap.appc.dg.licmgr.impl;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.licmgr.LicenseManager;
import org.onap.appc.licmgr.objects.LicenseModel;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.mockito.Mockito;

@RunWith(PowerMockRunner.class)
@PrepareForTest(LicenseModel.class)
public class LicenseManagerPluginImplTest {

    private LicenseManager licenseManagerMock = Mockito.mock(LicenseManager.class);
    private LicenseModel licenseModelMock = PowerMockito.mock(LicenseModel.class);

    @Test
    public void testRetrieveLicenseModel() throws APPCException {
        LicenseManagerPluginImpl lmImpl = new LicenseManagerPluginImpl();
        lmImpl.setLicenseManager(licenseManagerMock);
        Mockito.doReturn(licenseModelMock).when(licenseManagerMock).retrieveLicenseModel(Mockito.anyString(), Mockito.anyString());
        Map<String, String> params = new HashMap<>();
        SvcLogicContext ctx = new SvcLogicContext();
        lmImpl.retrieveLicenseModel(params, ctx);
    }

}
