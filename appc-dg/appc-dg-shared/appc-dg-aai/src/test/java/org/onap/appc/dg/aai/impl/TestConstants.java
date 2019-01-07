/*
* ============LICENSE_START=======================================================
* ONAP : APPC
* ================================================================================
* Copyright 2018 TechMahindra
* ================================================================================
* Modifications Copyright (C) 2019 Ericsson
*=================================================================================
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* ============LICENSE_END=========================================================
*/
package org.onap.appc.dg.aai.impl;

import org.junit.Assert;
import org.junit.Test;

import org.onap.appc.dg.aai.impl.Constants.SDC_ARTIFACTS_FIELDS;

public class TestConstants {
        private SDC_ARTIFACTS_FIELDS c=SDC_ARTIFACTS_FIELDS.SERVICE_NAME;

        @Test
        public void testConstants() {
            Assert.assertEquals("sdnctl",Constants.NETCONF_SCHEMA);
        }

        @Test
        public void testName() {
            Assert.assertEquals("SERVICE_NAME",c.name());
        }

        @Test
        public void testEqual() {
            Assert.assertTrue(c.equals(SDC_ARTIFACTS_FIELDS.SERVICE_NAME));
            Assert.assertFalse(c.equals(null));
        }
}
