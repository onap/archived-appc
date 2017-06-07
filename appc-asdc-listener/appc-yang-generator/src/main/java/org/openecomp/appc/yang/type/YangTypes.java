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

package org.openecomp.appc.yang.type;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class YangTypes {

    private static final Map<String, String> yangTypeMap;
    private YangTypes(){}
    static {
        Map<String, String> typeMap = new HashMap<>();

        /* standard Types */
      /*  typeMap.put("bits","bits");
        typeMap.put("leafref","leafref");
        typeMap.put("decimal64","decimal64");
        typeMap.put("enumeration","enumeration");
        typeMap.put("identityref","identityref");
        typeMap.put("union","union");*/
        typeMap.put("binary","binary");
        typeMap.put("boolean","boolean");
        typeMap.put("empty","empty");
        typeMap.put("instance-identifier","instance-identifier");
        typeMap.put("int8","int8");
        typeMap.put("int16","int16");
        typeMap.put("int32","int32");
        typeMap.put("int64","int64");
        typeMap.put("string","string");
        typeMap.put("uint8","uint8");
        typeMap.put("uint16","uint16");
        typeMap.put("uint32","uint32");
        typeMap.put("uint64","uint64");


        /* ietf-yang-types */

        typeMap.put("counter32","yang:counter32");
        typeMap.put("zero-based-counter32","yang:zero-based-counter32");
        typeMap.put("counter64","yang:counter64");
        typeMap.put("zero-based-counter64","yang:zero-based-counter64");
        typeMap.put("gauge32","yang:gauge32");
        typeMap.put("gauge64","yang:gauge64");
        typeMap.put("object-identifier","yang:object-identifier");
        typeMap.put("object-identifier-128","yang:object-identifier-128");
        typeMap.put("yang-identifier","yang:yang-identifier");
        typeMap.put("date-and-time","yang:date-and-time");
        typeMap.put("timeticks","yang:timeticks");
        typeMap.put("timestamp","yang:timestamp");
        typeMap.put("phys-address","yang:phys-address");
        typeMap.put("mac-address","yang:mac-address");
        typeMap.put("xpath1.0","yang:xpath1.0");
        typeMap.put("hex-string","yang:hex-string");
        typeMap.put("uuid","yang:uuid");
        typeMap.put("dotted-quad","yang:dotted-quad");

        /* ietf-inet-types */

        typeMap.put("ip-version","inet:ip-version");
        typeMap.put("dscp","inet:dscp");
        typeMap.put("ipv6-flow-label","inet:ipv6-flow-label");
        typeMap.put("port-number","inet:port-number");
        typeMap.put("as-number","inet:as-number");
        typeMap.put("ip-address","inet:ip-address");
        typeMap.put("ipv4-address","inet:ipv4-address");
        typeMap.put("ipv6-address","inet:ipv6-address");
        typeMap.put("ip-address-no-zone","inet:ip-address-no-zone");
        typeMap.put("ipv4-address-no-zone","inet:ipv4-address-no-zone");
        typeMap.put("ipv6-address-no-zone","inet:ipv6-address-no-zone");
        typeMap.put("ip-prefix","inet:ip-prefix");
        typeMap.put("ipv4-prefix","inet:ipv4-prefix");
        typeMap.put("ipv6-prefix","inet:ipv6-prefix");
        typeMap.put("domain-name","inet:domain-name");
        typeMap.put("host","inet:host");
        typeMap.put("uri","inet:uri");

        yangTypeMap = Collections.unmodifiableMap(typeMap);
    }

    public static Map<String, String> getYangTypeMap(){
        return yangTypeMap;
    }

}
