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

package org.openecomp.appc.listener.demo.model;

public enum Status {
    /*
     * APP-C acknowledges that it has read the event off of the wire. This is the initial status of an OutgoingEvent
     */
    ACCEPTED("ACCEPTED"),

    /*
     * APP-C has finished processing the event without errors
     */
    SUCCESS("SUCCESS"),

    /*
     * APP-C has finished processing the event with errors
     */
    FAILURE("FAILURE");

    /**
     * Converts the string to an Status
     * 
     * @param value
     *            The string to try and convert. Is case insensitive
     * @return The status matching the string or null if no match was found.
     */
    public static Status toStatus(String value) {
        if (value != null) {
            for (Status e : values()) {
                if (e.getValue().toUpperCase().equals(value.toUpperCase())) {
                    return e;
                }
            }
        }

        return null;
    }

    private String value;

    private Status(String valueToUse) {
        value = valueToUse;
    }

    public final String getValue() {
        return value;
    }

}
