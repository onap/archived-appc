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

package org.openecomp.appc.dg.util;

import java.util.Map;

import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.sdnc.sli.SvcLogicContext;
import org.openecomp.sdnc.sli.SvcLogicJavaPlugin;


public interface  ExecuteNodeAction extends SvcLogicJavaPlugin {
     void getResource(Map<String, String> params, SvcLogicContext ctx) throws APPCException;

     void postResource(Map<String, String> params, SvcLogicContext ctx) throws APPCException;

     void deleteResource(Map<String, String> params, SvcLogicContext ctx) throws APPCException;

     void getVnfHierarchy(Map<String, String> params, SvcLogicContext ctx) throws APPCException;
     void waitMethod(Map<String, String> params, SvcLogicContext ctx) throws APPCException;
}
