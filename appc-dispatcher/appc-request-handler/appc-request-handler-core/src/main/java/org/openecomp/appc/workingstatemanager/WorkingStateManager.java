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

import org.openecomp.appc.requesthandler.exceptions.VNFNotFoundException;
import org.openecomp.appc.workingstatemanager.objects.VNFWorkingState;


public interface WorkingStateManager {

    /**
     * Return true if vnf state exists in working state map and state is STABLE else return false. If vnf does not exists in working state map throws vnf not found  exception.
     * @param vnfId vnf Id to be verified for stable state
     * @return True if vnf Exists and state is STABLE else False.
     */
    public boolean isVNFStable(String vnfId);

    /**
     * Updates working state for given vnf Id. Returns true if update was allowed and succeeded. Update will success only if the existing vnf state is 'STABLE' or
     * if the registered ownerId is equal to the given ownerId or if the forceFlag is true.
     * Note on case of simultaneously updates the latest updates will be failed, and another attempts will be done after refetching the updated data from persistent store.
     * @param vnfId vnf Id to be updated
     * @param workingState new working state
     * @param ownerId
     * @param forceFlag - force to update also on case given onwerId is different then the registered one
     */
    public boolean setWorkingState(String vnfId, VNFWorkingState workingState, String ownerId, boolean forceFlag);

}
