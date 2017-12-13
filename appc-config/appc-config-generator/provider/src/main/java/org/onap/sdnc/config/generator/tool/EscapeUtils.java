/*-
 * ============LICENSE_START=======================================================
 * ONAP : APP-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property.  All rights reserved.
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
 */

package org.openecomp.sdnc.config.generator.tool;

import org.apache.commons.lang3.StringUtils;

public class EscapeUtils {

    public EscapeUtils() {
        // TODO Auto-generated constructor stub
    }

    public static String escapeSql(String str) {
        if (str == null) {
            return null;
        }
        String searchList[] = new String[]{"'","\\"};
        String replacementList[] = new String[]{ "''","\\\\"};
        return StringUtils.replaceEach(str,searchList, replacementList);
    }
    
    public static String unescapeSql(String str) {
        if (str == null) {
            return null;
        }
        
        String searchList[] = new String[] {"''"};
        String replacementList[] = new String[] {"'"};
        return StringUtils.replaceEach(str, searchList, replacementList);
    }


    // For Generic Purpose
    public static String escapeSQL(String s) {
        if (s == null) {
            return null;
        }

        int length = s.length();
        int newLength = length;
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            switch (c) {
            case '\\':
            case '\"':
            case '\'':
            case '\0': {
                newLength += 1;
            }
                break;
            }
        }
        if (length == newLength) {
            // nothing to escape in the string
            return s;
        }
        StringBuffer sb = new StringBuffer(newLength);
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            switch (c) {
            case '\\': {
                sb.append("\\\\");
            }
                break;
            case '\"': {
                sb.append("\\\"");
            }
                break;
            case '\'': {
                sb.append("\\\'");
            }
                break;
            case '\0': {
                sb.append("\\0");
            }
                break;
            default: {
                sb.append(c);
            }
            }
        }
        return sb.toString();
    }
}
