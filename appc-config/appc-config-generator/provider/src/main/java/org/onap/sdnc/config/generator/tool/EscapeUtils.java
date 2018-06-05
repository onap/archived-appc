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

package org.onap.sdnc.config.generator.tool;

import org.apache.commons.lang3.StringUtils;

public class EscapeUtils {

    private EscapeUtils() {}

    public static String escapeSql(String str) {
        if (str == null) {
            return null;
        }
        String[] searchList = new String[]{"'", "\\"};
        String[] replacementList = new String[]{"''", "\\\\"};
        return StringUtils.replaceEach(str, searchList, replacementList);
    }

    // Added for E2E defect 266908 Quote issue
    public static String unescapeSql(String str) {
        if (str == null) {
            return null;
        }
        String[] searchList = new String[]{"''"};
        String[] replacementList = new String[]{"'"};
        return StringUtils.replaceEach(str, searchList, replacementList);
    }


    // For Generic Purpose
    public static String escapeString(String s) {
        if (s == null) {
            return null;
        }
        int length = s.length();
        int newLength = length;
        for (char ch : s.toCharArray()) {
            switch (ch) {
                case '\\':
                case '\"':
                case '\'':
                case '\0':
                    newLength += 1;
                    break;
                default:
                    break;
            }
        }
        if (length == newLength) {
            // nothing to escape in the string
            return s;
        }
        StringBuilder builder = new StringBuilder(newLength);
        for (char ch : s.toCharArray()) {
            switch (ch) {
                case '\\':
                    builder.append("\\\\");
                    break;
                case '\"':
                    builder.append("\\\"");
                    break;
                case '\'':
                    builder.append("\\\'");
                    break;
                case '\0':
                    builder.append("\\0");
                    break;
                default:
                    builder.append(ch);
            }
        }
        return builder.toString();
    }
}
