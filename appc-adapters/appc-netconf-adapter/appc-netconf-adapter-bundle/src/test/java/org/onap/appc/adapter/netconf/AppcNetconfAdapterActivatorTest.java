package org.onap.appc.adapter.netconf;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.appc.adapter.netconf.internal.NetconfDataAccessServiceImpl;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.powermock.reflect.Whitebox;

public class AppcNetconfAdapterActivatorTest {

    private BundleContext bundle;

    @Before
    public void setup() {
        bundle = Mockito.mock(BundleContext.class);
    }

    @Test
    public void testStart() throws Exception {
        AppcNetconfAdapterActivator activator = new AppcNetconfAdapterActivator();
        activator.start(bundle);
        Mockito.verify(bundle, Mockito.times(2)).registerService(Mockito.any(Class.class),
                Mockito.any(NetconfDataAccessServiceImpl.class), Mockito.any());
    }

    @Test
    public void testStop() throws Exception {
        AppcNetconfAdapterActivator activator = Mockito.spy(new AppcNetconfAdapterActivator());
        ServiceRegistration registration = Mockito.mock(ServiceRegistration.class);
        ServiceRegistration reporterRegistration = Mockito.mock(ServiceRegistration.class);
        ServiceRegistration factoryRegistration = Mockito.mock(ServiceRegistration.class);
        ServiceRegistration dbRegistration = Mockito.mock(ServiceRegistration.class);
        Whitebox.setInternalState(activator, "registration", registration);
        Whitebox.setInternalState(activator, "reporterRegistration", reporterRegistration);
        Whitebox.setInternalState(activator, "factoryRegistration", factoryRegistration);
        Whitebox.setInternalState(activator, "dbRegistration", dbRegistration);
        activator.stop(bundle);
        Mockito.verify(dbRegistration).unregister();
    }
}
