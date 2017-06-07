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

package org.openecomp.appc.requesthandler.helper;

import org.openecomp.appc.domainmodel.lcm.RuntimeContext;
import org.openecomp.appc.executor.UnstableVNFException;
import org.openecomp.appc.lifecyclemanager.objects.LifecycleException;
import org.openecomp.appc.lifecyclemanager.objects.NoTransitionDefinedException;
import org.openecomp.appc.requesthandler.exceptions.DGWorkflowNotFoundException;
import org.openecomp.appc.requesthandler.exceptions.DuplicateRequestException;
import org.openecomp.appc.requesthandler.exceptions.InvalidInputException;
import org.openecomp.appc.requesthandler.exceptions.LCMOperationsDisabledException;
import org.openecomp.appc.requesthandler.exceptions.MissingVNFDataInAAIException;
import org.openecomp.appc.requesthandler.exceptions.RequestExpiredException;
import org.openecomp.appc.requesthandler.exceptions.VNFNotFoundException;
import org.openecomp.appc.requesthandler.exceptions.WorkflowNotFoundException;

public interface RequestValidator {
    public void validateRequest(RuntimeContext runtimeContext) throws VNFNotFoundException, RequestExpiredException, UnstableVNFException, InvalidInputException, DuplicateRequestException, NoTransitionDefinedException, LifecycleException, WorkflowNotFoundException, DGWorkflowNotFoundException, MissingVNFDataInAAIException, LCMOperationsDisabledException;
}
