/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 Ericsson
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
 *
 * ============LICENSE_END=========================================================
 */

package org.onap.tlv.sdc.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class PasswordsTest {

    @Test
    public void testHashPassword() {
        Passwords.main(new String[] {"TEST_PASSWORD"});
        assertEquals(2, Passwords.hashPassword("TEST_PASSWORD").split(":").length);
    }

    @Test
    public void testHashPasswordNull() {
        Passwords.main(new String[] {});
        assertNull(Passwords.hashPassword(null));
    }

    @Test
    public void testIsExpectedPassword() {
        assertFalse(Passwords.isExpectedPassword("", "1:1"));
    }

    @Test
    public void testIsExpectedPasswordNull() {
        assertFalse(Passwords.isExpectedPassword(null, "1234", "1234"));
    }

}
