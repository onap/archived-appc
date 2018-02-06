package org.onap.appc.adapter.chef;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.only;

import java.util.Dictionary;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.appc.adapter.chef.impl.ChefAdapterImpl;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

@RunWith(MockitoJUnitRunner.class)
public class ChefActivatorTest {

    @Mock
    private ServiceRegistration<ChefAdapter> serviceRegistration;
    @Mock
    private BundleContext bundleContext;

    private ChefActivator chefActivator = new ChefActivator();

    @Before
    public void setUp() {
        given(bundleContext.registerService(eq(ChefAdapter.class), isA(ChefAdapterImpl.class), isNull(
            Dictionary.class))).willReturn(serviceRegistration);
    }

    @Test
    public void start_shouldRegisterService_whenRegistrationOccursForTheFirstTime() throws Exception {
        registerService();

        then(bundleContext).should(only())
            .registerService(eq(ChefAdapter.class), isA(ChefAdapterImpl.class), isNull(
                Dictionary.class));
    }

    @Test
    public void start_shouldRegisterServiceOnlyOnce_whenServiceRegistrationIsNotNull() throws Exception {
        // GIVEN
        registerService();

        // WHEN
        registerService();

        // THEN
        then(bundleContext).should(only()).registerService(eq(ChefAdapter.class), isA(ChefAdapterImpl.class), isNull(
            Dictionary.class));
    }

    @Test
    public void stop_shouldUnregisterService_whenServiceRegistrationObjectIsNotNull() throws Exception {
        // GIVEN
        registerService();

        // WHEN
        unregisterService();

        // THEN
        then(serviceRegistration).should().unregister();
    }

    @Test
    public void stop_shouldNotAttemptToUnregisterService_whenServiceHasAlreadyBeenUnregistered()
        throws Exception {
        // GIVEN
        registerService();
        unregisterService();

        // WHEN
        unregisterService();

        // THEN
        then(serviceRegistration).should(only()).unregister();
    }

    private void registerService() throws Exception {
        chefActivator.start(bundleContext);
    }

    private void unregisterService() throws Exception {
        chefActivator.stop(bundleContext);
    }
}