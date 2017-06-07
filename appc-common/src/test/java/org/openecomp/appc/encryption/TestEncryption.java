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

package org.openecomp.appc.encryption;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;
import org.openecomp.appc.encryption.EncryptionTool;

public class TestEncryption {

    @Test
    public void testEncryptionDecryption() {
        String plain = "AppC";
        String enc = EncryptionTool.getInstance().encrypt(plain);
        assertNotEquals(plain, enc);
        String dec = EncryptionTool.getInstance().decrypt(enc);
        assertNotEquals(enc, dec);
        assertEquals(plain, dec);
        System.out.println(String.format("%s = [%s]", plain, enc));
    }

}
