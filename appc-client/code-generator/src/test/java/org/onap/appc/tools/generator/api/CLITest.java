/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.tools.generator.api;

import org.junit.Assert;
import org.junit.Test;

public class CLITest {
    @Test
    public void missingSourceFileTest()  {
        CLI cli = new CLI();
        try {
            String[] input = new String[1];
            cli.main(input);
        } catch (Exception e) {
            Assert.assertEquals("Source file is missing. Please add argument 'client <source file> <destination file> <model template>'",e.getMessage());
        }
    }
    @Test
    public void missingDestinationFileTest()  {
        CLI cli = new CLI();
        try {
            String[] input = {"sourceFilePath",null};
            cli.main(input);
        } catch (Exception e) {
            Assert.assertEquals("Destination file name is missing. Please add argument 'client sourceFilePath <destination> <model template> <builder> <conf file>'",e.getMessage());
        }
    }
    @Test
    public void missingTemplateFileTest()  {
        CLI cli = new CLI();
        try {
            String[] input = {"sourceFilePath","destinationPath",null};
            cli.main(input);
        } catch (Exception e) {
            Assert.assertEquals("template file name is missing. Please add argument 'client sourceFilePath destinationPath <model template> <builder> <conf file>'",e.getMessage());
        }
    }
    @Test
    public void missingBuilderNameTest()  {
        CLI cli = new CLI();
        try {
            String[] input = {"sourceFilePath","destinationPath","templateFileName",null};
            cli.main(input);
        } catch (Exception e) {
            Assert.assertEquals("builder FQDN is missing. Please add argument 'client sourceFilePath destinationPath templateFileName <builder> <conf file>'",e.getMessage());
        }
    }
    @Test
    public void missingContextConfFileNameTest()  {
        CLI cli = new CLI();
        try {
            String[] input = {"sourceFilePath","destinationPath","templateFileName","builderFQDN",null};
            cli.main(input);
        } catch (Exception e) {
            Assert.assertEquals(e.getMessage(),"context conf file is missing. Please add argument 'client sourceFilePath destinationPath templateFileName builderFQDN <conf file>'");
        }
    }
}
