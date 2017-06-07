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

package org.openecomp.appc.adapter.factory;
/**
 * The message service types that are available. Only DMaaP available
 **/
public enum MessageService {
    DMaaP("dmaap");

    private String val;

    private MessageService(String val) {
        this.val = val;
    }

    public String getValue() {
        return val;
    }

    /**
     * Tries to match a string to a MessageService. If no match is found, returns the default (DMaaP)
     *
     * @param input
     *            the string to try and match
     * @return A MessasgeService
     */
    public static MessageService parse(String input) {
        if (input != null) {
            for (MessageService ms : MessageService.values()) {
                if (ms.getValue().equals(input.toLowerCase())) {
                    return ms;
                }
            }
        }
        return MessageService.DMaaP; // Default
    }
}
