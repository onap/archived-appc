package org.onap.appc.oam.util;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;

public class BundleFilterTest {

    @Test
    public void testBundleFilter() {
        Bundle mockBundle1 = Mockito.mock(Bundle.class);
        Bundle mockBundle2 = Mockito.mock(Bundle.class);
        Mockito.doReturn("Bundle-regex1").when(mockBundle1).getSymbolicName();
        Mockito.doReturn("Bundle-regex2").when(mockBundle2).getSymbolicName();
        Bundle[] bundles = {mockBundle1, mockBundle2};
        String[] stopRegex = {"Bundle-regex1"};
        String[] exceptRegex = {"NOT_MATCHING_REGEX"};
        BundleFilter mockBundleFilter = Mockito.spy(new BundleFilter(stopRegex, exceptRegex, bundles));
        assertEquals(1, mockBundleFilter.getBundlesToStop().size());
        assertEquals(1, mockBundleFilter.getBundlesToNotStop().size());
    }

}
