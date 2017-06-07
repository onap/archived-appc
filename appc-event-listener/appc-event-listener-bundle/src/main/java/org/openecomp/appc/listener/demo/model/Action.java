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

public enum Action {
    Restart("Restart"), Rebuild("Rebuild"), Migrate("Migrate"), Evacuate("Evacuate"), Snapshot("Snapshot"),modifyconfig("ModifyConfig");

    /**
     * Converts the string to an Action
     * 
     * @param value
     *            The string to try and convert. Is case insensitive
     * @return The action matching the string or null if no match was found.
     */
    public static Action toAction(String value) {
        if (value != null) {
            for (Action e : values()) {
                if (e.getValue().toUpperCase().equals(value.toUpperCase())) {
                    return e;
                }
            }
        }

        return null;
    }

    private String value;

    private Action(String valueToUse) {
        value = valueToUse;
    }

    public final String getValue() {
        return value;
    }
}
