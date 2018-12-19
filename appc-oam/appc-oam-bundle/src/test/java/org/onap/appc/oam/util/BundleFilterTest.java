/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 Ericsson
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
