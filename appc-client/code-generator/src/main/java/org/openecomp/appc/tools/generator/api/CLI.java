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

package org.openecomp.appc.tools.generator.api;

import org.openecomp.appc.tools.generator.impl.ModelGenerator;

public class CLI {
    public static void main(String... args) throws Exception {
        String sourceFile = args[0];
        if(sourceFile == null)
            throw new IllegalArgumentException("Source file is missing. Please add argument 'client <source file> <destination file> <model template>'");

        String destinationFile = args[1];
        if(destinationFile == null)
            throw new IllegalArgumentException("Destination file name is missing. Please add argument 'client "
                    + sourceFile
                    + "<destination> <model template> <builder> <conf file>'");

        String templateFile = args[2];
        if(templateFile == null)
            throw new IllegalArgumentException("template file name is missing. Please add argument 'client "
                    + sourceFile
                    + destinationFile
                    + " <model template> <builder> <conf file>'");

        String builderName = args[3];
        if(builderName == null)
            throw new IllegalArgumentException("builder FQDN is missing. Please add argument 'client "
                    + sourceFile
                    + destinationFile
                    + templateFile
                    + " <builder> <conf file>'");
        String contextConfName = args[4];
        if(contextConfName == null)
            throw new IllegalArgumentException("context conf file is missing. Please add argument 'client "
                    + sourceFile
                    + destinationFile
                    + templateFile
                    + builderName
                    + " <conf file>'");
        ModelGenerator generator = new ModelGenerator();
        generator.execute(sourceFile, destinationFile, templateFile, builderName, contextConfName);
    }
}
