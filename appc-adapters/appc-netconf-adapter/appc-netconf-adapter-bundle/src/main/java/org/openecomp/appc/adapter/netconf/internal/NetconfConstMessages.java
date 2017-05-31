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

package org.openecomp.appc.adapter.netconf.internal;

public class NetconfConstMessages {

    public static final String CAPABILITIES_START =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<hello xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                    "  <capabilities>\n";

    public static final String CAPABILITIES_BASE =
            "    <capability>urn:ietf:params:netconf:base:1.0</capability>\n";

    public static final String CAPABILITIES_END =
            "  </capabilities>\n" +
                    "</hello>";

    public static final String GET_RUNNING_CONFIG =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                    "    <get-config>\n" +
                    "        <source>\n" +
                    "            <running/>\n" +
                    "        </source>\n" +
                    "    </get-config>\n" +
                    "</rpc>";

    public static final String CLOSE_SESSION =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<rpc message-id=\"terminateConnection\" xmlns:netconf=\"urn:ietf:params:xml:ns:netconf:base:1.0\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                    "    <close-session/>\n" +
                    "</rpc>";
}
