/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.requesthandler.helper;

import org.onap.appc.domainmodel.lcm.RuntimeContext;
import org.onap.appc.executor.UnstableVNFException;
import org.onap.appc.lifecyclemanager.objects.LifecycleException;
import org.onap.appc.lifecyclemanager.objects.NoTransitionDefinedException;
import org.onap.appc.requesthandler.exceptions.DGWorkflowNotFoundException;
import org.onap.appc.requesthandler.exceptions.DuplicateRequestException;
import org.onap.appc.requesthandler.exceptions.InvalidInputException;
import org.onap.appc.requesthandler.exceptions.LCMOperationsDisabledException;
import org.onap.appc.requesthandler.exceptions.MissingVNFDataInAAIException;
import org.onap.appc.requesthandler.exceptions.RequestExpiredException;
import org.onap.appc.requesthandler.exceptions.VNFNotFoundException;
import org.onap.appc.requesthandler.exceptions.WorkflowNotFoundException;

public interface RequestValidator {
    public void validateRequest(RuntimeContext runtimeContext) throws VNFNotFoundException, RequestExpiredException, UnstableVNFException, InvalidInputException, DuplicateRequestException, NoTransitionDefinedException, LifecycleException, WorkflowNotFoundException, DGWorkflowNotFoundException, MissingVNFDataInAAIException, LCMOperationsDisabledException;
}
