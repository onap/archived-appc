/*-
 * ============LICENSE_START=======================================================
 *  ONAP : APPC
 * ================================================================================
 *  Copyright (C) 2016-2018 Ericsson. All rights reserved.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.sdnc.config.params.transformer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.onap.sdnc.config.params.transformer.tosca.TestGenerateArtifactString;

/**
 * This class is used as a utility class to support the test cases.
 */
public class CommonUtility {

    public static String getFileContent(String fileName) throws IOException {
        ClassLoader classLoader = new TestGenerateArtifactString().getClass().getClassLoader();
        return getFileContent(classLoader.getResource(fileName).getFile());
    }

    public static String getFileContent(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        BufferedReader buf = new BufferedReader(new InputStreamReader(is));
        String line = buf.readLine();
        StringBuilder sb = new StringBuilder();

        while (line != null) {
            sb.append(line).append("\n");
            line = buf.readLine();
        }
        String fileString = sb.toString();
        is.close();
        return fileString;
    }
}
