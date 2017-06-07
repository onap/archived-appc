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

package org.openecomp.appc.yang;

import org.openecomp.appc.yang.exception.YANGGenerationException;

import java.io.OutputStream;

/**
 * The Interface YANGGenerator - provides method to generate YANG file from TOSCA.
 */
public interface YANGGenerator {

    /**
     * Generate YANG from TOSCA.
     * if any exceptional Type is coming in the input tosca as a part of configuration parameter property, YANGGenerationException will be thrown.
     * This API is not supporting below mentioned built-in Types:
     * bits, decimal64, enumeration, identityref, leafref, union
     *
     * @param uniqueID - Set as module name in the yang, mandatory, cannot be null or empty
     * @param tosca - TOSCA String from which the YANG is to be generated, mandatory, cannot be null or empty
     * @param stream - The outputStream to which the generated yang is written, mandatory, cannot be null
     * @throws YANGGenerationException - Thrown when any error occurred during method execution, the origin can be found from ex.getCause() or ex.getMessage()
     */

    void generateYANG(String uniqueID, String tosca, OutputStream stream) throws YANGGenerationException;
}
