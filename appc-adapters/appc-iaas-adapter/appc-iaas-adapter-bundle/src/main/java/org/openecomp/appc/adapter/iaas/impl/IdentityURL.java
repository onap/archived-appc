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

package org.openecomp.appc.adapter.iaas.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is used to parse the VM URL returned from OpenStack and extract all of the constituent parts.
 */
public class IdentityURL {
    /**
     * The regular expression pattern used to parse the URL. Capturing groups are used to identify and extract the
     * various component parts of the URL.
     */
    private static Pattern pattern = Pattern.compile("(\\p{Alnum}+)://([^/:]+)(?::([0-9]+))?/(v[0-9\\.]+)/?");

    /**
     * The URL scheme or protocol, such as HTTP or HTTPS
     */
    private String scheme;

    /**
     * The host name or ip address
     */
    private String host;

    /**
     * The port number, or null if no port is defined
     */
    private String port;

    /**
     * The version of the service
     */
    private String version;

    /**
     * A private default constructor prevents instantiation by any method other than the factory method
     * 
     * @see #parseURL(String)
     */
    private IdentityURL() {

    }

    /**
     * This static method is used to parse the provided server URL string and return a parse results object (VMURL)
     * which represents the state of the parse.
     * 
     * @param serverUrl
     *            The server URL to be parsed
     * @return The VMURL parse results object, or null if the URL was not valid or null.
     */
    public static IdentityURL parseURL(String serverUrl) {
        IdentityURL obj = null;
        if (serverUrl != null) {
            Matcher matcher = pattern.matcher(serverUrl.trim());
            if (matcher.matches()) {
                obj = new IdentityURL();
                obj.scheme = matcher.group(1);
                obj.host = matcher.group(2);
                obj.port = matcher.group(3);
                obj.version = matcher.group(4);
            }
        }

        return obj;
    }

    /**
     * @return The URL scheme
     */
    public String getScheme() {
        return scheme;
    }

    /**
     * @return The URL host
     */
    public String getHost() {
        return host;
    }

    /**
     * @return The URL port, or null if no port was defined
     */
    public String getPort() {
        return port;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return String.format("%s://%s:%s/%s", scheme, host, port, version);
    }

}
