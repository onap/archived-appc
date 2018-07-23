package org.onap.appc.dg.util.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.ccsdk.sli.adaptors.aai.AAIService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

@RunWith(MockitoJUnitRunner.class)
public class AAIServiceFactoryTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AAIServiceFactory.FrameworkUtilWrapper frameworkUtilWrapper = new AAIServiceFactory.FrameworkUtilWrapper();

    @InjectMocks
    private AAIServiceFactory aaiServiceFactory;

    @Test
    public void getAAIservice_shouldLookupAAIService_fromBundle() {
        // GIVEN
        AAIService expectedAAIService = mock(AAIService.class);

        BundleContext mockedBundleContext = mock(BundleContext.class);
        ServiceReference mockedServiceReference = mock(ServiceReference.class);
        given(frameworkUtilWrapper.getBundle(AAIService.class).getBundleContext()).willReturn(mockedBundleContext);
        given(mockedBundleContext.getServiceReference(AAIService.class.getName())).willReturn(mockedServiceReference);
        given(mockedBundleContext.getService(mockedServiceReference)).willReturn(expectedAAIService);

        // WHEN
        AAIService resultAAIService = aaiServiceFactory.getAAIService();

        // THEN
        assertThat(resultAAIService).isNotNull().isEqualTo(expectedAAIService);
    }

    @Test
    public void getAAIservice_shouldNotLookupAAIService_forNullServiceReference() {
        // GIVEN
        BundleContext mockedBundleContext = mock(BundleContext.class);
        given(frameworkUtilWrapper.getBundle(AAIService.class).getBundleContext()).willReturn(mockedBundleContext);
        given(mockedBundleContext.getServiceReference(AAIService.class.getName())).willReturn(null);

        // WHEN
        AAIService resultAAIService = aaiServiceFactory.getAAIService();

        // THEN
        assertThat(resultAAIService).isNull();
        then(mockedBundleContext).should(never()).getService(any(ServiceReference.class));
    }
}