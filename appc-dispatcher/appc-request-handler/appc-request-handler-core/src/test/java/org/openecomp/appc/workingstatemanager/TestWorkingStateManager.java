/*-
 * ============LICENSE_START=======================================================
 * APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Amdocs
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */

package org.openecomp.appc.workingstatemanager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openecomp.appc.configuration.ConfigurationFactory;
import org.openecomp.appc.dao.util.AppcJdbcConnectionFactory;
import org.openecomp.appc.workingstatemanager.impl.WorkingStateManagerImpl;
import org.openecomp.appc.workingstatemanager.objects.VNFWorkingState;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

import java.util.UUID;



public class TestWorkingStateManager {
    private static final EELFLogger logger = EELFManager.getInstance().getLogger(TestWorkingStateManager.class);
    WorkingStateManagerImpl workingStateManager;

    @Before
    public void init() throws Exception {
        workingStateManager = new WorkingStateManagerImpl();
        AppcJdbcConnectionFactory appcJdbcConnectionFactory = new AppcJdbcConnectionFactory();
        String schema = "sdnctl";
        appcJdbcConnectionFactory.setSchema(schema);
        workingStateManager.setConnectionFactory(appcJdbcConnectionFactory);
        String property = ConfigurationFactory.getConfiguration().getProperty(String.format("org.openecomp.appc.db.url.%s", schema));
        logger.info(property+" will be used as connection URL to mySQL.");
        logger.warn("you can set connection URL to other IP by adding -DmysqlIp=<MYSQL_IP> in VM Option");
//        System.getProperties().getProperty("mys")
    }

    @Test
    // this test run on mysql you need to uncomment Ignore and to add -DmysqlIp=<MYSQL_IP> in VM Option, to make that test pass successfully.
    @Ignore
    public void testUpdateWorkingState() {
        String vnfId = UUID.randomUUID().toString();
        String myOwnerId = "myOwnerId";
        String otherOwnerId = "otherOwnerId";
        boolean vnfStable = workingStateManager.isVNFStable(vnfId);
        logger.info("isVNFStable returns "+vnfStable+" for vnfId "+vnfId);

        //set to unstable with force true
        boolean updated = workingStateManager.setWorkingState(vnfId, VNFWorkingState.UNSTABLE, myOwnerId, true);
        Assert.assertTrue(updated);
        Assert.assertFalse(workingStateManager.isVNFStable(vnfId));

        //negative test - try to set to any value by other ownerId when vnf state is UNSTABLE
        updated = workingStateManager.setWorkingState(vnfId, VNFWorkingState.UNSTABLE, otherOwnerId, false);
        Assert.assertFalse(updated);
        updated = workingStateManager.setWorkingState(vnfId, VNFWorkingState.UNKNOWN, otherOwnerId, false);
        Assert.assertFalse(updated);
        updated = workingStateManager.setWorkingState(vnfId, VNFWorkingState.STABLE, otherOwnerId, false);
        Assert.assertFalse(updated);

        //positive test - set with same ownerId and force false
        updated = workingStateManager.setWorkingState(vnfId, VNFWorkingState.UNSTABLE, myOwnerId, false);
        Assert.assertTrue(updated);
        Assert.assertFalse(workingStateManager.isVNFStable(vnfId));
        updated = workingStateManager.setWorkingState(vnfId, VNFWorkingState.UNKNOWN, myOwnerId, false);
        Assert.assertTrue(updated);
        Assert.assertFalse(workingStateManager.isVNFStable(vnfId));
        updated = workingStateManager.setWorkingState(vnfId, VNFWorkingState.STABLE, myOwnerId, false);
        Assert.assertTrue(updated);
        Assert.assertTrue(workingStateManager.isVNFStable(vnfId));

        //positive test - set with otherOwnerId and force false when VNF is stable
        updated = workingStateManager.setWorkingState(vnfId, VNFWorkingState.UNKNOWN, otherOwnerId, false);
        Assert.assertTrue(updated);
        Assert.assertFalse(workingStateManager.isVNFStable(vnfId));

        //negative test - try to set to any value by myOwnerId when vnf state is UNKNOWN
        updated = workingStateManager.setWorkingState(vnfId, VNFWorkingState.UNSTABLE, myOwnerId, false);
        Assert.assertFalse(updated);
        updated = workingStateManager.setWorkingState(vnfId, VNFWorkingState.UNKNOWN, myOwnerId, false);
        Assert.assertFalse(updated);
        updated = workingStateManager.setWorkingState(vnfId, VNFWorkingState.STABLE, myOwnerId, false);
        Assert.assertFalse(updated);

        //positive test - try to set to any value by myOwnerId when vnf state is UNKNOWN but with force
        updated = workingStateManager.setWorkingState(vnfId, VNFWorkingState.UNSTABLE, myOwnerId, true);
        Assert.assertTrue(updated);
        Assert.assertFalse(workingStateManager.isVNFStable(vnfId));
    }


}
