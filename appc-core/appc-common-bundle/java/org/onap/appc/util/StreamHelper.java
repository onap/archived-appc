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


package org.onap.appc.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class StreamHelper {

    /**
     * private default constructor prevents instantiation
     */
    private StreamHelper() {
    }

    /**
     * @param inputStream
     * @return Input stream converted to string
     */
    public static String getStringFromInputStream(InputStream inputStream) {
        StringBuffer buffer = new StringBuffer();
        byte[] array = new byte[4096];

        if (inputStream != null) {
            try {
                int len = inputStream.read(array);
                while (len != -1) {
                    buffer.append(new String(array, 0, len, Charset.forName("UTF-8")));
                    len = inputStream.read(array);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return buffer.toString();
    }

}
