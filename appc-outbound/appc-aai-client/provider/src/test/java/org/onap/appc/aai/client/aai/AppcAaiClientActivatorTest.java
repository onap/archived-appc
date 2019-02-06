package org.onap.appc.aai.client.aai;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.appc.aai.client.AppcAaiClientActivator;
import org.osgi.framework.BundleContext;

@RunWith(MockitoJUnitRunner.class)
public class AppcAaiClientActivatorTest {
	
	@InjectMocks
	AppcAaiClientActivator appcAaiClientActivator;
	
	@Mock
	BundleContext ctx;

	@Test
	public void startTest() throws Exception {
		appcAaiClientActivator.start(ctx);
	}
	
	@Test
	public void stopTest() throws Exception {
		appcAaiClientActivator.stop(ctx);
	}
}
