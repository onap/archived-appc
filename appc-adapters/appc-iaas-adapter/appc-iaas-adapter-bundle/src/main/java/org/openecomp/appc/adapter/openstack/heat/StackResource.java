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

package org.openecomp.appc.adapter.openstack.heat;

import org.openecomp.appc.adapter.openstack.heat.model.CreateSnapshotParams;
import org.openecomp.appc.adapter.openstack.heat.model.Snapshot;

import com.woorea.openstack.base.client.Entity;
import com.woorea.openstack.base.client.HttpMethod;
import com.woorea.openstack.base.client.OpenStackClient;
import com.woorea.openstack.base.client.OpenStackRequest;
import com.woorea.openstack.heat.model.Stack;


public class StackResource {

    private final OpenStackClient client;

    public StackResource(OpenStackClient client) {
        this.client = client;
    }

    public ShowStack show(String stackName, String stackID) {
        return new ShowStack(stackName, stackID);
    }

    public class ShowStack extends OpenStackRequest<Stack> {
        public ShowStack(String stackName, String stackID) {
            super(client, HttpMethod.GET, "/stacks/" + stackName + "/" + stackID, null, Stack.class);
        }
    }
}
