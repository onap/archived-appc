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

package org.openecomp.appc.util;

import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MessageFormatter {
    private final static String paramNameRegexGroupName = "paramName";
    private final static String paramRegex = "\\$\\{(?<paramName>[^}$]+)\\}"; //start with ${ and after there is one or more characters that are not $ and not } and ended with }


    public static  String format(String messageTemplate, Map<String,Object> params) {
        if (StringUtils.isEmpty(messageTemplate))
            return "";
        if (params == null || params.isEmpty())
            return messageTemplate;

        String formattedMessage = messageTemplate;
        if (formattedMessage.contains("$")) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                formattedMessage = formattedMessage.replaceAll("\\$\\{" + entry.getKey() + "\\}", String.valueOf(entry.getValue()));
            }
        }

        return formattedMessage;
    }

    public static List<String> getParamsNamesList(String messageTemplate) {
        List<String> paramsNames = null;
        if(!StringUtils.isEmpty(messageTemplate)){
            paramsNames = new ArrayList<String>();
            Matcher m = Pattern.compile(paramRegex).matcher(messageTemplate);
            while (m.find()) {
                String paramName = m.group(paramNameRegexGroupName);
                paramsNames.add(paramName);
            }
        }
        return paramsNames;
    }
    public static Set<String> getParamsNamesSet(String messageTemplate) {
        List<String> paramsNamesList = getParamsNamesList(messageTemplate);
        Set<String> paramsNamesSet = null;
        if(paramsNamesList != null && !paramsNamesList.isEmpty()){
            paramsNamesSet = new HashSet<String>();
            for(String paramName : paramsNamesList){
                paramsNamesSet.add(paramName);
            }
        }
        return paramsNamesSet;
    }
}
