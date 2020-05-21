/*
 * ============LICENSE_START=============================================================================================================
 * Copyright (c) 2018 AT&T Intellectual Property.  All rights reserved.
 * ===================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 * ============LICENSE_END===============================================================================================================
 * 
 */
package org.onap.appc.flow.controller.utils;

import static org.junit.Assert.*;

import org.junit.Test;

public class EncryptionToolTest {

    @Test
    public final void testGetInstance() {
        String encrypted = EncryptionTool.getInstance().encrypt("AbCdEf");
        assertNotNull(encrypted);
    }

    @Test
    public final void testGetInstanceTwice() {
        String encrypted = EncryptionTool.getInstance().encrypt("GhIjKl");
        assertNotNull(encrypted);
        assertEquals(encrypted, "enc:BDczBmon");
        String encrypted2 = EncryptionTool.getInstance().encrypt("MNOPQR");
        assertNotNull(encrypted2);
        assertEquals(encrypted2, "enc:DhE1PHAZ");
    }

    @Test
    public void testAll() {
        String plainText = "AnyString123";
        String encrypted = EncryptionTool.getInstance().encrypt(plainText);
        assertNotEquals(plainText, encrypted);
        String dec = EncryptionTool.getInstance().decrypt(encrypted);
        assertNotEquals(encrypted, dec);
        assertEquals(plainText, dec);
        System.out.println(String.format("%s = [%s]", plainText, encrypted));
    }

    @Test
    public final void testDecrypt() {
        String decrypted = EncryptionTool.getInstance().decrypt("enc:BBczJmoH");
        assertNotNull(decrypted);
        assertEquals(decrypted, "GHIJKL");

        String decrypted2 = EncryptionTool.getInstance().decrypt("BBczJmoH");
        assertEquals(decrypted2, "BBczJmoH");
    }

    @Test
    public final void testEncrypt() {
        String encrypted = EncryptionTool.getInstance().encrypt("GHIJKL");
        assertEquals(encrypted, "enc:BBczJmoH");
    }

    @Test
    public final void testIsEncrytpedWithNull() {

        String encrypted = EncryptionTool.getInstance().encrypt(null);
        assertNull(encrypted);
    }
}
