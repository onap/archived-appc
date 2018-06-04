/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
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

package org.onap.appc.metadata.objects;

import static org.onap.appc.metadata.objects.DependencyModelIdentifier.prime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

public class DependencyModelIdentifierTest {
    private static final String vnfType = "vnfType";
    private static final String vnfType2 = "vnfType2";
    private static final String cVersion = "catalogVersion";
    private DependencyModelIdentifier identifier;
    private DependencyModelIdentifier identifier1;
    private DependencyModelIdentifier identifier2;
    private DependencyModelIdentifier identifier3;

    @Before
    public void setUp() throws Exception {
        identifier = new DependencyModelIdentifier(vnfType, cVersion);
        identifier1 = new DependencyModelIdentifier(null, null);
        identifier2 = new DependencyModelIdentifier(vnfType, null);
        identifier3 = new DependencyModelIdentifier(null, cVersion);
    }

    @Test
    public void testConstructorAndGetterAndToString() throws Exception {
        Assert.assertEquals(vnfType, Whitebox.getInternalState(identifier, "vnfType"));
        Assert.assertEquals(cVersion, Whitebox.getInternalState(identifier, "catalogVersion"));

        Assert.assertEquals(vnfType, identifier.getVnfType());
        Assert.assertEquals(cVersion, identifier.getCatalogVersion());

        Assert.assertEquals(
                String.format(DependencyModelIdentifier.TO_STRING_FORMAT, vnfType, cVersion),
                identifier.toString());
    }

    @Test
    public void testHashCode() throws Exception {
        Assert.assertEquals((prime + vnfType.hashCode()) * prime + cVersion.hashCode(),
                identifier.hashCode());
        Assert.assertEquals(prime * prime, identifier1.hashCode());
        Assert.assertEquals((prime + vnfType.hashCode()) * prime, identifier2.hashCode());
        Assert.assertEquals(prime * prime + cVersion.hashCode(), identifier3.hashCode());
    }

    @Test
    public void testEquals() throws Exception {
        // other object is null
        Assert.assertFalse(identifier.equals(null));
        // other object is wrong data type
        Assert.assertFalse(identifier.equals("abc"));

        // my vnfType is null
        Assert.assertFalse(identifier1.equals(identifier));
        // different vnfType
        DependencyModelIdentifier identifier4 = new DependencyModelIdentifier(vnfType2, cVersion);
        Assert.assertFalse(identifier.equals(identifier4));
        // same vnfType, my catalogVerson is null
        Assert.assertFalse(identifier2.equals(identifier));
        // same vnfType and both catalogVersion are null
        identifier4 = new DependencyModelIdentifier(vnfType, null);
        Assert.assertTrue(identifier2.equals(identifier4));

        Assert.assertFalse(identifier.equals(identifier1));
        Assert.assertFalse(identifier.equals(identifier2));
        Assert.assertFalse(identifier.equals(identifier3));

        Assert.assertFalse(identifier2.equals(identifier1));
        Assert.assertFalse(identifier2.equals(identifier3));


        Assert.assertFalse(identifier3.equals(identifier));
        Assert.assertFalse(identifier3.equals(identifier1));
        Assert.assertFalse(identifier3.equals(identifier2));

        identifier4 = new DependencyModelIdentifier(vnfType, cVersion);
        Assert.assertTrue(identifier.equals(identifier4));
    }

}
