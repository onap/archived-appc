package org.onap.sdnc.config.generator.tool;

/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 IBM Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR ConstantsDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ============LICENSE_END=========================================================
 */

import org.junit.Assert;
import org.junit.Test;

public class TestConstants {

    @Test
    public void testConstants() {

        Assert.assertEquals("org.onap.ccsdk.sli.core.dblib.DBResourceManager", Constants.DBLIB_SERVICE);
        Assert.assertEquals("DEVICE_AUTHENTICATION", Constants.DEVICE_AUTHENTICATION);
        Assert.assertEquals("SDNCTL", Constants.SCHEMA_SDNCTL);
        Assert.assertEquals("/opt/onap/appc/data/properties", Constants.APPC_CONFIG_DIR);
    }
}