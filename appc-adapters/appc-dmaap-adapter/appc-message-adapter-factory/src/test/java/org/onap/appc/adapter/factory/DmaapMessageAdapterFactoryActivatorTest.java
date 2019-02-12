package org.onap.appc.adapter.factory;

import org.junit.Test;
import org.mockito.Mockito;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.powermock.reflect.Whitebox;


public class DmaapMessageAdapterFactoryActivatorTest {

    @Test
    public void testStart() throws Exception {
        DmaapMessageAdapterFactoryActivator activator = new DmaapMessageAdapterFactoryActivator();
        BundleContext context = Mockito.mock(BundleContext.class);
        activator.start(context);
        Mockito.verify(context).registerService(Mockito.anyString(), Mockito.any(), Mockito.any());
    }

    @Test
    public void testStop() throws Exception {
        DmaapMessageAdapterFactoryActivator activator = new DmaapMessageAdapterFactoryActivator();
        ServiceRegistration registration = Mockito.mock(ServiceRegistration.class);
        Whitebox.setInternalState(activator, "registration", registration);
        activator.stop(Mockito.mock(BundleContext.class));
        Mockito.verify(registration).unregister();
    }
}
