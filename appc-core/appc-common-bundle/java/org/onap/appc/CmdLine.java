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

    package org.onap.appc;

import org.onap.appc.encryption.EncryptionTool;

public class CmdLine {

        public static void main(String[] args) {

            if (args.length < 1) {
                printUsage();
                return;
            }

            String command = args[0];//first parameter

            if (0 == command.compareTo("encrypt") && args.length == 2)//two parameters are required
            {
                String clearText = args[1];
                String encrypted = EncryptionTool.getInstance().encrypt(clearText);
                System.out.println(encrypted);
                return;
            } else {
                printUsage();
            }
        }
        
        private static void printUsage(){
            System.out.println("Usage: java -jar <this jar> ...");
            System.out.println("\tencrypt <your text> \t\t(Encrypts your text)");
        }
}