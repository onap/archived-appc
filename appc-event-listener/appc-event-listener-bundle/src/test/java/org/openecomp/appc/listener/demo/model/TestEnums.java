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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.openecomp.appc.listener.demo.model.Action;
import org.openecomp.appc.listener.demo.model.Status;

public class TestEnums {

    @Test
    public void testAction() {
        assertEquals(Action.Rebuild, Action.toAction("Rebuild"));
        assertEquals(Action.Restart, Action.toAction("restart"));
        assertEquals(Action.Migrate, Action.toAction("MIGRATE"));
        assertEquals(Action.Evacuate, Action.toAction("Evacuate"));
        assertNull(Action.toAction("Unknown"));
        assertNull(Action.toAction(null));

        assertEquals(6, Action.values().length);
    }

    @Test
    public void testStatus() {

        assertEquals(Status.ACCEPTED, Status.toStatus("accepted"));
        assertEquals(Status.SUCCESS, Status.toStatus("SuCcEsS"));
        assertEquals(Status.FAILURE, Status.toStatus("Failure"));
        assertNull(Status.toStatus("Unknown"));
        assertNull(Status.toStatus(null));

        assertEquals(3, Status.values().length);

    }

}
