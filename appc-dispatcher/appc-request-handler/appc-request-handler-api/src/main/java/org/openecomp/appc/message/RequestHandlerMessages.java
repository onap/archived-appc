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

package org.openecomp.appc.message;


public class RequestHandlerMessages {
    public final static String  VNF_WORKING_STATE_UPDATED = "VNF WorkingState for vnfId ${vnfId} was updated to ${workingState} at attempt ${attempt} out of ${maxAttempts} with ownerId = ${ownerId} and forceFlag = ${forceFlag}";
    public final static String  VNF_WORKING_STATE_WAS_NOT_UPDATED = "VNF WorkingState for vnfId ${vnfId} was not updated to ${workingState} attempt ${attempt} out of ${maxAttempts} with ownerId = ${ownerId} and forceFlag = ${forceFlag}";
}
