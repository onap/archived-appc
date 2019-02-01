/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 Ericsson
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
 *
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.adapter.iaas.provider.operation.impl;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.onap.appc.exceptions.APPCException;
import com.att.cdp.exceptions.ZoneException;
import com.att.cdp.zones.VolumeService;
import com.att.cdp.zones.model.ModelObject;
import com.att.cdp.zones.model.Server;
import com.att.cdp.zones.model.Server.Status;
import com.att.cdp.zones.model.Volume;


public class AttachVolumeServerTest {

    @Test
    public void attachVolumeTest() throws ZoneException, APPCException {
        MockGenerator mg = new MockGenerator(Status.RUNNING);
        Server server = mg.getServer();
        VolumeService volumeService = mock(VolumeService.class);
        List<Volume> volumeList = new ArrayList<>();
        doReturn(volumeList).when(volumeService).getVolumes();
        doReturn(mg.getContext()).when(server).getContext();
        doReturn(volumeService).when(mg.getContext()).getVolumeService();
        AttachVolumeServer rbs = new AttachVolumeServer();
        rbs.setProviderCache(mg.getProviderCacheMap());
        assertTrue(rbs.executeProviderOperation(mg.getParams(), mg.getSvcLogicContext()) instanceof ModelObject);
    }

}
